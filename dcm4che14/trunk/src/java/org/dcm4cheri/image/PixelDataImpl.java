/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package org.dcm4cheri.image;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Stack;

import javax.imageio.stream.ImageInputStream;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;
import org.dcm4che.image.PixelData;

/**
 * @author <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @author <a href="mailto:joseph@tiani.com">joseph foraci</a>
 * @since July 2003
 * @version $Revision$ $Date$
 * @see "DICOM Part 5: Data Structures and Encoding, Section 8. 'Encoding of Pixel,
 *      Overlay and Waveform Data', Annex D"
 */
public class PixelDataImpl
    implements PixelData
{
    private final int sampleMaskLS;
    private final int sampleMaskRS;
    private final int hOvlMask;
    private final int hOvlMaskRS;
    private final int lOvlMask;
    private final long len = -1L;
    private final ImageInputStream in;
    private final int cols;
    private final int rows;
    private final int nf;
    private final int frameSize;
    private final int size;
    private final int samplesPerFrame;
    private final int ba;
    private final int bs;
    private final int hb;
    private final int spp;
    private final boolean signed;
    private final boolean byPlane;
    private final boolean ow = true;
    private final String pmi;
    private final int bytesPerCell;
    private int data[][][];
    private int ovlData[][][];

    //readSample(), stream-related fields
    private final long initialReadPos;
    private final PixelDataStreamMark initialStreamState;
    private int bOff;  //bit off within cw
    private int bMask;  //mask of bits 'bOff' to 16 (inclusive) of 'cw'
    private int cw;  //current word (last one read)
    private long samplesPassed = 0;

    //mark-related fields
    private final static class PixelDataStreamMark
    {
        public final int offset, currentWord, mask;
        public final long samplesPassed;
        PixelDataStreamMark(int offset, int currentWord, int mask, long samplesPassed)
        {
            this.offset = offset;
            this.currentWord = currentWord;
            this.mask = mask;
            this.samplesPassed = samplesPassed;
        }
    }
    private final Stack markStack = new Stack();

    PixelDataImpl(Dataset ds, ImageInputStream in, ByteOrder byteOrder, int pixelDataVr)
    {
        data = ovlData = null;
        try {
            initialReadPos = in.getStreamPosition();
        }
        catch (IOException e) {
            throw new IllegalStateException("Could not determine current position in stream");
        }
        bOff = 16; //cause next word to be read
        initialStreamState = new PixelDataStreamMark(bOff, cw, bMask, 0);
        if((pmi = ds.getString(Tags.PhotometricInterpretation, "MONOCHROME2")) == null)
            throw new IllegalArgumentException("No photometric interpretation");
        if((ba = ds.getInt(Tags.BitsAllocated, -1)) == -1)
            throw new IllegalArgumentException("No Bits Allocated");
        if((bs = ds.getInt(Tags.BitsStored, -1)) == -1)
            throw new IllegalArgumentException("No Bits Stored");
        if((hb = ds.getInt(Tags.HighBit, -1)) == -1)
            throw new IllegalArgumentException("No High Bit");
        if(bs <= hb)
            throw new IllegalArgumentException("Bits Stored <= High Bit");
        int tmp;
        if((tmp = ds.getInt(Tags.PixelRepresentation, -1)) == -1)
            throw new IllegalArgumentException("No Pixel Representation");
        signed = tmp == 1;
        if((spp = ds.getInt(Tags.SamplesPerPixel, 1)) == -1)
            throw new IllegalArgumentException("No Samples Per Pixel");
        tmp = ds.getInt(Tags.PlanarConfiguration, 0);
        byPlane = tmp == 1;
        this.in = in;
        if((cols = ds.getInt(Tags.Columns, -1)) == -1)
            throw new IllegalArgumentException("No Columns");
        if((rows = ds.getInt(Tags.Rows, -1)) == -1)
            throw new IllegalArgumentException("No Rows");
        nf = ds.getInt(Tags.NumberOfFrames, 1);
        frameSize = cols * rows;
        samplesPerFrame = frameSize * spp;
        size = frameSize * nf;
        bytesPerCell = (ba >> 3) + ((ba % 8 != 0) ? 1 : 0);
        if(bytesPerCell > 4)
            throw new UnsupportedOperationException("Bits Allocated > 32 are not supported: " + ba);
        //shifts for getting pixel value from an int containing a cell
        sampleMaskLS = 32 - hb - 1;
        sampleMaskRS = 32 - bs;
        //masks and shifts for getting overlay bits from an int containing a cell
        hOvlMask = (1 << ba) - (1 << hb + 1);
        hOvlMaskRS = bs;
        lOvlMask = (1 << (hb + 1) - bs) - 1;
        //set byte-order
        if (pixelDataVr == VRs.OW)
            in.setByteOrder(byteOrder);
        else
            in.setByteOrder(ByteOrder.LITTLE_ENDIAN);
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

    public String getPhotometricInterp()
    {
        return pmi;
    }

    public int readSample()
        throws IOException
    {
        int cell = 0;
        int bRead = 0;
        
        while (bRead < ba) {
            if (bOff == 16) {
                cw = in.readShort();
                bOff = 0;
                bMask = -1 & 0xFFFF;
            }
            cell |= ((cw & bMask) >>> bOff) << bRead;
            bRead += 16 - bOff;
            bOff = 16;
        }
        if (bRead > ba) {
            bOff -= (bRead - ba); //bOff == 16 here
            bMask = (~((1 << bOff) - 1)) & 0xFFFF;
        }
        samplesPassed++;
        return getSampleBitsFromCell(cell);
    }

    public void readFully(int[] samples, int offset, int len)
        throws IOException
    {
        int[] arr = readFully(len);
        for (int i = 0; i < len; i++) {
            samples[i + offset] = arr[i];
        }
    }

    public int[] readFully(int len)
        throws IOException
    {
        int[] arr = new int[len];
        for (int i = 0; i < len; i++)
            arr[i] = readSample();
        return arr;
    }

    public void skipSamples(int n)
        throws IOException
    {
        skipSamples((long)n);
    }

    /*TODO try to make state of imageinput stream and this class reset to before
           skipSamples() was called! (could use mark() but in the case of no error,
           no matching call to reset() would be needed (doesn't seem correct))*/
    public void skipSamples(long n)
        throws IOException
    {
        long nWords = (ba * n) / 16;
        bOff += (ba * n) % 16;
        if (bOff > 16) {
            nWords++;
            bOff -= 16;
        }
        bMask = (~((1 << bOff) - 1)) & 0xFFFF;
        in.skipBytes((nWords - 1) * 2); //TODO should check if skipBytes() actually skips less than what it was told
        samplesPassed += n;
        cw = in.readShort();
    }

    public void skipToNextFrame()
        throws IOException
    {
        long numSamplesToSkip = samplesPerFrame - (samplesPassed % samplesPerFrame);
        skipSamples(numSamplesToSkip);
    }

    public void mark()
    {
        in.mark();
        markStack.push(new PixelDataStreamMark(bOff, cw, bMask, samplesPassed));
    }

    public void reset()
        throws IOException
    {
        PixelDataStreamMark mark;
        if (markStack.size() != 0) {
            in.reset();
            mark = (PixelDataStreamMark)markStack.pop();
            bOff = mark.offset;
            cw = mark.currentWord;
            bMask = mark.mask;
            samplesPassed = mark.samplesPassed;
        }
    }

    public void resetStream()
        throws IOException
    {
        in.seek(initialReadPos);
        bOff = initialStreamState.offset;
        cw = initialStreamState.currentWord;
        bMask = initialStreamState.mask;
        samplesPassed = initialStreamState.samplesPassed;
    }

    /**
     * Returns a sample from a specified <b>absolute pixel</b> and band.
     * @param pixel Index of pixel to retrieve
     * @param band Index of band [0..getSamplesPerPixel() - 1]
     * @return The sample value
     * @throws IOException On I/O error
     * @throws IllegalArgumentException If the required sample value has been passed
     */
    private final int getSampleInternal(int pixel, int band)
        throws IOException
    {
        int sampleIndex = (byPlane) ? (pixel / frameSize) * samplesPerFrame
                                      + (pixel % frameSize) * spp + band
                                    : pixel * spp + band;
        mark();
        int sample = 0;
        try {
            resetStream();
            skipSamples(sampleIndex);
            sample = readSample();
        }
        finally {
            reset();
        }
        return sample;
    }

    public int[] getPixel(int i, int j, int k)
        throws IOException
    {
        checkBounds(i, j, k, 0);
        int p[] = new int[spp];
        for(int s = 0; s < spp; s++)
            p[s] = getSampleInternal(i + j * cols + k * frameSize, s);
        return p;
    }

    public int getSample(int i, int j, int k, int band)
        throws IOException
    {
        checkBounds(i, j, k, band);
        return getSampleInternal(i + j * cols + k * frameSize, band);
    }

    private void checkBounds(int i, int j, int k, int band)
    {
        if(i < 0 || i >= cols || j < 0 || i >= rows || k < 0 || k >= nf || band < 0 || band >= spp)
            throw new ArrayIndexOutOfBoundsException("pixel[" + i + "," + j + ","
                + k + "], band = " + band + " is out of bounds");
    }

    private void checkBounds(int i, int band)
    {
        if(i < 0 || i >= size || band < 0 || band >= spp)
            throw new ArrayIndexOutOfBoundsException("pixel[" + i + "], band = "
                + band + "is out of bounds");
    }

    private final int getSampleBitsFromCell(int cell)
    {
        return signed ? (cell << sampleMaskLS) >> sampleMaskRS
                      : (cell << sampleMaskLS) >>> sampleMaskRS;
    }

    private final int getOverlayBitsFromCell(int cell)
    {
        return (cell & hOvlMask) >> hOvlMaskRS | cell & lOvlMask;
    }

    public DataBuffer getPixelDataBuffer(int frame)
    {
        if (frame < 0 || frame >= nf)
            throw new IllegalArgumentException("Invalid frame: " + frame);
        if (data != null)
            return new DataBufferInt(data[frame], spp);
        else
            throw new IllegalStateException("No pixel data has been read");
    }

    public DataBuffer getOverlayDataBuffer(int frame)
    {
        if (frame < 0 || frame >= nf)
            throw new IllegalArgumentException("Invalid frame: " + frame);
        if (ovlData != null)
            return new DataBufferInt(ovlData[frame], spp);
        else
            throw new IllegalStateException("No pixel data has been read or "
                + "pixel data has been read, but no overlay data was retrieved");
    }

    public DataBuffer readPixelData(int frame)
        throws IOException
    {
        return readPixelData(frame, false);
    }

    public DataBuffer readPixelData(int frame, boolean grabOverlayData)
        throws IOException
    {
        if (data != null)
            return getPixelDataBuffer(frame);
        if (grabOverlayData && (ovlData == null || ovlData.length != nf
                               || ovlData[0].length != spp
                               || ovlData[0][0].length != frameSize))
            throw new IllegalArgumentException("Overlay data should be [" + nf
                + "][" + spp + "][" + frameSize + "] entries");
        
        data = new int[nf][spp][frameSize];
        if (grabOverlayData)
            ovlData = new int[nf][spp][frameSize];
        
        final int numSamplesToRead = size * spp;
        int i = 0, ii, cell;
        int f, s = 0, p; //frame, sample, and pixel indicies
        
        while (i < numSamplesToRead) {
            cell = readSample();
            f = i / samplesPerFrame;
            ii = i % samplesPerFrame;
            if (byPlane) {
                s = ii / frameSize;
                p = ii % frameSize;
                data[f][s][p] = cell;
                if (grabOverlayData)
                    ovlData[f][s][p] = getOverlayBitsFromCell(cell);
            }
            else {
                p = ii / spp;
                data[f][s][p] = cell;
                if (grabOverlayData)
                    ovlData[f][s][p] = getOverlayBitsFromCell(cell);
                s = (s + 1) % spp;
            }
            i++;
        }
        return new DataBufferInt(data[frame], spp);

        /*System.out.println("byplane = " + byPlane);
        System.out.println("buff len = " + in.length());
        System.out.println("size = " + size);
        
        int nSamples = size * spp;
        int nSamplesPerFrame = frameSize * spp;
        data = new int[nf][spp][frameSize];
        int bitsRead = 0;
        int cell = 0;
        int s = 0;
        if(16 % ba == 0)
        {
            int rounds = 16 / ba;
            int cm = (1 << ba) - 1;
            for(int n = 0; n < nSamples;)
            {
                int w = in.readShort() & 0xffff;
                int r;
                for(int mb = r = 0; r < rounds && n < nSamples; mb += ba)
                {
                    cell = (w & cm << mb) >>> mb;
                    int f = n / nSamplesPerFrame;
                    int nn = n % nSamplesPerFrame;
                    if(byPlane)
                    {
                        s = nn / frameSize;
                        int p = nn % frameSize;
                        data[f][s][p] = getSampleBitsFromCell(cell);
                        if(grabOverlayData)
                            ovlData[f][s][p] = getOverlayBitsFromCell(cell);
                    } else
                    {
                        int p = nn / spp;
                        data[f][s][p] = getSampleBitsFromCell(cell);
                        if(grabOverlayData)
                            ovlData[f][s][p] = getOverlayBitsFromCell(cell);
                        s = (s + 1) % spp;
                    }
                    n++;
                    r++;
                }

            }

        } else
        if(ba < 16)
        {
            int mb = ba;
            int cm = (1 << mb) - 1;
            for(int n = 0; n < nSamples;)
            {
                int w = in.readShort() & 0xffff;
                cell |= (w & cm) << bitsRead;
                int f = n / nSamplesPerFrame;
                int nn = n % nSamplesPerFrame;
                if(byPlane)
                {
                    s = nn / frameSize;
                    int p = nn % frameSize;
                    data[f][s][p] = getSampleBitsFromCell(cell);
                    if(grabOverlayData)
                        ovlData[f][s][p] = getOverlayBitsFromCell(cell);
                } else
                {
                    int p = nn / spp;
                    data[f][s][p] = getSampleBitsFromCell(cell);
                    if(grabOverlayData)
                        ovlData[f][s][p] = getOverlayBitsFromCell(cell);
                    s = (s + 1) % spp;
                }
                while(ba <= 16 - mb) 
                {
                    n++;
                    mb += ba;
                    cm = (1 << mb) - 1;
                    cell = (w & cm) >>> mb - ba;
                    f = n / nSamplesPerFrame;
                    nn = n % nSamplesPerFrame;
                    if(byPlane)
                    {
                        s = nn / frameSize;
                        int p = nn % frameSize;
                        data[f][s][p] = getSampleBitsFromCell(cell);
                        if(grabOverlayData)
                            ovlData[f][s][p] = getOverlayBitsFromCell(cell);
                    } else
                    {
                        int p = nn / spp;
                        data[f][s][p] = getSampleBitsFromCell(cell);
                        if(grabOverlayData)
                            ovlData[f][s][p] = getOverlayBitsFromCell(cell);
                        s = (s + 1) % spp;
                    }
                }
                n++;
                cm = ~cm;
                cell = (w & cm) >>> mb;
                bitsRead = 16 - mb;
                mb = ba - bitsRead;
                cm = (1 << mb) - 1;
            }

        } else
        {
            int mb = 16;
            int cm = 65535;
            for(int n = 0; n < nSamples;)
            {
                int w = in.readShort() & 0xffff;
                if(ba - bitsRead <= 16)
                {
                    cell |= (w & cm) << bitsRead;
                    int f = n / nSamplesPerFrame;
                    int nn = n % nSamplesPerFrame;
                    if(byPlane)
                    {
                        s = nn / frameSize;
                        int p = nn % frameSize;
                        data[f][s][p] = getSampleBitsFromCell(cell);
                        if(grabOverlayData)
                            ovlData[f][s][p] = getOverlayBitsFromCell(cell);
                    } else
                    {
                        int p = nn / spp;
                        data[f][s][p] = getSampleBitsFromCell(cell);
                        if(grabOverlayData)
                            ovlData[f][s][p] = getOverlayBitsFromCell(cell);
                        s = (s + 1) % spp;
                    }
                    n++;
                    cm = ~cm;
                    cell = (w & cm) >>> mb;
                    bitsRead = 16 - mb;
                    mb = (ba - bitsRead) % 16;
                    cm = (1 << mb) - 1;
                } else
                {
                    cell |= w << bitsRead;
                    bitsRead += 16;
                }
            }

        }
        return new DataBufferInt(data[frame], spp);*/
    }

    public ByteBuffer getEncoded()
    {
        throw new UnsupportedOperationException();
    }
}
