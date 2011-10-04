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
import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.delegate.ShowImageViewDelegate;
import in.raster.mayam.delegate.StudyListUpdator;
import in.raster.mayam.exception.ImageReadingException;
import in.raster.mayam.model.Instance;
import in.raster.mayam.model.Series;
import in.raster.mayam.model.Study;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import org.dcm4che.dict.Tags;
import org.dcm4che.image.ColorModelFactory;
import org.dcm4che.image.ColorModelParam;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;
import org.dcm4che.data.Dataset;
import org.dcm4che.imageio.plugins.DcmMetadata;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.io.DicomInputStream;

/**
 *
 * @author  BabuHussain
 * @version 0.5 
 *
 */
public class WindowingImagePanel extends javax.swing.JPanel implements MouseWheelListener,
        MouseMotionListener, MouseListener {

    private WindowingCanvas canvas;
    private boolean newBufferedImage = false;
    private boolean firstTime = true;
    public static String tool = "";
    private String patientName;
    private int windowLevel;
    private int windowWidth;
    private int WC;
    private int WW;
    private double pixelSpacingX;
    private double pixelSpacingY;
    private String patientID;
    private String sex;
    private String studyDate;
    private String studyTime;
    private String modality;
    private String studyUID;
    private String seriesUID;
    private String instanceUID;
    private String bodyPartExamined;
    private String slicePosition;
    private String patientPosition;
    private String institutionName;
    private Image loadedImage;
    private ImageInputStream iis;
    private Iterator iter;
    private ImageReader reader;
    private Dataset dataset;
    private BufferedImage currentbufferedimage;
    private BufferedImage image;
    private int mouseLocX1;
    private int mouseLocX2;
    private int mouseLocY1;
    private int mouseLocY2;
    private ColorModelParam cmParam = null;
    private static final ColorModelFactory cmFactory = ColorModelFactory.getInstance();
    private int windowingMultiplier = 1;
    private int originalWidth;
    private int originalHeight;
    private ColorModel cm = null;
    private ImageIcon imageIcon;
    private String aspectRatio[];
    private float floatAspectRatio;
    // private MediaTracker tracker;
    private String dicomFileUrl;
    private ArrayList<Instance> instanceArray = null;
    private int thumbWidth = 384;
    private int thumbHeight = 384;
    private int maxHeight = 384;
    private int maxWidth = 384;
    private double thumbRatio;
    private int startX = 0;
    private int startY = 0;
    //multiframe variables
    private int nFrames = 0;
    private int currentFrame = 0;
    private boolean mulitiFrame = false;
    private double currentScaleFactor = 1;
    private double initialPixelSpacingX;
    private double initialPixelSpacingY;
    private String pixelSpacing;
    private int row;
    private int column;
    private double scaleFactor = 1;
    private String imageOrientation;
    private PDFFile curFile = null;
    private int curpage = -1;
    private boolean isEncapsulatedDocument = false;

    public WindowingImagePanel() {
        initComponents();
    }

    /**
     * Constructs the imagepanel by passing file url and outer canvas
     * @param dicomFileUrl
     * @param canvas
     */
    public WindowingImagePanel(String dicomFileUrl, WindowingCanvas canvas) {
        this.dicomFileUrl = dicomFileUrl;
        this.canvas = canvas;
        try {
            initComponents();
            readDicomFile(new File(dicomFileUrl));
            retrievePatientInformation();
            retrieveInstanceInformation();
            addlisteners();
            setTotalInstance();
            //setSizing();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSizing() {
        this.thumbHeight = this.getSize().height;
        this.thumbWidth = this.getSize().width;
    }

    /**
     * Constructs the imagepanel by passing file url parameter
     * @param dicomFileUrl
     */
    public WindowingImagePanel(String dicomFileUrl) {
        this.dicomFileUrl = dicomFileUrl;
        try {
            readDicomFile(new File(dicomFileUrl));
            retrievePatientInformation();
            initComponents();
            addlisteners();
            retrieveInstanceInformation();
            setTotalInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This routine used to retrive the patient related information
     */
    private void retrievePatientInformation() {
        patientName = dataset.getString(Tags.PatientName);
        patientID = dataset.getString(Tags.PatientID);
        sex = dataset.getString(Tags.PatientSex);
        modality = dataset.getString(Tags.Modality);
        studyDate = dataset.getString(Tags.StudyDate);
        studyTime = dataset.getString(Tags.StudyTime);
        studyUID = dataset.getString(Tags.StudyInstanceUID);
        seriesUID = dataset.getString(Tags.SeriesInstanceUID);
        MainScreen.selectedSeries = seriesUID;
        bodyPartExamined = dataset.getString(Tags.BodyPartExamined);
        slicePosition = dataset.getString(Tags.SliceLocation);
        patientPosition = dataset.getString(Tags.PatientPosition);
        institutionName = dataset.getString(Tags.InstitutionName);
        instanceUID = dataset.getString(Tags.SOPInstanceUID);
        aspectRatio = dataset.getStrings(Tags.PixelAspectRatio);
        try {

            currentInstanceNo = Integer.parseInt(dataset.getString(Tags.InstanceNumber));
        } catch (NumberFormatException e) {
            System.out.println("Instance Number format error currentInstanceNo: " + currentInstanceNo);
            currentInstanceNo = 0; // if Number error correct and put a correct number (1)
        } catch (NullPointerException e) {
            System.out.println("Instance number Null pointer error");
            currentInstanceNo = 0; // if Number error correct and put a correct number (1)
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
                windowLevel = c = (int) cmParam.getWindowCenter(0);
                windowWidth = w = (int) cmParam.getWindowWidth(0);
            } else {
                WW = windowWidth = w = (int) Math.pow(2, bits);
                WC = windowLevel = c = (int) w / 2;
            }
        }
        imageOrientation = dataset.getString(Tags.ImageOrientation) != null ? dataset.getString(Tags.ImageOrientation, 0) + "\\" + dataset.getString(Tags.ImageOrientation, 1) + "\\" + dataset.getString(Tags.ImageOrientation, 2) + "\\" + dataset.getString(Tags.ImageOrientation, 3) + "\\" + dataset.getString(Tags.ImageOrientation, 4) + "\\" + dataset.getString(Tags.ImageOrientation, 5) : null;
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

    public void forceGotoPage(int pagenum) {
        if (pagenum <= 0) {
            pagenum = 0;
        } else if (pagenum >= curFile.getNumPages()) {
            pagenum = curFile.getNumPages() - 1;
        }
        totalInstance = curFile.getNumPages();
        curpage = pagenum;
        PDFPage pg = curFile.getPage(pagenum + 1, true);

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
        Image img = pg.getImage(rect.width, rect.height, rect, null, true, true);
        imageIcon = new ImageIcon();
        imageIcon.setImage(current);
        loadedImage = imageIcon.getImage();
        image = null;
        repaint();
    }

    private void calculateResolutionForPdfDicom(double imageWidthParam, double imageHeightParam) {
        thumbHeight = 384;
        thumbWidth = 384;
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

    /**
     * Tdhis routine used to return the canvas of the image box
     * @return
     */
    public WindowingCanvas getCanvas() {
        return canvas;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getStudyUID() {
        return studyUID;
    }

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
            reader.setInput(iis, false);
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
            } finally {
                if (!isMulitiFrame()) {
                    iis.close();
                    iter = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (ImageReadingException e) {
            e.printStackTrace();
        }
    }

    public void setImage(Instance instance) {
        try {
            newBufferedImage = true;
            currentbufferedimage = instance.getPixelData();
            windowChanged(this.windowLevel, this.windowWidth);
            imageOrientation = instance.getImageOrientation();
        } catch (ImageReadingException e) {
            e.printStackTrace();
        }
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

    public int getnFrames() {
        return nFrames;
    }

    public void setnFrames(int nFrames) {
        this.nFrames = nFrames;
    }

    /**
     * This routine used to select the first instance of series
     */
    public void selectFirstInstance() {
        for (Study study : MainScreen.studyList) {
            if (study.getStudyInstanceUID().equalsIgnoreCase(ApplicationContext.imgPanel.getStudyUID())) {
                ArrayList<Series> seriesList = (ArrayList<Series>) study.getSeriesList();
                for (int i = 0; i < seriesList.size(); i++) {
                    Series series = seriesList.get(i);
                    if (series.getSeriesInstanceUID().equalsIgnoreCase(this.seriesUID)) {
                        Instance instance = series.getImageList().get(0);
                        //setImage(instance.getPixelData());
                        setImage(instance);
                    }
                }
            }
        }
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

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    /**
     * This override routine used to paint the image box
     * @param gs
     */
    @Override
    public void paintComponent(Graphics gs) {
        super.paintComponent(gs);
        Graphics2D g = (Graphics2D) gs;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        if (image != null) {
            calculateNewHeightAndWidthBasedonAspectRatio();
            g.drawImage(image, startX, startY, thumbWidth, thumbHeight, null);

        }
        if (firstTime) {
            centerImage();
            originalHeight = this.getSize().height;
            originalWidth = this.getSize().width;
            firstTime = false;
        }
        if (dataset.getString(Tags.SOPClassUID)!=null&&dataset.getString(Tags.SOPClassUID).equalsIgnoreCase("1.2.840.10008.5.1.4.1.1.104.1")) {
            calculateResolutionForPdfDicom(loadedImage.getWidth(null), loadedImage.getHeight(null));
            this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setCurrentInstance(curpage);
            this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setTotalInstance(Integer.toString(totalInstance));
            g.drawImage(loadedImage, startX, startY, thumbWidth, thumbHeight, null);

        }
    }
    int finalHeight;
    int finalWidth;

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

    /**
     * This routine used to convert the rgb image
     */
    public void convertToRGBImage() {

        imageIcon = new ImageIcon(currentbufferedimage);
        loadedImage = imageIcon.getImage();
        image = new BufferedImage(loadedImage.getWidth(null), loadedImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        g2.drawImage(loadedImage, 0, 0, null);
        imageIcon = null;
        loadedImage = null;

    }

    public void resizeHandler() {
        repaint();
        centerImage();
    }

    /**
     *This routine used to set the tool as null
     */
    public void setToolsToNull() {
        tool = "";
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

    /**
     * This routine used to calculate the x and y position of the image box to be placed in the
     * outer canvas and set the bounds
     */
    private void centerImage() {
        int xPosition = (canvas.getSize().width - this.getSize().width) / 2;
        int yPosition = (canvas.getSize().height - this.getSize().height) / 2;
        this.setBounds(xPosition, yPosition, this.getSize().width, this.getSize().height);

    }

    /**
     * This method creates the color model based on the window level and width
     * and apply the values to the image box
     * @param windowCenter
     * @param windowWidth
     */
    public void windowChanged(int windowCenter, int windowWidth) {
        try {
            if (cmParam != null) {
                cmParam = cmParam.update(windowCenter,
                        windowWidth, cmParam.isInverse());
                cm = cmFactory.getColorModel(cmParam);
                currentbufferedimage = new BufferedImage(cm, currentbufferedimage.getRaster(), false, null);
                this.windowLevel = windowCenter;
                this.windowWidth = windowWidth;
            }
            convertToRGBImage();
            repaint();
            changeTextOverlay();
        } catch (Exception e) {
            System.out.println("Windowing can't be changed");
        }

    }

    public ColorModel getColorModel() {
        cmParam = cmParam.update(this.windowLevel,
                this.windowWidth, cmParam.isInverse());
        cm = cmFactory.getColorModel(cmParam);
        return cm;
    }

    /**
     * This routine used to change the text overlay of the image box
     */
    public void changeTextOverlay() {
        this.getCanvas().getLayeredCanvas().textOverlay.setWindowingParameter(Integer.toString(this.windowLevel), Integer.toString(this.windowWidth));
    }

    private void addlisteners() {
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
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

    public String getBodyPartExamined() {
        return bodyPartExamined;
    }

    public void setBodyPartExamined(String bodyPartExamined) {
        this.bodyPartExamined = bodyPartExamined;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public String getPatientID() {
        return patientID;
    }

    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    public String getPatientPosition() {
        return patientPosition;
    }

    public void setPatientPosition(String patientPosition) {
        this.patientPosition = patientPosition;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getSlicePosition() {
        return slicePosition;
    }

    public void setSlicePosition(String slicePosition) {
        this.slicePosition = slicePosition;
    }

    public String getStudyDate() {
        return studyDate;
    }

    public void setStudyDate(String studyDate) {
        this.studyDate = studyDate;
    }

    public String getStudyTime() {
        return studyTime;
    }

    public void setStudyTime(String studyTime) {
        this.studyTime = studyTime;
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

        setBackground(new java.awt.Color(0, 0, 0));
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

    public void openSingleImage(String filePath) {
        if (!ApplicationContext.imageViewExist()) {
            ApplicationContext.createImageView();
        }
        ShowImageViewDelegate showImgView = new ShowImageViewDelegate(filePath);
        StudyListUpdator studyListUpdator = new StudyListUpdator();
        studyListUpdator.addStudyToStudyList(this.studyUID, MainScreen.studyList, filePath);

    }
    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
    }//GEN-LAST:event_formMouseClicked
    public double getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(double scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    private void setTotalInstance() {
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
        int notches = e.getWheelRotation();
        if (notches < 0) {
            if (ApplicationContext.databaseRef.getMultiframeStatus() && isMulitiFrame()) {
                showPreviousFrame();
            } else if (isEncapsulatedDocument) {
                previousofEncapsulatedDocument();

            } else {
                if (instanceArray == null) {
                    previousInstance();
                } else {
                    selectPreviousInstance();
                }
            }
        } else {

            if (ApplicationContext.databaseRef.getMultiframeStatus() && isMulitiFrame()) {
                showNextFrame();
            } else if (isEncapsulatedDocument) {
                nextofEncapsulatedDocument();

            } else {
                if (instanceArray == null) {
                    nextInstance();
                } else {
                    selectNextInstance();
                }
            }
        }
    }

    public boolean isMulitiFrame() {
        return mulitiFrame;
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
        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(ImagePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void selectPreviousInstance() {
        if (currentInstanceNo == 0) {
            currentInstanceNo = totalInstance;
        }
        currentInstanceNo--;
        Instance instance = instanceArray.get(currentInstanceNo);
        if(instance!=null)
            ApplicationContext.mainScreen.getCanvas().setFilePath(instance.getFilepath());        
        dicomFileUrl = instance.getFilepath();
        if (instance.isMultiframe()) {
            this.getCanvas().getLayeredCanvas().textOverlay.multiframeStatusDisplay(true);
            this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setFramePosition((instance.getCurrentFrameNum() + 1) + "/" + instance.getTotalNumFrames());
        } else {
            this.getCanvas().getLayeredCanvas().textOverlay.multiframeStatusDisplay(false);
            this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setFramePosition("");
        }
        setImage(instance);
        if (!ApplicationContext.databaseRef.getMultiframeStatus()) {
            this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setCurrentInstance(instance.getSeriesLevelIndex());
        } else {
            this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setCurrentInstance(this.currentInstanceNo);
        }
    }

    private void selectNextInstance() {
        currentInstanceNo++;
        if (currentInstanceNo == totalInstance) {
            currentInstanceNo = 0;
        }
        Instance instance = instanceArray.get(currentInstanceNo);
        if(instance!=null)
            ApplicationContext.mainScreen.getCanvas().setFilePath(instance.getFilepath());
        dicomFileUrl = instance.getFilepath();
        if (instance.isMultiframe()) {
            this.getCanvas().getLayeredCanvas().textOverlay.multiframeStatusDisplay(true);
            this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setFramePosition((instance.getCurrentFrameNum() + 1) + "/" + instance.getTotalNumFrames());
        } else {
            this.getCanvas().getLayeredCanvas().textOverlay.multiframeStatusDisplay(false);
            this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setFramePosition("");
        }
        setImage(instance);
        if (!ApplicationContext.databaseRef.getMultiframeStatus()) {
            this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setCurrentInstance(instance.getSeriesLevelIndex());
        } else {
            this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setCurrentInstance(this.currentInstanceNo);
        }
    }

    public void showMultiframeInfo() {
        Instance instance = instanceArray.get(currentInstanceNo);
        if (instance.isMultiframe()) {
            this.getCanvas().getLayeredCanvas().textOverlay.multiframeStatusDisplay(true);
            this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setFramePosition((instance.getCurrentFrameNum() + 1) + "/" + instance.getTotalNumFrames());
        } else {
            this.getCanvas().getLayeredCanvas().textOverlay.multiframeStatusDisplay(false);
            this.getCanvas().getLayeredCanvas().textOverlay.getTextOverlayParam().setFramePosition("");
        }
    }

    private void previousInstance() {
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

    private void nextInstance() {
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

    public String getImageOrientation() {
        return imageOrientation;
    }

    public void setImageOrientation(String imageOrientation) {
        this.imageOrientation = imageOrientation;
    }

    public void setFirstTime(boolean firstTime) {
        this.firstTime = firstTime;
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            openSingleImage(dicomFileUrl);
        }
    }

    public void mousePressed(MouseEvent e) {
        this.canvas.requestFocus();
        mouseLocX1 = e.getX();
        mouseLocY1 = e.getY();
    }

    public void mouseReleased(MouseEvent e) {
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
        windowChanged(windowLevel, windowWidth);
        windowLevel = (int) newWindowLevel;
        windowWidth = (int) newWindowWidth;
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

    public void mouseMoved(MouseEvent e) {
        mouseLocX2 = e.getX();
        mouseLocY2 = e.getY();
    }

    public String getModality() {
        return modality;
    }

    public void setModality(String modality) {
        this.modality = modality;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    private int currentInstanceNo = 0;
    private int totalInstance = 1;
    private boolean canStart = true;
}
