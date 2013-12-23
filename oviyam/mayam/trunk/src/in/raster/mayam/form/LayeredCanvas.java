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
import in.raster.mayam.delegates.LocalizerDelegate;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.border.LineBorder;

/**
 *
 * @author BabuHussain
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

    public LayeredCanvas() {
        addFocusListener(this);
        addMouseListener(this);
        addComponentListener(this);
        setBorder(new LineBorder(Color.DARK_GRAY));
        fileIsNull = true;
    }

    public LayeredCanvas(File file, int startBufferingFrom, boolean isImageLayout) {
        setBackground(Color.BLACK);
        setLayout(null);
        createImageCanvas(file, startBufferingFrom, isImageLayout);
        createAnnotationOverlay();
        createTextOverlay();
        setTextOverlayParam();
        findMultiframeStatus();
        createLayers();
        addComponentListener(this);
    }

    public LayeredCanvas(boolean isImageLayout, String studyUid, String seriesUid) {
        setBackground(Color.BLACK);
        setLayout(null);
        createImageCanvas();
        createAnnotationOverlay();
        createTextOverlay();
        createLayers();
        addComponentListener(this);
    }

    /**
     * This routine used to create the component once again for another series.
     *
     * @param filePath
     */
    public void createSubComponents(String filePath, int startFrom, boolean isImageLayout) {
        if (this.canvas != null) {
            canvas.remove(imgpanel);
            annotationPanel.resetMeasurements();
            if (filePath != null) {
                imgpanel = new ImagePanel(new File(filePath), canvas);
            } else {
                imgpanel = new ImagePanel(canvas);
            }
            if (!isImageLayout) {
                imgpanel.startImageBuffering(startFrom);
            }
            canvas.add(imgpanel);
        } else if (this.imgpanel == null && this.annotationPanel == null && this.textOverlay == null) {
            createImageCanvas(new File(filePath), startFrom, isImageLayout);
            createAnnotationOverlay();
            createTextOverlay();
            createLayers();
        }
        textOverlay.setTextOverlayParam(this.imgpanel.getTextOverlayParam());
        imgpanel.revalidate();
        imgpanel.repaint();
        annotationPanel.revalidate();
        annotationPanel.repaint();
        textOverlay.revalidate();
        textOverlay.repaint();
        imgpanel.getCanvas().setSelection(true);
        ApplicationContext.setSeriesContext();
        if (!isImageLayout && ImagePanel.isDisplayScout()) {
            LocalizerDelegate localizer = new LocalizerDelegate(false);
            localizer.start();
        }
        try {
            if (canvas != null) {
                canvas.resizeHandler();
                imgpanel.resizeHandler();
                textOverlay.resizeHandler();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void createImageLayoutComponents() {
        if (this.canvas != null) {
            canvas.remove(imgpanel);
            annotationPanel.resetMeasurements();
            imgpanel = new ImagePanel(canvas);
            canvas.add(imgpanel);
        } else if (this.imgpanel == null) {
            createImageCanvas(null, 0, true);
            createAnnotationOverlay();
            createTextOverlay();
            createLayers();
        }
        imgpanel.revalidate();
        imgpanel.repaint();
        textOverlay.revalidate();
        textOverlay.repaint();
    }

    private void createLayers() {
        this.add(canvas, Integer.valueOf(0));
        this.add(annotationPanel, Integer.valueOf(2));
        this.add(textOverlay, Integer.valueOf(1));
    }

    private void createImageCanvas(File file, int startFrom, boolean isImageLayout) {
        canvas = new Canvas(this);
        canvas.setSize(514, 514);
        if (file != null) {
            imgpanel = new ImagePanel(file, canvas);
        } else {
            imgpanel = new ImagePanel(canvas);
        }
        if (!isImageLayout) {
            imgpanel.startImageBuffering(startFrom);
        }
        canvas.add(imgpanel);
    }

    private void createImageCanvas() {
        canvas = new Canvas(this);
        canvas.setSize(514, 514);
        imgpanel = new ImagePanel(canvas);
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

    private void findMultiframeStatus() {
        if (imgpanel.isMultiFrame()) {
            textOverlay.multiframeStatusDisplay(true);
        } else {
            textOverlay.multiframeStatusDisplay(false);
        }

    }

    private void setTextOverlayParam() {
        textOverlay.setTextOverlayParam(imgpanel.getTextOverlayParam());
    }

    @Override
    public void focusGained(FocusEvent arg0) {
        this.setBorder(new LineBorder(Color.YELLOW));
    }

    @Override
    public void focusLost(FocusEvent arg0) {
        this.setBorder(new LineBorder(Color.DARK_GRAY));
    }

    @Override
    public void mouseClicked(MouseEvent arg0) {
        if (fileIsNull) {
            setSelection();
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
        if (this.canvas != null) {
            if (ApplicationContext.layeredCanvas != null) {
                if (ApplicationContext.layeredCanvas.getCanvas() != null) {
                    ApplicationContext.layeredCanvas.getCanvas().setNoSelectionColoring();
                } else {
                    ApplicationContext.layeredCanvas.setNoSelectionColoring();
                }
            }
            ApplicationContext.layeredCanvas = null;
            ApplicationContext.layeredCanvas = this;
            ApplicationContext.layeredCanvas.setSelectionColoring();
        }
    }

    @Override
    public void mousePressed(MouseEvent arg0) {
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
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

    @Override
    public void componentResized(ComponentEvent e) {
        try {
            if (canvas != null) {
                this.canvas.resizeHandler();
                this.imgpanel.resizeHandler();
                this.textOverlay.resizeHandler();
                this.annotationPanel.resizeHandler();
            }
        } catch (NullPointerException ex) {
            //Null pointer exception occurs if any of the components creation on progress
        }
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }
}
