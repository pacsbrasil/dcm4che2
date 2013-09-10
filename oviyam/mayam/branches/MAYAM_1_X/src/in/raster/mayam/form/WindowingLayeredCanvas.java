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
import in.raster.mayam.param.TextOverlayParam;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.JLayeredPane;

/**
 *
 * @author  BabuHussain
 * @version 0.5
 *
 */
public class WindowingLayeredCanvas extends JLayeredPane implements ComponentListener {

    private static final int ImageHeight = 384;
    private static final int ImageWidth = 384;
    public WindowingImagePanel imgpanel;
    public WindowingTextOverlay textOverlay;
    public WindowingCanvas canvas;
    private String filePath = "";

    public WindowingLayeredCanvas() {
    }

    public WindowingLayeredCanvas(String filePath) {
        this.filePath = filePath;
        constitudeChild();
    }

    private void constitudeChild() {
        this.setBackground(Color.BLACK);
        this.setLayout(null);
        createImageCanvas(filePath);
        createTextOverlay();
        setTextOverlayParam();
        findMultiframeStatus();
        createLayers();
        this.addComponentListener(this);
    }

    private void createLayers() {
        this.add(canvas, Integer.valueOf(0));
        this.add(textOverlay, Integer.valueOf(1));
    }

    private void createImageCanvas(String filePath) {
        canvas = new WindowingCanvas(this);
        canvas.setSize(ImageWidth, ImageHeight);
        imgpanel = new WindowingImagePanel(filePath, canvas);
        imgpanel.setSize(ImageWidth, ImageHeight);
        canvas.add(imgpanel);
    }

    private void createTextOverlay() {
        textOverlay = new WindowingTextOverlay(this);
        textOverlay.setForeground(Color.white);
        textOverlay.setSize(new Dimension(ImageWidth, ImageHeight));
    }

    private void setTextOverlayParam() {
        TextOverlayParam textOverlayParam = new TextOverlayParam();
        if (imgpanel.getPatientID() != null) {
            textOverlayParam.setPatientID(imgpanel.getPatientID());
        }

        if (imgpanel.getPatientName() != null) {
            textOverlayParam.setPatientName(imgpanel.getPatientName());
        }

        if (imgpanel.getInstitutionName() != null) {
            textOverlayParam.setInstitutionName(imgpanel.getInstitutionName());
        }

        if (imgpanel.getBodyPartExamined() != null) {
            textOverlayParam.setBodyPartExamined(imgpanel.getBodyPartExamined());
        }

        if (imgpanel.getPatientPosition() != null) {
            textOverlayParam.setPatientPosition(imgpanel.getPatientPosition());
        }


        if (imgpanel.getSex() != null) {
            textOverlayParam.setSex(imgpanel.getSex());
        }

        if (imgpanel.getStudyDate() != null) {
            textOverlayParam.setStudyDate(imgpanel.getStudyDate());
        }

        if (imgpanel.getStudyTime() != null) {
            textOverlayParam.setStudyTime(imgpanel.getStudyTime());
        }

        if (imgpanel.getWindowLevel() != -1) {
            textOverlayParam.setWindowLevel(Integer.toString(imgpanel.getWindowLevel()));
        }

        if (imgpanel.getWindowWidth() != -1) {
            textOverlayParam.setWindowWidth(Integer.toString(imgpanel.getWindowWidth()));
        }

        if (imgpanel.getCurrentInstanceNo() != -1) {            
            textOverlayParam.setCurrentInstance(imgpanel.getCurrentInstanceNo());
        }

        if (imgpanel.getTotalInstance() != -1) {
            textOverlayParam.setTotalInstance(Integer.toString(imgpanel.getTotalInstance()));
        }

        textOverlay.setTextOverlayParam(textOverlayParam);
    }
    public void findMultiframeStatus()
    {
        if (imgpanel.isMulitiFrame()) {
          textOverlay.multiframeStatusDisplay(true);
           if(!ApplicationContext.databaseRef.getMultiframeStatus())
          textOverlay.getTextOverlayParam().setFramePosition("1/"+imgpanel.getnFrames());
        }
        else
            textOverlay.multiframeStatusDisplay(false);
       

    }
    public void centerImagePanel() {
        int xPosition = (this.getSize().width - imgpanel.getSize().width) / 2;
        int yPosition = (this.getSize().height - imgpanel.getSize().height) / 2;
        imgpanel.setBounds(xPosition, yPosition, imgpanel.getSize().width, imgpanel.getSize().height);
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void clearAll() {
        imgpanel.removeAll();
        textOverlay.removeAll();
        canvas.removeAll();
        this.removeAll();
    }

    public void componentResized(ComponentEvent e) {
        try {
            this.canvas.resizeHandler();
            this.imgpanel.resizeHandler();
            this.textOverlay.resizeHandler();
        } catch (Exception ex) {ex.printStackTrace();
        }

    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
    }

    public void componentHidden(ComponentEvent e) {
    }

    public WindowingImagePanel getImgpanel() {
        return imgpanel;
    }

}
