package org.dcm4che.image;

import java.awt.image.DataBuffer;
import java.nio.ByteBuffer;

/**
 * @author jforaci
 */
public interface PixelData
{
    public int getBitsAllocated();
    public int getBitsStored();
    public int getHighBit();
    public boolean isSigned();
    public int getSamplesPerPixel();
    public String getPhotometricInterp();
    public int[] getPixel(int i, int j, int k);
    public int getSample(int i, int j, int k, int band);
    public DataBuffer getPixelData();
    public ByteBuffer getEncoded();
}
