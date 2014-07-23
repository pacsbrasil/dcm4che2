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
import java.util.Vector;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

/**
 *
 * @author Devishree
 * @version 2.1
 */
public class SeriesNode {

    boolean isHeader, isMultiframe, isVideo;
    private String studyUID, seriesUID, seriesNo, modality, institutionName, seriesDesc, bodyPartExamined, seriesDate, seriesTime, seriesRelatedInstance, studyReleatedInstances, instanceUIDIfMultiframe;
    Vector<Dataset> instances_Data = null;
    Vector<Dataset> multiframesAndVideos = null;

    public SeriesNode(Dataset seriesDataset) {
        if (seriesDataset != null) {
            this.seriesUID = seriesDataset.getString(Tags.SeriesInstanceUID);
            this.studyUID = seriesDataset.getString(Tags.StudyInstanceUID);
            this.seriesNo = seriesDataset.getString(Tags.SeriesNumber) != null ? seriesDataset.getString(Tags.SeriesNumber) : "";
            this.modality = seriesDataset.getString(Tags.Modality);
            this.bodyPartExamined = seriesDataset.getString(Tags.BodyPartExamined);
            this.seriesDesc = seriesDataset.getString(Tags.SeriesDescription) != null ? seriesDataset.getString(Tags.SeriesDescription) : "";
            try {
                seriesTime = DateFormat.getTimeInstance(DateFormat.DEFAULT, ApplicationContext.currentLocale).format(seriesDataset.getDate(Tags.SeriesTime));
            } catch (Exception ex) {
                seriesTime = "unknown";
            }
            try {
                seriesDate = DateFormat.getDateInstance(DateFormat.DEFAULT, ApplicationContext.currentLocale).format(seriesDataset.getDate(Tags.SeriesDate));
            } catch (Exception ex) {
                seriesDate = "unknown";
            }
            this.institutionName = seriesDataset.getString(Tags.InstitutionName) != null ? seriesDataset.getString(Tags.InstitutionName) : "";
            this.seriesRelatedInstance = String.valueOf(seriesDataset.getInteger(Tags.NumberOfSeriesRelatedInstances));
        } else {
            isHeader = true;
        }
    }

//    public SeriesNode(DicomObject seriesDataset) {
//        if (seriesDataset != null) {
//            this.seriesUID = seriesDataset.getString(Tags.SeriesInstanceUID);
//            this.studyUID = seriesDataset.getString(Tags.StudyInstanceUID);
//            this.seriesNo = seriesDataset.getString(Tags.SeriesNumber) != null ? seriesDataset.getString(Tags.SeriesNumber) : "";
//            this.modality = seriesDataset.getString(Tags.Modality);
//            this.bodyPartExamined = seriesDataset.getString(Tags.BodyPartExamined);
//            this.seriesDesc = seriesDataset.getString(Tags.SeriesDescription) != null ? seriesDataset.getString(Tags.SeriesDescription) : "";
//            try {
//                seriesTime = DateFormat.getTimeInstance(DateFormat.DEFAULT, ApplicationContext.currentLocale).format(seriesDataset.getDate(Tags.SeriesTime));
//            } catch (Exception ex) {
//                seriesTime = "unknown";
//            }
//            try {
//                seriesDate = DateFormat.getDateInstance(DateFormat.DEFAULT, ApplicationContext.currentLocale).format(seriesDataset.getDate(Tags.SeriesDate));
//            } catch (Exception ex) {
//                seriesDate = "unknown";
//            }
//            this.institutionName = seriesDataset.getString(Tags.InstitutionName) != null ? seriesDataset.getString(Tags.InstitutionName) : "";
//            this.seriesRelatedInstance = String.valueOf(seriesDataset.getInt(Tags.NumberOfSeriesRelatedInstances));
//        } else {
//            isHeader = true;
//        }
//    }
    public SeriesNode(String studyUid, String seriesUid, String seriesNo, String seriesDesc, String bodyPart, String seriesDate, String seriesTime, boolean multiframe, String instanceUid, int noOfInstances) {
        this.studyUID = studyUid;
        this.seriesUID = seriesUid;
        this.seriesNo = seriesNo;
        this.seriesDesc = seriesDesc;
        this.bodyPartExamined = bodyPart;
        this.isMultiframe = multiframe;
        this.instanceUIDIfMultiframe = instanceUid;
        this.seriesRelatedInstance = String.valueOf(noOfInstances);
        try {
            this.seriesDate = DateFormat.getDateInstance(DateFormat.DEFAULT, ApplicationContext.currentLocale).format(new SimpleDateFormat("dd/MM/yyyy").parse(seriesDate));
        } catch (ParseException ex) {
            this.seriesDate = seriesDate;
        }
        try {
            this.seriesTime = DateFormat.getTimeInstance(DateFormat.DEFAULT, ApplicationContext.currentLocale).format(new SimpleDateFormat("hh:mm:ss").parse(seriesTime));
        } catch (ParseException ex) {
            this.seriesTime = seriesTime;
        }
    }

    public boolean isIsHeader() {
        return isHeader;
    }

    public void setIsHeader(boolean isHeader) {
        this.isHeader = isHeader;
    }

    public boolean isMultiframe() {
        return isMultiframe;
    }

    public void setMultiframe(boolean isMultiframe) {
        this.isMultiframe = isMultiframe;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideoStatus(boolean isVideo) {
        this.isVideo = isVideo;
    }

    public String getStudyUID() {
        return studyUID;
    }

    public void setStudyUID(String studyUID) {
        this.studyUID = studyUID;
    }

    public String getSeriesUID() {
        return seriesUID;
    }

    public void setSeriesUID(String seriesUID) {
        this.seriesUID = seriesUID;
    }

    public String getSeriesNo() {
        return seriesNo;
    }

    public void setSeriesNo(String seriesNo) {
        this.seriesNo = seriesNo;
    }

    public String getModality() {
        return modality;
    }

    public void setModality(String modality) {
        this.modality = modality;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public String getSeriesDesc() {
        return seriesDesc;
    }

    public void setSeriesDesc(String seriesDesc) {
        this.seriesDesc = seriesDesc;
    }

    public String getBodyPartExamined() {
        return bodyPartExamined;
    }

    public void setBodyPartExamined(String bodyPartExamined) {
        this.bodyPartExamined = bodyPartExamined;
    }

    public String getSeriesDate() {
        return seriesDate;
    }

    public void setSeriesDate(String seriesDate) {
        this.seriesDate = seriesDate;
    }

    public String getSeriesTime() {
        return seriesTime;
    }

    public void setSeriesTime(String seriesTime) {
        this.seriesTime = seriesTime;
    }

    public String getSeriesRelatedInstance() {
        return seriesRelatedInstance;
    }

    public void setSeriesRelatedInstance(String seriesRelatedInstance) {
        this.seriesRelatedInstance = seriesRelatedInstance;
    }

    public String getStudyReleatedInstances() {
        return studyReleatedInstances;
    }

    public void setStudyReleatedInstances(String studyReleatedInstances) {
        this.studyReleatedInstances = studyReleatedInstances;
    }

    public String getInstanceUIDIfMultiframe() {
        return instanceUIDIfMultiframe;
    }

    public void setInstanceUIDIfMultiframe(String instanceUIDIfMultiframe) {
        this.instanceUIDIfMultiframe = instanceUIDIfMultiframe;
    }

    public Vector<Dataset> getInstances_Data() {
        return instances_Data;
    }

    public void setInstances_Data(Vector<Dataset> instances_Data) {
        this.instances_Data = instances_Data;
    }

    public Vector<Dataset> getMultiframesAndVideos() {
        return multiframesAndVideos;
    }

    public void setMultiframesAndVideos(Vector<Dataset> multiframesAndVideos) {
        this.multiframesAndVideos = multiframesAndVideos;
    }

    public boolean isAllMulltiframe() {
        return (instances_Data.size() - multiframesAndVideos.size() <= 0);
    }
}
