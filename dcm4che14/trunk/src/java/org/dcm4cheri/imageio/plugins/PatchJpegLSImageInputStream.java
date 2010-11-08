/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa HealthCare.
 * Portions created by the Initial Developer are Copyright (C) 2010
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4cheri.imageio.plugins;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteOrder;

import javax.imageio.stream.IIOByteBuffer;
import javax.imageio.stream.ImageInputStream;

import org.apache.log4j.Logger;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date:: xxxx-xx-xx $
 * @since Nov 4, 2010
 */
public class PatchJpegLSImageInputStream implements ImageInputStream {

    private static final Logger log =
            Logger.getLogger(PatchJpegLSImageInputStream.class);

    private static final int SOI = 0xffd8;
    private static final int SOF55 = 0xfff7;
    private static final int LSE = 0xfff8;
    private static final int SOS = 0xffda;
    private static final byte[] LSE_13 = {
        (byte) 0xff, (byte) 0xf8, (byte) 0x00, (byte) 0x0D,
        (byte) 0x01, 
        (byte) 0x1f, (byte) 0xff,
        (byte) 0x00, (byte) 0x22,  // T1 = 34
        (byte) 0x00, (byte) 0x83,  // T2 = 131
        (byte) 0x02, (byte) 0x24,  // T3 = 548
        (byte) 0x00, (byte) 0x40,
        (byte) 0xff, (byte) 0xda
    };
    private static final byte[] LSE_14 = {
        (byte) 0xff, (byte) 0xf8, (byte) 0x00, (byte) 0x0D,
        (byte) 0x01, 
        (byte) 0x3f, (byte) 0xff,
        (byte) 0x00, (byte) 0x42, // T1 = 66
        (byte) 0x01, (byte) 0x03, // T2 = 259
        (byte) 0x04, (byte) 0x44, // T3 = 1092
        (byte) 0x00, (byte) 0x40,
        (byte) 0xff, (byte) 0xda
    };
    private static final byte[] LSE_15 = {
        (byte) 0xff, (byte) 0xf8, (byte) 0x00, (byte) 0x0D,
        (byte) 0x01, 
        (byte) 0x7f, (byte) 0xff,
        (byte) 0x00, (byte) 0x82, // T1 = 130
        (byte) 0x02, (byte) 0x03, // T2 = 515
        (byte) 0x08, (byte) 0x84, // T3 = 2180
        (byte) 0x00, (byte) 0x40,
        (byte) 0xff, (byte) 0xda
    };
    private static final byte[] LSE_16 = {
        (byte) 0xff, (byte) 0xf8, (byte) 0x00, (byte) 0x0D,
        (byte) 0x01, 
        (byte) 0xff, (byte) 0xff,
        (byte) 0x01, (byte) 0x02, // T1 = 258
        (byte) 0x04, (byte) 0x03, // T2 = 1027
        (byte) 0x11, (byte) 0x04, // T3 = 4356
        (byte) 0x00, (byte) 0x40,
        (byte) 0xff, (byte) 0xda
    };

    private final ImageInputStream iis;
    private final byte[] patch;
    private int patchPos;
    private int markPos;

    public PatchJpegLSImageInputStream(ImageInputStream iis)
            throws IOException {
        if (iis == null)
            throw new NullPointerException("iis");
        this.iis = iis;
        this.patch = selectPatch();
    }

    private byte[] selectPatch() throws IOException {
        byte[] jpegheader = new byte[17];
        iis.readFully(jpegheader);
        if (toInt(jpegheader, 0) != SOI) {
            log.warn("SOI marker is missing - do not patch JPEG LS");
            return jpegheader;
        }
        int marker = toInt(jpegheader, 2);
        if (marker != SOF55) {
            log.warn(marker == LSE
                    ? "contains already LSE marker segment "
                            + "- do not patch JPEG LS"
                    : "SOI marker is not followed by JPEG-LS SOF marker "
                            + "- do not patch JPEG LS");
            return jpegheader;
        }
        if (toInt(jpegheader, 4) != 11) {
            log.warn("unexpected length of JPEG-LS SOF marker segment "
                    + "- do not patch JPEG LS");
            return jpegheader;
        }
        marker = toInt(jpegheader, 15);
        if (marker != SOS) {
            log.warn(marker == LSE
                ? "contains already LSE marker segment "
                    + "- do not patch JPEG LS"
                : "JPEG-LS SOF marker segment is not followed by SOS marker "
                    + "- do not patch JPEG LS");
            return jpegheader;
        }
        switch (jpegheader[6]) {
        case 13:
            log.info("Patch JPEG LS 13-bit with "
                    + "LSE segment(T1=34, T2=131, T3=548)");
            return makePatch(jpegheader, LSE_13);
        case 14:
            log.info("Patch JPEG LS 14-bit with "
                    + "LSE segment(T1=66, T2=259, T3=1092)");
            return makePatch(jpegheader, LSE_14);
        case 15:
            log.info("Patch JPEG LS 15-bit with "
                    + "LSE segment(T1=130, T2=515, T3=2180)");
            return makePatch(jpegheader, LSE_15);
        case 16:
            log.info("Patch JPEG LS 16-bit with "
                    + "LSE segment(T1=258, T2=1027, T3=4356)");
            return makePatch(jpegheader, LSE_16);
        }
        return jpegheader;
    }

    private static int toInt(byte[] b, int off) {
        return (b[off] & 0xff) << 8 | (b[off+1] & 0xff);
    }

    private byte[] makePatch(byte[] jpegheader, byte[] lse) {
        byte[] patch = new byte[32];
        System.arraycopy(jpegheader, 0, patch, 0, 15);
        System.arraycopy(lse, 0, patch, 15, 17);
        return patch;
    }

    public void close() throws IOException {
        iis.close();
    }

    public void flush() throws IOException {
        iis.flush();
    }

    public void flushBefore(long pos) throws IOException {
        iis.flushBefore(17 + Math.max(pos - patch.length, 0));
    }

    public int getBitOffset() throws IOException {
        return iis.getBitOffset();
    }

    public ByteOrder getByteOrder() {
        return iis.getByteOrder();
    }

    public long getFlushedPosition() {
        return iis.getFlushedPosition() + patchPos - 17;
    }

    public long getStreamPosition() throws IOException {
        return iis.getStreamPosition() + patchPos - 17;
    }

    public boolean isCached() {
        return iis.isCached();
    }

    public boolean isCachedFile() {
        return iis.isCachedFile();
    }

    public boolean isCachedMemory() {
        return iis.isCachedMemory();
    }

    public long length() throws IOException {
        return iis.length() + patch.length - 17;
    }

    public void mark() {
        markPos = patchPos;
        iis.mark();
    }

    public int read() throws IOException {
        return remainingPatch() > 0 ? patch[patchPos++] & 0xff : iis.read();
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int l1 = Math.min(remainingPatch(), len);
        System.arraycopy(patch, patchPos, b, off, l1);
        patchPos += l1;
        return l1 + iis.read(b, off+l1, len-l1);
    }

    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public int readBit() throws IOException {
        noRemainingPatch();
        return iis.readBit();
    }

    public long readBits(int numBits) throws IOException {
        noRemainingPatch();
        return iis.readBits(numBits);
    }

    public boolean readBoolean() throws IOException {
        return (readUnsignedByte() != 0);
    }

    public byte readByte() throws IOException {
        return (byte)readUnsignedByte();
    }

    public void readBytes(IIOByteBuffer buf, int len) throws IOException {
        throw new UnsupportedOperationException();
    }

    public char readChar() throws IOException {
        throw new UnsupportedOperationException();
    }

    public double readDouble() throws IOException {
        throw new UnsupportedOperationException();
    }

    public float readFloat() throws IOException {
        throw new UnsupportedOperationException();
    }

    public void readFully(byte[] b, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > b.length || off + len < 0) {
            throw new IndexOutOfBoundsException
                ("off < 0 || len < 0 || off + len > b.length!");
        }

        int remainingPatch = remainingPatch();
        if (remainingPatch > 0) {
            int l = Math.min(remainingPatch, len);
            System.arraycopy(patch, patchPos, b, off, l);
            patchPos += l;
            off += l;
            len -= l;
        }
        iis.readFully(b, off, len);
    }

    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    public void readFully(char[] c, int off, int len) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void readFully(double[] d, int off, int len) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void readFully(float[] f, int off, int len) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void readFully(int[] i, int off, int len) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void readFully(long[] l, int off, int len) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void readFully(short[] s, int off, int len) throws IOException {
        throw new UnsupportedOperationException();
    }

    public int readInt() throws IOException {
        throw new UnsupportedOperationException();
    }

    public String readLine() throws IOException {
        throw new UnsupportedOperationException();
    }

    public long readLong() throws IOException {
        throw new UnsupportedOperationException();
    }

    public short readShort() throws IOException {
        throw new UnsupportedOperationException();
    }

    public int readUnsignedByte() throws IOException {
        int ch = this.read();
        if (ch < 0) {
            throw new EOFException();
        }
        return ch;
    }

    public long readUnsignedInt() throws IOException {
        throw new UnsupportedOperationException();
    }

    public int readUnsignedShort() throws IOException {
        throw new UnsupportedOperationException();
    }

    public String readUTF() throws IOException {
        throw new UnsupportedOperationException();
    }

    public void reset() throws IOException {
        patchPos = markPos;
        iis.reset();
    }

    public void seek(long pos) throws IOException {
        patchPos = (int) Math.min(pos, patch.length);
        iis.seek(17 + Math.max(pos - patch.length, 0));
    }

    public void setBitOffset(int bitOffset) throws IOException {
        noRemainingPatch();
        iis.setBitOffset(bitOffset);
    }

    public void setByteOrder(ByteOrder byteOrder) {
        iis.setByteOrder(byteOrder);
    }

    public int skipBytes(int n) throws IOException {
        int n1 = Math.min(remainingPatch(), n);
        patchPos += n1;
        return n1 + iis.skipBytes(n-n1);
    }

    public long skipBytes(long n) throws IOException {
        int n1 = (int) Math.min(remainingPatch(), n);
        patchPos += n1;
        return n1 + iis.skipBytes(n-n1);
    }

    private int remainingPatch() {
        return patch.length - patchPos;
    }

    private void noRemainingPatch() {
        if (remainingPatch() > 0)
            throw new IllegalStateException();
    }

}
