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
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunterze@gmail.com>
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

package org.dcm4che2.imageioimpl.plugins.rle;

import java.awt.image.BufferedImage;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since May 11, 2006
 * 
 */
public class RLEImageReader extends ImageReader {
    
    private int[] header = new int[16];
    private byte[] buf = new byte[8192];
    private long headerPos;
    private long bufOff;
    private int bufPos;
    private int bufLen;
    private ImageInputStream iis;
    
    public RLEImageReader(ImageReaderSpi originator) {
        super(originator);
    }

    public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        iis = (ImageInputStream) input;
    }
    
    public int getHeight(int imageIndex) throws IOException {
        return 1;
    }

    public int getWidth(int imageIndex) throws IOException {
        return 1;
    }

    public int getNumImages(boolean allowSearch) throws IOException {
        return 1;
    }

    public Iterator getImageTypes(int imageIndex) throws IOException {
        return null;
    }

    public IIOMetadata getStreamMetadata() throws IOException {
        return null;
    }

    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        return null;
    }

    public BufferedImage read(int imageIndex, ImageReadParam param)
            throws IOException {
        if (input == null)
            throw new IllegalStateException("Input not set");
        BufferedImage bi = RLEImageReader.getDestination(param);
        readRLEHeader();
        int nSegs = header[0];
        checkDestination(nSegs, bi);
        WritableRaster raster = bi.getRaster();
        DataBuffer db = raster.getDataBuffer();
        if (db instanceof DataBufferByte) {
            DataBufferByte dbb = (DataBufferByte) db;
            byte[][] bankData = dbb.getBankData();
            ComponentSampleModel sm = (ComponentSampleModel) bi.getSampleModel();
            int[] bankIndices = sm.getBankIndices();
            int[] bandOffsets = sm.getBandOffsets();
            int pixelStride = sm.getPixelStride();
            for (int i = 0; i < nSegs; i++) {
                 seekSegment(i+1);
                 unrle(bankData[bankIndices[i]], bandOffsets[i], pixelStride);                    
            } 
        } else{
            short[] ss = db instanceof DataBufferUShort 
                    ? ((DataBufferUShort) db).getData()
                    : ((DataBufferShort) db).getData();
            unrleMSB(ss);
            seekSegment(2);
            unrleLSB(ss);            
        }
        seekInputToEndOfRLEData();
        return bi;
    }

    private static BufferedImage getDestination(ImageReadParam param) {
        BufferedImage bi;
        if (param == null || (bi = param.getDestination()) == null) {
            throw new IllegalArgumentException(
                    "RLE Image Reader needs set ImageReadParam.destination");
        }
        SampleModel sm = bi.getSampleModel();
        if (!(sm instanceof ComponentSampleModel)) {
            throw new IllegalArgumentException(
                    "Unsupported Destination Sample Model: " + sm);            
        }
        return bi;
    }
    
    private void readRLEHeader() throws IOException {
        headerPos = iis.getStreamPosition();
        fillBuffer();
        if (bufLen < 64) {
            throw new EOFException();
        }
        for (int i = 0; i < 16; i++, bufPos += 4) {
            header[i] = buf[bufPos] & 0xff | (buf[bufPos+1] & 0xff) << 8 
                | (buf[bufPos+2] & 0xff) << 16 | (buf[bufPos+3] & 0xff) << 24;
        }
    }

    private void seekSegment(int seg) throws IOException {
        long segPos = headerPos + header[seg];
        if (segPos < bufOff) { // backwards seek should not happen!
            iis.seek(segPos);
            fillBuffer();            
        } else {
            while (segPos - bufOff >= bufLen) {
                fillBuffer();
            }
            bufPos = (int) (segPos - bufOff);
        }
    }

    private void seekInputToEndOfRLEData() throws IOException {
        iis.seek(bufOff + bufPos);        
    }
    
    private byte nextByte() throws IOException {
        if (bufPos == bufLen)
            fillBuffer();
        return buf[bufPos++];        
    }

    private void nextBytes(byte[] bs, int off, int len) throws IOException {
        int read, pos = 0;
        while (pos < len) {
            if (bufPos == bufLen)
                fillBuffer();
            read = Math.min(len - pos, bufLen - bufPos);
            System.arraycopy(buf, bufPos, bs, off + pos, read);
            bufPos += read;
            pos += read;
        }        
    }
    
    private void fillBuffer() throws IOException {
        bufOff = iis.getStreamPosition();
        bufPos = 0;
        bufLen = iis.read(buf);
        if (bufLen <= 0) {
            throw new EOFException();
        }        
    }
    
    private void checkDestination(int nSegs, BufferedImage bi)
    throws IIOException {
        WritableRaster raster = bi.getRaster();
        int nBands = raster.getNumBands();
        int dataType = raster.getTransferType();
        if (nSegs == 1 || nSegs == 3) {
            if (nBands == nSegs && dataType == DataBuffer.TYPE_BYTE)
                return;
        } else if (nSegs == 2) {
            if (nBands == 1 && (dataType == DataBuffer.TYPE_USHORT 
                    || dataType == DataBuffer.TYPE_SHORT))
                return;
        } else {
            throw new IIOException("Unsupported Number of RLE Segments: " 
                        + (nSegs & 0xffffffffL));            
        }
        throw new IIOException("Number of RLE Segments: " + nSegs
                + " incompatible with Destination[bands=" + nBands
                + ", data=" + raster.getDataBuffer() + "]");            
    }
    
    private void unrle(byte[] bs, int off, int pixelStride)
            throws IOException {
        if (pixelStride == 1) {
            unrle(bs);
            return;
        }
        int l, pos = off;
        byte b;
        while (pos < bs.length) {
            b = nextByte();
            if (b >= 0) {
                l = b + 1;
                for (int i = 0; i < l; i++, pos += pixelStride) {
                    bs[pos] = nextByte();
                }
            } else if (b != -128){
                l = -b + 1;
                b = nextByte();
                for (int i = 0; i < l; i++, pos += pixelStride) {
                    bs[pos] = b;
                }
            }
        }        
    }

    private void unrle(byte[] bs) throws IOException {
        int l, pos = 0;
        byte b;
        while (pos < bs.length) {
            b = nextByte();
            if (b >= 0) {
                l = b + 1;
                nextBytes(bs, pos, l);
                pos += l;
            } else if (b != -128){
                l = -b + 1;
                b = nextByte();
                Arrays.fill(bs, pos, pos + l, b);
                pos += l;
            }
        }        
    }

    private void unrleMSB(short[] ss)
    throws IOException {
        int l, pos = 0;
        short s;
        byte b;
        while (pos < ss.length) {
            b = nextByte();
            if (b >= 0) {
                l = b + 1;
                for (int i = 0; i < l; i++, pos++) {
                    ss[pos] = (short) (nextByte() << 8);
                }
             } else if (b != -128) {
                l = -b + 1;
                s = (short) (nextByte() << 8);
                for (int i = 0; i < l; i++, pos++) {
                    ss[pos] = s;
                }
             }
        }        
    }


    private void unrleLSB(short[] ss)
    throws IOException {
        int v, l, pos = 0;
        byte b;
        while (pos < ss.length) {
            b = nextByte();
            if (b >= 0) {
                l = b + 1;
                for (int i = 0; i < l; i++, pos++) {
                    ss[pos] |= (nextByte() & 0xff);
                }
             } else if (b != -128) {
                l = -b + 1;
                v = (nextByte() & 0xff);
                for (int i = 0; i < l; i++, pos++) {
                    ss[pos] |= v;
                }
             }
        }        
    }
}
