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
import in.raster.mayam.delegates.Buffer;
import in.raster.mayam.delegates.DicomImageReader;
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
import java.util.logging.Level;
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
public class ImagePanel extends javax.swing.JPanel {

    private LayeredCanvas layeredCanvas = null;
    //Image manipulation Flags
    public boolean isRotate = false, flipHorizontalFlag = false, flipVerticalFlag = false, zoomFlag = false;
    private boolean invertFlag = false;
    public int rotateRightAngle = 0;
    private int rotateLeftAngle = 0;
    boolean isPDF = false;
    //Windowing, Hu related variables
    private int windowLevel, windowWidth, WC, WW;
    private double pixelSpacingX, pixelSpacingY;
    //ImageIO variables
    private BufferedImage currentbufferedimage, image;
    ImageReader reader = null;
    //Mouse pointer variables
    private int mouseLocX1, mouseLocX2, mouseLocY1, mouseLocY2;
    //ColorModel variables
    private ColorModelParam cmParam = null;
    private static final ColorModelFactory cmFactory = ColorModelFactory.getInstance();
    private ColorModel cm = null;
    private String hu = "", dicomFileUrl;
    //Image related variables
    private int currentInstanceNo = 0, totalInstance;
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
    private int axis1LeftX, axis1LeftY, axis1RightX, axis1RightY, axis2LeftX, axis2LeftY, axis2RightX, axis2RightY, axisLeftX, axisLeftY, axisRightX, axisRightY;
    public static boolean synchornizeTiles = false;
    private PDFFile curFile = null;
    private int curpage = -1;
    long timeDelay = 0; //To synchronize the mouse scroll amount    
    ArrayList<String> instanceUidList;
    String fileLocation;
    SeriesAnnotations currentSeriesAnnotation = null;
    //added for mouse drag zooming 
    private double scale = 0.0;
    private int originX = 0, originY = 0;
    private Point mousePosition;
    public Buffer buffer = null;
    boolean isNormal = false;
    private Dataset dataset;
    public boolean setHints = true;
    int multiframePtr = 0;

    /**
     * Constructs the image panel by passing file url and outer canvas
     *
     * @param dicomFileUrl
     * @param canvas
     */
    public ImagePanel(File dicomFile, LayeredCanvas canvas) {
        this.dicomFileUrl = dicomFile.getAbsolutePath();
        this.layeredCanvas = canvas;
        readDicomFile(dicomFile);
        initComponents();
        setIsNormal();
    }

    public void createBuffer(int i) {
        if (!multiframe) {
            currentInstanceNo = i;
            if (i - 10 > 0) {
                buffer = new Buffer(this, i - 10);
                buffer.setStartBuffering(true);
            } else {
                buffer = new Buffer(this, i);
            }
        }
    }

    public ImagePanel(LayeredCanvas canvas) {
        this.layeredCanvas = canvas;
        initComponents();
    }

    /**
     * This routine used to retrieve the text overlay related information from
     * the dataset
     */
    private void retriveTextOverlayParam(Dataset dataset) {
        textOverlayParam = new TextOverlayParam();
        textOverlayParam.setPatientName(dataset.getString(Tags.PatientName));
        textOverlayParam.setPatientID(dataset.getString(Tags.PatientID));
        textOverlayParam.setSex(dataset.getString(Tags.PatientSex));
        textOverlayParam.setStudyDate(dataset.getDate(Tags.StudyDate));
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
        textOverlayParam.setIsMultiframe(multiframe);
    }

    /**
     * This routine used to retrieve some other tag information from the dataset
     */
    private void retrieveTagInfo(Dataset dataset) {
        try {
            instanceUidList = ApplicationContext.databaseRef.getInstanceUidList(dataset.getString(Tags.StudyInstanceUID), dataset.getString(Tags.SeriesInstanceUID));
            totalInstance = !isMultiFrame() ? instanceUidList.size() : totalInstance;
        } catch (NullPointerException e) {
            ApplicationContext.logger.log(Level.INFO, "Image Panel", e);
        }
    }

    private void retrieveScoutParam() {
        currentScoutDetails = ApplicationContext.databaseRef.getScoutLineDetails(dataset.getString(Tags.StudyInstanceUID), dataset.getString(Tags.SeriesInstanceUID), dataset.getString(Tags.SOPInstanceUID));
        isLocalizer = (currentScoutDetails.getImageType().equalsIgnoreCase("LOCALIZER")) ? true : false;
        findOrientation();
    }

    /**
     * This routine used to retrieve the instance related information
     */
    private void retrieveInstanceInformation(Dataset dataset) {
        String inverted = dataset.getString(Tags.PhotometricInterpretation, null);
        if ("MONOCHROME1".equals(inverted) || "MONOCHROME2".equals(inverted)) {
            cmParam = cmFactory.makeParam(dataset);
            int size = 1 << dataset.getInt(Tags.BitsStored, 8);
            int signed = dataset.getInt(Tags.PixelRepresentation, 0);
            int min = dataset.getInt(Tags.SmallestImagePixelValue, signed == 0 ? 0 : -(size >> 1));
            int max = dataset.getInt(Tags.LargestImagePixelValue, signed == 0 ? size - 1 : (size >> 1) - 1);
            int cMin = (int) cmParam.toMeasureValue(min);
            int cMax = (int) cmParam.toMeasureValue(max - 1);
            int wMax = cMax - cMin;
            int w = wMax;
            try {
                pixelSpacingY = Double.parseDouble(dataset.getString(Tags.PixelSpacing, 0));
                pixelSpacingX = Double.parseDouble(dataset.getString(Tags.PixelSpacing, 1));
            } catch (NullPointerException e) { //ignore
                ApplicationContext.logger.log(Level.INFO, "Image Panel - Unable to get Pixel spacing", e);
            }
            int nWindow = cmParam.getNumberOfWindows();
            if (nWindow > 0) {
                WC = windowLevel = (int) cmParam.getWindowCenter(0);
                WW = windowWidth = (int) cmParam.getWindowWidth(0);
            } else {
                WW = windowWidth = (int) Math.pow(2, dataset.getInt(Tags.BitsStored, 8));
                WC = windowLevel = (int) w / 2;
            }
        }
//        windowChanged(windowLevel, windowWidth);
    }

    /**
     * This routine used to read the dicom file
     *
     * @param selFile-This the file to be read
     */
    private void readDicomFile(File selFile) {
        try {
            fileLocation = selFile.getParent();
//            ImageIO.scanForPlugins();
            ImageInputStream iis = ImageIO.createImageInputStream(selFile);
            reader = (ImageReader) ImageIO.getImageReadersByFormatName("DICOM").next();
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
                        image = combineImages(currentbufferedimage, OverlayUtils.extractOverlay(obj, Tag.OverlayData, reader, "FFFFFF"));
                    } else {
                        image = currentbufferedimage;
                    }
                }
                totalInstance = reader.getNumImages(true);
                if (totalInstance - 1 > 0) {
                    multiframe = true;
                }
                if (dataset.getString(Tags.SOPClassUID) != null && dataset.getString(Tags.SOPClassUID).equalsIgnoreCase("1.2.840.10008.5.1.4.1.1.104.1")) {
                    isPDF = true;
                    readDicom(selFile);
                }
                retrieveTagInfo(dataset);
                retrieveInstanceInformation(dataset);
                retrieveScoutParam();
                retriveTextOverlayParam(dataset);
                repaint();
            } catch (RuntimeException e) {
                ApplicationContext.logger.log(Level.SEVERE, "Image Panel", e);
            }
        } catch (Exception e) {
            ApplicationContext.logger.log(Level.SEVERE, "Image Panel", e);
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
            ApplicationContext.logger.log(Level.SEVERE, "Image Panel", ex);
        }
    }

    private void setIsNormal() {
        if (!multiframe && !isEncapsulatedDocument && !isPDF) {
            isNormal = true;
        }
    }

    public void setIsNormal(boolean normal) {
        isNormal = normal;
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
        originX = (maxWidth - thumbWidth) / 2;
        originY = (maxHeight - thumbHeight) / 2;
    }

    public void showNextFrame() {
        storeMultiframeAnnotation();
        currentInstanceNo++;
        if (currentInstanceNo == totalInstance) {
            if (ApplicationContext.isLoopBack) {
                currentInstanceNo = 0;
            } else {
                ((ViewerJPanel) layeredCanvas.getParent().getParent().getParent().getParent()).stopAutoPlay();
            }
        } else {
            layeredCanvas.textOverlay.getTextOverlayParam().setCurrentInstance(currentInstanceNo);
            displayFrames();
        }
    }

    public void showPreviousFrame() {
        storeMultiframeAnnotation();
        if (currentInstanceNo > 0) {
            currentInstanceNo--;
            layeredCanvas.textOverlay.getTextOverlayParam().setCurrentInstance(currentInstanceNo);
            displayFrames();
        } else if (ApplicationContext.isLoopBack) {
            currentInstanceNo = totalInstance;
        }
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
    public boolean negative() {
        convertToRGBImage();
        if (invertFlag) {
            invertFlag = false;
        } else {
            invertFlag = true;
            invert();
        }
        repaint();
        return invertFlag;
    }

    public void invert() {
        byte negative[] = new byte[256];
        for (int i = 0; i < 256; i++) {
            negative[i] = (byte) (255 - i);
        }
        ByteLookupTable table = new ByteLookupTable(0, negative);
        LookupOp op = new LookupOp(table, null);
        filter(op);
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
        layeredCanvas.textOverlay.repaint();
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
        layeredCanvas.textOverlay.repaint();
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
        flipVerticalFlag = flipVerticalFlag ? false : true;
    }

    private void flipH() {
        flipHorizontalFlag = flipHorizontalFlag ? false : true;
    }

    public void reset() {
        layeredCanvas.annotationPanel.resetAnnotation();
        layeredCanvas.annotationPanel.resetMeasurements();
        layeredCanvas.annotationPanel.clearAllMeasurement();
        windowLevel = (int) WC;
        windowWidth = (int) WW;
        mousePosition = null;
        initializeParams();
        invertFlag = flipHorizontalFlag = flipVerticalFlag = isRotate = displayScout = synchornizeTiles = false;
        rotateLeftAngle = rotateRightAngle = 0;
        windowChanged(windowLevel, windowWidth);
        LocalizerDelegate.hideAllScoutLines((JPanel) layeredCanvas.getParent().getParent());
        layeredCanvas.annotationPanel.resetAnnotaionTools();
        ((ViewerJPanel) ApplicationContext.tabbedPane.getSelectedComponent()).setWindowing();
        repaint();
    }

    public void resetWindowing() {
        windowChanged(WC, WW);
    }

    public void resizeHandler() {
        repaint();
        centerImage();
    }

    /**
     * This override routine used to paint the image box
     *
     * @param gs
     */
    @Override
    public void paintComponent(Graphics gs) {
        Graphics2D g = (Graphics2D) gs;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (setHints) {
//            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);            
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        if (flipHorizontalFlag) {
            g.translate(getWidth(), 0);
            g.scale(-1, 1);
        }
        if (flipVerticalFlag) {
            g.translate(0, getHeight());
            g.scale(1, -1);
        }
        if (isRotate) {
            switch (rotateRightAngle) {
                case 90:
                    g.rotate(Math.PI / 2, getWidth() / 2, getHeight() / 2);
                    break;
                case 180:
                    g.rotate(Math.PI, getWidth() / 2, getHeight() / 2);
                    break;
                case 270:
                    g.rotate((Math.PI * 3) / 2, getWidth() / 2, getHeight() / 2);
                    break;
            }
        }
        g.translate(originX, originY);
        g.scale(scale, scale);
        if (image != null) {
            g.drawImage(image, 0, 0, null);
        }
        //Scout Lines
        if (displayScout && isLocalizer) {
            if (dataset.getString(Tags.Modality).equals("MR") || (dataset.getString(Tags.Modality).equals("CT"))) {
//            if (modality.equals("MR") || (modality.equals("CT"))) {
                g.setColor(Color.YELLOW);
                if (orientationLabel.equalsIgnoreCase("SAGITTAL")) {
                    if (dataset.getString(Tags.Modality).equals("CT") || slope1 == slope2) {
//                        g.drawLine((int) (boundaryLine1X1 + (originX / scale)), (int) (boundaryLine1Y1 + (originY / scale)), (int) (boundaryLine1X2 + (originX / scale)), (int) (boundaryLine1Y2 + (originY / scale)));
//                        g.drawLine((int) (boundaryLine2X1 + (originX / scale)), (int) (boundaryLine2Y1 + (originY / scale)), (int) (boundaryLine2X2 + (originX / scale)), (int) (boundaryLine2Y2 + (originY / scale)));
                        g.drawLine(boundaryLine1X1, boundaryLine1Y1, boundaryLine1X2, boundaryLine1Y2);
                        g.drawLine(boundaryLine2X1, boundaryLine2Y1, boundaryLine2X2, boundaryLine2Y2);
                    }
                    g.setColor(Color.GREEN);
//                    g.drawLine((int) (scoutLine1X1 + (originX / scale)), (int) (scoutLine1Y1 + (originY / scale)), (int) (scoutLine1X2 + (originX / scale)), (int) (scoutLine1Y2 + (originY / scale)));
//                    g.drawLine((int) (scoutLine2X1 + (originX / scale)), (int) (scoutLine2Y1 + (originY / scale)), (int) (scoutLine2X2 + (originX / scale)), (int) (scoutLine2Y2 + (originY / scale)));
                    g.drawLine(scoutLine1X1, scoutLine1Y1, scoutLine1X2, scoutLine1Y2);
                    g.drawLine(scoutLine2X1, scoutLine2Y1, scoutLine2X2, scoutLine2Y2);
                } else if (orientationLabel.equalsIgnoreCase("CORONAL")) {
                    if (dataset.getString(Tags.Modality).equals("CT") || slope1 == slope2) {
//                        g.drawLine((int) (axis1LeftX + (originX / scale)), (int) (axis1LeftY + (originY / scale)), (int) (axis1RightX + (originX / scale)), (int) (axis1RightY + (originY / scale)));
//                        g.drawLine((int) (axis2LeftX + (originX / scale)), (int) (axis2LeftY + (originY / scale)), (int) (axis2RightX + (originX / scale)), (int) (axis2RightY + (originY / scale)));
                        g.drawLine(axis1LeftX, axis1LeftY, axis1RightX, axis1RightY);
                        g.drawLine(axis2LeftX, axis2LeftY, axis2RightX, axis2RightY);
                    }
                    g.setColor(Color.GREEN);
//                    g.drawLine((int) (axisLeftX + (originX / scale)), (int) (axisLeftY + (originY / scale)), (int) (axisRightX + (originX / scale)), (int) (axisRightY + (originY / scale)));
                    g.drawLine(axisLeftX, axisLeftY, axisRightX, axisRightY);
                }
            }
        }
        g.dispose();
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
        int currentZoomLevel = (int) Math.floor(scale * 100);
        layeredCanvas.textOverlay.getTextOverlayParam().setZoomLevel(ApplicationContext.currentBundle.getString("ImageView.textOverlay.zoomLabel.text") + currentZoomLevel + "%");
//        layeredCanvas.textOverlay.getTextOverlayParam().setImageSize((image.getWidth() + "x" + image.getHeight()));
        layeredCanvas.textOverlay.getTextOverlayParam().setImageSize(dataset.getString(Tags.Rows) + "x" + dataset.getString(Tags.Columns));
        layeredCanvas.textOverlay.getTextOverlayParam().setViewSize(getWidth() + "x" + getHeight());
    }

    public void calculateCurrentScaleFactor() {
//        double imageWidth = image.getWidth();
//        double imageHeight = image.getHeight();
        double imageWidth = Double.parseDouble(dataset.getString(Tags.Rows));
        double imageHeight = Double.parseDouble(dataset.getString(Tags.Columns));
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
        slope1 = findSlope(boundaryLine1X1 * currentScaleFactor + originX, boundaryLine1Y1 * currentScaleFactor + originY, boundaryLine1X2 * currentScaleFactor + originX, boundaryLine1Y2 * currentScaleFactor + originY);
    }

    public void setScoutBorder2Coordinates(int line1X1, int line1Y1, int line1X2, int line1Y2) {
        boundaryLine2X1 = line1X1;
        boundaryLine2X2 = line1X2;
        boundaryLine2Y1 = line1Y1;
        boundaryLine2Y2 = line1Y2;
        slope2 = findSlope(boundaryLine2X1 * currentScaleFactor + originX, boundaryLine2Y1 * currentScaleFactor + originY, boundaryLine2X2 * currentScaleFactor + originX, boundaryLine2Y2 * currentScaleFactor + originY);
    }

    public void setAxis1Coordinates(int leftx, int lefty, int rightx, int righty, int topx, int topy, int bottomx, int bottomy) {
        axis1LeftX = leftx;
        axis1LeftY = lefty;
        axis1RightX = rightx;
        axis1RightY = righty;
        slope1 = findSlope(axis1LeftX, axis1LeftY, axis1RightX, axis1RightY);
    }

    public void setAxis2Coordinates(int leftx, int lefty, int rightx, int righty, int topx, int topy, int bottomx, int bottomy) {
        axis2LeftX = leftx;
        axis2LeftY = lefty;
        axis2RightX = rightx;
        axis2RightY = righty;
        slope2 = findSlope(axis2LeftX, axis2LeftY, axis2RightX, axis2RightY);
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
        if (currentbufferedimage != null) {
            ImageIcon imageIcon = new ImageIcon();
            imageIcon.setImage(currentbufferedimage);
            Image loadedImage = imageIcon.getImage();
            image = new BufferedImage(loadedImage.getWidth(null), loadedImage.getHeight(null), BufferedImage.TYPE_INT_BGR);
            Graphics2D g2 = image.createGraphics();
            g2.drawImage(loadedImage, 0, 0, null);
            g2.dispose();
        }
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
            if (invertFlag) {
                invert();
            }
            layeredCanvas.textOverlay.setWindowingParameter(Integer.toString(windowLevel), Integer.toString(windowWidth));
            repaint();
        } catch (Exception e) {
            ApplicationContext.logger.log(Level.INFO, "Image Panel - Unable to apply windowing", e);
        }
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
    }//GEN-LAST:event_formMouseClicked

    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getWheelRotation() < 0) {
            doPrevious();
        } else {
            doNext();
        }
    }

    public void doPrevious() {
        if (isNormal) {
            previous();
        } else {
            JPanel panel = (JPanel) layeredCanvas.getParent();
            if (panel.getComponentCount() > 1) {
                if (System.currentTimeMillis() - timeDelay > panel.getComponentCount() * 10) {
                    reverseMultipleImages(panel);
                    timeDelay = System.currentTimeMillis();
                }
            } else if (multiframe) {
                showPreviousFrame();
            } else if (isEncapsulatedDocument) {
                if (isPDF) {
                    forceGotoPage(curpage - 1);
                }
            }
        }
    }

    public void doNext() {
        if (isNormal) {
            next();
        } else {
            JPanel panel = (JPanel) layeredCanvas.getParent();
            if (panel.getComponentCount() > 1) {
                if (System.currentTimeMillis() - timeDelay > panel.getComponentCount() * 10) {
                    forwardMultipleImages(panel);
                    timeDelay = System.currentTimeMillis();
                }
            } else if (multiframe) {
                showNextFrame();
            } else if (isEncapsulatedDocument) {
                if (isPDF) {
                    forceGotoPage(curpage + 1);
                }
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2 && !((LayeredCanvas) getParent().getParent()).annotationPanel.isAnnotationEnabled()) {
            if (scale == getOriginalScaleFacotor()) {
                pixelMapping();
            } else {
                initializeParams();
            }

        }
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
            ArrayList presetList = ApplicationContext.databaseRef.getPresetsForModality(dataset.getString(Tags.Modality));
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
                                JPanel currentPanel = (JPanel) layeredCanvas.getParent();
                                try {
                                    for (int i = 0; i < currentPanel.getComponentCount(); i++) {
                                        ((LayeredCanvas) currentPanel.getComponent(i)).imgpanel.windowChanged(Integer.parseInt(presetModel.getWindowLevel()), Integer.parseInt(presetModel.getWindowWidth()));
                                    }
                                } catch (NullPointerException npe) {
                                    ApplicationContext.logger.log(Level.INFO, "Image Panel - No components in tile", e);
                                }
                            }
                        });
                    }
                }
                jPopupMenu1.add(menu);
            }
        }
    }

    public void mouseDragWindowing(int mouseLocX2, int mouseLocY2) {
        int mouseLocDiffX = (int) ((mouseLocX2 - mouseLocX1));
        int mouseLocDiffY = (int) ((mouseLocY1 - mouseLocY2));
        mouseLocX1 = mouseLocX2;
        mouseLocY1 = mouseLocY2;
        double newWindowWidth = windowWidth + mouseLocDiffX * 2;
//        double newWindowWidth = windowWidth + mouseLocDiffX;
        if (newWindowWidth < 1.0) {
            newWindowWidth = 1.0;
        }
        double newWindowLevel = windowLevel + mouseLocDiffY * 2;
//        double newWindowLevel = windowLevel + mouseLocDiffY;
        JPanel currentSeriesPanel = (JPanel) layeredCanvas.getParent();
        for (int i = 0; i < currentSeriesPanel.getComponentCount(); i++) {
            ((LayeredCanvas) currentSeriesPanel.getComponent(i)).imgpanel.windowChanged((int) newWindowLevel, (int) newWindowWidth);
        }
    }

    public void mouseDragStack(int mouseLocX2, int mouseLocY2) {
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

    public void mouseDragZoom(int mouseLocX2, int mouseLocY2) {
        this.mouseLocX2 = mouseLocX2;
        this.mouseLocY2 = mouseLocY2;

        int mouseLocDiffY = (int) ((mouseLocY2 - mouseLocY1));
        if (mouseLocDiffY < 0) {
            zoomImage(0.2);
        } else if (scale > 0.2) {
            zoomImage(-0.2);
        }
        mouseLocX1 = mouseLocX2;
        mouseLocY1 = mouseLocY2;
    }

    public void setImage(String sliceLocation, boolean isForward, String sliceThickness) {
        /**
         * This method has been added for synchronized scroll it will not
         * support for multiframe sync scroll. so that instance number can be
         * set directly to instanceNumber and currentInstanceNo variable.
         */
        storeAnnotation();
        String iuid = ApplicationContext.databaseRef.getInstanceUIDBasedOnSliceLocation(dataset.getString(Tags.StudyInstanceUID), dataset.getString(Tags.SeriesInstanceUID), sliceLocation, sliceThickness);
        if (iuid != null) {
            currentInstanceNo = instanceUidList.indexOf(iuid);
            if (buffer.isImageExist(currentInstanceNo)) {
                setImage(buffer.getImage(currentInstanceNo));
            } else {
                setImage(DicomImageReader.readDicomFile(new File(fileLocation + File.separator + instanceUidList.get(currentInstanceNo))));
            }
            buffer.update(currentInstanceNo);
        }
    }

    public ScoutLineInfoModel[] prepareScoutBorder() {
        return ApplicationContext.databaseRef.getFirstAndLastInstances(dataset.getString(Tags.StudyInstanceUID), dataset.getString(Tags.SeriesInstanceUID));
    }

    private String calculateHU(int x, int y) {
        try {
            int[] pixelValueArray = currentbufferedimage.getSampleModel().getPixel(x, y, (int[]) null, currentbufferedimage.getRaster().getDataBuffer());
            int pixelValue = pixelValueArray[0];
            try {
                hu = Integer.toString(pixelValue * Integer.parseInt(dataset.getString(Tags.RescaleSlope)) + Integer.parseInt(dataset.getString(Tags.RescaleIntercept)));
//                hu = Integer.toString(pixelValue * Integer.parseInt(rescaleSlope) + Integer.parseInt(rescaleIntercept));
            } catch (Exception e) {
                hu = Integer.toString(pixelValue * 1 - 1024);
            }
        } catch (Exception e) {
            ApplicationContext.logger.log(Level.INFO, "Image Panel - Unable to calculate HU", e);
        }
        return hu;
    }

    public double calculateMean(int x, int y, int width, int height) {
        try {
            int sum = 0;
            int pixelCount = 0;
            int[] pixelValueArray = currentbufferedimage.getSampleModel().getPixels(x, y, width, height, (int[]) null, currentbufferedimage.getRaster().getDataBuffer());
            for (int i = 0; i < pixelValueArray.length; i++) {
                ++pixelCount;
                try {
                    sum += pixelValueArray[i] * Integer.parseInt(dataset.getString(Tags.RescaleSlope)) + Integer.parseInt(dataset.getString(Tags.RescaleIntercept));
//                    sum += pixelValueArray[i] * Integer.parseInt(rescaleSlope) + Integer.parseInt(rescaleIntercept);
                } catch (Exception e) {
                    sum += pixelValueArray[i] * 1 - 1024;
                }
            }
            if (pixelCount == 0) {
                return 0;
            }
            return (double) sum / pixelCount;
        } catch (Exception e) {
            return 0;
        }
    }

    public double getPixelAt(int x, int y) {
        int[] pixel = currentbufferedimage.getSampleModel().getPixel(x, y, (int[]) null, currentbufferedimage.getRaster().getDataBuffer());
        int hu = 0;
        try {
            hu = pixel[0] * Integer.parseInt(dataset.getString(Tags.RescaleSlope)) + Integer.parseInt(dataset.getString(Tags.RescaleIntercept));
//            hu = pixel[0] * Integer.parseInt(rescaleSlope) + Integer.parseInt(rescaleIntercept);
        } catch (Exception e) {
            hu = pixel[0] * 1 - 1024;
        }
        return hu;
    }

    public double calculateStandardDeviation(double mean, int x, int y, int width, int height) {
        try {
            double sum = 0;
            int pixelCount = 0;
            int[] pixelValueArray = currentbufferedimage.getSampleModel().getPixels(x, y, width, height, (int[]) null, currentbufferedimage.getRaster().getDataBuffer());
            for (int i = 0; i < pixelValueArray.length; i++) {
                ++pixelCount;
                int value;
                try {
                    value = pixelValueArray[i] * Integer.parseInt(dataset.getString(Tags.RescaleSlope)) + Integer.parseInt(dataset.getString(Tags.RescaleIntercept));
//                    value = pixelValueArray[i] * Integer.parseInt(rescaleSlope) + Integer.parseInt(rescaleIntercept);
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

    public void mouseMoved(MouseEvent e) {
        mousePosition = e.getPoint();
        mouseLocX2 = e.getX();
        mouseLocY2 = e.getY();
        if (((ViewerJPanel) ApplicationContext.tabbedPane.getSelectedComponent()).getTool().equals("probe")) {

//        if (tool.equalsIgnoreCase("probe")) {
            String probeParameter[] = new String[3];
            probeParameter[0] = Integer.toString((int) Math.round((mouseLocX2 - originX) / scale));
            probeParameter[1] = Integer.toString((int) Math.round((mouseLocY2 - originY) / scale));
            probeParameter[2] = calculateHU((int) Math.round((mouseLocX2 - originX) / scale), (int) Math.round((mouseLocY2 - originY) / scale));
            layeredCanvas.textOverlay.setProbeParameters(probeParameter);
            probeParameter = null;
        }
    }

    private void designContext() {
        ArrayList<Series> seriesList = ApplicationContext.databaseRef.getSeriesList_SepMulti(dataset.getString(Tags.StudyInstanceUID));
        JMenu menu;
        if ((dataset.getString(Tags.StudyDescription) == null) || (dataset.getString(Tags.StudyDescription).equalsIgnoreCase(""))) {
            menu = new JMenu(dataset.getString(Tags.StudyInstanceUID));
        } else {
            menu = new JMenu(dataset.getString(Tags.StudyDescription));
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
                    if (((JPanel) layeredCanvas.getParent()).getComponentCount() == 1) {
                        changeSeries(series.getStudyInstanceUID(), series.getSeriesInstanceUID(), null, 0);
                    } else {
                        changeSeries(series.getStudyInstanceUID(), series.getSeriesInstanceUID(), null, 0, (JPanel) layeredCanvas.getParent());
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
        synchornizeTiles = (synchornizeTiles || layeredCanvas.getParent().getParent().getComponentCount() == 1 || !dataset.getString(Tags.Modality).contains("CT")) ? false : true;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPopupMenu jPopupMenu1;
    // End of variables declaration//GEN-END:variables

    //To change the series by choosing from the preview panel
    public void changeSeries(String studyUID, String seriesUID, String sopUid, int instanceNo) {
        if (!multiframe) {
            storeAnnotation();
        } else {
            storeMultiframeAnnotation();
        }
        SeriesChooserDelegate seriesChooserDelegate = new SeriesChooserDelegate(studyUID, seriesUID, sopUid, layeredCanvas, instanceNo);
    }

    //To change series in case of image layout
    public void changeSeries(String studyUID, String seriesUID, String sopUid, int instanceNumber, JPanel panel) {
        SeriesChooserDelegate seriesChooserDelegate = new SeriesChooserDelegate(studyUID, seriesUID, sopUid, instanceNumber, panel);
    }

    public void displayFrames() {
        try {
            currentbufferedimage = reader.read(currentInstanceNo);
            convertToRGBImage();
            layeredCanvas.annotationPanel.setAnnotation(currentSeriesAnnotation.getMultiframeAnnotation(currentInstanceNo));
            repaint();
        } catch (Exception ex) {
            ApplicationContext.logger.log(Level.INFO, "Image Panel", ex);
        }
    }

    public void displayFrame(int i) {
        this.currentInstanceNo = i;
        layeredCanvas.textOverlay.getTextOverlayParam().setCurrentInstance(currentInstanceNo);
        displayFrames();
    }

    public void forwardMultipleImages(JPanel panel) {
        int lastInstanceNumber = ((LayeredCanvas) panel.getComponent(0)).imgpanel.currentInstanceNo;
        int tiles = panel.getComponentCount();
        if (totalInstance > ApplicationContext.buffer.getDefaultBufferSize()) {
            ApplicationContext.buffer.clearTo(lastInstanceNumber);
            ApplicationContext.buffer.updateFrom(lastInstanceNumber + tiles + tiles - 1);
        }
        lastInstanceNumber += tiles + 1;
        if (lastInstanceNumber <= totalInstance) {
            int x = lastInstanceNumber % tiles;
            if (x > 0) {
                displayImages(panel, lastInstanceNumber - x, false);
            } else {
                displayImages(panel, lastInstanceNumber - tiles, false);
            }
        }
    }

    public void reverseMultipleImages(JPanel panel) {
        int lastInstanceNumber = ((LayeredCanvas) panel.getComponent(0)).imgpanel.currentInstanceNo;
        int tiles = panel.getComponentCount();
        if (totalInstance > ApplicationContext.buffer.getDefaultBufferSize()) {
            ApplicationContext.buffer.clearFrom(lastInstanceNumber + tiles);
            ApplicationContext.buffer.updateFrom(lastInstanceNumber - (tiles + tiles) - 1);
        }
        if (lastInstanceNumber != 0) {
            int x = lastInstanceNumber % tiles;
            if (x > 0) {
                displayImages(panel, lastInstanceNumber - x, false);
            } else {
                displayImages(panel, lastInstanceNumber - tiles, false);
            }
        }
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
                        if (tempCanvas.imgpanel != null && tempCanvas.imgpanel.dataset.getString(Tags.SeriesInstanceUID).equals(dataset.getString(Tags.SeriesInstanceUID))) {
                            tempCanvas.imgpanel.currentInstanceNo = instanceNumber;
                            tempCanvas.textOverlay.updateCurrentInstanceNo(instanceNumber);
                            if (!waitForImages) {
                                tempCanvas.imgpanel.setImage(ApplicationContext.buffer.getImage(instanceNumber));
                            } else {
                                tempCanvas.imgpanel.setImage(ApplicationContext.buffer.waitAndGet(instanceNumber));
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
            ((ViewerJPanel) ApplicationContext.tabbedPane.getSelectedComponent()).setSeriesIdentification();
        }
    }

    public void selectImage(int instanceNumber) {
        storeAnnotation();
        currentInstanceNo = instanceNumber;
        if (buffer.getDefaultBufferSize() < totalInstance) {
            if (buffer.isImageExist(instanceNumber)) {
                setImage(buffer.getImage(instanceNumber));
            } else {
                setImage(DicomImageReader.readDicomFile(new File(fileLocation + File.separator + instanceUidList.get(instanceNumber))));
            }
            buffer.update(instanceNumber);
        } else {
            setImage(buffer.getImage(instanceNumber));
        }

        if (synchornizeTiles && dataset.getString(Tags.Modality).contains("CT") && !multiframe) {
            doTileSync(true);
        }
        ((ViewerJPanel) ApplicationContext.tabbedPane.getSelectedComponent()).setSeriesIdentification();
        layeredCanvas.annotationPanel.setAnnotation(currentSeriesAnnotation.getInstanceAnnotation(currentInstanceNo));
    }

    public void doTileSync(boolean isForward) {
        JPanel outerComponent = (JPanel) layeredCanvas.getParent().getParent();
        for (int i = 0; i < outerComponent.getComponentCount(); i++) {
            try {
                LayeredCanvas tempCanvas = (LayeredCanvas) ((JPanel) outerComponent.getComponent(i)).getComponent(0);
                if (tempCanvas.imgpanel != this) {
                    tempCanvas.imgpanel.setImage(currentScoutDetails.getSliceLocation(), isForward, (dataset.getString(Tags.SliceThickness) != null) ? dataset.getString(Tags.SliceThickness) : "");
                }
            } catch (Exception ex) {
                ApplicationContext.logger.log(Level.INFO, "Image Panel - Unable to synchronize", ex);
            }
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

    public synchronized void next() {
        if (currentInstanceNo + 1 < totalInstance) {
            storeAnnotation();
            currentInstanceNo++;
            layeredCanvas.textOverlay.getTextOverlayParam().setCurrentInstance(currentInstanceNo);
            setImage(buffer.getForward(currentInstanceNo));
            if (currentInstanceNo == 10) {
                buffer.setStartBuffering(true);
            }
            if (!multiframe && synchornizeTiles && dataset.getString(Tags.Modality).startsWith("CT")) {
                doTileSync(true);
            }
            ((ViewerJPanel) ApplicationContext.tabbedPane.getSelectedComponent()).setSeriesIdentification();
        } else if (ApplicationContext.isLoopBack) {
            storeAnnotation();
            currentInstanceNo = -1;
            buffer.forwardLoopBack();
        }
    }

    public synchronized void previous() {
        if (currentInstanceNo > 0) {
            storeAnnotation();
            currentInstanceNo--;
            layeredCanvas.textOverlay.getTextOverlayParam().setCurrentInstance(currentInstanceNo);
            setImage(buffer.getBackward(currentInstanceNo));
            if (!multiframe && synchornizeTiles && dataset.getString(Tags.Modality).startsWith("CT")) {
                doTileSync(false);
            }
            ((ViewerJPanel) ApplicationContext.tabbedPane.getSelectedComponent()).setSeriesIdentification();
        } else if (ApplicationContext.isLoopBack) {
            storeAnnotation();
            currentInstanceNo = totalInstance;
            buffer.getBackwardLoopBack();
        }
    }

    public synchronized void showImage(int i) {
        storeAnnotation();
        this.currentInstanceNo = i;
        if (currentInstanceNo == 10) {
            buffer.setStartBuffering(true);
        }
        layeredCanvas.textOverlay.getTextOverlayParam().setCurrentInstance(currentInstanceNo);
        setImage(buffer.getForward(currentInstanceNo));
        ((ViewerJPanel) ApplicationContext.tabbedPane.getSelectedComponent()).setSeriesIdentification();
    }

    private void setImage(BufferedImage tempImage) {
        if (tempImage != null) {
            currentbufferedimage = tempImage;
            try {
                if (cm != null) {
                    currentbufferedimage = new BufferedImage(cm, currentbufferedimage.getRaster(), false, null);
                    convertToRGBImage();
                    if (invertFlag) {
                        invert();
                    }
                } else {
                    image = currentbufferedimage;
                }
            } catch (Exception e) {
                ApplicationContext.logger.log(Level.INFO, "Image Panel", e);
                image = currentbufferedimage;
            }
            updateCurrentInstance();
            repaint();
        }
    }

    public void updateCurrentInstance() {
        if (!multiframe) {
            currentScoutDetails = ApplicationContext.databaseRef.getScoutLineDetails(dataset.getString(Tags.StudyInstanceUID), dataset.getString(Tags.SeriesInstanceUID), instanceUidList.get(currentInstanceNo));
            if (displayScout) {
                isLocalizer = (currentScoutDetails.getImageType().equalsIgnoreCase("LOCALIZER")) ? true : false;
                if (isLocalizer) {
                    findOrientation();
                }
            }
            try {
                layeredCanvas.annotationPanel.setAnnotation(currentSeriesAnnotation.getInstanceAnnotation(currentInstanceNo));
            } catch (NullPointerException ex) {
                ApplicationContext.logger.log(Level.INFO, "Image Paenl - Unable to update current instance", ex);
            }
            layeredCanvas.textOverlay.getTextOverlayParam().setSlicePosition(currentScoutDetails.getSliceLocation());
        }
    }

    public void updateTextOverlay() {
        layeredCanvas.textOverlay.getTextOverlayParam().setCurrentInstance(currentInstanceNo);

    }

    public void storeAnnotation() { //Adds new instance's annotaion
        try {
            currentSeriesAnnotation.addAnnotation(currentInstanceNo, layeredCanvas.annotationPanel.getAnnotation());
        } catch (NullPointerException ex) {
            ApplicationContext.logger.log(Level.INFO, "Image Panel - No annotation present", ex);
            //ignoreNull pointer exception occurs if there is no annotaion for instance
        }
    }

    public void storeMultiframeAnnotation() { //Add new frame's annotation
        currentSeriesAnnotation.addMultiframeAnnotation(currentInstanceNo, layeredCanvas.annotationPanel.getAnnotation());
    }

    public void storeAllAnnotations() {
        try {
            currentSeriesAnnotation.addAnnotation(currentInstanceNo, layeredCanvas.annotationPanel.getAnnotation());
        } catch (NullPointerException ex) {
            ApplicationContext.logger.log(Level.INFO, "Image Panel - No Annotation present", ex);
            //ignoreNull pointer exception occurs if there is no annotaion for instance
        }
        if (multiframe) {
            currentSeriesAnnotation.addMultiframeAnnotation(currentInstanceNo, layeredCanvas.annotationPanel.getAnnotation());
        }
    }

    public void setCurrentSeriesAnnotation() { //Sets the annotation for first time
        currentSeriesAnnotation = ((ViewerJPanel) layeredCanvas.getParent().getParent().getParent().getParent()).getSeriesAnnotaions(dataset.getString(Tags.SeriesInstanceUID));
        if (!multiframe) {
            layeredCanvas.annotationPanel.setAnnotation(currentSeriesAnnotation.getInstanceAnnotation(currentInstanceNo));
        } else {
            if (!currentSeriesAnnotation.isMultiframeAnnotationsExist()) {
                currentSeriesAnnotation.initializeMultiframeAnnotations();
            }
            layeredCanvas.annotationPanel.setAnnotation(currentSeriesAnnotation.getMultiframeAnnotation(currentInstanceNo));
        }
    }

    public void removeAllAnnotations() {
        if (!multiframe) {
            currentSeriesAnnotation.removeInstanceAnnotation(currentInstanceNo);
        } else {
            currentSeriesAnnotation.removeMultiframeAnnotation(currentInstanceNo);
        }
    }

    public void setImage(int instanceNumber) {
        currentInstanceNo = instanceNumber;
        currentbufferedimage = ApplicationContext.buffer.waitAndGet(currentInstanceNo);
        windowChanged(windowLevel, windowWidth);
        layeredCanvas.textOverlay.getTextOverlayParam().setCurrentInstance(currentInstanceNo);
        layeredCanvas.textOverlay.getTextOverlayParam().setSlicePosition(ApplicationContext.databaseRef.getSlicePosition(dataset.getString(Tags.StudyInstanceUID), dataset.getString(Tags.SeriesInstanceUID), instanceUidList.get(currentInstanceNo)));
        multiframe = layeredCanvas.textOverlay.getTextOverlayParam().isMultiframe();
        layeredCanvas.textOverlay.multiframeStatusDisplay(multiframe);
        dicomFileUrl = fileLocation + File.separator + instanceUidList.get(currentInstanceNo);
        initializeParams();
    }

    public void setImageInfo(double pixelSpacingX, double pixelSpacingY, String studyUid, String seriesUid, String fileLocation, SeriesAnnotations currentSeriesAnnotation, ArrayList<String> instanceUidList, ColorModelParam cmParam, ColorModel cm, int windowLevel, int windowWidth, String modality, String studyDesc, ScoutLineInfoModel currentScoutDetails) {
        this.pixelSpacingX = pixelSpacingX;
        this.pixelSpacingY = pixelSpacingY;
        this.fileLocation = fileLocation;
        this.currentSeriesAnnotation = currentSeriesAnnotation;
        this.instanceUidList = instanceUidList;
        this.totalInstance = instanceUidList.size();
        this.cmParam = cmParam;
        this.cm = cm;
        this.windowLevel = WC = windowLevel;
        this.windowWidth = WW = windowWidth;
        this.currentScoutDetails = currentScoutDetails;
    }

    public void setImageInfo(double pixelSpacingX, double pixelSpacingY, String fileLocation, SeriesAnnotations currentSeriesAnnotation, ArrayList<String> instanceUidList, ColorModelParam cmParam, ColorModel cm, int windowLevel, int windowWidth, ScoutLineInfoModel currentScoutDetails, Dataset dataset) {
        this.pixelSpacingX = pixelSpacingX;
        this.pixelSpacingY = pixelSpacingY;
        this.fileLocation = fileLocation;
        this.currentSeriesAnnotation = currentSeriesAnnotation;
        this.instanceUidList = instanceUidList;
        this.totalInstance = instanceUidList.size();
        this.cmParam = cmParam;
        this.cm = cm;
        this.windowLevel = WC = windowLevel;
        this.windowWidth = WW = windowWidth;
        this.currentScoutDetails = currentScoutDetails;
        this.dataset = dataset;
        this.totalInstance = instanceUidList.size();
    }

    public TextOverlayParam getTextOverlayParam() {
        return textOverlayParam;
    }

    public boolean isMultiFrame() {
        return multiframe;
    }

    public String getDicomFileUrl() {
        return dicomFileUrl;
    }

    public String getStudyUID() {
        return dataset.getString(Tags.StudyInstanceUID);
    }

    public boolean isEncapsulatedDocument() {
        return isEncapsulatedDocument;
    }

    public BufferedImage getCurrentbufferedimage() {
        return currentbufferedimage;
    }

    public String getInstanceUID() {
        return dataset.getString(Tags.SOPInstanceUID);
    }

    public String getSeriesUID() {
        return dataset.getString(Tags.SeriesInstanceUID);
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
        return scale;
    }

    public double getCurrentScaleFactor() {
        return currentScaleFactor;
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
        return dataset.getString(Tags.Modality);
    }

    public boolean isLocalizer() {
        return isLocalizer;
    }

    public String getFrameOfReferenceUID() {
        return (currentScoutDetails != null) ? currentScoutDetails.getImageFrameofReferenceUID() : null;
    }

    public String getImageOrientation() {
        return (currentScoutDetails != null) ? currentScoutDetails.getImageOrientation() : null;
    }

    public String getReferencedSOPInstanceUID() {
        return (currentScoutDetails != null) ? currentScoutDetails.getImageReferenceSOPInstanceUID() : null;
    }

    public ScoutLineInfoModel getCurrentScoutDetails() {
        return currentScoutDetails;
    }

    public ArrayList<String> getInstanceUidList() {
        return instanceUidList;
    }

    public int getTotalFrames() {
        return dataset.getInteger(Tags.NumberOfFrames);
    }

    public void initializeParams() {
        if (image != null) {
            double xScale = (double) getWidth() / image.getWidth();
            double yScale = (double) getHeight() / image.getHeight();
            scale = Math.min(xScale, yScale);
            centerImage();
            displayZoomLevel();
        }
    }

    private void centerImage() {
        originX = (int) (getWidth() - (int) (scale * image.getWidth())) / 2;
        originY = (int) (getHeight() - (int) (scale * image.getHeight())) / 2;
    }

    public void panImage(Point p) {
        int xDelta = p.x - mousePosition.x;
        int yDelta = p.y - mousePosition.y;
        originX += xDelta;
        originY += yDelta;
        mousePosition = p;
        repaint();
    }

    public int getOriginX() {
        return originX;
    }

    public int getOriginY() {
        return originY;
    }

    public String getFileLocation(int i) {
        return fileLocation + File.separator + instanceUidList.get(i);
    }

    public String get(int i) {
        return instanceUidList.get(i);
    }

    public void setInfo(ImagePanel imgPanelRef) {
        imgPanelRef.setImageInfo(pixelSpacingX, pixelSpacingY, fileLocation, currentSeriesAnnotation, instanceUidList, cmParam, cm, windowLevel, windowWidth, currentScoutDetails, dataset);
    }

    public void shutDown() {
        buffer.shutDown();
        buffer = null;
    }

    public String getSeriesLocation() {
        return fileLocation;
    }

    public double getOriginalScaleFacotor() {
        return image != null ? Math.min((double) getWidth() / image.getWidth(), (double) getHeight() / image.getHeight()) : 0;
    }

    public void pixelMapping() {
        scale = 1;
        centerImage();
        displayZoomLevel();
        repaint();
    }

    private void zoomImage(double factor) {
        //gives the actual mouse position on image
        double imgPosX = (mouseLocX2 - originX) / scale;
        double imgPosY = (mouseLocY2 - originY) / scale;

        scale += factor;

        //gives the mouse position after the scale factor applied
        double afterZoomX = (imgPosX * scale) + originX;
        double afterZoomY = (imgPosY * scale) + originY;

        //adds the difference in mouse position to origins
        originX += (mouseLocX2 - afterZoomX);
        originY += (mouseLocY2 - afterZoomY);

        displayZoomLevel();

        revalidate();
        repaint();
        layeredCanvas.annotationPanel.repaint();
    }
}
