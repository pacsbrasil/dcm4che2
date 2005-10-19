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

package org.dcm4che2.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.zip.DeflaterOutputStream;

import org.apache.log4j.Logger;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.TransferSyntax;
import org.dcm4che2.data.VR;
import org.dcm4che2.util.ByteUtils;

public class DicomOutputStream extends FilterOutputStream
{

    private static Logger log = Logger.getLogger(DicomOutputStream.class);

    private static final int PREAMBLE_LENGTH = 128;

    private TransferSyntax ts = TransferSyntax.ExplicitVRLittleEndian;
    private boolean includeGroupLength = false;
    private boolean explicitItemLength = false;
    private boolean explicitSequenceLength = false;
    private boolean explicitItemLengthIfZero = true;
    private boolean explicitSequenceLengthIfZero = true;

    byte[] header = new byte[8];
    byte[] preamble = new byte[PREAMBLE_LENGTH];

    long pos = 0;

    public DicomOutputStream(OutputStream out)
    {
        super(out);
    }

    public final long getStreamPosition()
    {
        return pos;
    }

    public final void setStreamPosition(long pos)
    {
        this.pos = pos;
    }

    public final TransferSyntax getTransferSyntax()
    {
        return ts;
    }

    public final void setTransferSyntax(TransferSyntax ts)
    {
        this.ts = ts;
    }

    public void write(byte[] b, int off, int len) throws IOException
    {
        out.write(b, off, len);
        pos += len;
    }

    public void write(int b) throws IOException
    {
        out.write(b);
        ++pos;
    }

    public final boolean isExplicitItemLength()
    {
        return explicitItemLength;
    }

    public final void setExplicitItemLength(boolean explicitItemLength)
    {
        this.explicitItemLength = explicitItemLength;
    }

    public final boolean isExplicitItemLengthIfZero()
    {
        return explicitItemLengthIfZero;
    }

    public final void setExplicitItemLengthIfZero(
            boolean explicitItemLengthIfZero)
    {
        this.explicitItemLengthIfZero = explicitItemLengthIfZero;
    }

    public final boolean isExplicitSequenceLength()
    {
        return explicitSequenceLength;
    }

    public final void setExplicitSequenceLength(boolean explicitSequenceLength)
    {
        this.explicitSequenceLength = explicitSequenceLength;
    }

    public final boolean isExplicitSequenceLengthIfZero()
    {
        return explicitSequenceLengthIfZero;
    }

    public final void setExplicitSequenceLengthIfZero(
            boolean explicitSequenceLengthIfZero)
    {
        this.explicitSequenceLengthIfZero = explicitSequenceLengthIfZero;
    }

    public final boolean isIncludeGroupLength()
    {
        return includeGroupLength;
    }

    public final void setIncludeGroupLength(boolean includeGroupLength)
    {
        this.includeGroupLength = includeGroupLength;
    }

    public void writeDicomObject(DicomObject attrs) throws IOException
    {
        this.ts = TransferSyntax.ExplicitVRLittleEndian;
        writeElements(attrs.iterator(), false, null);
        writeHeader(Tag.ItemDelimitationItem, null, 0);
    }

    public void writeCommand(DicomObject attrs) throws IOException
    {
        this.ts = TransferSyntax.ImplicitVRLittleEndian;
        writeElements(attrs.commandIterator(), true, new ItemInfo(attrs
                .commandIterator(), true));
    }

    private void writeGroupLength(int tag, int length) throws IOException
    {
        writeHeader(tag, VR.UL, 4);
        write(VR.UL.toBytes(length, ts.bigEndian()), 0, 4);
    }

    public void writeDicomFile(DicomObject attrs) throws IOException
    {
        writeFileMetaInformation(attrs);
        writeDataset(attrs);
    }

    public void writeFileMetaInformation(DicomObject attrs) throws IOException
    {
        write(preamble, 0, 128);
        write('D');
        write('I');
        write('C');
        write('M');
        this.ts = TransferSyntax.ExplicitVRLittleEndian;
        writeElements(attrs.fileMetaInfoIterator(), true, new ItemInfo(attrs
                .fileMetaInfoIterator(), true));
    }

    public void writeDataset(DicomObject attrs) throws IOException
    {
        writeDataset(attrs, attrs.getTransferSyntax());
    }

    public void writeDataset(DicomObject attrs, TransferSyntax ts)
            throws IOException
    {
        if (ts.deflated())
            out = new DeflaterOutputStream(out);
        this.ts = ts;
        writeElements(attrs.datasetIterator(), includeGroupLength,
                createItemInfo(attrs));
        if (ts.deflated())
            ((DeflaterOutputStream) out).finish();
    }

    private ItemInfo createItemInfo(DicomObject attrs)
    {
        if (needItemInfo())
            return new ItemInfo(attrs.datasetIterator(), includeGroupLength);
        return null;
    }

    private boolean needItemInfo()
    {
        return includeGroupLength || explicitItemLength
                || explicitSequenceLength;
    }

    public void writeItem(DicomObject item, TransferSyntax ts)
            throws IOException
    {
        this.ts = ts;
        writeItem(item, createItemInfo(item));
    }

    private void writeItem(DicomObject item, ItemInfo itemInfo)
            throws IOException
    {
        item.setItemOffset(pos);
        int len;
        if (item.isEmpty())
        {
            len = explicitItemLengthIfZero ? 0 : -1;
        } else
        {
            len = explicitItemLength ? itemInfo.len : -1;
        }
        writeHeader(Tag.Item, null, len);
        writeElements(item.iterator(), includeGroupLength, itemInfo);
        if (len == -1)
        {
            writeHeader(Tag.ItemDelimitationItem, null, 0);
        }
    }

    private void writeElements(Iterator itr, boolean groupLength1,
            ItemInfo itemInfo) throws IOException
    {
        int gggg0 = -1;
        int gri = -1;
        int sqi = -1;
        while (itr.hasNext())
        {
            DicomElement a = (DicomElement) itr.next();
            if (groupLength1)
            {
                int gggg = a.tag() & 0xffff0000;
                if (gggg != gggg0)
                {
                    gggg0 = gggg;
                    writeGroupLength(gggg, itemInfo.grlen[++gri]);
                }
            }
            final VR vr = a.vr();
            int len = a.length();
            if (vr == VR.SQ)
            {
                if (len == -1 && explicitSequenceLength)
                {
                    len = itemInfo.sqlen[++sqi];
                } else if (len == 0 && !explicitSequenceLengthIfZero)
                {
                    len = -1;
                }
            }
            writeHeader(a.tag(), vr, len);
            a.bigEndian(ts.bigEndian());
            if (a.hasItems())
            {
                if (vr == VR.SQ)
                {
                    for (int i = 0, n = a.countItems(); i < n; i++)
                    {
                        DicomObject item = a.getDicomObject(i);
                        ItemInfo childItemInfo = itemInfo != null ? (ItemInfo) itemInfo.childs
                                .removeFirst()
                                : null;
                        writeItem(item, childItemInfo);
                    }
                } else
                {
                    for (int i = 0, n = a.countItems(); i < n; i++)
                    {
                        byte[] val = a.getFragment(i);
                        writeHeader(Tag.Item, null, (val.length + 1) & ~1);
                        write(val);
                        if ((val.length & 1) != 0)
                            write(0);
                    }
                }
            } else if (len > 0)
            {
                byte[] val = a.getBytes();
                write(val);
                if ((val.length & 1) != 0)
                    write(vr.padding());
            }
            if (len == -1)
            {
                writeHeader(Tag.SequenceDelimitationItem, null, 0);
            }
        }
    }

    public void writeHeader(int tag, VR vr, int len) throws IOException
    {
        if (ts.bigEndian())
        {
            ByteUtils.tag2bytesBE(tag, header, 0);
        } else
        {
            ByteUtils.tag2bytesLE(tag, header, 0);
        }
        int off = 0;
        if (vr != null && ts.explicitVR())
        {
            ByteUtils.ushort2bytesBE(vr.code(), header, 4);
            if (vr.explicitVRHeaderLength() == 8)
            {
                if (ts.bigEndian())
                {
                    ByteUtils.ushort2bytesBE(len, header, 6);
                } else
                {
                    ByteUtils.ushort2bytesLE(len, header, 6);
                }
                write(header, 0, 8);
                return;
            }
            header[6] = header[7] = 0;
            write(header, 0, 8);
            off = 4;
        }
        if (ts.bigEndian())
        {
            ByteUtils.int2bytesBE(len, header, 4);
        } else
        {
            ByteUtils.int2bytesLE(len, header, 4);
        }
        write(header, off, 8 - off);
    }

    private class ItemInfo
    {
        int len = 0;
        int[] grlen =
        {
            0
        };
        int[] sqlen = {};
        LinkedList childs = null;

        ItemInfo(Iterator it, boolean groupLength1)
        {
            int gggg0 = -1;
            int gri = -1;
            int sqi = -1;
            while (it.hasNext())
            {
                DicomElement a = (DicomElement) it.next();
                final VR vr = a.vr();
                int vlen = a.length();
                if (vlen == -1)
                {
                    if (a.vr() == VR.SQ)
                    {
                        vlen = calcItemSqLen(a);
                        if (explicitSequenceLength)
                        {
                            if (++sqi >= sqlen.length)
                            {
                                sqlen = realloc(sqlen);
                            }
                            sqlen[sqi] = vlen;
                        }
                    } else
                    {
                        vlen = calcFragSqLen(a);
                    }
                } else if (a.vr() == VR.SQ)
                { // vlen == 0
                    if (!explicitSequenceLengthIfZero)
                        vlen = 8;
                }
                final int alen = (ts.explicitVR() ? vr.explicitVRHeaderLength()
                        : 8)
                        + vlen;
                len += alen;
                final int gggg = a.tag() & 0xffff0000;
                if (groupLength1)
                {
                    if (gggg != gggg0)
                    {
                        gggg0 = gggg;
                        len += 12;
                        if (++gri >= grlen.length)
                        {
                            grlen = realloc(grlen);
                        }
                    }
                    grlen[gri] += alen;
                }
            }
            if (!(len == 0 ? explicitItemLengthIfZero : explicitItemLength))
            {
                len += 8;
            }
        }

        private int calcFragSqLen(DicomElement a)
        {
            int l = 8;
            for (int i = 0, n = a.countItems(); i < n; ++i)
            {
                byte[] b = a.getFragment(i);
                l += 8 + (b.length + 1) & ~1;
            }
            return l;
        }

        private int calcItemSqLen(DicomElement a)
        {
            int l = explicitSequenceLength ? 0 : 8;
            for (int i = 0, n = a.countItems(); i < n; ++i)
            {
                DicomObject item = a.getDicomObject(i);
                ItemInfo itemInfo = new ItemInfo(item.iterator(),
                        includeGroupLength);
                if (childs == null) // lazy allocation
                    childs = new LinkedList();
                childs.add(itemInfo);
                l += 8 + itemInfo.len;
            }
            return l;
        }
    }

    private static int[] realloc(int[] src)
    {
        int[] dest = new int[src.length + 10];
        System.arraycopy(src, 0, dest, 0, src.length);
        return dest;
    }

}
