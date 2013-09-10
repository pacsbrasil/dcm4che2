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
import in.raster.mayam.form.MainScreen;
import in.raster.mayam.model.Instance;
import in.raster.mayam.model.Series;
import in.raster.mayam.model.Study;
import java.io.File;

/**
 *
 * @author  BabuHussain
 * @version 0.5
 *
 */
public class SeriesChooserDelegate extends Thread {

    private String studyUID;
    private String seriesUID;
    private boolean multiframe;
    private String instanceUID;
    private LayeredCanvas canvas;

    public SeriesChooserDelegate() {
    }

    public SeriesChooserDelegate(String studyUID, String seriesUID, LayeredCanvas canvas) {
        this.studyUID = studyUID;
        this.seriesUID = seriesUID;
        this.canvas = canvas;
        this.start();
    }

    public SeriesChooserDelegate(String studyUID, String seriesUID, boolean multiframe, String instanceUID, LayeredCanvas canvas) {
        this.studyUID = studyUID;
        this.seriesUID = seriesUID;
        this.multiframe = multiframe;
        this.instanceUID = instanceUID;
        this.canvas = canvas;
        this.start();
    }

    /**
     * This routine used to change the series in the tile.
     */
    private void changeSeries() {
        if (ApplicationContext.databaseRef.getMultiframeStatus()) {
            changeSeries_SepMulti();
        } else {
            changeSeries_Normal();
            
        }
        canvas.revalidate();
        canvas.repaint();
    }

    public void changeSeries_Normal() {
        for (Study study : MainScreen.studyList) {
            if (study.getStudyInstanceUID().equalsIgnoreCase(studyUID)) {
                for (Series series : study.getSeriesList()) {
                    if (series.getSeriesInstanceUID().equalsIgnoreCase(seriesUID)) {
                        for (Instance instance : series.getImageList()) {
                            File file = new File(ApplicationContext.getAppDirectory() + File.separator + instance.getFilepath());
                            if (file.exists()) {
                                canvas.createSubComponents(ApplicationContext.getAppDirectory() + File.separator + instance.getFilepath());
                                canvas.annotationPanel.setAnnotation(instance.getAnnotation());
                            } else {
                                canvas.createSubComponents(instance.getFilepath());
                                canvas.annotationPanel.setAnnotation(instance.getAnnotation());
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * This routine used to change the series in the tile.
     */
    private void changeSeries_SepMulti() {
        for (Study study : MainScreen.studyList) {
            if (study.getStudyInstanceUID().equalsIgnoreCase(studyUID)) {
                for (Series series : study.getSeriesList()) {
                    if (!this.multiframe) {
                        multiframeProcess(series);
                    } else {
                        stillImageProcess(series);
                    }
                }//for loop series closed.
            }
            if(canvas.getCanvas()!=null)
            canvas.getCanvas().setSelection();
        }
    }

    public void run() {
        changeSeries();
    }

    private void multiframeProcess(Series series) {
        if (!series.isMultiframe() && series.getSeriesInstanceUID().equalsIgnoreCase(seriesUID)) {
            for (Instance instance : series.getImageList()) {
                File file = new File(ApplicationContext.getAppDirectory() + File.separator + instance.getFilepath());
                if (file.exists()) {
                    canvas.createSubComponents(ApplicationContext.getAppDirectory() + File.separator + instance.getFilepath());
                    canvas.annotationPanel.setAnnotation(instance.getAnnotation());
                } else {
                    canvas.createSubComponents(instance.getFilepath());
                    canvas.annotationPanel.setAnnotation(instance.getAnnotation());
                }
                break;
            }
        }
    }

    private void stillImageProcess(Series series) {
        if (series.isMultiframe() && series.getSeriesInstanceUID().equalsIgnoreCase(seriesUID) && series.getInstanceUID().equalsIgnoreCase(instanceUID)) {
            for (Instance instance : series.getImageList()) {
                File file = new File(ApplicationContext.getAppDirectory()+ File.separator + instance.getFilepath());
                if (file.exists()) {
                    canvas.createSubComponents(ApplicationContext.getAppDirectory() + File.separator + instance.getFilepath());
                    if(instance.getAnnotations()!=null&&instance.getAnnotations().get(0)!=null)
                    canvas.annotationPanel.setAnnotation(instance.getAnnotations().get(0));
                } else {
                    canvas.createSubComponents(instance.getFilepath());
                    if(instance.getAnnotations()!=null&&instance.getAnnotations().get(0)!=null)
                    canvas.annotationPanel.setAnnotation(instance.getAnnotations().get(0));
                }
                break;
            }
        }
    }
}
