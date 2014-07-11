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
package in.raster.mayam.models.treetable;

import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.models.Series;
import in.raster.mayam.models.StudyModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Devishree
 * @version 2.0
 */
public class DataNode {

    String location;
    String patientid;
    String patientname;
    String dob;
    String accno;
    String studydate;
    String studydesc;
    String modalitiesInStudy;
    String instances = " ";
    //Required for other operations not visible in Treetable
    String studyInstanceUid;
    StudyModel studyDetails;
    ArrayList<Series> seriesList;
    // variables For  Series Information
    String seriesNumber;
    String seriesDescription;
    String modality;
    String seriesDate;
    String seriesTime;
    String bodyPart;
    String seriesRelatedInstances;
    boolean isSeries, isRoot;
    String header;
    List<DataNode> children;

    public DataNode(String loc, String pid, String pname, String dob, String acc, String sdate, String sdesc, String mod, String ins, String studyUid, String seriesno, String seriesdesc, String modality, String seriesdate, String seriestime, String bodypart, String seriesRelatedInstances, List<DataNode> children, StudyModel studyDetails, ArrayList<Series> seriesList, boolean isSeries, boolean isRoot) {
        this.patientid = pid;
        this.patientname = pname;
        this.dob = dob;
        this.accno = acc;
        this.studydate = sdate;
        this.studydesc = sdesc;
        this.modalitiesInStudy = mod;
        this.instances = ins;
        this.studyInstanceUid = studyUid;
        this.children = children;

        if (this.children == null) {
            this.children = Collections.emptyList();
        }

        this.seriesNumber = seriesno;
        this.seriesDescription = seriesdesc;
        this.modality = modality;
        this.seriesDate = seriesdate;
        this.seriesTime = seriestime;
        this.bodyPart = bodypart;
        this.seriesRelatedInstances = seriesRelatedInstances;

        this.studyDetails = studyDetails;
        this.seriesList = seriesList;
        this.isSeries = isSeries;
        this.isRoot = isRoot;
        if (this.seriesNumber.equals(ApplicationContext.currentBundle.getString("MainScreen.seriesNoColumn.text"))) {
            header = "true";
        } else {
            header = "false";
        }
    }

    public String getBodyPart() {
        return bodyPart;
    }

    public String getModality() {
        return modality;
    }

    public String getSeriesDate() {
        return seriesDate;
    }

    public String getSeriesDescription() {
        return seriesDescription;
    }

    public String getSeriesNumber() {
        return seriesNumber;
    }

    public String getSeriesTime() {
        return seriesTime;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return location;
    }

    public String getAccno() {
        return accno;
    }

    public List<DataNode> getChildren() {
        return children;
    }

    public void addChild(DataNode child) {
        children.add(child);
    }

    public String getDob() {
        return dob;
    }

    public String getInstances() {
        return instances;
    }

    public String getModalitiesInStudy() {
        return modalitiesInStudy;
    }

    public String getPatientid() {
        return patientid;
    }

    public String getPatientname() {
        return patientname;
    }

    public String getStudydate() {
        return studydate;
    }

    public String getStudydesc() {
        return studydesc;
    }

    public String getStudyInstanceUid() {
        return studyInstanceUid;
    }

    public String getSeriesRelatedInstances() {
        return seriesRelatedInstances;
    }

    public ArrayList<Series> getSeriesList() {
        return seriesList;
    }

    public StudyModel getStudyDetails() {
        return studyDetails;
    }

    public void setAccno(String accno) {
        this.accno = accno;
    }

    public void setBodyPart(String bodyPart) {
        this.bodyPart = bodyPart;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public void setInstances(String instances) {
        this.instances = instances;
    }

    public void setModalitiesInStudy(String modalitiesInStudy) {
        this.modalitiesInStudy = modalitiesInStudy;
    }

    public void setPatientid(String patientid) {
        this.patientid = patientid;
    }

    public void setPatientname(String patientname) {
        this.patientname = patientname;
    }

    public void setSeriesNumber(String seriesNumber) {
        this.seriesNumber = seriesNumber;
    }

    public void setStudyDetails(StudyModel studyDetails) {
        this.studyDetails = studyDetails;
    }

    public void setStudyInstanceUid(String studyInstanceUid) {
        this.studyInstanceUid = studyInstanceUid;
    }

    public void setStudydate(String studydate) {
        this.studydate = studydate;
    }

    public void setStudydesc(String studydesc) {
        this.studydesc = studydesc;
    }

    public boolean isIsSeries() {
        return isSeries;
    }

    public void setIsSeries(boolean isSeries) {
        this.isSeries = isSeries;
    }

    public boolean isIsRoot() {
        return isRoot;
    }

    public void setIsRoot(boolean isRoot) {
        this.isRoot = isRoot;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }
}