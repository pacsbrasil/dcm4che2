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
 * Portions created by the Initial Developer are Copyright (C) 2014
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
import in.raster.mayam.util.core.DcmQR;
import in.raster.mayam.util.core.MoveScu;
import java.util.logging.Level;

/**
 *
 * @author Devishree
 * @version 2.1
 */
public class SeriesRetriever implements Runnable {

    String moveArg[] = null;
    String studyUid = null, seriesUid = null;
    int totalInstances;
    boolean isFirstSeries = false, constructPreview = false, useMoveRQ = false;

    public SeriesRetriever(String[] moveArg, String studyUid, String seriesUID, int totalInstances, boolean isFirstSeries, boolean constructPreview, boolean useMoveRQ) {
        this.moveArg = moveArg;
        this.studyUid = studyUid;
        this.seriesUid = seriesUID;
        this.totalInstances = totalInstances;
        this.isFirstSeries = isFirstSeries;
        this.constructPreview = constructPreview;
        this.useMoveRQ = useMoveRQ;
    }

    @Override
    public void run() {
        if (useMoveRQ) {
            try {
                MoveScu.main(moveArg);
            } catch (Exception ex) {
                ApplicationContext.logger.log(Level.SEVERE, null, ex);
            }
        } else {
            DcmQR.main(moveArg);
        }
        showSeries();
    }

    private void showSeries() {
        if (totalInstances == ApplicationContext.databaseRef.getStudyLevelInstances(studyUid)) {
            ApplicationContext.studyRetirivalCompleted(studyUid);
        }
        if (isFirstSeries) {
            ApplicationContext.createCanvas(ApplicationContext.databaseRef.getFirstInstanceLocation(studyUid, seriesUid), studyUid, 0);
        }
        if (constructPreview) {
            new ConstructThumbnails(studyUid, seriesUid, false).start();
        }
        ApplicationContext.databaseRef.update("series", "NoOfSeriesRelatedInstances", ApplicationContext.databaseRef.getSeriesLevelInstance(studyUid, seriesUid), "SeriesInstanceUID", seriesUid);
    }
}