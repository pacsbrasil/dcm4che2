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

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.nio.*;
import java.nio.*;
import java.util.*;
import java.text.*;
import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;
import javax.imageio.spi.*;

import org.dcm4che.data.*;
import org.dcm4che.dict.*;
import org.dcm4che.util.*;
import org.dcm4che.imageio.plugins.*;


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
 */
public class DcmImageWriter extends ImageWriter {

  
	/**
	 * Version number
	 */
	public final static String    VERSION = "1.0";

  
  /**
   * Constructs an ImageWriter and sets its originatingProvider instance
   * variable to the supplied value.<br>
   * @param originatingProvider the ImageWriterSpi that is constructing this 
   *                            object, or null.
   */
  public DcmImageWriter(ImageWriterSpi originatingProvider) {
    super(originatingProvider);
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
   * @pram param an ImageWriteParam that will be used to encode the image, or null.
   * @return allways null. The plug-in does not provide metadata encoding capabilities.
   */
  public IIOMetadata convertImageMetadata(IIOMetadata inData, ImageTypeSpecifier imageType, ImageWriteParam param) {
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
   * @pram param an ImageWriteParam that will be used to encode the image, or null.
   * @return allways null. The plug-in does not provide metadata encoding capabilities.
   */
  public IIOMetadata convertStreamMetadata(IIOMetadata inData, ImageWriteParam param) {
    // The DcmImageWriter can not encode other IIOMetadata types
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
  public ImageWriteParam getDefaultWriteParam() {
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
   * @return allways null. The DcmImageWriter does not supports image-metadata
   */
  public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType, ImageWriteParam param) {
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
   * The returned metadata are sufficient to construct a Sacondary Capture Image IOD.
   * @param param an ImageWriteParam that will be used to encode the image, or null.
   * @return an DcmMetadata object which is a subclass of an IIOMetadata object.
   */
  public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) {
    Dataset           ds;
    UIDGenerator      uidGen;
    Date              now = new Date();
    SimpleDateFormat  dateFormatter = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat  timeFormatter = new SimpleDateFormat("HHmmss.SSS");
    
    ds = DcmObjectFactory.getInstance().newDataset();
    uidGen = UIDGenerator.getInstance();
    
    // Secondary Capture Image IOD: PS 3.3 - A.8
    
    // Unique Identifiers (UIDs) PS 3.5 - 9
    // Each UID is composed of two parts: UID = <org root>.<suffix>
    
    // Patient IE, Patient Module, PS 3.3 - C.7.1.1, M
    ds.putPN(Tags.PatientName, "PatientName");                          // Type 2
    ds.putLO(Tags.PatientID, "PatientID");                              // Type 2
    ds.putDA(Tags.PatientBirthDate, "19501031");                        // Type 2
    ds.putCS(Tags.PatientSex, "O");                                     // Type 2
    
    // Study IE, General Study Module, PS 3.3 - C.7.2.1, M
    ds.putUI(Tags.StudyInstanceUID, uidGen.createUID());
    ds.putDA(Tags.StudyDate, dateFormatter.format(now));                // Type 2
    ds.putTM(Tags.StudyTime, timeFormatter.format(now));                // Type 2
    ds.putPN(Tags.ReferringPhysicianName, "ReferringPhysicianName");    // Type 2
    ds.putSH(Tags.StudyID, "1");                                        // Type 2
    ds.putSH(Tags.AccessionNumber, "0");                                // Type 2
    
    // Study IE, Patient Study Module, PS 3.3 - C.7.2.2, U
    
    // Series IE, General Series Module, PS 3.3 - C.7.3.1, M
    ds.putCS(Tags.Modality, "OT");
    ds.putUI(Tags.SeriesInstanceUID, uidGen.createUID());
    ds.putIS(Tags.SeriesNumber, "1");                                   // Type 2
    ds.putCS(Tags.Laterality, "");                                      // Type 2C; only if ImageLaterality not sent; enumerativ L or R

    
    // Equipment IE, General Equipment Module, PS 3.3 - C.7.5.1, U
    
    // Equipment IE, SC Equipment Module, PS 3.3 - C.8.6.1, M
    ds.putCS(Tags.ConversionType, "WSD");
    ds.putCS(Tags.Modality, "OT");                                      // Type 3; enumerativ see C7.3.1.1.1
    ds.putLO(Tags.SecondaryCaptureDeviceID, "");                        // Type 3
    ds.putLO(Tags.SecondaryCaptureDeviceManufacturer, "dcm4cheri");     // Type 3
    // ds.putLO(Tags.SecondaryCaptureDeviceManufacturerModelName, "");  // Type 3
    ds.putLO(Tags.SecondaryCaptureDeviceSoftwareVersion, "1.0");        // Type 3
    // ds.putSH(Tags.VideoImageFormatAcquired, "");                     // Type 3
    // ds.putLO(Tags.DigitalImageFormatAcquired, "");                   // Type 3
    
    
    // Image IE, General Image Module, PS 3.3 - C.7.6.1, M
    ds.putIS(Tags.InstanceNumber, "1");                                 // Type 2
    String[] po = {"", ""};
    ds.putCS(Tags.PatientOrientation, po);                              // Type 2C; see PS 3.3 - C.7.6.1.1.1
    ds.putDA(Tags.ContentDate, dateFormatter.format(now));              // Type 2C; if image is part of a series. Was Image Date
    ds.putTM(Tags.ContentTime, timeFormatter.format(now));              // Type 2C; if image is part of a series. Was Image Time
    
    
    // Image IE, Image Pixel Module, PS 3.3 - C.7.6.3, M
    // ds.putUS(Tags.SamplesPerPixel, -1);                                             // Type 1
    // ds.putCS(Tags.PhotometricInterpretation, "");                                   // Type 1
    // ds.putUS(Tags.Rows, -1);                                                        // Type 1
    // ds.putUS(Tags.Columns, -1);                                                     // Type 1
    // ds.putUS(Tags.BitsAllocated, -1);                                               // Type 1
    // ds.putUS(Tags.BitsStored, -1);                                                  // Type 1
    // ds.putUS(Tags.HighBit, -1);                                                     // Type 1
    // ds.putUS(Tags.PixelRepresentation, -1);                                         // Type 1; 0x0=unsigned int, 0x1=2's complement
    // ds.putOB(Tags.PixelData, ByteBuffer.allocate(0));                               // Type 1; or ds.putOW
    // ds.putUS(Tags.PlanarConfiguration, -1);                                         // Type 1C, if SamplesPerPixel > 1, should not present otherwise 
    // ds.putIS(Tags.PixelAspectRatio, 1);                                             // Type 1C, if vertical/horizontal != 1
    // ds.putSS(Tags.SmallestImagePixelValue, 0);                                      // Type 3, if vertical/horizontal != 1
    // ds.putSS(Tags.LargestImagePixelValue, 0);                                       // Type 3, if vertical/horizontal != 1
    // ds.putXX(Tags.RedPaletteColorLUTDescriptor, VRs.US, ByteBuffer.allocate(0));    // Type 1C; US/US or SS/US
    // ds.putXX(Tags.GreenPaletteColorLUTDescriptor, VRs.US, ByteBuffer.allocate(0));  // Type 1C; US/US or SS/US
    // ds.putXX(Tags.BluePaletteColorLUTDescriptor, VRs.US, ByteBuffer.allocate(0));   // Type 1C; US/US or SS/US
    // ds.putXX(Tags.RedPaletteColorLUTData, VRs.US, ByteBuffer.allocate(0));          // Type 1C; US or SS or OW
    // ds.putXX(Tags.GreenPaletteColorLUTData, VRs.US, ByteBuffer.allocate(0));        // Type 1C; US or SS or OW
    // ds.putXX(Tags.BluePaletteColorLUTData, VRs.US, ByteBuffer.allocate(0));         // Type 1C; US or SS or OW
    
    // Image IE, SC Image Module, PS 3.3 - C.8.6.2, M
    ds.putDA(Tags.DateOfSecondaryCapture, dateFormatter.format(now));   // Type 3
    ds.putTM(Tags.TimeOfSecondaryCapture, timeFormatter.format(now));   // Type 3
    
    // Image IE, Overlay Plane Module, PS 3.3 - C.9.2, U
    
    // Image IE, Modality LUT Module, PS 3.3 - C.11.1, U
    // ds.putDS(Tags.RescaleIntercept, "0");                            // Type 1C; ModalityLUTSequence is not present
    // ds.putDS(Tags.RescaleSlope, "1");                                // Type 1C; ModalityLUTSequence is not present
    // ds.putLO(Tags.RescaleType, "PIXELVALUE");                        // Type 1C; ModalityLUTSequence is not present; arbitrary text
    
    // Image IE, VOI LUT Module, PS 3.3 - C.11.2, U
    // String[] wc = {""};
    // ds.putDS(Tags.WindowCenter, wc);                                 // Type 3
    // String[] ww = {""};
    // ds.putDS(Tags.WindowWidth, ww);                                  // Type 1C; WindowCenter is present
    // String[] we = {"",};
    // ds.putLO(Tags.WindowCenterWidthExplanation, we);                 // Type 3; arbitrary text
    
    // Image IE, SOP Common Module, PS 3.3 - C.12.1, M
    ds.putUI(Tags.SOPClassUID, UIDs.SecondaryCaptureImageStorage);      // Type 1
    ds.putUI(Tags.SOPInstanceUID, uidGen.createUID());                  // Type 1

    // Metadaten aus Dataset erzeugen
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
   * @param image an IIOImage object containing an image, thumbnails, and metadata 
   *              to be written.
   * @param param an ImageWriteParam, or null to use a default ImageWriteParam.
   * @throws IllegalStateException  if the output has not been set.
   * @throws UnsupportedOperationException if image  contains a Raster and 
   *                                       canWriteRasters  returns false. 
   * @throws IllegalArgumentException if image is null.
   * @throws IOException if an error occurs during writing.
   */
  public void write(IIOMetadata streamMetadata, IIOImage ioImage, ImageWriteParam param) throws IOException {
    Dataset             ds;
    DcmImageWriteParam  dcmParam;
    
    // Gueltigkeit der Parameter ueberpruefen
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
    
    if (! (streamMetadata instanceof DcmMetadata)) {
      throw new IllegalArgumentException("streamMetadata != DcmImageMetadata" + this);
    }
    
    // dcmParam setzen, ggf. DefaultWriteParam verwenden
    if (param == null) {
      param = this.getDefaultWriteParam();
    } else {
      if (! (param instanceof DcmImageWriteParam)) {
        throw new UnsupportedOperationException("param != DcmImageWriteParam" + this);
      }
    }
    dcmParam = (DcmImageWriteParam) param;
    
    //>>>>>>>>>>>>>>>>>>>
    // Test
    // dcmParam.setSourceRegion(new Rectangle(64, 64, 512, 512));
    // dcmParam.setWriteFMI(false);
    // dcmParam.setMONOCHROME2(false);
    // dcmParam.setBitsStored(12);
    // dcmParam.setHighBit(11);
    // dcmParam.setWriteAlwaysRGB(true);
    //<<<<<<<<<<<<<<<<<<<
    
    // Das Dataset aus den Metadaten extrahieren
    ds = ((DcmMetadata) streamMetadata).getDataset();

    // Ggf. File-Meta-Information-Block schreiben
    if (dcmParam.isWriteFMI()) {
      DcmObjectFactory.getInstance().newFileMetaInfo(ds, UIDs.ImplicitVRLittleEndian).write((ImageOutputStream) output);
    }
    
    // Alle nicht speziell implementierten Typen des BufferedImage werden nach
    // RGB konvertiert und so geschreben
    switch (((BufferedImage) ioImage.getRenderedImage()).getType()) {
      
      case BufferedImage.TYPE_BYTE_GRAY:
      case BufferedImage.TYPE_USHORT_GRAY:
        if (dcmParam.isWriteAlwaysRGB()) {
          writeRGB((BufferedImage) ioImage.getRenderedImage(), dcmParam, ds);
        } else {
          writeMONOCHROME((BufferedImage) ioImage.getRenderedImage(), dcmParam, ds);
        }
        break;
        
      case BufferedImage.TYPE_BYTE_INDEXED:
        if (dcmParam.isWriteAlwaysRGB() | dcmParam.isWriteIndexedAsRGB()) {
          writeRGB((BufferedImage) ioImage.getRenderedImage(), dcmParam, ds);
        } else {
          writePALETTE((BufferedImage) ioImage.getRenderedImage(), dcmParam, ds);
        }
        break;
        
      case BufferedImage.TYPE_INT_RGB:
      default:
        writeRGB((BufferedImage) ioImage.getRenderedImage(), dcmParam, ds);
    }
    
  }
  
  
  /**
   * Write a RGB image.
   * @param sourceImage  the BufferedImage of type TYPE_INT_RGB to write.
   * @param dcmParam  the DcmImageWriteParam to use for writing.
   * @param ds the Dataset as given by the IIOMetadata.
   * @throws IOException if an error occurs during writing.
   */
  private void writeRGB(BufferedImage sourceImage, DcmImageWriteParam dcmParam, Dataset ds) throws IOException, IllegalArgumentException {
    ByteBuffer      byteBuf;
    int[]           dataBuf;
    int             dataBufIndex;
    BufferedImage   destinationImage;
    Graphics        g;
    Rectangle       rect;
    Rectangle       sourceRect;
    int             value;
    
    // Die Source-Region (Rechteck) mit dem sourceImage clippen
    rect = new Rectangle(sourceImage.getWidth(), sourceImage.getHeight());
    if (dcmParam.getSourceRegion() == null) {
      // sourceRect umfasst das gesamte sourceImage
      sourceRect = rect;
    } else {
      // sourceRect umfasst die Schnittmenge von sourceImage und sourceRect
      sourceRect = rect.intersection(dcmParam.getSourceRegion());
    }
    // Exception, falls sourceRect leer ist
    if (sourceRect.isEmpty()) {
      throw new IllegalArgumentException("Source region is empty." + this);
    }

    // Ein neues BufferedImage mit definierten Eigenschaften erzeugen:
    // - DirectColorModel
    // - IntegerInterleavedRaster
    // - DataBufferInt
    // - SingelPixelPackedSampleModel: R=0x00FF0000, G=0x0000FF00, B=0x000000FF
    destinationImage = new BufferedImage(sourceRect.width, sourceRect.height, BufferedImage.TYPE_INT_RGB);

    // sourceImage unter Beruecksichtigung des sourceRect in destinationImage kopieren
    g = destinationImage.createGraphics();
    g.drawImage(sourceImage, 0, 0, destinationImage.getWidth(), destinationImage.getHeight(), sourceRect.x, sourceRect.y, sourceRect.x + sourceRect.width, sourceRect.y + sourceRect.height, null);
    g.dispose();

    // Image IE, Image Pixel Module, PS 3.3 - C.7.6.3, M
    ds.putUS(Tags.SamplesPerPixel, 3);                      // Type 1
    ds.putUS(Tags.BitsAllocated, 8);                        // Type 1
    ds.putUS(Tags.BitsStored, 8);                           // Type 1
    ds.putUS(Tags.HighBit, 7);                              // Type 1
    ds.putCS(Tags.PhotometricInterpretation, "RGB");        // Type 1
    ds.putUS(Tags.Rows, destinationImage.getHeight());      // Type 1
    ds.putUS(Tags.Columns, destinationImage.getWidth());    // Type 1
    ds.putUS(Tags.PixelRepresentation, 0);                  // Type 1; 0x0=unsigned int, 0x1=2's complement
    ds.putUS(Tags.PlanarConfiguration, 0);                  // Type 1C, if SamplesPerPixel > 1, should not present otherwise 
    ds.putIS(Tags.PixelAspectRatio, 1);                     // Type 1C, if vertical/horizontal != 1

    dataBuf = ((DataBufferInt) destinationImage.getRaster().getDataBuffer()).getData();
    byteBuf = ByteBuffer.allocate(dataBuf.length * 3);
    dataBufIndex = 0;
    while (dataBufIndex < dataBuf.length) {
      value = dataBuf[dataBufIndex];
      byteBuf.put((byte) ((value >> 16) & 0xff));   // R-Kanal
      byteBuf.put((byte) ((value >>  8) & 0xff));   // G-Kanal
      byteBuf.put((byte) ( value        & 0xff));   // B-Kanal
      dataBufIndex++;
    }

    // Pixeldaten in Dataset eintragen
    ds.putOB(Tags.PixelData, byteBuf);                      // Type 1; or ds.putOW

    // Dataset schreiben
    ds.writeDataset((ImageOutputStream) output, DcmEncodeParam.valueOf(UIDs.ImplicitVRLittleEndian));

  }
  
  
  
  
  /**
   * Write a MONOCHROME image.
   * @param sourceImage  the BufferedImage of type TYPE_INT_RGB to write.
   * @param dcmParam  the DcmImageWriteParam to use for writing.
   * @param ds the Dataset as given by the IIOMetadata.
   * @throws IOException if an error occurs during writing.
   */
  private void writeMONOCHROME(BufferedImage sourceImage, DcmImageWriteParam dcmParam, Dataset ds) throws IOException, IllegalArgumentException {
    BufferedImage     bi;
    int               bitsAllocated;
    int               bitsStored;
    ByteBuffer        byteBuf;
    byte[]            bytePixel;
    short[]           dataBuf;
    int               dataBufIndex;
    BufferedImage     destinationImage;
    Graphics          g;
    int               highBit;
    int               intValue;
    int               max;
    int               min;
    Raster            raster;
    Rectangle         rect;
    DataBufferUShort  shortBuffer;
    short[]           shortPixel;
    Rectangle         sourceRect;
    short             value;
    
    // 8 Bit Graustufenbilder werden nicht korrekt ueber Graphics.drawImage()
    // in ein 16 Bit Grauwertbild konvertiert. Aus dem 8-Bit Wert 0xaa wird im
    // Datenbuffer des 16 Bit Bildes 0xaaaa.
    // Deshalb zunaechst Konvertierung von 8 nach 16 Bit:
    switch (sourceImage.getType()) {
      
      case BufferedImage.TYPE_USHORT_GRAY:
        // Keine Konvertierung notwendig
        break;
      
      case BufferedImage.TYPE_BYTE_GRAY:
        // Ein neues BufferedImage mit definierten Eigenschaften erzeugen:
        // - ComponentColorModel
        // - PixelInterleavedSampleModel (superclass: ComponentSampleModel)
        //   -- numBanks = 1
        //   -- numBands = 1
        //   -- bandOffset[0] = 0
        //   -- pixelStride = 1
        //   -- scanlineStride = <image width>
        // - ByteInterleavedRaster
        //   -- DataBufferByte
        bi = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), BufferedImage.TYPE_USHORT_GRAY);
        // Pixel-Daten aus dem sourceImage holen
        bytePixel = ((DataBufferByte) sourceImage.getData().getDataBuffer()).getData();
        // Byte Pixel in short Pixel umwandel
        shortPixel = new short[bytePixel.length];
        for (int idx = 0; idx < bytePixel.length; idx++) {
          shortPixel[idx] = (short) (bytePixel[idx] & 0xff);
        }
        // Aus Pixel-Daten einen DataBuffer erzeugen
        shortBuffer = new DataBufferUShort(shortPixel, shortPixel.length);
        // Ein zum BufferedImage kompatibles WritabelRaster erzeugen
        raster = Raster.createWritableRaster(bi.getSampleModel(),shortBuffer, new Point(0, 0));
        // Das neue Raster als Daten eintragen
        bi.setData(raster);
        // Mit diesem BufferedImage weiterarbeiten
        sourceImage = bi;
        break;
      
      default:
        throw new IllegalArgumentException("BufferedImage not TYPE_USHORT_GRAY or TYPE_BYTE_GRAY." + this);
        
    }

    // Die Source-Region (Rechteck) mit dem sourceImage clippen
    rect = new Rectangle(sourceImage.getWidth(), sourceImage.getHeight());
    if (dcmParam.getSourceRegion() == null) {
      // sourceRect umfasst das gesamte sourceImage
      sourceRect = rect;
    } else {
      // sourceRect umfasst die Schnittmenge von sourceImage und sourceRect
      sourceRect = rect.intersection(dcmParam.getSourceRegion());
    }
    
    // Exception, falls sourceRect leer ist
    if (sourceRect.isEmpty()) {
      throw new IllegalArgumentException("Source region is empty." + this);
    }
    
    // Ein neues BufferedImage mit definierten Eigenschaften erzeugen:
    // - ComponentColorModel
    // - PixelInterleavedSampleModel (superclass: ComponentSampleModel)
    //   -- numBanks = 1
    //   -- numBands = 1
    //   -- bandOffset[0] = 0
    //   -- pixelStride = 1
    //   -- scanlineStride = <image width>
    // - ShortInterleavedRaster
    //   -- DataBufferUShort

    destinationImage = new BufferedImage(sourceRect.width, sourceRect.height, BufferedImage.TYPE_USHORT_GRAY);

    // sourceImage unter Beruecksichtigung des sourceRect in destinationImage kopieren
    g = destinationImage.createGraphics();
    g.drawImage(sourceImage, 0, 0, destinationImage.getWidth(), destinationImage.getHeight(), sourceRect.x, sourceRect.y, sourceRect.x + sourceRect.width, sourceRect.y + sourceRect.height, null);
    g.dispose();

    // Image IE, Image Pixel Module, PS 3.3 - C.7.6.3, M
    ds.putUS(Tags.SamplesPerPixel, 1);                          // Type 1
    if (dcmParam.isMONOCHROME2()) {
      ds.putCS(Tags.PhotometricInterpretation, "MONOCHROME2");  // Type 1
    } else {
      ds.putCS(Tags.PhotometricInterpretation, "MONOCHROME1");  // Type 1
    }
    ds.putUS(Tags.Rows, destinationImage.getHeight());          // Type 1
    ds.putUS(Tags.Columns, destinationImage.getWidth());        // Type 1
    
    bitsAllocated = 16;
    ds.putUS(Tags.BitsAllocated, bitsAllocated);                // Type 1
    
    bitsStored = dcmParam.getBitsStored();
    if (bitsStored == -1) {
      bitsStored = 16;
    } else {
      if ((bitsStored < 1) || (bitsStored > bitsAllocated)) {
        bitsStored = 16;
      }
    }
    ds.putUS(Tags.BitsStored, bitsStored);                      // Type 1
    
    highBit = dcmParam.getHighBit();
    if (highBit == -1) {
      highBit = bitsStored -1;
    } else {
      if ((highBit < 0) || (highBit > (bitsStored - 1))) {
        highBit = bitsStored -1;
      }
    }
    ds.putUS(Tags.HighBit, highBit);                            // Type 1
    
    ds.putUS(Tags.PixelRepresentation, 0);                      // Type 1; 0x0=unsigned int, 0x1=2's complement
    ds.putIS(Tags.PixelAspectRatio, 1);                         // Type 1C, if vertical/horizontal != 1

    dataBuf = ((DataBufferUShort) destinationImage.getRaster().getDataBuffer()).getData();
    byteBuf = ByteBuffer.allocate(dataBuf.length * 2);
    min = 65535;
    max = 0;
    dataBufIndex = 0;
    while (dataBufIndex < dataBuf.length) {
      value = dataBuf[dataBufIndex];
      intValue = ((int) value) & 0xffff;
      if (intValue > max) max = intValue;
      if (intValue < min) min = intValue;
      byteBuf.putShort(value);
      dataBufIndex++;
    }

    // Pixeldaten in Dataset eintragen
    ds.putOW(Tags.PixelData, byteBuf);

    ds.putSS(Tags.SmallestImagePixelValue, min);                  // Type 3, if vertical/horizontal != 1
    ds.putSS(Tags.LargestImagePixelValue, max);                   // Type 3, if vertical/horizontal != 1
    
    // Image IE, Modality LUT Module, PS 3.3 - C.11.1, U
    // Wenn das Dataset schon Intercept/Slope enthaelt nicht ueberschreiben
    if (!ds.contains(Tags.RescaleIntercept)) {
      ds.putDS(Tags.RescaleIntercept, "0");                       // Type 1C; ModalityLUTSequence is not present
      ds.putDS(Tags.RescaleSlope, "1");                           // Type 1C; ModalityLUTSequence is not present
      ds.putLO(Tags.RescaleType, "PIXELVALUE");                   // Type 1C; ModalityLUTSequence is not present; arbitrary text
    }
    
    // Image IE, VOI LUT Module, PS 3.3 - C.11.2, U
    // Wenn das Dataset schon Center/Width enthaelt nicht ueberschreiben
    if (!ds.contains(Tags.WindowCenter)) {
      String[] wc = {Integer.toString((max + min) / 2)};
      ds.putDS(Tags.WindowCenter, wc);                            // Type 3
      String[] ww = {Integer.toString((max - min) / 2)};
      ds.putDS(Tags.WindowWidth, ww);                             // Type 1C; WindowCenter is present
      String[] we = {"DcmImageWriter"};
      ds.putLO(Tags.WindowCenterWidthExplanation, we);            // Type 3; arbitrary text
    }

    // Dataset schreiben
    ds.writeDataset((ImageOutputStream) output, DcmEncodeParam.valueOf(UIDs.ImplicitVRLittleEndian));

  }
  
  
  /**
   * Write a PALETTE COLOR image.
   * @param sourceImage  the BufferedImage of type TYPE_INT_RGB to write.
   * @param dcmParam  the DcmImageWriteParam to use for writing.
   * @param ds the Dataset as given by the IIOMetadata.
   * @throws IOException if an error occurs during writing.
   */
  private void writePALETTE(BufferedImage sourceImage, DcmImageWriteParam dcmParam, Dataset ds) throws IOException, IllegalArgumentException {
    ByteBuffer      byteBuf;
    byte[]          dataBuf;
    int             dataBufIndex;
    BufferedImage   destinationImage;
    Graphics        g;
    Rectangle       rect;
    Rectangle       sourceRect;
    int             value;
    ByteBuffer      pDescriptor;
    byte[]          rPalette;
    byte[]          gPalette;
    byte[]          bPalette;
    ByteBuffer      rByteBuffer;
    ByteBuffer      gByteBuffer;
    ByteBuffer      bByteBuffer;
    
    // Die Source-Region (Rechteck) mit dem sourceImage clippen
    rect = new Rectangle(sourceImage.getWidth(), sourceImage.getHeight());
    if (dcmParam.getSourceRegion() == null) {
      // sourceRect umfasst das gesamte sourceImage
      sourceRect = rect;
    } else {
      // sourceRect umfasst die Schnittmenge von sourceImage und sourceRect
      sourceRect = rect.intersection(dcmParam.getSourceRegion());
    }
    // Exception, falls sourceRect leer ist
    if (sourceRect.isEmpty()) {
      throw new IllegalArgumentException("Source region is empty." + this);
    }

    // Ein neues BufferedImage mit definierten Eigenschaften erzeugen:
    // - PixelInterleavedSampleModel (superclass: ComponentSampleModel)
    //   -- numBanks = 1
    //   -- numBands = 1
    //   -- bandOffset[0] = 0
    //   -- pixelStride = 1
    //   -- scanlineStride = <image width>
    // - IndexColorModel
    // - ByteInterleavedRaster
    //   -- DataBufferByte
    destinationImage = new BufferedImage(sourceRect.width, sourceRect.height, BufferedImage.TYPE_BYTE_INDEXED);

    // sourceImage unter Beruecksichtigung des sourceRect in destinationImage kopieren
    g = destinationImage.createGraphics();
    g.drawImage(sourceImage, 0, 0, destinationImage.getWidth(), destinationImage.getHeight(), sourceRect.x, sourceRect.y, sourceRect.x + sourceRect.width, sourceRect.y + sourceRect.height, null);
    g.dispose();

    // Image IE, Image Pixel Module, PS 3.3 - C.7.6.3, M
    ds.putUS(Tags.SamplesPerPixel,1);                                     // Type 1
    ds.putCS(Tags.PhotometricInterpretation, "PALETTE COLOR");            // Type 1
    ds.putUS(Tags.Rows, destinationImage.getHeight());                    // Type 1
    ds.putUS(Tags.Columns, destinationImage.getWidth());                  // Type 1
    ds.putUS(Tags.BitsAllocated, 8);                                      // Type 1
    ds.putUS(Tags.BitsStored, 8);                                         // Type 1
    ds.putUS(Tags.HighBit, 7);                                            // Type 1
    ds.putUS(Tags.PixelRepresentation, 0);                                // Type 1; 0x0=unsigned int, 0x1=2's complement
    ds.putIS(Tags.PixelAspectRatio, 1);                                   // Type 1C, if vertical/horizontal != 1

    // Paletten Deskriptoren schreiben
    pDescriptor = ByteBuffer.allocate(3 * 2);
    pDescriptor.putShort((short) 256);                                    // number of entries
    pDescriptor.putShort((short) 0);                                      // first stored pixel value mapped
    pDescriptor.putShort((short) 8);                                      // number of bits
    ds.putXX(Tags.RedPaletteColorLUTDescriptor, VRs.US, pDescriptor);     // Type 1C; US/US or SS/US
    ds.putXX(Tags.GreenPaletteColorLUTDescriptor, VRs.US, pDescriptor);   // Type 1C; US/US or SS/US
    ds.putXX(Tags.BluePaletteColorLUTDescriptor, VRs.US, pDescriptor);    // Type 1C; US/US or SS/US

    // Paletten schreiben
    rPalette = new byte[256];
    gPalette = new byte[256];
    bPalette = new byte[256];
    ((IndexColorModel) destinationImage.getColorModel()).getReds(rPalette);
    ((IndexColorModel) destinationImage.getColorModel()).getGreens(gPalette);
    ((IndexColorModel) destinationImage.getColorModel()).getBlues(bPalette);
    
    rByteBuffer = ByteBuffer.allocate(256 * 2);
    gByteBuffer = ByteBuffer.allocate(256 * 2);
    bByteBuffer = ByteBuffer.allocate(256 * 2);
    
    for (int idx = 0; idx < 256; idx++) {
      rByteBuffer.putShort((short) (rPalette[idx] & 0xff));
      gByteBuffer.putShort((short) (gPalette[idx] & 0xff));
      bByteBuffer.putShort((short) (bPalette[idx] & 0xff));
    }

    ds.putXX(Tags.RedPaletteColorLUTData, VRs.US, rByteBuffer);          // Type 1C; US or SS or OW
    ds.putXX(Tags.GreenPaletteColorLUTData, VRs.US, gByteBuffer);        // Type 1C; US or SS or OW
    ds.putXX(Tags.BluePaletteColorLUTData, VRs.US, bByteBuffer);         // Type 1C; US or SS or OW

    // Pixeldaten extrahieren
    dataBuf = ((DataBufferByte) destinationImage.getRaster().getDataBuffer()).getData();
    byteBuf = ByteBuffer.allocate(dataBuf.length);
    dataBufIndex = 0;
    while (dataBufIndex < dataBuf.length) {
      byteBuf.put(dataBuf[dataBufIndex]);
      dataBufIndex++;
    }

    // Pixeldaten in Dataset eintragen
    ds.putOB(Tags.PixelData, byteBuf);                                  // Type 1; or ds.putOW

    // Dataset schreiben
    ds.writeDataset((ImageOutputStream) output, DcmEncodeParam.valueOf(UIDs.ImplicitVRLittleEndian));

  }  
}
