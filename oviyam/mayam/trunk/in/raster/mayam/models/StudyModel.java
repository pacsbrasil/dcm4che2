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
package in.raster.mayam.models;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

/**
 *
 * @author Devishree
 * @version 2.0
 */
public class StudyModel {

    private String patientId;
    private String patientName;
    private String dob;
    private String accessionNo;
    private String studyDate;
    private String studyTime;
    private String studyDescription;
    private String modalitiesInStudy;
    private String studyUID;
    private String[] modalities;
    private String studyLevelInstances;
    private String numberOfSeries;
    private String modality = "";

    public StudyModel() {
    }

    public StudyModel(String patientId, String patientName, String dob, String accessionNo, String studyDate, String studyTime, String studyDescription, String modality, String numberOfSeries, String studyLevelInstances, String studyInstaceUid) {
        this.patientId = patientId;
        this.patientName = patientName;
        this.dob = dob;
        this.accessionNo = accessionNo;
        this.studyDate = studyDate;
        this.studyTime = studyTime;
        this.studyDescription = studyDescription;
        this.modality = modality;
        this.modalitiesInStudy = modality;
        this.numberOfSeries = numberOfSeries;
        this.studyLevelInstances = studyLevelInstances;
        this.studyUID = studyInstaceUid;
    }

    public StudyModel(Dataset dataSet) {
        studyUID = dataSet.getString(Tags.StudyInstanceUID);
        studyDescription = dataSet.getString(Tags.StudyDescription) != null ? dataSet.getString(Tags.StudyDescription) : "unknown";
        studyDate = dataSet.getString(Tags.StudyDate) != null ? dataSet.getString(Tags.StudyDate) : "unknown";
        studyTime = dataSet.getString(Tags.StudyTime) != null ? dataSet.getString(Tags.StudyTime) : "unknown";
        modalities = dataSet.getStrings(Tags.ModalitiesInStudy) != null ? dataSet.getStrings(Tags.ModalitiesInStudy) : null;
        if (modalities != null) {
            for (int i = 0; i < modalities.length; i++) {
                if (i == 0) {
                    modalitiesInStudy = modalities[i];
                } else {
                    modalitiesInStudy += "\\" + modalities[i];
                }
            }
        }
        // modalitiesInStudy = dataSet.getString(Tags.ModalitiesInStudy) != null ? dataSet.getString(Tags.ModalitiesInStudy) : "";
        patientName = dataSet.getString(Tags.PatientName) != null ? dataSet.getString(Tags.PatientName) : "unknown";
        patientId = dataSet.getString(Tags.PatientID) != null ? dataSet.getString(Tags.PatientID) : "unknown";
        dob = dataSet.getString(Tags.PatientBirthDate) != null ? dataSet.getString(Tags.PatientBirthDate) : "unknown";
        accessionNo = dataSet.getString(Tags.AccessionNumber) != null ? dataSet.getString(Tags.AccessionNumber) : "unknown";
        studyLevelInstances = dataSet.getString(Tags.NumberOfStudyRelatedInstances) != null ? dataSet.getString(Tags.NumberOfStudyRelatedInstances) : "unknown";
        numberOfSeries = dataSet.getString(Tags.NumberOfStudyRelatedSeries) != null ? dataSet.getString(Tags.NumberOfStudyRelatedSeries) : "unknown";
    }

    public String getAccessionNo() {
        return accessionNo;
    }

    public void setAccessionNo(String accessionNo) {
        this.accessionNo = accessionNo;
    }

    public String getStudyUID() {
        return studyUID;
    }

    public void setStudyUID(String studyUID) {
        this.studyUID = studyUID;
    }

    public String getDob() {
        return dob;
    }

    public String getModalitiesInStudy() {
        return modalitiesInStudy;
    }

    public void setModalitiesInStudy(String modalitiesInStudy) {
        this.modalitiesInStudy = modalitiesInStudy;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getStudyDate() {
        return studyDate;
    }

    public void setStudyDate(String studyDate) {
        this.studyDate = studyDate;
    }

    public String getStudyDescription() {
        return studyDescription;
    }

    public void setStudyDescription(String studyDescription) {
        this.studyDescription = studyDescription;
    }

    public String getStudyLevelInstances() {
        return studyLevelInstances;
    }

    public void setStudyLevelInstances(String studyLevelInstances) {
        this.studyLevelInstances = studyLevelInstances;
    }

    public String getNumberOfSeries() {
        return numberOfSeries;
    }

    public void setNumberOfSeries(String numberOfSeries) {
        this.numberOfSeries = numberOfSeries;
    }

    public String getStudyTime() {
        return studyTime;
    }

    public void setStudyTime(String studyTime) {
        this.studyTime = studyTime;
    }
}
