

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
*
* The Initial Developer of the Original Code is
* Raster Images
* Portions created by the Initial Developer are Copyright (C) 2009-2010
* the Initial Developer. All Rights Reserved.
*
* Contributor(s):
* Babu Hussain A
* Meer Asgar Hussain B
* Prakash J
* Suresh V
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
package in.raster.mayam.delegate;


import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.form.MainScreen;
import in.raster.mayam.model.Instance;
import in.raster.mayam.model.Series;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;

/**
 *
 * @author  BabuHussain
 * @version 0.5
 *
 */
public class JpegConvertorDelegate {

    public JpegConvertorDelegate() {
    }
    /**
     * This routine used to convert the dicom study to jpeg file based on the studyUID specified as parameter.
     * @param studyIUID
     * @param outputPath
     */
    public void studyLevelConvertor(String studyIUID, String outputPath) {       
        String patientName = ApplicationContext.databaseRef.getPatientNameBasedonStudyUID(studyIUID);
        File patientNameFile = new File(outputPath, patientName);
        if (!patientNameFile.exists()) {
            patientNameFile.mkdirs();
        }
        ArrayList<Series> seriesList = ApplicationContext.databaseRef.getSeriesList(studyIUID);
        Iterator<Series> seriesItr = seriesList.iterator();
        while (seriesItr.hasNext()) {
            Series series = seriesItr.next();
            Iterator<Instance> imgitr = series.getImageList().iterator();
            while (imgitr.hasNext()) {
                Instance img = imgitr.next();         
                studyExportAsJpeg(img.getFilepath(), patientNameFile.getAbsolutePath());
            }
        }
    }
    /**
     * This routine used to convert the dicom series as jpeg file based on the seriesUID specified as parameter
     * @param studyIUID
     * @param seriesUID
     * @param outputPath
     * @param cm
     */
    public void seriesLevelConvertor(String studyIUID, String seriesUID, String outputPath, ColorModel cm) {
        ArrayList<Series> seriesList = ApplicationContext.databaseRef.getSeriesList(studyIUID);
        Iterator<Series> seriesItr = seriesList.iterator();
        while (seriesItr.hasNext()) {
            Series series = seriesItr.next();
            if (series.getSeriesInstanceUID().equalsIgnoreCase(seriesUID)) {
                Iterator<Instance> imgitr = series.getImageList().iterator();
                while (imgitr.hasNext()) {
                    Instance img = imgitr.next();
                    seriesExportAsJpeg(img.getFilepath(), outputPath, cm);                
                }
            }
        }
    }
    /**
     * This routine used to convert the dicom instance as jpeg file based on the buffered image specified as parameter.
     * @param outputPath
     * @param bimg
     */
    public void instanceConvertor(String outputPath, BufferedImage bimg) {
        instanceExportAsJpeg(outputPath, bimg);
    }
    /**
     * This routine perform the core process for converting jpeg using input,output file path specified as parameter.
     * @param inputFilePath
     * @param outputPath
     */
    private void studyExportAsJpeg(String inputFilePath, String outputPath) {
        OutputStream output = null;
        try {
            File inputDicomFile = new File(inputFilePath);
            int dotPos = inputDicomFile.getName().lastIndexOf(".");
            String outputFileName=inputDicomFile.getName();
            try
            {
            String extension = inputDicomFile.getName().substring(dotPos);       
            if(extension.equalsIgnoreCase(".dcm"))
            {
                outputFileName=inputDicomFile.getName().replace(extension, "");               
            }
            }catch(StringIndexOutOfBoundsException e)
            {
            }            
            File outputJpegFile = new File(outputPath, outputFileName+".jpg");
            ImageInputStream iis = ImageIO.createImageInputStream(inputDicomFile);
            BufferedImage jpegImage = null;
            Iterator<ImageReader> iterator = ImageIO.getImageReadersByFormatName("DICOM");
            ImageReader reader = (ImageReader) iterator.next();
            reader.setInput(iis, false);
            jpegImage = reader.read(0);
            if(System.getProperty("os.name").startsWith("Mac"))
            {
            output = new BufferedOutputStream(new FileOutputStream(outputJpegFile));
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(output);
            encoder.encode(jpegImage);
            output.close();
            }else
            {
            //For linux and windows           
            Iterator it = ImageIO.getImageWritersByFormatName("jpeg");
            ImageWriter writer = (ImageWriter)it.next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(1);           
            FileImageOutputStream ios=new FileImageOutputStream(outputJpegFile);
            writer.setOutput(ios);            
            IIOImage iioImage = new IIOImage(jpegImage, null, null);
            writer.write(null,iioImage,param);
            writer.dispose();
            ios.close();
            }        

        } catch (IOException ex) {
            Logger.getLogger(MainScreen.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(MainScreen.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {               
                if(output!=null)
                output.close();
            } catch (Exception ex) {
                Logger.getLogger(MainScreen.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    /**
     * This routine performs the core process for converting jpeg using input, output file specified as parameter.
     * It will generate the output jpeg file with colormodel specified as arguments.
     * @param inputFilePath
     * @param outputPath
     * @param cm
     */
    private void seriesExportAsJpeg(String inputFilePath, String outputPath, ColorModel cm) {
        OutputStream output = null;
        try {
            File inputDicomFile = new File(inputFilePath);
            int dotPos = inputDicomFile.getName().lastIndexOf(".");
            String outputFileName=inputDicomFile.getName();
            try
            {
            String extension = inputDicomFile.getName().substring(dotPos);
            if(extension.equalsIgnoreCase(".dcm"))
            {
                outputFileName=inputDicomFile.getName().replace(extension, "");
            }
            }catch(StringIndexOutOfBoundsException e)
            {
            }
            File outputJpegFile = new File(outputPath, outputFileName+".jpg");
            ImageInputStream iis = ImageIO.createImageInputStream(inputDicomFile);
            BufferedImage jpegImage = null;
            Iterator<ImageReader> iterator = ImageIO.getImageReadersByFormatName("DICOM");
            ImageReader reader = (ImageReader) iterator.next();
            reader.setInput(iis, false);
            jpegImage = reader.read(0);
            BufferedImage temp = new BufferedImage(cm, jpegImage.getRaster(), false, null);
           if(System.getProperty("os.name").startsWith("Mac"))
            {
            output = new BufferedOutputStream(new FileOutputStream(outputJpegFile));
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(output);
            encoder.encode(temp);
            output.close();
            }else
            {
            //For linux and windows
            Iterator it = ImageIO.getImageWritersByFormatName("jpeg");
            ImageWriter writer = (ImageWriter)it.next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(1);
            FileImageOutputStream ios=new FileImageOutputStream(outputJpegFile);
            writer.setOutput(ios);
            IIOImage iioImage = new IIOImage(temp, null, null);
            writer.write(null,iioImage,param);
            writer.dispose();
            ios.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(MainScreen.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(MainScreen.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                 if(output!=null)
                output.close();
            } catch (IOException ex) {
                Logger.getLogger(MainScreen.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    /**
     * This routine performs the core process for converting jpeg file using the output filepath,buffered image specified as parameter.
     *
     * @param outputPath
     * @param bimg
     */
    private void instanceExportAsJpeg(String outputPath, BufferedImage bimg) {
        OutputStream output = null;
        try {
            File outputJpegFile = new File(outputPath+".jpg");
            BufferedImage jpegImage = bimg;
             if(System.getProperty("os.name").startsWith("Mac"))
            {
            output = new BufferedOutputStream(new FileOutputStream(outputJpegFile));
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(output);
            encoder.encode(jpegImage);
            output.close();
            }else
            {
            //For linux and windows
            Iterator it = ImageIO.getImageWritersByFormatName("jpeg");
            ImageWriter writer = (ImageWriter)it.next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(1);
            FileImageOutputStream ios=new FileImageOutputStream(outputJpegFile);
            writer.setOutput(ios);
            IIOImage iioImage = new IIOImage(jpegImage, null, null);
            writer.write(null,iioImage,param);
            writer.dispose();
            ios.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(MainScreen.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(MainScreen.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                 if(output!=null)
                output.close();
            } catch (IOException ex) {
                Logger.getLogger(MainScreen.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}
