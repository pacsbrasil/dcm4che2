/*
 * Copyright (C) 2002 Thomas Hacklaender, mailto:hacklaender@iftm.de
 *
 * IFTM Institut fuer Telematik in der Medizin GmbH, www.iftm.de
 *
 *  This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * http://www.gnu.org/copyleft/copyleft.html
 */
package org.dcm4cheri.imageio.plugins;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.dict.VRs;
import org.dcm4che.imageio.plugins.DcmImageWriteParam;
import org.dcm4che.imageio.plugins.DcmMetadata;
import org.dcm4che.util.UIDGenerator;
import org.w3c.dom.Document;

/**
 * This class implements an ImageIO compliant writer for DICOM Secondary Capture
 * images.<br>
 * Return  values of the default implementation of IamgeWriter: <br> 
 * boolean canInsertEmpty(int imageIndex) = false <br>
 * boolean canInsertImage(int imageIndex) = false <br>
 * boolean canRemoveImage(int imageIndex) = false <br>
 * boolean canReplaceImageMetadata(int imageIndex) = false <br>
 * boolean canReplacePixels(int imageIndex) = false <br>
 * boolean canReplaceStreamMetadata() = false <br>
 * boolean canWriteEmpty() = false <br>
 * boolean canWriteRasters() = false <br>
 * boolean canWriteSequence()  = false <br>
 * 
 * @author   Thomas Hacklaender
 * @version  2002.6.16
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>20021121 gunter zeilinger:</b>
 * <ul>
 * <li> Fix value for Pixel Aspect Ratio from 1 to int[]{1,1}
 * </ul>
 */
public class DcmImageWriter extends ImageWriter
{
    /**
	 * Version number
	 */
	public final static String VERSION = "1.0";

    /**
     * Holds a Dataset of (empty) attributes that are allowed in the
     * DICOM metadata
     */
    private static final Dataset DS_MASK;

    static {
        DS_MASK = DcmObjectFactory.getInstance().newDataset();
        
        // Patient IE, Patient Module, PS 3.3 - C.7.1.1, M
        DS_MASK.putPN(Tags.PatientName);                        // Type 2
        DS_MASK.putLO(Tags.PatientID);                          // Type 2
        DS_MASK.putDA(Tags.PatientBirthDate);                   // Type 2
        DS_MASK.putCS(Tags.PatientSex);                         // Type 2
        
        // Study IE, General Study Module, PS 3.3 - C.7.2.1, M
        DS_MASK.putUI(Tags.StudyInstanceUID);
        DS_MASK.putDA(Tags.StudyDate);                          // Type 2
        DS_MASK.putTM(Tags.StudyTime);                          // Type 2
        DS_MASK.putPN(Tags.ReferringPhysicianName);             // Type 2
        DS_MASK.putSH(Tags.StudyID);                            // Type 2
        DS_MASK.putSH(Tags.AccessionNumber);                    // Type 2
        
        // Study IE, Patient Study Module, PS 3.3 - C.7.2.2, U
        
        // Series IE, General Series Module, PS 3.3 - C.7.3.1, M
        DS_MASK.putCS(Tags.Modality);
        DS_MASK.putUI(Tags.SeriesInstanceUID);
        DS_MASK.putIS(Tags.SeriesNumber);                       // Type 2
        DS_MASK.putCS(Tags.Laterality);                         // Type 2C; only if ImageLaterality not sent; enumerativ L or R
        
        // Equipment IE, General Equipment Module, PS 3.3 - C.7.5.1, U
        
        // Equipment IE, SC Equipment Module, PS 3.3 - C.8.6.1, M
        DS_MASK.putCS(Tags.ConversionType);
        DS_MASK.putCS(Tags.Modality);                                       // Type 3; enumerativ see C7.3.1.1.1
        DS_MASK.putLO(Tags.SecondaryCaptureDeviceID);                       // Type 3
        DS_MASK.putLO(Tags.SecondaryCaptureDeviceManufacturer);             // Type 3
        DS_MASK.putLO(Tags.SecondaryCaptureDeviceManufacturerModelName);    //Type 3
        DS_MASK.putLO(Tags.SecondaryCaptureDeviceSoftwareVersion);          // Type 3
        DS_MASK.putSH(Tags.VideoImageFormatAcquired);                       // Type 3
        DS_MASK.putLO(Tags.DigitalImageFormatAcquired);                     // Type 3
        
        // Image IE, General Image Module, PS 3.3 - C.7.6.1, M
        DS_MASK.putIS(Tags.InstanceNumber);                     // Type 2
        DS_MASK.putCS(Tags.PatientOrientation);                 // Type 2C; see PS 3.3 - C.7.6.1.1.1
        DS_MASK.putDA(Tags.ContentDate);                        // Type 2C; if image is part of a series. Was Image Date
        DS_MASK.putTM(Tags.ContentTime);                        // Type 2C; if image is part of a series. Was Image Time
        
        // Image IE, Image Pixel Module, PS 3.3 - C.7.6.3, M
        DS_MASK.putUS(Tags.SamplesPerPixel);                    // Type 1
        DS_MASK.putCS(Tags.PhotometricInterpretation);          // Type 1
        DS_MASK.putUS(Tags.Rows);                               // Type 1
        DS_MASK.putUS(Tags.Columns);                            // Type 1
        DS_MASK.putUS(Tags.BitsAllocated);                      // Type 1
        DS_MASK.putUS(Tags.BitsStored);                         // Type 1
        DS_MASK.putUS(Tags.HighBit);                            // Type 1
        DS_MASK.putUS(Tags.PixelRepresentation);                // Type 1; 0x0=unsigned int, 0x1=2's complement
        DS_MASK.putOB(Tags.PixelData);                          // Type 1; OB or OW
        DS_MASK.putUS(Tags.PlanarConfiguration);                // Type 1C, if SamplesPerPixel > 1, should not present otherwise 
        DS_MASK.putIS(Tags.PixelAspectRatio);                   // Type 1C, if vertical/horizontal != 1
        DS_MASK.putSS(Tags.SmallestImagePixelValue);            // Type 3, if vertical/horizontal != 1
        DS_MASK.putSS(Tags.LargestImagePixelValue);             // Type 3, if vertical/horizontal != 1
        DS_MASK.putXX(Tags.RedPaletteColorLUTDescriptor);       // Type 1C; US/US or SS/US
        DS_MASK.putXX(Tags.GreenPaletteColorLUTDescriptor);     // Type 1C; US/US or SS/US
        DS_MASK.putXX(Tags.BluePaletteColorLUTDescriptor);      // Type 1C; US/US or SS/US
        DS_MASK.putXX(Tags.RedPaletteColorLUTData);             // Type 1C; US or SS or OW
        DS_MASK.putXX(Tags.GreenPaletteColorLUTData);           // Type 1C; US or SS or OW
        DS_MASK.putXX(Tags.BluePaletteColorLUTData);            // Type 1C; US or SS or OW
        
        // Image IE, SC Image Module, PS 3.3 - C.8.6.2, M
        DS_MASK.putDA(Tags.DateOfSecondaryCapture);             // Type 3
        DS_MASK.putTM(Tags.TimeOfSecondaryCapture);             // Type 3
        
        // Image IE, Overlay Plane Module, PS 3.3 - C.9.2, U
        
        // Image IE, Modality LUT Module, PS 3.3 - C.11.1, U
        DS_MASK.putDS(Tags.RescaleIntercept);                   // Type 1C; ModalityLUTSequence is not present
        DS_MASK.putDS(Tags.RescaleSlope);                       // Type 1C; ModalityLUTSequence is not present
        DS_MASK.putLO(Tags.RescaleType);                        // Type 1C; ModalityLUTSequence is not present; arbitrary text
        
        // Image IE, VOI LUT Module, PS 3.3 - C.11.2, U
        DS_MASK.putDS(Tags.WindowCenter);                       // Type 3
        DS_MASK.putDS(Tags.WindowWidth);                        // Type 1C; WindowCenter is present
        DS_MASK.putLO(Tags.WindowCenterWidthExplanation);       // Type 3; arbitrary text
        
        // Image IE, SOP Common Module, PS 3.3 - C.12.1, M
        DS_MASK.putUI(Tags.SOPClassUID);                        // Type 1
        DS_MASK.putUI(Tags.SOPInstanceUID);                     // Type 1
    }
    
    /**
     * Constructs an ImageWriter and sets its originatingProvider instance
     * variable to the supplied value.<br>
     * @param originatingProvider the ImageWriterSpi that is constructing this 
     *                            object, or null.
     */
    public DcmImageWriter(ImageWriterSpi originatingProvider)
    {
        super(originatingProvider);
    }
    
    /**
     * @param output must be an <code>ImageOutputStream</code> or an unchecked
     * <code>IllegalArgumentException</code> will be thrown
     */
    public void setOutput(Object output)
    {
        if (output != null) {
            if (output instanceof ImageOutputStream) {
                this.output = (ImageOutputStream)output;
            }
            else {
                throw new
                    IllegalArgumentException("output is not an ImageOutputStream");
            }
        }
        else {
            this.output = null;
        }
    }
    
    /**
     * Returns an IIOMetadata object that may be used for encoding and optionally 
     * modified using its document interfaces or other interfaces specific to the 
     * writer plug-in that will be used for encoding.<br>
     * An optional ImageWriteParam may be supplied for cases where it may affect 
     * the structure of the image metadata.<br>
     * If the supplied ImageWriteParam contains optional setting values not 
     * understood by this writer or transcoder, they will be ignored.
     * @param inData an IIOMetadata object representing image metadata, used to 
     *               initialize the state of the returned object.
     * @param imageType an ImageTypeSpecifier indicating the layout and color 
     *                  information of the image with which the metadata will be 
     *                  associated.
     * @param param an ImageWriteParam that will be used to encode the image, or null.
     * @return always null. The plug-in does not provide metadata encoding capabilities.
     */
    public IIOMetadata convertImageMetadata(IIOMetadata inData,
                                            ImageTypeSpecifier imageType,
                                            ImageWriteParam param)
    {
        // The DcmImageWriter can not encode other IIOMetadata types
        return null;
    }
    
    /**
     * Returns an IIOMetadata object that may be used for encoding and optionally 
     * modified using its document interfaces or other interfaces specific to the 
     * writer plug-in that will be used for encoding.<br>
     * An optional ImageWriteParam may be supplied for cases where it may affect 
     * the structure of the stream metadata.<br>
     * If the supplied ImageWriteParam contains optional setting values not 
     * understood by this writer or transcoder, they will be ignored. 
     * @param inData an IIOMetadata object representing stream metadata, used to 
     *               initialize the state of the returned object.
     * @param param an ImageWriteParam that will be used to encode the image, or null.
     * @return always null. The plug-in does not provide metadata encoding capabilities.
     */
    public IIOMetadata convertStreamMetadata(IIOMetadata inData, ImageWriteParam param)
    {
        // The DcmImageWriter can not encode other IIOMetadata types
        if (inData instanceof DcmMetadata)
            return inData;
        return null;
    }
    
    /**
     * Returns a new ImageWriteParam object of the appropriate type for this file 
     * format containing default values, that is, those values that would be used 
     * if no ImageWriteParam object were specified. This is useful as a starting 
     * point for tweaking just a few parameters and otherwise leaving the default 
     * settings alone.<br>
     * @return a new DcmImageWriteParam object containing default values. The object
     *         is a subclass of ImageWriteParam.
     */
    public ImageWriteParam getDefaultWriteParam()
    {
        return new DcmImageWriteParamImpl();
    }
    
    /**
     * Returns an IIOMetadata object containing default values for encoding an 
     * image of the given type. The contents of the object may be manipulated using
     * either the XML tree structure returned by the IIOMetadata.getAsTree  method, 
     * an IIOMetadataController object, or via plug-in specific interfaces, and the 
     * resulting data supplied to one of the write methods that take a stream 
     * metadata parameter.<br>
     * An optional ImageWriteParam may be supplied for cases where it may affect 
     * the structure of the image metadata.<br>
     * If the supplied ImageWriteParam contains optional setting values not 
     * supported by this writer, they will be ignored.<br>
     * @param imageType an ImageTypeSpecifier indicating the format of the image 
     *                  to be written later.
     * @param param an ImageWriteParam that will be used to encode the image, or null.
     * @return always null. The DcmImageWriter does not supports image-metadata
     */
    public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType, ImageWriteParam param)
    {
        return null;
    }
    
    /**
     * Returns an IIOMetadata object containing default values for encoding a stream 
     * of images. The contents of the object may be manipulated using either the XML
     * tree structure returned by the IIOMetadata.getAsTree method, an 
     * IIOMetadataController object, or via plug-in specific interfaces, and the 
     * resulting data supplied to one of the write methods that take a stream 
     * metadata parameter.<br>
     * An optional ImageWriteParam may be supplied for cases where it may affect the
     * structure of the stream metadata.<br>
     * If the supplied ImageWriteParam contains optional setting values not supported 
     * by this writer, they will be ignored.<br>
     * The returned metadata are sufficient to construct a Secondary Capture Image IOD.
     * @param param an ImageWriteParam that will be used to encode the image, or null.
     * @return an DcmMetadata object which is a subclass of an IIOMetadata object.
     */
    public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param)
    {
        Dataset             ds;
        UIDGenerator        uidGen;
        Date                now = new Date();
        SimpleDateFormat    dateFormatter = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat    timeFormatter = new SimpleDateFormat("HHmmss.SSS");
        DcmImageWriteParam  dcmParam = (DcmImageWriteParam)param;
        
        ds = DcmObjectFactory.getInstance().newDataset();
        uidGen = UIDGenerator.getInstance();
        
        // Secondary Capture Image IOD: PS 3.3 - A.8
        
        // Unique Identifiers (UIDs) PS 3.5 - 9
        // Each UID is composed of two parts: UID = <org root>.<suffix>
        
        // Patient IE, Patient Module, PS 3.3 - C.7.1.1, M
        //ds.putPN(Tags.PatientName, "PatientName");                          // Type 2
        //ds.putLO(Tags.PatientID, "PatientID");                              // Type 2
        //ds.putDA(Tags.PatientBirthDate, "19501031");                        // Type 2
        //ds.putCS(Tags.PatientSex, "O");                                     // Type 2
        
        // Study IE, General Study Module, PS 3.3 - C.7.2.1, M
        //ds.putUI(Tags.StudyInstanceUID, uidGen.createUID());
        //ds.putDA(Tags.StudyDate, dateFormatter.format(now));                // Type 2
        //ds.putTM(Tags.StudyTime, timeFormatter.format(now));                // Type 2
        //ds.putPN(Tags.ReferringPhysicianName, "ReferringPhysicianName");    // Type 2
        //ds.putSH(Tags.StudyID, "1");                                        // Type 2
        //ds.putSH(Tags.AccessionNumber, "0");                                // Type 2
        
        // Study IE, Patient Study Module, PS 3.3 - C.7.2.2, U
        
        // Series IE, General Series Module, PS 3.3 - C.7.3.1, M
        //ds.putCS(Tags.Modality, "OT");
        //ds.putUI(Tags.SeriesInstanceUID, uidGen.createUID());
        //ds.putIS(Tags.SeriesNumber, "1");                                   // Type 2
        //ds.putCS(Tags.Laterality, "");                                      // Type 2C; only if ImageLaterality not sent; enumerativ L or R
        
        // Equipment IE, General Equipment Module, PS 3.3 - C.7.5.1, U
        
        // Equipment IE, SC Equipment Module, PS 3.3 - C.8.6.1, M
        ds.putCS(Tags.ConversionType, "WSD");                               // Type 1
        ds.putCS(Tags.Modality, "OT");                                      // Type 3; enumerativ see C7.3.1.1.1
        // ds.putLO(Tags.SecondaryCaptureDeviceID, "");                        // Type 3
        // ds.putLO(Tags.SecondaryCaptureDeviceManufacturer, "dcm4cheri");     // Type 3
        // ds.putLO(Tags.SecondaryCaptureDeviceManufacturerModelName, "");  // Type 3
        // ds.putLO(Tags.SecondaryCaptureDeviceSoftwareVersion, VERSION);      // Type 3
        // ds.putSH(Tags.VideoImageFormatAcquired, "");                     // Type 3
        // ds.putLO(Tags.DigitalImageFormatAcquired, "");                   // Type 3
        
        // Image IE, General Image Module, PS 3.3 - C.7.6.1, M
        ds.putIS(Tags.InstanceNumber, "1");                                 // Type 2
        //String[] po = {"", ""};
        //ds.putCS(Tags.PatientOrientation, po);                            // Type 2C; see PS 3.3 - C.7.6.1.1.1
        //ds.putDA(Tags.ContentDate, dateFormatter.format(now));            // Type 2C; if image is part of a series. Was Image Date
        //ds.putTM(Tags.ContentTime, timeFormatter.format(now));            // Type 2C; if image is part of a series. Was Image Time
        
        // Image IE, Image Pixel Module, PS 3.3 - C.7.6.3, M
        // --> must be set by DcmImageWriteParams
        
        // Image IE, SC Image Module, PS 3.3 - C.8.6.2, M
        ds.putDA(Tags.DateOfSecondaryCapture, dateFormatter.format(now));   // Type 3
        ds.putTM(Tags.TimeOfSecondaryCapture, timeFormatter.format(now));   // Type 3
        
        // Image IE, Overlay Plane Module, PS 3.3 - C.9.2, U
        
        // Image IE, Modality LUT Module, PS 3.3 - C.11.1, U
        //ds.putDS(Tags.RescaleIntercept);                                    // Type 1C; ModalityLUTSequence is not present
        //ds.putDS(Tags.RescaleSlope);                                        // Type 1C; ModalityLUTSequence is not present
        //ds.putLO(Tags.RescaleType);                                         // Type 1C; ModalityLUTSequence is not present; arbitrary text
        
        // Image IE, VOI LUT Module, PS 3.3 - C.11.2, U
        //ds.putDS(Tags.WindowCenter);                                        // Type 3
        //ds.putDS(Tags.WindowWidth);                                         // Type 1C; WindowCenter is present
        //ds.putLO(Tags.WindowCenterWidthExplanation);                        // Type 3; arbitrary text
        
        // Image IE, SOP Common Module, PS 3.3 - C.12.1, M
        ds.putUI(Tags.SOPClassUID, UIDs.SecondaryCaptureImageStorage);      // Type 1
        ds.putUI(Tags.SOPInstanceUID, uidGen.createUID());                  // Type 1
        
        //create metadata backed by ds
        return new DcmMetadataImpl(ds);
    }
    
    /**
     * Appends a complete image stream containing a single image to the output. 
     * Any necessary header nformation is included.
     * If the output is an ImageOutputStream, its existing contents prior to the 
     * current seek position are not affected, and need not be readable or writable.<br>
     * The output must have been set beforehand using the setOutput method.
     * Stream metadata may optionally be supplied; if it is null, default stream 
     * metadata will be used.<br>
     * If canWriteRasters returns true, the IIOImage may contain a Raster source. 
     * Otherwise, it must contain a RenderedImage source.<br>
     * An ImageWriteParam may optionally be supplied to control the writing process. 
     * If param is null, a default write param will be used.<br>
     * If the supplied ImageWriteParam contains optional setting values not supported 
     * by this writer, they will be ignored.
     * @param streamMetadata an IIOMetadata object representing stream metadata, or 
     *                       null to use default values.
     * @param ioImage an IIOImage object containing an image, thumbnails, and metadata 
     *              to be written.
     * @param param an ImageWriteParam, or null to use a default ImageWriteParam.
     * @throws IllegalStateException  if the output has not been set.
     * @throws UnsupportedOperationException if image  contains a Raster and 
     *                                       canWriteRasters  returns false. 
     * @throws IllegalArgumentException if ioImage is null.
     * @throws IOException if an error occurs during writing.
     */
    public void write(IIOMetadata streamMetadata, IIOImage ioImage, ImageWriteParam param) throws IOException {
        Dataset             ds;
        DcmImageWriteParam  dcmParam;
        DcmMetadata dcmMetaData;
        
        //basic error checks
        if (output == null) {
          throw new IllegalStateException("output == null" + this);
        }
        
        if (! (output instanceof ImageOutputStream)) {
          throw new UnsupportedOperationException("output != ImageOutputStream" + this);
        }
        
        if (ioImage == null) {
          throw new IllegalArgumentException("image == null" + this);
        }
        
        if (ioImage.getRenderedImage() == null) {
          throw new IllegalArgumentException("RenderedImage == null" + this);
        }
        
        //DICOM write params
        if (param != null
            && !(param instanceof DcmImageWriteParam)) {
            throw new UnsupportedOperationException("param != DcmImageWriteParam" + this);
        }
        else if (param == null) {
            dcmParam = (DcmImageWriteParam)getDefaultWriteParam();
        }
        else {
            dcmParam = (DcmImageWriteParam)param;
        }
        
        //DICOM Metadata
        if (streamMetadata != null
            && !(streamMetadata instanceof DcmMetadata)) {
            throw new IllegalArgumentException("streamMetadata != DcmImageMetadata" + this);
        }
        else if (streamMetadata == null) {
            dcmMetaData = (DcmMetadata)getDefaultStreamMetadata(dcmParam); //ignores dcmParam, just creates a dummy dataset
        }
        else {
            dcmMetaData = (DcmMetadata)streamMetadata;
        }
        //filter out unwanted tags
        dcmMetaData = new DcmMetadataImpl(dcmMetaData.getDataset().subSet(DS_MASK));
        
        //get rendered image
        BufferedImage bi = (BufferedImage)ioImage.getRenderedImage();
        int dataType = bi.getType();
        
        //get Dataset from metadata
        ds = dcmMetaData.getDataset();
        
        //create a new FileMetaInfo object and reference it in the Dataset
        if (dcmParam.isWriteFMI()) {
            ds.setFileMetaInfo(DcmObjectFactory.getInstance().newFileMetaInfo(ds,
                getTransferSyntax(dcmParam.getDcmEncodeParameters())));
        }
        else {
            ds.setFileMetaInfo(null);
        }
        
        boolean writeAsMono = ((dataType == BufferedImage.TYPE_BYTE_GRAY
                                || dataType == BufferedImage.TYPE_USHORT_GRAY)
                               && !dcmParam.isWriteAlwaysRGB());
        
        if (writeAsMono) {
            System.out.println("writing as monochrome...");
            writeMONOCHROME(bi, dcmParam, ds);
        }
        else {
            boolean writeAsRGB = (dcmParam.isWriteAlwaysRGB()
                                  || dcmParam.isWriteIndexedAsRGB()
                                  || !(bi.getColorModel() instanceof IndexColorModel));
            
            if (writeAsRGB) {
                System.out.println("writing as rgb...");
                writeRGB(bi, dcmParam, ds);
            }
            else {
                System.out.println("writing as palette color...");
                writePALETTE(bi, dcmParam, ds);
            }
        }
    }
    
    /**
     * Write a RGB image, ignoring bits allocated, bits stored, and high bit in dcmParam.
     * @param sourceImage  the BufferedImage of type TYPE_INT_RGB to write.
     * @param dcmParam  the DcmImageWriteParam to use for writing.
     * @param ds the Dataset as given by the IIOMetadata.
     * @throws IOException if an error occurs during writing.
     */
    private void writeRGB(BufferedImage sourceImage, DcmImageWriteParam dcmParam, Dataset ds)
        throws IOException, IllegalArgumentException
    {
        Rectangle       rect;
        Rectangle       sourceRect;
        
        rect = new Rectangle(sourceImage.getWidth(), sourceImage.getHeight());
        if (dcmParam.getSourceRegion() == null) {
          sourceRect = rect;
        } else {
          sourceRect = rect.intersection(dcmParam.getSourceRegion());
        }
        //IllegalArgumentException thrown if sourceRect is empty
        if (sourceRect.isEmpty()) {
          throw new IllegalArgumentException("Source region is empty." + this);
        }
        
        int width = sourceRect.width;
        int height = sourceRect.height;
        boolean signed = false; //dcmParam.getPixelRepresentation();
        int bitsAllocd = 8;
        int bitsStored = 8; //forcing 8-bits per component!
        int highBit = 7;
        
        // Image IE, Image Pixel Module, PS 3.3 - C.7.6.3, M
        ds.putUS(Tags.SamplesPerPixel, 3);                      // Type 1
        ds.putUS(Tags.BitsAllocated, bitsAllocd);               // Type 1
        ds.putUS(Tags.BitsStored, bitsStored);                  // Type 1
        ds.putUS(Tags.HighBit, highBit);                        // Type 1
        ds.putCS(Tags.PhotometricInterpretation, "RGB");        // Type 1
        ds.putUS(Tags.Rows, height);                            // Type 1
        ds.putUS(Tags.Columns, width);                          // Type 1
        ds.putUS(Tags.PixelRepresentation, (signed) ? 1 : 0);   // Type 1; 0x0=unsigned int, 0x1=2's complement
        ds.putUS(Tags.PlanarConfiguration, 0);                  // Type 1C, if SamplesPerPixel > 1, should not present otherwise 
        ds.putIS(Tags.PixelAspectRatio, new int[]{1,1});        // Type 1C, if vertical/horizontal != 1
        
        byte[] rgbOut = new byte[width * height * 3];
        int dataType = sourceImage.getData().getDataBuffer().getDataType();
        ColorModel cm = sourceImage.getColorModel();
        int[] pixels = sourceImage.getRGB(sourceRect.x, sourceRect.y,
                                          width, height,
                                          (int[])null, 0, width);
        int ind = 0;
        
        for (int i = 0; i < pixels.length; i++) {
            rgbOut[ind++] = (byte)((pixels[i] >> 16) & 0xff);
            rgbOut[ind++] = (byte)((pixels[i] >> 8) & 0xff);
            rgbOut[ind++] = (byte)(pixels[i] & 0xff);
            /*rgbOut[ind++] = (byte)cm.getRed(pixels[i]);
            rgbOut[ind++] = (byte)cm.getGreen(pixels[i]);
            rgbOut[ind++] = (byte)cm.getBlue(pixels[i]);*/
        }
        
        //set pixeldata
        ds.putOB(Tags.PixelData, ByteBuffer.wrap(rgbOut));                      // Type 1; OB or OW
        
        //write Dataset
        ds.writeFile((ImageOutputStream) output, dcmParam.getDcmEncodeParameters());
    }
    
    /**
     * Write a MONOCHROME image, ignoring bits allocated, bits stored, and high bit in dcmParam.
     * @param sourceImage  the BufferedImage of type TYPE_INT_RGB to write.
     * @param dcmParam  the DcmImageWriteParam to use for writing.
     * @param ds the Dataset as given by the IIOMetadata.
     * @throws IOException if an error occurs during writing.
     */
    private void writeMONOCHROME(BufferedImage sourceImage, DcmImageWriteParam dcmParam, Dataset ds)
        throws IOException, IllegalArgumentException
    {
        ByteBuffer byteBuf;
        int max, min, value;
        Rectangle rect;
        short[] shortPixel;
        Rectangle sourceRect;
        
        rect = new Rectangle(sourceImage.getWidth(), sourceImage.getHeight());
        if (dcmParam.getSourceRegion() == null) {
          sourceRect = rect;
        } else {
          sourceRect = rect.intersection(dcmParam.getSourceRegion());
        }
        //IllegalArgumentException thrown if sourceRect is empty
        if (sourceRect.isEmpty()) {
          throw new IllegalArgumentException("Source region is empty." + this);
        }
        
        int dataType = sourceImage.getType();
        int width = sourceRect.width;
        int height = sourceRect.height;
        boolean signed = false; //dcmParam.getPixelRepresentation();
        int bitsAllocated = (dataType == BufferedImage.TYPE_BYTE_GRAY) ? 8 : 16;
        int bitsStored = bitsAllocated;
        int highBit = bitsStored - 1;
        boolean mono2 = dcmParam.isMONOCHROME2();
        
        // Image IE, Image Pixel Module, PS 3.3 - C.7.6.3, M
        ds.putUS(Tags.SamplesPerPixel, 1);                          // Type 1
        //TODO need seperate cases for monochrome1/2 ??
        if (mono2) {
          ds.putCS(Tags.PhotometricInterpretation, "MONOCHROME2");  // Type 1
        }
        else {
          ds.putCS(Tags.PhotometricInterpretation, "MONOCHROME1");  // Type 1
        }
        ds.putUS(Tags.Rows, height);          // Type 1
        ds.putUS(Tags.Columns, width);        // Type 1
        ds.putUS(Tags.BitsAllocated, bitsAllocated);                // Type 1
        ds.putUS(Tags.BitsStored, bitsStored);                      // Type 1
        ds.putUS(Tags.HighBit, highBit);                            // Type 1
        ds.putUS(Tags.PixelRepresentation, (signed) ? 1 : 0);       // Type 1; 0x0=unsigned int, 0x1=2's complement
        ds.putIS(Tags.PixelAspectRatio, new int[]{1,1});            // Type 1C, if vertical/horizontal != 1
        
        int[] pixels = sourceImage.getRaster().getPixels(sourceRect.x, sourceRect.y,
                                                         width, height,
                                                         (int[])null);
        
        //find min/max
        min = max = pixels[0];
        for (int i = 1; i < pixels.length; i++) {
            value = pixels[i];
            if (value > max)
                max = value;
            else if (value < min)
                min = value;
        }
        
        //write pixels to byte buffer
        byte[] out;
        if (bitsAllocated == 8) {
            out = new byte[pixels.length];
            for (int i = 1; i < pixels.length; i++)
                out[i] = (byte)(pixels[(i % 2 == 0)? i+1 : i-1] & 0xff);
        }
        else { //bitsAllocated == 16
            out = new byte[pixels.length * 2];
            for (int i = 1; i < pixels.length; ) {
                out[i++] = (byte)((pixels[i] >> 8) & 0xff);
                out[i++] = (byte)(pixels[i] & 0xff);
            }
        }
        byteBuf = ByteBuffer.wrap(out);
        
        //set pixeldata
        ds.putOW(Tags.PixelData, byteBuf);
        
        //set min/max pixel values
        ds.putSS(Tags.SmallestImagePixelValue, min);                  // Type 3
        ds.putSS(Tags.LargestImagePixelValue, max);                   // Type 3
        
        // Image IE, Modality LUT Module, PS 3.3 - C.11.1, U
        // don't overwrite if the Dataset already contains a Rescale Intercept/Slope
        float rs = ds.getFloat(Tags.RescaleSlope, 1).floatValue();
        float ri = ds.getFloat(Tags.RescaleIntercept, 0).floatValue();
        if (!ds.contains(Tags.RescaleIntercept)) {
          ds.putDS(Tags.RescaleIntercept, ri);                       // Type 1C; ModalityLUTSequence is not present
          ds.putDS(Tags.RescaleSlope, rs);                           // Type 1C; ModalityLUTSequence is not present
          ds.putLO(Tags.RescaleType, "PIXELVALUE");                   // Type 1C; ModalityLUTSequence is not present; arbitrary text
        }
        
        // Image IE, VOI LUT Module, PS 3.3 - C.11.2, U
        // don't overwrite if the Dataset already contains a Window Center/Width
        if (!ds.contains(Tags.WindowCenter)) {
          String[] wc = { Float.toString((rs * (max + min)) / 2 + ri) };
          ds.putDS(Tags.WindowCenter, wc);                            // Type 3
          String[] ww = { Float.toString((rs * (max - min)) / 2) };
          ds.putDS(Tags.WindowWidth, ww);                             // Type 1C; WindowCenter is present
          String[] we = { "DcmImageWriter" };
          ds.putLO(Tags.WindowCenterWidthExplanation, we);            // Type 3; arbitrary text
        }
        
        //write Dataset
        ds.writeFile((ImageOutputStream) output, dcmParam.getDcmEncodeParameters());
    }
    
    /**
     * Write a PALETTE COLOR image, ignoring bits allocated, bits stored, and high bit in dcmParam.
     * @param sourceImage  the BufferedImage of type TYPE_INT_RGB to write.
     * @param dcmParam  the DcmImageWriteParam to use for writing.
     * @param ds the Dataset as given by the IIOMetadata.
     * @throws IOException if an error occurs during writing.
     */
    private void writePALETTE(BufferedImage sourceImage, DcmImageWriteParam dcmParam, Dataset ds) throws IOException, IllegalArgumentException {
        Rectangle       rect;
        Rectangle       sourceRect;
        
        rect = new Rectangle(sourceImage.getWidth(), sourceImage.getHeight());
        if (dcmParam.getSourceRegion() == null) {
          sourceRect = rect;
        } else {
          sourceRect = rect.intersection(dcmParam.getSourceRegion());
        }
        //IllegalArgumentException thrown if sourceRect is empty
        if (sourceRect.isEmpty()) {
          throw new IllegalArgumentException("Source region is empty." + this);
        }
        
        int dataType = sourceImage.getData().getDataBuffer().getDataType();
        ColorModel cm = sourceImage.getColorModel();
        IndexColorModel icm;
        
        if (!(cm instanceof IndexColorModel)) {
            throw new IllegalArgumentException(
                "sourceImage's ColorModel must be an IndexColorModel to write"
                + " as a \"PALETTE COLOR\" DICOM image");
        }
        else {
            icm = (IndexColorModel)cm;
        }
        
        //write palette
        final int maxPaletteSize = 65536;
        final int paletteSize = icm.getMapSize();
        final int paletteIndexSize = icm.getPixelSize();
        
        //System.out.println("icm.getMapSize() = " + paletteSize);
        //System.out.println("icm.getPixelSize() = " + paletteIndexSize);
        
        //sanity check on palette size
        if (paletteSize > maxPaletteSize)
            throw new IllegalArgumentException(
                "sourceImage contains a palette that is too large to be"
                + " encoded as a DICOM Color LUT (" + paletteSize + ")");
        
        //sanity check on pixel size (the index into palette)
        if (paletteIndexSize == 0 || paletteIndexSize > 16)
            throw new UnsupportedOperationException("BufferedImages with a pixel size of "
                + paletteIndexSize + " bits are not supported, only 1 to 16 bits are supported");
        
        int width = sourceRect.width;
        int height = sourceRect.height;
        boolean signed = false; //dcmParam.getPixelRepresentation();
        int bitsAllocd = (paletteIndexSize <=8) ? 8 : 16;
        int bitsStored = bitsAllocd;
        int highBit = bitsStored - 1;
        
        // Image IE, Image Pixel Module, PS 3.3 - C.7.6.3, M
        ds.putUS(Tags.SamplesPerPixel, 1);                                      // Type 1
        ds.putCS(Tags.PhotometricInterpretation, "PALETTE COLOR");              // Type 1
        ds.putUS(Tags.Rows, height);                                            // Type 1
        ds.putUS(Tags.Columns, width);                                          // Type 1
        ds.putUS(Tags.BitsAllocated, bitsAllocd);                               // Type 1
        ds.putUS(Tags.BitsStored, bitsStored);                                  // Type 1
        ds.putUS(Tags.HighBit, highBit);                                        // Type 1
        ds.putUS(Tags.PixelRepresentation, (signed) ? 1 : 0);                   // Type 1; 0x0=unsigned int, 0x1=2's complement
        ds.putIS(Tags.PixelAspectRatio, new int[]{1,1});                        // Type 1C, if vertical/horizontal != 1
        
        //write palette descriptor
        ByteBuffer pDescriptor;
        byte[] rPalette;
        byte[] gPalette;
        byte[] bPalette;
        ByteBuffer rByteBuffer;
        ByteBuffer gByteBuffer;
        ByteBuffer bByteBuffer;
        
        pDescriptor = ByteBuffer.allocate(3 * 2);
        pDescriptor.putShort((short) ((paletteSize == maxPaletteSize) ? 0 : paletteSize));  // number of entries
        pDescriptor.putShort((short) 0);    // first stored pixel value mapped
        pDescriptor.putShort((short) 8);    // number of bits, always 8 for IndexColorModel's internal color component tables
        
        ds.putXX(Tags.RedPaletteColorLUTDescriptor, VRs.US, pDescriptor);     // Type 1C; US/US or SS/US
        ds.putXX(Tags.GreenPaletteColorLUTDescriptor, VRs.US, pDescriptor);   // Type 1C; US/US or SS/US
        ds.putXX(Tags.BluePaletteColorLUTDescriptor, VRs.US, pDescriptor);    // Type 1C; US/US or SS/US
        
        rPalette = new byte[paletteSize];
        gPalette = new byte[paletteSize];
        bPalette = new byte[paletteSize];
        icm.getReds(rPalette);
        icm.getGreens(gPalette);
        icm.getBlues(bPalette);
        
        //default is ByteOrder.BIG_ENDIAN byte ordering
        rByteBuffer = ByteBuffer.allocate(paletteSize);
        gByteBuffer = ByteBuffer.allocate(paletteSize);
        bByteBuffer = ByteBuffer.allocate(paletteSize);
        
        //grab word chunks and place words in buffer
        for (int idx = 0; idx < paletteSize; idx += 2) {
            rByteBuffer.putShort((short) ((rPalette[idx+1] << 8) + rPalette[idx]));
            gByteBuffer.putShort((short) ((gPalette[idx+1] << 8) + gPalette[idx]));
            bByteBuffer.putShort((short) ((bPalette[idx+1] << 8) + bPalette[idx]));
        }
        
        ds.putOW(Tags.RedPaletteColorLUTData,rByteBuffer);           // Type 1C; US or SS or OW
        ds.putOW(Tags.GreenPaletteColorLUTData, gByteBuffer);        // Type 1C; US or SS or OW
        ds.putOW(Tags.BluePaletteColorLUTData, bByteBuffer);         // Type 1C; US or SS or OW
        
        //read pixels as int's, should be one sample per each pixel (palette index)
        int[] pixels = sourceImage.getRaster().getPixels(sourceRect.x, sourceRect.y,
                                                         width, height,
                                                         (int[])null);
        byte[] indOut;
        int ind = 0;
        //put pixeldata (must be OW if transfer syntax is Implicit VR/Little Endian)
        if (paletteIndexSize <= 8) {
            indOut = new byte[width * height];
            for (int i = 0; i < pixels.length; i++) {
                indOut[i] = (byte)(pixels[(i % 2 == 0)? i+1 : i-1] & 0xff);
            }
            ds.putOW(Tags.PixelData, ByteBuffer.wrap(indOut));              // Type 1; OW
        }
        else if (paletteIndexSize > 8 && paletteIndexSize <= 16) {
            indOut = new byte[width * height * 2];
            for (int i = 0; i < pixels.length; i++) {
                indOut[ind++] = (byte)((pixels[i] >> 8) & 0xff);
                indOut[ind++] = (byte)(pixels[i] & 0xff);
            }
            ds.putOW(Tags.PixelData, ByteBuffer.wrap(indOut));              // Type 1; OW
        }
        
        //write Dataset
        ds.writeFile((ImageOutputStream) output, dcmParam.getDcmEncodeParameters());
    }
    
    private String getTransferSyntax(DcmEncodeParam dcmEncodeParams)
    {
        if (dcmEncodeParams.byteOrder == ByteOrder.LITTLE_ENDIAN) {
            if (dcmEncodeParams.explicitVR)
                return UIDs.ExplicitVRLittleEndian;
            else
                return UIDs.ImplicitVRLittleEndian;
        }
        else if (dcmEncodeParams.byteOrder == ByteOrder.BIG_ENDIAN
            && dcmEncodeParams.explicitVR)
            return UIDs.ExplicitVRBigEndian;
        else {
            throw new IllegalStateException("Bad DICOM encoding parameters");
        }
    }
    
    public static void main(String[] args)
        throws IOException
    {
        final String xml = "/home/joe/dump.xml";
        Document doc;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(xml));
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
        
        BufferedImage bi;
        final String testimg = "/home/joe/work/dicom/dicom-images/MR001.dcm"; //dicom-images/sbarre/OT-PAL-8-face";//OT-PAL-8-face,MR001.dcm
        DcmImageReader rdr;
        
        Iterator iter = ImageIO.getImageReadersByFormatName("DICOM");
        rdr = (DcmImageReader)iter.next();
        rdr.setInput(ImageIO.createImageInputStream(new File(testimg)));
        bi = rdr.read(0);
        System.out.println("read ok");
        
        int dataType = bi.getType();
        System.out.println("type = " + dataType);
        
        iter = ImageIO.getImageWritersByFormatName("DICOM");
        DcmImageWriter writer = (DcmImageWriter) iter.next();
        writer.setOutput(ImageIO.createImageOutputStream(new File(testimg + ".out")));
        //DcmMetadata dcmmd = (DcmMetadata)genrdr.getStreamMetadata();
        DcmImageWriteParam wparam = (DcmImageWriteParam)writer.getDefaultWriteParam();
        wparam.setWriteAlwaysRGB(false);
        wparam.setWriteIndexedAsRGB(false);
        wparam.setMONOCHROME2(true);
        DcmMetadata dcmmd = (DcmMetadata)writer.getDefaultStreamMetadata(wparam);
        dcmmd.setFromTree(DcmMetadata.nativeMetadataFormatName, doc);
        writer.write(dcmmd, new IIOImage(bi,null,null), wparam);
        System.out.println("write ok");
    }
}
