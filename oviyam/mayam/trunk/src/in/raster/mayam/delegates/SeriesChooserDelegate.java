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
import in.raster.mayam.form.LayeredCanvas;
import in.raster.mayam.models.SeriesAnnotations;
import in.raster.mayam.param.TextOverlayParam;
import java.awt.image.ColorModel;
import java.util.ArrayList;
import javax.swing.JPanel;
import org.dcm4che.image.ColorModelParam;

/**
 *
 * @author BabuHussain
 * @version 0.5
 *
 */
public class SeriesChooserDelegate extends Thread {

    private String studyUID;
    private String seriesUID;
    private LayeredCanvas canvas;
    private String instanceUID;
    private int instanceNumber;
    JPanel tilePanel;

    public SeriesChooserDelegate() {
    }

    public SeriesChooserDelegate(String studyUID, String seriesUID, LayeredCanvas canvas) {
        this.studyUID = studyUID;
        this.seriesUID = seriesUID;
        this.canvas = canvas;
        this.start();
    }

    public SeriesChooserDelegate(String studyUID, String seriesUID, String sopUid, LayeredCanvas canvas, int instanceNo) {
        this.studyUID = studyUID;
        this.seriesUID = seriesUID;
        this.canvas = canvas;
        this.instanceUID = sopUid;
        this.instanceNumber = instanceNo;
        this.start();
    }

    public SeriesChooserDelegate(String studyUID, String seriesUID, String sopUid, int instanceNumber, JPanel panel) {
        this.studyUID = studyUID;
        this.seriesUID = seriesUID;
        this.instanceUID = sopUid;
        this.instanceNumber = instanceNumber;
        this.tilePanel = panel;
        this.start();
    }

    /**
     * This routine used to change the series in the tile.
     */
    private void changeSeries() {
        changeSeries_SepMulti();
        if (canvas != null) {
            canvas.revalidate();
            canvas.repaint();
        }
    }

    @Override
    public void run() {
        changeSeries();
    }

    private void changeSeries_SepMulti() {
        if (canvas != null) {
            if (instanceUID == null) {
                canvas.createSubComponents(ApplicationContext.databaseRef.getFirstInstanceLocation(studyUID, seriesUID), 0, false);
            } else {
                canvas.createSubComponents(ApplicationContext.databaseRef.getFileLocation(studyUID, seriesUID, instanceUID), instanceNumber, false);
                canvas.imgpanel.updateCurrentInstance();
            }
            canvas.imgpanel.setCurrentSeriesAnnotation();
        } else {
            instanceNumber++;
            int x = instanceNumber % tilePanel.getComponentCount();
            if (x > 0) {
                instanceNumber -= x;
                createImageCanvas(instanceNumber);
            } else {
                instanceNumber -= tilePanel.getComponentCount();
                createImageCanvas(instanceNumber);
            }
        }
    }

    private void createImageCanvas(int imageToDisplay) {
        if (imageToDisplay < 0) {
            imageToDisplay = 0;
        }
        String fileLocation = ApplicationContext.databaseRef.getFileLocation(studyUID, seriesUID, imageToDisplay);
        LayeredCanvas tempCanvas = ((LayeredCanvas) tilePanel.getComponent(0));
        tempCanvas.createSubComponents(fileLocation, instanceNumber, true);
        TextOverlayParam textOverlayParam = tempCanvas.imgpanel.getTextOverlayParam();
        tempCanvas.imgpanel.getFilePathsifLink();
        double pixelSpacingX = tempCanvas.imgpanel.getPixelSpacingX();
        double pixelSpacingY = tempCanvas.imgpanel.getPixelSpacingY();
        tempCanvas.imgpanel.setCurrentSeriesAnnotation();
        SeriesAnnotations currentSeriesAnnotation = tempCanvas.imgpanel.getCurrentSeriesAnnotation();
        ArrayList<String> instanceUidList = tempCanvas.imgpanel.getInstanceUidList();
        String fileLoc = tempCanvas.imgpanel.getFileLocation();
        ColorModelParam cmParam = tempCanvas.imgpanel.getCmParam();
        ColorModel cm = tempCanvas.imgpanel.getCm();
        int windowLevel = tempCanvas.imgpanel.getWindowLevel();
        int windowWidth = tempCanvas.imgpanel.getWindowWidth();
        String modality = tempCanvas.imgpanel.getModality();
        String studyDesc = tempCanvas.imgpanel.getStudyDesc();
        tempCanvas.imgpanel.setCurrentInstanceNo(imageToDisplay);
        tempCanvas.textOverlay.getTextOverlayParam().setCurrentInstance(imageToDisplay);
        ApplicationContext.imgBuffer.clearBuffer();
        ApplicationContext.imgBuffer = new ImageBuffer(tempCanvas.imgpanel);
        ApplicationContext.imageUpdator.terminateThread();
        ApplicationContext.imageUpdator = new ImageGenerator(ApplicationContext.imgBuffer, ApplicationContext.imgBuffer.getImagePanelRef(), true);
        ApplicationContext.imgBuffer.setDefaultBufferSize(tilePanel.getComponentCount() + tilePanel.getComponentCount() + tilePanel.getComponentCount());
        if (instanceUidList.size() > ApplicationContext.imgBuffer.getDefaultBufferSize()) {
            if (imageToDisplay - tilePanel.getComponentCount() > 0 && imageToDisplay + tilePanel.getComponentCount() < instanceUidList.size()) {
                ApplicationContext.imageUpdator.setParameters(instanceNumber - tilePanel.getComponentCount(), instanceNumber + tilePanel.getComponentCount() + tilePanel.getComponentCount(), true);
            } else {
                ApplicationContext.imageUpdator.setParameters(instanceUidList.size() - tilePanel.getComponentCount(), tilePanel.getComponentCount() + tilePanel.getComponentCount(), true);
            }
        } else {
            ApplicationContext.imageUpdator.setParameters(0, instanceUidList.size(), true);
        }

        ApplicationContext.imageUpdator.start();
        imageToDisplay++;

        for (int i = 1; i < tilePanel.getComponentCount(); i++) {
            tempCanvas = ((LayeredCanvas) tilePanel.getComponent(i));
            if (imageToDisplay < instanceUidList.size()) {
                tempCanvas.createImageLayoutComponents();
                tempCanvas.textOverlay.setTextOverlayParam(new TextOverlayParam(textOverlayParam.getPatientName(), textOverlayParam.getPatientID(), textOverlayParam.getSex(), textOverlayParam.getStudyDate(), textOverlayParam.getStudyDescription(), textOverlayParam.getSeriesDescription(), textOverlayParam.getBodyPartExamined(), textOverlayParam.getInstitutionName(), textOverlayParam.getWindowLevel(), textOverlayParam.getWindowWidth(), i, textOverlayParam.getTotalInstance(), textOverlayParam.isMultiframe()));
                tempCanvas.imgpanel.setImageInfo(pixelSpacingX, pixelSpacingY, studyUID, seriesUID, fileLoc, currentSeriesAnnotation, instanceUidList, cmParam, cm, windowLevel, windowWidth, modality, studyDesc);
                tempCanvas.imgpanel.setImage(imageToDisplay);
                tempCanvas.imgpanel.setVisibility(tempCanvas, true);
            } else {
                tempCanvas = ((LayeredCanvas) tilePanel.getComponent(i));
                try {
                    tempCanvas.imgpanel.setVisibility(tempCanvas, false);
                } catch (NullPointerException npe) {
                    System.out.println("Null pointer exception [createImageCanvas]");
                    //Null pointer occurs when there is no image panel
                }
            }
            imageToDisplay++;
        }
        ApplicationContext.setImageIdentification();
    }
}
