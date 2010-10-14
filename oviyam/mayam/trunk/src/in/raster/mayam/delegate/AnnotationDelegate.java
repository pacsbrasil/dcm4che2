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
package in.raster.mayam.delegate;

import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.form.LayeredCanvas;
import in.raster.mayam.form.LayoutManagerPanel;
import in.raster.mayam.form.MainScreen;
import in.raster.mayam.model.Instance;
import in.raster.mayam.model.Series;
import in.raster.mayam.model.Study;
import in.raster.mayam.util.measurement.InstanceAnnotation;
import in.raster.mayam.util.measurement.SeriesAnnotation;
import in.raster.mayam.util.measurement.StudyAnnotation;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

/**
 *
 * @author  BabuHussain
 * @version 0.6
 *
 */
public class AnnotationDelegate {

    public AnnotationDelegate() {
    }

    /**
     * This method used to save the annotation information to the file.
     *
     * @param i
     */
    public void saveAnnotation(int i) {
        Study studyTobeDelete = null;
        LayeredCanvas tempCanvas = null;
        StudyAnnotation studyAnnotation = new StudyAnnotation();
        for (int x = 0; x < ((JPanel) ApplicationContext.imgView.jTabbedPane1.getComponent(i)).getComponentCount(); x++) {
            if (((JPanel) ApplicationContext.imgView.jTabbedPane1.getComponent(i)).getComponent(x) instanceof LayeredCanvas) {
                tempCanvas = ((LayeredCanvas) ((JPanel) ApplicationContext.imgView.jTabbedPane1.getComponent(i)).getComponent(x));
                File instanceFile = new File(tempCanvas.imgpanel.getDicomFileUrl());
                String studyDir = instanceFile.getParent();
                for (Study study : MainScreen.studyList) {
                    if (study.getStudyInstanceUID().equalsIgnoreCase(tempCanvas.imgpanel.getStudyUID())) {
                        studyTobeDelete = study;
                        studyAnnotation.setStudyUID(study.getStudyInstanceUID());
                        if (ApplicationContext.databaseRef.getMultiframeStatus()) {
                            sepMultiframeModelSave(study, studyAnnotation);
                        } else {
                            defaultModelSave(study, studyAnnotation);
                        }
                    }
                }
                writeToFile(studyDir, studyAnnotation);
                //RemoveStudy.removeStudyFromStudylist(studyTobeDelete);
                removeStudyFromStudylist(studyTobeDelete);
                break;
            }
        }
    }

    /**
     * This Method used to update instanceAnnotation object based on the details of the instance
     * (multiframe instance are condisered as seperate series)
     * If Multiframe instance are considered as a seperate series so that it will concat the
     * annotation of this series  with the original series.
     * @param study
     * @param studyAnnotation
     */
    public void sepMultiframeModelSave(Study study, StudyAnnotation studyAnnotation) {
        for (Series series : study.getSeriesList()) {
            SeriesAnnotation seriesAnnotation = (SeriesAnnotation) studyAnnotation.getSeriesAnnotation().get(series.getSeriesInstanceUID());
            if (seriesAnnotation == null) {
                seriesAnnotation = new SeriesAnnotation();
                seriesAnnotation.setSeriesUID(series.getSeriesInstanceUID());
            }
            for (Instance instance : series.getImageList()) {
                InstanceAnnotation instanceAnnotation = null;
                if (!series.isMultiframe()) {
                    instanceAnnotation = new InstanceAnnotation(instance.getAnnotation());
                } else {
                    instanceAnnotation = new InstanceAnnotation(instance.getAnnotations());
                }
                instanceAnnotation.setInstanceUID(instance.getSop_iuid());
                instanceAnnotation.setMultiframe(series.isMultiframe());
                seriesAnnotation.getInstanceArray().put(instance.getSop_iuid(), instanceAnnotation);
            }
            studyAnnotation.getSeriesAnnotation().put(series.getSeriesInstanceUID(), seriesAnnotation);
        }
    }

    /**
     * This Method used to update instanceAnnotation object based on the details of the instance
     * (multiframe instance are not condisered as seperate series)
     * Single series level SeriesAnnotation object has been used store all the instance such as
     * single frame and multiframe instances.
     * multiframe instance will have annotations hashmap to hold all the frame level annotation states.
     *
     * @param study
     * @param studyAnnotation
     */
    public void defaultModelSave(Study study, StudyAnnotation studyAnnotation) {
        for (Series series : study.getSeriesList()) {
            SeriesAnnotation seriesAnnotation = new SeriesAnnotation();
            seriesAnnotation.setSeriesUID(series.getSeriesInstanceUID());
            for (Instance instance : series.getImageList()) {
                InstanceAnnotation tempAnnotation = null;
                if (seriesAnnotation != null && seriesAnnotation.getInstanceArray() != null) {
                    tempAnnotation = (InstanceAnnotation) seriesAnnotation.getInstanceArray().get(instance.getSop_iuid());
                }
                if (instance.isMultiframe()) {
                    if (tempAnnotation != null) {
                        tempAnnotation.getAnnotations().put(instance.getCurrentFrameNum(), instance.getAnnotation());
                    } else {
                        InstanceAnnotation instanceAnnotation = new InstanceAnnotation();  //When adding first image it has been considered as single frame instance so annotation will have the value.
                        instanceAnnotation.getAnnotations().put(instance.getCurrentFrameNum(), instance.getAnnotation());
                        seriesAnnotation.getInstanceArray().put(instance.getSop_iuid(), instanceAnnotation);
                    }
                } else {
                    InstanceAnnotation instanceAnnotation = new InstanceAnnotation(instance.getAnnotation());
                    seriesAnnotation.getInstanceArray().put(instance.getSop_iuid(), instanceAnnotation);
                }
            }
            studyAnnotation.getSeriesAnnotation().put(series.getSeriesInstanceUID(), seriesAnnotation);
        }
    }

    /**
     *
     * @param studyDir
     * @param studyAnnotation
     */
    private void writeToFile(String studyDir, StudyAnnotation studyAnnotation) {
        ObjectOutputStream oos = null;
        try {
            FileOutputStream fos = new FileOutputStream(new File(studyDir, "info.ser"));
            oos = new ObjectOutputStream(fos);
            oos.writeObject(studyAnnotation);
            oos.close();
        } catch (IOException ex) {
            Logger.getLogger(AnnotationDelegate.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(AnnotationDelegate.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                oos.close();
            } catch (IOException ex) {
                Logger.getLogger(AnnotationDelegate.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void storeAnnotationHook(int i) {
        for (int j = 0; j < ((JPanel) ApplicationContext.imgView.jTabbedPane1.getComponent(i)).getComponentCount(); j++) {
            try {
                if (((JPanel) ApplicationContext.imgView.jTabbedPane1.getComponent(i)).getComponent(j) instanceof LayeredCanvas) {
                    LayeredCanvas tempCanvas = ((LayeredCanvas) ((JPanel) ApplicationContext.imgView.jTabbedPane1.getComponent(i)).getComponent(j));
                    if (tempCanvas.imgpanel != null) {
                        tempCanvas.imgpanel.storeAnnotation();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void removeStudyFromStudylist(Study study) {
        if (MainScreen.selectedStudy.equalsIgnoreCase(study.getStudyInstanceUID())) {
            if (!LayoutManagerPanel.updateSeries) {
                for (Study tempStudy : MainScreen.studyList) {
                    synchronized (MainScreen.studyList) {
                        if (tempStudy.getStudyInstanceUID().equalsIgnoreCase(study.getStudyInstanceUID())) {
                            synchronized (tempStudy.getSeriesList()) {
                                Series notTobeDeleted = null;
                                for (Series series : tempStudy.getSeriesList()) {
                                    if (series.getSeriesInstanceUID().equalsIgnoreCase(MainScreen.selectedSeries)) {
                                        notTobeDeleted = series;
                                        break;
                                    }
                                }
                                tempStudy.getSeriesList().clear();
                                tempStudy.addSeries(notTobeDeleted);
                                notTobeDeleted = null;
                            }
                        }
                    }
                }
            } else {
                MainScreen.studyList.remove(study);
            }
        } else {
            MainScreen.studyList.remove(study);
        }
    }
}
