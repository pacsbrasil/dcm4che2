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
 * Devishree V
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
package in.raster.mayam.delegates;

import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.form.LayeredCanvas;
import in.raster.mayam.form.MainScreen;
import in.raster.mayam.models.Instance;
import in.raster.mayam.models.Series;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.*;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.imageio.plugins.DcmMetadata;

/**
 *
 * @author BabuHussain
 * @version 0.5
 *
 */
public class JpegConvertorDelegate {

    public JpegConvertorDelegate() {
    }

    /**
     * This routine used to convert the dicom study to jpeg file based on the
     * studyUID specified as parameter.
     *
     * @param studyIUID
     * @param outputPath
     */
    public void studyLevelConvertor(String studyIUID, String outputPath) {
        String patientName = ((LayeredCanvas) ApplicationContext.selectedPanel.getComponent(0)).imgpanel.getTextOverlayParam().getPatientName();
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
                if (img.isMultiframe()) {
                    if (img.getCurrentFrameNum() == 0) {
                        studyExportAsJpeg(img, patientNameFile.getAbsolutePath());
                    }
                } else {
                    studyExportAsJpeg(img, patientNameFile.getAbsolutePath());
                }
            }
        }
    }

    /**
     * This routine used to convert the dicom series as jpeg file based on the
     * seriesUID specified as parameter
     *
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
                    if (img.isMultiframe()) {
                        if (img.getCurrentFrameNum() == 0) {
                            seriesExportAsJpeg(img, outputPath, cm);

                        }
                    } else {
                        seriesExportAsJpeg(img, outputPath, cm);
                    }
                }
            }
        }
    }

    public void seriesLevelConvertor(String studyIUID, String seriesUID, boolean multiframe, String instanceUID, String outputPath, ColorModel cm) {
        ArrayList<Series> seriesList = ApplicationContext.databaseRef.getSeriesList(studyIUID);
        Iterator<Series> seriesItr = seriesList.iterator();
        while (seriesItr.hasNext()) {
            Series series = seriesItr.next();
            if (multiframe) {
                if (series.isMultiframe() && series.getSeriesInstanceUID().equalsIgnoreCase(seriesUID) && series.getInstanceUID().equalsIgnoreCase(instanceUID)) {        //if multiframe image then instance uid also to be checked.
                    Iterator<Instance> imgitr = series.getImageList().iterator();
                    while (imgitr.hasNext()) {
                        Instance img = imgitr.next();
                        seriesExportAsJpeg(img.getFilepath(), outputPath, series.isMultiframe(), cm);
                    }
                }
            } else if (ApplicationContext.layeredCanvas.imgpanel.isEncapsulatedDocument()) {
                int i = 1;
                ArrayList<BufferedImage> pdfArray = ApplicationContext.layeredCanvas.imgpanel.createPDFArray();
                for (BufferedImage b : pdfArray) {
                    instanceExportAsJpeg(outputPath + File.separator + i, b);
                    i++;
                }
            } else {
                if (!series.isMultiframe() && series.getSeriesInstanceUID().equalsIgnoreCase(seriesUID)) {        //if multiframe image then instance uid also to be checked.
                    Iterator<Instance> imgitr = series.getImageList().iterator();
                    while (imgitr.hasNext()) {
                        Instance img = imgitr.next();
                        seriesExportAsJpeg(img.getFilepath(), outputPath, series.isMultiframe(), cm);
                    }
                }
            }
        }
    }

    private void seriesExportAsJpeg(String inputFilePath, String outputPath, boolean multiframe, ColorModel cm) {
        OutputStream output = null;
        try {
            File inputDicomFile = new File(inputFilePath);
            int dotPos = inputDicomFile.getName().lastIndexOf(".");
            String outputFileName = inputDicomFile.getName();
            try {
                String extension = inputDicomFile.getName().substring(dotPos);
                if (extension.equalsIgnoreCase(".dcm")) {
                    outputFileName = inputDicomFile.getName().replace(extension, "");
                }
            } catch (StringIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
            seriesExportAsJpegProcess(multiframe, inputDicomFile, outputPath, outputFileName, cm, output);
        } catch (IOException ex) {
            Logger.getLogger(MainScreen.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(MainScreen.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(MainScreen.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void seriesExportAsJpegProcess(boolean multiframe, File inputDicomFile, String outputPath, String outputFileName, ColorModel cm, OutputStream output) throws IOException {
        File outputJpegFile = null;
        ImageInputStream iis = ImageIO.createImageInputStream(inputDicomFile);
        BufferedImage jpegImage = null;
        Iterator<ImageReader> iterator = ImageIO.getImageReadersByFormatName("DICOM");
        ImageReader reader = (ImageReader) iterator.next();
        reader.setInput(iis, false);
        int nFrames = 1;
        if (multiframe) {
            nFrames = reader.getNumImages(true);
        }
        for (int i = 0; i < nFrames; i++) {
            outputJpegFile = new File(outputPath, outputFileName + i + ".jpg");
            jpegImage = reader.read(i);
            BufferedImage temp = jpegImage;
            if (cm != null) {
                temp = new BufferedImage(cm, jpegImage.getRaster(), false, null);
            }
            exportStudy(outputJpegFile, temp);
        }
    }

    /**
     * This routine used to convert the dicom instance as jpeg file based on the
     * buffered image specified as parameter.
     *
     * @param outputPath
     * @param bimg
     */
    public void instanceConvertor(String outputPath, BufferedImage bimg) {
        instanceExportAsJpeg(outputPath, bimg);
    }

    /**
     * This routine perform the core process for converting jpeg using
     * input,output file path specified as parameter.
     *
     * @param inputFilePath
     * @param outputPath
     */
    private void studyExportAsJpeg(Instance instance, String outputPath) {
        OutputStream output = null;
        try {
            File inputDicomFile = new File(instance.getFilepath());
            int dotPos = inputDicomFile.getName().lastIndexOf(".");
            String outputFileName = inputDicomFile.getName();
            try {
                String extension = inputDicomFile.getName().substring(dotPos);
                if (extension.equalsIgnoreCase(".dcm")) {
                    outputFileName = inputDicomFile.getName().replace(extension, "");
                }
            } catch (StringIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
            studyExportAsJpegProcess(instance, outputPath, outputFileName, output, inputDicomFile);
        } catch (Exception ex) {
            Logger.getLogger(MainScreen.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (Exception ex) {
                Logger.getLogger(MainScreen.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void studyExportAsJpegProcess(Instance instance, String outputPath, String outputFileName, OutputStream output, File inputDicomFile) throws IOException {
        File outputJpegFile = new File(outputPath, outputFileName + ".jpg");
        ImageInputStream iis = ImageIO.createImageInputStream(inputDicomFile);
        BufferedImage jpegImage = null;
        Iterator<ImageReader> iterator = ImageIO.getImageReadersByFormatName("DICOM");
        ImageReader reader = (ImageReader) iterator.next();
        reader.setInput(iis, false);
        Dataset dataset = ((DcmMetadata) reader.getStreamMetadata()).getDataset();
        int nFrames = 1;
        if (instance.isMultiframe()) {
            nFrames = reader.getNumImages(true);
        }
        for (int i = 0; i < nFrames; i++) {
            outputJpegFile = new File(outputPath, outputFileName + i + ".jpg");
            if (dataset.getString(Tags.SOPClassUID) != null && dataset.getString(Tags.SOPClassUID).equalsIgnoreCase("1.2.840.10008.5.1.4.1.1.104.1")) {
                //This condition used to check whether the input file is a encapsulated pdf dicom file
                //following lines of codes are used to create the jpeg image from the encapsulated pdf
                int k = 1;
                EncapsulatedPdfToJpeg encapsulatedPdfToJpeg = new EncapsulatedPdfToJpeg();
                encapsulatedPdfToJpeg.readDicom(inputDicomFile);
                ArrayList<BufferedImage> pdfArray = encapsulatedPdfToJpeg.createPDFArray();
                for (BufferedImage b : pdfArray) {
                    instanceExportAsJpeg(outputPath + File.separator + k, b);
                    k++;
                }
            } else {
                jpegImage = reader.read(i);
                BufferedImage temp = jpegImage;
                exportStudy(outputJpegFile, jpegImage);
            }
        }
    }

    public void exportStudy(File outputJpegFile, BufferedImage jpegImage) {
        OutputStream outputoutput = null;
        try {
            outputoutput = new BufferedOutputStream(new FileOutputStream(outputJpegFile));
            ImageIO.write(jpegImage, "jpeg", outputJpegFile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JpegConvertorDelegate.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(JpegConvertorDelegate.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                outputoutput.close();
            } catch (IOException ex) {
                Logger.getLogger(JpegConvertorDelegate.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * This routine performs the core process for converting jpeg using input,
     * output file specified as parameter. It will generate the output jpeg file
     * with colormodel specified as arguments.
     *
     * @param inputFilePath
     * @param outputPath
     * @param cm
     */
    private void seriesExportAsJpeg(Instance instance, String outputPath, ColorModel cm) {
        OutputStream output = null;
        try {
            File inputDicomFile = new File(instance.getFilepath());
            int dotPos = inputDicomFile.getName().lastIndexOf(".");
            String outputFileName = inputDicomFile.getName();
            try {
                String extension = inputDicomFile.getName().substring(dotPos);
                if (extension.equalsIgnoreCase(".dcm")) {
                    outputFileName = inputDicomFile.getName().replace(extension, "");
                }
            } catch (StringIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
            seriesExportAsJpegProcess(instance, outputPath, cm, inputDicomFile, output, outputFileName);
        } catch (IOException ex) {
            Logger.getLogger(MainScreen.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(MainScreen.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(MainScreen.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void seriesExportAsJpegProcess(Instance instance, String outputPath, ColorModel cm, File inputDicomFile, OutputStream output, String outputFileName) throws IOException {
        File outputJpegFile = null;
        ImageInputStream iis = ImageIO.createImageInputStream(inputDicomFile);
        BufferedImage jpegImage = null;
        Iterator<ImageReader> iterator = ImageIO.getImageReadersByFormatName("DICOM");
        ImageReader reader = (ImageReader) iterator.next();
        reader.setInput(iis, false);
        jpegImage = reader.read(instance.getCurrentFrameNum());

        int nFrames = 1;
        if (instance.isMultiframe()) {
            nFrames = reader.getNumImages(true);

        }
        for (int i = 0; i < nFrames; i++) {
            outputJpegFile = new File(outputPath, outputFileName + i + ".jpg");
            jpegImage = reader.read(i);
            BufferedImage temp = jpegImage;
            if (cm != null) {
                temp = new BufferedImage(cm, jpegImage.getRaster(), false, null);
            }
            exportStudy(outputJpegFile, temp);
        }
    }

    /**
     * This routine performs the core process for converting jpeg file using the
     * output filepath,buffered image specified as parameter.
     *
     * @param outputPath
     * @param bimg
     */
    private void instanceExportAsJpeg(String outputPath, BufferedImage bimg) {
        OutputStream output = null;
        try {
            File outputJpegFile = new File(outputPath + ".jpg");
            BufferedImage jpegImage = bimg;
            output = new BufferedOutputStream(new FileOutputStream(outputJpegFile));
            ImageIO.write(jpegImage, "jpeg", outputJpegFile);
        } catch (IOException ex) {
            Logger.getLogger(MainScreen.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(MainScreen.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    public void exportInstance(File outputJpegFile, BufferedImage jpegImage) {
        FileImageOutputStream ios = null;
        ImageWriter writer = null;
        try {
            Iterator it = ImageIO.getImageWritersByFormatName("jpeg");
            writer = (ImageWriter) it.next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(1);
            ios = new FileImageOutputStream(outputJpegFile);
            writer.setOutput(ios);
            IIOImage iioImage = new IIOImage(jpegImage, null, null);
            writer.write(null, iioImage, param);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ios != null) {
                try {
                    ios.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    // ignore
                }
                if (writer != null) {
                    try {
                        writer.dispose();
                    } catch (Exception e) {
                        e.printStackTrace();
                        // ignore
                    }
                }
            }
        }
    }
}
