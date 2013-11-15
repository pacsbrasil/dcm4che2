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
import in.raster.mayam.delegates.CineTimer;
import in.raster.mayam.delegates.ImageBuffer;
import in.raster.mayam.delegates.ImageGenerator;
import in.raster.mayam.delegates.LocalizerDelegate;
import in.raster.mayam.dicomtags.DicomTags;
import in.raster.mayam.dicomtags.DicomTagsReader;
import in.raster.mayam.form.dialogs.ExportDialog;
import in.raster.mayam.form.display.Display;
import in.raster.mayam.models.PresetModel;
import in.raster.mayam.models.ScoutLineInfoModel;
import in.raster.mayam.models.SeriesAnnotations;
import in.raster.mayam.param.TextOverlayParam;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.ColorModel;
import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import javax.swing.*;
import org.dcm4che.image.ColorModelParam;

/**
 *
 * @author BabuHussain
 * @version 0.5
 *
 */
public class ImageToolbar extends javax.swing.JPanel {

    CineTimer cineTimer;
    Timer timer;
    ImageView imgView;
    LayoutPopupDesign layoutPopupDesign = null;
    public boolean isImageLayout = false;
    KeyEventDispatcher keyEventDispatcher = null;

    /**
     * Creates new form ImageToolbar1
     */
    public ImageToolbar() {
        initComponents();
        cineTimer = new CineTimer();
    }

    public ImageToolbar(ImageView imgView) {
        initComponents();
        cineTimer = new CineTimer();
        this.imgView = imgView;
        layoutButton.setArrowPopupMenu(jPopupMenu1);
        layoutPopupDesign = new LayoutPopupDesign(jPopupMenu1);
        textOverlayContext();
    }

    public void addKeyEventDispatcher() {
        keyEventDispatcher = new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() == KeyEvent.KEY_PRESSED && ApplicationContext.imgView != null && ApplicationContext.imgView.isFocused() && windowing.isEnabled()) {
                    keyEventProcessor(e);
                }
                boolean discardEvent = false;
                return discardEvent;
            }
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyEventDispatcher);
    }

    private void keyEventProcessor(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_LEFT) {
            ApplicationContext.layeredCanvas.imgpanel.doPrevious();
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_RIGHT) {
            ApplicationContext.layeredCanvas.imgpanel.doNext();
        } else if (e.getKeyCode() == KeyEvent.VK_O) {
            doScout();
        } else if (e.getKeyCode() == KeyEvent.VK_I) {
            doTextOverlay();
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            doReset();
        } else if (e.getKeyCode() == KeyEvent.VK_C) {
            if (!loopCheckbox.isSelected()) {
                loopCheckbox.setSelected(true);
            } else {
                loopCheckbox.setSelected(false);
            }
            doCineLoop();
        } else if (e.getKeyCode() == KeyEvent.VK_S) {
            doStack();
        } else if (e.getKeyCode() == KeyEvent.VK_D) {
            doRuler(false);
        } else if (e.getKeyCode() == KeyEvent.VK_A) {
            doRuler(true);
        } else if (e.getKeyCode() == KeyEvent.VK_T) {
            doPan();
        }
    }

    public void changeImageLayout(int row, int col) {
        try {
            ApplicationContext.layeredCanvas.imgpanel.storeAnnotation();
            GridLayout g = new GridLayout(1, 1);
            ArrayList<String> tempRef = ApplicationContext.databaseRef.getSeriesInstancesLocation(ApplicationContext.layeredCanvas.imgpanel.getStudyUID());
            JPanel panel = ((JPanel) ((JSplitPane) ApplicationContext.tabbedPane.getSelectedComponent()).getRightComponent());
            panel = new JPanel(new GridLayout(row, col));
            ((JSplitPane) ApplicationContext.tabbedPane.getSelectedComponent()).setRightComponent(panel);
            for (int i = 0; i < (row * col); i++) {
                if (i < tempRef.size()) {
                    JPanel newPanel = new JPanel(g);
                    LayeredCanvas canvas = new LayeredCanvas(new File(tempRef.get(i)), 0, false);
                    canvas.imgpanel.setScaleFactor((ApplicationContext.tabbedPane.getWidth()) / col, ApplicationContext.tabbedPane.getHeight() / row, (row * col));
                    canvas.annotationPanel.doZoomIn();
                    canvas.imgpanel.layoutRows = row;
                    canvas.imgpanel.layoutColumns = col;
                    newPanel.add(canvas);
                    panel.add(newPanel);
                    canvas.canvas.setSelection(true);
                    if (synchronizeButton.isSelected()) {
                        canvas.imgpanel.doSynchronize();
                    }
                    canvas.imgpanel.setCurrentSeriesAnnotation();
                } else {
                    JPanel newPanel = new JPanel(g);
                    LayeredCanvas j = new LayeredCanvas();
                    j.setStudyUID(ApplicationContext.layeredCanvas.imgpanel.getStudyUID());
                    newPanel.add(j);
                    panel.add(newPanel);
                }
            }
            jPopupMenu1.setVisible(false);
            isImageLayout = false;
            ApplicationContext.setSeriesContext();//To set the selected series   
            ApplicationContext.setImageIdentification();
            layoutPopupDesign.resetPopupMenu();
        } catch (IllegalArgumentException ex) {
            //ignore : Illegal argument occurs in Jdk 1.7
        }
    }

    public void changeTileLayout(final int row, final int col) {
        try {
            jPopupMenu1.setVisible(false);
            String studyUid = ApplicationContext.layeredCanvas.imgpanel.getStudyUID();
            String seriesUid = ApplicationContext.layeredCanvas.imgpanel.getSeriesUID();
            ApplicationContext.selectedPanel.removeAll();
            ApplicationContext.selectedPanel.revalidate();
            ApplicationContext.selectedPanel.repaint();
            ApplicationContext.selectedPanel.setLayout(new GridLayout(row, col));

            LayeredCanvas canvas = new LayeredCanvas(new File(ApplicationContext.databaseRef.getFirstInstanceLocation(studyUid, seriesUid)), 0, true);
            ApplicationContext.selectedPanel.add(canvas);
            ArrayList<String> instanceUidList = canvas.imgpanel.getInstanceUidList();
            ApplicationContext.imgBuffer = new ImageBuffer(canvas.imgpanel);
            ApplicationContext.imgBuffer.setDefaultBufferSize((row * col) + (row * col) + (row * col));
            if (instanceUidList.size() > ApplicationContext.imgBuffer.getDefaultBufferSize()) {
                ApplicationContext.imageUpdator = new ImageGenerator(ApplicationContext.imgBuffer, canvas.imgpanel, true);
                ApplicationContext.imageUpdator.setParameters(instanceUidList.size() - (row * col), (row * col) + (row * col), true);
                ApplicationContext.imageUpdator.start();
            } else {
                ApplicationContext.imageUpdator = new ImageGenerator(ApplicationContext.imgBuffer, ApplicationContext.imgBuffer.getImagePanelRef(), true);
                ApplicationContext.imageUpdator.setParameters(0, instanceUidList.size(), true);
                ApplicationContext.imageUpdator.start();
            }

            TextOverlayParam textOverlayParam = canvas.imgpanel.getTextOverlayParam();
            double pixelSpacingX = canvas.imgpanel.getPixelSpacingX();
            double pixelSpacingY = canvas.imgpanel.getPixelSpacingY();
            canvas.imgpanel.setCurrentSeriesAnnotation();
            SeriesAnnotations currentSeriesAnnotation = canvas.imgpanel.getCurrentSeriesAnnotation();
            String fileLoc = canvas.imgpanel.getFileLocation();
            canvas.imgpanel.getFilePathsifLink();
            ColorModelParam cmParam = canvas.imgpanel.getCmParam();
            ColorModel cm = canvas.imgpanel.getCm();
            int windowLevel = canvas.imgpanel.getWindowLevel();
            int windowWidth = canvas.imgpanel.getWindowWidth();
            String modality = canvas.imgpanel.getModality();
            String studyDesc = canvas.imgpanel.getStudyDesc();
            ScoutLineInfoModel currentScoutDetails = canvas.imgpanel.getCurrentScoutDetails();
            setParameters(canvas, row, col);

            for (int i = 1; i < (row * col); i++) {
                if (i < instanceUidList.size()) {
                    canvas = new LayeredCanvas(true, studyUid, seriesUid);
                    ApplicationContext.selectedPanel.add(canvas);
                    canvas.imgpanel.setImageInfo(pixelSpacingX, pixelSpacingY, studyUid, seriesUid, fileLoc, currentSeriesAnnotation, instanceUidList, cmParam, cm, windowLevel, windowWidth, modality, studyDesc, currentScoutDetails);
                    canvas.textOverlay.setTextOverlayParam(new TextOverlayParam(textOverlayParam.getPatientName(), textOverlayParam.getPatientID(), textOverlayParam.getSex(), textOverlayParam.getStudyDate(), textOverlayParam.getStudyDescription(), textOverlayParam.getSeriesDescription(), textOverlayParam.getBodyPartExamined(), textOverlayParam.getInstitutionName(), textOverlayParam.getWindowLevel(), textOverlayParam.getWindowWidth(), i, textOverlayParam.getTotalInstance(), textOverlayParam.isMultiframe()));
                    setParameters(canvas, row, col);
                } else {
                    LayeredCanvas j = new LayeredCanvas();
                    j.setStudyUID(ApplicationContext.layeredCanvas.imgpanel.getStudyUID());
                    ApplicationContext.selectedPanel.add(j, i);
                }
            }
            final int total = instanceUidList.size();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    for (int i = 1; i < (row * col); i++) {
                        if (i < total) {
                            ((LayeredCanvas) ApplicationContext.selectedPanel.getComponent(i)).imgpanel.setImage(i);
                        }
                    }
                    ApplicationContext.setSeriesContext();//To set the selected series   
                    ApplicationContext.setImageIdentification();
                }
            });
            isImageLayout = true;
            LocalizerDelegate.hideAllScoutLines();
            scoutButton.setSelected(false);
            textOverlayParam = null;
            currentSeriesAnnotation = null;
            layoutPopupDesign.resetPopupMenu();
        } catch (IllegalArgumentException iae) {
            //ignore : Illegal argument occurs in Jdk 1.7
        }
    }

    private void setParameters(LayeredCanvas canvas, int row, int col) {
        if (row != 1 || col != 1) {
            canvas.imgpanel.setScaleFactor(ApplicationContext.selectedPanel.getWidth() / col, ApplicationContext.selectedPanel.getHeight() / row, (row * col) + (row * col));
        } else {
            if (((JPanel) ((JSplitPane) ApplicationContext.tabbedPane.getSelectedComponent()).getRightComponent()).getComponentCount() > 1) {
                canvas.imgpanel.setScaleFactor(ApplicationContext.selectedPanel.getWidth(), ApplicationContext.selectedPanel.getHeight(), ((JPanel) ((JSplitPane) ApplicationContext.tabbedPane.getSelectedComponent()).getRightComponent()).getComponentCount());
            } else {
                canvas.imgpanel.setScaleFactor(ApplicationContext.tabbedPane.getWidth(), ApplicationContext.selectedPanel.getHeight(), col);
            }
        }
        canvas.annotationPanel.resizeHandler();
        canvas.imgpanel.layoutRows = row;
        canvas.imgpanel.layoutColumns = col;
        canvas.getCanvas().setSelection(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolsButtonGroup = new javax.swing.ButtonGroup();
        jPopupMenu1 = new javax.swing.JPopupMenu();
        jPopupMenu2 = new javax.swing.JPopupMenu();
        jPopupMenu3 = new javax.swing.JPopupMenu();
        jPopupMenu4 = new javax.swing.JPopupMenu();
        jToolBar1 = new javax.swing.JToolBar();
        layoutButton = new in.raster.mayam.form.JComboButton();
        windowing = new javax.swing.JButton();
        presetButton = new javax.swing.JButton();
        probeButton = new javax.swing.JButton();
        verticalFlip = new javax.swing.JButton();
        horizontalFlip = new javax.swing.JButton();
        leftRotate = new javax.swing.JButton();
        rightRotate = new javax.swing.JButton();
        zoomin = new javax.swing.JButton();
        zoomoutButton = new javax.swing.JButton();
        panButton = new javax.swing.JButton();
        invert = new javax.swing.JButton();
        rulerButton = new javax.swing.JButton();
        rectangleButton = new javax.swing.JButton();
        ellipseButton = new javax.swing.JButton();
        arrowButton = new javax.swing.JButton();
        clearAllMeasurement = new javax.swing.JButton();
        deleteMeasurement = new javax.swing.JButton();
        moveMeasurement = new javax.swing.JButton();
        annotationVisibility = new javax.swing.JButton();
        textOverlay = new javax.swing.JButton();
        reset = new javax.swing.JButton();
        exportButton = new javax.swing.JButton();
        metaDataButton = new javax.swing.JButton();
        stackButton = new javax.swing.JButton();
        scoutButton = new javax.swing.JButton();
        cube3DButton = new javax.swing.JButton();
        synchronizeButton = new javax.swing.JButton();
        loopCheckbox = new javax.swing.JCheckBox();
        loopSlider = new javax.swing.JSlider();

        setBackground(new java.awt.Color(102, 102, 102));

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        layoutButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/layout.png"))); // NOI18N
        layoutButton.setText("");
        layoutButton.setFocusable(false);
        layoutButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        layoutButton.setPreferredSize(new java.awt.Dimension(45, 45));
        layoutButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(layoutButton);

        windowing.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/windowing.png"))); // NOI18N
        toolsButtonGroup.add(windowing);
        windowing.setFocusPainted(false);
        windowing.setFocusable(false);
        windowing.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        windowing.setPreferredSize(new java.awt.Dimension(45, 45));
        windowing.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        windowing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                windowingActionPerformed(evt);
            }
        });
        jToolBar1.add(windowing);

        presetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/drop_down.png"))); // NOI18N
        toolsButtonGroup.add(presetButton);
        presetButton.setComponentPopupMenu(jPopupMenu2);
        presetButton.setFocusable(false);
        presetButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        presetButton.setMaximumSize(new java.awt.Dimension(12, 24));
        presetButton.setMinimumSize(new java.awt.Dimension(12, 24));
        presetButton.setPreferredSize(new java.awt.Dimension(45, 45));
        presetButton.setRequestFocusEnabled(false);
        presetButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        presetButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                presetButtonMouseClicked(evt);
            }
        });
        jToolBar1.add(presetButton);

        probeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/probe.png"))); // NOI18N
        toolsButtonGroup.add(probeButton);
        probeButton.setFocusable(false);
        probeButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        probeButton.setPreferredSize(new java.awt.Dimension(45, 45));
        probeButton.setRequestFocusEnabled(false);
        probeButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        probeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                probeButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(probeButton);

        verticalFlip.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/flip_vertical.png"))); // NOI18N
        toolsButtonGroup.add(verticalFlip);
        verticalFlip.setFocusable(false);
        verticalFlip.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        verticalFlip.setPreferredSize(new java.awt.Dimension(45, 45));
        verticalFlip.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        verticalFlip.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                verticalFlipActionPerformed(evt);
            }
        });
        jToolBar1.add(verticalFlip);

        horizontalFlip.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/flip_horizontal.png"))); // NOI18N
        toolsButtonGroup.add(horizontalFlip);
        horizontalFlip.setFocusable(false);
        horizontalFlip.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        horizontalFlip.setPreferredSize(new java.awt.Dimension(45, 45));
        horizontalFlip.setRequestFocusEnabled(false);
        horizontalFlip.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        horizontalFlip.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                horizontalFlipActionPerformed(evt);
            }
        });
        jToolBar1.add(horizontalFlip);

        leftRotate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/rotate_left.png"))); // NOI18N
        toolsButtonGroup.add(leftRotate);
        leftRotate.setFocusable(false);
        leftRotate.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        leftRotate.setPreferredSize(new java.awt.Dimension(45, 45));
        leftRotate.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        leftRotate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                leftRotateActionPerformed(evt);
            }
        });
        jToolBar1.add(leftRotate);

        rightRotate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/rotate_right.png"))); // NOI18N
        toolsButtonGroup.add(rightRotate);
        rightRotate.setFocusable(false);
        rightRotate.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        rightRotate.setPreferredSize(new java.awt.Dimension(45, 45));
        rightRotate.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        rightRotate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rightRotateActionPerformed(evt);
            }
        });
        jToolBar1.add(rightRotate);

        zoomin.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/zoomin.png"))); // NOI18N
        toolsButtonGroup.add(zoomin);
        zoomin.setFocusable(false);
        zoomin.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        zoomin.setPreferredSize(new java.awt.Dimension(45, 45));
        zoomin.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        zoomin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoominActionPerformed(evt);
            }
        });
        jToolBar1.add(zoomin);

        zoomoutButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/zoomout.png"))); // NOI18N
        toolsButtonGroup.add(zoomoutButton);
        zoomoutButton.setFocusable(false);
        zoomoutButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        zoomoutButton.setPreferredSize(new java.awt.Dimension(45, 45));
        zoomoutButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        zoomoutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomoutButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(zoomoutButton);

        panButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/pan.png"))); // NOI18N
        toolsButtonGroup.add(panButton);
        panButton.setFocusable(false);
        panButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        panButton.setPreferredSize(new java.awt.Dimension(45, 45));
        panButton.setRequestFocusEnabled(false);
        panButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        panButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                panButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(panButton);

        invert.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/invert.png"))); // NOI18N
        invert.setFocusable(false);
        invert.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        invert.setPreferredSize(new java.awt.Dimension(45, 45));
        invert.setRequestFocusEnabled(false);
        invert.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        invert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                invertActionPerformed(evt);
            }
        });
        jToolBar1.add(invert);

        rulerButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/ruler.png"))); // NOI18N
        rulerButton.setActionCommand("ruler");
        toolsButtonGroup.add(rulerButton);
        rulerButton.setFocusable(false);
        rulerButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        rulerButton.setPreferredSize(new java.awt.Dimension(45, 45));
        rulerButton.setRequestFocusEnabled(false);
        rulerButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        rulerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rulerButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(rulerButton);

        rectangleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/rectangle.png"))); // NOI18N
        rectangleButton.setActionCommand("rectangle");
        toolsButtonGroup.add(rectangleButton);
        rectangleButton.setFocusable(false);
        rectangleButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        rectangleButton.setPreferredSize(new java.awt.Dimension(45, 45));
        rectangleButton.setRequestFocusEnabled(false);
        rectangleButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        rectangleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rectangleButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(rectangleButton);

        ellipseButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/ellipse.png"))); // NOI18N
        ellipseButton.setActionCommand("ellipse");
        toolsButtonGroup.add(ellipseButton);
        ellipseButton.setFocusable(false);
        ellipseButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ellipseButton.setPreferredSize(new java.awt.Dimension(45, 45));
        ellipseButton.setRequestFocusEnabled(false);
        ellipseButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        ellipseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ellipseButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(ellipseButton);

        arrowButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/arrow.png"))); // NOI18N
        arrowButton.setActionCommand("arrow");
        toolsButtonGroup.add(arrowButton);
        arrowButton.setFocusable(false);
        arrowButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        arrowButton.setPreferredSize(new java.awt.Dimension(45, 45));
        arrowButton.setRequestFocusEnabled(false);
        arrowButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        arrowButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                arrowButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(arrowButton);

        clearAllMeasurement.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/clear_all_annotation.png"))); // NOI18N
        clearAllMeasurement.setActionCommand("clearAll");
        toolsButtonGroup.add(clearAllMeasurement);
        clearAllMeasurement.setPreferredSize(new java.awt.Dimension(45, 45));
        clearAllMeasurement.setRequestFocusEnabled(false);
        clearAllMeasurement.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearAllMeasurementActionPerformed(evt);
            }
        });
        jToolBar1.add(clearAllMeasurement);

        deleteMeasurement.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/delete_annotation.png"))); // NOI18N
        deleteMeasurement.setActionCommand("deleteMeasurement");
        toolsButtonGroup.add(deleteMeasurement);
        deleteMeasurement.setFocusable(false);
        deleteMeasurement.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteMeasurement.setPreferredSize(new java.awt.Dimension(45, 45));
        deleteMeasurement.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        deleteMeasurement.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteMeasurementActionPerformed(evt);
            }
        });
        jToolBar1.add(deleteMeasurement);

        moveMeasurement.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/annotation_selection.png"))); // NOI18N
        moveMeasurement.setActionCommand("moveMeasurement");
        toolsButtonGroup.add(moveMeasurement);
        moveMeasurement.setDoubleBuffered(true);
        moveMeasurement.setFocusable(false);
        moveMeasurement.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        moveMeasurement.setPreferredSize(new java.awt.Dimension(45, 45));
        moveMeasurement.setRequestFocusEnabled(false);
        moveMeasurement.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        moveMeasurement.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveMeasurementActionPerformed(evt);
            }
        });
        jToolBar1.add(moveMeasurement);

        annotationVisibility.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/annotation_overlay.png"))); // NOI18N
        toolsButtonGroup.add(annotationVisibility);
        annotationVisibility.setFocusable(false);
        annotationVisibility.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        annotationVisibility.setPreferredSize(new java.awt.Dimension(45, 45));
        annotationVisibility.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        annotationVisibility.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                annotationVisibilityActionPerformed(evt);
            }
        });
        jToolBar1.add(annotationVisibility);

        textOverlay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/textoverlay.png"))); // NOI18N
        toolsButtonGroup.add(textOverlay);
        textOverlay.setFocusable(false);
        textOverlay.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        textOverlay.setPreferredSize(new java.awt.Dimension(45, 45));
        textOverlay.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        textOverlay.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                textOverlayMousePressed(evt);
            }
        });
        jToolBar1.add(textOverlay);

        reset.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/reset.png"))); // NOI18N
        toolsButtonGroup.add(reset);
        reset.setFocusable(false);
        reset.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        reset.setPreferredSize(new java.awt.Dimension(45, 45));
        reset.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        reset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetActionPerformed(evt);
            }
        });
        jToolBar1.add(reset);

        exportButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/export_series.png"))); // NOI18N
        toolsButtonGroup.add(exportButton);
        exportButton.setFocusable(false);
        exportButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        exportButton.setPreferredSize(new java.awt.Dimension(45, 45));
        exportButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(exportButton);

        metaDataButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/metadata_viewerpage.png"))); // NOI18N
        toolsButtonGroup.add(metaDataButton);
        metaDataButton.setFocusable(false);
        metaDataButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        metaDataButton.setPreferredSize(new java.awt.Dimension(45, 45));
        metaDataButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        metaDataButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                metaDataButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(metaDataButton);

        stackButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/stack.png"))); // NOI18N
        toolsButtonGroup.add(stackButton);
        stackButton.setFocusable(false);
        stackButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        stackButton.setPreferredSize(new java.awt.Dimension(45, 45));
        stackButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        stackButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stackButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(stackButton);

        scoutButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/scout.png"))); // NOI18N
        scoutButton.setFocusable(false);
        scoutButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        scoutButton.setPreferredSize(new java.awt.Dimension(45, 45));
        scoutButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        scoutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scoutButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(scoutButton);

        cube3DButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/Cube3D.png"))); // NOI18N
        toolsButtonGroup.add(cube3DButton);
        cube3DButton.setComponentPopupMenu(jPopupMenu4);
        cube3DButton.setEnabled(false);
        cube3DButton.setFocusable(false);
        cube3DButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cube3DButton.setPreferredSize(new java.awt.Dimension(45, 45));
        cube3DButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(cube3DButton);

        synchronizeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/Link.png"))); // NOI18N
        synchronizeButton.setFocusable(false);
        synchronizeButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        synchronizeButton.setPreferredSize(new java.awt.Dimension(45, 45));
        synchronizeButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        synchronizeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                synchronizeButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(synchronizeButton);

        loopCheckbox.setToolTipText("Cine Loop");
        loopCheckbox.setFocusable(false);
        loopCheckbox.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        loopCheckbox.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        loopCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loopCheckboxActionPerformed(evt);
            }
        });
        jToolBar1.add(loopCheckbox);

        loopSlider.setMaximum(9);
        loopSlider.setPaintTicks(true);
        loopSlider.setValue(6);
        loopSlider.setDoubleBuffered(true);
        loopSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                loopSliderStateChanged(evt);
            }
        });
        jToolBar1.add(loopSlider);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1540, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void windowingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_windowingActionPerformed
        setWindowingTool();
    }//GEN-LAST:event_windowingActionPerformed

    private void presetButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_presetButtonMouseClicked
        if (presetButton.isEnabled()) {
            int x = evt.getX();
            int y = evt.getY();
            long z = evt.getWhen();
            int mo = evt.getModifiers();
            int cc = evt.getClickCount();
            designPresetContext();
            presetButton.dispatchEvent(new java.awt.event.MouseEvent(this.presetButton, MouseEvent.MOUSE_CLICKED, z, mo, x, y, cc, true));
        }
    }//GEN-LAST:event_presetButtonMouseClicked

    private void probeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_probeButtonActionPerformed
        if (ApplicationContext.layeredCanvas.annotationPanel != null && ApplicationContext.layeredCanvas.imgpanel != null) {
            ApplicationContext.layeredCanvas.annotationPanel.setAddLine(false);
            ApplicationContext.layeredCanvas.annotationPanel.setAddArrow(false);
            ApplicationContext.layeredCanvas.annotationPanel.setAddEllipse(false);
            ApplicationContext.layeredCanvas.annotationPanel.setAddRect(false);
            ApplicationContext.layeredCanvas.annotationPanel.stopPanning();
            if (ApplicationContext.layeredCanvas.imgpanel.probe()) {
                probeButton.setSelected(true);
            } else {
                probeButton.setSelected(false);
            }
        } else {
            JOptionPane.showMessageDialog(ImageToolbar.this, "Tile selected is not valid for this process");
        }
    }//GEN-LAST:event_probeButtonActionPerformed

    private void verticalFlipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_verticalFlipActionPerformed
        JPanel currentSeriesPanel = (JPanel) ApplicationContext.layeredCanvas.getParent();
        LayeredCanvas tempCanvas;
        for (int i = 0; i < currentSeriesPanel.getComponentCount(); i++) {
            tempCanvas = (LayeredCanvas) currentSeriesPanel.getComponent(i);
            if (tempCanvas != null && tempCanvas.annotationPanel != null && tempCanvas.imgpanel != null) {
                tempCanvas.imgpanel.flipVertical();
                tempCanvas.annotationPanel.doFlipVertical();
                tempCanvas.imgpanel.repaint();
                tempCanvas.textOverlay.repaint();
            }
        }
        tempCanvas = null;
    }//GEN-LAST:event_verticalFlipActionPerformed

    private void horizontalFlipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_horizontalFlipActionPerformed
        JPanel currentSeriesPanel = (JPanel) ApplicationContext.layeredCanvas.getParent();
        LayeredCanvas tempCanvas;
        for (int i = 0; i < currentSeriesPanel.getComponentCount(); i++) {
            tempCanvas = (LayeredCanvas) currentSeriesPanel.getComponent(i);
            if (tempCanvas != null && tempCanvas.annotationPanel != null && tempCanvas.imgpanel != null) {
                tempCanvas.imgpanel.flipHorizontal();
                tempCanvas.annotationPanel.doFlipHorizontal();
                tempCanvas.imgpanel.repaint();
                tempCanvas.textOverlay.repaint();
            }
        }
        tempCanvas = null;
    }//GEN-LAST:event_horizontalFlipActionPerformed

    private void leftRotateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_leftRotateActionPerformed
        JPanel currentSeriesPanel = (JPanel) ApplicationContext.layeredCanvas.getParent();
        LayeredCanvas tempCanvas;
        for (int i = 0; i < currentSeriesPanel.getComponentCount(); i++) {
            tempCanvas = (LayeredCanvas) currentSeriesPanel.getComponent(i);
            if (tempCanvas != null && tempCanvas.annotationPanel != null && tempCanvas.imgpanel != null) {
                tempCanvas.imgpanel.rotateLeft();
                tempCanvas.annotationPanel.doRotateLeft();
            }
        }
        tempCanvas = null;
    }//GEN-LAST:event_leftRotateActionPerformed

    private void rightRotateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rightRotateActionPerformed
        JPanel currentSeriesPanel = (JPanel) ApplicationContext.layeredCanvas.getParent();
        LayeredCanvas tempCanvas;
        for (int i = 0; i < currentSeriesPanel.getComponentCount(); i++) {
            tempCanvas = (LayeredCanvas) currentSeriesPanel.getComponent(i);
            if (tempCanvas != null && tempCanvas.annotationPanel != null && tempCanvas.imgpanel != null) {
                tempCanvas.imgpanel.rotateRight();
                tempCanvas.annotationPanel.doRotateRight();
            }
        }
        tempCanvas = null;
    }//GEN-LAST:event_rightRotateActionPerformed

    private void zoominActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoominActionPerformed
        JPanel currentSeriesPanel = (JPanel) ApplicationContext.layeredCanvas.getParent();
        LayeredCanvas tempCanvas;
        for (int i = 0; i < currentSeriesPanel.getComponentCount(); i++) {
            tempCanvas = (LayeredCanvas) currentSeriesPanel.getComponent(i);
            if (tempCanvas != null && tempCanvas.annotationPanel != null && tempCanvas.imgpanel != null) {
                resetTools();
                tempCanvas.imgpanel.doZoomIn();
                tempCanvas.annotationPanel.doZoomIn();
                tempCanvas.imgpanel.repaint();
            }
        }
        tempCanvas = null;
    }//GEN-LAST:event_zoominActionPerformed

    private void zoomoutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomoutButtonActionPerformed
        JPanel currentSeriesPanel = (JPanel) ApplicationContext.layeredCanvas.getParent();
        LayeredCanvas tempCanvas;
        for (int i = 0; i < currentSeriesPanel.getComponentCount(); i++) {
            tempCanvas = (LayeredCanvas) currentSeriesPanel.getComponent(i);
            if (tempCanvas != null && tempCanvas.annotationPanel != null && tempCanvas.imgpanel != null) {
                resetTools();
                tempCanvas.imgpanel.doZoomOut();
                tempCanvas.annotationPanel.doZoomOut();
                tempCanvas.imgpanel.repaint();
            }
        }
        tempCanvas = null;
    }//GEN-LAST:event_zoomoutButtonActionPerformed

    private void panButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_panButtonActionPerformed
        doPan();
    }//GEN-LAST:event_panButtonActionPerformed

    private void invertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_invertActionPerformed
        JPanel currentSeriesPanel = (JPanel) ApplicationContext.layeredCanvas.getParent();
        LayeredCanvas tempCanvas;
        for (int i = 0; i < currentSeriesPanel.getComponentCount(); i++) {
            tempCanvas = (LayeredCanvas) currentSeriesPanel.getComponent(i);
            if (tempCanvas != null && tempCanvas.annotationPanel != null && tempCanvas.imgpanel != null) {
                tempCanvas.imgpanel.negative();
            }
        }
        tempCanvas = null;
        if (invert.isSelected()) {
            invert.setSelected(false);
        } else {
            invert.setSelected(true);
        }
    }//GEN-LAST:event_invertActionPerformed

    private void rulerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rulerButtonActionPerformed
        doRuler(false);
    }//GEN-LAST:event_rulerButtonActionPerformed

    private void rectangleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rectangleButtonActionPerformed
        doRectangle();
    }//GEN-LAST:event_rectangleButtonActionPerformed

    private void ellipseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ellipseButtonActionPerformed
        doEllipse();
    }//GEN-LAST:event_ellipseButtonActionPerformed

    private void arrowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_arrowButtonActionPerformed
        doRuler(true);
    }//GEN-LAST:event_arrowButtonActionPerformed

    private void clearAllMeasurementActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearAllMeasurementActionPerformed
        if (ApplicationContext.layeredCanvas.annotationPanel != null) {
            ApplicationContext.layeredCanvas.annotationPanel.clearAllMeasurement();
        } else {
            JOptionPane.showMessageDialog(ImageToolbar.this, "Tile selected is not valid for this process");
        }
    }//GEN-LAST:event_clearAllMeasurementActionPerformed

    private void deleteMeasurementActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteMeasurementActionPerformed
        toolsButtonGroup.clearSelection();
        if (ApplicationContext.layeredCanvas.annotationPanel != null) {
            ApplicationContext.layeredCanvas.annotationPanel.doDeleteMeasurement();
            if (ApplicationContext.layeredCanvas.annotationPanel.isDeleteMeasurement()) {
                toolsButtonGroup.setSelected(deleteMeasurement.getModel(), true);
            }
        } else {
            JOptionPane.showMessageDialog(ImageToolbar.this, "Tile selected is not valid for this process");
        }
    }//GEN-LAST:event_deleteMeasurementActionPerformed

    private void moveMeasurementActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveMeasurementActionPerformed
        toolsButtonGroup.clearSelection();
        if (ApplicationContext.layeredCanvas.annotationPanel != null && ApplicationContext.layeredCanvas.imgpanel != null) {
            ImagePanel.tool = "";
            AnnotationPanel.tool = "";
            ApplicationContext.layeredCanvas.annotationPanel.doMoveMeasurement();
            if (AnnotationPanel.isMoveMeasurement()) {
                toolsButtonGroup.setSelected(moveMeasurement.getModel(), true);
            }
        } else {
            JOptionPane.showMessageDialog(ImageToolbar.this, "Tile selected is not valid for this process");
        }
    }//GEN-LAST:event_moveMeasurementActionPerformed

    private void annotationVisibilityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_annotationVisibilityActionPerformed
        if (ApplicationContext.layeredCanvas.annotationPanel != null) {
            ApplicationContext.layeredCanvas.annotationPanel.toggleAnnotation();
            setAnnotationToolsStatus();
        } else {
            JOptionPane.showMessageDialog(ImageToolbar.this, "Tile selected is not valid for this process");
        }
    }//GEN-LAST:event_annotationVisibilityActionPerformed

    private void textOverlayMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_textOverlayMousePressed
        if (textOverlay.isEnabled()) {
            int x = evt.getX();
            int y = evt.getY();
            long z = evt.getWhen();
            int mo = evt.getModifiers();
            int cc = evt.getClickCount();
            textOverlay.dispatchEvent(new java.awt.event.MouseEvent(this.textOverlay, MouseEvent.MOUSE_CLICKED, z, mo, x, y, cc, true));
        }
    }//GEN-LAST:event_textOverlayMousePressed

    private void resetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetActionPerformed
        doReset();
    }//GEN-LAST:event_resetActionPerformed

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
        if (ApplicationContext.layeredCanvas.annotationPanel != null && ApplicationContext.layeredCanvas.imgpanel != null) {
            ExportDialog jpegConvertor = new ExportDialog(ApplicationContext.imgView, true);
            Display.alignScreen(jpegConvertor);
            jpegConvertor.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Tile selected is not valid for this process");
        }
    }//GEN-LAST:event_exportButtonActionPerformed

    private void metaDataButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_metaDataButtonActionPerformed
        if (ApplicationContext.layeredCanvas.annotationPanel != null && ApplicationContext.layeredCanvas.imgpanel != null) {
            ArrayList<DicomTags> dcmTags = DicomTagsReader.getTags(new File(ApplicationContext.layeredCanvas.imgpanel.getDicomFileUrl()));
            DicomTagsViewer dicomTagsViewer = new DicomTagsViewer(dcmTags);
            Display.alignScreen(dicomTagsViewer);
            dicomTagsViewer.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Tile selected is not valid for this process");
        }
    }//GEN-LAST:event_metaDataButtonActionPerformed

    private void stackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stackButtonActionPerformed
        doStack();
    }//GEN-LAST:event_stackButtonActionPerformed

    private void scoutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scoutButtonActionPerformed
        doScout();
    }//GEN-LAST:event_scoutButtonActionPerformed

    private void synchronizeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_synchronizeButtonActionPerformed
        if (!synchronizeButton.isSelected()) {
            synchronizeButton.setSelected(true);
        } else {
            synchronizeButton.setSelected(false);
        }
        ApplicationContext.layeredCanvas.imgpanel.doSynchronize();
    }//GEN-LAST:event_synchronizeButtonActionPerformed

    private void loopCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loopCheckboxActionPerformed
        doCineLoop();
    }//GEN-LAST:event_loopCheckboxActionPerformed

    private void loopSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_loopSliderStateChanged
        try {
            if (ApplicationContext.layeredCanvas.annotationPanel != null && ApplicationContext.layeredCanvas.imgpanel != null) {
                if (loopCheckbox.isSelected()) {
                    if (timer != null) {
                        timer.cancel();
                        timer = new Timer();
                        timer.scheduleAtFixedRate(new CineTimer(), 0, (11 - loopSlider.getValue()) * 100);//

                    } else {
                        timer = new Timer();
                        timer.scheduleAtFixedRate(new CineTimer(), 0, (11 - loopSlider.getValue()) * 100);//
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_loopSliderStateChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton annotationVisibility;
    private javax.swing.JButton arrowButton;
    private javax.swing.JButton clearAllMeasurement;
    private javax.swing.JButton cube3DButton;
    private javax.swing.JButton deleteMeasurement;
    private javax.swing.JButton ellipseButton;
    private javax.swing.JButton exportButton;
    private javax.swing.JButton horizontalFlip;
    private javax.swing.JButton invert;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JPopupMenu jPopupMenu2;
    private javax.swing.JPopupMenu jPopupMenu3;
    private javax.swing.JPopupMenu jPopupMenu4;
    private javax.swing.JToolBar jToolBar1;
    private in.raster.mayam.form.JComboButton layoutButton;
    private javax.swing.JButton leftRotate;
    private javax.swing.JCheckBox loopCheckbox;
    private javax.swing.JSlider loopSlider;
    private javax.swing.JButton metaDataButton;
    private javax.swing.JButton moveMeasurement;
    private javax.swing.JButton panButton;
    private javax.swing.JButton presetButton;
    private javax.swing.JButton probeButton;
    private javax.swing.JButton rectangleButton;
    private javax.swing.JButton reset;
    private javax.swing.JButton rightRotate;
    private javax.swing.JButton rulerButton;
    private javax.swing.JButton scoutButton;
    private javax.swing.JButton stackButton;
    private javax.swing.JButton synchronizeButton;
    private javax.swing.JButton textOverlay;
    private javax.swing.ButtonGroup toolsButtonGroup;
    private javax.swing.JButton verticalFlip;
    private javax.swing.JButton windowing;
    private javax.swing.JButton zoomin;
    private javax.swing.JButton zoomoutButton;
    // End of variables declaration//GEN-END:variables

    private void textOverlayContext() {
        JMenuItem currentFrame = new JMenuItem("Selected");
        JMenuItem allFrame = new JMenuItem("All");
        jPopupMenu3.add(currentFrame);
        jPopupMenu3.add(allFrame);
        textOverlay.setComponentPopupMenu(jPopupMenu3);
        allFrame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LayeredCanvas tempCanvas = null;
                JPanel panel = ((JPanel) ((JSplitPane) ApplicationContext.tabbedPane.getSelectedComponent()).getRightComponent());
                int childCount = panel.getComponentCount();
                for (int i = 0; i < childCount; i++) {
                    if (panel.getComponent(i) instanceof LayeredCanvas) {
                        tempCanvas = ((LayeredCanvas) panel.getComponent(i));
                        if (tempCanvas.textOverlay != null) {
                            tempCanvas.textOverlay.toggleTextOverlay();
                        }
                    }
                    JPanel seriesLevelPanel = (JPanel) panel.getComponent(i);
                    for (int k = 0; k < seriesLevelPanel.getComponentCount(); k++) {
                        if (seriesLevelPanel.getComponent(k) instanceof LayeredCanvas) {
                            tempCanvas = (LayeredCanvas) seriesLevelPanel.getComponent(k);
                            if (tempCanvas.textOverlay != null) {
                                tempCanvas.textOverlay.toggleTextOverlay();
                            }
                        }
                    }
                }
            }
        });
        currentFrame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ApplicationContext.layeredCanvas.annotationPanel != null && ApplicationContext.layeredCanvas.imgpanel != null) {
                    ApplicationContext.layeredCanvas.textOverlay.toggleTextOverlay();

                } else {
                    JOptionPane.showMessageDialog(ImageToolbar.this, "Tile selected is not valid for this process");
                }
            }
        });
    }

    public void setWindowingTool() {
        toolsButtonGroup.clearSelection();
        if (ApplicationContext.layeredCanvas.annotationPanel != null && ApplicationContext.layeredCanvas.imgpanel != null) {
            ApplicationContext.layeredCanvas.annotationPanel.setAddLine(false);
            ApplicationContext.layeredCanvas.annotationPanel.setAddArrow(false);
            ApplicationContext.layeredCanvas.annotationPanel.setAddEllipse(false);
            ApplicationContext.layeredCanvas.annotationPanel.setAddRect(false);
            ApplicationContext.layeredCanvas.annotationPanel.stopPanning();
            ApplicationContext.layeredCanvas.imgpanel.doWindowing();
            if (ApplicationContext.layeredCanvas.imgpanel.isWindowingSelected()) {
                toolsButtonGroup.setSelected(windowing.getModel(), true);
            }
        }
    }

    private void designPresetContext() {
        if (ApplicationContext.layeredCanvas.annotationPanel != null && ApplicationContext.layeredCanvas.imgpanel != null) {
            ArrayList presetList = ApplicationContext.databaseRef.getPresetsForModality(ApplicationContext.layeredCanvas.imgpanel.getModality());
            jPopupMenu2.removeAll();
            JMenuItem menu = new JMenuItem("PRESETS") {
                @Override
                protected void paintComponent(Graphics grphcs) {
                    grphcs.setFont(new Font("Arial", Font.BOLD, 12));
                    grphcs.setColor(Color.blue);
                    grphcs.drawString(this.getText(), 32, 14);
                }
            };
            menu.setEnabled(false);
            jPopupMenu2.add(menu);
            jPopupMenu2.addSeparator();
            for (int i = 0; i < presetList.size(); i++) {
                final PresetModel presetModel = (PresetModel) presetList.get(i);
                if (!presetModel.getPresetName().equalsIgnoreCase("PRESETNAME")) {
                    JMenuItem menu1 = new JMenuItem(presetModel.getPresetName());
                    jPopupMenu2.add(menu1);
                    menu1.addActionListener(new ActionListener() {
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
            this.setComponentPopupMenu(jPopupMenu1);
        } else {
            JOptionPane.showMessageDialog(this, "Tile selected is not valid for this process");
        }
    }

    private void resetTools() {
        if (ApplicationContext.layeredCanvas.annotationPanel != null && ApplicationContext.layeredCanvas.imgpanel != null) {
            ApplicationContext.layeredCanvas.annotationPanel.setMouseLocX1(-1);
        } else {
            JOptionPane.showMessageDialog(ImageToolbar.this, "Tile selected is not valid for this process");
        }
    }

    public void doPan() {
        toolsButtonGroup.clearSelection();
        if (ApplicationContext.layeredCanvas.annotationPanel != null && ApplicationContext.layeredCanvas.imgpanel != null) {
            ApplicationContext.layeredCanvas.annotationPanel.setAddLine(false);
            ApplicationContext.layeredCanvas.annotationPanel.setAddArrow(false);
            ApplicationContext.layeredCanvas.annotationPanel.setAddEllipse(false);
            ApplicationContext.layeredCanvas.annotationPanel.setAddRect(false);
            if (ApplicationContext.layeredCanvas.imgpanel.doPan()) {
                toolsButtonGroup.setSelected(panButton.getModel(), true);
            }
            ApplicationContext.layeredCanvas.annotationPanel.doPan();
        } else {
            JOptionPane.showMessageDialog(ImageToolbar.this, "Tile selected is not valid for this process");
        }
    }

    public void doRuler(boolean addArrow) {
        if (ApplicationContext.layeredCanvas.annotationPanel != null && ApplicationContext.layeredCanvas.imgpanel != null) {
            ApplicationContext.layeredCanvas.annotationPanel.stopPanning();
            ApplicationContext.layeredCanvas.imgpanel.setToolsToNull();
            ApplicationContext.layeredCanvas.annotationPanel.setMouseLocX1(-1);
            toolsButtonGroup.clearSelection();
            if (addArrow) {
                if (!ApplicationContext.layeredCanvas.annotationPanel.isAddArrow()) {
                    ApplicationContext.layeredCanvas.annotationPanel.setAddArrow(true);
                    ApplicationContext.layeredCanvas.annotationPanel.setAddLine(false);
                    ApplicationContext.layeredCanvas.annotationPanel.setAddEllipse(false);
                    ApplicationContext.layeredCanvas.annotationPanel.setAddRect(false);
                } else {
                    ApplicationContext.layeredCanvas.annotationPanel.setAddArrow(false);
                }
                toolsButtonGroup.setSelected(arrowButton.getModel(), true);
            } else {
                if (!ApplicationContext.layeredCanvas.annotationPanel.isAddLine()) {
                    ApplicationContext.layeredCanvas.annotationPanel.setAddArrow(false);
                    ApplicationContext.layeredCanvas.annotationPanel.setAddLine(true);
                    ApplicationContext.layeredCanvas.annotationPanel.setAddEllipse(false);
                    ApplicationContext.layeredCanvas.annotationPanel.setAddRect(false);
                } else {
                    ApplicationContext.layeredCanvas.annotationPanel.setAddLine(false);
                }
                toolsButtonGroup.setSelected(rulerButton.getModel(), true);
            }
        } else {
            JOptionPane.showMessageDialog(ImageToolbar.this, "Tile selected is not valid for this process");
        }
    }

    public void doRectangle() {
        toolsButtonGroup.clearSelection();
        if (ApplicationContext.layeredCanvas.annotationPanel != null && ApplicationContext.layeredCanvas.imgpanel != null) {
            ApplicationContext.layeredCanvas.annotationPanel.stopPanning();
            ApplicationContext.layeredCanvas.imgpanel.setToolsToNull();
            ApplicationContext.layeredCanvas.annotationPanel.setMouseLocX1(-1);
            if (!ApplicationContext.layeredCanvas.annotationPanel.isAddRect()) {
                ApplicationContext.layeredCanvas.annotationPanel.setAddLine(false);
                ApplicationContext.layeredCanvas.annotationPanel.setAddArrow(false);
                ApplicationContext.layeredCanvas.annotationPanel.setAddEllipse(false);
                ApplicationContext.layeredCanvas.annotationPanel.setAddRect(true);
            } else {
                ApplicationContext.layeredCanvas.annotationPanel.setAddRect(false);
            }
            toolsButtonGroup.setSelected(rectangleButton.getModel(), true);
        } else {
            JOptionPane.showMessageDialog(ImageToolbar.this, "Tile selected is not valid for this process");
        }
    }

    public void doEllipse() {
        toolsButtonGroup.clearSelection();
        if (ApplicationContext.layeredCanvas.annotationPanel != null && ApplicationContext.layeredCanvas.imgpanel != null) {
            ApplicationContext.layeredCanvas.annotationPanel.stopPanning();
            ApplicationContext.layeredCanvas.imgpanel.setToolsToNull();
            ApplicationContext.layeredCanvas.annotationPanel.setMouseLocX1(-1);
            if (!ApplicationContext.layeredCanvas.annotationPanel.isAddEllipse()) {
                ApplicationContext.layeredCanvas.annotationPanel.setAddLine(false);
                ApplicationContext.layeredCanvas.annotationPanel.setAddArrow(false);
                ApplicationContext.layeredCanvas.annotationPanel.setAddEllipse(true);
                ApplicationContext.layeredCanvas.annotationPanel.setAddRect(false);
            } else {
                ApplicationContext.layeredCanvas.annotationPanel.setAddEllipse(false);
            }
            toolsButtonGroup.setSelected(ellipseButton.getModel(), true);
        } else {
            JOptionPane.showMessageDialog(ImageToolbar.this, "Tile selected is not valid for this process");
        }
    }

    public void setAnnotationToolsStatus() {
        if (ApplicationContext.layeredCanvas.annotationPanel.isShowAnnotation()) {
            showAnnotationTools();
        } else {
            hideAnnotationTools();
        }
    }

    public void doReset() {
        loopCheckbox.setSelected(false);
        doCineLoop();
        JPanel currentSeriesPanel = (JPanel) ApplicationContext.layeredCanvas.getParent();
        for (int i = 0; i < currentSeriesPanel.getComponentCount(); i++) {
            LayeredCanvas tempcanCanvas = (LayeredCanvas) currentSeriesPanel.getComponent(i);
            if (tempcanCanvas != null && tempcanCanvas.annotationPanel != null && tempcanCanvas.imgpanel != null) {
                tempcanCanvas.imgpanel.reset();
            }
        }
        setWindowing();
    }

    public void setWindowing() {
        if (ApplicationContext.layeredCanvas.annotationPanel != null && ApplicationContext.layeredCanvas.imgpanel != null) {
            ApplicationContext.layeredCanvas.imgpanel.setWindowingToolsAsDefault();
            toolsButtonGroup.setSelected(windowing.getModel(), true);
        }
    }

    public void doStack() {
        toolsButtonGroup.clearSelection();
        if (ApplicationContext.layeredCanvas.annotationPanel != null && ApplicationContext.layeredCanvas.imgpanel != null) {
            ApplicationContext.layeredCanvas.annotationPanel.stopPanning();
            ApplicationContext.layeredCanvas.annotationPanel.setAddLine(false);
            ApplicationContext.layeredCanvas.annotationPanel.setAddArrow(false);
            ApplicationContext.layeredCanvas.annotationPanel.setAddEllipse(false);
            ApplicationContext.layeredCanvas.annotationPanel.setAddRect(false);
            ApplicationContext.layeredCanvas.imgpanel.doStack();
            ApplicationContext.layeredCanvas.imgpanel.repaint();
            if (ApplicationContext.layeredCanvas.imgpanel.isStackSelected()) {
                toolsButtonGroup.setSelected(stackButton.getModel(), true);
            }
        } else {
            JOptionPane.showMessageDialog(ImageToolbar.this, "Tile selected is not valid for this process");
        }
    }

    public void doScout() {
        if (!ImagePanel.isDisplayScout() && ((JPanel) ((JPanel) ApplicationContext.layeredCanvas.getParent()).getParent()).getComponentCount() > 1 && ((JPanel) ApplicationContext.layeredCanvas.getParent()).getComponentCount() == 1) {
            ImagePanel.setDisplayScout(true);
            scoutButton.setSelected(true);
            LocalizerDelegate localizer = new LocalizerDelegate(false);
            localizer.start();
        } else {
            ImagePanel.setDisplayScout(false);
            scoutButton.setSelected(false);
            LocalizerDelegate.hideAllScoutLines();
        }
    }

    public void doCineLoop() {
        if (ApplicationContext.layeredCanvas.annotationPanel != null && ApplicationContext.layeredCanvas.imgpanel != null) {
            if (loopCheckbox.isSelected()) {
                try {
                    timer = new Timer();
                    timer.scheduleAtFixedRate(new CineTimer(), 0, (11 - loopSlider.getValue()) * 50);//
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                if (timer != null) {
                    timer.cancel();
                }
            }
        } else {
            loopCheckbox.setSelected(false);
        }
    }

    public void addKeyEventToViewer() {
        addKeyEventDispatcher();
    }

    public void doTextOverlay() {
        if (ApplicationContext.layeredCanvas.annotationPanel != null && ApplicationContext.layeredCanvas.imgpanel != null) {
            ApplicationContext.layeredCanvas.textOverlay.toggleTextOverlay();
        } else {
            JOptionPane.showMessageDialog(ImageToolbar.this, "Tile selected is not valid for this process");
        }
    }

    public void refreshToolsDisplay() {
        if (!windowing.isEnabled()) {
            enableAllTools();
        } else {
            setAnnotationToolsStatus();
        }
    }

    private void enableAllTools() {
        layoutButton.setEnabled(true);
        windowing.setEnabled(true);
        presetButton.setEnabled(true);
        probeButton.setEnabled(true);
        verticalFlip.setEnabled(true);
        horizontalFlip.setEnabled(true);
        leftRotate.setEnabled(true);
        rightRotate.setEnabled(true);
        zoomin.setEnabled(true);
        zoomoutButton.setEnabled(true);
        panButton.setEnabled(true);
        invert.setEnabled(true);
        annotationVisibility.setEnabled(true);
        textOverlay.setEnabled(true);
        reset.setEnabled(true);
        exportButton.setEnabled(true);
        metaDataButton.setEnabled(true);
        stackButton.setEnabled(true);
        scoutButton.setEnabled(true);
        cube3DButton.setEnabled(true);
        synchronizeButton.setEnabled(true);
        setAnnotationToolsStatus();
        loopSlider.setEnabled(true);
    }

    public void disableAllTools() {
        layoutButton.setEnabled(false);
        windowing.setEnabled(false);
        presetButton.setEnabled(false);
        probeButton.setEnabled(false);
        verticalFlip.setEnabled(false);
        horizontalFlip.setEnabled(false);
        leftRotate.setEnabled(false);
        rightRotate.setEnabled(false);
        zoomin.setEnabled(false);
        zoomoutButton.setEnabled(false);
        panButton.setEnabled(false);
        invert.setEnabled(false);
        rulerButton.setEnabled(false);
        arrowButton.setEnabled(false);
        rectangleButton.setEnabled(false);
        ellipseButton.setEnabled(false);
        clearAllMeasurement.setEnabled(false);
        deleteMeasurement.setEnabled(false);
        moveMeasurement.setEnabled(false);
        annotationVisibility.setEnabled(false);
        textOverlay.setEnabled(false);
        reset.setEnabled(false);
        exportButton.setEnabled(false);
        metaDataButton.setEnabled(false);
        stackButton.setEnabled(false);
        scoutButton.setEnabled(false);
        cube3DButton.setEnabled(false);
        synchronizeButton.setEnabled(false);
        loopSlider.setEnabled(false);
    }

    public void resetCineTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public void showAnnotationTools() {
        arrowButton.setEnabled(true);
        rulerButton.setEnabled(true);
        rectangleButton.setEnabled(true);
        ellipseButton.setEnabled(true);
        clearAllMeasurement.setEnabled(true);
        deleteMeasurement.setEnabled(true);
        moveMeasurement.setEnabled(true);
    }

    public void hideAnnotationTools() {
        arrowButton.setEnabled(false);
        rulerButton.setEnabled(false);
        rectangleButton.setEnabled(false);
        ellipseButton.setEnabled(false);
        clearAllMeasurement.setEnabled(false);
        deleteMeasurement.setEnabled(false);
        moveMeasurement.setEnabled(false);
        String actionCommand = null;
        if (toolsButtonGroup != null && toolsButtonGroup.getSelection() != null) {
            actionCommand = toolsButtonGroup.getSelection().getActionCommand();
        }
        if (ApplicationContext.layeredCanvas.annotationPanel != null && ApplicationContext.layeredCanvas.imgpanel != null) {
            if (actionCommand != null) {
                if (actionCommand.equalsIgnoreCase("ruler") || actionCommand.equalsIgnoreCase("arrow") || actionCommand.equalsIgnoreCase("rectangle") || actionCommand.equalsIgnoreCase("ellipse") || actionCommand.equalsIgnoreCase("deleteMeasurement") || actionCommand.equalsIgnoreCase("moveMeasurement")) {
                    ApplicationContext.layeredCanvas.annotationPanel.setAddLine(false);
                    ApplicationContext.layeredCanvas.annotationPanel.setAddEllipse(false);
                    ApplicationContext.layeredCanvas.annotationPanel.setAddRect(false);
                    ApplicationContext.layeredCanvas.annotationPanel.setAddArrow(false);
                    ApplicationContext.layeredCanvas.annotationPanel.stopPanning();
                    ApplicationContext.layeredCanvas.imgpanel.doWindowing();
                    toolsButtonGroup.clearSelection();
                    toolsButtonGroup.setSelected(windowing.getModel(), true);
                }
            }
        }
    }

    public LayoutPopupDesign getImageLayoutPopupDesign() {
        return layoutPopupDesign;
    }

    public void applyLocale() {
        layoutButton.setToolTipText(ApplicationContext.currentBundle.getString("ImageView.imageLayout.toolTipText"));
        windowing.setToolTipText(ApplicationContext.currentBundle.getString("ImageView.windowingButton.toolTipText"));
        presetButton.setToolTipText(ApplicationContext.currentBundle.getString("ImageView.presetButton.toolTipText"));
        probeButton.setToolTipText(ApplicationContext.currentBundle.getString("ImageView.probeButton.toolTipText"));
        verticalFlip.setToolTipText(ApplicationContext.currentBundle.getString("ImageView.verticalFlipButton.toolTipText"));
        horizontalFlip.setToolTipText(ApplicationContext.currentBundle.getString("ImageView.horizontalFlipButton.toolTipText"));
        leftRotate.setToolTipText(ApplicationContext.currentBundle.getString("ImageView.leftRotateButton.toolTipText"));
        rightRotate.setToolTipText(ApplicationContext.currentBundle.getString("ImageView.rightRotateButton.toolTipText"));
        zoomin.setToolTipText(ApplicationContext.currentBundle.getString("ImageView.zoomInButton.toolTipText"));
        zoomoutButton.setToolTipText(ApplicationContext.currentBundle.getString("ImageView.zoomOutButton.toolTipText"));
        panButton.setToolTipText(ApplicationContext.currentBundle.getString("ImageView.panButton.toolTipText"));
        invert.setToolTipText(ApplicationContext.currentBundle.getString("ImageView.invertButton.toolTipText"));
        rulerButton.setToolTipText(ApplicationContext.currentBundle.getString("ImageView.rulerButton.toolTipText"));
        rectangleButton.setToolTipText(ApplicationContext.currentBundle.getString("ImageView.rectangleButton.toolTipText"));
        ellipseButton.setToolTipText(ApplicationContext.currentBundle.getString("ImageView.ellipseButton.toolTipText"));
        arrowButton.setToolTipText(ApplicationContext.currentBundle.getString("ImageView.arrowButton.toolTipText"));
        deleteMeasurement.setToolTipText(ApplicationContext.currentBundle.getString("ImageView.deleteSelectedMeasurementButton.toolTipText"));
        clearAllMeasurement.setToolTipText(ApplicationContext.currentBundle.getString("ImageView.deleteAllMeasurementButton.toolTipText"));
        moveMeasurement.setToolTipText(ApplicationContext.currentBundle.getString("ImageView.moveMeasurementButton.toolTipText"));
        annotationVisibility.setToolTipText(ApplicationContext.currentBundle.getString("ImageView.annotaionOverlayButton.toolTipText"));
        textOverlay.setToolTipText(ApplicationContext.currentBundle.getString("ImageView.textOverlayButton.toolTipText"));
        reset.setToolTipText(ApplicationContext.currentBundle.getString("ImageView.resetButton.toolTipText"));
        exportButton.setToolTipText(ApplicationContext.currentBundle.getString("ImageView.exportButton.toolTipText"));
        metaDataButton.setToolTipText(ApplicationContext.currentBundle.getString("ImageView.metaDataButton.toolTipText"));
        stackButton.setToolTipText(ApplicationContext.currentBundle.getString("ImageView.stackButton.toolTipText"));
        scoutButton.setToolTipText(ApplicationContext.currentBundle.getString("ImageView.scoutButton.toolTipText"));
        cube3DButton.setToolTipText(ApplicationContext.currentBundle.getString("ImageView.3DButton.toolTipText"));
        synchronizeButton.setToolTipText(ApplicationContext.currentBundle.getString("ImageView.synchronizeButton.toolTipText"));
        loopCheckbox.setToolTipText(ApplicationContext.currentBundle.getString("ImageView.loopChk.toolTipText"));
        layoutPopupDesign.applyLocaleChange();
    }

    public void deselectTools() {
        scoutButton.setSelected(false);
        synchronizeButton.setSelected(false);
        invert.setSelected(false);
        probeButton.setSelected(false);
    }

    public void deselectLoopChk() {
        loopCheckbox.setSelected(false);
    }

    public boolean getAnnotationStatus() {
        if (rulerButton.isEnabled()) {
            return true;
        }
        return false;
    }

    public void disableImageTools() {
        if (windowing.isEnabled()) {
            disableAllTools();
        }
    }

    public void enableImageTools() {
        if (!windowing.isEnabled()) {
            enableAllTools();
        }
    }
}