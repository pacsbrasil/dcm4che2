package org.dcm4cheri.image;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import org.dcm4che.dict.VRs;
import org.dcm4che.image.PixelDataDescription;
import org.dcm4che.image.PixelDataWriter;

public class PixelDataWriterImpl implements PixelDataWriter
{
    private final ImageOutputStream out;
    private final int[][][] data;
    private final int sampleOffset;
    private final boolean containsOverlayData;
    private final PixelDataDescription pdDesc;
    //fields copied from the initialized pixel data description
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
    private final String pmi;
    private final ByteOrder byteOrder;
    private final int pixelDataVr;

    PixelDataWriterImpl(int[][][] data, boolean containsOverlayData, PixelDataDescription desc, ImageOutputStream out)
    {
        this.containsOverlayData = containsOverlayData;
        pdDesc = desc;
        //attributes from pixel data description
        cols = desc.getCols();
        rows = desc.getRows();
        nf = desc.getNumberOfFrames();
        frameSize = desc.getFrameSize();
        size = desc.getSize();
        samplesPerFrame = desc.getSamplesPerFrame();
        ba = desc.getBitsAllocated();
        bs = desc.getBitsStored();
        hb = desc.getHighBit();
        spp = desc.getSamplesPerPixel();
        signed = desc.isSigned();
        byPlane = desc.isByPlane();
        pmi = desc.getPmi();
        byteOrder = desc.getByteOrder();
        pixelDataVr = desc.getPixelDataVr();
        //
        this.out = out;
        this.data = data;
        //offset in bits from low-order bit of a sample
        sampleOffset = hb - bs + 1;
        //set byte-order
        if (pixelDataVr == VRs.OW)
            out.setByteOrder(byteOrder);
        else
            out.setByteOrder(ByteOrder.LITTLE_ENDIAN);
    }
    
    public ByteBuffer writePixelDataToByteBuffer(boolean writeOverlayData)
    {
        ByteArrayOutputStream buff = new ByteArrayOutputStream((size * ba) >>> 3)
            {
                //avoid ByteArrayOutputStream making a copy
                public synchronized byte[] toByteArray()
                {
                    return buf;
                }
            };
        try {
            ImageOutputStream out = ImageIO.createImageOutputStream(new BufferedOutputStream(buff));
            writePixelData(out, writeOverlayData);
            out.flush();
            out.close();
            return ByteBuffer.wrap(buff.toByteArray(), 0, buff.size());
        }
        catch (IOException ioe) {
            return null;
        }
    }

    public void writePixelData(boolean writeOverlayData)
        throws IOException
    {
        writePixelData(out, true);
    }

    public void writePixelData(ImageOutputStream out, boolean writeOverlayData)
        throws IOException
    {
        if (data == null)
            throw new IllegalStateException("No pixel data has been read");

        final int shift = (writeOverlayData || !containsOverlayData) ? 0 : sampleOffset;
        final int cellBits = (writeOverlayData && containsOverlayData) ? ba : bs;
        int w = 0, bNeeded = 16, bUsed = 0, read;
        int f, p, s;

        if (byPlane) {
            for (f = 0; f < nf; f++) {
                for (s = 0; s < spp; s++) {
                    for (p = 0; p < frameSize; ) {
                        //pre-conditions: bUsed is setup, bNeeded is [1..16]
                        w |= (data[f][s][p] >>> (bUsed + shift)) << (16 - bNeeded);
                        read = cellBits - bUsed;
                        if (read < bNeeded) {
                            bUsed = 0;
                            bNeeded -= read;
                            p++;
                        }
                        else {
                            bUsed += bNeeded;
                            if (bUsed == cellBits) {
                                p++;
                                bUsed = 0;
                            }
                            bNeeded = 16;
                            out.writeShort(w);
                            w = 0;
                        }
                    }
                }
            }
        }
        else {
            for (f = 0; f < nf; f++) {
                for (p = 0; p < frameSize; p++) {
                    for (s = 0; s < spp; ) {
                        //pre-conditions: bUsed is setup, bNeeded is [1..16]
                        w |= (data[f][s][p] >>> (bUsed + shift)) << (16 - bNeeded);
                        read = cellBits - bUsed;
                        if (read < bNeeded) {
                            bUsed = 0;
                            bNeeded -= read;
                            s++;
                        }
                        else {
                            bUsed += bNeeded;
                            if (bUsed == cellBits) {
                                s++;
                                bUsed = 0;
                            }
                            bNeeded = 16;
                            out.writeShort(w);
                            w = 0;
                        }
                    }
                }
            }
        }
    }

    public PixelDataDescription getPixelDataDescription()
    {
        return pdDesc;
    }
}
