package org.dcm4cheri.image;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;
import org.dcm4che.image.PixelData;

/**
 * @author jforaci
 */
public class PixelDataImpl implements PixelData
{
    private final int len;
    private final ByteBuffer buff;
    private final int cols, rows, nf, frameSize, size;
    private final int ba, bs, hb, spp;
    private final boolean signed, byPlane;
    private final boolean ow;
    private final String pmi;
    private final int bytesPerCell;
    
    PixelDataImpl(Dataset ds)
    {
        if ((pmi = ds.getString(Tags.PhotometricInterpretation)) == null)
            throw new IllegalArgumentException("No photometric interpretation");
        if ((ba = ds.getInt(Tags.BitsAllocated, -1)) == -1)
            throw new IllegalArgumentException("No Bits Allocated");
        if ((bs = ds.getInt(Tags.BitsStored, -1)) == -1)
            throw new IllegalArgumentException("No Bits Stored");
        if ((hb = ds.getInt(Tags.HighBit, -1)) == -1)
            throw new IllegalArgumentException("No High Bit");
        int tmp;
        if ((tmp = ds.getInt(Tags.PixelRepresentation, -1)) == -1)
            throw new IllegalArgumentException("No Pixel Representation");
        signed = (tmp == 1);
        if ((spp = ds.getInt(Tags.SamplesPerPixel, -1)) == -1)
            throw new IllegalArgumentException("No Samples Per Pixel");
        if ((tmp = ds.getInt(Tags.PlanarConfiguration, -1)) == -1)
            throw new IllegalArgumentException("No Planar Configuration");
        byPlane = (tmp == 1);
        if (ds.getByteBuffer(Tags.PixelData) == null)
            throw new IllegalArgumentException("No Pixel Data");
        buff = ds.getByteBuffer(Tags.PixelData).duplicate();
        len = buff.limit();
        if ((cols = ds.getInt(Tags.Columns, -1)) == -1)
            throw new IllegalArgumentException("No Columns");
        if ((rows = ds.getInt(Tags.Rows, -1)) == -1)
            throw new IllegalArgumentException("No Rows");
        nf = ds.getInt(Tags.NumberOfFrames, 1);
        frameSize = cols * rows;
        size = frameSize * nf;
        ow = (ds.get(Tags.PixelData).vr() == VRs.OW);
        bytesPerCell = (ba >> 3) + ((ba % 8 == 0) ? 0 : 1);
        if (bytesPerCell > 4)
            throw new UnsupportedOperationException("Bits Allocated > 32 are not supported: " + ba);
    }

    public int getBitsAllocated()
    {
        return ba;
    }

    public int getBitsStored()
    {
        return bs;
    }

    public int getHighBit()
    {
        return hb;
    }

    public boolean isSigned()
    {
        return signed;
    }

    public int getSamplesPerPixel()
    {
        return spp;
    }

    public String getPhotometricInterp() {
        return pmi;
    }

    public final int[] getPixel(int i, int j, int k)
    {
        checkBounds(i, j, k, 0);
        int[] p = new int[spp];
        for (int s = 0; s < spp; s++)
            p[s] = getSampleInternal(i, j, k, s);
        return p;
    }

    private final int getSampleInternal(int i, int band)
    {
        int off = (byPlane) ? (int)((long)band * frameSize + i)
                            : (int)(((long)i * spp + band) * ba);
        int bytepos = off >>> 3;
        int bitpos = off % 8;
        int mask = (0xFF << bitpos);
        int bitsLeft = ba, bitsToRead = 8 - bitpos;
        int cell = 0, abyte, shift = -bitpos;
        
        do {
            if (ow) {
                if ((bytepos & 0x1) != 0)
                    abyte = (buff.getShort(bytepos - 1) >> 8);
                else
                    abyte = buff.getShort(bytepos);
            }
            else
                abyte = buff.get(bytepos);
            if (shift < 0) {
                cell = (abyte & mask) >> -shift;
                shift = 0;
            }
            else
                cell |= (abyte & mask) << shift;
            shift += 8;
            bitsLeft -= bitsToRead;
            if (bitsLeft > 8)
                mask = 0xFF;
            else
                mask = (1 << bitsLeft) - 1;
            bitsToRead = 8;
        } while (bitsLeft > 0);

        return cell;
    }

    private final int getSampleInternal(int i, int j, int k, int band)
    {
        return getSampleInternal(i + j*cols + k*frameSize, band);
    }

    public final int getSample(int i, int j, int k, int band)
    {
        checkBounds(i, j, k, band);
        return getSampleInternal(i, j, k, band);
    }

    private void checkBounds(int i, int j, int k, int band)
    {
        if (i < 0 || i >= cols
            || j < 0 || i >= rows
            || k < 0 || k >= nf
            || band < 0 || band >= spp)
        throw new ArrayIndexOutOfBoundsException("pixel[" + i + "," + j + "," + k
                                                 + "] is out of bounds");
    }

    private void checkBounds(int i, int band)
    {
        if (i < 0 || i >= size
            || band < 0 || band >= spp)
        throw new ArrayIndexOutOfBoundsException("pixel[" + i
                                                 + "] is out of bounds");
    }

    public DataBuffer getPixelData()
    {
        DataBuffer db = new DataBufferInt(size, spp);
        int s;
        
        for (int i = 0; i < size; i++) {
            for (s = 0; s < spp; s++) {
                db.setElem(s, i, getSampleInternal(i, s));
            }
        }
        
        return null;
    }
    
    public ByteBuffer getEncoded()
    {
        return buff;
    }
}
