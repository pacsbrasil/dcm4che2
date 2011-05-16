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
package in.raster.mayam.form;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PagePanel;
import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.delegate.ImageOrientation;
import in.raster.mayam.delegate.LocalizerDelegate;
import in.raster.mayam.delegate.SeriesChooserDelegate;
import in.raster.mayam.delegate.SynchronizationDelegate;
import in.raster.mayam.model.Instance;
import in.raster.mayam.model.ScoutLineInfoModel;
import in.raster.mayam.model.Series;
import in.raster.mayam.model.Study;
import in.raster.mayam.param.TextOverlayParam;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dcm4che.dict.Tags;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che.image.ColorModelFactory;
import org.dcm4che.image.ColorModelParam;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ByteLookupTable;
import java.awt.image.LookupOp;
import java.awt.image.ColorModel;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.dcm4che.data.Dataset;
import org.dcm4che.imageio.plugins.DcmMetadata;
import org.dcm4che2.data.Tag;

/**
 *
 * @author  BabuHussain
 * @version 0.5 
 *
 */
public class ImagePanel extends javax.swing.JPanel implements MouseWheelListener,
        MouseMotionListener, MouseListener {

    private Canvas canvas;
    //Image manipulation Flags
    public boolean isRotate = false;
    public boolean flipHorizontalFlag = false;
    public boolean flipVerticalFlag = false;
    private boolean invertFlag = false;
    private boolean newBufferedImage = false;
    private boolean scaleFlag = false;
    private boolean firstTime = true;
    private boolean mousePressed = false;
    private static boolean widowingFlag = false;
    private static boolean probeFlag;
    private double scaleFactor = 1;
    public int rotateRightAngle = 0;
    private int rotateLeftAngle = 0;
    public static String tool = "windowing";
    //Windowing, Hu related variables
    private int windowLevel;
    private int windowWidth;
    private int WC;
    private int WW;
    private String rescaleSlope;
    private String rescaleIntercept;
    private double pixelSpacingX;
    private double pixelSpacingY;
    //Unique id variables
    private String studyUID;
    private String seriesUID;
    private String instanceUID;
    private String seriesNo;
    private String modality;
    private String studyDesc;
    //ImageIO variables
    private Image loadedImage;
    private BufferedImage currentbufferedimage;
    private BufferedImage image;
    private ImageIcon imageIcon;
    private ImageInputStream iis;
    private Iterator iter;
    private ImageReader reader;
    private Dataset dataset;
    //Mouse pointer variables
    private int mouseLocX1;
    private int mouseLocX2;
    private int mouseLocY1;
    private int mouseLocY2;
    //ColorModel variables
    private ColorModelParam cmParam = null;
    private static final ColorModelFactory cmFactory = ColorModelFactory.getInstance();
    private ColorModel cm = null;
    private int windowingMultiplier = 1;
    private int[] pixelValueArray;
    private int pixelValue;
    private String hu = "";
    private int originalWidth;
    private int originalHeight;
    private String dicomFileUrl;
    //Multiframe image related variables
    private int nFrames = 0;
    private int currentFrame = 0;
    private boolean mulitiFrame = false;
    private int currentInstanceNo = 0;
    private int totalInstance;
    boolean canStart = true;
    private ArrayList<Instance> instanceArray = null;
    //TextOverlay 
    private TextOverlayParam textOverlayParam;
    private String[] aspectRatio;
    private float floatAspectRatio;
    //Scout Param
    private String frameOfReferenceUID;
    private String imagePosition;
    private String imageOrientation;
    private String[] imageType;
    private String referencedSOPInstanceUID = "";
    private String pixelSpacing;
    private int row;
    private int column;
    private boolean isLocalizer = false;
    private static boolean displayScout = false;
    private int scoutLine1X1;
    private int scoutLine1Y1;
    private int scoutLine1X2;
    private int scoutLine1Y2;
    private int scoutLine2X1;
    private int scoutLine2Y1;
    private int scoutLine2X2;
    private int scoutLine2Y2;
    private String orientationLabel = "";
    private int boundaryLine1X1;
    private int boundaryLine1Y1;
    private int boundaryLine1X2;
    private int boundaryLine1Y2;
    private int boundaryLine2X1;
    private int boundaryLine2Y1;
    private int boundaryLine2X2;
    private int boundaryLine2Y2;
    private int thumbWidth = 512;
    private int thumbHeight = 512;
    private int maxHeight = 512;
    private int maxWidth = 512;
    private double thumbRatio;
    private int startX = 0;
    private int startY = 0;
    private double currentScaleFactor = 1;
    private double initialPixelSpacingX;
    private double initialPixelSpacingY;
    private int axis1LeftX;
    private int axis1LeftY;
    private int axis1RightX;
    private int axis1RightY;
    private int axis1BottomX;
    private int axis1BottomY;
    private int axis1TopX;
    private int axis1TopY;
    private int axis2LeftX;
    private int axis2LeftY;
    private int axis2RightX;
    private int axis2RightY;
    private int axis2BottomX;
    private int axis2BottomY;
    private int axis2TopX;
    private int axis2TopY;
    private int axisLeftX;
    private int axisLeftY;
    private int axisRightX;
    private int axisRightY;
    private int axisBottomX;
    private int axisBottomY;
    private int axisTopX;
    private int axisTopY;
    private int instanceNumber;
    private String sliceLocation;
    public static boolean synchornizeTiles = false;
    public int syncStartInstance;
    private PDFFile curFile = null;
    public PagePanel page = null;
    private int curpage = -1;
    private boolean isEncapsulatedDocument = false;

    public ImagePanel() {
        initComponents();
    }

    /**
     * Constructs the imagepanel by passing file url and outer canvas
     * @param dicomFileUrl
     * @param canvas
     */
    public ImagePanel(String dicomFileUrl, Canvas canvas) {
        this.dicomFileUrl = dicomFileUrl;
        this.canvas = canvas;
        readDicomFile(new File(dicomFileUrl));
        retriveTagInfo();
        initComponents();
        addlisteners();
        retrieveInstanceInformation();
        retrieveScoutParam();
        setTotalInstacne();
        retriveTextOverlayParam();
        // designContext();
    }

    /**
     * Constructs the imagepanel by passing file url parameter
     * @param dicomFileUrl
     */
    public ImagePanel(String dicomFileUrl) {
        readDicomFile(new File(dicomFileUrl));
        retriveTagInfo();
        initComponents();
        addlisteners();
        retrieveInstanceInformation();
        retrieveScoutParam();
        setTotalInstacne();
        retriveTextOverlayParam();

    }

    /**
     * This routine used to retrive the text overlay related information from the dataset
     */
    private void retriveTextOverlayParam() {
        textOverlayParam = new TextOverlayParam();
        textOverlayParam.setPatientName(dataset.getString(Tags.PatientName));
        textOverlayParam.setPatientID(dataset.getString(Tags.PatientID));
        textOverlayParam.setSex(dataset.getString(Tags.PatientSex));
        textOverlayParam.setStudyDate(dataset.getString(Tags.StudyDate));
        textOverlayParam.setStudyDescription(dataset.getString(Tags.StudyDescription) != null ? dataset.getString(Tags.StudyDescription) : "");
        textOverlayParam.setSeriesDescription(dataset.getString(Tags.SeriesDescription) != null ? dataset.getString(Tags.SeriesDescription) : "");
        textOverlayParam.setInstanceNumber(dataset.getString(Tags.InstanceNumber) != null ? dataset.getString(Tags.InstanceNumber) : "");
        textOverlayParam.setBodyPartExamined(dataset.getString(Tags.BodyPartExamined));
        textOverlayParam.setSlicePosition(dataset.getString(Tags.SliceLocation));
        textOverlayParam.setPatientPosition(dataset.getString(Tags.PatientPosition));
        textOverlayParam.setInstitutionName(dataset.getString(Tags.InstitutionName));
        textOverlayParam.setWindowLevel(dataset.getString(Tags.WindowCenter) != null ? dataset.getString(Tags.WindowCenter) : "");
        textOverlayParam.setWindowWidth(dataset.getString(Tags.WindowWidth) != null ? dataset.getString(Tags.WindowWidth) : "");
        textOverlayParam.setCurrentInstance(currentInstanceNo);
        textOverlayParam.setTotalInstance(Integer.toString(totalInstance));

    }

    /**
     * This routine used to retrive some other tag information from the dataset
     */
    private void retriveTagInfo() {
        studyUID = dataset.getString(Tags.StudyInstanceUID);
        seriesUID = dataset.getString(Tags.SeriesInstanceUID);
        instanceUID = dataset.getString(Tags.SOPInstanceUID);
        modality = dataset.getString(Tags.Modality);
        seriesNo = dataset.getString(Tags.SeriesNumber);
        studyDesc = dataset.getString(Tags.StudyDescription);
        rescaleSlope = dataset.getString(Tags.RescaleSlope);
        rescaleIntercept = dataset.getString(Tags.RescaleIntercept);
        aspectRatio = dataset.getStrings(Tags.PixelAspectRatio);
        sliceLocation = (dataset.getString(Tag.SliceLocation) != null) ? dataset.getString(Tag.SliceLocation) : "";
        try {
            currentInstanceNo = Integer.parseInt(dataset.getString(Tags.InstanceNumber));
        } catch (NumberFormatException e) {
            System.out.println("Instance Number format error ");
            currentInstanceNo = 1;
        } catch (NullPointerException e) {
            System.out.println("Instance Number is null, set to 1");
            currentInstanceNo = 1;
        }
    }

    private void retrieveScoutParam() {
        try {
            frameOfReferenceUID = dataset.getString(Tags.FrameOfReferenceUID) != null ? dataset.getString(Tags.FrameOfReferenceUID) : "";
            imagePosition = dataset.getString(Tags.ImagePosition, 0) != null ? dataset.getString(Tags.ImagePosition, 0) + "\\" + dataset.getString(Tags.ImagePosition, 1) + "\\" + dataset.getString(Tags.ImagePosition, 2) : null;
            imageOrientation = dataset.getString(Tags.ImageOrientation) != null ? dataset.getString(Tags.ImageOrientation, 0) + "\\" + dataset.getString(Tags.ImageOrientation, 1) + "\\" + dataset.getString(Tags.ImageOrientation, 2) + "\\" + dataset.getString(Tags.ImageOrientation, 3) + "\\" + dataset.getString(Tags.ImageOrientation, 4) + "\\" + dataset.getString(Tags.ImageOrientation, 5) : null;
            imageType = dataset.getStrings(Tags.ImageType) != null ? dataset.getStrings(Tags.ImageType) : null;
            pixelSpacing = dataset.getString(Tags.PixelSpacing) != null ? dataset.getString(Tags.PixelSpacing, 0) + "\\" + dataset.getString(Tags.PixelSpacing, 1) : null;
            row = dataset.getString(Tags.Rows) != null ? Integer.parseInt(dataset.getString(Tags.Rows)) : 0;
            column = dataset.getString(Tags.Columns) != null ? Integer.parseInt(dataset.getString(Tags.Columns)) : 0;
            Dataset referencedImageSequence = dataset.getItem(Tag.ReferencedImageSequence) != null ? dataset.getItem(Tag.ReferencedImageSequence) : null;
            if (imageType != null) {
                if (imageType.length >= 3 && imageType[2].equalsIgnoreCase("LOCALIZER")) {
                    isLocalizer = true;
                } else {
                    if (referencedImageSequence != null) {
                        referencedSOPInstanceUID = referencedImageSequence.getString(Tag.ReferencedSOPInstanceUID);
                    }
                    isLocalizer = false;
                }
            }
            findOrientation();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * This routine used to retrive the instance related information
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
                initialPixelSpacingY = pixelSpacingY = Double.parseDouble(dataset.getString(
                        Tags.PixelSpacing, 0));
                initialPixelSpacingX = pixelSpacingX = Double.parseDouble(dataset.getString(
                        Tags.PixelSpacing, 1));

            } catch (NullPointerException e) {
                initialPixelSpacingX = 0;
                initialPixelSpacingY = 0;
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
    }

    /**
     *
     * @return textoverlayParam-It return the text overlay param
     */
    public TextOverlayParam getTextOverlayParam() {
        return textOverlayParam;
    }

    /**
     *
     * @param textOverlayParam-It sets the textOverlayParam
     */
    public void setTextOverlayParam(TextOverlayParam textOverlayParam) {
        this.textOverlayParam = textOverlayParam;
    }

    /**
     * This routine used to check the multiframe status
     * @return
     */
    public boolean isMulitiFrame() {
        return mulitiFrame;
    }

    /**
     * This routine used to set the multiframe status
     * @param mulitiFrame
     */
    public void setMulitiFrame(boolean mulitiFrame) {
        this.mulitiFrame = mulitiFrame;
    }

    /**
     * This routine used to return the dicom file url
     * @return dicomFileUrl-It returns the dicom file url of the current image
     */
    public String getDicomFileUrl() {
        return dicomFileUrl;
    }

    public int getnFrames() {
        return nFrames;
    }

    public void setnFrames(int nFrames) {
        this.nFrames = nFrames;
    }

    /**
     * This routine used to set the dicom file url for the current image box
     * @param dicomFileUrl-dicom file url
     */
    public void setDicomFileUrl(String dicomFileUrl) {
        this.dicomFileUrl = dicomFileUrl;
    }

    /**
     * This routine used to set the flag for panning
     */
    public void doPan() {
        if (tool.equalsIgnoreCase("panning")) {
            tool = "";
        } else {
            tool = "panning";
        }
    }

    /**
     * Tdhis routine used to return the canvas of the image box
     * @return
     */
    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * This routine used to get the study uid value for the current image box
     * @return
     */
    public String getStudyUID() {
        return studyUID;
    }

    /**
     * This routine used to set teh study uid value for the current image box
     * @param studyUID
     */
    public void setStudyUID(String studyUID) {
        this.studyUID = studyUID;
    }

    /**
     * This routine used to read the dicom file
     * @param selFile-This the file to be read
     */
    private void readDicomFile(File selFile) {
        try {
            iis = ImageIO.createImageInputStream(selFile);
            iter = ImageIO.getImageReadersByFormatName("DICOM");
            reader = (ImageReader) iter.next();
            this.reader.setInput(iis, false);
            dataset = ((DcmMetadata) reader.getStreamMetadata()).getDataset();
            try {
                if (reader.getNumImages(true) > 0) {
                    currentbufferedimage = reader.read(0);
                    floatAspectRatio = reader.getAspectRatio(0);
                }
                nFrames = reader.getNumImages(true);
                if (nFrames - 1 > 0) {
                    mulitiFrame = true;
                    this.totalInstance = nFrames;
                }
                if (reader.getNumImages(true) > 0) {
                    imageIcon = new ImageIcon();
                    imageIcon.setImage(currentbufferedimage);
                    loadedImage = imageIcon.getImage();
                    image = new BufferedImage(loadedImage.getWidth(null), loadedImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2 = image.createGraphics();
                    g2.drawImage(loadedImage, 0, 0, null);
                }
                if (dataset.getString(Tags.SOPClassUID)!=null&&dataset.getString(Tags.SOPClassUID).equalsIgnoreCase("1.2.840.10008.5.1.4.1.1.104.1")) {
                    readDicom(selFile);
                }
                repaint();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println("io exception");
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
        this.curFile = newfile;
        forceGotoPage(0);
    }
    PDFPage pg = null;

    public void forceGotoPage(int pagenum) {
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

    public boolean isIsEncapsulatedDocument() {
        return isEncapsulatedDocument;
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
        currentFrame++;
        if (currentFrame == nFrames) {
            currentFrame = 0;
        }
        currentInstanceNo = currentFrame;
        totalInstance = nFrames;
        try {
            currentbufferedimage = reader.read(currentFrame);
            convertToRGBImage();
            repaint();
            this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setCurrentInstance(this.currentInstanceNo);
            this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setTotalInstance(Integer.toString(this.totalInstance));
            Instance instance = instanceArray.get(0);
            this.getCanvas().getLayeredCanvas().annotationPanel.setAnnotation(instance.getMultiframeAnnotation(currentInstanceNo));
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.getLogger(ImagePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void showPreviousFrame() {
        if (currentFrame == 0) {
            currentFrame = nFrames;
        }
        currentFrame--;
        currentInstanceNo = currentFrame;
        totalInstance = nFrames;
        try {
            currentbufferedimage = reader.read(currentFrame);
            convertToRGBImage();
            repaint();
            this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setCurrentInstance(this.currentInstanceNo);
            this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setTotalInstance(Integer.toString(this.totalInstance));
            Instance instance = instanceArray.get(0);
            this.getCanvas().getLayeredCanvas().annotationPanel.setAnnotation(instance.getMultiframeAnnotation(currentInstanceNo));
        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(ImagePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This routine used to set the image of this image box
     * @param newImg
     */
    public void setImage(BufferedImage newImg) {
        try {
            newBufferedImage = true;
            currentbufferedimage = newImg;
            windowChanged(this.windowLevel, this.windowWidth);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    /**
     * This routine used to retrive the buffered image of the current image box
     * @return
     */
    public BufferedImage getCurrentbufferedimage() {
        return currentbufferedimage;
    }

    /**
     * This routine used to get the instance uid value for the current image box
     * @return
     */
    public String getInstanceUID() {
        return instanceUID;
    }

    /**
     * This routine used to get the series instance uid value for the current image box
     * @return
     */
    public String getSeriesUID() {
        return seriesUID;
    }

    /**
     * This routine used to apply the filter to the image box
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
    /*
    private void convolve(float[] elements) {
    Kernel kernel = new Kernel(3, 3, elements);
    ConvolveOp op = new ConvolveOp(kernel);
    filter(op);*
    }

    public void blur() {
    float weight = 1.0f / 9.0f;
    float[] elements = new float[9];
    for (int i = 0; i < 9; i++) {
    elements[i] = weight;
    }
    convolve(elements);
    }

    public void sharpen() {
    float[] elements = {0.0f, -1.0f, 0.0f, -1.0f, 5.f, -1.0f, 0.0f, -1.0f,
    0.0f};
    convolve(elements);
    }

    public void edgeDetect() {
    float[] elements = {0.0f, -1.0f, 0.0f, -1.0f, 4.f, -1.0f, 0.0f, -1.0f,
    0.0f};
    convolve(elements);
    }

    public void brighten() {
    float a = 1.5f;
    float b = -20.0f;
    RescaleOp op = new RescaleOp(a, b, null);
    filter(op);
    }
     */

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
        widowingFlag = false;
        newBufferedImage = false;
        this.getCanvas().repaint();

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
        widowingFlag = false;
        newBufferedImage = false;
        this.repaint();
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
        widowingFlag = false;
        newBufferedImage = false;
        this.repaint();
        repaintTextOverlay();

    }
    /*
    static AffineTransform mirrorHorizontalTransform;

    static { // Create and initialize the AffineTransform
    mirrorHorizontalTransform = AffineTransform.getTranslateInstance(512, 0);
    mirrorHorizontalTransform.scale(-1.0, 1.0); // flip horizontally
    }
    static AffineTransform mirrorVerticalTransform;

    static { // Create and initialize the AffineTransform
    mirrorVerticalTransform = AffineTransform.getTranslateInstance(0, 512);
    mirrorVerticalTransform.scale(1.0, -1.0); // flip horizontally
    }
     */

    /**
     * This routine flips the image box horizontally
     */
    public void flipHorizontal() {
        if ((rotateRightAngle == 90) || (rotateRightAngle == 270) || (rotateLeftAngle == -90) || (rotateLeftAngle == -270)) {
            flipV();
        } else {
            flipH();
        }
    }

    /**
     * This routine flips the image box vertically
     */
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
        widowingFlag = false;
        newBufferedImage = false;

    }

    private void flipH() {
        if (flipHorizontalFlag) {
            flipHorizontalFlag = false;
        } else {
            flipHorizontalFlag = true;
        }
        widowingFlag = false;
        newBufferedImage = false;
    }

    /**
     * This routine used to reset the image box with the original buffered image
     */
    public void reset() {
        this.getCanvas().getLayeredCanvas().annotationPanel.resetAnnotation();
        windowLevel = (int) WC;
        windowWidth = (int) WW;
        windowChanged(windowLevel, windowWidth);
        firstTime = true;
        scaleFactor = 1;
        scaleProcess();
        this.getCanvas().getLayeredCanvas().annotationPanel.scaleProcess();
        invertFlag = false;
        flipHorizontalFlag = false;
        flipVerticalFlag = false;
        isRotate = false;
        rotateLeftAngle = 0;
        rotateRightAngle = 0;
        currentInstanceNo = 0;
        canvas.setBackground(Color.BLACK);
        canvas.setForeground(Color.WHITE);
        repaint();
        if (!firstTime) {
            repaint();
        }
        selectFirstInstance();
        this.getCanvas().getLayeredCanvas().annotationPanel.resetAnnotaionTools();
        this.tool = "windowing";
    }

    public void resizeHandler() {
        repaint();
        centerImage();
        this.canvas.setSelection();
    }

    /**
     * This routine used to reset the windowing values of the current image box
     */
    public void resetWindowing() {
        windowLevel = (int) WC;
        windowWidth = (int) WW;
        windowChanged(windowLevel, windowWidth);
    }

    /**
     * This routine used to select the first instance of series
     */
    public void selectFirstInstance() {
        Iterator<Study> studyItr = MainScreen.studyList.iterator();
        while (studyItr.hasNext()) {
            Study study = studyItr.next();
            if (study.getStudyInstanceUID().equalsIgnoreCase(ApplicationContext.imgPanel.getStudyUID())) {
                ArrayList<Series> seriesList = (ArrayList<Series>) study.getSeriesList();
                for (int i = 0; i < seriesList.size(); i++) {
                    Series series = seriesList.get(i);
                    if (series.getSeriesInstanceUID().equalsIgnoreCase(this.seriesUID)) {
                        Instance instance = series.getImageList().get(0);
                        setImage(instance.getPixelData());
                        setInstanceInfo(instance);
                        this.getCanvas().getLayeredCanvas().annotationPanel.setAnnotation(instance.getAnnotation());
                    }
                }
            }
        }
        this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setCurrentInstance(this.currentInstanceNo);
        this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setInstanceNumber("" + this.instanceNumber);
    }

    /**
     * This routine used to set the windowing tool enabled
     */
    public void doWindowing() {
        if (tool.equalsIgnoreCase("windowing")) {
            tool = "";
        } else {
            tool = "windowing";
        }
    }

    public void setWindowingToolsAsDefault() {
        if (!(tool.equalsIgnoreCase("windowing"))) {
            tool = "windowing";
        }
    }

    public boolean isWindowingSelected() {
        return (tool.equalsIgnoreCase("windowing"));
    }

    /**
     * This routine used to get the dataset value of the current image box
     * @return
     */
    public Dataset getDataset() {
        return dataset;
    }

    /**
     *  This routine used to set the dataset values of the current image box
     * @param dataset
     */
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    /**
     * This routine used to check the horizontal flip flag for the current image box
     * @return
     */
    public boolean isFlipHorizontalFlag() {
        return flipHorizontalFlag;
    }

    /**
     * This routine used to set the horizontal flip flag for the current image box
     * @param flipHorizontalFlag
     */
    public void setFlipHorizontalFlag(boolean flipHorizontalFlag) {
        this.flipHorizontalFlag = flipHorizontalFlag;
    }

    /**
     * This routine used to check the vertical flip flag for the current image box
     * @return
     */
    public boolean isFlipVerticalFlag() {
        return flipVerticalFlag;
    }

    /**
     * This routine used to set tej vertical flip flag for the current image box
     * @param flipVerticalFlag
     */
    public void setFlipVerticalFlag(boolean flipVerticalFlag) {
        this.flipVerticalFlag = flipVerticalFlag;
    }

    /**
     * This routine used to check status of rotate of the image box
     * @return
     */
    public boolean isIsRotate() {
        return isRotate;
    }

    /**
     * This routine used to set the rotate flag for the current image box
     * @param isRotate
     */
    public void setIsRotate(boolean isRotate) {
        this.isRotate = isRotate;
    }

    public int getRotateLeftAngle() {
        return rotateLeftAngle;
    }

    public void setRotateLeftAngle(int rotateLeftAngle) {
        this.rotateLeftAngle = rotateLeftAngle;
    }

    public int getRotateRightAngle() {
        return rotateRightAngle;
    }

    public void setRotateRightAngle(int rotateRightAngle) {
        this.rotateRightAngle = rotateRightAngle;
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(double scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public boolean isScaleFlag() {
        return scaleFlag;
    }

    public void setScaleFlag(boolean scaleFlag) {
        this.scaleFlag = scaleFlag;
    }

    /**
     * This override routine used to paint the image box
     * @param gs
     */
    @Override
    public void paintComponent(Graphics gs) {
        Graphics2D g = (Graphics2D) gs;
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
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
            if ((widowingFlag && invertFlag) || (newBufferedImage && invertFlag)) {
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
            }
            calculateNewHeightAndWidthBasedonAspectRatio();
            g.drawImage(image, startX, startY, thumbWidth, thumbHeight, null);
            if (displayScout) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(Color.YELLOW);
                if (orientationLabel.equalsIgnoreCase("SAGITTAL")) {
                    g.drawLine((int) (boundaryLine1X1 * this.getCurrentScaleFactor() + startX), (int) (boundaryLine1Y1 * this.getCurrentScaleFactor() + startY), (int) (boundaryLine1X2 * this.getCurrentScaleFactor() + startX), (int) (boundaryLine1Y2 * this.getCurrentScaleFactor() + startY));
                    g.drawLine((int) (boundaryLine2X1 * this.getCurrentScaleFactor() + startX), (int) (boundaryLine2Y1 * this.getCurrentScaleFactor() + startY), (int) (boundaryLine2X2 * this.getCurrentScaleFactor() + startX), (int) (boundaryLine2Y2 * this.getCurrentScaleFactor() + startY));
                    g.setColor(Color.GREEN);
                    g.drawLine((int) (scoutLine1X1 * this.getCurrentScaleFactor() + startX), (int) (scoutLine1Y1 * this.getCurrentScaleFactor() + startY), (int) (scoutLine1X2 * this.getCurrentScaleFactor() + startX), (int) (scoutLine1Y2 * this.getCurrentScaleFactor() + startY));
                    g.drawLine((int) (scoutLine2X1 * this.getCurrentScaleFactor() + startX), (int) (scoutLine2Y1 * this.getCurrentScaleFactor() + startY), (int) (scoutLine2X2 * this.getCurrentScaleFactor() + startX), (int) (scoutLine2Y2 * this.getCurrentScaleFactor() + startY));
                } else if (orientationLabel.equalsIgnoreCase("CORONAL")) {
                    g.drawLine((int) (axis1LeftX * this.getCurrentScaleFactor() + startX), (int) (axis1LeftY * this.getCurrentScaleFactor() + startY), (int) (axis1RightX * this.getCurrentScaleFactor() + startX), (int) (axis1RightY * this.getCurrentScaleFactor() + startY));
                    g.drawLine((int) (axis2LeftX * this.getCurrentScaleFactor() + startX), (int) (axis2LeftY * this.getCurrentScaleFactor() + startY), (int) (axis2RightX * this.getCurrentScaleFactor() + startX), (int) (axis2RightY * this.getCurrentScaleFactor() + startY));
                    g.setColor(Color.GREEN);
                    g.drawLine((int) (axisLeftX * this.getCurrentScaleFactor() + startX), (int) (axisLeftY * this.getCurrentScaleFactor() + startY), (int) (axisRightX * this.getCurrentScaleFactor() + startX), (int) (axisRightY * this.getCurrentScaleFactor() + startY));
                }
            }
        }
        if (dataset.getString(Tags.SOPClassUID)!=null&&dataset.getString(Tags.SOPClassUID).equalsIgnoreCase("1.2.840.10008.5.1.4.1.1.104.1")) {
            calculateResolutionForPdfDicom(loadedImage.getWidth(null), loadedImage.getHeight(null));
            this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setCurrentInstance(curpage);
            this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setTotalInstance(Integer.toString(totalInstance));
            g.drawImage(loadedImage, startX, startY, thumbWidth, thumbHeight, null);
            ApplicationContext.imgView.getImageToolbar().hideAnnotationTools();
        }
        if (firstTime) {
            centerImage();
            originalHeight = this.getSize().height;
            originalWidth = this.getSize().width;
            firstTime = false;
        }
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

    private void calculateNewHeightAndWidthBasedonAspectRatio() {
        thumbRatio = thumbWidth / thumbHeight;
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();
        double imageRatio = (double) imageWidth / (double) imageHeight;
        if (imageRatio < floatAspectRatio) {
            imageHeight = (imageWidth + 0.00f) / floatAspectRatio;
            pixelSpacingY = ((initialPixelSpacingY * image.getHeight()) / imageHeight);
            pixelSpacing = pixelSpacingY + "\\" + pixelSpacingX;
        } else {
            imageWidth = (imageHeight + 0.00f) * floatAspectRatio;
            pixelSpacingX = (initialPixelSpacingX * image.getWidth()) / imageWidth;
            pixelSpacing = pixelSpacingY + "\\" + pixelSpacingX;
        }
        this.row = (int) Math.round(imageHeight);
        this.column = (int) Math.round(imageWidth);
        imageRatio = imageWidth / imageHeight;
        if (thumbRatio < imageRatio) {
            thumbHeight = (int) Math.round((thumbWidth + 0.00f) / imageRatio);
        } else {
            thumbWidth = (int) Math.round((thumbHeight + 0.00f) * imageRatio);
        }
        startX = (maxWidth - thumbWidth) / 2;
        startY = (maxHeight - thumbHeight) / 2;
        displayZoomLevel();
    }

    private void displayZoomLevel() {
        int currentZoomLevel = (int) Math.round(this.scaleFactor * currentScaleFactor * 100);
        this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setZoomLevel(" Zoom: " + currentZoomLevel + "%");
    }

    public double getCurrentScaleFactor() {
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();
        double imageRatio = imageWidth / imageHeight;
        if (imageRatio < floatAspectRatio) {
            imageHeight = (imageWidth + 0.00f) / floatAspectRatio;
        } else {
            imageWidth = (imageHeight + 0.00f) * floatAspectRatio;
        }
        currentScaleFactor = (thumbHeight + 0.000f) / imageHeight;
        return currentScaleFactor;
    }

    public void setScoutBorder1Coordinates(int line1X1, int line1Y1, int line1X2, int line1Y2) {
        boundaryLine1X1 = line1X1;
        boundaryLine1X2 = line1X2;
        boundaryLine1Y1 = line1Y1;
        boundaryLine1Y2 = line1Y2;
    }

    public void setScoutBorder2Coordinates(int line1X1, int line1Y1, int line1X2, int line1Y2) {
        boundaryLine2X1 = line1X1;
        boundaryLine2X2 = line1X2;
        boundaryLine2Y1 = line1Y1;
        boundaryLine2Y2 = line1Y2;
    }

    public void setAxis1Coordinates(int leftx, int lefty, int rightx, int righty, int topx, int topy, int bottomx, int bottomy) {
        axis1LeftX = leftx;
        axis1LeftY = lefty;
        axis1RightX = rightx;
        axis1RightY = righty;
        axis1TopX = topx;
        axis1TopY = topy;
        axis1BottomX = bottomx;
        axis1BottomY = bottomy;
    }

    public void setAxis2Coordinates(int leftx, int lefty, int rightx, int righty, int topx, int topy, int bottomx, int bottomy) {
        axis2LeftX = leftx;
        axis2LeftY = lefty;
        axis2RightX = rightx;
        axis2RightY = righty;
        axis2TopX = topx;
        axis2TopY = topy;
        axis2BottomX = bottomx;
        axis2BottomY = bottomy;
    }

    public void setAxisCoordinates(int leftx, int lefty, int rightx, int righty, int topx, int topy, int bottomx, int bottomy) {
        axisLeftX = leftx;
        axisLeftY = lefty;
        axisRightX = rightx;
        axisRightY = righty;
        axisTopX = topx;
        axisTopY = topy;
        axisBottomX = bottomx;
        axisBottomY = bottomy;
    }

    private void findOrientation() {
        String imageOrientationArray[];
        if (imageOrientation != null) {
            imageOrientationArray = imageOrientation.split("\\\\");
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
            if ((((axis1.equals("R")) || (axis1.equals("L")))) && (((axis2.equals("A")) || (axis2.equals("P"))))) {
                label = "AXIAL";
            } else if ((((axis2.equals("R")) || (axis2.equals("L")))) && (((axis1.equals("A")) || (axis1.equals("P"))))) {
                label = "AXIAL";
            } else if ((((axis1.equals("R")) || (axis1.equals("L")))) && (((axis2.equals("H")) || (axis2.equals("F"))))) {
                label = "CORONAL";
            } else if ((((axis2.equals("R")) || (axis2.equals("L")))) && (((axis1.equals("H")) || (axis1.equals("F"))))) {
                label = "CORONAL";
            } else if ((((axis1.equals("A")) || (axis1.equals("P")))) && (((axis2.equals("H")) || (axis2.equals("F"))))) {
                label = "SAGITTAL";
            } else if ((((axis2.equals("A")) || (axis2.equals("P")))) && (((axis1.equals("H")) || (axis1.equals("F"))))) {
                label = "SAGITTAL";
            }

        } else {
            label = "OBLIQUE";
        }

        return label;
    }

    private void setEnclosingSizes(int finalWidth, int finalHeight) {
        this.getCanvas().getLayeredCanvas().getAnnotationPanel().setSize(finalWidth, finalHeight);
        this.getCanvas().getLayeredCanvas().getAnnotationPanel().setBounds(xPosition, yPosition, this.getSize().width, this.getSize().height);
    }

    /**
     * This routine used to zoom in the image box using the scale factor
     */
    public void doZoomIn() {
        scaleFlag = true;
        scaleFactor = scaleFactor + 0.5;
        displayZoomLevel();
        scaleProcess();
        this.getCanvas().repaint();

    }

    public void convertToRGBImage() {
        imageIcon = new ImageIcon();
        if (currentbufferedimage != null) {
            imageIcon.setImage(currentbufferedimage);
            loadedImage = imageIcon.getImage();
            image = new BufferedImage(loadedImage.getWidth(null), loadedImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            g2.drawImage(loadedImage, 0, 0, null);
        }
    }

    /**
     * This routine used to zoom out the image box using the scale factor
     */
    public void doZoomOut() {
        scaleFlag = true;
        scaleFactor = scaleFactor - 0.5;
        if (scaleFactor > 0) {
            scaleProcess();
        } else {
            scaleFactor = 0.5;
        }
        displayZoomLevel();
        this.getCanvas().repaint();
    }

    /**
     * This routine used to enable the stack process
     */
    public void doStack() {
        if (tool.equalsIgnoreCase("stack")) {
            tool = "";
        } else {
            tool = "stack";
        }
    }

    public boolean isStackSelected() {
        return (tool.equalsIgnoreCase("stack"));
    }

    public void setToolsToNull() {
        tool = "";
    }

    private void scaleProcess() {
        double currentWidth = this.getSize().width;
        double currentHeight = this.getSize().height;
        double newWidth = originalWidth * scaleFactor;
        double newHeight = originalHeight * scaleFactor;
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

    public double getOriginalHeight() {
        return originalHeight;
    }

    public void setOriginalHeight(int originalHeight) {
        this.originalHeight = originalHeight;
    }

    public double getOriginalWidth() {
        return originalWidth;
    }

    public void setOriginalWidth(int originalWidth) {
        this.originalWidth = originalWidth;
    }

    private void moveComponent(int x, int y) {
        this.setBounds(x, y, this.getSize().width, this.getSize().height);
    }
    int xPosition;
    int yPosition;

    /**
     * This routine used to calculate the x and y position of the image box to be placed in the
     * outer canvas and set the bounds
     */
    private void centerImage() {
        xPosition = (canvas.getSize().width - this.getSize().width) / 2;
        yPosition = (canvas.getSize().height - this.getSize().height) / 2;
        this.setBounds(xPosition, yPosition, this.getSize().width, this.getSize().height);

    }

    /**
     * This routine erase the drawing in the outer canvas
     */
    private void clearCanvas() {
        Graphics g = canvas.getGraphics();
        Dimension d = canvas.getSize();
        Color c = canvas.getBackground();
        g.setColor(c);
        g.fillRect(0, 0, d.width, d.height);
    }

    /**
     * This method creates the color model based on the window level and width
     * and apply the values to the image box
     * @param windowCenter
     * @param windowWidth
     */
    public void windowChanged(int windowCenter, int windowWidth) {
        try {
            widowingFlag = true;
            if (cmParam != null) {
                cmParam = cmParam.update(windowCenter, windowWidth, cmParam.isInverse());
                cm = cmFactory.getColorModel(cmParam);
                currentbufferedimage = new BufferedImage(cm, currentbufferedimage.getRaster(), false, null);
                this.windowLevel = windowCenter;
                this.windowWidth = windowWidth;
            }
            convertToRGBImage();
            repaint();
            changeTextOverlay();


        } catch (Exception e) {
            System.out.println("Windowing can't be applied");
        }
    }

    public void repaintTextOverlay() {
        this.getCanvas().getLayeredCanvas().textOverlay.repaint();
    }

    public ColorModel getColorModel() {
        if(cmParam!=null){
        cmParam = cmParam.update(this.windowLevel,
                this.windowWidth, cmParam.isInverse());
        cm = cmFactory.getColorModel(cmParam);
        }
        return cm;
    }

    /**
     * This routine used to change the text overlay of the image box
     */
    public void changeTextOverlay() {
        this.getCanvas().getLayeredCanvas().textOverlay.setWindowingParameter(Integer.toString(this.windowLevel), Integer.toString(this.windowWidth));
    }

    /**
     * This routine used to get the dataset object from the file specified
     * @param filePath
     * @return
     */
    public DicomObject getDatasetFromFile(String filePath) {

        DicomInputStream dis = null;
        DicomObject data = null;
        try {
            File parseFile = new File(filePath);
            dis = new DicomInputStream(parseFile);
            data = new BasicDicomObject();
            data = dis.readDicomObject();

        } catch (IOException ex) {
            ex.printStackTrace();
            // Logger.getLogger(ModelUpdator.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                dis.close();
            } catch (IOException ex) {
                ex.printStackTrace();
                //   Logger.getLogger(ModelUpdator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return data;
    }

    private void addlisteners() {
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
    }

    /**
     *
     */
    public void probe() {
        if (!probeFlag) {
            probeFlag = true;
            this.getCanvas().getLayeredCanvas().textOverlay.repaint();
        } else {
            probeFlag = false;
            this.getCanvas().getLayeredCanvas().textOverlay.repaint();
        }
    }

    public boolean isInvertFlag() {
        return invertFlag;
    }

    public void setInvertFlag(boolean invertFlag) {
        this.invertFlag = invertFlag;
    }

    public static boolean isProbeFlag() {
        return probeFlag;
    }

    public double getPixelSpacingX() {
        return pixelSpacingX;
    }

    public void setPixelSpacingX(double pixelSpacingX) {
        this.pixelSpacingX = pixelSpacingX;
    }

    public double getPixelSpacingY() {
        return pixelSpacingY;
    }

    public void setPixelSpacingY(double pixelSpacingY) {
        this.pixelSpacingY = pixelSpacingY;
    }

    public int getWindowLevel() {
        return windowLevel;
    }

    public void setWindowLevel(int windowLevel) {
        this.windowLevel = windowLevel;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();

        setBackground(new java.awt.Color(0, 0, 0));
        setComponentPopupMenu(jPopupMenu1);
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
        this.canvas.requestFocus();
    }//GEN-LAST:event_formMouseClicked
    private void setTotalInstacne() {
        if (ApplicationContext.databaseRef.getMultiframeStatus()) {
            if (!isMulitiFrame()) {
                totalInstance = ApplicationContext.databaseRef.getSeriesLevelInstance(this.studyUID, this.seriesUID);
                currentInstanceNo = 0;
            } else {
                totalInstance = nFrames;
                currentInstanceNo = 0;
            }
        } else {
            totalInstance = ApplicationContext.databaseRef.getSeriesLevelInstance(this.studyUID, this.seriesUID);
            currentInstanceNo = 0;
        }
        if (dataset.getString(Tags.SOPClassUID)!=null&&dataset.getString(Tags.SOPClassUID).equalsIgnoreCase("1.2.840.10008.5.1.4.1.1.104.1")) {
            totalInstance = curFile.getNumPages();
            currentInstanceNo = 0;
        }
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        this.requestFocus();
        this.canvas.setSelection();
        this.storeAnnotation();
        int notches = e.getWheelRotation();
        if (notches < 0) {
            doPrevious();
        } else {
            doNext();
        }
    }

    private void nextofEncapsulatedDocument() {
        if (dataset.getString(Tags.SOPClassUID)!=null&&dataset.getString(Tags.SOPClassUID).equalsIgnoreCase("1.2.840.10008.5.1.4.1.1.104.1")) {
            forceGotoPage(curpage + 1);
        }
    }

    private void previousofEncapsulatedDocument() {
        if (dataset.getString(Tags.SOPClassUID)!=null&&dataset.getString(Tags.SOPClassUID).equalsIgnoreCase("1.2.840.10008.5.1.4.1.1.104.1")) {
            forceGotoPage(curpage - 1);
        }
    }

    public void moveToNextInstance() {
        this.storeAnnotation();
        doNext();
    }

    public void moveToPreviousInstance() {
        this.storeAnnotation();
        doPrevious();

    }

    public void doPrevious() {
        if (isEncapsulatedDocument) {
            previousofEncapsulatedDocument();

        } else if (ApplicationContext.databaseRef.getMultiframeStatus() && isMulitiFrame()) {
            if (instanceArray == null) {
                previousFrame();
            } else {
                showPreviousFrame();
            }
        } else {
            if (instanceArray == null) {
                previousInstance();
            } else {
                selectPreviousInstance();
            }
        }
    }

    public void doNext() {
        if (isEncapsulatedDocument) {
            nextofEncapsulatedDocument();

        } else if (ApplicationContext.databaseRef.getMultiframeStatus() && isMulitiFrame()) {
            if (instanceArray == null) {
                nextFrame();
            } else {
                showNextFrame();
            }
        } else {
            if (instanceArray == null) {
                nextInstance();
            } else {
                selectNextInstance();
            }
        }
    }

    private void setInstanceArray() {
        for (Study study : MainScreen.studyList) {
            if (study.getStudyInstanceUID().equalsIgnoreCase(this.studyUID)) {
                for (Series series : study.getSeriesList()) {
                    if (mulitiFrame) {
                        if (series.isMultiframe() && series.getSeriesInstanceUID().equalsIgnoreCase(seriesUID) && series.getInstanceUID().equalsIgnoreCase(instanceUID)) {
                            instanceArray = (ArrayList<Instance>) series.getImageList();
                        } else {
                            if (series.getSeriesInstanceUID().equalsIgnoreCase(seriesUID)) {
                                for (Instance instance : series.getImageList()) {
                                    if (instance.getSop_iuid().equalsIgnoreCase(this.instanceUID)) {
                                        ArrayList<Instance> temp = new ArrayList<Instance>();
                                        temp.add(instance);
                                        instanceArray = temp;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void previousFrame() {
        for (Study study : MainScreen.studyList) {
            if (study.getStudyInstanceUID().equalsIgnoreCase(this.studyUID)) {
                for (Series series : study.getSeriesList()) {
                    if (mulitiFrame) {
                        if (ApplicationContext.databaseRef.getMultiframeStatus()) {
                            if (series.isMultiframe() && series.getSeriesInstanceUID().equalsIgnoreCase(seriesUID) && series.getInstanceUID().equalsIgnoreCase(instanceUID)) {
                                instanceArray = (ArrayList<Instance>) series.getImageList();
                                showPreviousFrame();
                            }
                        } else {
                            if (series.getSeriesInstanceUID().equalsIgnoreCase(seriesUID)) {
                                for (Instance instance : series.getImageList()) {
                                    if (instance.getSop_iuid().equalsIgnoreCase(this.instanceUID)) {
                                        ArrayList<Instance> temp = new ArrayList<Instance>();
                                        temp.add(instance);
                                        instanceArray = temp;
                                    }
                                }
                            }
                            showPreviousFrame();
                        }
                    }
                }
            }
        }
    }

    public void nextFrame() {
        for (Study study : MainScreen.studyList) {
            if (study.getStudyInstanceUID().equalsIgnoreCase(studyUID)) {
                for (Series series : study.getSeriesList()) {
                    if (mulitiFrame) {
                        if (ApplicationContext.databaseRef.getMultiframeStatus()) {
                            if (series.isMultiframe() && series.getSeriesInstanceUID().equalsIgnoreCase(seriesUID) && series.getInstanceUID().equalsIgnoreCase(instanceUID)) {
                                instanceArray = (ArrayList<Instance>) series.getImageList();
                                showNextFrame();
                            }
                        } else {
                            if (series.getSeriesInstanceUID().equalsIgnoreCase(seriesUID)) {
                                for (Instance instance : series.getImageList()) {
                                    if (instance.getSop_iuid().equalsIgnoreCase(this.instanceUID)) {
                                        ArrayList<Instance> temp = new ArrayList<Instance>();
                                        temp.add(instance);
                                        instanceArray = temp;
                                    }
                                }
                            }
                            showNextFrame();
                        }
                    }
                }
            }
        }
    }

    public void storeAnnotation() {
        Iterator<Study> studyItr = MainScreen.studyList.iterator();
        while (studyItr.hasNext()) {
            Study study = studyItr.next();
            if (study.getStudyInstanceUID().equalsIgnoreCase(studyUID)) {
                ArrayList<Series> seriesList = (ArrayList<Series>) study.getSeriesList();
                Iterator<Series> seriesItr = seriesList.iterator();
                while (seriesItr.hasNext()) {
                    Series series = seriesItr.next();
                    if (!ApplicationContext.databaseRef.getMultiframeStatus()) {
                        if (series.getSeriesInstanceUID().equalsIgnoreCase(seriesUID)) {
                            Instance instance = series.getImageList().get(currentInstanceNo);
                            instance.setAnnotation(this.getCanvas().getLayeredCanvas().annotationPanel.getAnnotation());
                        }
                    } else {
                        if (!isMulitiFrame()) {
                            if (!series.isMultiframe() && series.getSeriesInstanceUID().equalsIgnoreCase(seriesUID)) {
                                Instance instance = series.getImageList().get(currentInstanceNo);
                                instance.setAnnotation(this.getCanvas().getLayeredCanvas().annotationPanel.getAnnotation());
                            }
                        }//for multiframe instance setAnnotations should be there it can be of hash map with index values.
                        else {
                            if (series.isMultiframe() && series.getSeriesInstanceUID().equalsIgnoreCase(seriesUID) && series.getInstanceUID().equalsIgnoreCase(instanceUID)) {
                                Instance instance = series.getImageList().get(0);//becuase multiframe series contains single instance.
                                instance.addMultiframeAnnotation(currentInstanceNo, this.getCanvas().getLayeredCanvas().annotationPanel.getAnnotation());
                            }
                        }
                    }
                }
            }
        }
    }

    public String[] getInstancesFilePath() {
        String[] s = null;
        Iterator studyItr = MainScreen.studyList.iterator();
        while (studyItr.hasNext()) {
            Study study = (Study) studyItr.next();
            if (study.getStudyInstanceUID().equalsIgnoreCase(ApplicationContext.imgPanel.getStudyUID())) {
                ArrayList seriesList = (ArrayList) study.getSeriesList();
                for (int i = 0; i < seriesList.size(); ++i) {
                    Series series = (Series) seriesList.get(i);
                    if (series.getSeriesInstanceUID().equalsIgnoreCase(this.seriesUID)) {
                        s = new String[series.getImageList().size()];
                        int x = 0;
                        for (Iterator i$ = series.getImageList().iterator(); i$.hasNext();) {
                            Instance instance = (Instance) i$.next();

                            s[x] = instance.getFilepath();
                            ++x;
                        }
                    }
                }
            }

        }

        return s;
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        selectSeries(e);
    }

    private void selectSeries(MouseEvent e) {
        this.canvas.setSelection();
        mouseLocX1 = e.getX();
        mouseLocY1 = e.getY();
        mousePressed = true;
        if (e.isPopupTrigger()) {
            designContext();
            jPopupMenu1.show(this, e.getX(), e.getY());
        }
    }

    public void mouseReleased(MouseEvent e) {
        selectSeries(e);
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
        mouseLocX2 = e.getX();
        mouseLocY2 = e.getY();

        if (tool.equalsIgnoreCase("windowing")) {
            if (canStart) {
                canStart = false;
                mouseDragWindowing(mouseLocX2, mouseLocY2);
                canStart = true;
            }
        } else if (tool.equalsIgnoreCase("panning")) {
            this.setLocation(this.getBounds().x + mouseLocX2 - mouseLocX1, this.getBounds().y + mouseLocY2 - mouseLocY1);
            repaint();
            this.getCanvas().repaint();
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
        windowChanged((int) newWindowLevel, (int) newWindowWidth);
    }

    private void mouseDragStack(int mouseLocX2, int mouseLocY2) {
        int mouseLocDiffY = (int) ((mouseLocY2 - mouseLocY1));
        if (mouseLocDiffY < -12) {
            mouseLocX1 = mouseLocX2;
            mouseLocY1 = mouseLocY2;
            if (ApplicationContext.databaseRef.getMultiframeStatus() && isMulitiFrame()) {
                if (instanceArray == null) {
                    previousFrame();
                } else {
                    showPreviousFrame();
                }
            } else {
                if (instanceArray == null) {
                    previousInstance();
                } else {
                    selectPreviousInstance();
                }
            }
        } else if (mouseLocDiffY > 12) {
            mouseLocX1 = mouseLocX2;
            mouseLocY1 = mouseLocY2;
            if (ApplicationContext.databaseRef.getMultiframeStatus() && isMulitiFrame()) {
                if (instanceArray == null) {
                    nextFrame();
                } else {
                    showNextFrame();
                }
            } else {
                if (instanceArray == null) {
                    nextInstance();
                } else {
                    selectNextInstance();
                }
            }
        }
    }

    private void selectPreviousInstance() {
        if (currentInstanceNo == 0) {
            currentInstanceNo = totalInstance;
        }
        currentInstanceNo--;
        Instance instance = instanceArray.get(currentInstanceNo);
        if (instance.isMultiframe()) {
            this.getCanvas().getLayeredCanvas().textOverlay.multiframeStatusDisplay(true);
            this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setFramePosition((instance.getCurrentFrameNum() + 1) + "/" + instance.getTotalNumFrames());
        } else {
            this.getCanvas().getLayeredCanvas().textOverlay.multiframeStatusDisplay(false);
            this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setFramePosition("");
        }
        setImage(instance.getPixelData());
        setInstanceInfo(instance);
        if (displayScout) {
            findOrientation();
            if (!this.isLocalizer) {
                LocalizerDelegate localizer = new LocalizerDelegate();
                localizer.drawScoutLineWithBorder();
            }
        }
        this.getCanvas().getLayeredCanvas().annotationPanel.setAnnotation(instance.getAnnotation());
        if (!ApplicationContext.databaseRef.getMultiframeStatus()) {
            this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setCurrentInstance(instance.getSeriesLevelIndex());
        } else {
            this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setCurrentInstance(this.currentInstanceNo);
        }
        this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setInstanceNumber("" + this.instanceNumber);
        this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setSlicePosition(sliceLocation);
        if (synchornizeTiles && !isMulitiFrame() && modality.startsWith("CT")) {
            SynchronizationDelegate synchronizationDelegate = new SynchronizationDelegate();
            synchronizationDelegate.doTileSync();
        }
    }

    public void selectNextInstance() {
        currentInstanceNo++;
        if (currentInstanceNo == totalInstance) {
            currentInstanceNo = 0;
        }
        Instance instance = instanceArray.get(currentInstanceNo);
        if (instance.isMultiframe()) {
            this.getCanvas().getLayeredCanvas().textOverlay.multiframeStatusDisplay(true);
            this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setFramePosition((instance.getCurrentFrameNum() + 1) + "/" + instance.getTotalNumFrames());
        } else {
            this.getCanvas().getLayeredCanvas().textOverlay.multiframeStatusDisplay(false);
            this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setFramePosition("");
        }
        setImage(instance.getPixelData());
        setInstanceInfo(instance);
        if (displayScout) {
            findOrientation();
            if (!this.isLocalizer) {
                LocalizerDelegate localizer = new LocalizerDelegate();
                localizer.drawScoutLineWithBorder();
            }
        }
        this.getCanvas().getLayeredCanvas().annotationPanel.setAnnotation(instance.getAnnotation());
        //this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setCurrentInstance(this.currentInstanceNo);
        if (!ApplicationContext.databaseRef.getMultiframeStatus()) {
            this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setCurrentInstance(instance.getSeriesLevelIndex());
        } else {
            this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setCurrentInstance(this.currentInstanceNo);
        }
        this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setInstanceNumber("" + this.instanceNumber);
        this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setSlicePosition(sliceLocation);
        if (synchornizeTiles && !isMulitiFrame() && modality.startsWith("CT")) {
            SynchronizationDelegate synchronizationDelegate = new SynchronizationDelegate();
            synchronizationDelegate.doTileSync();
        }
    }

    public void setImage(String sliceLocation) {
        /**
         * This mehtod has been added for synchronized scroll it will not support for multiframe sync scroll. so that
         * instance number can be set directly to instanceNumber and currentInstanceNo variable.
         */
        int instanceNo = ApplicationContext.databaseRef.getInstaneNumberBasedOnSliceLocation(studyUID, seriesUID, instanceUID, sliceLocation);
        instanceNo--;
        if (instanceNumber != -1) {
            if (instanceNo > -1 && instanceNo < instanceArray.size()) {
                setImage(instanceArray.get(instanceNo).getPixelData());
                setInstanceInfo(instanceArray.get(instanceNo));
                this.instanceNumber = instanceNo;
                currentInstanceNo = instanceNo - 1;
                updateTextoverlay();
            }
        }
    }

    public void setImage(int instanceNo) {
        /**
         * This mehtod has been added for synchronized scroll it will not support for multiframe sync scroll. so that
         * instance number can be set directly to instanceNumber and currentInstanceNo variable.
         */
        if (instanceNo > -1 && instanceNo < instanceArray.size()) {
            setImage(instanceArray.get(instanceNo).getPixelData());
            setInstanceInfo(instanceArray.get(instanceNo));
            this.instanceNumber = instanceNo + 1;
            currentInstanceNo = instanceNo;
            updateTextoverlay();
        }
    }

    public void updateTextoverlay() {
        this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setCurrentInstance(this.currentInstanceNo);
        this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setInstanceNumber("" + this.instanceNumber);
        this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setSlicePosition(sliceLocation);
    }

    public void setInstanceArryFromList() {
        if (instanceArray == null) {
            for (Study study : MainScreen.studyList) {
                if (study.getStudyInstanceUID().equalsIgnoreCase(this.studyUID)) {
                    for (Series series : study.getSeriesList()) {
                        if (ApplicationContext.databaseRef.getMultiframeStatus()) {
                            if (!series.isMultiframe() && series.getSeriesInstanceUID().equalsIgnoreCase(seriesUID)) {
                                totalInstance = series.getImageList().size();
                                instanceArray = (ArrayList<Instance>) series.getImageList();
                            }
                        } else {
                            if (series.getSeriesInstanceUID().equalsIgnoreCase(seriesUID)) {
                                totalInstance = series.getImageList().size();
                                instanceArray = (ArrayList<Instance>) series.getImageList();
                            }
                        }
                    }
                }

            }
        }
    }

    public String getSliceLocation() {
        return sliceLocation;
    }

    private void setInstanceInfo(Instance img) {
        try {
            this.instanceUID = img.getSop_iuid();
            this.imagePosition = img.getImagePosition();
            this.imageOrientation = img.getImageOrientation();
            this.imageType = img.getImageType();
            this.pixelSpacing = img.getPixelSpacing();
            this.row = img.getRow();
            this.column = img.getColumn();
            this.frameOfReferenceUID = img.getFrameOfReferenceUID().equalsIgnoreCase("") ? "" : img.getFrameOfReferenceUID();
            this.referencedSOPInstanceUID = img.getReferenceSOPInstanceUID().equalsIgnoreCase("") ? "" : img.getReferenceSOPInstanceUID();
            this.instanceNumber = img.getInstanceNumber();
            this.sliceLocation = img.getSliceLocation();
        } catch (Exception e) {
            System.out.println("[ImagePanel]" + e.getMessage());
        }
    }

    public void previousInstance() {
        for (Study study : MainScreen.studyList) {
            if (study.getStudyInstanceUID().equalsIgnoreCase(this.studyUID)) {
                for (Series series : study.getSeriesList()) {
                    if (ApplicationContext.databaseRef.getMultiframeStatus()) {
                        if (!series.isMultiframe() && series.getSeriesInstanceUID().equalsIgnoreCase(seriesUID)) {
                            totalInstance = series.getImageList().size();
                            instanceArray = (ArrayList<Instance>) series.getImageList();
                            selectPreviousInstance();
                        }
                    } else {
                        if (series.getSeriesInstanceUID().equalsIgnoreCase(seriesUID)) {
                            totalInstance = series.getImageList().size();
                            instanceArray = (ArrayList<Instance>) series.getImageList();
                            selectPreviousInstance();
                        }
                    }
                }
            }
        }
    }

    public void nextInstance() {
        for (Study study : MainScreen.studyList) {
            if (study.getStudyInstanceUID().equalsIgnoreCase(studyUID)) {
                for (Series series : study.getSeriesList()) {
                    if (ApplicationContext.databaseRef.getMultiframeStatus()) {
                        if (!series.isMultiframe() && series.getSeriesInstanceUID().equalsIgnoreCase(seriesUID)) {
                            totalInstance = series.getImageList().size();
                            instanceArray = (ArrayList<Instance>) series.getImageList();
                            selectNextInstance();
                        }
                    } else {
                        if (series.getSeriesInstanceUID().equalsIgnoreCase(seriesUID)) {
                            totalInstance = series.getImageList().size();
                            instanceArray = (ArrayList<Instance>) series.getImageList();
                            selectNextInstance();
                        }
                    }
                }
            }
        }
    }

    public ScoutLineInfoModel[] getScoutBorder() {
        ScoutLineInfoModel[] borderArray = null;
        for (Study study : MainScreen.studyList) {
            if (study.getStudyInstanceUID().equalsIgnoreCase(studyUID)) {
                for (Series series : study.getSeriesList()) {
                    if (series.getSeriesInstanceUID().equalsIgnoreCase(seriesUID)) {
                        totalInstance = series.getImageList().size();
                        instanceArray = (ArrayList<Instance>) series.getImageList();
                        borderArray = prepareScoutBorder();
                    }
                }
            }
        }
        return borderArray;
    }

    private ScoutLineInfoModel[] prepareScoutBorder() {
        Instance firstInstance = instanceArray.get(0);
        Instance lastInstance = instanceArray.get(this.totalInstance - 1);
        ScoutLineInfoModel borderLine1 = new ScoutLineInfoModel();
        ScoutLineInfoModel borderLine2 = new ScoutLineInfoModel();

        borderLine1.setImagePosition(firstInstance.getImagePosition());
        borderLine1.setImageOrientation(firstInstance.getImageOrientation());
        borderLine1.setImagePixelSpacing(firstInstance.getPixelSpacing());
        borderLine1.setImageRow(firstInstance.getRow());
        borderLine1.setImageColumn(firstInstance.getColumn());
        borderLine1.setImageFrameofReferenceUID(firstInstance.getFrameOfReferenceUID());
        borderLine1.setImageReferenceSOPInstanceUID(firstInstance.getReferenceSOPInstanceUID());

        borderLine2.setImagePosition(lastInstance.getImagePosition());
        borderLine2.setImageOrientation(lastInstance.getImageOrientation());
        borderLine2.setImagePixelSpacing(lastInstance.getPixelSpacing());
        borderLine2.setImageRow(lastInstance.getRow());
        borderLine2.setImageColumn(lastInstance.getColumn());
        borderLine2.setImageFrameofReferenceUID(lastInstance.getFrameOfReferenceUID());
        borderLine2.setImageReferenceSOPInstanceUID(lastInstance.getReferenceSOPInstanceUID());

        ScoutLineInfoModel[] borderArray = new ScoutLineInfoModel[2];
        borderArray[0] = borderLine1;
        borderArray[1] = borderLine2;
        return borderArray;
    }

    public int getTotalInstance() {
        return totalInstance;
    }

    public void setTotalInstance(int totalInstance) {
        this.totalInstance = totalInstance;
    }

    public int getCurrentInstanceNo() {
        return currentInstanceNo;
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

    private void clearProbeValues() {

        Graphics g = this.getCanvas().getLayeredCanvas().textOverlay.getGraphics();
        Color c = this.getCanvas().getLayeredCanvas().textOverlay.getBackground();
        g.setColor(Color.white);
        g.fillRect(0, this.getCanvas().getLayeredCanvas().textOverlay.getHeight() - 70, 200, 30);
    }

    private String calculateHU(int x, int y) {
        try {
            pixelValueArray = currentbufferedimage.getSampleModel().getPixel(
                    x, y, (int[]) null,
                    currentbufferedimage.getRaster().getDataBuffer());
            pixelValue = pixelValueArray[0];
            try {
                hu = Integer.toString(pixelValue * Integer.parseInt(rescaleSlope) + Integer.parseInt(rescaleIntercept));
            } catch (Exception e) {
                hu = Integer.toString(pixelValue * 1 - 1024);
            }
        } catch (Exception e) {
            System.out.println("Array index out of bound exception");
        }

        return hu;
    }

    public int[] getPixels(int x, int y, int w, int h) {
        return currentbufferedimage.getSampleModel().getPixels(x, y, w, h, (int[]) null, currentbufferedimage.getRaster().getDataBuffer());
    }

    public void mouseMoved(MouseEvent e) {
        mouseLocX2 = e.getX();
        mouseLocY2 = e.getY();
        if (probeFlag) {
            String probeParameter[] = new String[3];
            probeParameter[0] = Integer.toString((int) Math.round(mouseLocX2 / this.scaleFactor));
            probeParameter[1] = Integer.toString((int) Math.round(mouseLocY2 / this.scaleFactor));
            probeParameter[2] = calculateHU((int) Math.round(mouseLocX2 / this.scaleFactor), (int) Math.round(mouseLocY2 / this.scaleFactor));
            this.getCanvas().getLayeredCanvas().textOverlay.setProbeParameters(probeParameter);
        }
    }

    public float getRescaleIntercept() {
        if (rescaleIntercept != null && !rescaleIntercept.equalsIgnoreCase("")) {
            return Float.parseFloat(rescaleIntercept);
        } else {
            return 0.0f;
        }
    }

    public void setRescaleIntercept(String rescaleIntercept) {
        this.rescaleIntercept = rescaleIntercept;
    }

    public float getRescaleSlope() {
        if (rescaleSlope != null && !rescaleSlope.equalsIgnoreCase("")) {
            return Float.parseFloat(rescaleSlope);
        } else {
            return 0.0f;
        }
    }

    public void setRescaleSlope(String rescaleSlope) {
        this.rescaleSlope = rescaleSlope;
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
        return frameOfReferenceUID;
    }

    public String getImageOrientation() {
        return imageOrientation;
    }

    public String getImagePosition() {
        return imagePosition;
    }

    public String getPixelSpacing() {
        return pixelSpacing;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public String getReferencedSOPInstanceUID() {
        return referencedSOPInstanceUID;
    }

    private void designContext() {
        ArrayList<Series> seriesList = ApplicationContext.databaseRef.getSeriesList(this.studyUID);
        JMenu menu;
        if ((this.studyDesc == null) || (this.studyDesc.equalsIgnoreCase(""))) {
            menu = new JMenu(this.studyUID);
        } else {
            menu = new JMenu(this.studyDesc);
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
                    if (ApplicationContext.databaseRef.getMultiframeStatus()) {
                        changeSeries(arg0, series.getStudyInstanceUID(), series.getSeriesInstanceUID(), series.isMultiframe(), series.getInstanceUID());
                    } else {
                        changeSeries(arg0, series.getStudyInstanceUID(), series.getSeriesInstanceUID());
                    }
                }
            });
        }
        jPopupMenu1.removeAll();
        jPopupMenu1.add(menu);

        createOtherPatientStudiesMenu(jPopupMenu1);
        this.setComponentPopupMenu(jPopupMenu1);
    }
    JMenu studyMenu = null;

    public void createOtherPatientStudiesMenu(JPopupMenu mainMenu) {

        if (canvas.getLayeredCanvas().getComparedWithStudies() != null) {
            //Other Studies
            mainMenu.addSeparator();
            for (String s : canvas.getLayeredCanvas().getComparedWithStudies()) {
                if (!s.equalsIgnoreCase(this.studyUID)) {
                    studyMenu = new JMenu(ApplicationContext.databaseRef.getPatientNameBasedonStudyUID(s));
                    ArrayList<Series> seriesList = ApplicationContext.databaseRef.getSeriesList(s);
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
                            studyMenu.add(menuitem);
                        } else {
                            studyMenu.add(menu1);
                        }
                        menuitem.addActionListener(new java.awt.event.ActionListener() {

                            public void actionPerformed(ActionEvent arg0) {
                                if (ApplicationContext.databaseRef.getMultiframeStatus()) {
                                    changeSeries(arg0, series.getStudyInstanceUID(), series.getSeriesInstanceUID(), series.isMultiframe(), series.getInstanceUID());
                                } else {
                                    changeSeries(arg0, series.getStudyInstanceUID(), series.getSeriesInstanceUID());
                                }
                            }
                        });
                    }

                    mainMenu.add(studyMenu);
                }
            }

        }
    }

    public boolean isInstanceArray() {
        if (this.instanceArray != null) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isSynchornizeTiles() {
        return synchornizeTiles;
    }

    public void doSynchronize() {
        if (synchornizeTiles) {
            synchornizeTiles = false;
        } else {
            synchornizeTiles = true;
            syncStartInstance = this.currentInstanceNo;
            SynchronizationDelegate.setSyncStartInstanceInAllTiles();
        }
    }

    public boolean canBeProcessed() {
        boolean temp = false;
        if (modality.startsWith("CT")) {
            temp = true;
        }
        return temp;
    }

    public int getSyncDifference() {
        return currentInstanceNo - syncStartInstance;
    }

    public void updateSyncStartInstance() {
        syncStartInstance = this.currentInstanceNo;
    }

    public int getSyncStartInstance() {
        return syncStartInstance;
    }

    private void changeSeries(ActionEvent e, String studyUID, String seriesUID) {
        SeriesChooserDelegate seriesChooser = new SeriesChooserDelegate(studyUID, seriesUID, this.getCanvas().getLayeredCanvas());
    }

    private void changeSeries(ActionEvent e, String studyUID, String seriesUID, boolean multiframe, String instanceUID) {
        SeriesChooserDelegate seriesChooser = new SeriesChooserDelegate(studyUID, seriesUID, multiframe, instanceUID, this.getCanvas().getLayeredCanvas());
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPopupMenu jPopupMenu1;
    // End of variables declaration//GEN-END:variables
}
