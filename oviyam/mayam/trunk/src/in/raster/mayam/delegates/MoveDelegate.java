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
import in.raster.mayam.models.Series;
import in.raster.mayam.util.core.MoveScu;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Devishree
 * @version 2.0
 */
public class MoveDelegate extends Thread {

    String moveArg[];
    String patientId, studyUid, modalitiesInStudy;
    int totalInstances;
    ArrayList<Series> seriesList;
    String[] patientInfo;

    public MoveDelegate(String moveArg[], String patientId, String studyInstanceUid, String modalitiesInStudy, ArrayList<Series> seriesList, int totalInstances, String[] patientInfo) {
        this.moveArg = moveArg;
        this.patientId = patientId;
        studyUid = studyInstanceUid;
        this.modalitiesInStudy = modalitiesInStudy;
        this.seriesList = seriesList;
        this.totalInstances = totalInstances;
        this.patientInfo = patientInfo;
        this.start();
    }

    @Override
    public void run() {
        move();
    }

    private void move() {
        try {
            MoveScu.main(moveArg); //Retrieves the study
            //Database Updations after completing the whole study            
            ApplicationContext.databaseRef.update("study", "NoOfSeries", seriesList.size(), "StudyInstanceUID", studyUid);
            ApplicationContext.databaseRef.update("study", "NoOfInstances", ApplicationContext.databaseRef.getTotalInstances(studyUid), "StudyInstanceUID", studyUid);
            for (int i = 0; i < seriesList.size(); i++) {
                ApplicationContext.databaseRef.update("series", "NoOfSeriesRelatedInstances", seriesList.get(i).getSeriesRelatedInstance(), "SeriesInstanceUID", seriesList.get(i).getSeriesInstanceUID());
                ConstructThumbnails constructThumbnails = new ConstructThumbnails(studyUid, seriesList.get(i).getSeriesInstanceUID());
                ApplicationContext.mainScreenObj.increaseProgressValue();
            }
            ApplicationContext.databaseRef.update("study", "DownloadStatus", true, "StudyInstanceUID", studyUid);
            String filePath = ApplicationContext.databaseRef.getFirstInstanceLocation(studyUid, seriesList.get(0).getSeriesInstanceUID());
            if (filePath != null) {
                ApplicationContext.openImageView(filePath, studyUid, patientInfo,0);
            }
            //To check wheather all studies completed
            boolean studiesPending = ApplicationContext.databaseRef.isDownloadPending();
            if (!studiesPending) {
                ApplicationContext.mainScreenObj.hideProgressBar();
            }
        } catch (Exception ex) {
            Logger.getLogger(MoveDelegate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
