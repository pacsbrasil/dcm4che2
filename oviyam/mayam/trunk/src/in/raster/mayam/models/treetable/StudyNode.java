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
package in.raster.mayam.models.treetable;

import in.raster.mayam.context.ApplicationContext;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

/**
 *
 * @author Devishree
 * @version 2.1
 */
public class StudyNode {

    private String location, studyUID = "";
    private String patientId = "", patientName = "", dob = "", sex = "", accessionNo = "", studyDate = "", studyTime, referredBy = "", retAET, studyDescription = "", modalitiesInStudy = "", studyReleatedInstances;
    private String[] modalities;
    List<SeriesNode> children = null;
    private int isRoot = 1;

    public StudyNode(Dataset studyDataset, List<SeriesNode> children) {
        studyUID = studyDataset.getString(Tags.StudyInstanceUID);
        studyDescription = studyDataset.getString(Tags.StudyDescription) != null ? studyDataset.getString(Tags.StudyDescription) : "unknown";
        studyDate = studyDataset.getString(Tags.StudyDate) != null ? studyDataset.getString(Tags.StudyDate) : "unknown";
        studyTime = studyDataset.getString(Tags.StudyTime) != null ? studyDataset.getString(Tags.StudyTime) : "unknown";
        modalities = studyDataset.getStrings(Tags.ModalitiesInStudy) != null ? studyDataset.getStrings(Tags.ModalitiesInStudy) : null;
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
        patientName = studyDataset.getString(Tags.PatientName) != null ? studyDataset.getString(Tags.PatientName) : "unknown";
        patientId = studyDataset.getString(Tags.PatientID) != null ? studyDataset.getString(Tags.PatientID) : "unknown";
        sex = studyDataset.getString(Tags.PatientSex) != null ? studyDataset.getString(Tags.PatientSex) : "unknown";
        referredBy = studyDataset.getString(Tags.ReferringPhysicianName) != null ? studyDataset.getString(Tags.ReferringPhysicianName) : "unknown";
        retAET = studyDataset.getString(Tags.RetrieveAET) != null ? studyDataset.getString(Tags.RetrieveAET) : "unknown";
        accessionNo = studyDataset.getString(Tags.AccessionNumber) != null ? studyDataset.getString(Tags.AccessionNumber) : "unknown";
        studyReleatedInstances = studyDataset.getString(Tags.NumberOfStudyRelatedInstances) != null ? studyDataset.getString(Tags.NumberOfStudyRelatedInstances) : "unknown";
        DateFormat df1 = DateFormat.getDateInstance(DateFormat.DEFAULT, ApplicationContext.currentLocale);
        try {
            studyDate = df1.format(studyDataset.getDate(Tags.StudyDate));
            dob = df1.format(studyDataset.getDate(Tags.PatientBirthDate));
        } catch (Exception ex) {
            studyDate = "unknown";
            dob = "unknown";
        }
        try {
            studyTime = DateFormat.getTimeInstance(DateFormat.DEFAULT, ApplicationContext.currentLocale).format(studyDataset.getDate(Tags.StudyTime));
        } catch (Exception ex) {
            studyTime = "unknown";
        }
        this.children = children;
    }

    public StudyNode(String patientId, String patientName, String dob, String accessionNo, String studyDate, String studyTime, String studyDescription, String modality, String numberOfSeries, String studyLevelInstances, String studyInstaceUid) {
        this.patientId = patientId;
        this.patientName = patientName;
        this.accessionNo = accessionNo;
        try {
            DateFormat df1 = DateFormat.getDateInstance(DateFormat.DEFAULT, ApplicationContext.currentLocale);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            this.studyDate = df1.format(sdf.parse(studyDate));
            this.dob = df1.format(sdf.parse(dob));
        } catch (ParseException ex) {
            this.studyDate = studyDate;
            this.dob = dob;
        }
        try {
            this.studyTime = DateFormat.getTimeInstance(DateFormat.DEFAULT, ApplicationContext.currentLocale).format(new SimpleDateFormat("hh:mm:ss").parse(studyTime));
        } catch (ParseException ex) {
            this.studyTime = studyTime;
        }
        this.studyDescription = studyDescription;
        this.modalitiesInStudy = modality;
        this.studyReleatedInstances = studyLevelInstances;
        this.studyUID = studyInstaceUid;
    }

    public StudyNode(List children) {
        this.children = children;
        isRoot = 0;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStudyUID() {
        return studyUID;
    }

    public void setStudyUID(String studyUID) {
        this.studyUID = studyUID;
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

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getAccessionNo() {
        return accessionNo;
    }

    public void setAccessionNo(String accessionNo) {
        this.accessionNo = accessionNo;
    }

    public String getStudyDate() {
        return studyDate;
    }

    public void setStudyDate(String studyDate) {
        this.studyDate = studyDate;
    }

    public String getStudyTime() {
        return studyTime;
    }

    public void setStudyTime(String studyTime) {
        this.studyTime = studyTime;
    }

    public String getReferredBy() {
        return referredBy;
    }

    public void setReferredBy(String referredBy) {
        this.referredBy = referredBy;
    }

    public String getRetAET() {
        return retAET;
    }

    public void setRetAET(String retAET) {
        this.retAET = retAET;
    }

    public String getStudyDescription() {
        return studyDescription;
    }

    public void setStudyDescription(String studyDescription) {
        this.studyDescription = studyDescription;
    }

    public String getModalitiesInStudy() {
        return modalitiesInStudy;
    }

    public void setModalitiesInStudy(String modalitiesInStudy) {
        this.modalitiesInStudy = modalitiesInStudy;
    }

    public String getStudyReleatedInstances() {
        return studyReleatedInstances;
    }

    public void setStudyReleatedInstances(String studyReleatedInstances) {
        this.studyReleatedInstances = studyReleatedInstances;
    }

    public String[] getModalities() {
        return modalities;
    }

    public void setModalities(String[] modalities) {
        this.modalities = modalities;
    }

    public int getStudyRelatedSeries() {
        return (children != null && isRoot != 0) ? children.size() : 0;
    }

    public List<SeriesNode> getChildren() {
        return children;
    }

    public void setChildren(List<SeriesNode> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return location;
    }

    public int getChildCount() {
        return children != null ? children.size() : 0;
    }

    public Object getChild(int index) {
        return this.children.get(index);
    }

    public SeriesNode getFirstChild() {
        return children.get(1);
    }

    public int isRoot() {
        return isRoot;
    }

    public void addChild(SeriesNode child) {
        this.children.add(child);
    }

    public void setChildren(List children, int isRoot) {
        this.children = children;
        this.isRoot = isRoot;
    }
}
