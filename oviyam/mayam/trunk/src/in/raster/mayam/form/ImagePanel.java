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
package in.raster.mayam.form;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.delegates.ImageBuffer;
import in.raster.mayam.delegates.ImageGenerator;
import in.raster.mayam.delegates.ImageOrientation;
import in.raster.mayam.delegates.LocalizerDelegate;
import in.raster.mayam.delegates.SeriesChooserDelegate;
import in.raster.mayam.listeners.PopupMenuListener;
import in.raster.mayam.models.*;
import in.raster.mayam.param.TextOverlayParam;
import in.raster.mayam.models.SeriesAnnotations;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.image.ColorModelFactory;
import org.dcm4che.image.ColorModelParam;
import org.dcm4che.imageio.plugins.DcmMetadata;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.image.OverlayUtils;
import org.dcm4che2.io.DicomInputStream;

/**
 *
 * @author BabuHussain
 * @version 0.5
 *
 */
public class ImagePanel extends javax.swing.JPanel implements MouseWheelListener, MouseMotionListener, MouseListener {

    private Canvas canvas;
    //Image manipulation Flags
    public boolean isRotate = false, flipHorizontalFlag = false, flipVerticalFlag = false;
    private boolean invertFlag = false, scaleFlag = false, newBufferedImage = false;
    private static boolean probeFlag;
    private double scaleFactor = 1;
    public int rotateRightAngle = 0;
    private int rotateLeftAngle = 0;
    public static String tool = "windowing";
    //Windowing, Hu related variables
    private int windowLevel, windowWidth, WC, WW;
    private String rescaleSlope, rescaleIntercept;
    private double pixelSpacingX, pixelSpacingY;
    //Unique id variables
    private String studyUID, seriesUID, instanceUID, modality, studyDesc;
    //ImageIO variables
    private BufferedImage currentbufferedimage, image;
    ImageReader reader = null;
    private Dataset dataset;
    //Mouse pointer variables
    private int mouseLocX1, mouseLocX2, mouseLocY1, mouseLocY2;
    //ColorModel variables
    private ColorModelParam cmParam = null;
    private static final ColorModelFactory cmFactory = ColorModelFactory.getInstance();
    private ColorModel cm = null;
    private int windowingMultiplier = 1, pixelValue;
    private int[] pixelValueArray;
    private String hu = "", dicomFileUrl;
    //Multiframe image related variables
    private int nFrames = 0, currentFrame = 0, currentInstanceNo = 0, totalInstance;
    private boolean multiframe = false;
    //TextOverlay 
    private TextOverlayParam textOverlayParam;
    private float floatAspectRatio;
    //Scout Param
    ScoutLineInfoModel currentScoutDetails;
    private String orientationLabel = "";
    private boolean isLocalizer = false, isEncapsulatedDocument = false;
    private static boolean displayScout = false;
    private int scoutLine1X1, scoutLine1Y1, scoutLine1X2, scoutLine1Y2, scoutLine2X1, scoutLine2Y1, scoutLine2X2, scoutLine2Y2;
    private int boundaryLine1X1, boundaryLine1Y1, boundaryLine1X2, boundaryLine1Y2, boundaryLine2X1, boundaryLine2Y1, boundaryLine2X2, boundaryLine2Y2;
    double slope1, slope2;
    private int thumbWidth = 512, thumbHeight = 512, maxHeight = 512, maxWidth = 512;
    private double thumbRatio, currentScaleFactor = 1;
    private int startX = 0, startY = 0;
    private int axis1LeftX, axis1LeftY, axis1RightX, axis1RightY, axis2LeftX, axis2LeftY, axis2RightX, axis2RightY, axisLeftX, axisLeftY, axisRightX, axisRightY;
    private String sliceThickness;
    public static boolean synchornizeTiles = false;
    private PDFFile curFile = null;
    private int curpage = -1;
    public int imgHeight = 0, imgWidth = 0, layoutRows = 1, layoutColumns = 1;
    private String instanceUidIfMultiframe = null;
    long timeDelay = 0; //To synchronize the mouse scroll amount
    public boolean borderAlreadyPresent = false; //To identify the yellow scout borders
    ArrayList<String> instanceUidList;
    String fileLocation;
    SeriesAnnotations currentSeriesAnnotation = null;
    ArrayList<String> fileUrlsifLink = null;
    public boolean isLink = false;
    ImageBuffer imgBuffer = null;
    public ImageGenerator imageUpdator = null;

    /**
     * Constructs the image panel by passing file url and outer canvas
     *
     * @param dicomFileUrl
     * @param canvas
     */
    public ImagePanel(File dicomFile, Canvas canvas) {
        this.dicomFileUrl = dicomFile.getAbsolutePath();
        this.canvas = canvas;
        readDicomFile(dicomFile);
        retrieveTagInfo();
        initComponents();
        addlisteners();
        retrieveInstanceInformation();
        retrieveScoutParam();
        setTotalInstance();
        retriveTextOverlayParam();
        calculateHeightAndWidth();
        centerImage();
        instanceUidList = ApplicationContext.databaseRef.getInstanceUidList(studyUID, seriesUID);
        getFilePathsifLink();
    }

    public ImagePanel(Canvas canvas) {
        this.canvas = canvas;
        initComponents();
        addlisteners();
    }

    /**
     * This routine used to retrieve the text overlay related information from
     * the dataset
     */
    private void retriveTextOverlayParam() {
        textOverlayParam = new TextOverlayParam();
        textOverlayParam.setPatientName(dataset.getString(Tags.PatientName));
        textOverlayParam.setPatientID(dataset.getString(Tags.PatientID));
        textOverlayParam.setSex(dataset.getString(Tags.PatientSex));
        textOverlayParam.setStudyDate(dataset.getString(Tags.StudyDate));
        textOverlayParam.setStudyDescription(dataset.getString(Tags.StudyDescription) != null ? dataset.getString(Tags.StudyDescription) : "");
        textOverlayParam.setSeriesDescription(dataset.getString(Tags.SeriesDescription) != null ? dataset.getString(Tags.SeriesDescription) : "");
        textOverlayParam.setBodyPartExamined(dataset.getString(Tags.BodyPartExamined));
        textOverlayParam.setSlicePosition(currentScoutDetails.getSliceLocation());
        textOverlayParam.setPatientPosition(dataset.getString(Tags.PatientPosition));
        textOverlayParam.setInstitutionName(dataset.getString(Tags.InstitutionName));
        textOverlayParam.setWindowLevel(dataset.getString(Tags.WindowCenter) != null ? dataset.getString(Tags.WindowCenter) : "");
        textOverlayParam.setWindowWidth(dataset.getString(Tags.WindowWidth) != null ? dataset.getString(Tags.WindowWidth) : "");
        textOverlayParam.setCurrentInstance(currentInstanceNo);
        textOverlayParam.setTotalInstance(String.valueOf(totalInstance));
        if (!multiframe) {
            textOverlayParam.setIsMultiframe(false);
        } else {
            textOverlayParam.setIsMultiframe(true);
        }
    }

    /**
     * This routine used to retrieve some other tag information from the dataset
     */
    private void retrieveTagInfo() {
        try {
            studyUID = dataset.getString(Tags.StudyInstanceUID);
            seriesUID = dataset.getString(Tags.SeriesInstanceUID);
            instanceUID = dataset.getString(Tags.SOPInstanceUID);
            modality = dataset.getString(Tags.Modality);
            studyDesc = dataset.getString(Tags.StudyDescription);
            rescaleSlope = dataset.getString(Tags.RescaleSlope);
            rescaleIntercept = dataset.getString(Tags.RescaleIntercept);
            sliceThickness = (dataset.getString(Tags.SliceThickness) != null) ? dataset.getString(Tags.SliceThickness) : "";
            try {
                imgHeight = dataset.getInteger(Tags.Rows);
                imgWidth = dataset.getInteger(Tags.Columns);
            } catch (Exception e) {
                imgHeight = 512;
                imgWidth = 512;
            }
        } catch (NullPointerException e) {
        }
    }

    private void retrieveScoutParam() {
        currentScoutDetails = ApplicationContext.databaseRef.getScoutLineDetails(studyUID, seriesUID, instanceUID);
        checkIsLocalizer();
        findOrientation();
    }

    /**
     * This routine used to retrieve the instance related information
     */
    private void retrieveInstanceInformation() {
        String inverted = dataset.getString(Tags.PhotometricInterpretation, null);
        if ("MONOCHROME1".equals(inverted) || "MONOCHROME2".equals(inverted)) {
            cmParam = cmFactory.makeParam(dataset);
            int bits = dataset.getInt(Tags.BitsStored, 8);
            int size = 1 << bits;
            int signed = dataset.getInt(Tags.PixelRepresentation, 0);
            int min = dataset.getInt(Tags.SmallestImagePixelValue,
                    signed == 0 ? 0 : -(size >> 1));
            int max = dataset.getInt(Tags.LargestImagePixelValue,
                    signed == 0 ? size - 1 : (size >> 1) - 1);
            int c = (int) cmParam.toMeasureValue((min + max) >> 1);
            int cMin = (int) cmParam.toMeasureValue(min);
            int cMax = (int) cmParam.toMeasureValue(max - 1);
            int wMax = cMax - cMin;
            int w = wMax;
            try {
                pixelSpacingY = Double.parseDouble(dataset.getString(Tags.PixelSpacing, 0));
                pixelSpacingX = Double.parseDouble(dataset.getString(Tags.PixelSpacing, 1));
            } catch (NullPointerException e) { //ignore
            }
            int nWindow = cmParam.getNumberOfWindows();
            if (nWindow > 0) {
                WC = windowLevel = c = (int) cmParam.getWindowCenter(0);
                WW = windowWidth = w = (int) cmParam.getWindowWidth(0);
            } else {
                WW = windowWidth = w = (int) Math.pow(2, bits);
                WC = windowLevel = c = (int) w / 2;
            }
        }
        windowChanged(windowLevel, windowWidth);
    }

    /**
     * This routine used to read the dicom file
     *
     * @param selFile-This the file to be read
     */
    private void readDicomFile(File selFile) {
        try {
            fileLocation = selFile.getParent();
            ImageInputStream iis = ImageIO.createImageInputStream(selFile);
            Iterator iter = ImageIO.getImageReadersByFormatName("DICOM");
            reader = (ImageReader) iter.next();
            reader.setInput(iis, false);
            DicomInputStream dis = new DicomInputStream(selFile);
            DicomObject obj = dis.readDicomObject();
            dataset = ((DcmMetadata) reader.getStreamMetadata()).getDataset();
            try {
                if (reader.getNumImages(true) > 0) {
                    currentbufferedimage = reader.read(0);
                    floatAspectRatio = reader.getAspectRatio(0);
                    String overlayData = obj.getString(Tag.OverlayData);
                    if (overlayData != null && overlayData.length() > 0) {
                        BufferedImage overlayImg = OverlayUtils.extractOverlay(obj, Tag.OverlayData, reader, "FFFFFF");
                        image = combineImages(currentbufferedimage, overlayImg);
                    } else {
                        image = currentbufferedimage;
                    }
                }
                nFrames = reader.getNumImages(true);
                if (nFrames - 1 > 0) {
                    multiframe = true;
                    totalInstance = nFrames;
                    instanceUidIfMultiframe = dataset.getString(Tags.SOPInstanceUID);
                }
                if (dataset.getString(Tags.SOPClassUID) != null && dataset.getString(Tags.SOPClassUID).equalsIgnoreCase("1.2.840.10008.5.1.4.1.1.104.1")) {
                    readDicom(selFile);
                }
                repaint();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
//            System.out.println("Exception in readDicomFile() : " + e.getMessage());
        }
    }

    public void readDicom(File file) {
        try {
            DicomInputStream din = new DicomInputStream(new File(file.getAbsolutePath()));
            DicomObject dcmObject = din.readDicomObject();
            byte[] buf = dcmObject.getBytes(Tags.EncapsulatedDocument);
            ByteBuffer byteBuffer = ByteBuffer.wrap(buf);
            openPDFByteBuffer(byteBuffer, null, null);
            isEncapsulatedDocument = true;
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void openPDFByteBuffer(ByteBuffer buf, String path, String name) {
        PDFFile newfile = null;
        try {
            newfile = new PDFFile(buf);
        } catch (IOException ioe) {
            return;
        }
        curFile = newfile;
        forceGotoPage(0);
    }
    PDFPage pg = null;

    public void forceGotoPage(int pagenum) {
        Image loadedImage = null;
        ImageIcon imageIcon = null;
        if (pagenum <= 0) {
            pagenum = 0;
        } else if (pagenum >= curFile.getNumPages()) {
            pagenum = curFile.getNumPages() - 1;
        }
        totalInstance = curFile.getNumPages();
        curpage = pagenum;
        pg = curFile.getPage(pagenum + 1);
        Rectangle rect = new Rectangle(0, 0,
                (int) pg.getBBox().getWidth(),
                (int) pg.getBBox().getHeight());

        //generate the image
        Image current = pg.getImage(
                rect.width, rect.height, //width & height
                rect, // clip rect
                null, // null for the ImageObserver
                true, // fill background with white
                true // block until drawing is done
                );
        imageIcon = new ImageIcon();
        imageIcon.setImage(current);
        loadedImage = imageIcon.getImage();
        currentbufferedimage = new BufferedImage(loadedImage.getWidth(null), loadedImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = currentbufferedimage.createGraphics();
        g2.drawImage(loadedImage, 0, 0, null);
        image = null;
    }

    public ArrayList createPDFArray() {
        ImageIcon imageIcon = null;
        ArrayList<BufferedImage> temp = new ArrayList<BufferedImage>();
        for (int pagenum = 0; pagenum < curFile.getNumPages(); pagenum++) {
            PDFPage pdfPage = curFile.getPage(pagenum + 1);
            Rectangle rect = new Rectangle(0, 0,
                    (int) pdfPage.getBBox().getWidth(),
                    (int) pdfPage.getBBox().getHeight());

            //generate the image
            Image current = pdfPage.getImage(
                    rect.width, rect.height, //width & height
                    rect, // clip rect
                    null, // null for the ImageObserver
                    true, // fill background with white
                    true // block until drawing is done
                    );
            imageIcon = new ImageIcon();
            imageIcon.setImage(current);
            Image tempImage = imageIcon.getImage();
            BufferedImage tempBufferedImage = new BufferedImage(tempImage.getWidth(null), tempImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = tempBufferedImage.createGraphics();
            g2.drawImage(tempImage, 0, 0, null);
            temp.add(tempBufferedImage);
        }
        return temp;
    }

    private void calculateResolutionForPdfDicom(double imageWidthParam, double imageHeightParam) {
        thumbHeight = 512;
        thumbWidth = 512;
        thumbRatio = thumbWidth / thumbHeight;
        double imageWidth = imageWidthParam;
        double imageHeight = imageHeightParam;
        double imageRatio = (double) imageWidth / (double) imageHeight;
        if (thumbRatio < imageRatio) {
            thumbHeight = (int) Math.round((thumbWidth + 0.00f) / imageRatio);
        } else {
            thumbWidth = (int) Math.round((thumbHeight + 0.00f) * imageRatio);
        }
        startX = (maxWidth - thumbWidth) / 2;
        startY = (maxHeight - thumbHeight) / 2;
    }

    public void showNextFrame() {
        storeMultiframeAnnotation();
        currentFrame++;
        if (currentFrame == nFrames) {
            currentFrame = 0;
        }
        currentInstanceNo = currentFrame;
        totalInstance = nFrames;
        displayFrames();
    }

    public void showPreviousFrame() {
        storeMultiframeAnnotation();
        if (currentFrame == 0) {
            currentFrame = nFrames;
        }
        currentFrame--;
        currentInstanceNo = currentFrame;
        totalInstance = nFrames;
        displayFrames();
    }

    /**
     * This routine used to apply the filter to the image box
     *
     * @param op
     */
    private void filter(BufferedImageOp op) {
        if (image != null) {
            BufferedImage filteredImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            op.filter(image, filteredImage);
            image = filteredImage;
            repaint();
        }
    }

    /**
     * This routine will be invoked to invert the image box
     */
    public void negative() {
        convertToRGBImage();
        if (invertFlag) {
            invertFlag = false;
            repaint();
        } else {
            byte negative[] = new byte[256];
            for (int i = 0; i < 256; i++) {
                negative[i] = (byte) (255 - i);
            }
            ByteLookupTable table = new ByteLookupTable(0, negative);
            LookupOp op = new LookupOp(table, null);
            filter(op);
            invertFlag = true;
        }
        canvas.repaint();

    }

    /**
     * This routine used to rotate the image box ninety degree right
     */
    public void rotateRight() {
        isRotate = true;
        rotateRightAngle = rotateRightAngle + 90;
        if (rotateRightAngle == 360) {
            rotateRightAngle = 0;
            rotateLeftAngle = 0;
        } else if (rotateRightAngle == 90) {
            rotateLeftAngle = -270;
        } else if (rotateRightAngle == 180) {
            rotateLeftAngle = -180;
        } else if (rotateRightAngle == 270) {
            rotateLeftAngle = -90;
        }
        repaint();
        repaintTextOverlay();
    }

    /**
     * This routine used to rotate the image box ninety degree left
     */
    public void rotateLeft() {
        isRotate = true;
        rotateLeftAngle = rotateLeftAngle - 90;
        if (rotateLeftAngle == -360) {
            rotateRightAngle = 0;
            rotateLeftAngle = 0;
        } else if (rotateLeftAngle == -90) {
            rotateRightAngle = 270;
        } else if (rotateLeftAngle == -180) {
            rotateRightAngle = 180;
        } else if (rotateLeftAngle == -270) {
            rotateRightAngle = 90;
        }
        repaint();
        repaintTextOverlay();
    }
    /*
     * static AffineTransform mirrorHorizontalTransform;
     *
     * static { // Create and initialize the AffineTransform
     * mirrorHorizontalTransform = AffineTransform.getTranslateInstance(512, 0);
     * mirrorHorizontalTransform.scale(-1.0, 1.0); // flip horizontally } static
     * AffineTransform mirrorVerticalTransform;
     *
     * static { // Create and initialize the AffineTransform
     * mirrorVerticalTransform = AffineTransform.getTranslateInstance(0, 512);
     * mirrorVerticalTransform.scale(1.0, -1.0); // flip horizontally }
     */

    public void flipHorizontal() {
        if ((rotateRightAngle == 90) || (rotateRightAngle == 270) || (rotateLeftAngle == -90) || (rotateLeftAngle == -270)) {
            flipV();
        } else {
            flipH();
        }
    }

    public void flipVertical() {
        if ((rotateRightAngle == 90) || (rotateRightAngle == 270) || (rotateLeftAngle == -90) || (rotateLeftAngle == -270)) {
            flipH();
        } else {
            flipV();
        }
    }

    private void flipV() {
        if (flipVerticalFlag) {
            flipVerticalFlag = false;
        } else {
            flipVerticalFlag = true;
        }
    }

    private void flipH() {
        if (flipHorizontalFlag) {
            flipHorizontalFlag = false;
        } else {
            flipHorizontalFlag = true;
        }
    }

    public void reset() {
        canvas.getLayeredCanvas().annotationPanel.resetAnnotation();
        canvas.getLayeredCanvas().annotationPanel.clearAllMeasurement();
        windowLevel = (int) WC;
        windowWidth = (int) WW;
        windowChanged(windowLevel, windowWidth);
        JPanel panel = ((JPanel) ((JSplitPane) ApplicationContext.tabbedPane.getSelectedComponent()).getRightComponent());
        if (!ApplicationContext.imgView.getImageToolbar().isImageLayout) {
            setScaleFactor(ApplicationContext.tabbedPane.getWidth(), ((JPanel) panel.getComponent(0)).getHeight(), layoutColumns * layoutRows);
        } else {
            setScaleFactor(((JPanel) panel.getComponent(0)).getWidth() / layoutColumns, ((JPanel) panel.getComponent(0)).getHeight() / layoutRows, layoutColumns * layoutRows);
        }
        canvas.getLayeredCanvas().annotationPanel.scaleProcess();
        canvas.getLayeredCanvas().annotationPanel.resizeHandler();
        centerImage();
        invertFlag = flipHorizontalFlag = flipVerticalFlag = isRotate = displayScout = synchornizeTiles = false;
        rotateLeftAngle = rotateRightAngle = 0;
        canvas.setBackground(Color.BLACK);
        canvas.setForeground(Color.WHITE);
        LocalizerDelegate.hideAllScoutLines();
        canvas.getLayeredCanvas().annotationPanel.resetAnnotaionTools();
        tool = "windowing";
        canvas.getLayeredCanvas().annotationPanel.tool = "windowing";
        ApplicationContext.imgView.getImageToolbar().deselectTools();
        repaint();
    }

    public void resizeHandler() {
        repaint();
        centerImage();
        canvas.setSelection(false);
    }

    /**
     * This override routine used to paint the image box
     *
     * @param gs
     */
    @Override
    public void paintComponent(Graphics gs) {
        Graphics2D g = (Graphics2D) gs;
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            if (isRotate) {
                if (rotateRightAngle == 90) {
                    g.rotate(Math.PI / 2, this.getSize().width / 2, this.getSize().height / 2);
                } else if (rotateRightAngle == 180) {
                    g.rotate(Math.PI, this.getSize().width / 2, this.getSize().height / 2);
                } else if (rotateRightAngle == 270) {
                    g.rotate((Math.PI * 3) / 2, this.getSize().width / 2, this.getSize().height / 2);
                }
            }
            if (flipHorizontalFlag) {
                g.translate(this.getSize().width, 0);
                g.scale(-1, 1);
            }
            if (flipVerticalFlag) {
                g.translate(0, this.getSize().height);
                g.scale(1, -1);
            }
            if (scaleFlag) {
                g.scale(scaleFactor, scaleFactor);
            }
            if (image != null) {
                if (newBufferedImage && invertFlag) {
                    newBufferedImage = false;
                    byte[] negative = new byte[256];
                    for (int i = 0; i < 256; i++) {
                        negative[i] = (byte) (255 - i);
                    }
                    ByteLookupTable table = new ByteLookupTable(0, negative);
                    LookupOp op = new LookupOp(table, null);
                    convertToRGBImage();
                    BufferedImage filteredImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
                    op.filter(image, filteredImage);
                    image = filteredImage;
                    filteredImage = null;
                }
                g.drawImage(image, startX, startY, thumbWidth, thumbHeight, null);
                if (ApplicationContext.layeredCanvas.imgpanel != null && !ApplicationContext.layeredCanvas.imgpanel.isLocalizer && displayScout && isLocalizer) {
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(Color.YELLOW);
                    if (orientationLabel.equalsIgnoreCase("SAGITTAL")) {
                        if (modality.equals("CT") || slope1 == slope2) {
                            g.drawLine((int) (boundaryLine1X1 * currentScaleFactor + startX), (int) (boundaryLine1Y1 * currentScaleFactor + startY), (int) (boundaryLine1X2 * currentScaleFactor + startX), (int) (boundaryLine1Y2 * currentScaleFactor + startY));
                            g.drawLine((int) (boundaryLine2X1 * currentScaleFactor + startX), (int) (boundaryLine2Y1 * currentScaleFactor + startY), (int) (boundaryLine2X2 * currentScaleFactor + startX), (int) (boundaryLine2Y2 * currentScaleFactor + startY));
                        }
                        g.setColor(Color.GREEN);
                        g.drawLine((int) (scoutLine1X1 * currentScaleFactor + startX), (int) (scoutLine1Y1 * currentScaleFactor + startY), (int) (scoutLine1X2 * currentScaleFactor + startX), (int) (scoutLine1Y2 * currentScaleFactor + startY));
                        g.drawLine((int) (scoutLine2X1 * currentScaleFactor + startX), (int) (scoutLine2Y1 * currentScaleFactor + startY), (int) (scoutLine2X2 * currentScaleFactor + startX), (int) (scoutLine2Y2 * currentScaleFactor + startY));
                    } else if (orientationLabel.equalsIgnoreCase("CORONAL")) {
                        g.drawLine((int) (axis1LeftX * currentScaleFactor + startX), (int) (axis1LeftY * currentScaleFactor + startY), (int) (axis1RightX * currentScaleFactor + startX), (int) (axis1RightY * currentScaleFactor + startY));
                        g.drawLine((int) (axis2LeftX * currentScaleFactor + startX), (int) (axis2LeftY * currentScaleFactor + startY), (int) (axis2RightX * currentScaleFactor + startX), (int) (axis2RightY * currentScaleFactor + startY));
                        g.setColor(Color.GREEN);
                        g.drawLine((int) (axisLeftX * currentScaleFactor + startX), (int) (axisLeftY * currentScaleFactor + startY), (int) (axisRightX * currentScaleFactor + startX), (int) (axisRightY * currentScaleFactor + startY));
                    }
                }
            }
            if (dataset.getString(Tags.SOPClassUID) != null && dataset.getString(Tags.SOPClassUID).equalsIgnoreCase("1.2.840.10008.5.1.4.1.1.104.1")) {
                Image loadedImage = null;
                calculateResolutionForPdfDicom(loadedImage.getWidth(null), loadedImage.getHeight(null));
                canvas.getLayeredCanvas().textOverlay.getTextOverlayParam().setCurrentInstance(curpage);
                canvas.getLayeredCanvas().textOverlay.getTextOverlayParam().setTotalInstance(Integer.toString(totalInstance));
                g.drawImage(loadedImage, startX, startY, thumbWidth, thumbHeight, null);
                ApplicationContext.imgView.getImageToolbar().hideAnnotationTools();
            }
        } catch (Exception ex) { //ignore
        } finally {
            g.dispose();
//            g = null;
        }
    }

    private void calculateHeightAndWidth() {
        thumbHeight = thumbWidth = 512;
        if (imgHeight < imgWidth) {
            thumbHeight = Math.min(imgHeight, imgWidth) * maxWidth / Math.max(imgHeight, imgWidth);
        } else {
            thumbWidth = Math.min(imgHeight, imgWidth) * maxWidth / Math.max(imgHeight, imgWidth);
        }
        startX = (maxWidth - thumbWidth) / 2;
        startY = (maxHeight - thumbHeight) / 2;
        calculateCurrentScaleFactor();
    }

    public void setScoutCoordinates(int line1X1, int line1Y1, int line1X2, int line1Y2, int line2X1, int line2Y1, int line2X2, int line2Y2) {
        displayScout = true;
        scoutLine1X1 = line1X1;
        scoutLine1X2 = line1X2;
        scoutLine1Y1 = line1Y1;
        scoutLine1Y2 = line1Y2;
        scoutLine2X1 = line2X1;
        scoutLine2X2 = line2X2;
        scoutLine2Y1 = line2Y1;
        scoutLine2Y2 = line2Y2;
        repaint();
    }

    public void displayZoomLevel() {
        int currentZoomLevel = (int) Math.round(scaleFactor * currentScaleFactor * 100);
        canvas.getLayeredCanvas().textOverlay.getTextOverlayParam().setZoomLevel(ApplicationContext.currentBundle.getString("ImageView.textOverlay.zoomLabel.text") + currentZoomLevel + "%");
    }

    public void calculateCurrentScaleFactor() {
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();
        double imageRatio = imageWidth / imageHeight;
        if (imageRatio < floatAspectRatio) {
            imageHeight = (imageWidth + 0.00f) / floatAspectRatio;
        } else {
            imageWidth = (imageHeight + 0.00f) * floatAspectRatio;
        }
        currentScaleFactor = (thumbHeight + 0.000f) / imageHeight;
    }

    public void setScoutBorder1Coordinates(int line1X1, int line1Y1, int line1X2, int line1Y2) {
        boundaryLine1X1 = line1X1;
        boundaryLine1X2 = line1X2;
        boundaryLine1Y1 = line1Y1;
        boundaryLine1Y2 = line1Y2;
        slope1 = findSlope(boundaryLine1X1 * currentScaleFactor + startX, boundaryLine1Y1 * currentScaleFactor + startY, boundaryLine1X2 * currentScaleFactor + startX, boundaryLine1Y2 * currentScaleFactor + startY);
    }

    public void setScoutBorder2Coordinates(int line1X1, int line1Y1, int line1X2, int line1Y2) {
        boundaryLine2X1 = line1X1;
        boundaryLine2X2 = line1X2;
        boundaryLine2Y1 = line1Y1;
        boundaryLine2Y2 = line1Y2;
        slope2 = findSlope(boundaryLine2X1 * currentScaleFactor + startX, boundaryLine2Y1 * currentScaleFactor + startY, boundaryLine2X2 * currentScaleFactor + startX, boundaryLine2Y2 * currentScaleFactor + startY);
    }

    public void setAxis1Coordinates(int leftx, int lefty, int rightx, int righty, int topx, int topy, int bottomx, int bottomy) {
        axis1LeftX = leftx;
        axis1LeftY = lefty;
        axis1RightX = rightx;
        axis1RightY = righty;
    }

    public void setAxis2Coordinates(int leftx, int lefty, int rightx, int righty, int topx, int topy, int bottomx, int bottomy) {
        axis2LeftX = leftx;
        axis2LeftY = lefty;
        axis2RightX = rightx;
        axis2RightY = righty;
    }

    public void setAxisCoordinates(int leftx, int lefty, int rightx, int righty, int topx, int topy, int bottomx, int bottomy) {
        axisLeftX = leftx;
        axisLeftY = lefty;
        axisRightX = rightx;
        axisRightY = righty;
    }

    private void findOrientation() {
        String imageOrientationArray[];
        if (!currentScoutDetails.getImageOrientation().equalsIgnoreCase("null")) {
            imageOrientationArray = currentScoutDetails.getImageOrientation().split("\\\\");
            float _imgRowCosx = Float.parseFloat(imageOrientationArray[0]);
            float _imgRowCosy = Float.parseFloat(imageOrientationArray[1]);
            float _imgRowCosz = Float.parseFloat(imageOrientationArray[2]);
            float _imgColCosx = Float.parseFloat(imageOrientationArray[3]);
            float _imgColCosy = Float.parseFloat(imageOrientationArray[4]);
            float _imgColCosz = Float.parseFloat(imageOrientationArray[5]);
            orientationLabel = getOrientationLabelFromImageOrientation(_imgRowCosx, _imgRowCosy, _imgRowCosz, _imgColCosx, _imgColCosy, _imgColCosz);
            if (orientationLabel.equalsIgnoreCase("CORONAL") || orientationLabel.equalsIgnoreCase("SAGITTAL")) {
                isLocalizer = true;
            }
        }
    }

    public String getOrientationLabelFromImageOrientation(double rowX, double rowY, double rowZ, double colX, double colY, double colZ) {
        String label = null;
        String ColumnRight = ImageOrientation.getOrientation(rowX, rowY, rowZ);
        String rowDown = ImageOrientation.getOrientation(colX, colY, colZ);
        String axis1 = ColumnRight.substring(0, 1);
        String axis2 = rowDown.substring(0, 1);
        if ((axis1 != null) && (axis2 != null)) {
            if ((((axis1.equals(ApplicationContext.currentBundle.getString("ImageView.imageOrientation.right"))) || (axis1.equals(ApplicationContext.currentBundle.getString("ImageView.imageOrientation.left"))))) && (((axis2.equals(ApplicationContext.currentBundle.getString("ImageView.imageOrientation.anterior").substring(0, 1))) || (axis2.equals(ApplicationContext.currentBundle.getString("ImageView.imageOrientation.posterior").substring(0, 1)))))) {
                label = "AXIAL";
            } else if ((((axis2.equals(ApplicationContext.currentBundle.getString("ImageView.imageOrientation.right"))) || (axis2.equals(ApplicationContext.currentBundle.getString("ImageView.imageOrientation.left"))))) && (((axis1.equals(ApplicationContext.currentBundle.getString("ImageView.imageOrientation.anterior").substring(0, 1))) || (axis1.equals(ApplicationContext.currentBundle.getString("ImageView.imageOrientation.posterior").substring(0, 1)))))) {
                label = "AXIAL";
            } else if ((((axis1.equals(ApplicationContext.currentBundle.getString("ImageView.imageOrientation.right"))) || (axis1.equals(ApplicationContext.currentBundle.getString("ImageView.imageOrientation.left"))))) && (((axis2.equals(ApplicationContext.currentBundle.getString("ImageView.imageOrientation.head").substring(0, 1))) || (axis2.equals(ApplicationContext.currentBundle.getString("ImageView.imageOrientation.foot").substring(0, 1)))))) {
                label = "CORONAL";
            } else if ((((axis2.equals(ApplicationContext.currentBundle.getString("ImageView.imageOrientation.right"))) || (axis2.equals(ApplicationContext.currentBundle.getString("ImageView.imageOrientation.left"))))) && (((axis1.equals(ApplicationContext.currentBundle.getString("ImageView.imageOrientation.head").substring(0, 1))) || (axis1.equals(ApplicationContext.currentBundle.getString("ImageView.imageOrientation.foot").substring(0, 1)))))) {
                label = "CORONAL";
            } else if ((((axis1.equals(ApplicationContext.currentBundle.getString("ImageView.imageOrientation.anterior").substring(0, 1))) || (axis1.equals(ApplicationContext.currentBundle.getString("ImageView.imageOrientation.posterior").substring(0, 1))))) && (((axis2.equals(ApplicationContext.currentBundle.getString("ImageView.imageOrientation.head").substring(0, 1))) || (axis2.equals(ApplicationContext.currentBundle.getString("ImageView.imageOrientation.foot").substring(0, 1)))))) {
                label = "SAGITTAL";
            } else if ((((axis2.equals(ApplicationContext.currentBundle.getString("ImageView.imageOrientation.anterior").substring(0, 1))) || (axis2.equals(ApplicationContext.currentBundle.getString("ImageView.imageOrientation.posterior").substring(0, 1))))) && (((axis1.equals(ApplicationContext.currentBundle.getString("ImageView.imageOrientation.head").substring(0, 1))) || (axis1.equals(ApplicationContext.currentBundle.getString("ImageView.imageOrientation.foot").substring(0, 1)))))) {
                label = "SAGITTAL";
            }
        } else {
            label = "OBLIQUE";
        }
        return label;
    }

    public void convertToRGBImage() {
        Image loadedImage = null;
        ImageIcon imageIcon = new ImageIcon();
        if (currentbufferedimage != null) {
            imageIcon.setImage(currentbufferedimage);
            loadedImage = imageIcon.getImage();
            image = new BufferedImage(loadedImage.getWidth(null), loadedImage.getHeight(null), BufferedImage.TYPE_INT_BGR);
            Graphics2D g2 = image.createGraphics();
            g2.drawImage(loadedImage, 0, 0, null);
        }
    }

    private void scaleProcess() {
        double currentWidth = this.getSize().width;
        double currentHeight = this.getSize().height;
        double newWidth = maxWidth * scaleFactor;
        double newHeight = maxHeight * scaleFactor;
        double widthDiff = newWidth - currentWidth;
        double heightDiff = newHeight - currentHeight;
        int currentX = this.getBounds().x;
        int currentY = this.getBounds().y;
        double newX = currentX - (widthDiff / 2);
        double newY = currentY - (heightDiff / 2);
        this.setBounds((int) newX, (int) newY, (int) newWidth, (int) newHeight);
        this.revalidate();
        repaint();
    }

    /**
     * This routine used to calculate the x and y position of the image box to
     * be placed in the outer canvas and set the bounds
     */
    private void centerImage() {
        this.setBounds((canvas.getSize().width - this.getSize().width) / 2, (canvas.getSize().height - this.getSize().height) / 2, this.getSize().width, this.getSize().height);
    }

    /**
     * This method creates the color model based on the window level and width
     * and apply the values to the image box
     *
     * @param windowCenter
     * @param windowWidth
     */
    public void windowChanged(int windowCenter, int winWidth) {
        try {
            if (cmParam != null) {
                cmParam = cmParam.update(windowCenter, winWidth, cmParam.isInverse());
                cm = cmFactory.getColorModel(cmParam);
                currentbufferedimage = new BufferedImage(cm, currentbufferedimage.getRaster(), false, null);
                windowLevel = windowCenter;
                windowWidth = winWidth;
            }
            convertToRGBImage();
            repaint();
            changeTextOverlay();
        } catch (Exception e) { //ignore
        }
    }

    public void repaintTextOverlay() {
        canvas.getLayeredCanvas().textOverlay.repaint();
    }

    @Override
    public ColorModel getColorModel() {
        if (cmParam != null) {
            cmParam = cmParam.update(windowLevel, windowWidth, cmParam.isInverse());
            cm = cmFactory.getColorModel(cmParam);
        }
        return cm;
    }

    /**
     * This routine used to change the text overlay of the image box
     */
    public void changeTextOverlay() {
        canvas.getLayeredCanvas().textOverlay.setWindowingParameter(Integer.toString(windowLevel), Integer.toString(windowWidth));
    }

    private void addlisteners() {
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
    }

    public void probe() {
        if (!probeFlag) {
            probeFlag = true;
            canvas.getLayeredCanvas().textOverlay.repaint();
        } else {
            probeFlag = false;
            canvas.getLayeredCanvas().textOverlay.repaint();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();

        setBackground(new java.awt.Color(0, 0, 0));
        setDoubleBuffered(false);
        setFocusCycleRoot(true);
        setOpaque(false);
        setPreferredSize(new java.awt.Dimension(512, 512));
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
        });
        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
        canvas.requestFocus();
    }//GEN-LAST:event_formMouseClicked
    private void setTotalInstance() {
        if (multiframe) {
            totalInstance = nFrames;
        } else {
            totalInstance = ApplicationContext.databaseRef.getSeriesLevelInstance(studyUID, seriesUID);
        }
        if (dataset.getString(Tags.SOPClassUID) != null && dataset.getString(Tags.SOPClassUID).equalsIgnoreCase("1.2.840.10008.5.1.4.1.1.104.1")) {
            totalInstance = curFile.getNumPages();
        }
        currentInstanceNo = currentFrame = 0;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        this.requestFocus();
        int notches = e.getWheelRotation();
        if (notches < 0) {
            doPrevious();
        } else {
            doNext();
        }
    }

    public void doPrevious() {
        JPanel panel = (JPanel) canvas.getLayeredCanvas().getParent();
        if (panel.getComponentCount() > 1) {
            if (System.currentTimeMillis() - timeDelay > panel.getComponentCount() * 10) {
                reverseMultipleImages(panel);
                timeDelay = System.currentTimeMillis();
            }
        } else if (multiframe) {
            canvas.setSelection(false);
            showPreviousFrame();
        } else if (isEncapsulatedDocument) {
            canvas.setSelection(false);
            if (dataset.getString(Tags.SOPClassUID) != null && dataset.getString(Tags.SOPClassUID).equalsIgnoreCase("1.2.840.10008.5.1.4.1.1.104.1")) {
                forceGotoPage(curpage - 1);
            }
        } else {
            canvas.setSelection(false);
            previous();
        }
    }

    public void doNext() {
        JPanel panel = (JPanel) canvas.getLayeredCanvas().getParent();
        if (panel.getComponentCount() > 1) {
            if (System.currentTimeMillis() - timeDelay > panel.getComponentCount() * 10) {
                forwardMultipleImages(panel);
                timeDelay = System.currentTimeMillis();
            }
        } else if (multiframe) {
            canvas.setSelection(false);
            showNextFrame();
        } else if (isEncapsulatedDocument) {
            canvas.setSelection(false);
            if (dataset.getString(Tags.SOPClassUID) != null && dataset.getString(Tags.SOPClassUID).equalsIgnoreCase("1.2.840.10008.5.1.4.1.1.104.1")) {
                forceGotoPage(curpage + 1);
            }
        } else {
            canvas.setSelection(false);
            next();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        canvas.setSelection(true);
        selectSeries(e);
    }

    private void selectSeries(MouseEvent e) {
        mouseLocX1 = e.getX();
        mouseLocY1 = e.getY();
        if (SwingUtilities.isRightMouseButton(e)) {
            designContext();
            jPopupMenu1.show(this, e.getX(), e.getY());
        }
    }
    private PopupMenuListener popupListener = new PopupMenuListener();

    public void addContextItem() {
        MenuElement[] me = jPopupMenu1.getSubElements();
        JMenu menu;
        if (me.length > 0) {
            ArrayList presetList = ApplicationContext.databaseRef.getPresetsForModality(ApplicationContext.layeredCanvas.imgpanel.getModality());
            if (presetList.size() > 0) {
                menu = new JMenu("Window Width & Level");
                jPopupMenu1.addSeparator();
                for (int i = 0; i < presetList.size(); i++) {
                    final PresetModel presetModel = (PresetModel) presetList.get(i);
                    if (!presetModel.getPresetName().equalsIgnoreCase("PRESETNAME")) {
                        JMenuItem menu1 = new JMenuItem(presetModel.getPresetName());
                        menu.add(menu1);
                        menu1.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                JPanel currentPanel = (JPanel) ApplicationContext.layeredCanvas.getParent();
                                try {
                                    for (int i = 0; i < currentPanel.getComponentCount(); i++) {
                                        ((LayeredCanvas) currentPanel.getComponent(i)).imgpanel.windowChanged(Integer.parseInt(presetModel.getWindowLevel()), Integer.parseInt(presetModel.getWindowWidth()));
                                    }
                                } catch (NullPointerException npe) {
                                    //Null pointer exception occurs when there is no components in image layout
                                }
                            }
                        });
                    }
                }
                jPopupMenu1.add(menu);
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseLocX2 = e.getX();
        mouseLocY2 = e.getY();

        if (tool.equalsIgnoreCase("windowing")) {
            mouseDragWindowing(mouseLocX2, mouseLocY2);
        } else if (tool.equalsIgnoreCase("panning")) {
            JPanel currentSeriesPanel = (JPanel) ApplicationContext.layeredCanvas.getParent();
            for (int i = 0; i < currentSeriesPanel.getComponentCount(); i++) {
                LayeredCanvas tempCanvas = (LayeredCanvas) currentSeriesPanel.getComponent(i);
                if (tempCanvas.imgpanel != null) {
                    tempCanvas.imgpanel.setLocation(this.getBounds().x + mouseLocX2 - mouseLocX1, this.getBounds().y + mouseLocY2 - mouseLocY1);
                    tempCanvas.imgpanel.repaint();
                    tempCanvas.canvas.repaint();
                }
            }
        } else if (tool.equalsIgnoreCase("stack")) {
            mouseDragStack(mouseLocX2, mouseLocY2);
        }
    }

    private void mouseDragWindowing(int mouseLocX2, int mouseLocY2) {
        int mouseLocDiffX = (int) ((mouseLocX2 - mouseLocX1)) * windowingMultiplier;
        int mouseLocDiffY = (int) ((mouseLocY1 - mouseLocY2)) * windowingMultiplier;
        mouseLocX1 = mouseLocX2;
        mouseLocY1 = mouseLocY2;
        double newWindowWidth = windowWidth + mouseLocDiffX * 5;
        if (newWindowWidth < 1.0) {
            newWindowWidth = 1.0;
        }
        double newWindowLevel = windowLevel + mouseLocDiffY * 5;
        JPanel currentSeriesPanel = (JPanel) ApplicationContext.layeredCanvas.getParent();
        for (int i = 0; i < currentSeriesPanel.getComponentCount(); i++) {
            ((LayeredCanvas) currentSeriesPanel.getComponent(i)).imgpanel.windowChanged((int) newWindowLevel, (int) newWindowWidth);
        }
    }

    private void mouseDragStack(int mouseLocX2, int mouseLocY2) {
        int mouseLocDiffY = (int) ((mouseLocY2 - mouseLocY1));
        if (mouseLocDiffY < -12) {
            mouseLocX1 = mouseLocX2;
            mouseLocY1 = mouseLocY2;
            doPrevious();
        } else if (mouseLocDiffY > 12) {
            mouseLocX1 = mouseLocX2;
            mouseLocY1 = mouseLocY2;
            doNext();
        }
    }

    public void setImage(String sliceLocation, boolean isForward) {
        /**
         * This method has been added for synchronized scroll it will not
         * support for multiframe sync scroll. so that instance number can be
         * set directly to instanceNumber and currentInstanceNo variable.
         */
        storeAnnotation();
        String iuid = ApplicationContext.databaseRef.getInstanceUIDBasedOnSliceLocation(studyUID, seriesUID, sliceLocation, ApplicationContext.layeredCanvas.imgpanel.sliceThickness);
        if (iuid != null) {
            currentInstanceNo = instanceUidList.indexOf(iuid);
            setImage(imgBuffer.get(currentInstanceNo, isForward));
        }
    }

    public void updateTextoverlay() {
        canvas.getLayeredCanvas().textOverlay.getTextOverlayParam().setCurrentInstance(currentInstanceNo);
        canvas.getLayeredCanvas().textOverlay.getTextOverlayParam().setSlicePosition(currentScoutDetails.getSliceLocation());
    }

    public ScoutLineInfoModel[] prepareScoutBorder() {
        return ApplicationContext.databaseRef.getFirstAndLastInstances(studyUID, seriesUID);
    }

    private String calculateHU(int x, int y) {
        try {
            pixelValueArray = currentbufferedimage.getSampleModel().getPixel(x, y, (int[]) null, currentbufferedimage.getRaster().getDataBuffer());
            pixelValue = pixelValueArray[0];
            try {
                hu = Integer.toString(pixelValue * Integer.parseInt(rescaleSlope) + Integer.parseInt(rescaleIntercept));
            } catch (Exception e) {
                hu = Integer.toString(pixelValue * 1 - 1024);
            }
        } catch (Exception e) {
        }
        return hu;
    }

    public double calculateMean(int x, int y, int width, int height) {
        try {
            int sum = 0;
            int pixelCount = 0;
            pixelValueArray = currentbufferedimage.getSampleModel().getPixels(x, y, width, height, (int[]) null, currentbufferedimage.getRaster().getDataBuffer());
            for (int i = 0; i < pixelValueArray.length; i++) {
                ++pixelCount;
                int value;
                try {
                    value = pixelValueArray[i] * Integer.parseInt(rescaleSlope) + Integer.parseInt(rescaleIntercept);
                } catch (Exception e) {
                    value = pixelValueArray[i] * 1 - 1024;
                }
                sum += value;
            }
            if (pixelCount == 0) {
                return 0;
            }
            return (double) sum / pixelCount;
        } catch (Exception e) {
            return 0;
        }
    }

    public double calculateStandardDeviation(double mean, int x, int y, int width, int height) {
        try {
            double sum = 0;
            int pixelCount = 0;
            pixelValueArray = currentbufferedimage.getSampleModel().getPixels(x, y, width, height, (int[]) null, currentbufferedimage.getRaster().getDataBuffer());
            for (int i = 0; i < pixelValueArray.length; i++) {
                ++pixelCount;
                int value;
                try {
                    value = pixelValueArray[i] * Integer.parseInt(rescaleSlope) + Integer.parseInt(rescaleIntercept);
                } catch (Exception e) {
                    value = pixelValueArray[i] * 1 - 1024;
                }
                double deviation = value - mean;
                sum += deviation * deviation;
            }
            if (pixelCount == 0) {
                return 0;
            }
            return Math.sqrt(sum / pixelCount);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseLocX2 = e.getX();
        mouseLocY2 = e.getY();
        if (probeFlag) {
            String probeParameter[] = new String[3];
            probeParameter[0] = Integer.toString((int) Math.round(mouseLocX2 / scaleFactor));
            probeParameter[1] = Integer.toString((int) Math.round(mouseLocY2 / scaleFactor));
            probeParameter[2] = calculateHU((int) Math.round(mouseLocX2 / scaleFactor), (int) Math.round(mouseLocY2 / scaleFactor));
            canvas.getLayeredCanvas().textOverlay.setProbeParameters(probeParameter);
            probeParameter = null;
        }
    }

    private void designContext() {
        ArrayList<Series> seriesList = ApplicationContext.databaseRef.getSeriesList_SepMulti(studyUID);
        JMenu menu;
        if ((studyDesc == null) || (studyDesc.equalsIgnoreCase(""))) {
            menu = new JMenu(studyUID);
        } else {
            menu = new JMenu(studyDesc);
        }
        JMenu menu1 = new JMenu("Multiframe(s)");
        for (final Series series : seriesList) {
            JMenuItem menuitem = null;
            if (series.isMultiframe()) {
                if (Integer.parseInt(series.getImageList().get(0).getInstance_no()) < 10) {
                    menuitem = new JMenuItem(series.getImageList().get(0).getInstance_no() + "   - Frames  " + series.getImageList().get(0).getTotalNumFrames());
                } else {
                    menuitem = new JMenuItem(series.getImageList().get(0).getInstance_no() + " - Frames  " + series.getImageList().get(0).getTotalNumFrames());
                }
                menu1.add(menuitem);
            } else if (!series.getSeriesDesc().equalsIgnoreCase("null")) {
                menuitem = new JMenuItem(series.getSeriesDesc());
            } else if (!series.getBodyPartExamined().equalsIgnoreCase("null")) {
                menuitem = new JMenuItem(series.getBodyPartExamined());
            } else {
                menuitem = new JMenuItem(series.getSeriesInstanceUID());
            }
            if (!series.isMultiframe()) {
                menu.add(menuitem);
            } else {
                menu.add(menu1);
            }
            menuitem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    if (ApplicationContext.selectedPanel.getComponentCount() == 1) {
                        changeSeries(series.getStudyInstanceUID(), series.getSeriesInstanceUID(), null, 0);
                    } else {
                        changeSeries(series.getStudyInstanceUID(), series.getSeriesInstanceUID(), null, 0, ApplicationContext.selectedPanel);
                    }
                }
            });
        }
        jPopupMenu1.removeAll();
        popupListener.createPopupMenu(jPopupMenu1);
        addContextItem();
        jPopupMenu1.addSeparator();
        jPopupMenu1.add(menu);
    }

    public void doSynchronize() {
        if (synchornizeTiles || ((JPanel) ((JPanel) canvas.getLayeredCanvas().getParent()).getParent()).getComponentCount() == 1 || !modality.contains("CT")) {
            synchornizeTiles = false;
        } else {
            synchornizeTiles = true;
            selectTiles();
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPopupMenu jPopupMenu1;
    // End of variables declaration//GEN-END:variables

    public void setScaleFactor(float parentWidth, float parentHeight, int noOfComponents) {
        JPanel panel = (JPanel) ((JSplitPane) ApplicationContext.tabbedPane.getSelectedComponent()).getRightComponent();
        if (noOfComponents == 1) {
            if (parentWidth > imgWidth) {
                zoom((maxWidth / maxHeight) + (maxWidth / parentWidth));
            } else {
                zoom(panel.getComponentCount() + (parentHeight / parentWidth));
            }
        } else if (noOfComponents == 2) {
            zoom(maxWidth / maxHeight);
        } else {
            if (parentHeight <= parentWidth) {
                zoom(parentHeight / maxHeight);
            } else {
                zoom(parentWidth / maxWidth);
            }
        }
        canvas.getLayeredCanvas().textOverlay.getTextOverlayParam().setImageSize((imgHeight + "x" + imgWidth));
        if (ApplicationContext.tabbedPane.getWidth() == parentWidth) {
            canvas.getLayeredCanvas().textOverlay.getTextOverlayParam().setViewSize(((int) ApplicationContext.tabbedPane.getWidth() - ((JSplitPane) panel.getParent()).getDividerLocation() + "x" + (int) parentHeight));
        } else {
            canvas.getLayeredCanvas().textOverlay.getTextOverlayParam().setViewSize(((int) parentWidth + "x" + (int) parentHeight));
        }
    }

    public void zoom(double scalefactor) {
        scaleFlag = true;
        scaleFactor = scalefactor;
        displayZoomLevel();
        scaleProcess();
        canvas.repaint();
    }

    //To change the series by choosing from the preview panel
    public void changeSeries(String studyUID, String seriesUID, String sopUid, int instanceNo) {
        if (!multiframe) {
            storeAnnotation();
        } else {
            storeMultiframeAnnotation();
        }
        SeriesChooserDelegate seriesChooserDelegate = new SeriesChooserDelegate(studyUID, seriesUID, sopUid, canvas.getLayeredCanvas(), instanceNo);
    }

    //To change series in case of image layout
    public void changeSeries(String studyUID, String seriesUID, String sopUid, int instanceNumber, JPanel panel) {
        SeriesChooserDelegate seriesChooserDelegate = new SeriesChooserDelegate(studyUID, seriesUID, sopUid, instanceNumber, panel);
    }

    public void displayFrames() {
        try {
            currentbufferedimage = reader.read(currentFrame);
            convertToRGBImage();
            calculateHeightAndWidth();
            repaint();
            canvas.getLayeredCanvas().textOverlay.getTextOverlayParam().setCurrentInstance(currentInstanceNo);
            canvas.getLayeredCanvas().textOverlay.getTextOverlayParam().setTotalInstance(Integer.toString(totalInstance));
            canvas.getLayeredCanvas().annotationPanel.setAnnotation(currentSeriesAnnotation.getMultiframeAnnotation(currentInstanceNo));
        } catch (Exception ex) {
            Logger.getLogger(ImagePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void forwardMultipleImages(JPanel panel) {
        int lastInstanceNumber = ((LayeredCanvas) panel.getComponent(0)).imgpanel.currentInstanceNo;
        int tiles = panel.getComponentCount();
        if (totalInstance > ApplicationContext.imgBuffer.getDefaultBufferSize()) {
            ApplicationContext.imgBuffer.clearSubMap(lastInstanceNumber - tiles, lastInstanceNumber);
            ApplicationContext.imgBuffer.clearElementsForward(lastInstanceNumber + tiles + tiles);//Useful to remove the unwanted images exist in buffer because of loop back
            ApplicationContext.imageUpdator.terminateThread();
            ApplicationContext.imageUpdator = new ImageGenerator(ApplicationContext.imgBuffer, ApplicationContext.imgBuffer.getImagePanelRef(), false);
            if (lastInstanceNumber + tiles + tiles + 1 <= totalInstance) {
                ApplicationContext.imageUpdator.setParameters(lastInstanceNumber + tiles + tiles, lastInstanceNumber + tiles + tiles + tiles, true);
            } else if (ApplicationContext.imgBuffer.getFirstKey() != 0) {//Avoid multiple time updation of same images on loop back
                ApplicationContext.imageUpdator.setParameters(0, tiles + tiles, true);
            } else {
                ApplicationContext.imageUpdator.setParameters(ApplicationContext.imgBuffer.getLowerKey(tiles + tiles), ApplicationContext.imgBuffer.getLowerKey(tiles + tiles) + (ApplicationContext.imgBuffer.getDefaultBufferSize() - ApplicationContext.imgBuffer.getCurrentBufferSize()), true);
            }
            ApplicationContext.imageUpdator.start();
        }
        lastInstanceNumber += tiles + 1;
        if (lastInstanceNumber > ((LayeredCanvas) panel.getComponent(0)).imgpanel.totalInstance) {
            displayImages(panel, 0, false);
        } else {
            int x = lastInstanceNumber % tiles;
            if (x > 0) {
                displayImages(panel, lastInstanceNumber - x, false);
            } else {
                displayImages(panel, lastInstanceNumber - tiles, false);
            }
        }
        canvas.setSelection(false);
    }

    public void reverseMultipleImages(JPanel panel) {
        int lastInstanceNumber = ((LayeredCanvas) panel.getComponent(0)).imgpanel.currentInstanceNo;
        int tiles = panel.getComponentCount();
        if (totalInstance > ApplicationContext.imgBuffer.getDefaultBufferSize()) {
            ApplicationContext.imgBuffer.clearSubMap(lastInstanceNumber + tiles, lastInstanceNumber + tiles + tiles);
            ApplicationContext.imgBuffer.clearElementsBackward(lastInstanceNumber - tiles - tiles);
            ApplicationContext.imageUpdator.terminateThread();
            ApplicationContext.imageUpdator = new ImageGenerator(ApplicationContext.imgBuffer, ApplicationContext.imgBuffer.getImagePanelRef(), false);
            if (lastInstanceNumber - (tiles + tiles) >= 0) {
                ApplicationContext.imageUpdator.setParameters(lastInstanceNumber - tiles, lastInstanceNumber - (tiles + tiles), false);
            } else if (ApplicationContext.imgBuffer.getLastKey() + 1 != totalInstance) {
                ApplicationContext.imageUpdator.setParameters(totalInstance - 1, totalInstance - (totalInstance % tiles), false);
            } else {
                ApplicationContext.imageUpdator.setParameters(totalInstance - (totalInstance % tiles) - 1, totalInstance - (totalInstance % tiles) - tiles, false);
            }
            ApplicationContext.imageUpdator.start();
        }
        if (lastInstanceNumber == 0) {
            displayImages(panel, totalInstance - (totalInstance % tiles), false);
        } else {
            int x = lastInstanceNumber % tiles;
            if (x > 0) {
                displayImages(panel, lastInstanceNumber - x, false);
            } else {
                displayImages(panel, lastInstanceNumber - tiles, false);
            }
        }
        canvas.setSelection(false);
    }

    public void setVisibility(LayeredCanvas tempCanvas, boolean visibility) {
        if (tempCanvas.imgpanel != null) {
            tempCanvas.imgpanel.setVisible(visibility);
            tempCanvas.textOverlay.setVisible(visibility);
            tempCanvas.annotationPanel.setVisible(visibility);
            tempCanvas.setVisible(visibility);
            tempCanvas.repaint();
        }
    }

    //To render the overlay data in image
    private BufferedImage combineImages(BufferedImage currentbufferedimage, BufferedImage overlayImg) {
        Graphics2D g2d = currentbufferedimage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.drawImage(overlayImg, 0, 0, null);
        g2d.dispose();
        return currentbufferedimage;
    }

    //Displays set of images when moving forward,backward,thumbnail click of same series
    public void displayImages(JPanel panel, int instanceNumber, boolean waitForImages) {
        if (((LayeredCanvas) panel.getComponent(0)).imgpanel.totalInstance != instanceNumber) {
            for (int i = 0; i < panel.getComponentCount(); i++) {
                if (instanceNumber < ((LayeredCanvas) panel.getComponent(0)).imgpanel.totalInstance) {
                    if (panel.getComponent(i) instanceof LayeredCanvas) {
                        LayeredCanvas tempCanvas = ((LayeredCanvas) panel.getComponent(i));
                        if (tempCanvas.imgpanel != null && tempCanvas.imgpanel.seriesUID.equals(seriesUID)) {
                            tempCanvas.imgpanel.currentInstanceNo = instanceNumber;
                            if (!waitForImages) {
                                tempCanvas.imgpanel.displayImage(ApplicationContext.imgBuffer.getImmediately(instanceNumber));
                            } else {
                                tempCanvas.imgpanel.displayImage(ApplicationContext.imgBuffer.get(instanceNumber));
                            }

                            tempCanvas.imgpanel.repaint();
                            instanceNumber++;
                            setVisibility(tempCanvas, true);
                        } else {
                            tempCanvas.createSubComponents(fileLocation + File.separator + instanceUidList.get(instanceNumber), 0, true);
                            tempCanvas.imgpanel.setCurrentInstanceNo(instanceNumber);
                            tempCanvas.textOverlay.getTextOverlayParam().setCurrentInstance(instanceNumber);
                            tempCanvas.textOverlay.multiframeStatusDisplay(tempCanvas.imgpanel.isMultiFrame());
                            tempCanvas.imgpanel.setCurrentSeriesAnnotation();
                            instanceNumber++;
                            setVisibility(tempCanvas, true);
                        }
                    }
                } else {
                    LayeredCanvas tempCanvas = ((LayeredCanvas) panel.getComponent(i));
                    setVisibility(tempCanvas, false);
                }
            }
        }
        ApplicationContext.setImageIdentification();
    }

    public void selectImage(int instanceNumber) {
        storeAnnotation();
        currentInstanceNo = instanceNumber;
        if (imgBuffer.getDefaultBufferSize() < totalInstance) {
            setImage(readDicomFile(currentInstanceNo));
            imgBuffer.clearBuffer();
            imageUpdator.terminateThread();
            imageUpdator = new ImageGenerator(imgBuffer, this, false);
            imageUpdator.setParameters(currentInstanceNo - 15, currentInstanceNo + 10, true);
            imageUpdator.start();
        } else {
            setImage(imgBuffer.get(currentInstanceNo, true));
        }
        if (displayScout) {
            LocalizerDelegate localizerDelegate = null;
            if (currentInstanceNo > 1) {
                localizerDelegate = new LocalizerDelegate(true);
            } else {
                localizerDelegate = new LocalizerDelegate(false);
            }
            localizerDelegate.start();
        }
        if (synchornizeTiles && modality.contains("CT") && !multiframe) {
            selectTiles();
        }
        canvas.getLayeredCanvas().annotationPanel.setAnnotation(currentSeriesAnnotation.getInstanceAnnotation(currentInstanceNo));
    }

    public void doTileSync(boolean isForward) {
        JPanel outerComponent = (JPanel) ((JPanel) ApplicationContext.layeredCanvas.imgpanel.getCanvas().getLayeredCanvas().getParent()).getParent();
        for (int i = 0; i < outerComponent.getComponentCount(); i++) {
            LayeredCanvas layeredCanvas = (LayeredCanvas) ((JPanel) outerComponent.getComponent(i)).getComponent(0);
            ImagePanel imgPanel = layeredCanvas.imgpanel;
            if (ApplicationContext.layeredCanvas.imgpanel != imgPanel) {
                try {
                    imgPanel.setImage(ApplicationContext.layeredCanvas.imgpanel.getSliceLocation(), isForward);
                } catch (NullPointerException npe) {
                    //ignore : Null pointer exception occurs when there is no canvas
                }
            }
        }
    }

    public void checkIsLocalizer() {
        if (currentScoutDetails.getImageType().equalsIgnoreCase("Localizer")) {
            isLocalizer = true;
            if (displayScout) {
                JPanel panel = ((JPanel) ((JSplitPane) ApplicationContext.tabbedPane.getSelectedComponent()).getRightComponent());
                for (int j = 0; j < panel.getComponentCount(); j++) {
                    try {
                        LayeredCanvas temp = ((LayeredCanvas) ((JPanel) panel.getComponent(j)).getComponent(0));
                        if (temp.imgpanel != null) {
                            temp.imgpanel.repaint();
                        }
                    } catch (Exception e) {
//                        System.out.println("Exception in checkIsLocalizer() : " + e.getMessage());
                    }
                }
            }
        } else {
            isLocalizer = false;
        }
    }

    public double findSlope(double x1, double y1, double x2, double y2) {
        try {
            double slope = (y2 - y1) / (x2 - x1);
            return slope;
        } catch (ArithmeticException ex) {
            return 0;
        }
    }

    public BufferedImage readDicomFile(int i) {
        BufferedImage tempImage = null;
        ImageInputStream iis = null;
        File currentFile = null;
        try {
            if (!isLink) {
                currentFile = new File(fileLocation + File.separator + instanceUidList.get(i));
            } else {
                currentFile = new File(fileUrlsifLink.get(i));
            }
            iis = ImageIO.createImageInputStream(currentFile);
            Iterator iter = ImageIO.getImageReadersByFormatName("DICOM");
            reader = (ImageReader) iter.next();
            reader.setInput(iis, false);
            tempImage = reader.read(0);
            DicomInputStream dis = new DicomInputStream(currentFile);
            DicomObject obj = dis.readDicomObject();
            String overlayData = obj.getString(Tag.OverlayData);
            if (overlayData != null && overlayData.length() > 0) {
                tempImage = combineImages(tempImage, OverlayUtils.extractOverlay(obj, Tag.OverlayData, reader, "FFFFFF"));
            }
        } catch (IOException ex) {
            Logger.getLogger(ImagePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tempImage;
    }

    public synchronized void next() {
        storeAnnotation();
        if (currentInstanceNo + 1 < totalInstance) {
            currentInstanceNo++;
            setImage(imgBuffer.get(currentInstanceNo, true));
            if (displayScout) {
                LocalizerDelegate localizerDelegate = null;
                if (currentInstanceNo > 1) {
                    localizerDelegate = new LocalizerDelegate(true);
                } else {
                    localizerDelegate = new LocalizerDelegate(false);
                }
                localizerDelegate.start();
            }
            if (!multiframe && synchornizeTiles && modality.startsWith("CT")) {
                doTileSync(true);
            }
        } else {
            if (ApplicationContext.databaseRef.getLoopbackStatus()) {
                if (totalInstance > imgBuffer.getDefaultBufferSize()) {
                    imgBuffer.clearBuffer();
                    imageUpdator.terminateThread();
                    imageUpdator = new ImageGenerator(imgBuffer, this, false);
                    imageUpdator.setParameters(0, 25, true);
                    imageUpdator.start();
                }
                currentInstanceNo = -1;
                if (synchornizeTiles) {
                    updateTiles(true);
                }
            } else {
                currentInstanceNo = totalInstance;
            }
        }
    }

    public synchronized void previous() {
        storeAnnotation();
        if (currentInstanceNo - 1 >= 0) {
            currentInstanceNo--;
            setImage(imgBuffer.get(currentInstanceNo, false));
            if (displayScout) {
                LocalizerDelegate localizerDelegate = null;
                if (currentInstanceNo > 1) {
                    localizerDelegate = new LocalizerDelegate(true);
                } else {
                    localizerDelegate = new LocalizerDelegate(false);
                }
                localizerDelegate.start();
            }
            if (!multiframe && synchornizeTiles && modality.startsWith("CT")) {
                doTileSync(false);
            }
        } else {
            if (ApplicationContext.databaseRef.getLoopbackStatus()) {
                if (totalInstance > imgBuffer.getDefaultBufferSize()) {
                    imgBuffer.clearBuffer();
                    imageUpdator.terminateThread();
                    imageUpdator = new ImageGenerator(imgBuffer, this, false);
                    imageUpdator.setParameters(totalInstance - 1, totalInstance - 25, false);
                    imageUpdator.start();
                }
                currentInstanceNo = totalInstance;
                if (synchornizeTiles) {
                    updateTiles(false);
                }
            } else {
                currentInstanceNo = 0;
            }
        }
    }

    private void setImage(BufferedImage tempImage) {
        try {
            if (tempImage != null) {
                currentbufferedimage = tempImage;
                currentScoutDetails = ApplicationContext.databaseRef.getScoutLineDetails(studyUID, seriesUID, instanceUidList.get(currentInstanceNo));
                checkIsLocalizer();
                try {
                    if (cm != null) {
                        currentbufferedimage = new BufferedImage(cm, currentbufferedimage.getRaster(), false, null);
                    }
                } catch (Exception e) {
//                    System.out.println("Exception in setImage() : " + currentInstanceNo);
                }
                image = currentbufferedimage;
                newBufferedImage = true;
                canvas.getLayeredCanvas().textOverlay.getTextOverlayParam().setCurrentInstance(currentInstanceNo);
                canvas.getLayeredCanvas().textOverlay.getTextOverlayParam().setSlicePosition(currentScoutDetails.getSliceLocation());
                canvas.getLayeredCanvas().annotationPanel.setAnnotation(currentSeriesAnnotation.getInstanceAnnotation(currentInstanceNo));
                ApplicationContext.setImageIdentification();
                repaint();
            }
        } catch (NullPointerException ex) {
//            System.out.println("Null in setImage() : " + currentInstanceNo);
        }
    }

    public void startImageBuffering(int startFrom) {  //Starts the producer with the specified value
        if (!multiframe) {
            currentInstanceNo = startFrom;
            imgBuffer = new ImageBuffer(this);
            imageUpdator = new ImageGenerator(imgBuffer, this, false);
            if (imgBuffer.getDefaultBufferSize() < totalInstance) {
                imageUpdator.setParameters(startFrom, startFrom + 25, true);
            } else {
                imageUpdator.setParameters(0, totalInstance, true);
            }
            imageUpdator.start();
        }
    }

    public void updateCurrentInstance() {
        currentScoutDetails = ApplicationContext.databaseRef.getScoutLineDetails(studyUID, seriesUID, instanceUidList.get(currentInstanceNo));
        canvas.getLayeredCanvas().textOverlay.getTextOverlayParam().setCurrentInstance(currentInstanceNo);
        canvas.getLayeredCanvas().textOverlay.getTextOverlayParam().setSlicePosition(currentScoutDetails.getSliceLocation());
        ApplicationContext.setImageIdentification();
    }

    private void updateTiles(boolean isForward) { //Updates the other tiles when the tile to be synchronized reaches the last instance
        JPanel outerComponent = (JPanel) ((JPanel) ApplicationContext.layeredCanvas.getParent()).getParent();
        for (int i = 0; i < outerComponent.getComponentCount(); i++) {
            LayeredCanvas layeredCanvas = (LayeredCanvas) ((JPanel) outerComponent.getComponent(i)).getComponent(0);
            ImagePanel imgPanel = layeredCanvas.imgpanel;
            if (ApplicationContext.layeredCanvas.imgpanel != imgPanel) {
                try {
                    imgPanel.imgBuffer.clearBuffer();
                    imgPanel.imageUpdator.terminateThread();
                    imgPanel.imageUpdator = new ImageGenerator(imgPanel.imgBuffer, imgPanel, false);
                    if (isForward) {
                        imgPanel.imageUpdator.setParameters(0, 25, isForward);
                    } else {
                        imgPanel.imageUpdator.setParameters(imgPanel.totalInstance - 1, imgPanel.totalInstance - 25, isForward);
                    }
                    imgPanel.imageUpdator.start();
                } catch (NullPointerException npe) {
                    //ignore : Null pointer exception occurs when there is no canvas
                }
            }
        }
    }

    private synchronized void selectTiles() { //Selects the instance on tiles when a thumbnail click occurs on synchronization or synchronization starts from middle
        JPanel outerComponent = (JPanel) ((JPanel) ApplicationContext.layeredCanvas.getParent()).getParent();
        for (int i = 0; i < outerComponent.getComponentCount(); i++) {
            LayeredCanvas layeredCanvas = (LayeredCanvas) ((JPanel) outerComponent.getComponent(i)).getComponent(0);
            ImagePanel imgPanel = layeredCanvas.imgpanel;
            if (imgPanel != null && ApplicationContext.layeredCanvas.imgpanel != imgPanel) {
                String iuid = ApplicationContext.databaseRef.getInstanceUIDBasedOnSliceLocation(studyUID, imgPanel.seriesUID, ApplicationContext.layeredCanvas.imgpanel.getSliceLocation(), ApplicationContext.layeredCanvas.imgpanel.sliceThickness);
                if (iuid != null) {
                    int index = imgPanel.instanceUidList.indexOf(iuid);
                    if (!imgPanel.imgBuffer.isImageExists(index)) {
                        imgPanel.imgBuffer.clearBuffer();
                        imgPanel.imageUpdator.terminateThread();
                        imgPanel.imageUpdator = new ImageGenerator(imgPanel.imgBuffer, imgPanel, false);
                        imgPanel.imageUpdator.setParameters(index - 1, index + 24, true);
                        imgPanel.imageUpdator.start();
                        imgPanel.currentInstanceNo = index;
                        imgPanel.setImage(imgPanel.imgBuffer.get(imgPanel.currentInstanceNo));
                    } else {
                        imgPanel.currentInstanceNo = index;
                        imgPanel.setImage(imgPanel.imgBuffer.get(imgPanel.currentInstanceNo));
                    }
                }
            }
        }
    }

    public void storeAnnotation() { //Adds new instance's annotaion
        try {
            currentSeriesAnnotation.addAnnotation(currentInstanceNo, canvas.getLayeredCanvas().annotationPanel.getAnnotation());
        } catch (NullPointerException ex) {
            //ignore
        }
    }

    public void storeMultiframeAnnotation() { //Add new frame's annotation
        currentSeriesAnnotation.addMultiframeAnnotation(currentInstanceNo, canvas.getLayeredCanvas().annotationPanel.getAnnotation());
    }

    public void setCurrentSeriesAnnotation() { //Sets the annotation for first time
        currentSeriesAnnotation = ApplicationContext.imgView.selectedSeriesDisplays.get(ApplicationContext.imgView.selectedStudy).getStudyAnnotation().getSeriesAnnotation(seriesUID);
        if (currentSeriesAnnotation == null) {
            currentSeriesAnnotation = new SeriesAnnotations(seriesUID);
            ApplicationContext.imgView.selectedSeriesDisplays.get(ApplicationContext.imgView.selectedStudy).getStudyAnnotation().putSeriesAnnotation(seriesUID, currentSeriesAnnotation);
        }
        if (!multiframe) {
            canvas.getLayeredCanvas().annotationPanel.setAnnotation(currentSeriesAnnotation.getInstanceAnnotation(currentInstanceNo));
        } else {
            if (!currentSeriesAnnotation.isMultiframeAnnotationsExist()) {
                currentSeriesAnnotation.initializeMultiframeAnnotations();
            }
            canvas.getLayeredCanvas().annotationPanel.setAnnotation(currentSeriesAnnotation.getMultiframeAnnotation(currentInstanceNo));
        }
    }

    public void removeAllAnnotations() {
        if (!multiframe) {
            currentSeriesAnnotation.removeInstanceAnnotation(currentInstanceNo);
        } else {
            currentSeriesAnnotation.removeMultiframeAnnotation(currentInstanceNo);
        }
    }

    public void getFilePathsifLink() {
        if (ApplicationContext.databaseRef.isLink(studyUID)) {
            fileUrlsifLink = ApplicationContext.databaseRef.getInstancesLocation(studyUID, seriesUID);
            isLink = true;
        }
    }

    public void setImage(int instanceNumber) {
        currentInstanceNo = instanceNumber;
        currentbufferedimage = ApplicationContext.imgBuffer.get(currentInstanceNo);
        windowChanged(windowLevel, windowWidth);
        canvas.getLayeredCanvas().textOverlay.getTextOverlayParam().setCurrentInstance(currentInstanceNo);
        canvas.getLayeredCanvas().textOverlay.getTextOverlayParam().setSlicePosition(ApplicationContext.databaseRef.getSlicePosition(studyUID, seriesUID, instanceUidList.get(currentInstanceNo)));
        multiframe = canvas.getLayeredCanvas().textOverlay.getTextOverlayParam().isMultiframe();
        canvas.getLayeredCanvas().textOverlay.multiframeStatusDisplay(multiframe);
        instanceUID = instanceUidList.get(currentInstanceNo);
        dicomFileUrl = fileLocation + File.separator + instanceUID;
        centerImage();
    }

    public void displayImage(BufferedImage tempImage) {
        try {
            if (tempImage != null) {
                currentbufferedimage = tempImage;
                try {
                    if (cm != null) {
                        currentbufferedimage = new BufferedImage(cm, currentbufferedimage.getRaster(), false, null);
                    }
                } catch (Exception e) {
                    //ignore
                }
                image = currentbufferedimage;
                newBufferedImage = true;
                canvas.getLayeredCanvas().textOverlay.getTextOverlayParam().setCurrentInstance(currentInstanceNo);
                canvas.getLayeredCanvas().textOverlay.getTextOverlayParam().setSlicePosition(ApplicationContext.databaseRef.getSlicePosition(studyUID, seriesUID, instanceUidList.get(currentInstanceNo)));
                canvas.getLayeredCanvas().annotationPanel.setAnnotation(currentSeriesAnnotation.getInstanceAnnotation(currentInstanceNo));
                instanceUID = instanceUidList.get(currentInstanceNo);
                repaint();
            }
        } catch (NullPointerException ex) {
//            System.out.println("Null in setImageLayoutImage() : " + currentInstanceNo);
        }
    }

    public void doPan() {
        if (tool.equalsIgnoreCase("panning")) {
            tool = "";
        } else {
            tool = "panning";
        }
    }

    public void doWindowing() {
        if (tool.equalsIgnoreCase("windowing")) {
            tool = "";
        } else {
            tool = "windowing";
        }
    }

    public void doZoomIn() {
        scaleFlag = true;
        scaleFactor = scaleFactor + 0.5;
        displayZoomLevel();
        scaleProcess();
        canvas.repaint();
    }

    public void doZoomOut() {
        scaleFlag = true;
        scaleFactor = scaleFactor - 0.5;
        if (scaleFactor > 0) {
            scaleProcess();
        } else {
            scaleFactor = 0.5;
        }
        displayZoomLevel();
        canvas.repaint();
    }

    public void doStack() {
        if (tool.equalsIgnoreCase("stack")) {
            tool = "";
        } else {
            tool = "stack";
        }
    }

    public void setImageInfo(double pixelSpacingX, double pixelSpacingY, String studyUid, String seriesUid, String fileLocation, SeriesAnnotations currentSeriesAnnotation, ArrayList<String> instanceUidList, ColorModelParam cmParam, ColorModel cm, int windowLevel, int windowWidth, String modality, String studyDesc) {
        this.pixelSpacingX = pixelSpacingX;
        this.pixelSpacingY = pixelSpacingY;
        this.studyUID = studyUid;
        this.seriesUID = seriesUid;
        this.fileLocation = fileLocation;
        this.currentSeriesAnnotation = currentSeriesAnnotation;
        this.instanceUidList = instanceUidList;
        this.totalInstance = instanceUidList.size();
        this.cmParam = cmParam;
        this.cm = cm;
        this.windowLevel = WC = windowLevel;
        this.windowWidth = WW = windowWidth;
        this.modality = modality;
        this.studyDesc = studyDesc;
    }

    public TextOverlayParam getTextOverlayParam() {
        return textOverlayParam;
    }

    public void setTextOverlayParam(TextOverlayParam textOverlayParam) {
        this.textOverlayParam = textOverlayParam;
    }

    public boolean isMultiFrame() {
        return multiframe;
    }

    public String getDicomFileUrl() {
        return dicomFileUrl;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public String getStudyUID() {
        return studyUID;
    }

    public boolean isEncapsulatedDocument() {
        return isEncapsulatedDocument;
    }

    public BufferedImage getCurrentbufferedimage() {
        return currentbufferedimage;
    }

    public String getInstanceUID() {
        return instanceUID;
    }

    public String getSeriesUID() {
        return seriesUID;
    }

    public void setWindowingToolsAsDefault() {
        if (!(tool.equalsIgnoreCase("windowing"))) {
            tool = "windowing";
        }
    }

    public boolean isWindowingSelected() {
        return (tool.equalsIgnoreCase("windowing"));
    }

    public boolean isFlipHorizontalFlag() {
        return flipHorizontalFlag;
    }

    public boolean isFlipVerticalFlag() {
        return flipVerticalFlag;
    }

    public boolean isIsRotate() {
        return isRotate;
    }

    public int getRotateLeftAngle() {
        return rotateLeftAngle;
    }

    public int getRotateRightAngle() {
        return rotateRightAngle;
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public boolean isScaleFlag() {
        return scaleFlag;
    }

    public static boolean isProbeFlag() {
        return probeFlag;
    }

    public double getCurrentScaleFactor() {
        return currentScaleFactor;
    }

    public boolean isStackSelected() {
        return (tool.equalsIgnoreCase("stack"));
    }

    public void setToolsToNull() {
        tool = "";
    }

    public boolean isInvertFlag() {
        return invertFlag;
    }

    public double getPixelSpacingX() {
        return pixelSpacingX;
    }

    public double getPixelSpacingY() {
        return pixelSpacingY;
    }

    public int getWindowLevel() {
        return windowLevel;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public String getSliceLocation() {
        return currentScoutDetails.getSliceLocation();
    }

    public int getTotalInstance() {
        return totalInstance;
    }

    public void setCurrentInstanceNo(int currentInstanceNo) {
        this.currentInstanceNo = currentInstanceNo;
    }

    public static boolean isDisplayScout() {
        return displayScout;
    }

    public static void setDisplayScout(boolean displayScout) {
        ImagePanel.displayScout = displayScout;
    }

    public int getCurrentInstanceNo() {
        return currentInstanceNo;
    }

    public String getModality() {
        return modality;
    }

    public void setModality(String modality) {
        this.modality = modality;
    }

    public boolean isLocalizer() {
        return isLocalizer;
    }

    public String getFrameOfReferenceUID() {
        if (currentScoutDetails != null) {
            return currentScoutDetails.getImageFrameofReferenceUID();
        }
        return null;
    }

    public String getImageOrientation() {
        if (currentScoutDetails != null) {
            return currentScoutDetails.getImageOrientation();
        }
        return null;
    }

    public String getReferencedSOPInstanceUID() {
        if (currentScoutDetails != null) {
            return currentScoutDetails.getImageReferenceSOPInstanceUID();
        }
        return null;
    }

    public String getInstanceUidIfMultiframe() {
        return instanceUidIfMultiframe;
    }

    public ScoutLineInfoModel getCurrentScoutDetails() {
        return currentScoutDetails;
    }

    public void setCurrentScoutDetails(ScoutLineInfoModel currentScoutDetails) {
        this.currentScoutDetails = currentScoutDetails;
    }

    public SeriesAnnotations getCurrentSeriesAnnotation() {
        return currentSeriesAnnotation;
    }

    public ArrayList<String> getInstanceUidList() {
        return instanceUidList;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public ColorModelParam getCmParam() {
        return cmParam;
    }

    public ColorModel getCm() {
        return cm;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public String getStudyDesc() {
        return studyDesc;
    }
}