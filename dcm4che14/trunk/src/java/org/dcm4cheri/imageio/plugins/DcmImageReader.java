/*$Id$*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>     *
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

import org.dcm4che.image.ColorModelFactory;
import org.dcm4che.imageio.plugins.DcmImageReadParam;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.data.FileFormat;
import org.dcm4che.dict.Tags;
import org.dcm4cheri.image.ImageReaderFactory;
import org.dcm4cheri.image.ItemParser;

import com.sun.media.imageio.stream.SegmentedImageInputStream;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferUShort;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.WritableRaster;
import java.awt.Rectangle;
import java.awt.Point;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import javax.imageio.IIOException;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageReadParam;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public class DcmImageReader extends ImageReader {

    static final DcmParserFactory pfact = DcmParserFactory.getInstance();

    private static final ColorModelFactory cmFactory = ColorModelFactory
            .getInstance();

    private ImageInputStream stream = null;
    private SegmentedImageInputStream itemStream = null;
    private ImageReader decompressor = null;
    private ItemParser itemParser = null;

    // The image to be written.
    private BufferedImage theImage = null;

    // The image's tile.
    private WritableRaster theTile = null;
    private DcmParser theParser = null;
    private DcmMetadataImpl theMetadata = null;
    private Dataset theDataset = null;
    private long[] frameStartPos = null;
    private int width = -1;
    private int height = -1;
    private int planes = 0;
    private String pmi = null;

    private int dataType = 0;
    private int stored = 0;
    private float aspectRatio = 0;
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

    public DcmImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    // Take input from an ImageInputStream
    public void setInput(Object input, boolean seekForwardOnly,
            boolean ignoreMetadata) {
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        if (input != null) {
            if (!(input instanceof ImageInputStream)) {
                throw new IllegalArgumentException(
                        "input not an ImageInputStream!");
            }
            this.stream = (ImageInputStream) input;
        } else {
            this.stream = null;
        }

        // Clear all values based on the previous stream contents
        resetStreamSettings();
    }

    public int getNumImages(boolean allowSearch) throws IOException {
        readMetadata();
        return frameStartPos.length;
    }

    private void checkIndex(int imageIndex) {
        if (imageIndex >= frameStartPos.length) {
            throw new IndexOutOfBoundsException("index: " + imageIndex
                    + ", frames: " + frameStartPos.length);
        }
    }

    public int getWidth(int imageIndex) throws IOException {
        readMetadata();
        checkIndex(imageIndex);
        return width;
    }

    public int getHeight(int imageIndex) throws IOException {
        readMetadata();
        checkIndex(imageIndex);
        return height;
    }

    public float getAspectRatio(int imageIndex) throws IOException {
        readMetadata();
        checkIndex(imageIndex);
        return aspectRatio;
    }

    public IIOMetadata getStreamMetadata() throws IOException {
        readMetadata();
        return theMetadata;
    }

    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        readMetadata();
        checkIndex(imageIndex);
        return null;
    }

    private void readMetadata() throws IOException {
        if (theMetadata != null) {
            return;
        }
        if (stream == null) {
            throw new IllegalStateException("Input not set!");
        }
        theParser = pfact.newDcmParser(stream);
        FileFormat fileFormat = theParser.detectFileFormat();
        if (fileFormat == null) {
            throw new IOException("Unrecognized file format!");
        }
        this.theDataset = DcmObjectFactory.getInstance().newDataset();
        theParser.setDcmHandler(theDataset.getDcmHandler());
        theParser.parseDcmFile(fileFormat, Tags.PixelData);
        this.theMetadata = new DcmMetadataImpl(theDataset);
        if (theParser.getReadTag() != Tags.PixelData) {
            this.frameStartPos = new long[0];
        } else {
            initParams();
        }
    }

    private void initParams() throws IOException {
        int alloc;
        switch (alloc = theDataset.getInt(Tags.BitsAllocated, 8)) {
        case 8:
            this.dataType = DataBuffer.TYPE_BYTE;
            break;
        case 16:
            this.dataType = DataBuffer.TYPE_USHORT;
            break;
        default:
            throw new IOException("" + alloc + " Bits Allocated not supported!");
        }
        this.stored = theDataset.getInt(Tags.BitsStored, alloc);
        this.width = theDataset.getInt(Tags.Columns, 0);
        this.height = theDataset.getInt(Tags.Rows, 0);
        this.pmi = theDataset.getString(Tags.PhotometricInterpretation, null);
        this.planes = theDataset.getInt(Tags.PlanarConfiguration, 0);
        this.aspectRatio = width * pixelRatio() / height;

        this.frameStartPos = new long[theDataset.getInt(Tags.NumberOfFrames, 1)];

        int rLen = theParser.getReadLength();
        if (rLen == -1) {
            ImageReaderFactory f = ImageReaderFactory.getInstance();
            String ts = theDataset.getFileMetaInfo().getTransferSyntaxUID();
            this.decompressor = f.getReaderForTransferSyntax(ts);
            this.itemParser = new ItemParser(theParser);
            this.itemStream = new SegmentedImageInputStream(stream, itemParser);
            return;
        }
        frameStartPos[0] = theParser.getStreamPosition();
        int frameLength = rLen / frameStartPos.length;
        for (int i = 1; i < frameStartPos.length; ++i) {
            frameStartPos[i] = frameStartPos[i - 1] + frameLength;
        }

        if ("RGB".equals(this.pmi)) {
            if (alloc == 16) {
                throw new IOException("RGB 16 Bits allocated not supported!");
            }
            if (frameLength < 3 * width * height) {
                throw new DcmValueException("Invalid Length of Pixel Data: "
                        + rLen);
            }
            return;
        }

        if (frameLength < width * height * (alloc >> 3)) {
            throw new DcmValueException("Invalid Length of Pixel Data: " + rLen);
        }
    }

    private float pixelRatio() {
        int[] ratio = theDataset.getInts(Tags.PixelAspectRatio);
        if (ratio != null && ratio.length == 2) {
            if (ratio[0] == ratio[1] || ratio[0] <= 0 || ratio[1] <= 0) {
                return 1.f; // accept 0/0
            }
            return ratio[1] / (float) ratio[0];
        }
        float[] spacing = theDataset.getFloats(Tags.PixelSpacing);
        if (spacing == null || spacing.length != 2) {
            spacing = theDataset.getFloats(Tags.ImagerPixelSpacing);
            if (spacing == null || spacing.length != 2) {
                return 1.f;
            }
        }
        if (spacing[0] == spacing[1] || spacing[0] <= 0 || spacing[1] <= 0) {
            return 1.f;
        }
        return spacing[1] / spacing[0];
    }

    private static final ColorSpace sRGB = ColorSpace
            .getInstance(ColorSpace.CS_sRGB);

    private static final ImageTypeSpecifier RGB_PLANE = ImageTypeSpecifier
            .createBanded(sRGB, new int[] { 0, 1, 2 }, new int[] { 0, 0, 0 },
                    DataBuffer.TYPE_BYTE, false, false);

    private static final ImageTypeSpecifier RGB_PIXEL = ImageTypeSpecifier
            .createInterleaved(sRGB, new int[] { 0, 1, 2 },
                    DataBuffer.TYPE_BYTE, false, false);

    public Iterator getImageTypes(int imageIndex) throws IOException {
        return getImageTypes(imageIndex, null);
    }

    private Iterator getImageTypes(int imageIndex, DcmImageReadParam param)
            throws IOException {
        readMetadata();
        checkIndex(imageIndex);
        ArrayList l = new ArrayList(1);
        if ("RGB".equals(this.pmi)) {
            l.add(this.planes != 0 ? RGB_PLANE : RGB_PIXEL);
        } else {
            l.add(new ImageTypeSpecifier(cmFactory.getColorModel(cmFactory
                    .makeParam(theDataset, param != null ? param.getPValToDDL()
                            : null)), new PixelInterleavedSampleModel(
                    this.dataType, 1, 1, 1, 1, new int[] { 0 })));
        }
        return l.iterator();
    }

    public ImageReadParam getDefaultReadParam() {
        return new DcmImageReadParamImpl();
    }

    public BufferedImage read(int imageIndex, ImageReadParam param)
            throws IOException {
        readMetadata();
        checkIndex(imageIndex);
        DcmImageReadParam readParam = (DcmImageReadParam) param;
        if (readParam == null) {
            readParam = (DcmImageReadParam) getDefaultReadParam();
        }
        if (decompressor != null) {
            if (frameStartPos[imageIndex] == 0) {
                for (int i = 0; i < imageIndex; ++i)
                    if (frameStartPos[i+1] == 0)
                        decompress(i, readParam);
            }
            BufferedImage bi = decompress(imageIndex, readParam);
            if (readParam.isMaskPixelData())
                maskPixelData(bi.getTile(0,0).getDataBuffer());

            return bi;
        }
        stream.seek(this.frameStartPos[imageIndex]);

        Iterator imageTypes = getImageTypes(imageIndex, readParam);
        this.theImage = getDestination(param, imageTypes, this.width,
                this.height);
        this.theTile = theImage.getWritableTile(0, 0);

        Rectangle rect = getSourceRegion(param, width, height);
        this.sourceXOffset = rect.x;
        this.sourceYOffset = rect.y;
        this.sourceWidth = rect.width;
        this.sourceHeight = rect.height;
        this.sourceXSubsampling = readParam.getSourceXSubsampling();
        this.sourceYSubsampling = readParam.getSourceYSubsampling();
        this.subsamplingXOffset = readParam.getSubsamplingXOffset();
        this.subsamplingYOffset = readParam.getSubsamplingYOffset();
        Point point = readParam.getDestinationOffset();
        this.destXOffset = point.x;
        this.destYOffset = point.y;
        this.destWidth = sourceWidth / sourceXSubsampling;
        this.totDestWidth = theTile.getWidth();
        this.totDestHeight = theTile.getHeight();
        if (destXOffset < 0) {
            sourceXOffset -= destXOffset * sourceXSubsampling;
            if ((sourceWidth += destXOffset * sourceXSubsampling) < 0) {
                sourceWidth = 0;
            }
            destXOffset = 0;
        }
        if (destYOffset < 0) {
            sourceYOffset -= destYOffset * sourceYSubsampling;
            if ((sourceHeight += destYOffset * sourceYSubsampling) < 0) {
                sourceHeight = 0;
            }
            destYOffset = 0;
        }

        DataBuffer db = this.theTile.getDataBuffer();
        if (this.dataType == DataBuffer.TYPE_BYTE) {
            if ("RGB".equals(this.pmi)) {
                if (this.planes != 0) {
                    readByteSamples(1, ((DataBufferByte) db).getData(0));
                    readByteSamples(1, ((DataBufferByte) db).getData(1));
                    readByteSamples(1, ((DataBufferByte) db).getData(2));
                } else {
                    readByteSamples(3, ((DataBufferByte) db).getData());
                }
            } else {
                readByteSamples(1, ((DataBufferByte) db).getData());
            }
        } else {
            readWordSamples(1, ((DataBufferUShort) db).getData());
        }

        if (readParam.isMaskPixelData())
            maskPixelData(db);

        return this.theImage;
    }

    private BufferedImage decompress(int imageIndex, DcmImageReadParam readParam)
            throws IOException {
        itemStream.seek(this.frameStartPos[imageIndex]);
        decompressor.setInput(itemStream);
        BufferedImage bi = decompressor.read(0, readParam);
        decompressor.reset();
        if (imageIndex + 1 < frameStartPos.length) {
            this.frameStartPos[imageIndex + 1] = itemParser
                    .seekNextFrame(itemStream);
        }
        return bi;
    }

    private void maskPixelData(DataBuffer db) {
        if (db instanceof DataBufferUShort) {
            short[] data = ((DataBufferUShort) db).getData();
            final int mask = -1 >>> (32 - stored);
            for (int i = 0; i < data.length; i++)
                data[i] &= mask;
        }
    }

    private void readByteSamples(int samples, byte[] dest) throws IOException {
        byte[] srcRow = null;
        final int rowLen = width * samples;
        final int srcRowLen = sourceWidth * samples;
        final int srcXOffsetLen = sourceXOffset * samples;
        final int destXOffsetLen = destXOffset * samples;
        if (sourceXSubsampling != 1) {
            srcRow = new byte[srcRowLen];
        }
        final int maxPosMax = totDestHeight * totDestWidth;
        final int totDestRowLen = totDestWidth * samples;
        stream.skipBytes(rowLen * sourceYOffset);
        int destY = destYOffset;
        int pos = 0, posMax = 0;
        int x = 0, y = 0;
        int x3, pos3;
        try {
            for (y = 0; y < sourceHeight; ++y) {
                if ((y - subsamplingYOffset) % sourceYSubsampling != 0) {
                    stream.skipBytes(rowLen);
                    continue;
                }
                stream.skipBytes(srcXOffsetLen);
                if (sourceXSubsampling == 1) {
                    stream.readFully(dest, destY * totDestRowLen
                            + destXOffsetLen, srcRowLen);
                } else {
                    stream.readFully(srcRow);
                    pos = destY * totDestWidth + destXOffset;
                    posMax = Math.min(pos + destWidth, maxPosMax);
                    switch (samples) {
                    case 1:
                        for (x = 0; pos < posMax; ++x) {
                            if ((x - subsamplingXOffset) % sourceXSubsampling == 0) {
                                dest[pos++] = srcRow[x];
                            }
                        }
                        break;
                    case 3:
                        for (x = 0, x3 = 0, pos3 = pos * 3; pos < posMax; ++x) {
                            if ((x - subsamplingXOffset) % sourceXSubsampling == 0) {
                                dest[pos3++] = srcRow[x3++];
                                dest[pos3++] = srcRow[x3++];
                                dest[pos3++] = srcRow[x3++];
                                ++pos;
                            } else {
                                x3 += 3;
                            }
                        }
                        break;
                    default:
                        throw new Error("Internal dcm4che Error");
                    }
                }
                stream.skipBytes(rowLen - srcXOffsetLen - srcRowLen);
                if (++destY >= totDestHeight) {
                    ++y;
                    break;
                }
            }
            stream.skipBytes(rowLen * (height - sourceYOffset - y));
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IIOException("Exception in readByteSamples", ex);
        }
    }

    private void readWordSamples(int samples, short[] dest) throws IOException {
        final int rowLen = width * samples;
        final int srcRowLen = sourceWidth * samples;
        final int srcXOffsetLen = sourceXOffset * samples;
        final int destXOffsetLen = destXOffset * samples;
        final byte[] srcRow = new byte[srcRowLen << 1];
        final ShortBuffer srcRowBuf = ByteBuffer.wrap(srcRow).order(
                theParser.getDcmDecodeParam().byteOrder).asShortBuffer();
        final int maxPosMax = totDestHeight * totDestWidth;
        final int totDestRowLen = totDestWidth * samples;
        stream.skipBytes((rowLen * sourceYOffset) << 1);
        int destY = destYOffset;
        int pos = 0, posMax = 0;
        int x = 0, y = 0;
        int x3, pos3;
        try {
            for (y = 0; y < sourceHeight; ++y) {
                if ((y - subsamplingYOffset) % sourceYSubsampling != 0) {
                    stream.skipBytes(rowLen << 1);
                    continue;
                }
                stream.skipBytes(srcXOffsetLen << 1);
                stream.readFully(srcRow);
                if (sourceXSubsampling == 1) {
                    srcRowBuf.rewind();
                    srcRowBuf.get(dest, destY * totDestRowLen + destXOffsetLen,
                            srcRowLen);
                } else {
                    pos = destY * totDestWidth + destXOffset;
                    posMax = Math.min(pos + destWidth, maxPosMax);
                    switch (samples) {
                    case 1:
                        for (x = 0; pos < posMax; ++x) {
                            if ((x - subsamplingXOffset) % sourceXSubsampling == 0) {
                                dest[pos++] = srcRowBuf.get(x);
                            }
                        }
                        break;
                    case 3:
                        for (x = 0, x3 = 0, pos3 = pos * 3; pos < posMax; ++x) {
                            if ((x - subsamplingXOffset) % sourceXSubsampling == 0) {
                                dest[pos3++] = srcRowBuf.get(x3++);
                                dest[pos3++] = srcRowBuf.get(x3++);
                                dest[pos3++] = srcRowBuf.get(x3++);
                                ++pos;
                            } else {
                                x3 += 3;
                            }
                        }
                        break;
                    default:
                        throw new Error("Internal dcm4che Error");
                    }
                }
                stream.skipBytes((rowLen - srcXOffsetLen - srcRowLen) << 1);
                if (++destY >= totDestHeight) {
                    ++y;
                    break;
                }
            }
            stream.skipBytes((rowLen * (height - sourceYOffset - y)) << 1);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IIOException("Exception in readWordSamples", ex);
        }
    }

    /**
     * Remove all settings including global settings such as
     * <code>Locale</code>s and listeners, as well as stream settings.
     */
    public void reset() {
        super.reset();
        this.stream = null;
        resetStreamSettings();
    }

    /**
     * Remove local settings based on parsing of a stream.
     */
    private void resetStreamSettings() {
        theParser = null;
        theMetadata = null;
        theDataset = null;
        frameStartPos = null;
        pmi = null;

        theImage = null;
        theTile = null;
        if (decompressor != null)
            decompressor.dispose();
        decompressor = null;
        itemStream = null;
        itemParser = null;
    }
}
