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
import in.raster.mayam.delegate.SeriesChooserDelegate;
import in.raster.mayam.model.Series;
import in.raster.mayam.model.Study;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.LineBorder;

/**
 *
 * @author  BabuHussain
 * @version 0.5
 *
 */
public class LayeredCanvas extends JLayeredPane implements FocusListener, MouseListener, ComponentListener {

    public static int ImageHeight = 512;
    public static int ImageWidth = 512;
    public ImagePanel imgpanel;
    public AnnotationPanel annotationPanel;
    public TextOverlay textOverlay;
    public Canvas canvas;
    public boolean focusGained = false;
    public boolean fileIsNull = false;
    private String studyUID = "";
    private String[] comparedWithStudies;

    public LayeredCanvas() {
        this.addFocusListener(this);
        this.addMouseListener(this);
        this.addComponentListener(this);
        this.setBorder(new LineBorder(Color.DARK_GRAY));
        fileIsNull = true;
    }

    public LayeredCanvas(String filePath) {
        this.setBackground(Color.BLACK);
        this.setLayout(null);
        createImageCanvas(filePath);
        createAnnotationOverlay();
        createTextOverlay();
        setTextOverlayParam();
        findMultiframeStatus();
        createLayers();
        this.addComponentListener(this);
    }
    
    /**
     * This routine used to create the component once again for another series.
     * @param filePath
     */
    public void createSubComponents(String filePath) {
        if (textOverlay != null) {
            this.remove(textOverlay);
        }
        if (annotationPanel != null) {
            this.remove(annotationPanel);
        }
        if (canvas != null) {
            this.remove(canvas);
        }
        createImageCanvas(filePath);
        createAnnotationOverlay();
        createTextOverlay();
        setTextOverlayParam();
        findMultiframeStatus();
        createLayers();
        this.imgpanel.firstTime = false;
        this.imgpanel.reset();
        this.imgpanel.revalidate();
        this.imgpanel.repaint();
        this.annotationPanel.revalidate();
        this.annotationPanel.repaint();
        this.textOverlay.revalidate();
        this.textOverlay.repaint();
    }

    private void createLayers() {
        this.add(canvas, Integer.valueOf(0));
        this.add(annotationPanel, Integer.valueOf(2));
        this.add(textOverlay, Integer.valueOf(1));
    }

    private void createImageCanvas(String filePath) {
        canvas = new Canvas(this);
        canvas.setSize(514, 514);
        imgpanel = new ImagePanel(filePath, canvas);
        imgpanel.setSize(ImageWidth, ImageHeight);
        canvas.add(imgpanel);
    }

    private void createAnnotationOverlay() {
        annotationPanel = new AnnotationPanel(this);
        annotationPanel.setForeground(Color.white);
        annotationPanel.setSize(new Dimension(ImageWidth, ImageHeight));
    }

    private void createTextOverlay() {
        textOverlay = new TextOverlay(this);
        textOverlay.setForeground(Color.white);
        textOverlay.setSize(new Dimension(ImageWidth, ImageHeight));
    }

    public boolean isFocusGained() {
        return focusGained;
    }

    public void setFocusGained(boolean focusGained) {
        this.focusGained = focusGained;
        repaint();
    }

    public void findMultiframeStatus() {
        if (imgpanel.isMulitiFrame()) {
            textOverlay.multiframeStatusDisplay(true);
            if (!ApplicationContext.databaseRef.getMultiframeStatus()) {
                textOverlay.getTextOverlayParam().setFramePosition("1/" + imgpanel.getnFrames());
            }
        } else {
            textOverlay.multiframeStatusDisplay(false);
        }

    }

    private void setTextOverlayParam() {
        textOverlay.setTextOverlayParam(imgpanel.getTextOverlayParam());
    }

    public void centerImagePanel() {
        int xPosition = (this.getSize().width - imgpanel.getSize().width) / 2;
        int yPosition = (this.getSize().height - imgpanel.getSize().height) / 2;
        imgpanel.setBounds(xPosition, yPosition, imgpanel.getSize().width, imgpanel.getSize().height);
    }

    public void focusGained(FocusEvent arg0) {
        this.setBorder(new LineBorder(Color.YELLOW));
    }

    public void focusLost(FocusEvent arg0) {
        this.setBorder(new LineBorder(Color.DARK_GRAY));
    }

    public void mouseClicked(MouseEvent arg0) {
        if (fileIsNull) {
            setSelection();
            designContext();

        }

    }

    /**
     * This routine used to set the selection coloring.
     */
    public void setSelectionColoring() {
        this.setBorder(new LineBorder(new Color(255, 138, 0)));
        ApplicationContext.imgView.getImageToolbar().disableAllTools();
    }

    /**
     * This routine used to remove the selection coloring.
     */
    public void setNoSelectionColoring() {
        this.setBorder(new LineBorder(Color.DARK_GRAY));
    }

    public void setSelection() {
        if (ApplicationContext.layeredCanvas != null) {
            if (ApplicationContext.layeredCanvas.getCanvas() != null) {
                ApplicationContext.layeredCanvas.getCanvas().setNoSelectionColoring();
            } else {
                ApplicationContext.layeredCanvas.setNoSelectionColoring();
            }
        }
        ApplicationContext.imgPanel = this.imgpanel;
        ApplicationContext.layeredCanvas = this;
        ApplicationContext.annotationPanel = this.annotationPanel;
        ApplicationContext.layeredCanvas.setSelectionColoring();
    }

    public void mousePressed(MouseEvent arg0) {
    }

    public void mouseReleased(MouseEvent arg0) {
    }

    public void mouseEntered(MouseEvent arg0) {
    }

    public void mouseExited(MouseEvent arg0) {
    }

    private void designContext() {
        final LayeredCanvas ref = this;
        ArrayList<Study> studyList = null;
        if (((JPanel) ApplicationContext.imgView.jTabbedPane1.getSelectedComponent()).getName() != null) {
            String patientName = ((JPanel) ApplicationContext.imgView.jTabbedPane1.getSelectedComponent()).getName();
            studyList = ApplicationContext.databaseRef.getStudyUIDBasedOnPatientName(patientName);
        }
        JPopupMenu jPopupMenu2 = new JPopupMenu();
        for (Study study : studyList) {
            ArrayList<Series> seriesList = ApplicationContext.databaseRef.getSeriesList(study.getStudyInstanceUID());
            JMenu menu = new JMenu(study.getStudyDesc());
            for (final Series series : seriesList) {
                JMenuItem menuitem = new JMenuItem(series.getSeriesDesc());
                menu.add(menuitem);
                menuitem.addActionListener(new java.awt.event.ActionListener() {

                    public void actionPerformed(ActionEvent arg0) {
                        if (ApplicationContext.databaseRef.getMultiframeStatus()) {
                            SeriesChooserDelegate seriesChooser = new SeriesChooserDelegate(series.getStudyInstanceUID(), series.getSeriesInstanceUID(), series.isMultiframe(), series.getInstanceUID(), ref);
                        } else {
                            SeriesChooserDelegate seriesChooser = new SeriesChooserDelegate(series.getStudyInstanceUID(), series.getSeriesInstanceUID(), ref);
                        }
                    }
                });
            }
            jPopupMenu2.add(menu);
        }
        this.setComponentPopupMenu(jPopupMenu2);
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public String getStudyUID() {
        return studyUID;
    }

    public void setStudyUID(String studyUID) {
        this.studyUID = studyUID;
    }

    public AnnotationPanel getAnnotationPanel() {
        return annotationPanel;
    }

    public TextOverlay getTextOverlay() {
        return textOverlay;
    }

    public void componentResized(ComponentEvent e) {
        try {
            if (canvas != null) {
                this.canvas.resizeHandler();
                this.imgpanel.resizeHandler();
                this.textOverlay.resizeHandler();
                this.annotationPanel.resizeHandler();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
    }

    public void componentHidden(ComponentEvent e) {
    }

    public String[] getComparedWithStudies() {
        return comparedWithStudies;
    }

    public void setComparedWithStudies(String[] comparedWithStudies) {
        this.comparedWithStudies = comparedWithStudies;
    }

}
