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
import in.raster.mayam.models.Series;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.dcm4che2.tool.dcm2xml.Dcm2Xml;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;

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
    String sopUid;
    ArrayList<Integer> selectedInstances = new ArrayList<Integer>(0);

    /**
     * Creates new form ViewerPreviewPanel
     */
    public ViewerPreviewPanel(String studyInstanceUid, String seriesInstanceUid, String seriesDescription, int totalImages, Thumbnail[] thumbnails, boolean isMultiframe, String instanceUid, boolean isVideo) {
        initComponents();
        this.studyInstanceUid = studyInstanceUid;
        this.seriesInstanceUid = seriesInstanceUid;
        this.seriesDescription = seriesDescription;
        this.totalImages = totalImages;
        if (totalImages == 0) {
            totalImages = 1;
        }
        this.thumbnails = thumbnails;
        this.SeriesLabel.setText(seriesDescription);
        this.selectedButton = three;
        this.isMultiframe = isMultiframe;
        this.sopUid = instanceUid;
        this.isVideo = isVideo;
        setLayout(null);
        createComponents();
        addMouseAdapter();
    }

    public ViewerPreviewPanel(String StudyUid, Series series, String instanceUid) {
        initComponents();
        this.studyInstanceUid = StudyUid;
        this.seriesInstanceUid = series.getSeriesInstanceUID();
        this.seriesDescription = series.getSeriesDesc();
        this.selectedButton = three;
        isMultiframe = (instanceUid == null ? false : true);
        this.sopUid = instanceUid != null ? instanceUid.split(",")[0] : null;
        this.isVideo = series.isVideo();
        totalImages = isVideo || isMultiframe ? 1 : series.getSeriesRelatedInstance();
        if (!isVideo) {
            if (!isMultiframe) {
                SeriesLabel.setText(seriesDescription + "-Images: " + series.getSeriesRelatedInstance());
            } else {
                SeriesLabel.setText("Multiframe-Frames: " + instanceUid.split(",")[1]);
            }
        } else {
            SeriesLabel.setText("");
        }
        setLayout(null);
        constructComponents();
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(SeriesLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(labelPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(imagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(button)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(SeriesLabel)
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
    // End of variables declaration//GEN-END:variables

    private void constructComponents() {
        SeriesLabel.setBounds(0, 0, 220, 20);
        labelPanelHeight = 9;
        labelPanel.setLayout(null);
        button.setBounds(207, 21, 14, 14);
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
            labelPanel.setBounds(0, 23, 205, labelPanelHeight);
        }
        colorLabels(selectedButton);
        imagePanel.setLayout(null);
        imagePanel.setBounds(0, 23 + labelPanelHeight + 5, 230, 76);
        totalHeight = 23 + labelPanelHeight + 76 + 5;
        selectedInstances.add(0);
    }

    private void createComponents() {
        SeriesLabel.setBounds(0, 0, 220, 20);
        labelPanelHeight = 9;
        labelPanel.setLayout(null);
        button.setBounds(207, 21, 14, 14);
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
            colorLabels(all);
        }

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
            labelPanel.setBounds(0, 23, 205, labelPanelHeight);
        }
        colorLabels(selectedButton);
        imagePanel.setLayout(null);
        int imagePanelHeight = loadThreePreviewImages();
        imagePanel.setBounds(0, 23 + labelPanelHeight + 5, 230, imagePanelHeight);
        totalHeight = 23 + labelPanelHeight + imagePanelHeight + 5;
        selectedInstances.add(0);
    }

    private void performButtonAction() {
        if (three.equals(button.getName())) {
            button.setIcon(imgOne);
            button.setName(one);
            selectedButton = one;
            colorLabels(selectedButton);
            ApplicationContext.setAllSeriesIdentification(studyInstanceUid);
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
            ApplicationContext.setAllSeriesIdentification(studyInstanceUid);
            int imagePanelHeight = loadAllPreviewImages();
            alignComponents(imagePanelHeight);
        } else {
            button.setIcon(imgThree);
            button.setName(three);
            selectedButton = three;
            colorLabels(selectedButton);
            ApplicationContext.setAllSeriesIdentification(studyInstanceUid);
            int imagePanelHeight = loadThreePreviewImages();
            alignComponents(imagePanelHeight);
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

    public int loadThreePreviewImages() {
        int xPos = 0, yPos = 0, hei = 76;
        imagePanel.removeAll();
        imagePanel.setLayout(null);
        if (thumbnails.length <= 3) {
            for (int i = 0; i < thumbnails.length; i++) {
                imagePanel.add(thumbnails[i]);
                thumbnails[i].setVisible(true);
                thumbnails[i].setBounds(xPos, yPos, 76, 76);
                xPos += 76;
            }
        } else {
            imagePanel.add(thumbnails[0]);
            thumbnails[0].setVisible(true);
            thumbnails[0].setBounds(xPos, yPos, 76, 76);
            xPos += 76;

            imagePanel.add(thumbnails[thumbnails.length / 2]);
            thumbnails[thumbnails.length / 2].setVisible(true);
            thumbnails[thumbnails.length / 2].setBounds(xPos, yPos, 76, 76);
            xPos += 76;

            imagePanel.add(thumbnails[thumbnails.length - 1]);
            thumbnails[thumbnails.length - 1].setVisible(true);
            thumbnails[thumbnails.length - 1].setBounds(xPos, yPos, 76, 76);
            xPos += 76;
        }
        return hei;
    }

    public int loadAllPreviewImages() {
        imagePanel.removeAll();
        imagePanel.setLayout(null);
        int xPos = 0, yPos = 0, hei = 0;

        for (int i = 0; i < thumbnails.length; i++) {
            imagePanel.add(thumbnails[i]);
            thumbnails[i].setVisible(true);
            thumbnails[i].setBounds(xPos, yPos, 76, 76);

            //To position the images
            if (xPos + 76 < 220) {
                xPos += 76;
            } else {
                hei += 76;
                xPos = 0;
                yPos += 76;
            }
        }
        if (totalImages % 3 != 0) {
            hei += 76;
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
                ApplicationContext.imgView.getImageToolbar().deselectLoopChk();
                ApplicationContext.imgView.getImageToolbar().resetCineTimer();
                String iuid = ((JLabel) me.getSource()).getName();
                if (!isVideo) {
                    ApplicationContext.layeredCanvas.clearThumbnailSelection();
                    ApplicationContext.imgView.getImageToolbar().enableImageTools();
                    if (ApplicationContext.selectedPanel != null) {
                        if (ApplicationContext.selectedPanel.getComponentCount() == 1) {
                            if (sopUid == null) {
                                if (ApplicationContext.layeredCanvas.imgpanel.getSeriesUID().equals(seriesInstanceUid) && !ApplicationContext.layeredCanvas.imgpanel.isMultiFrame()) {
                                    for (int i = 0; i < thumbnails.length; i++) {
                                        if (thumbnails[i].getName().equals(iuid)) {
                                            ApplicationContext.layeredCanvas.imgpanel.selectImage(i);
                                            break;
                                        }
                                    }
                                } else {
                                    for (int i = 0; i < thumbnails.length; i++) {
                                        if (thumbnails[i].getName().equals(iuid)) {
                                            ApplicationContext.layeredCanvas.imgpanel.changeSeries(studyInstanceUid, seriesInstanceUid, iuid, i);
                                            break;
                                        }
                                    }
                                }
                            } else {
                                ApplicationContext.layeredCanvas.imgpanel.changeSeries(studyInstanceUid, seriesInstanceUid, iuid, 0);
                            }
                        } else {
                            int clickedImg = 0;
                            for (int i = 0; i < thumbnails.length; i++) {
                                if (thumbnails[i].getName().equals(iuid)) {
                                    clickedImg = i + 1;
                                    int x = clickedImg % ApplicationContext.selectedPanel.getComponentCount();
                                    if (seriesInstanceUid.equals(ApplicationContext.layeredCanvas.imgpanel.getSeriesUID())) {
                                        if (x > 0) {
                                            setImageUpdatorArgs(clickedImg - x);
                                            ApplicationContext.layeredCanvas.imgpanel.displayImages(ApplicationContext.selectedPanel, clickedImg - x, true);
                                        } else {
                                            setImageUpdatorArgs(clickedImg - ApplicationContext.selectedPanel.getComponentCount());
                                            ApplicationContext.layeredCanvas.imgpanel.displayImages(ApplicationContext.selectedPanel, clickedImg - ApplicationContext.selectedPanel.getComponentCount(), true);
                                        }
                                    } else {
                                        ApplicationContext.layeredCanvas.imgpanel.changeSeries(studyInstanceUid, seriesInstanceUid, iuid, i, ApplicationContext.selectedPanel);
                                    }
                                    break;
                                }
                            }
                        }
                    } else {
                        JSplitPane splitPane = ((JSplitPane) ApplicationContext.tabbedPane.getSelectedComponent());
                        JPanel parent = (JPanel) splitPane.getRightComponent();
                        parent.removeAll();
                        GridLayout g = new GridLayout(1, 1);
                        parent = new JPanel(g);
                        JPanel container = new JPanel(g);
                        container.setBackground(Color.BLACK);
                        File dicomFile = new File(ApplicationContext.databaseRef.getFileLocation(studyInstanceUid, seriesInstanceUid, iuid));
                        for (int i = 0; i < thumbnails.length; i++) {
                            if (thumbnails[i].getName().equals(iuid)) {
                                LayeredCanvas canvas = new LayeredCanvas(dicomFile, i, false);
                                container.add(canvas, 0);
                                parent.add(container);
                                parent.setName(studyInstanceUid);
                                canvas.imgpanel.getTextOverlayParam().setCurrentInstance(i);
                                container.setName(canvas.imgpanel.getTextOverlayParam().getPatientName());
                                break;
                            }
                        }
                        ((JSplitPane) ApplicationContext.tabbedPane.getSelectedComponent()).setRightComponent(parent);
                        ApplicationContext.layeredCanvas = ((LayeredCanvas) ((JPanel) ((JPanel) ((JSplitPane) splitPane).getRightComponent()).getComponent(0)).getComponent(0));
                        ApplicationContext.selectedPanel = ((JPanel) ((JPanel) ((JSplitPane) splitPane).getRightComponent()).getComponent(0));
                        ApplicationContext.imgView.getImageToolbar().setWindowing();
                        ApplicationContext.layeredCanvas.imgpanel.repaint();
                        ApplicationContext.layeredCanvas.imgpanel.setCurrentSeriesAnnotation();
                        ApplicationContext.layeredCanvas.canvas.setSelectionColoring();
                        ApplicationContext.layeredCanvas.imgpanel.invalidate();
                    }
                } else {
                    ApplicationContext.selectedPanel = null;
                    ApplicationContext.layeredCanvas = null;
                    JPanel panel = (JPanel) ((JSplitPane) ApplicationContext.tabbedPane.getSelectedComponent()).getRightComponent();
                    if (panel instanceof VideoPanel) {
                        ((VideoPanel) panel).stopTimer();
                    }
                    panel.removeAll();
                    panel = null;
                    VideoPanel videoPanel = new VideoPanel();
                    ((JSplitPane) ApplicationContext.tabbedPane.getSelectedComponent()).setRightComponent(videoPanel);
                    videoPanel.setName(studyInstanceUid);
                    videoPanel.setBorder(BorderFactory.createLineBorder(new Color(255, 138, 0)));
                    videoPanel.setUniqueIdentifier(iuid);
                    EmbeddedMediaPlayerComponent mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
                    videoPanel.setMediaPlayer(mediaPlayerComponent);
                    ApplicationContext.imgView.getImageToolbar().disableImageTools();
                    mediaPlayerComponent.getMediaPlayer().playMedia(ApplicationContext.databaseRef.getFileLocation(studyInstanceUid, seriesInstanceUid, iuid));
                    videoPanel.startTimer();
                    ApplicationContext.setVideoThumbnailIdentification(studyInstanceUid);
                }
            }
        };
        for (int i = 0; i < thumbnails.length; i++) {
            thumbnails[i].addMouseListener(mouseClickAdapter1);
        }
    }

    private void setImageUpdatorArgs(int selectedImage) {
        if (totalImages > ApplicationContext.buffer.getDefaultBufferSize()) {
            int tiles = ApplicationContext.selectedPanel.getComponentCount();
            if (selectedImage - tiles > 0 && selectedImage + tiles < totalImages) {
                ApplicationContext.buffer.updateFrom(selectedImage - tiles);
            } else {
                ApplicationContext.buffer.updateFrom(totalImages - tiles);
            }
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
        if (!isVideo && sopUid == null) {
            String thumbnailLocation = ApplicationContext.databaseRef.getThumbnailLocation(studyInstanceUid, seriesInstanceUid);
            if (!thumbnailLocation.contains("Thumbnails")) {
                thumbnailLocation += File.separator + "Thumbnails";
            }
            ArrayList<String> instanceUidList = ApplicationContext.databaseRef.getInstanceUidList(studyInstanceUid, seriesInstanceUid);
            thumbnails = new Thumbnail[instanceUidList.size()];
            for (int i = 0; i < instanceUidList.size(); i++) {
                thumbnails[i] = new Thumbnail(instanceUidList.get(i));
                try {
//                    thumbnails[i].setImage(ImageIO.read(new File(thumbnailLocation + File.separator + "Thumbnails" + File.separator + instanceUidList.get(i))));
                    thumbnails[i].setImage(ImageIO.read(new File(thumbnailLocation + File.separator + instanceUidList.get(i))));
                } catch (IOException ex) {
                    thumbnails[i].setDefaultImage();
                }
            }
            loadThreeThumbnails();
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
    }

    private void loadThreeThumbnails() {
        int xPos = 0, yPos = 0;
        imagePanel.removeAll();
        imagePanel.setLayout(null);
        if (thumbnails.length <= 3) {
            for (int i = 0; i < thumbnails.length; i++) {
                imagePanel.add(thumbnails[i]);
                thumbnails[i].setVisible(true);
                thumbnails[i].setBounds(xPos, yPos, 76, 76);
                xPos += 76;
            }
        } else {
            imagePanel.add(thumbnails[0]);
            thumbnails[0].setVisible(true);
            thumbnails[0].setBounds(xPos, yPos, 76, 76);
            xPos += 76;

            imagePanel.add(thumbnails[thumbnails.length / 2]);
            thumbnails[thumbnails.length / 2].setVisible(true);
            thumbnails[thumbnails.length / 2].setBounds(xPos, yPos, 76, 76);
            xPos += 76;

            imagePanel.add(thumbnails[thumbnails.length - 1]);
            thumbnails[thumbnails.length - 1].setVisible(true);
            thumbnails[thumbnails.length - 1].setBounds(xPos, yPos, 76, 76);
            xPos += 76;
            imagePanel.revalidate();
            imagePanel.repaint();
        }
        getParent().repaint();
    }

    public void loadVideoImage() {
        thumbnails = new Thumbnail[1];
        thumbnails[0] = new Thumbnail(sopUid);
        thumbnails[0].setVideoImage();

        loadThreeThumbnails();
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
            colorLabels(all);
        }
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
//                thumbnails[0].setImage(ImageIO.read(new File(fileLocation + File.separator + "Thumbnails" + File.separator + sopUid)));
                thumbnails[0].setImage(ImageIO.read(new File(fileLocation + File.separator + sopUid)));
            } catch (IOException ex) {
                thumbnails[0].setDefaultImage();
            }
            loadThreeThumbnails();
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
                colorLabels(all);
            }
            addMouseAdapter();
        }
    }

    public void convertVideo() {
        if (isVideo) {
            String fileLocation = ApplicationContext.databaseRef.getFileLocation(studyInstanceUid, seriesInstanceUid, sopUid);
            File videoFile = null;
            if (!fileLocation.contains("_V")) {
                videoFile = new File(fileLocation + "_V" + File.separator + "video.xml");
                videoFile.getParentFile().mkdirs();
                try {
                    videoFile.createNewFile();
                } catch (IOException ex) {
                    Logger.getLogger(ViewerPreviewPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
                Dcm2Xml.main(new String[]{fileLocation, "-X", "-o", videoFile.getAbsolutePath()});
            } else {
                videoFile = new File(new File(fileLocation).getParentFile().getParent(), "video.xml");
            }
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
                SeriesLabel.setText("Video : " + (int) Math.floor((Double.parseDouble(numberOfFrames.getTextContent()) * (1000 / Double.parseDouble(frameTime.getTextContent()))) / 1000) + " Sec");
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(ViewerPreviewPanel.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SAXException ex) {
                Logger.getLogger(ViewerPreviewPanel.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ViewerPreviewPanel.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathExpressionException ex) {
                Logger.getLogger(ViewerPreviewPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
