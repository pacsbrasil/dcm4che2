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
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
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
import java.util.List;

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
import org.dcm4che2.image.LookupTable;
import org.dcm4che2.image.PaletteColorUtils;
import org.dcm4che2.image.SimpleYBRColorSpace;
import org.dcm4che2.imageio.ImageReaderFactory;
import org.dcm4che2.imageio.ItemParser;
import org.dcm4che2.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.StopTagInputHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.media.imageio.stream.RawImageInputStream;
import com.sun.media.imageio.stream.SegmentedImageInputStream;
import com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageReaderCodecLib;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Sep 2, 2006
 */
public class DicomImageReader extends ImageReader {
    
    private static final Logger log = 
            LoggerFactory.getLogger(DicomImageReader.class);
    
    private static String PMI_DEFAULT_1 = "MONOCHROME2";
    
    private static String PMI_DEFAULT_3 = "RGB";

    private static final String[] PMI = {
        "MONOCHROME1", "MONOCHROME2", "PALETTE COLOR",
        "RGB", "YBR_FULL", "YBR_FULL_422", "YBR_PARTIAL_422",
        "YBR_PARTIAL_420", "YBR_ICT", "YBR_RCT"
    };
    
    private static final List PMI_LIST = Arrays.asList(PMI);
    
    private static final int MONOCHROME1 = 0;
    private static final int MONOCHROME2 = 1;
    private static final int PALETTE_COLOR = 2;
    private static final int RGB = 3;
    private static final int YBR_FULL = 4;
    private static final int YBR_FULL_422 = 5;
    private static final int YBR_PARTIAL_422 = 6;
    private static final int YBR_PARTIAL_420 = 7;
    private static final int YBR_ICT = 8;
    private static final int YBR_RCT = 9;
    
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
    private int[] bits;
    private int pmi;
    private boolean banded;
    private boolean signed;
    private long pixelDataPos;
    private int pixelDataLen;
    private boolean compressed;
    private long[] frameOffsets;
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
        width = 0;
        height = 0;
        frames = 0;
        allocated = 0;
        stored = 0;
        dataType = 0;
        samples = 0;
        bits = null;
        pmi = -1;
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
    
    private static boolean isColor(int pmi) {
        return pmi >= PALETTE_COLOR;
    }
    
    private static int samples4pmi(int pmi) {
        return pmi >= RGB ? 3 : 1; 
    }
    

    public ImageReadParam getDefaultReadParam() {
        return new DicomImageReadParam();
    }    

    public IIOMetadata getStreamMetadata() throws IOException {
        return null;
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
        if (dis.tag() == Tag.PixelData) {
            pixelDataPos = dis.getStreamPosition();
            pixelDataLen = dis.valueLength();
            compressed = pixelDataLen == -1;
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
            if (samples != 1 && samples != 3)  {
                throw new IIOException(
                        "Unsupported Samples per Pixel: " + samples);                
            }
            bits = new int[samples];
            Arrays.fill(bits, stored);
            String pmiStr = ds.getString(Tag.PhotometricInterpretation,
                    samples == 1 ? PMI_DEFAULT_1 : PMI_DEFAULT_3);
            pmi = PMI_LIST.indexOf(pmiStr);
            if (pmi == -1) {
                throw new IIOException(
                        "Unsupported Photometric Interpretation: " + pmiStr);                
            }
            if (samples != samples4pmi(pmi)) {
                throw new IIOException("Illegal Samples per Pixel: " + samples +
                        " for Photometric Interpretation: " + pmiStr);
            }
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
        if (itemParser == null) {
            this.itemParser = new ItemParser(dis, iis);
            this.itemStream = new SegmentedImageInputStream(iis, itemParser);
        }
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
        ColorModel cm = createColorModel();
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

    private ColorModel createColorModel() {
        if (pmi == PALETTE_COLOR) {
            return PaletteColorUtils.createPaletteColorModel(ds);
        } else {
            ColorSpace cs;
            if (isColor(pmi)) {
                cs = createRGBColorSpace();
                if (pmi != RGB) {
                    if (pmi == YBR_FULL || pmi == YBR_FULL_422) {
                        cs = SimpleYBRColorSpace.createYBRFullColorSpace(cs);
                    } else if (pmi == YBR_PARTIAL_420
                            || pmi == YBR_PARTIAL_422) {
                        cs = SimpleYBRColorSpace.createYBRPartialColorSpace(cs);                   
                    }
                }
            } else {
                cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
            }
            return new ComponentColorModel(cs, bits, false, false,
                    Transparency.OPAQUE, dataType);
        }
    }

    private ColorSpace createRGBColorSpace() {
        byte[] iccProfile = ds.getBytes(Tag.ICCProfile);
        return iccProfile != null
                ? new ICC_ColorSpace(ICC_Profile.getInstance(iccProfile))
                : ColorSpace.getInstance(ColorSpace.CS_sRGB);
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
        if (!isColor(pmi)) {
            ColorModel cm = bi.getColorModel();
            WritableRaster raster = bi.getRaster();
            DataBuffer data = raster.getDataBuffer();
            LookupTable lut = createLut((DicomImageReadParam) param);
            byte[] bb;
            short[] ss;
            switch (data.getDataType()) {
            case DataBuffer.TYPE_BYTE:
                bb = ((DataBufferByte) data).getData();
                lut.lookup(bb, bb);
                break;
            case DataBuffer.TYPE_USHORT:
                ss = ((DataBufferUShort) data).getData();
                lut.lookup(ss, ss);
                break;
            case DataBuffer.TYPE_SHORT:
                ss = ((DataBufferShort) data).getData();
                lut.lookup(ss, ss);
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
                    if (frameOffsets[i+1] == 0)
                        decompress(i, param);
            }
        }
    }

    private BufferedImage decompress(int imageIndex, ImageReadParam param)
            throws IOException {
        log.debug("Start decompressing frame#" + (imageIndex+1));
        itemStream.seek(this.frameOffsets[imageIndex]);
        reader.setInput(itemStream);
        BufferedImage bi = reader.read(0, param);
        // workaround for Bug in J2KImageReaderCodecLib.reset()
        if (reader instanceof J2KImageReaderCodecLib) {
            reader.dispose();
            reader = null;
        } else {
            reader.reset();
        }
        final int nextIndex = imageIndex + 1;
        if (nextIndex < frameOffsets.length && frameOffsets[nextIndex] == 0) {
            this.frameOffsets[nextIndex] = itemParser.seekNextFrame(itemStream);
        }
        log.debug("Finished decompressed frame#" + (imageIndex+1));
        return bi;
    }

    private LookupTable createLut(DicomImageReadParam param) {
        DicomObject pr = param.getPresentationState();
        if (pr != null) {
            return LookupTable.createLutForImageWithPR(ds, pr, stored);
        }
        DicomObject voiLut = param.getVoiLut();
        if (voiLut != null) {
            return LookupTable.createLutForImage(ds, voiLut, stored);
        }
        float c = param.getWindowCenter();
        float w = param.getWindowWidth();
        if (w > 0) {            
            return LookupTable.createLutForImage(ds, c, w, stored);
        }
        return LookupTable.createLutForImage(ds, stored);
    }
}
