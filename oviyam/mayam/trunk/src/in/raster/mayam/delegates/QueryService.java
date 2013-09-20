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

import de.iftm.dcm4che.services.CDimseService;
import de.iftm.dcm4che.services.ConfigProperties;
import de.iftm.dcm4che.services.StorageService;
import in.raster.mayam.context.ApplicationContext;
import java.io.IOException;
import java.net.ConnectException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dcm4che.util.DcmURL;

/**
 *
 * @author BabuHussain
 * @version 0.7
 *
 */
public class QueryService {

    private int numberOfStudies;
    private Vector datasetVector;

    public QueryService() {
        CDimseService.DEFAULT_CALLING_AET = ApplicationContext.listenerDetails[0];
    }

    public void callFindWithQuery(String searchPatientID, String SearchPatientName, String searchDob, String searchStudyDate, String searchModality, String searchTime, String searchAccNo, String studyDescription, String referringPhysicianName, String studyUID, DcmURL url) {
        try {
            ConfigProperties cfgDimseService = null;
            boolean isOpen;
            CDimseService cDimseService = null;
            try {
                cfgDimseService = new ConfigProperties(StorageService.class.getResource("/resources/CDimseService.cfg"));
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
                System.out.println("Unable to create Config Properties Instance");
            }

            try {
                if (searchPatientID != null) {
                    cfgDimseService.put("key.PatientID", searchPatientID);
                }

                if (SearchPatientName != null) {
                    cfgDimseService.put("key.PatientName", SearchPatientName + "*");
                }

                if (searchStudyDate != null && searchStudyDate.length() > 0) {
                    cfgDimseService.put("key.StudyDate", searchStudyDate);
                }

                if (searchDob != null && searchDob.length() > 0) {
                    cfgDimseService.put("key.PatientBirthDate", searchDob.replace("/", ""));
                }

                if (searchAccNo != null && searchAccNo.length() > 0) {
                    cfgDimseService.put("key.AccessionNumber", searchAccNo);
                }

                if (searchModality != null) {
                    searchModality = searchModality.toUpperCase();
                    cfgDimseService.put("key.ModalitiesInStudy", searchModality);
                }

                if (searchTime != null && searchTime.length() > 0) {
                    cfgDimseService.put("key.StudyTime", searchTime);
                }

                if (studyDescription != null && studyDescription.length() > 0) {
                    cfgDimseService.put("key.StudyDescription", studyDescription + "*");
                }

                if (referringPhysicianName != null && referringPhysicianName.length() > 0) {
                    cfgDimseService.put("key.ReferringPhysicianName", referringPhysicianName + "*");
                }

                if (studyUID != null) {
                    cfgDimseService.put("key.StudyInstanceUID", studyUID);
                }
            } catch (Exception e) {
                System.out.println("Unable to set key values for query");
            }
            try {
                cDimseService = new CDimseService(cfgDimseService, url);
            } catch (ParseException ex) {
                System.out.println("Unable to create CDimseService instance");
            }

            isOpen = cDimseService.aASSOCIATE();
            if (!isOpen) {
                return;
            }
            datasetVector = cDimseService.cFIND();
            numberOfStudies = datasetVector.size();
            cDimseService.aRELEASE(true);
        } catch (InterruptedException ex) {
            Logger.getLogger(QueryService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConnectException ex) {
            Logger.getLogger(QueryService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(QueryService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GeneralSecurityException ex) {
            Logger.getLogger(QueryService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Vector getDatasetVector() {
        return datasetVector;
    }

    public void setDatasetVector(Vector datasetvector) {
        this.datasetVector = datasetvector;
    }
}
