
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
import in.raster.mayam.form.dialog.ExportDialog;
import in.raster.mayam.form.display.Display;
import in.raster.mayam.form.ExportingProgress;
import in.raster.mayam.form.dialog.ExportLocationChooser;
import java.io.File;

/**
 *
 * @author  BabuHussain
 * @version 0.5
 *
 */
public class ExportingDelegate extends Thread {

    private File openedFile;
    private boolean isSeriesOrInstanceLevel;
    private String studyUID;

    public ExportingDelegate() {
    }

    public ExportingDelegate(File openedFile, String studyUID, boolean isSeriesOrInstanceLevel) {
        this.openedFile = openedFile;
        this.isSeriesOrInstanceLevel = isSeriesOrInstanceLevel;
        this.studyUID = studyUID;
        this.start();
    }

    public void run() {

        ExportingProgress exportingProgress = new ExportingProgress();
        Display.alignScreen(exportingProgress);
        exportingProgress.setVisible(true);
        exportingProgress.updateBar(0);
        JpegConvertorDelegate jpegDelegate = new JpegConvertorDelegate();
        ExportToDcmDelegate exportToDcmDelegate = new ExportToDcmDelegate();
        process(exportToDcmDelegate, jpegDelegate);
        exportingProgress.updateBar(100);
        exportingProgress.setVisible(false);
    }

    private void process(ExportToDcmDelegate exportToDcmDelegate, JpegConvertorDelegate jpegDelegate) {
        if (isSeriesOrInstanceLevel) {
            File patientNameFile = new File(openedFile, ApplicationContext.imgPanel.getTextOverlayParam().getPatientName());
            if (!patientNameFile.exists()) {
                patientNameFile.mkdir();
            }
            if (ExportDialog.seriesOfImage) {//series of image
                exportSeriesOfImage(patientNameFile, exportToDcmDelegate, jpegDelegate);
            } else {//Single image
                exportInstanceOfImage(patientNameFile, exportToDcmDelegate, jpegDelegate);
            }
        } else {//study convert
            exportStudy(openedFile, exportToDcmDelegate, jpegDelegate);
        }
    }

    private void exportSeriesOfImage(File patientNameFile, ExportToDcmDelegate exportToDcmDelegate, JpegConvertorDelegate jpegDelegate) {
        if (!ApplicationContext.databaseRef.getMultiframeStatus()) {
            if (ExportDialog.conversionFormatDcm) {
                exportToDcmDelegate.seriesExportAsDicom(ApplicationContext.imgPanel.getStudyUID(), ApplicationContext.imgPanel.getSeriesUID(), patientNameFile.getAbsolutePath());
            } else {
                jpegDelegate.seriesLevelConvertor(ApplicationContext.imgPanel.getStudyUID(), ApplicationContext.imgPanel.getSeriesUID(), patientNameFile.getAbsolutePath(), ApplicationContext.imgPanel.getColorModel());
            }
        } else {
            if (ExportDialog.conversionFormatDcm) {
                exportToDcmDelegate.seriesExportAsDicom(ApplicationContext.imgPanel.getStudyUID(), ApplicationContext.imgPanel.getSeriesUID(), ApplicationContext.imgPanel.isMulitiFrame(), ApplicationContext.imgPanel.getInstanceUID(), patientNameFile.getAbsolutePath());
            } else {
                jpegDelegate.seriesLevelConvertor(ApplicationContext.imgPanel.getStudyUID(), ApplicationContext.imgPanel.getSeriesUID(), ApplicationContext.imgPanel.isMulitiFrame(), ApplicationContext.imgPanel.getInstanceUID(), patientNameFile.getAbsolutePath(), ApplicationContext.imgPanel.getColorModel());
            }

        }
    }

    private void exportInstanceOfImage(File patientNameFile, ExportToDcmDelegate exportToDcmDelegate, JpegConvertorDelegate jpegDelegate) {
        if (ExportDialog.conversionFormatDcm) {
            exportToDcmDelegate.instanceExportAsDicom(ApplicationContext.imgPanel.getDicomFileUrl(), patientNameFile.getAbsolutePath(),ApplicationContext.imgPanel.isIsEncapsulatedDocument());
        } else {
            jpegDelegate.instanceConvertor(patientNameFile.getAbsolutePath() + File.separator + ApplicationContext.imgPanel.getInstanceUID(), ApplicationContext.imgPanel.getCurrentbufferedimage());
        }
    }

    private void exportStudy(File patientNameFile, ExportToDcmDelegate exportToDcmDelegate, JpegConvertorDelegate jpegDelegate) {
        if (ExportLocationChooser.conversionFormatDcm) {
            exportToDcmDelegate.studyExportAsDicom(this.studyUID, openedFile.getAbsolutePath());
        } else {
            jpegDelegate.studyLevelConvertor(this.studyUID, openedFile.getAbsolutePath());
        }
    }
}
