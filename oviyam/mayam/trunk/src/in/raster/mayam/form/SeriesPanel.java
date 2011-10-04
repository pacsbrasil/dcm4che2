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

import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.delegate.SeriesListUpdator;
import in.raster.mayam.delegate.ShowImageViewDelegate;
import in.raster.mayam.delegate.WindowingPanelLoader;
import in.raster.mayam.util.DicomTags;
import in.raster.mayam.util.DicomTagsReader;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;
import javax.swing.border.LineBorder;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.imageio.plugins.DcmMetadata;
import org.dcm4che2.data.Tag;

/**
 *
 * @author  BabuHussain
 * @version 0.5
 *
 */
public class SeriesPanel extends javax.swing.JPanel implements MouseListener {

    /** Creates new form SeriesPanel */
    //Image Reading variables.
    private Iterator iter;
    private ImageReader reader;
    private ImageInputStream iis;
    //Variables to store series details.
    private String studyUID = "";
    private String seriesUID = "";
    private String seriesDesc = "";
    private String modality = "";
    private String institutionName = "";
    private int totalInstace;
    private Dataset dataset;
    //Variables to store the series thumbnail image file path.
    private String fileUrl;
    //Variables to store the buffered image
    private Image loadedImage;
    private ImageIcon imageIcon;
    private BufferedImage currentbufferedimage;
    private BufferedImage image;
    private Thumbnail imageLabel;
    private boolean instanceListAdded = false;
    private int nFrames = 0;
    private boolean mulitiFrame = false;
    private String instanceUID = "";
    private boolean isEncapsulatedDocument = false;

    public SeriesPanel() {
        initComponents();
    }

    public SeriesPanel(String dicomFileUrl) {
        fileUrl = dicomFileUrl;
        try {
            readDicomFile(new File(dicomFileUrl));
            retrieveInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
        initComponents();
        this.addListeners();
        setHeaders();
        setNoSelectionColoring();


    }

    /**
     * This routine used to add the image in this container.
     */
    private void addImage() {
        imageLabel = new Thumbnail(image);
        imageLabel.setSize(96, 96);
        this.add(imageLabel);
        imageLabel.addMouseListener(this);
        imageLabel.setBounds(1, 1, 96, 96);
    }

    /**
     *
     */
    private void addListeners() {
        this.addMouseListener(this);
    }

    /**
     * This routine used to retrieve the dicom related details
     */
    private void retrieveInfo() {
        synchronized (this) {
            if(dataset==null)readDicomFile(new File(fileUrl));;
            studyUID = dataset.getString(Tags.StudyInstanceUID);
            seriesUID = dataset.getString(Tags.SeriesInstanceUID);
            if (ApplicationContext.databaseRef.getMultiframeStatus()) {
                setTotalInstacne();
            } else {
                totalInstace = ApplicationContext.databaseRef.getSeriesLevelInstance(studyUID, seriesUID);
            }
            seriesDesc = dataset.getString(Tags.SeriesDescription);
            modality = dataset.getString(Tags.Modality);
            institutionName = dataset.getString(Tags.InstitutionName);
            instanceUID = dataset.getString(Tags.SOPInstanceUID);
        }
    }

    /**
     * This routine used to update the instance list in the studylist array
     */
    public void updateInstanceList() {
        SeriesListUpdator series = new SeriesListUpdator(studyUID, seriesUID, instanceUID, isMulitiFrame(), false);
    }

    /**
     * This routine used to read the dicom file
     * @param selFile
     */
    private void readDicomFile(File selFile) {
        try {
            iis = ImageIO.createImageInputStream(selFile);
            ImageIO.scanForPlugins();
            iter = ImageIO.getImageReadersByFormatName("DICOM");
            reader = (ImageReader) iter.next();
            reader.setInput(iis, false);
            dataset = ((DcmMetadata) reader.getStreamMetadata()).getDataset();
            try {
                if (reader.getNumImages(true) > 0) {
                    currentbufferedimage = reader.read(0);
                }
                nFrames = reader.getNumImages(true);
                if (nFrames - 1 > 0) {
                    mulitiFrame = true;
                }
                if (reader.getNumImages(true) > 0) {
                    imageIcon = new ImageIcon();
                    imageIcon.setImage(currentbufferedimage);
                    loadedImage = imageIcon.getImage();
                    image = new BufferedImage(loadedImage.getWidth(null), loadedImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2 = image.createGraphics();
                    g2.drawImage(loadedImage, 0, 0, null);
                }
                if (dataset.getString(Tag.SOPClassUID).equalsIgnoreCase("1.2.840.10008.5.1.4.1.1.104.1")) {
                    isEncapsulatedDocument = true;
                    imageIcon = new ImageIcon();
                    imageIcon.setImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/in/raster/mayam/form/images/pdficon.png")));
                    loadedImage = imageIcon.getImage();
                    image = new BufferedImage(loadedImage.getWidth(null), loadedImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2 = image.createGraphics();
                    g2.drawImage(loadedImage, 0, 0, null);

                }
                addImage();
            } catch (RuntimeException e) {
                e.printStackTrace();
                System.out.println("while Reading Image " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        } finally {
            try {
                iis.close();
                iter = null;
                currentbufferedimage = null;
                imageIcon = null;
                loadedImage = null;
            } catch (Exception ex) {
                ex.printStackTrace();
                // Logger.getLogger(ThumbnailImage.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * This routine used to open the single image in the imageview.
     * @param filePath
     */
    public void openSingleImage(String filePath) {
        if (!ApplicationContext.imageViewExist()) {
            ApplicationContext.createImageView();
        }
        ShowImageViewDelegate showImgView = new ShowImageViewDelegate(filePath);
    }

    /**
     * This routine used to set the selection coloring
     */
    public void setSelectionColoring() {
        this.setBorder(new LineBorder(Color.BLACK, 2));
        this.setBackground(Color.black);
        seriesDescriptionText.setForeground(new Color(255, 195, 0));
        totalImagesText.setForeground(new Color(255, 195, 0));
        modalityText.setForeground(new Color(255, 195, 0));
        institutionText.setForeground(new Color(255, 195, 0));
    }

    /**
     * This routine used to remove the selection coloring.
     */
    public void setNoSelectionColoring() {
        this.setBackground(new Color(51, 51, 51));
        this.setBorder(new LineBorder(new Color(101, 101, 101)));
        seriesDescriptionText.setForeground(Color.gray);
        totalImagesText.setForeground(Color.gray);
        modalityText.setForeground(Color.gray);
        institutionText.setForeground(Color.gray);
    }

    private void setTotalInstacne() {

        if (!isMulitiFrame()) {
            totalInstace = ApplicationContext.databaseRef.getSeriesLevelInstance(this.studyUID, this.seriesUID);

        } else {
            totalInstace = nFrames;
        }
    }

    public boolean isMulitiFrame() {
        return mulitiFrame;
    }

    /**
     * This routine used to get the file url for this instance.
     * @return
     */
    public String getFileUrl() {
        return fileUrl;
    }

    /**
     * This routine used to set the file url for a specific instance.
     * @param fileUrl
     */
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    private void setHeaders() {
        seriesDescriptionText.setText("" + seriesDesc != null ? seriesDesc : "");
        if (!isEncapsulatedDocument) {
            totalImagesText.setText(this.totalInstace + " Images");
        } else {
            totalImagesText.setText("");
        }
        modalityText.setText("" + modality != null ? modality : "");
        institutionText.setText(institutionName != null ? institutionName : "");

    }

    public void mouseClicked(MouseEvent e) {
        if (ApplicationContext.selectedSeriesPanel != null) {
            ApplicationContext.selectedSeriesPanel.setNoSelectionColoring();
        }
        ApplicationContext.selectedSeriesPanel = this;
        ApplicationContext.selectedSeriesPanel.setSelectionColoring();
        if (e.getClickCount() == 2) {
            // openSingleImage(this.fileUrl);
        }
    }

    public void mousePressed(MouseEvent e) {
        if (!ApplicationContext.mainScreen.getCanvas().getFilePath().equalsIgnoreCase(this.getFileUrl())) {
            WindowingPanelLoader.loadImageOnWindowingPanel(this.getFileUrl());
            if (MainScreen.dicomTagsViewer.isVisible()) {
                ArrayList<DicomTags> dcmTags = DicomTagsReader.getTags(new File(this.getFileUrl()));
                MainScreen.dicomTagsViewer.setDataModelOnTable(dcmTags);
            } else ApplicationContext.mainScreen.getCanvas().setFilePath(this.getFileUrl());
        }
        if (!instanceListAdded) {
            updateInstanceList();
        }
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void clearAllMemoryReference() {
        this.removeMouseListener(this);
        this.removeAll();
        iter = null;
        reader = null;
        iis = null;
        dataset = null;
        loadedImage = null;
        imageIcon = null;
        currentbufferedimage = null;
        if (image != null) {
            image.flush();
        }
        image = null;
        fileUrl = null;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        seriesDescriptionText = new javax.swing.JLabel();
        modalityText = new javax.swing.JLabel();
        institutionText = new javax.swing.JLabel();
        totalImagesText = new javax.swing.JLabel();

        seriesDescriptionText.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        seriesDescriptionText.setText("s");

        modalityText.setText("s");

        institutionText.setText("s");

        totalImagesText.setText("s");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(112, 112, 112)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(totalImagesText, javax.swing.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(seriesDescriptionText, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
                            .addComponent(modalityText, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
                            .addComponent(institutionText, javax.swing.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(seriesDescriptionText)
                .addGap(2, 2, 2)
                .addComponent(modalityText)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(institutionText)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(totalImagesText)
                .addContainerGap(36, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel institutionText;
    private javax.swing.JLabel modalityText;
    private javax.swing.JLabel seriesDescriptionText;
    private javax.swing.JLabel totalImagesText;
    // End of variables declaration//GEN-END:variables
}
