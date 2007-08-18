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
 * See listed authors below.
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
 
package org.dcm4che2.imageioimpl.plugins.dcm;

import java.awt.Dimension;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.image.ColorModelFactory;
import org.dcm4che2.image.LookupTable;
import org.dcm4che2.image.VOIUtils;
import org.dcm4che2.imageio.ImageReaderFactory;
import org.dcm4che2.imageio.ItemParser;
import org.dcm4che2.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che2.imageio.plugins.dcm.DicomStreamMetaData;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.StopTagInputHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.media.imageio.stream.RawImageInputStream;
import com.sun.media.imageio.stream.SegmentedImageInputStream;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Sep 2, 2006
 */
public class DicomImageReader extends ImageReader {

    private static final Logger log = 
            LoggerFactory.getLogger(DicomImageReader.class);
    
    private static final String J2KIMAGE_READER = 
        "com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageReader";
    
    private static final int[] OFFSETS_0 = { 0 };
    
    private static final int[] OFFSETS_0_0_0 = { 0, 0, 0 };

    private static final int[] OFFSETS_0_1_2 = { 0, 1, 2 };

    private ImageInputStream iis;
    private DicomInputStream dis;
    private DicomObject ds;
    private int width;
    private int height;
    private int frames;
    private int allocated;
    private int stored;
    private int dataType;
    private int samples;
    private boolean monochrome;
    private boolean banded;
    private boolean signed;
    private long pixelDataPos;
    private int pixelDataLen;
    private boolean compressed;
    private long[] frameOffsets;
    private DicomStreamMetaData streamMetaData;
    private ImageReader reader;
    private ItemParser itemParser;
    private SegmentedImageInputStream itemStream;

    protected DicomImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }
    
    public void setInput(Object input, boolean seekForwardOnly,
            boolean ignoreMetadata) {
        super.setInput(input, seekForwardOnly, ignoreMetadata);

        resetLocal();

        if (input != null) {
            if (!(input instanceof ImageInputStream)) {
                throw new IllegalArgumentException(
                        "input not an ImageInputStream!"); 
            }
            this.iis = (ImageInputStream) input;
        }
    }
    
    public void dispose() {
        super.dispose();
        resetLocal();               
    }

    public void reset() {
        super.reset();
        resetLocal();
    }
    
    private void resetLocal() {
        iis = null;
        dis = null;
        ds = null;
        streamMetaData = null;
        width = 0;
        height = 0;
        frames = 0;
        allocated = 0;
        stored = 0;
        dataType = 0;
        samples = 0;
        banded = false;
        signed = false;
        pixelDataPos = 0L;
        pixelDataLen = 0;
        compressed = false;
        frameOffsets = null;
        if (reader != null) {
            reader.dispose();
            reader = null;
        }
        itemParser = null;
        itemStream = null;
    }
    
    public ImageReadParam getDefaultReadParam() {
        return new DicomImageReadParam();
    }    

    public IIOMetadata getStreamMetadata() throws IOException {
        return streamMetaData;
    }
    
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        return null;
    }
    
    public int getNumImages(boolean allowSearch) throws IOException {
        readMetadata();
        return frames;
    }

    private void readMetadata() throws IOException {
        if (iis == null) {
            throw new IllegalStateException("Input not set!");
        }
        if (ds != null) {
            return;
        }
        dis = new DicomInputStream(iis);
        dis.setHandler(new StopTagInputHandler(Tag.PixelData));
        ds = dis.readDicomObject();
        streamMetaData = new DicomStreamMetaData();
        streamMetaData.setDicomObject(ds);
        if (dis.tag() == Tag.PixelData) {
            pixelDataPos = dis.getStreamPosition();
            pixelDataLen = dis.valueLength();
            compressed = pixelDataLen == -1;
            if (compressed) {
                ImageReaderFactory f = ImageReaderFactory.getInstance();
                String ts = ds.getString(Tag.TransferSyntaxUID);                
                f.adjustDatasetForTransferSyntax(ds, ts);               
            }
            width = ds.getInt(Tag.Columns);
            height = ds.getInt(Tag.Rows);
            frames = ds.getInt(Tag.NumberOfFrames, 1);
            frameOffsets = new long[frames]; 
            allocated = ds.getInt(Tag.BitsAllocated, 8);
            stored = ds.getInt(Tag.BitsStored, allocated);
            signed = ds.getInt(Tag.PixelRepresentation) != 0;
            banded = ds.getInt(Tag.PlanarConfiguration) != 0;
            dataType = allocated <= 8 ? DataBuffer.TYPE_BYTE
                    : DataBuffer.TYPE_USHORT;
            samples = ds.getInt(Tag.SamplesPerPixel, 1);
            monochrome = ColorModelFactory.isMonochrome(ds);
        }
    }

    private void initImageReader() throws IOException {
        if (reader != null) {
            return;
        }
        readMetadata();
        if (compressed) {
            initCompressedImageReader();
        } else {
            initRawImageReader();
        }
    }

    private void initCompressedImageReader() throws IOException {
        ImageReaderFactory f = ImageReaderFactory.getInstance();
        String ts = ds.getString(Tag.TransferSyntaxUID);
        this.reader = f.getReaderForTransferSyntax(ts);
        this.itemParser = new ItemParser(dis, iis);
        this.itemStream = new SegmentedImageInputStream(iis, itemParser);
    }

    private void initRawImageReader() throws IIOException {
        int frameLen = width * height * samples * (allocated >> 8);
        frameOffsets[0] = pixelDataPos;
        for (int i = 1; i < frameOffsets.length; i++) {
            frameOffsets[i] = frameOffsets[i-1] + frameLen;
        }
        Dimension[] imageDimensions = new Dimension[frames];
        Arrays.fill(imageDimensions, new Dimension(width, height));
        RawImageInputStream riis = new RawImageInputStream(iis,
                createImageTypeSpecifier(),
                frameOffsets, imageDimensions);
        riis.setByteOrder(ds.bigEndian() ? ByteOrder.BIG_ENDIAN 
                : ByteOrder.LITTLE_ENDIAN);
        reader = (ImageReader) ImageIO.getImageReadersByFormatName("RAW").next();
        reader.setInput(riis);       
    }

    private ImageTypeSpecifier createImageTypeSpecifier() throws IIOException {
        ColorModel cm = ColorModelFactory.createColorModel(ds);
        SampleModel sm = createSampleModel();
        return new ImageTypeSpecifier(cm, sm);
    }

    private SampleModel createSampleModel() {
        if (samples == 1) {
            return new PixelInterleavedSampleModel(dataType, width, height,
                    1, width, OFFSETS_0 );
        } else { // samples == 3
            if (banded) {
                return new BandedSampleModel(dataType, width, height, width,
                        OFFSETS_0_1_2, OFFSETS_0_0_0);                
            } else {
                return new PixelInterleavedSampleModel(dataType, width, height,
                        3, width * 3, OFFSETS_0_1_2);              
            }
        }
    }

    public int getHeight(int imageIndex) throws IOException {
        readMetadata();
        return height;
    }

    public int getWidth(int imageIndex) throws IOException {
        readMetadata();
        return width;
    }

    public Iterator getImageTypes(int imageIndex) throws IOException {
        initImageReader();
        return reader.getImageTypes(0);
    }
    
    public boolean canReadRaster() {
        return true;
    }

    public Raster readRaster(int imageIndex, ImageReadParam param)
            throws IOException {
        initImageReader();
        if (param == null) {
            param = getDefaultReadParam();
        }
        if (compressed) {
            ImageReadParam param1 = reader.getDefaultReadParam();
            copyReadParam(param, param1);
            seekFrame(imageIndex, param1);
            return decompressRaster(imageIndex, param1);
        } else {
            return reader.readRaster(imageIndex, param);
        }
    }
    
    public BufferedImage read(int imageIndex, ImageReadParam param)
            throws IOException {
        initImageReader();
        if (param == null) {
            param = getDefaultReadParam();
        }
        BufferedImage bi;
        if (compressed) {
            ImageReadParam param1 = reader.getDefaultReadParam();
            copyReadParam(param, param1);
            seekFrame(imageIndex, param1);
            bi = decompress(imageIndex, param1);
        } else {
            bi = reader.read(imageIndex, param);
        }
        if (monochrome) {
            WritableRaster raster = bi.getRaster();
            DataBuffer data = raster.getDataBuffer();
            LookupTable lut = createLut((DicomImageReadParam) param, data);
            lut.lookup(data, data);
            if (data.getDataType() == DataBuffer.TYPE_SHORT) {
                ColorModel cm = bi.getColorModel();
                short[] ss = ((DataBufferShort) data).getData();
                return new BufferedImage(cm,
                        Raster.createWritableRaster(raster.getSampleModel(),
                                new DataBufferUShort(ss, ss.length), null),
                        cm.isAlphaPremultiplied(), new Hashtable());
            }
        }
        return bi;
    }

    private void copyReadParam(ImageReadParam src, ImageReadParam dst) {
        dst.setDestination(src.getDestination());
        dst.setSourceRegion(src.getSourceRegion());
        dst.setSourceSubsampling(
                src.getSourceXSubsampling(), 
                src.getSourceYSubsampling(),
                src.getSubsamplingXOffset(),
                src.getSubsamplingYOffset());
        dst.setDestinationOffset(src.getDestinationOffset());
    }

    private void seekFrame(int imageIndex, ImageReadParam param)
            throws IOException {
        if (imageIndex > 0 && frameOffsets[imageIndex] == 0) {
            if (itemParser.getNumberOfDataFragments() == frameOffsets.length) {
                for (int i = 1; i < frameOffsets.length; ++i)
                    frameOffsets[i] = itemParser.getOffsetOfDataFragment(i);                   
            } else {
                for (int i = 0; i < imageIndex; ++i)
                    if (frameOffsets[i+1] == 0) {
                        decompressRaster(i, param);
                    }
            }
        }
    }

    private BufferedImage decompress(int imageIndex, ImageReadParam param)
            throws IOException {
        itemStream.seek(this.frameOffsets[imageIndex]);
        reader.setInput(itemStream);
        log.debug("Start decompressing frame#" + (imageIndex+1));
        BufferedImage bi = reader.read(0, param);
        log.debug("Finished decompressing frame#" + (imageIndex+1));
        postDecompress(imageIndex+1);
        return bi;
    }

    private Raster decompressRaster(int imageIndex, ImageReadParam param)
            throws IOException {
        if (!reader.canReadRaster()) {
            return decompress(imageIndex, param).getData();
        }
        itemStream.seek(this.frameOffsets[imageIndex]);
        reader.setInput(itemStream);
        log.debug("Start decompressing frame#" + (imageIndex+1));
        Raster raster = reader.readRaster(0, param);
        log.debug("Finished decompressing frame#" + (imageIndex+1));
        postDecompress(imageIndex+1);
        return raster;
    }

    private void postDecompress(int nextIndex) throws IOException {
        if (nextIndex < frameOffsets.length && frameOffsets[nextIndex] == 0) {
            this.frameOffsets[nextIndex] = itemParser.seekNextFrame(itemStream);
        }
        // workaround for Bug in J2KImageReader and J2KImageReaderCodecLib.setInput()
        if (reader.getClass().getName().startsWith(J2KIMAGE_READER)) {
            reader.dispose();
            ImageReaderFactory f = ImageReaderFactory.getInstance();
            String ts = ds.getString(Tag.TransferSyntaxUID);
            reader = f.getReaderForTransferSyntax(ts);
        } else {
            reader.reset();
        }
    }

    private LookupTable createLut(DicomImageReadParam param, DataBuffer data) {
        short[] pval2gray = param.getPValue2Gray();
        DicomObject pr = param.getPresentationState();
        if (pr != null) {
            return LookupTable.createLutForImageWithPR(ds, pr, stored,
                    pval2gray);
        }
        DicomObject voiLut = param.getVoiLut();
        if (voiLut != null) {
            return LookupTable.createLutForImage(ds, voiLut, stored, pval2gray);
        }
        float c = param.getWindowCenter();
        float w = param.getWindowWidth();
        if (w > 0) {            
            return LookupTable.createLutForImage(ds, c, w, stored, pval2gray);
        }
        if (param.isAutoWindowing() && !VOIUtils.containsVOIAttributes(ds)) {
            float[] cw = VOIUtils.getMinMaxWindowCenterWidth(ds, data);
            return LookupTable.createLutForImage(ds, cw[0], cw[1], stored,
                    pval2gray);            
        }
        return LookupTable.createLutForImage(ds, stored, pval2gray);
    }
}
