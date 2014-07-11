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

import de.iftm.dcm4che.services.CDimseService;
import de.iftm.dcm4che.services.ConfigProperties;
import de.iftm.dcm4che.services.StorageService;
import in.raster.mayam.context.ApplicationContext;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.Vector;
import org.dcm4che.util.DcmURL;

/**
 *
 * @author BabuHussain
 * @version 0.7
 *
 */
public class QueryInstanceService {

    private int numberofInstances;
    private Vector datasetVector;

    public QueryInstanceService() {
        CDimseService.DEFAULT_CALLING_AET = ApplicationContext.listenerDetails[0];
    }

    /**
     * Queries(cFIND) the Patient/Study informations from the
     * machine(dcmProtocol://aeTitle@hostName:port).
     *
     * @param searchPatientID
     * @param SearchPatientName
     * @param searchDob
     * @param searchtoday
     * @param searchsterday
     * @param searchModality
     */
    @SuppressWarnings("unchecked")
    public void callFindWithQuery(String patientID, String studyInstanceUID, String seriesInstanceUID, DcmURL url) {

        ConfigProperties cfgCDimseService;
        boolean isOpen;
        // Vector object to keep the queried Datasets.

        CDimseService cDimseService;

        // Load configuration properties of the server
        try {
            cfgCDimseService = new ConfigProperties(StorageService.class.getResource("/resources/Image.cfg"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.out.println("Unable to create ConfigProperties instance");
            return;
        }

        /*
         * Setting filter values for query such as patientId, patientName etc.
         */
        try {
            cfgCDimseService.put("key.PatientID", patientID);
            cfgCDimseService.put("key.StudyInstanceUID", studyInstanceUID);
            cfgCDimseService.put("key.SeriesInstanceUID", seriesInstanceUID);
        } catch (Exception e) {
            System.out.println("Unable to set Key values for query");
        }

        try {
            cDimseService = new CDimseService(cfgCDimseService, url);

        } catch (ParseException e) {
            System.out.println("Unable to create CDimseService instance");
            return;

        }

        // Open association
        try {
            isOpen = cDimseService.aASSOCIATE();
            if (!isOpen) {
                return;
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return;
        } catch (GeneralSecurityException e) {
            System.out.println(e.getMessage());
            return;
        }
        // cFIND (Queries for datasets).
        try {
            datasetVector = cDimseService.cFIND();


        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }

        numberofInstances = datasetVector.size();
        /*
         * Gets the Dataset form the datasetVector and adds it to the
         * patientList and adds the studies to the corresponding patientList.
         */

        try {
            cDimseService.aRELEASE(true);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }

    }

    public Vector getDatasetVector() {
        return datasetVector;
    }

    public void setDatasetVector(Vector datasetVector) {
        this.datasetVector = datasetVector;
    }
}