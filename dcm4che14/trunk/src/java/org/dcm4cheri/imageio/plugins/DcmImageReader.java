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

package org.dcm4cheri.imageio.plugins;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferUShort;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.Tags;
import org.dcm4che.image.ColorModelFactory;
import org.dcm4che.image.PixelDataReader;
import org.dcm4che.image.PixelDataFactory;
import org.dcm4che.imageio.plugins.DcmImageReadParam;

/**
 * @author <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @author <a href="mailto:joseph@tiani.com">joseph foraci</a>
 * @version $Revision$ $Date$
 * @see "DICOM Part 5: Data Structures and Encoding, Section 8. 'Encoding of Pixel,
 *      Overlay and Waveform Data', Annex D"
 */
public class DcmImageReader extends ImageReader
{
    static final DcmParserFactory pfact = DcmParserFactory.getInstance();
    
    private static final ColorModelFactory cmFactory = ColorModelFactory.getInstance();
    
    private static final ColorSpace sRGB;
    private static final ImageTypeSpecifier RGB_PLANE;
    private static final ImageTypeSpecifier RGB_PIXEL;
    
    static
    {
        sRGB = ColorSpace.getInstance(1000);
        RGB_PLANE = ImageTypeSpecifier.createBanded(sRGB, new int[] {
            0, 1, 2
        }, new int[3], 0, false, false);
        RGB_PIXEL = ImageTypeSpecifier.createInterleaved(sRGB, new int[] {
            0, 1, 2
        }, 0, false, false);
    }
    
    private final PixelDataFactory pixelDataFact;
    private ImageInputStream stream;
    private BufferedImage theImage;
    private WritableRaster theTile;
    private DcmParser theParser;
    private DcmMetadataImpl theMetadata;
    private Dataset theDataset;
    private PixelDataReader pdReader;
    private int width;
    private int height;
    private int planes;
    private int samplesPerPixel;
    private String pmi;
    private int dataType;
    private float aspectRatio;
    private int sourceXOffset;
    private int sourceYOffset;
    private int sourceWidth;
    private int sourceHeight;
    private int sourceXSubsampling;
    private int sourceYSubsampling;
    private int subsamplingXOffset;
    private int subsamplingYOffset;
    private int destXOffset;
    private int destYOffset;
    private int destWidth;
    private int totDestWidth;
    private int totDestHeight;
    private int numberOfFrames;

    public DcmImageReader(ImageReaderSpi originatingProvider)
    {
        super(originatingProvider);
        pixelDataFact = PixelDataFactory.getInstance();
        stream = null;
        theImage = null;
        theTile = null;
        theParser = null;
        theMetadata = null;
        theDataset = null;
        width = -1;
        height = -1;
        planes = -1;
        samplesPerPixel = -1;
        pmi = null;
        dataType = 0;
        aspectRatio = 0.0F;
    }

    public void reset()
    {
        super.reset();
        stream = null;
        resetStreamSettings();
    }

    private void resetStreamSettings()
    {
        theParser = null;
        theMetadata = null;
        theDataset = null;
        pmi = null;
        theImage = null;
        theTile = null;
        pdReader = null;
    }

    public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata)
    {
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        if(input != null)
        {
            if(!(input instanceof ImageInputStream))
                throw new IllegalArgumentException("input not an ImageInputStream!");
            stream = (ImageInputStream)input;
        } else
        {
            stream = null;
        }
        resetStreamSettings();
    }

    public int getNumImages(boolean allowSearch)
        throws IOException
    {
        readMetadata();
        return numberOfFrames;
    }

    private void checkIndex(int imageIndex)
    {
        if(imageIndex >= numberOfFrames)
            throw new IndexOutOfBoundsException("index: " + imageIndex + ", frames: " + numberOfFrames);
        else
            return;
    }

    public int getWidth(int imageIndex)
        throws IOException
    {
        readMetadata();
        checkIndex(imageIndex);
        return width;
    }

    public int getHeight(int imageIndex)
        throws IOException
    {
        readMetadata();
        checkIndex(imageIndex);
        return height;
    }

    public float getAspectRatio(int imageIndex)
        throws IOException
    {
        readMetadata();
        checkIndex(imageIndex);
        return aspectRatio;
    }

    public IIOMetadata getStreamMetadata()
        throws IOException
    {
        readMetadata();
        return theMetadata;
    }

    public IIOMetadata getImageMetadata(int imageIndex)
        throws IOException
    {
        readMetadata();
        checkIndex(imageIndex);
        return null;
    }

    private void readMetadata()
        throws IOException
    {
        if(theMetadata != null)
            return;
        if(stream == null)
            throw new IllegalStateException("Input not set!");
        theParser = pfact.newDcmParser(stream);
        org.dcm4che.data.FileFormat fileFormat = theParser.detectFileFormat();
        if(fileFormat == null)
            throw new IOException("Unrecognized file format!");
        theDataset = DcmObjectFactory.getInstance().newDataset();
        theParser.setDcmHandler(theDataset.getDcmHandler());
        theParser.parseDcmFile(fileFormat, Tags.PixelData);
        theMetadata = new DcmMetadataImpl(theDataset);
        pdReader = pixelDataFact.newReader(theDataset, stream,
            theParser.getDcmDecodeParam().byteOrder, theParser.getReadVR());
        if(theParser.getReadTag() == Tags.PixelData)
            initParams();
    }

    private void initParams()
        throws IOException
    {
        int alloc = theDataset.getInt(Tags.BitsAllocated, 8);
        if(alloc <= 8)
            dataType = DataBuffer.TYPE_BYTE;
        else if(alloc <= 16)
            dataType = DataBuffer.TYPE_USHORT;
        else if(alloc <= 32) //dataType = DataBuffer.TYPE_INT
            throw new IOException(alloc + " Bits Allocated not supported for Java BufferedImages");
        else
            throw new IOException("Bits allocated " + alloc + " not supported by dcm4che");
        numberOfFrames = theDataset.getInt(Tags.NumberOfFrames, 1);
        final int rLen = theParser.getReadLength();
        if(rLen == -1)
            throw new IOException("Encapsulate Pixel Data not supported by this version!");
        width = theDataset.getInt(Tags.Columns, 0);
        height = theDataset.getInt(Tags.Rows, 0);
        pmi = theDataset.getString(Tags.PhotometricInterpretation, null);
        planes = theDataset.getInt(Tags.PlanarConfiguration, 0);
        samplesPerPixel = theDataset.getInt(Tags.SamplesPerPixel, 1);
        aspectRatio = ((float)width * pixelRatio()) / (float)height;
        
        long pixelDataLen = pdReader.getPixelDataDescription().calcPixelDataLength();
        
        if(rLen < pixelDataLen)
            throw new DcmValueException("Invalid Length of Pixel Data (too short): " + rLen);
        else if (rLen > pixelDataLen)
            System.err.println("Warning: Pixel Data too long. Trying to read anyway...");
        
        //check for other unsupported cases
        if("RGB".equals(pmi) && alloc > 8 && alloc <= 16)
                throw new IOException("RGB 16 Bits allocated not supported for Java BufferedImages");
    }

    private float pixelRatio()
    {
        int ratio[] = theDataset.getInts(Tags.PixelAspectRatio);
        if(ratio != null && ratio.length == 2)
            if(ratio[0] == ratio[1])
                return 1.0F;
            else
                return (float)ratio[1] / (float)ratio[0];
        float spacing[] = theDataset.getFloats(Tags.PixelSpacing);
        if(spacing == null || spacing.length != 2)
        {
            spacing = theDataset.getFloats(Tags.ImagerPixelSpacing);
            if(spacing == null || spacing.length != 2)
                return 1.0F;
        }
        return spacing[1] / spacing[0];
    }

    public Iterator getImageTypes(int imageIndex)
        throws IOException
    {
        return getImageTypes(imageIndex, null);
    }

    private Iterator getImageTypes(int imageIndex, DcmImageReadParam param)
        throws IOException
    {
        readMetadata();
        checkIndex(imageIndex);
        ArrayList l = new ArrayList(1);
        if("RGB".equals(pmi))
            l.add(planes == 0 ? ((Object) (RGB_PIXEL)) : ((Object) (RGB_PLANE)));
        else
            l.add(new ImageTypeSpecifier(cmFactory.getColorModel(cmFactory.makeParam(theDataset, param == null ? null : param.getPValToDDL())), new PixelInterleavedSampleModel(dataType, 1, 1, 1, 1, new int[1])));
        return l.iterator();
    }

    public ImageReadParam getDefaultReadParam()
    {
        return new DcmImageReadParamImpl();
    }

    public BufferedImage read(int imageIndex, ImageReadParam param)
        throws IOException
    {
        readMetadata();
        checkIndex(imageIndex);
        pdReader.resetStream();
        if(param == null)
            param = getDefaultReadParam();
        Iterator imageTypes = getImageTypes(imageIndex, (DcmImageReadParam)param);
        theImage = ImageReader.getDestination(param, imageTypes, width, height);
        theTile = theImage.getWritableTile(0, 0);
        Rectangle rect = ImageReader.getSourceRegion(param, width, height);
        sourceXOffset = rect.x;
        sourceYOffset = rect.y;
        sourceWidth = rect.width;
        sourceHeight = rect.height;
        sourceXSubsampling = param.getSourceXSubsampling();
        sourceYSubsampling = param.getSourceYSubsampling();
        subsamplingXOffset = param.getSubsamplingXOffset();
        subsamplingYOffset = param.getSubsamplingYOffset();
        Point point = param.getDestinationOffset();
        destXOffset = point.x;
        destYOffset = point.y;
        destWidth = sourceWidth / sourceXSubsampling;
        totDestWidth = theTile.getWidth();
        totDestHeight = theTile.getHeight();
        if(destXOffset < 0)
        {
            sourceXOffset -= destXOffset * sourceXSubsampling;
            if((sourceWidth += destXOffset * sourceXSubsampling) < 0)
                sourceWidth = 0;
            destXOffset = 0;
        }
        if(destYOffset < 0)
        {
            sourceYOffset -= destYOffset * sourceYSubsampling;
            if((sourceHeight += destYOffset * sourceYSubsampling) < 0)
                sourceHeight = 0;
            destYOffset = 0;
        }
        java.awt.image.DataBuffer db = theTile.getDataBuffer();
        //seek to proper frame (imageIndex parameter)
        for (int i = 0; i < imageIndex; i++)
            pdReader.skipToNextFrame();
        //read samples
        if (dataType == DataBuffer.TYPE_BYTE)
        {
            byte tilebuff[][] = ((DataBufferByte)db).getBankData();
            if(planes == 0) {
                readSamples(pdReader, samplesPerPixel, tilebuff[0]);
            }
            else {
                for(int s = 0; s < samplesPerPixel; s++)
                    readSamples(pdReader, 1, tilebuff[s]);
            }
        }
        else if (dataType == DataBuffer.TYPE_USHORT)
        {
            short tilebuff[][] = ((DataBufferUShort)db).getBankData();
            if(planes == 0) {
                readSamples(pdReader, samplesPerPixel, tilebuff[0]);
            }
            else {
                for(int s = 0; s < samplesPerPixel; s++)
                    readSamples(pdReader, 1, tilebuff[s]);
            }
        }
        return theImage;
    }

    private void readSamples(PixelDataReader pd, final int samples, byte dest[])
        throws IOException
    {
        final int maxPosMax = totDestHeight * totDestWidth;
        final int srcXOffsetLen = sourceXOffset * samples;
        final int destXOffsetLen = destXOffset * samples;
        final int rowLen = width * samples;
        final int srcRowLen = sourceWidth * samples;
        final int totDestRowLen = totDestWidth * samples;
        final int[] tmp = new int[srcRowLen];
        final int mask = (1 << theDataset.getInt(Tags.BitsStored, 0)) - 1;
        
        byte srcRow[] = null;
        if(sourceXSubsampling != 1)
            srcRow = new byte[srcRowLen];
        int destY = destYOffset;
        int pos = 0;
        int posMax = 0;
        int x = 0, y = 0;
        
        pd.skipSamples(rowLen * sourceYOffset);
        try
        {
            for(y = 0; y < sourceHeight; y++)
            {
                if((y - subsamplingYOffset) % sourceYSubsampling != 0)
                {
                    pd.skipSamples(rowLen);
                    continue;
                }
                pd.skipSamples(srcXOffsetLen);
                if(sourceXSubsampling == 1)
                {
                    pd.readFully(tmp, 0, srcRowLen);
                    final int off = destY * totDestRowLen + destXOffsetLen;
                    for (int i = 0; i < srcRowLen; i++)
                        dest[i + off] = (byte)(tmp[i] & mask);
                }
                else
                {
                    pd.readFully(tmp, 0, srcRowLen);
                    pos = destY * totDestWidth + destXOffset;
                    posMax = Math.min(pos + destWidth, maxPosMax);
                    x = 0;
                    int x3 = 0;
                    int pos3 = pos * samples;
                    while(pos < posMax)
                    {
                        if((x - subsamplingXOffset) % sourceXSubsampling == 0)
                        {
                            for (int i = 0; i < samples; i++)
                                dest[pos3++] = (byte)(tmp[x3++] & mask);
                            pos++;
                        } else
                        {
                            x3 += samples;
                        }
                        x++;
                    }
                    
                }
                pd.skipSamples(rowLen - srcXOffsetLen - srcRowLen);
                if(++destY < totDestHeight)
                    continue;
                y++;
                break;
            }
            pd.skipSamples(rowLen * (height - sourceYOffset - y));
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            throw new IIOException("Exception in readSamples", ex);
        }
    }

    private void readSamples(PixelDataReader pd, final int samples, short dest[])
        throws IOException
    {
        final int maxPosMax = totDestHeight * totDestWidth;
        final int srcXOffsetLen = sourceXOffset * samples;
        final int destXOffsetLen = destXOffset * samples;
        final int rowLen = width * samples;
        final int srcRowLen = sourceWidth * samples;
        final int totDestRowLen = totDestWidth * samples;
        final int[] tmp = new int[srcRowLen];
        final int mask = (1 << theDataset.getInt(Tags.BitsStored, 0)) - 1;
        
        byte srcRow[] = null;
        if(sourceXSubsampling != 1)
            srcRow = new byte[srcRowLen];
        int destY = destYOffset;
        int pos = 0;
        int posMax = 0;
        int x = 0, y = 0;
        
        pd.skipSamples(rowLen * sourceYOffset);
        try
        {
            for(y = 0; y < sourceHeight; y++)
            {
                if((y - subsamplingYOffset) % sourceYSubsampling != 0)
                {
                    pd.skipSamples(rowLen);
                    continue;
                }
                pd.skipSamples(srcXOffsetLen);
                if(sourceXSubsampling == 1)
                {
                    pd.readFully(tmp, 0, srcRowLen);
                    final int off = destY * totDestRowLen + destXOffsetLen;
                    for (int i = 0; i < srcRowLen; i++)
                        dest[i + off] = (short)(tmp[i] & mask);
                }
                else
                {
                    pd.readFully(tmp, 0, srcRowLen);
                    pos = destY * totDestWidth + destXOffset;
                    posMax = Math.min(pos + destWidth, maxPosMax);
                    x = 0;
                    int x3 = 0;
                    int pos3 = pos * samples;
                    while(pos < posMax)
                    {
                        if((x - subsamplingXOffset) % sourceXSubsampling == 0)
                        {
                            for (int i = 0; i < samples; i++)
                                dest[pos3++] = (short)(tmp[x3++] & mask);
                            pos++;
                        } else
                        {
                            x3 += samples;
                        }
                        x++;
                    }
                    
                }
                pd.skipSamples(rowLen - srcXOffsetLen - srcRowLen);
                if(++destY < totDestHeight)
                    continue;
                y++;
                break;
            }
            pd.skipSamples(rowLen * (height - sourceYOffset - y));
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            throw new IIOException("Exception in readSamples", ex);
        }
    }

    // !!! DEPRECATED !!!
    private void readByteSamples(int samples, byte dest[])
        throws IOException
    {
        byte srcRow[] = (byte[])null;
        int rowLen = width * samples;
        int srcRowLen = sourceWidth * samples;
        int srcXOffsetLen = sourceXOffset * samples;
        int destXOffsetLen = destXOffset * samples;
        if(sourceXSubsampling != 1)
            srcRow = new byte[srcRowLen];
        int maxPosMax = totDestHeight * totDestWidth;
        int totDestRowLen = totDestWidth * samples;
        stream.skipBytes(rowLen * sourceYOffset);
        int destY = destYOffset;
        int pos = 0;
        int posMax = 0;
        int x = 0;
        int y = 0;
        try
        {
            for(y = 0; y < sourceHeight; y++)
            {
                if((y - subsamplingYOffset) % sourceYSubsampling != 0)
                {
                    stream.skipBytes(rowLen);
                    continue;
                }
                stream.skipBytes(srcXOffsetLen);
                if(sourceXSubsampling == 1)
                {
                    stream.readFully(dest, destY * totDestRowLen + destXOffsetLen, srcRowLen);
                } else
                {
                    stream.readFully(srcRow);
                    pos = destY * totDestWidth + destXOffset;
                    posMax = Math.min(pos + destWidth, maxPosMax);
                    switch(samples)
                    {
                    case 1: // '\001'
                        for(x = 0; pos < posMax; x++)
                            if((x - subsamplingXOffset) % sourceXSubsampling == 0)
                                dest[pos++] = srcRow[x];

                        break;

                    case 3: // '\003'
                        x = 0;
                        int x3 = 0;
                        int pos3 = pos * 3;
                        while(pos < posMax) 
                        {
                            if((x - subsamplingXOffset) % sourceXSubsampling == 0)
                            {
                                dest[pos3++] = srcRow[x3++];
                                dest[pos3++] = srcRow[x3++];
                                dest[pos3++] = srcRow[x3++];
                                pos++;
                            } else
                            {
                                x3 += 3;
                            }
                            x++;
                        }
                        break;

                    case 2: // '\002'
                    default:
                        throw new Error("Internal dcm4che Error");
                    }
                }
                stream.skipBytes(rowLen - srcXOffsetLen - srcRowLen);
                if(++destY < totDestHeight)
                    continue;
                y++;
                break;
            }

            stream.skipBytes(rowLen * (height - sourceYOffset - y));
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            throw new IIOException("Exception in readByteSamples", ex);
        }
    }

    // !!! DEPRECATED !!!
    private void readWordSamples(int samples, short dest[])
        throws IOException
    {
        int rowLen = width * samples;
        int srcRowLen = sourceWidth * samples;
        int srcXOffsetLen = sourceXOffset * samples;
        int destXOffsetLen = destXOffset * samples;
        byte srcRow[] = new byte[srcRowLen << 1];
        ShortBuffer srcRowBuf = ByteBuffer.wrap(srcRow).order(theParser.getDcmDecodeParam().byteOrder).asShortBuffer();
        int maxPosMax = totDestHeight * totDestWidth;
        int totDestRowLen = totDestWidth * samples;
        stream.skipBytes(rowLen * sourceYOffset << 1);
        int destY = destYOffset;
        int pos = 0;
        int posMax = 0;
        int x = 0;
        int y = 0;
        try
        {
            for(y = 0; y < sourceHeight; y++)
            {
                if((y - subsamplingYOffset) % sourceYSubsampling != 0)
                {
                    stream.skipBytes(rowLen << 1);
                    continue;
                }
                stream.skipBytes(srcXOffsetLen << 1);
                stream.readFully(srcRow);
                if(sourceXSubsampling == 1)
                {
                    srcRowBuf.rewind();
                    srcRowBuf.get(dest, destY * totDestRowLen + destXOffsetLen, srcRowLen);
                } else
                {
                    pos = destY * totDestWidth + destXOffset;
                    posMax = Math.min(pos + destWidth, maxPosMax);
                    switch(samples)
                    {
                    case 1: // '\001'
                        for(x = 0; pos < posMax; x++)
                            if((x - subsamplingXOffset) % sourceXSubsampling == 0)
                                dest[pos++] = srcRowBuf.get(x);

                        break;

                    case 3: // '\003'
                        x = 0;
                        int x3 = 0;
                        int pos3 = pos * 3;
                        while(pos < posMax) 
                        {
                            if((x - subsamplingXOffset) % sourceXSubsampling == 0)
                            {
                                dest[pos3++] = srcRowBuf.get(x3++);
                                dest[pos3++] = srcRowBuf.get(x3++);
                                dest[pos3++] = srcRowBuf.get(x3++);
                                pos++;
                            } else
                            {
                                x3 += 3;
                            }
                            x++;
                        }
                        break;

                    case 2: // '\002'
                    default:
                        throw new Error("Internal dcm4che Error");
                    }
                }
                stream.skipBytes(rowLen - srcXOffsetLen - srcRowLen << 1);
                if(++destY < totDestHeight)
                    continue;
                y++;
                break;
            }

            stream.skipBytes(rowLen * (height - sourceYOffset - y) << 1);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            throw new IIOException("Exception in readWordSamples", ex);
        }
    }
}
