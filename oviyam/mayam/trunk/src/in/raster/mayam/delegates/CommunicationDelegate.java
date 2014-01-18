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
import in.raster.mayam.models.*;
import in.raster.mayam.models.treetable.DataNode;
import in.raster.mayam.param.QueryParam;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.JOptionPane;
import org.dcm4che.data.Dataset;
import org.dcm4che.util.DcmURL;

/**
 *
 * @author Devishree
 * @version 2.0
 */
public class CommunicationDelegate {

    QueryParam queryParam;
    ArrayList<StudySeriesMatch> studySeriesMatchs = new ArrayList<StudySeriesMatch>();

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

    /*
     * Queries from the server and construts tree table and stores the Query
     * Information
     */
    public void doQuery(String selectedButton) {
        int doQuery = 0;
        DcmURL url = constructURL(ApplicationContext.currentServer);
        int totalStudiesFound;
        studySeriesMatchs = new ArrayList<StudySeriesMatch>();
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
                if (qs.getDatasetVector().isEmpty()) {
                    ApplicationContext.mainScreenObj.setStudiesFound(ApplicationContext.currentBundle.getString("MainScreen.studiesFoundLabel.text") + ": 0");
                }
                totalStudiesFound = qs.getDatasetVector().size();
                ApplicationContext.mainScreenObj.setStudiesFound(ApplicationContext.currentBundle.getString("MainScreen.studiesFoundLabel.text") + totalStudiesFound);
                ArrayList<StudyModel> studyList = new ArrayList<StudyModel>();
                for (int datasetcount = 0; datasetcount < totalStudiesFound; datasetcount++) {
                    Dataset dataSet = (Dataset) qs.getDatasetVector().elementAt(datasetcount);
                    StudyModel studyModel = new StudyModel(dataSet);
                    ArrayList<Series> seriesList = constructSeriesList(studyModel, url);
                    StudySeriesMatch studySeriesMatch = new StudySeriesMatch(studyModel.getStudyUID(), seriesList);
                    studySeriesMatchs.add(studySeriesMatch);
                    studyList.add(studyModel);
                }
                //Important to display the total studies found and to set the search button selected which used to query(When tab changes)
                for (int i = 0; i < ApplicationContext.queryInformations.size(); i++) { //The previous information of the same server should be removed
                    if (ApplicationContext.queryInformations.get(i).getServerName().equals(ApplicationContext.currentServer)) {
                        ApplicationContext.queryInformations.remove(i);
                    }
                }
                QueryInformation serverInfo = new QueryInformation(ApplicationContext.currentServer, selectedButton, totalStudiesFound);
                ApplicationContext.queryInformations.add(serverInfo);
                DataNode root = constructTreeTableData(studySeriesMatchs, studyList, "", "", "", "", "", "", "");
                ApplicationContext.mainScreenObj.setTreeTableModel(root);
            }
        }
    }

    /*
     * To construct the series list by querying from server
     */
    private ArrayList<Series> constructSeriesList(StudyModel studyModel, DcmURL dcmurl) {
        ArrayList<Series> seriesList = new ArrayList<Series>();
        QuerySeriesService seriesService = new QuerySeriesService();
        seriesService.callFindWithQuery(studyModel.getPatientId(), studyModel.getStudyUID(), dcmurl);

        for (int i = 0; i < seriesService.getDatasetVector().size(); i++) {
            Dataset dataset = (Dataset) seriesService.getDatasetVector().elementAt(i);
            Series series = new Series(dataset);
            seriesList.add(series);
        }

        Collections.sort(seriesList, new Comparator() {
            @Override
            public int compare(Object t, Object t1) {
                Series i1 = (Series) t;
                Series i2 = (Series) t1;

                if (!i1.getSeriesNumber().equals("") && !i2.getSeriesNumber().equals("")) {
                    if (new Integer(i1.getSeriesNumber()) == null) {
                        return (-1);
                    } else if (new Integer(i2.getSeriesNumber()) == null) {
                        return (1);
                    } else {
                        int a = new Integer(i1.getSeriesNumber());
                        int b = new Integer(i2.getSeriesNumber());
                        int temp = (a == b ? 0 : (a > b ? 1 : -1));
                        return temp;
                    }
                } else {
                    return 0;
                }
            }
        });
        return seriesList;
    }

    private DataNode constructSeriesHeader() {
        DataNode header = new DataNode("", "", "", "", "", "", "", "", "", "", ApplicationContext.currentBundle.getString("MainScreen.seriesNoColumn.text"), ApplicationContext.currentBundle.getString("MainScreen.seriesDescColumn.text"), ApplicationContext.currentBundle.getString("MainScreen.seriesDateColumn.text"), ApplicationContext.currentBundle.getString("MainScreen.seriesTimeColumn.text"), ApplicationContext.currentBundle.getString("MainScreen.bodyPartColumn.text"), ApplicationContext.currentBundle.getString("MainScreen.modalityColumn.text"), ApplicationContext.currentBundle.getString("MainScreen.imagesColumn.text"), null, null, null, true, false);
        return header;
    }

    public DataNode constructTreeTableData(ArrayList<StudySeriesMatch> studySeriesMatches, ArrayList<StudyModel> studyList, String pid, String pName, String dob, String accNo, String studyDate, String studyDesc, String modality) {
        ArrayList<DataNode> rootNodes = new ArrayList<DataNode>();
        DataNode seriesHeader = constructSeriesHeader();
        DataNode root = null;
        for (int i = 0; i < studyList.size(); i++) {
            for (int j = 0; j < studySeriesMatches.size(); j++) {
                if (studyList.get(i).getStudyUID().equals(studySeriesMatches.get(j).getSuid())) {
                    ArrayList<DataNode> seriesNodes = new ArrayList<DataNode>();
                    seriesNodes.add(seriesHeader);
                    ArrayList<Series> seriesList = studySeriesMatches.get(j).getSeriesList();
                    for (int k = 0; k < seriesList.size(); k++) {
                        seriesNodes.add(new DataNode("", "", "", "", "", "", "", "", "", "", seriesList.get(k).getSeriesNumber(), seriesList.get(k).getSeriesDesc(), seriesList.get(k).getModality(), seriesList.get(k).getSeriesDate(), seriesList.get(k).getSeriesTime(), seriesList.get(k).getBodyPartExamined(), String.valueOf(seriesList.get(k).getSeriesRelatedInstance()), null, null, null, true, false));
                    }
                    rootNodes.add(new DataNode("", studyList.get(i).getPatientId(), studyList.get(i).getPatientName(), studyList.get(i).getDob(), studyList.get(i).getAccessionNo(), studyList.get(i).getStudyDate(), studyList.get(i).getStudyDescription(), studyList.get(i).getModalitiesInStudy(), studyList.get(i).getStudyLevelInstances(), studyList.get(i).getStudyUID(), "", "", "", "", "", "", "", seriesNodes, ((StudyModel) studyList.get(i)), seriesList, false, false));
                    root = new DataNode("", pid, pName, dob, accNo, studyDate, studyDesc, modality, "", "", "", "", "", "", "", "", "", rootNodes, null, null, false, true);
                }
            }
        }
        if (root == null) {
            root = new DataNode("", pid, pName, dob, accNo, studyDate, studyDesc, modality, "", "", "", "", "", "", "", "", "", rootNodes, null, null, false, true);
        }
        return root;
    }
}