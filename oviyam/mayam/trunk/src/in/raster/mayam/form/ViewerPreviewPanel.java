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

import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.models.treetable.SeriesNode;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Vector;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
//
//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.NameValuePair;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.message.BasicNameValuePair;
//import org.apache.http.util.EntityUtils;

/**
 *
 * @author Devishree
 * @version 2.0
 */
public class ViewerPreviewPanel extends javax.swing.JPanel {

    int totalImages, totalHeight;
    Thumbnail[] thumbnails;
    String studyInstanceUid, seriesInstanceUid, seriesDescription, selectedButton;
    ImageIcon imgOne = new ImageIcon(getClass().getResource("/in/raster/mayam/form/images/one.png"));
    ImageIcon imgThree = new ImageIcon(getClass().getResource("/in/raster/mayam/form/images/three.png"));
    ImageIcon imgAll = new ImageIcon(getClass().getResource("/in/raster/mayam/form/images/all.png"));
    static final String three = "three", one = "one", all = "all";
    ArrayList<JLabel> labelList = new ArrayList<JLabel>();
    int labelPanelHeight;
    MouseAdapter mouseClickAdapter1 = null;
    boolean isMultiframe = false, isVideo = false;
    String sopUid = null;
    ArrayList<Integer> selectedInstances = new ArrayList<Integer>(0);

    /**
     * Creates new form ViewerPreviewPanel
     */
    public ViewerPreviewPanel(String StudyUid, SeriesNode series, String instanceUid) {
        initComponents();
        this.studyInstanceUid = StudyUid;
        this.seriesInstanceUid = series.getSeriesUID();
        this.seriesDescription = series.getSeriesDesc();
        this.selectedButton = three;
        this.sopUid = instanceUid != null ? instanceUid.split(",")[0] : null;
        this.isVideo = series.isVideo();
        this.isMultiframe = series.isMultiframe();
        totalImages = isVideo || isMultiframe ? 1 : Integer.parseInt(series.getSeriesRelatedInstance());
        if (!isVideo) {
            if (!isMultiframe) {
                SeriesLabel.setText(seriesDescription);
                SeriesLabel.setToolTipText(seriesDescription);
                totalImagesLbl.setText(totalImages + " Imgs");
            } else {
                SeriesLabel.setText("Multiframe");
                totalImagesLbl.setText(instanceUid.split(",")[1] + " Frames");
            }
        } else {
            SeriesLabel.setText("Video");
        }
        setLayout(null);
        constructComponents();
    }

    public ViewerPreviewPanel(int id) {
        initComponents();
        setName(String.valueOf(id));
        setVisible(true);
    }

    public void normalPreview(String studyUid, String seriesUID, String seriesDesc, int totalInstances) {
        this.studyInstanceUid = studyUid;
        this.seriesInstanceUid = seriesUID;
        this.seriesDescription = seriesDesc != null ? seriesDesc : "";
        this.totalImages = totalInstances;
        isMultiframe = isVideo = false;
        SeriesLabel.setText(seriesDescription);
        SeriesLabel.setToolTipText(seriesDescription);
        totalImagesLbl.setText(totalImages + " Imgs");
        this.selectedButton = three;
        setLayout(null);
        constructComponents();
    }

    public void multiframePreview(String StudyUid, String seriesUID, String instanceUid) {
        this.studyInstanceUid = StudyUid;
        this.seriesInstanceUid = seriesUID;
        this.seriesDescription = "Multiframe";
        this.sopUid = instanceUid.split(",")[0];
        this.selectedButton = all;
        totalImages = 1;
        SeriesLabel.setText(seriesDescription);
        totalImagesLbl.setText(instanceUid.split(",")[1] + " Frames");
        this.isMultiframe = true;
        this.isVideo = false;
        setLayout(null);
        constructComponents();
    }

    public void videoPreview(String studyUid, String seriesUid, String instanceUid) {
        this.studyInstanceUid = studyUid;
        this.seriesInstanceUid = seriesUid;
        this.seriesDescription = "Video";
        this.sopUid = instanceUid.split(",")[0];
        this.selectedButton = all;
        totalImages = 1;
        SeriesLabel.setText(seriesDescription);
        totalImagesLbl.setText(instanceUid.split(",")[1] + " Frames");
        this.isMultiframe = false;
        this.isVideo = true;
        thumbnails = new Thumbnail[]{new Thumbnail(sopUid)};
        thumbnails[0].setVideoImage();
        setLayout(null);
        constructComponents();
        loadThreePreviews();
        button.setIcon(imgAll);
        addMouseAdapter();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        SeriesLabel = new javax.swing.JLabel();
        labelPanel = new javax.swing.JPanel();
        button = new javax.swing.JButton();
        imagePanel = new javax.swing.JPanel();
        totalImagesLbl = new javax.swing.JLabel();

        SeriesLabel.setFont(ApplicationContext.textFont);
        SeriesLabel.setText("jLabel1");

        javax.swing.GroupLayout labelPanelLayout = new javax.swing.GroupLayout(labelPanel);
        labelPanel.setLayout(labelPanelLayout);
        labelPanelLayout.setHorizontalGroup(
            labelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 245, Short.MAX_VALUE)
        );
        labelPanelLayout.setVerticalGroup(
            labelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 72, Short.MAX_VALUE)
        );

        button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/three.png"))); // NOI18N

        javax.swing.GroupLayout imagePanelLayout = new javax.swing.GroupLayout(imagePanel);
        imagePanel.setLayout(imagePanelLayout);
        imagePanelLayout.setHorizontalGroup(
            imagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        imagePanelLayout.setVerticalGroup(
            imagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 70, Short.MAX_VALUE)
        );

        totalImagesLbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        totalImagesLbl.setText("0 sec");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(SeriesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(totalImagesLbl))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(labelPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(imagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(button)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(SeriesLabel)
                    .addComponent(totalImagesLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(button)
                    .addComponent(labelPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(imagePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(27, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel SeriesLabel;
    private javax.swing.JButton button;
    private javax.swing.JPanel imagePanel;
    private javax.swing.JPanel labelPanel;
    private javax.swing.JLabel totalImagesLbl;
    // End of variables declaration//GEN-END:variables

    private void constructComponents() {
        int width = 230;
        int textWidth = SeriesLabel.getFontMetrics(SeriesLabel.getFont()).stringWidth(SeriesLabel.getText());
        if (textWidth + 80 < width) {
            SeriesLabel.setBounds(0, 0, textWidth, 20);
            totalImagesLbl.setBounds(SeriesLabel.getWidth() + 1, 0, width - SeriesLabel.getWidth(), 20);
        } else {
            SeriesLabel.setBounds(0, 0, 150, 20);
            totalImagesLbl.setBounds(151, 0, 80, 20);
        }
        totalHeight = 22;
        labelPanelHeight = 9;
        labelPanel.setLayout(null);
        button.setBounds(207, totalHeight + 3, 14, 14);
        int xPos = 0, yPos = 0;
        for (int l = 0; l < totalImages; l++) {
            JLabel label = new JLabel(" ");
            label.setOpaque(true);
            label.setBounds(xPos, yPos, 5, 5);
            if (xPos + 7 < 200) {
                xPos += 7;
            } else {
                xPos = 0;
                yPos += 7;
                labelPanelHeight += 7;
            }
            labelList.add(label);
            labelPanel.add(label);
            labelPanel.setBounds(0, totalHeight + 5, 205, labelPanelHeight);
        }
        totalHeight += labelPanelHeight;
        colorLabels(selectedButton);
        imagePanel.setLayout(null);
        imagePanel.setBounds(0, totalHeight + 10, 230, 76);
        totalHeight += imagePanel.getHeight() + 5;
        selectedInstances.add(0);
    }

    private void performButtonAction() {
        if (three.equals(button.getName())) {
            button.setIcon(imgOne);
            button.setName(one);
            selectedButton = one;
            colorLabels(selectedButton);
            ((ViewerJPanel) ApplicationContext.tabbedPane.getSelectedComponent()).setSeriesIdentification();
            if (thumbnails.length > 1) {
                for (int i = 1; i < thumbnails.length; i++) {
                    thumbnails[i].setVisible(false);
                }
            }
        } else if (one.equals(button.getName())) {
            button.setIcon(imgAll);
            button.setName(all);
            selectedButton = all;
            colorLabels(selectedButton);
            ((ViewerJPanel) ApplicationContext.tabbedPane.getSelectedComponent()).setSeriesIdentification();
            int imagePanelHeight = loadAllPreviewImages();
            alignComponents(imagePanelHeight);
        } else {
            button.setIcon(imgThree);
            button.setName(three);
            selectedButton = three;
            colorLabels(selectedButton);
            ((ViewerJPanel) ApplicationContext.tabbedPane.getSelectedComponent()).setSeriesIdentification();
            alignComponents(loadThreePreviews());
        }
    }

    public int getTotalHeight() {
        return totalHeight;
    }

    public int getLabelPanelHeight() {
        return labelPanelHeight;
    }

    private void colorLabels(String selectedButton) {
        for (int i = 0; i < labelList.size(); i++) {
            labelList.get(i).setBackground(Color.lightGray);
        }
        if ("one".equals(selectedButton)) {
            labelList.get(0).setBackground(Color.BLUE);
        } else if ("three".equals(selectedButton)) {
            if (totalImages >= 3) {
                labelList.get(0).setBackground(Color.BLUE);
                labelList.get(totalImages / 2).setBackground(Color.BLUE);
                labelList.get(totalImages - 1).setBackground(Color.BLUE);
            } else {
                for (int i = 0; i < labelList.size(); i++) {
                    labelList.get(i).setBackground(Color.BLUE);
                }
            }
        } else {
            for (int i = 0; i < labelList.size(); i++) {
                labelList.get(i).setBackground(Color.BLUE);
            }
        }
    }

    public int loadThreePreviews() {
        int xPos = 0, yPos = 0;
        imagePanel.removeAll();
        imagePanel.setLayout(null);

        if (totalImages <= 3) {
            for (int i = 0; i < thumbnails.length; i++) {
                imagePanel.add(thumbnails[i]);
                thumbnails[i].setVisible(true);
                thumbnails[i].setBounds(xPos, yPos, thumbnails[i].getWidth(), thumbnails[i].getHeight());
                xPos += thumbnails[i].getWidth();
            }
        } else {
            imagePanel.add(thumbnails[0]);
            thumbnails[0].setVisible(true);
            thumbnails[0].setBounds(xPos, yPos, thumbnails[0].getWidth(), thumbnails[0].getHeight());
            xPos += thumbnails[0].getWidth();

            imagePanel.add(thumbnails[thumbnails.length / 2]);
            thumbnails[thumbnails.length / 2].setVisible(true);
            thumbnails[thumbnails.length / 2].setBounds(xPos, yPos, thumbnails[thumbnails.length / 2].getWidth(), thumbnails[thumbnails.length / 2].getHeight());
            xPos += thumbnails[thumbnails.length / 2].getWidth();

            imagePanel.add(thumbnails[thumbnails.length - 1]);
            thumbnails[thumbnails.length - 1].setVisible(true);
            thumbnails[thumbnails.length - 1].setBounds(xPos, yPos, thumbnails[thumbnails.length - 1].getWidth(), thumbnails[thumbnails.length - 1].getHeight());
            xPos += thumbnails[thumbnails.length - 1].getWidth();
        }
        return thumbnails[0].getHeight();
    }

    public int loadAllPreviewImages() {
        imagePanel.removeAll();
        imagePanel.setLayout(null);
        int xPos = 0, yPos = 0, hei = 0;

        for (int i = 0; i < thumbnails.length; i++) {
            imagePanel.add(thumbnails[i]);
            thumbnails[i].setVisible(true);
            thumbnails[i].setBounds(xPos, yPos, thumbnails[i].getWidth(), thumbnails[i].getHeight());

            //To position the images
            if (xPos + thumbnails[i].getWidth() < 220) {
                xPos += thumbnails[i].getWidth();
            } else {
                hei += thumbnails[i].getHeight();
                xPos = 0;
                yPos += thumbnails[i].getHeight();
            }
        }
        if (totalImages % 3 != 0) {
            hei += thumbnails[0].getHeight();
        }
        imagePanel.setBounds(0, 23 + labelPanelHeight + 5, 230, hei);
        return hei;
    }

    private void alignComponents(int imagePanelHeight) {
        int locationY = 0, pos, size = 0;
        int modifiedPanel = Integer.parseInt(getName());
        JPanel parent = (JPanel) this.getParent();
        Component[] components = parent.getComponents();
        totalHeight = 23 + this.getLabelPanelHeight() + imagePanelHeight + 5;
        if (modifiedPanel != 0) {
            for (int i = 0; i < modifiedPanel; i++) {
                locationY += ((ViewerPreviewPanel) components[i]).getTotalHeight() + 5;
            }
        }
        this.setBounds(0, locationY, 230, totalHeight);
        parent.revalidate();
        parent.repaint();
        pos = ((ViewerPreviewPanel) components[modifiedPanel]).getY() + totalHeight + 5;
        for (int i = modifiedPanel + 1; i < components.length; i++) {
            ((ViewerPreviewPanel) components[i]).setLocation(0, pos);
            pos += ((ViewerPreviewPanel) components[i]).getTotalHeight() + 5;
            parent.revalidate();
            parent.repaint();
        }
        for (int i = 0; i < components.length; i++) {
            size += ((ViewerPreviewPanel) components[i]).getTotalHeight() + 5;
        }
        ((ImagePreviewPanel) ((JScrollPane) ((JViewport) ((JPanel) parent.getParent()).getParent()).getParent()).getParent()).paint(size);
    }

    public String getSeriesInstanceUid() {
        return seriesInstanceUid;
    }

    private void addMouseAdapter() {
        mouseClickAdapter1 = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                String iuid = ((JLabel) me.getSource()).getName();
                ViewerJPanel comp = (ViewerJPanel) ApplicationContext.tabbedPane.getSelectedComponent();
                comp.stopAutoPlay();
                if (!isVideo) {
                    if (!comp.isVideoDisplay()) {
                        comp.thumbnailClicked();
                        LayeredCanvas selectedCanvas = comp.getSelectedCanvas();
                        if (!comp.isTileLayout()) { //Either no layout or image layout
                            if (sopUid == null && !selectedCanvas.imgpanel.isMultiFrame()) { //Not Multiframe
                                if (selectedCanvas.imgpanel.getSeriesUID().equals(seriesInstanceUid)) { //Same series
                                    for (int i = 0; i < thumbnails.length; i++) {
                                        if (thumbnails[i].getName().equals(iuid)) {
                                            selectedCanvas.imgpanel.selectImage(i);
                                            break;
                                        }
                                    }
                                } else {  //Other series
                                    for (int i = 0; i < thumbnails.length; i++) {
                                        if (thumbnails[i].getName().equals(iuid)) {
                                            selectedCanvas.imgpanel.changeSeries(studyInstanceUid, seriesInstanceUid, iuid, i);
                                            break;
                                        }
                                    }
                                }
                            } else { //Select Multiframe or from Multiframe change to normal images
                                selectedCanvas.imgpanel.changeSeries(studyInstanceUid, seriesInstanceUid, iuid, 0);
                            }
                        } else { // Tile layout
                            int clickedImg = 0;
                            for (int i = 0; i < thumbnails.length; i++) {
                                if (thumbnails[i].getName().equals(iuid)) {
                                    clickedImg = i + 1;
                                    JPanel parent = ((JPanel) selectedCanvas.getParent());
                                    int x = clickedImg % parent.getComponentCount();
                                    if (seriesInstanceUid.equals(selectedCanvas.imgpanel.getSeriesUID())) {
                                        if (x > 0) {
                                            setImageUpdatorArgs(clickedImg - x, parent);
                                            selectedCanvas.imgpanel.displayImages(parent, clickedImg - x, true);
                                        } else {
                                            setImageUpdatorArgs(clickedImg - parent.getComponentCount(), parent);
                                            selectedCanvas.imgpanel.displayImages(parent, clickedImg - parent.getComponentCount(), true);
                                        }
                                    } else {
                                        selectedCanvas.imgpanel.changeSeries(studyInstanceUid, seriesInstanceUid, iuid, i, parent);
                                    }
                                    break;
                                }
                            }
                        }
                    } else { //From video display to normal image display
                        for (int ins_iter = 0; ins_iter < thumbnails.length; ins_iter++) {
                            if (thumbnails[ins_iter].getName().equals(iuid)) {
                                comp.addLayeredCanvas(new LayeredCanvas(new File(ApplicationContext.databaseRef.getFileLocation(studyInstanceUid, seriesInstanceUid, iuid)), ins_iter, false));
                                comp.updateTextOverlay();
                                comp.enableAllImageTools();
                                break;
                            }
                        }

                    }
                } else { //Change the video
                    if (comp.isVideoDisplay()) {
                        comp.stopVideoTimer();
                    }
                    comp.createVideoCanvas(ApplicationContext.databaseRef.getFileLocation(studyInstanceUid, seriesInstanceUid, iuid), studyInstanceUid);
                }
            }
        };
        for (int i = 0; i < thumbnails.length; i++) {
            thumbnails[i].addMouseListener(mouseClickAdapter1);
        }
    }

    private void setImageUpdatorArgs(int selectedImage, JPanel parent) {
        if (totalImages > ApplicationContext.buffer.getDefaultBufferSize()) {
            int tiles = parent.getComponentCount();
            ApplicationContext.buffer.updateFrom(selectedImage - tiles);
            ApplicationContext.buffer.clearBuffer();
        }
    }

    public void clearAllSelectedColor() {
        colorLabels(selectedButton);
    }

    public boolean isMultiframe() {
        return isMultiframe;
    }

    public String getSopUid() {
        return sopUid;
    }

    public void setSopUid(String sopUid) {
        this.sopUid = sopUid;
    }

    public String getStudyInstanceUid() {
        return studyInstanceUid;
    }

    public void clearSelectedInstances() {
        for (int i = 0; i < selectedInstances.size(); i++) {
            if (totalImages <= 3 || selectedButton.equals(all) || selectedInstances.get(i) == 0 || (!selectedButton.equals(one) && selectedInstances.get(i) == totalImages / 2) || (!selectedButton.equals(one) && selectedInstances.get(i) == totalImages - 1)) {
                labelList.get(selectedInstances.get(i)).setBackground(Color.BLUE);
            } else {
                labelList.get(selectedInstances.get(i)).setBackground(Color.lightGray);
            }
        }
        selectedInstances.clear();
    }

    public void setSelectedInstance(int instanceNumber) {
        if (instanceNumber >= 0 && instanceNumber < totalImages) {
            selectedInstances.add(instanceNumber);
            labelList.get(instanceNumber).setBackground(Color.RED);
        }
    }

    public void loadThumbnails() {
        if (!(isMultiframe || isVideo)) {
            String thumbnailLocation = ApplicationContext.databaseRef.getThumbnailLocation(studyInstanceUid, seriesInstanceUid);
            if (!thumbnailLocation.contains("Thumbnails")) {
                thumbnailLocation += File.separator + "Thumbnails";
            }
            ArrayList<String> instanceUidList = ApplicationContext.databaseRef.getInstanceUidList(studyInstanceUid, seriesInstanceUid);
            thumbnails = new Thumbnail[instanceUidList.size()];
            for (int i = 0; i < instanceUidList.size(); i++) {
                thumbnails[i] = new Thumbnail(instanceUidList.get(i));
                try {
                    thumbnails[i].setImage(ImageIO.read(new FileInputStream(new File(thumbnailLocation + File.separator + instanceUidList.get(i)))));
                } catch (IOException ex) {
                    ApplicationContext.logger.log(Level.INFO, "ViewerPreviewPanel - Unable to load thumbnail");
                    thumbnails[i].setDefaultImage();
                }
            }
            loadThreePreviews();
            if (totalImages > 3) {
                button.setName(three);
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        performButtonAction();
                    }
                });
            } else {
                button.setIcon(imgAll);
            }
            addMouseAdapter();
        } else if (isMultiframe) {
            loadMultiframes();
        }
    }

    public void loadVideoImage() {
        thumbnails = new Thumbnail[1];
        thumbnails[0] = new Thumbnail(sopUid);
        thumbnails[0].setVideoImage();

        loadThreePreviews();
        selectedButton = all;
        button.setIcon(imgAll);
        addMouseAdapter();
    }

    public void loadMultiframes() {
        if (!isVideo && sopUid != null) {
            thumbnails = new Thumbnail[1];
            thumbnails[0] = new Thumbnail(sopUid);
            String fileLocation = ApplicationContext.databaseRef.getThumbnailLocation(studyInstanceUid, seriesInstanceUid);
            if (!fileLocation.contains("Thumbnails")) {
                fileLocation += File.separator + "Thumbnails";
            }
            try {
                thumbnails[0].setImage(ImageIO.read(new File(fileLocation + File.separator + sopUid)));
            } catch (IOException ex) {
                thumbnails[0].setDefaultImage();
            }
            loadThreePreviews();
            button.setIcon(imgAll);
            addMouseAdapter();
        }
    }

    public void convertVideo() {
        if (isVideo) {
            String fileLocation = ApplicationContext.databaseRef.getFileLocation(studyInstanceUid, seriesInstanceUid, sopUid);
            File videoFile = null;
            videoFile = new File(new File(fileLocation).getParentFile().getParent(), "video.xml");
            try {
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(videoFile);
                NodeList elementsByTagName1 = doc.getElementsByTagName("item");
                for (int i = 0; i < elementsByTagName1.getLength(); i++) {
                    NamedNodeMap attributes = elementsByTagName1.item(i).getAttributes();
                    if (attributes.getNamedItem("src") != null) {
                        videoFile = new File(fileLocation + "_V" + File.separator + attributes.getNamedItem("src").getNodeValue());
                        videoFile.renameTo(new File(videoFile.getAbsolutePath() + ".mpg"));
                        ApplicationContext.databaseRef.update("image", "FileStoreUrl", videoFile.getAbsolutePath() + ".mpg", "SopUID", fileLocation.substring(fileLocation.lastIndexOf(File.separator) + 1));
                    }
                }
                XPath xPath = XPathFactory.newInstance().newXPath();
                XPathExpression expr = xPath.compile("//attr[@tag=\"00181063\"]");
                NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
                Node frameTime = nl.item(0);
                expr = xPath.compile("//attr[@tag=\"00280008\"]");
                nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
                Node numberOfFrames = nl.item(0);
                totalImagesLbl.setText((int) Math.floor((Double.parseDouble(numberOfFrames.getTextContent()) * (1000 / Double.parseDouble(frameTime.getTextContent()))) / 1000) + " Sec");
            } catch (ParserConfigurationException ex) {
                ApplicationContext.logger.log(Level.INFO, "ViewerPreviewPanel", ex);
            } catch (SAXException ex) {
                ApplicationContext.logger.log(Level.INFO, "ViewerPreviewPanel", ex);
            } catch (IOException ex) {
                ApplicationContext.logger.log(Level.INFO, "ViewerPreviewPanel", ex);
            } catch (XPathExpressionException ex) {
                ApplicationContext.logger.log(Level.INFO, "ViewerPreviewPanel", ex);
            }
        }
    }

    public void loadThumbnails(Vector<Dataset> instance_List, String wadoProtocol, String hostName, int wadoPort) {
        thumbnails = new Thumbnail[instance_List.size()];
        int i = 0;
        Calendar today = Calendar.getInstance(ApplicationContext.currentLocale);
        String dest = ApplicationContext.listenerDetails[2] + File.separator + today.get(Calendar.YEAR) + File.separator + today.get(Calendar.MONTH) + File.separator + today.get(Calendar.DATE) + File.separator + studyInstanceUid + File.separator + seriesInstanceUid + File.separator + "Thumbnails" + File.separator;

        for (int j = 0; j < instance_List.size(); j++) {
            String objUID = instance_List.get(i).getString(Tags.SOPInstanceUID);
            String wadoRequest = wadoProtocol + "://" + hostName + ":" + wadoPort + "/wado?requestType=WADO&studyUID=" + studyInstanceUid + "&seriesUID=" + seriesInstanceUid + "&objectUID=" + objUID + "&rows=75" + "&columns=75";
            thumbnails[i++] = new Thumbnail(wadoRequest, dest + objUID, objUID);

        }
        loadThreePreviews();
        if (totalImages > 3) {
            button.setName(three);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    performButtonAction();
                }
            });
        } else {
            button.setIcon(imgAll);
        }
        addMouseAdapter();
    }

    public void loadThumbnail(Dataset instanceData, String wadoProtocol, String hostName, int wadoPort) {
        thumbnails = new Thumbnail[1];
        Calendar today = Calendar.getInstance(ApplicationContext.currentLocale);
        String dest = ApplicationContext.listenerDetails[2] + File.separator + today.get(Calendar.YEAR) + File.separator + today.get(Calendar.MONTH) + File.separator + today.get(Calendar.DATE) + File.separator + studyInstanceUid + File.separator + seriesInstanceUid + File.separator + "Thumbnails" + File.separator;
        String objUID = instanceData.getString(Tags.SOPInstanceUID);
        String wadoRequest = wadoProtocol + "://" + hostName + ":" + wadoPort + "/wado?requestType=WADO&studyUID=" + studyInstanceUid + "&seriesUID=" + seriesInstanceUid + "&objectUID=" + objUID + "&rows=75" + "&columns=75";
        thumbnails[0] = new Thumbnail(wadoRequest, dest + objUID, objUID);
        thumbnails[0].load();
        loadThreePreviews();
        button.setIcon(imgAll);
        addMouseAdapter();
    }

    //Direct launch from XML
    public void loadThumbnails(ArrayList<String> instances, String wadoProtocol, String wadoContext, String hostName, int wadoPort) {
        thumbnails = new Thumbnail[instances.size()];
        Calendar today = Calendar.getInstance(ApplicationContext.currentLocale);
        String dest = ApplicationContext.listenerDetails[2] + File.separator + today.get(Calendar.YEAR) + File.separator + today.get(Calendar.MONTH) + File.separator + today.get(Calendar.DATE) + File.separator + studyInstanceUid + File.separator + seriesInstanceUid + File.separator + "Thumbnails" + File.separator;

        for (int i = 0; i < instances.size(); i++) {
            String objUID = instances.get(i).split(",")[1];
            String wadoRequest = wadoProtocol + "://" + hostName + ":" + wadoPort + "/wado?requestType=WADO&studyUID=" + studyInstanceUid + "&seriesUID=" + seriesInstanceUid + "&objectUID=" + objUID + "&rows=75" + "&columns=75";
            thumbnails[i] = new Thumbnail(wadoRequest, dest + objUID, objUID);
        }

        loadThreePreviews();
        if (totalImages > 3) {
            button.setName(three);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    performButtonAction();
                }
            });
        } else {
            button.setIcon(imgAll);
        }
        addMouseAdapter();
    }
//    private void loadThumbnails(NavigableMap<Integer, String> instance_List, ServerModel serverDetails) {TODO
//        thumbnails = new Thumbnail[instance_List.size()];
//        int i = 0;
//        Calendar today = Calendar.getInstance(ApplicationContext.currentLocale);
//        String dest = ApplicationContext.listenerDetails[2] + File.separator + today.get(Calendar.YEAR) + File.separator + today.get(Calendar.MONTH) + File.separator + today.get(Calendar.DATE) + File.separator + studyInstanceUid + File.separator + seriesInstanceUid + File.separator + "Thumbnails" + File.separator;
//
//        CloseableHttpClient httpclient = HttpClients.createDefault();        
//        Iterator<Integer> iterator = instance_List.navigableKeySet().iterator();
//        while (iterator.hasNext()) {
//            String objUID = instance_List.get(iterator.next()).split(",")[1];
//            String wadoRequest = serverDetails.getWadoProtocol() + "://" + serverDetails.getHostName() + ":" + serverDetails.getWadoPort() + "/wado?requestType=WADO&studyUID=" + studyInstanceUid + "&seriesUID=" + seriesInstanceUid + "&objectUID=" + objUID + "&rows=75" + "&columns=75";
//            System.out.println(wadoRequest);
//            //            thumbnails[i++] = new Thumbnail(wadoRequest, dest + objUID, objUID);
//            try {
//                HttpGet httpGet = new HttpGet(wadoRequest);
//                CloseableHttpResponse response1 = httpclient.execute(httpGet);
//                try {
//                    if(response1.getStatusLine().getStatusCode()>=200 && response1.getStatusLine().getStatusCode()<300) {
//                        HttpEntity entity1 = response1.getEntity();
//                        OutputStream stream = new FileOutputStream(new File(dest + objUID));
//                        entity1.writeTo(stream);
//                        // do something useful with the response body
//                        // and ensure it is fully consumed
//                        EntityUtils.consume(entity1);
//                    } else {
//                        System.out.println("Unable to get wado object for thumbnail.");
//                    }
//                } finally {
//                    response1.close();
//                }
//            } catch (IOException ex) {
//                Logger.getLogger(ViewerPreviewPanel.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
////        loadThreePreviewImages();
////        if (totalImages > 3) {
////            button.setName(three);
////            button.addActionListener(new ActionListener() {
////                @Override
////                public void actionPerformed(ActionEvent e) {
////                    performButtonAction();
////                }
////            });
////        } else {
////            button.setIcon(imgAll);
////        }
////        addMouseAdapter();
//
//    }

    public String load(boolean loadMultiframes) {
        if (!(isMultiframe || isVideo)) {
            for (int i = 0; i < thumbnails.length; i++) {
                thumbnails[i].load();
            }
        } else if (loadMultiframes) {
            displayMultiframes();
        }
        return seriesInstanceUid;
    }

    public void displayMultiframes() {
        if (isMultiframe) {
            thumbnails = new Thumbnail[]{new Thumbnail(sopUid)};
            Calendar today = Calendar.getInstance(ApplicationContext.currentLocale);
            String dest = ApplicationContext.listenerDetails[2] + File.separator + today.get(Calendar.YEAR) + File.separator + today.get(Calendar.MONTH) + File.separator + today.get(Calendar.DATE) + File.separator + studyInstanceUid + File.separator + seriesInstanceUid + File.separator + "Thumbnails" + File.separator + sopUid;
            thumbnails[0].readImage(dest);
            loadThreePreviews();
            button.setIcon(imgAll);
            addMouseAdapter();
        }
    }

    public void multiframes(String wadoProtocol, String wadoContext, String hostName, int wadoPort) {
        thumbnails = new Thumbnail[1];
        Calendar today = Calendar.getInstance(ApplicationContext.currentLocale);
        String dest = ApplicationContext.listenerDetails[2] + File.separator + today.get(Calendar.YEAR) + File.separator + today.get(Calendar.MONTH) + File.separator + today.get(Calendar.DATE) + File.separator + studyInstanceUid + File.separator + seriesInstanceUid + File.separator + "Thumbnails" + File.separator + sopUid;
        String wadoRequest = wadoProtocol + "://" + hostName + ":" + wadoPort + "/wado?requestType=WADO&studyUID=" + studyInstanceUid + "&seriesUID=" + seriesInstanceUid + "&objectUID=" + sopUid + "&rows=75" + "&columns=75";
        thumbnails[0] = new Thumbnail(wadoRequest, dest, sopUid);
        thumbnails[0].load();
        loadThreePreviews();
        button.setIcon(imgAll);
        addMouseAdapter();
    }
}
