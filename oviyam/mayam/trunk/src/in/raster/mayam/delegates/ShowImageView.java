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
import in.raster.mayam.form.ImagePreviewPanel;
import in.raster.mayam.form.LayeredCanvas;
import in.raster.mayam.form.Thumbnail;
import in.raster.mayam.form.VideoPanel;
import in.raster.mayam.form.ViewerPreviewPanel;
import in.raster.mayam.form.tab.component.ButtonTabComp;
import in.raster.mayam.models.Instance;
import in.raster.mayam.models.SeriesDisplayModel;
import in.raster.mayam.models.Series;
import in.raster.mayam.models.StudyAnnotation;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;

/**
 *
 * @author Devishree
 * @version 2.0
 */
public class ShowImageView {

    String studyUid;
    private boolean alreadyOpenedStudy = false;

    public ShowImageView(String filePath, String studyUid, String[] patientInfo, int startBufferingFrom) {
        this.studyUid = studyUid;
        showImageView(filePath, patientInfo, startBufferingFrom);
    }

    public ShowImageView(String videoFilePath, String studyUid, String[] patientInfo) {
        this.studyUid = studyUid;
        showVideo(videoFilePath, patientInfo);
    }

    private void showImageView(String filePath, String[] patientInfo, int instanceNoToStart) {
        ImagePreviewPanel imagePreviewPanel = new ImagePreviewPanel();
        imagePreviewPanel.setPatientInfo(patientInfo);
        GridLayout g = new GridLayout(1, 1);
        JPanel parent = new JPanel(g);
        JPanel container = new JPanel(g);
        container.setBackground(Color.BLACK);
        File dicomFile = new File(filePath);
        LayeredCanvas canvas = new LayeredCanvas(dicomFile, instanceNoToStart, false);
        if (ApplicationContext.tabbedPane != null) {
            ApplicationContext.imgView.getImageToolbar().enableImageTools();
            for (int i = 0; i < ApplicationContext.tabbedPane.getTabCount(); i++) { //If already opened study just set the tab selected which holds the study                
                if (((JPanel) ((JSplitPane) ApplicationContext.tabbedPane.getComponentAt(i)).getRightComponent()).getName().equals(studyUid)) {
                    alreadyOpenedStudy = true;
                    ApplicationContext.tabbedPane.setSelectedIndex(i);
                    break;
                }
            }
        }
        if (!alreadyOpenedStudy) { //If the study was not already opened create a new tab in image view
            container.add(canvas, 0);
            parent.add(container);
            parent.setName(studyUid);
            container.setName(canvas.imgpanel.getTextOverlayParam().getPatientName());
            CreateThumbnails createThumbnails = new CreateThumbnails(imagePreviewPanel, dicomFile.getParentFile().getParent());
            createThumbnails.start();
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, imagePreviewPanel, parent);
            splitPane.setOneTouchExpandable(true);
            splitPane.setDividerLocation(270);
            splitPane.setDividerSize(15);
            splitPane.setName(canvas.imgpanel.getTextOverlayParam().getPatientName());
            imagePreviewPanel.setMinimumSize(new Dimension(270, splitPane.getHeight()));
            setImgpanelToContext(splitPane);
            imagePreviewPanel.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent ce) {
                    resize();
                }
            });
        }
    }

    private void setImgpanelToContext(JSplitPane splitpane) {
        ApplicationContext.imgView.jTabbedPane1.add(splitpane);
        //The following lines are used for tab close button and event
        ButtonTabComp tabComp = new ButtonTabComp(ApplicationContext.imgView.jTabbedPane1);
        ApplicationContext.imgView.jTabbedPane1.setTabComponentAt(ApplicationContext.imgView.jTabbedPane1.getTabCount() - 1, tabComp);
        ApplicationContext.imgView.jTabbedPane1.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
        ApplicationContext.layeredCanvas = null;
        ApplicationContext.layeredCanvas = ((LayeredCanvas) ((JPanel) ((JPanel) ((JSplitPane) splitpane).getRightComponent()).getComponent(0)).getComponent(0));
        ApplicationContext.selectedPanel = ((JPanel) ((JPanel) ((JSplitPane) splitpane).getRightComponent()).getComponent(0));
        ApplicationContext.imgView.getImageToolbar().setWindowing();
        if (ApplicationContext.tabbedPane == null) {
            ApplicationContext.tabbedPane = (JTabbedPane) tabComp.getTabbedComponent();
        }
        ApplicationContext.tabbedPane.setSelectedIndex(ApplicationContext.tabbedPane.getTabCount() - 1);
        ApplicationContext.layeredCanvas.imgpanel.setScaleFactor(ApplicationContext.imgView.jTabbedPane1.getWidth(), ApplicationContext.imgView.jTabbedPane1.getHeight(), 1);
        ApplicationContext.layeredCanvas.annotationPanel.doZoomIn();
        ApplicationContext.layeredCanvas.imgpanel.repaint();
        ApplicationContext.layeredCanvas.canvas.setSelectionColoring();
        ApplicationContext.layeredCanvas.imgpanel.invalidate();
        ApplicationContext.imgView.setVisible(true);
    }

    public void setSelectedSeriesContext(ViewerPreviewPanel[] allPreviews, ArrayList<ViewerPreviewPanel> selectedPreviews, String studyDir, boolean isVideo) {
        if (!isVideo) {
            for (int i = 0; i < allPreviews.length; i++) {
                if (ApplicationContext.layeredCanvas.imgpanel.getSeriesUID().equals(allPreviews[i].getSeriesInstanceUid())) {
                    selectedPreviews.add(allPreviews[i]);
                    allPreviews[i].setSelectedInstance(ApplicationContext.layeredCanvas.imgpanel.getCurrentInstanceNo());
                    break;
                }
            }
            SeriesDisplayModel displayModel = new SeriesDisplayModel(studyUid, ApplicationContext.tabbedPane.getSelectedIndex(), selectedPreviews, allPreviews, ApplicationContext.layeredCanvas, studyDir);
            ApplicationContext.imgView.selectedSeriesDisplays.add(displayModel);
            ApplicationContext.imgView.selectedStudy = ApplicationContext.imgView.selectedSeriesDisplays.size() - 1;
            File annotationInfo = new File(studyDir, "info.ser");
            if (annotationInfo.isFile()) {
                try {
                    FileInputStream fis = new FileInputStream(annotationInfo);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    try {
                        displayModel.setStudyAnnotation((StudyAnnotation) ois.readObject());
                    } catch (ClassNotFoundException ex) {
//                    System.out.println("Class not found");
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ShowImageView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            ApplicationContext.layeredCanvas.imgpanel.setCurrentSeriesAnnotation();
        } else {
            String iuid = ((JPanel) ((VideoPanel) ((JSplitPane) ApplicationContext.imgView.jTabbedPane1.getSelectedComponent()).getRightComponent()).getComponent(0)).getName();
            for (int i = 0; i < allPreviews.length; i++) {
                if (allPreviews[i].getSopUid() != null && allPreviews[i].getSopUid().equals(iuid)) {
                    selectedPreviews.add(allPreviews[i]);
                    allPreviews[i].setSelectedInstance(0);
                    break;
                }
            }

            SeriesDisplayModel displayModel = new SeriesDisplayModel(studyUid, ApplicationContext.tabbedPane.getSelectedIndex(), selectedPreviews, allPreviews, ApplicationContext.layeredCanvas, studyDir);
            ApplicationContext.imgView.selectedSeriesDisplays.add(displayModel);
            ApplicationContext.imgView.selectedStudy = ApplicationContext.imgView.selectedSeriesDisplays.size() - 1;
        }
    }

    private void resize() {
        try {
            for (int i = 0; i < ApplicationContext.tabbedPane.getTabCount(); i++) {
                JPanel panel = ((JPanel) ((JSplitPane) ApplicationContext.tabbedPane.getComponentAt(i)).getRightComponent());
                if (panel.getComponent(0) instanceof LayeredCanvas) {
                    for (int j = 0; j < panel.getComponentCount(); j++) {
                        JPanel seriesLevelPanel = (JPanel) panel.getComponent(j);
                        for (int k = 0; k < seriesLevelPanel.getComponentCount(); k++) {
                            if (seriesLevelPanel.getComponent(k) instanceof LayeredCanvas) {
                                LayeredCanvas tempCanvas = (LayeredCanvas) seriesLevelPanel.getComponent(k);
                                if (tempCanvas.textOverlay != null) {
                                    tempCanvas.imgpanel.getTextOverlayParam().setViewSize(seriesLevelPanel.getComponent(k).getWidth() + " X " + seriesLevelPanel.getComponent(k).getHeight());
                                }
                            }
                        }
                    }
                }
            }
        } catch (NullPointerException npe) {
            //ignore : Null pointer occurs when there is no components
        }
    }

    public boolean isAlreadyOpenedStudy() {
        return alreadyOpenedStudy;
    }

    private class CreateThumbnails extends Thread {

        ImagePreviewPanel imagePreviewPanel;
        String studyDir;

        public CreateThumbnails(ImagePreviewPanel imagePreviewPanel, String studyDir) {
            this.imagePreviewPanel = imagePreviewPanel;
            this.studyDir = studyDir;
        }

        public void run() {
            int pos = 0, size = 0;
            Thumbnail[] thumbnails;
            ArrayList<ViewerPreviewPanel> selectedPreviews = new ArrayList<ViewerPreviewPanel>();
            ArrayList<Series> seriesList = ApplicationContext.databaseRef.getSeriesList_SepMulti(studyUid);
            ViewerPreviewPanel[] allPreviews = new ViewerPreviewPanel[seriesList.size()];
            for (int i = 0; i < seriesList.size(); i++) {
                List<Instance> imageList = seriesList.get(i).getImageList();
                String path = imageList.get(0).getFilepath();
                File file;
                if (!seriesList.get(i).isVideo()) {
                    String dest;
                    seriesList.get(i).setSeriesDesc(seriesList.get(i).getSeriesDesc() + ", " + imageList.size() + " images");
                    if (!ApplicationContext.databaseRef.isLink(studyUid)) {
                        dest = path.substring(0, path.lastIndexOf(File.separator)) + File.separator + "Thumbnails";
                    } else {
                        dest = ApplicationContext.getAppDirectory() + File.separator + "Thumbnails";
                    }
                    thumbnails = new Thumbnail[imageList.size()];
                    for (int j = 0; j < imageList.size(); j++) {
                        try {
                            file = new File(dest + File.separator + imageList.get(j).getSop_iuid());
                            thumbnails[j] = new Thumbnail(imageList.get(j).getSop_iuid());
                            thumbnails[j].setImage(ImageIO.read(file));
                        } catch (IOException ex) {
                            thumbnails[j] = new Thumbnail(imageList.get(j).getSop_iuid());
                            thumbnails[j].setDefaultImage();
                        } catch (NullPointerException npe) {                            
                        }
                    }
                } else {
                    thumbnails = new Thumbnail[1];
                    thumbnails[0] = new Thumbnail(imageList.get(0).getSop_iuid());
                    thumbnails[0].setVideoImage();
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder;
                    try {
                        dBuilder = dbFactory.newDocumentBuilder();
                        Document doc = null;
                        if (!ApplicationContext.databaseRef.isLink(studyUid)) {
                            doc = dBuilder.parse(imageList.get(0).getFilepath().split("_")[0] + "_V" + File.separator + "video.xml");
                        } else {
                            doc = dBuilder.parse(ApplicationContext.getAppDirectory() + File.separator + "Videos" + File.separator + imageList.get(0).getSop_iuid() + "_V" + File.separator + "video.xml");
                        }
                        XPathFactory xPathfactory = XPathFactory.newInstance();
                        XPath xpath = xPathfactory.newXPath();
                        XPathExpression expr = xpath.compile("//attr[@tag=\"00181063\"]");
                        NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
                        Node frameTime = nl.item(0);
                        expr = xpath.compile("//attr[@tag=\"00280008\"]");
                        nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
                        Node numberOfFrames = nl.item(0);
                        try {
                            seriesList.get(i).setSeriesDesc("Video : " + (int) Math.floor((Double.parseDouble(numberOfFrames.getTextContent()) * (1000 / Double.parseDouble(frameTime.getTextContent()))) / 1000) + " Sec");
                        } catch (NumberFormatException ex) {
                            System.out.println("Illegal number format : " + ex.getMessage());
                        }
                        dBuilder = null;
                        dbFactory = null;
                    } catch (IOException ex) {
                        Logger.getLogger(ShowImageView.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ParserConfigurationException ex) {
                        Logger.getLogger(ShowImageView.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (SAXException ex) {
                        Logger.getLogger(ShowImageView.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (XPathExpressionException ex) {
                        Logger.getLogger(ShowImageView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                ViewerPreviewPanel viewerPreviewPanel = new ViewerPreviewPanel(studyUid, seriesList.get(i).getSeriesInstanceUID(), seriesList.get(i).getSeriesDesc(), imageList.size(), thumbnails, seriesList.get(i).isMultiframe(), seriesList.get(i).getInstanceUID(), seriesList.get(i).isVideo());
                viewerPreviewPanel.setVisible(true);
                viewerPreviewPanel.setName(String.valueOf(i));
                int height = viewerPreviewPanel.getTotalHeight();
                size += height + 5;
                viewerPreviewPanel.setBounds(0, pos, 230, height);
                imagePreviewPanel.addViewerPanel(pos, height, viewerPreviewPanel, size);
                pos += (height + 5);
                allPreviews[i] = viewerPreviewPanel;
            }
            if (studyDir != null) {
                setSelectedSeriesContext(allPreviews, selectedPreviews, studyDir, false);
            } else {
                setSelectedSeriesContext(allPreviews, selectedPreviews, studyDir, true);
            }
        }
    }

    private void showVideo(String videoFilePath, String[] patientInfo) {
        try {
            if (ApplicationContext.tabbedPane != null) {
                for (int i = 0; i < ApplicationContext.tabbedPane.getTabCount(); i++) { //If already opened study just set the tab selected which holds the study
                    if (((JPanel) ((JSplitPane) ApplicationContext.tabbedPane.getComponentAt(i)).getRightComponent()).getName().equals(studyUid)) {
                        alreadyOpenedStudy = true;
                        ApplicationContext.tabbedPane.setSelectedIndex(i);
                        break;
                    }
                }
            }
            if (!alreadyOpenedStudy) {
                VideoPanel videoPanel = new VideoPanel();
                videoPanel.setName(studyUid);
                String iuid = videoFilePath.substring(videoFilePath.split("_")[0].lastIndexOf(File.separator) + 1, videoFilePath.indexOf("_"));
                EmbeddedMediaPlayerComponent mediaPlayerComponent = null;
                try {
                    mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
                } catch (NoClassDefFoundError ncdf) {
                    System.out.println("no cls found");
                }
                videoPanel.setMediaPlayer(mediaPlayerComponent);
                videoPanel.setUniqueIdentifier(iuid);
                videoPanel.setBorder(BorderFactory.createLineBorder(new Color(255, 138, 0)));
                ImagePreviewPanel imagePreviewPanel = new ImagePreviewPanel();
                imagePreviewPanel.setPatientInfo(patientInfo);
                JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, imagePreviewPanel, videoPanel);
                splitPane.setOneTouchExpandable(true);
                splitPane.setDividerLocation(270);
                splitPane.setDividerSize(15);
                splitPane.setName(patientInfo[0]);
                imagePreviewPanel.setMinimumSize(new Dimension(270, splitPane.getHeight()));
                ApplicationContext.imgView.jTabbedPane1.add(splitPane);

                //The following lines are used for tab close button and event
                ButtonTabComp tabComp = new ButtonTabComp(ApplicationContext.imgView.jTabbedPane1);
                if (ApplicationContext.tabbedPane == null) {
                    ApplicationContext.tabbedPane = (JTabbedPane) tabComp.getTabbedComponent();
                }
                ApplicationContext.imgView.jTabbedPane1.setTabComponentAt(ApplicationContext.imgView.jTabbedPane1.getTabCount() - 1, tabComp);
                ApplicationContext.imgView.jTabbedPane1.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
                ApplicationContext.tabbedPane.setSelectedIndex(ApplicationContext.tabbedPane.getTabCount() - 1);
                CreateThumbnails createThumbnails = new CreateThumbnails(imagePreviewPanel, null);
                createThumbnails.start();
                mediaPlayerComponent.getMediaPlayer().playMedia(videoFilePath);
                videoPanel.startTimer();
                ApplicationContext.imgView.getImageToolbar().disableImageTools();
            }
        } catch (Exception ex) {
            System.out.println("exception : "+ex);
            if (Desktop.isDesktopSupported()) {
                try {
                    if (ApplicationContext.tabbedPane == null || ApplicationContext.tabbedPane.getTabCount() == 0) {
                        ApplicationContext.imgView.dispose();
                    }
                    Desktop.getDesktop().open(new File(videoFilePath));
                } catch (IOException ex1) {
                    Logger.getLogger(ShowImageView.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        }
    }
}