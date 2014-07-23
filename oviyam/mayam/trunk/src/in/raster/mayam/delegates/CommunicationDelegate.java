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
import in.raster.mayam.models.*;
import in.raster.mayam.models.treetable.SeriesNode;
import in.raster.mayam.models.treetable.StudyNode;
import in.raster.mayam.param.QueryParam;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.util.DcmURL;

/**
 *
 * @author Devishree
 * @version 2.0
 */
public class CommunicationDelegate {

    QueryParam queryParam;

    /*
     * Verifies the server was alive or not
     */
    public boolean verifyServer(DcmURL url) {
        EchoDelegate echoDelegate = new EchoDelegate();
        echoDelegate.checkEcho(url);
        try {
            if (echoDelegate.getStatus().trim().equalsIgnoreCase("EchoSuccess")) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            ApplicationContext.logger.log(Level.WARNING, "Communication Delegate - Unable to verify server");
            return false;
        }
    }

    public DcmURL constructURL(String aetitle, String host, int port) {
        DcmURL url = new DcmURL("dicom://" + aetitle + "@" + host + ":" + port);
        return url;
    }

    public DcmURL constructURL(String serverName) {
        ServerModel serverDetails = ApplicationContext.databaseRef.getServerDetails(serverName);
        DcmURL url = new DcmURL("dicom://" + serverDetails.getAeTitle() + "@" + serverDetails.getHostName() + ":" + serverDetails.getPort());
        return url;
    }

    /*
     * To set the Query parameters using the Quick Search Buttons
     */
    public void setQueryParam(String modality, String studydate, String studytime) {
        queryParam = new QueryParam();
        queryParam.setModality(modality);
        queryParam.setSearchDate(studydate);
        queryParam.setSearchTime(studytime);
    }

    /*
     * To set the Query parameters using the SearchFilter Form
     */
    public void setQueryParameters(String patientName, String patientId, String accno, String dob, String modality, String searchdate, String studyDescription, String referringPhysicianName) {
        queryParam = new QueryParam();
        queryParam.setPatientName(patientName);
        queryParam.setPatientId(patientId);
        queryParam.setAccessionNo(accno);
        queryParam.setBirthDate(dob);
        queryParam.setSearchDate(searchdate);
        queryParam.setModality(modality);
        queryParam.setStudyDescription(studyDescription);
        queryParam.setReferringPhysicianName(referringPhysicianName);
    }

    public void query(String selectedButton) {
        int doQuery = 0;
        DcmURL url = constructURL(ApplicationContext.currentServer);
        int totalStudiesFound;

        ArrayList<StudyNode> rootNodes = new ArrayList<StudyNode>();
        SeriesNode seriesHeader = new SeriesNode(null);

        boolean serverStatus = verifyServer(url);
        if (!serverStatus) {
            JOptionPane.showOptionDialog(ApplicationContext.mainScreenObj, ApplicationContext.currentBundle.getString("MainScreen.echoFailiure.text") + " '" + ApplicationContext.currentServer + "'", ApplicationContext.currentBundle.getString("ErrorTitles.text"), JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[]{ApplicationContext.currentBundle.getString("OkButtons.text")}, "default");
        } else {
            if (queryParam.getPatientId().equals("") && queryParam.getPatientName().equals("") && queryParam.getBirthDate().equals("") && queryParam.getSearchDate().equals("") && queryParam.getModality().equals("") && queryParam.getSearchTime().equals("") && queryParam.getAccessionNo().equals("") && queryParam.getStudyDescription().equals("") && queryParam.getReferringPhysicianName().equals("")) {
                doQuery = JOptionPane.showOptionDialog(ApplicationContext.mainScreenObj, ApplicationContext.currentBundle.getString("MainScreen.noSearchCriteriaFound.text"), "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{ApplicationContext.currentBundle.getString("YesButtons.text"), ApplicationContext.currentBundle.getString("NoButtons.text")}, "default");
            }
            if (doQuery == 0) {
                QueryService qs = new QueryService();
                qs.callFindWithQuery(queryParam.getPatientId(), queryParam.getPatientName(), queryParam.getBirthDate(), queryParam.getSearchDate(), queryParam.getModality(), queryParam.getSearchTime(), queryParam.getAccessionNo(), queryParam.getStudyDescription(), queryParam.getReferringPhysicianName(), null, url);
                Vector study_DS = qs.getDatasetVector();

                totalStudiesFound = study_DS.size();
                ApplicationContext.mainScreenObj.setStudiesFound(ApplicationContext.currentBundle.getString("MainScreen.studiesFoundLabel.text") + study_DS.size());

                for (int study_Iter = 0; study_Iter < study_DS.size(); study_Iter++) {
                    Dataset study_Data = (Dataset) study_DS.elementAt(study_Iter);
                    ArrayList<SeriesNode> seriesNodes = new ArrayList<SeriesNode>();
                    QuerySeriesService querySeriesService = new QuerySeriesService();
                    querySeriesService.callFindWithQuery(study_Data.getString(Tags.PatientID), study_Data.getString(Tags.StudyInstanceUID), url);
                    Vector<Dataset> series_DS = querySeriesService.getDatasetVector();

                    sortSeries(series_DS);
                    seriesNodes.add(seriesHeader);
                    for (int ser_Iter = 0; ser_Iter < series_DS.size(); ser_Iter++) {
                        seriesNodes.add(new SeriesNode(series_DS.elementAt(ser_Iter)));
                    }//end of series iteration
                    rootNodes.add(new StudyNode(study_Data, seriesNodes));
                }//end of study iteration
                ApplicationContext.mainScreenObj.constructTreeTable(new StudyNode(rootNodes));

                //Important to display the total studies found and to set the search button selected which used to query(When tab changes)
                for (int i = 0; i < ApplicationContext.queryInformations.size(); i++) { //The previous information of the same server should be removed
                    if (ApplicationContext.queryInformations.get(i).getServerName().equals(ApplicationContext.currentServer)) {
                        ApplicationContext.queryInformations.remove(i);
                    }
                }
                QueryInformation serverInfo = new QueryInformation(ApplicationContext.currentServer, selectedButton, totalStudiesFound);
                ApplicationContext.queryInformations.add(serverInfo);
            } //end of query
        }
    }

    public void sortSeries(Vector<Dataset> datasets) {
        Collections.sort(datasets, new Comparator<Dataset>() {
            @Override
            public int compare(Dataset o1, Dataset o2) {
                try {
                    final int seriesNo1 = o1.getInteger(Tags.SeriesNumber);
                    final int seriesNo2 = o2.getInteger(Tags.SeriesNumber);
                    return (seriesNo1 == seriesNo2 ? 0 : (seriesNo1 > seriesNo2 ? 1 : -1));
                } catch (Exception ex) {
                    System.err.println(ex.getMessage());
                }
                return 0;
            }
        });
    }
}