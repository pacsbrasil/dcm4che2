package org.dcm4che.image;

import java.nio.ByteOrder;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

public class PixelDataDescription
{
    protected final int cols;
    protected final int rows;
    protected final int nf;
    protected final int frameSize;
    protected final int size;
    protected final int samplesPerFrame;
    protected final int ba;
    protected final int bs;
    protected final int hb;
    protected final int spp;
    protected final boolean signed;
    protected final boolean byPlane;
    protected final String pmi;
    protected final ByteOrder byteOrder;
    protected final int pixelDataVr;

    public PixelDataDescription(Dataset ds, ByteOrder byteOrder, int pixelDataVr)
    {
        if ((pmi = ds.getString(Tags.PhotometricInterpretation, "MONOCHROME2")) == null)
            throw new IllegalArgumentException("No photometric interpretation");
        if ((ba = ds.getInt(Tags.BitsAllocated, -1)) == -1)
            throw new IllegalArgumentException("No Bits Allocated");
        if ((bs = ds.getInt(Tags.BitsStored, -1)) == -1)
            throw new IllegalArgumentException("No Bits Stored");
        if ((hb = ds.getInt(Tags.HighBit, -1)) == -1)
            throw new IllegalArgumentException("No High Bit");
        if (bs <= hb)
            throw new IllegalArgumentException("Bits Stored <= High Bit");
        int tmp;
        if ((tmp = ds.getInt(Tags.PixelRepresentation, -1)) == -1)
            throw new IllegalArgumentException("No Pixel Representation");
        signed = tmp == 1;
        if ((spp = ds.getInt(Tags.SamplesPerPixel, 1)) == -1)
            throw new IllegalArgumentException("No Samples Per Pixel");
        tmp = ds.getInt(Tags.PlanarConfiguration, 0);
        byPlane = tmp == 1;
        if ((cols = ds.getInt(Tags.Columns, -1)) == -1)
            throw new IllegalArgumentException("No Columns");
        if ((rows = ds.getInt(Tags.Rows, -1)) == -1)
            throw new IllegalArgumentException("No Rows");
        nf = ds.getInt(Tags.NumberOfFrames, 1);
        frameSize = cols * rows;
        samplesPerFrame = frameSize * spp;
        size = frameSize * nf;
        if (ba > 32)
            throw new UnsupportedOperationException("Bits Allocated > 32 are not supported: " + ba);
        this.byteOrder = byteOrder;
        this.pixelDataVr = pixelDataVr;
    }

    public long calcPixelDataLength()
    {
        long pixelDataLen = (long)cols * rows * nf * spp * ba;
        if (pixelDataLen % 8 != 0)
            pixelDataLen = (pixelDataLen >>> 3) + 1;
        else
            pixelDataLen = pixelDataLen >>> 3;
        return ((pixelDataLen & 0x1) == 0) ? pixelDataLen : pixelDataLen + 1;
    }

    public int getBitsAllocated() {
        return ba;
    }

    public int getBitsStored() {
        return bs;
    }

    public boolean isByPlane() {
        return byPlane;
    }

    public ByteOrder getByteOrder() {
        return byteOrder;
    }

    public int getCols() {
        return cols;
    }

    public int getFrameSize() {
        return frameSize;
    }

    public int getHighBit() {
        return hb;
    }

    public int getNumberOfFrames() {
        return nf;
    }

    public int getPixelDataVr() {
        return pixelDataVr;
    }

    public String getPmi() {
        return pmi;
    }

    public int getRows() {
        return rows;
    }

    public int getSamplesPerFrame() {
        return samplesPerFrame;
    }

    public boolean isSigned() {
        return signed;
    }

    public int getSize() {
        return size;
    }

    public int getSamplesPerPixel() {
        return spp;
    }
}
