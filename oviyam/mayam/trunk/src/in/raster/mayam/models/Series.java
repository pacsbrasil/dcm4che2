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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

/**
 *
 * @author BabuHussain
 * @version 0.5
 *
 */
public class Series {

    private String SeriesInstanceUID;
    private String StudyInstanceUID;
    private String SeriesNumber;
    private String Modality;
    private String institutionName;
    private List<Instance> imageList;
    private String seriesDesc;
    private String bodyPartExamined;
    private int seriesRelatedInstance;
    String seriesDate;
    String seriesTime;
    private boolean multiframe;
    private String instanceUID;
    private boolean isVideo;

    public Series() {
        SeriesInstanceUID = "";
        StudyInstanceUID = "";
        Modality = "";
        SeriesNumber = "";
        seriesDesc = "";
        multiframe = false;
        instanceUID = "";
        seriesDate = "";
        seriesTime = "";
        isVideo = false;
        imageList = new ArrayList<Instance>();
    }

    public Series(Dataset dataset) {
        SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm:ss");
        SimpleDateFormat timeParser = new SimpleDateFormat("hhmmss");
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat dateParser = new SimpleDateFormat("yyyyMMdd");
        this.SeriesInstanceUID = dataset.getString(Tags.SeriesInstanceUID);
        this.StudyInstanceUID = dataset.getString(Tags.StudyInstanceUID);
        this.SeriesNumber = dataset.getString(Tags.SeriesNumber) != null ? dataset.getString(Tags.SeriesNumber) : "";
        this.Modality = dataset.getString(Tags.Modality);
        this.bodyPartExamined = dataset.getString(Tags.BodyPartExamined);
        this.seriesDesc = dataset.getString(Tags.SeriesDescription) != null ? dataset.getString(Tags.SeriesDescription) : "";
        try {
            seriesTime = timeFormatter.format(timeParser.parse(dataset.getString(Tags.SeriesTime)));
        } catch (Exception ex) {
            seriesTime = "unknown";
        }
        try {
            seriesDate = dateFormatter.format(dateParser.parse(dataset.getString(Tags.SeriesDate)));
        } catch (Exception ex) {
            seriesDate = "unknown";
        }

//        seriesDate = dataset.getString(Tags.SeriesDate) != null ? dataset.getString(Tags.SeriesDate) : "";
//        seriesTime = dataset.getString(Tags.SeriesTime) != null ? dataset.getString(Tags.SeriesTime) : "";
        this.institutionName = dataset.getString(Tags.InstitutionName) != null ? dataset.getString(Tags.InstitutionName) : "";
        this.seriesRelatedInstance = dataset.getInteger(Tags.NumberOfSeriesRelatedInstances);
    }

    public Series(String studyUid, String seriesUid, String seriesNo, String seriesDesc, String bodyPart, String seriesDate, String seriesTime, boolean multiframe, String instanceUid, int noOfInstances) {
        this.StudyInstanceUID = studyUid;
        this.SeriesInstanceUID = seriesUid;
        this.SeriesNumber = seriesNo;
        this.seriesDesc = seriesDesc;
        this.bodyPartExamined = bodyPart;
        this.seriesDate = seriesDate;
        this.seriesTime = seriesTime;
        this.multiframe = multiframe;
        this.instanceUID = instanceUid;
        this.seriesRelatedInstance = noOfInstances;
        imageList = new ArrayList<Instance>();
    }

    /**
     * @return the SeriesInstanceUID
     */
    public String getSeriesInstanceUID() {
        return SeriesInstanceUID;
    }

    /**
     * @param SeriesInstanceUID the SeriesInstanceUID to set
     */
    public void setSeriesInstanceUID(String SeriesInstanceUID) {
        this.SeriesInstanceUID = SeriesInstanceUID;
    }

    /**
     * @return the Modality
     */
    public String getModality() {
        return Modality;
    }

    /**
     * @param Modality the Modality to set
     */
    public void setModality(String Modality) {
        this.Modality = Modality;
    }

    public String getSeriesNumber() {
        return SeriesNumber;
    }

    /**
     *
     * @param seriesNumber
     */
    public void setSeriesNumber(String seriesNumber) {
        SeriesNumber = seriesNumber;
    }

    /**
     * @return the imageList
     */
    public List<Instance> getImageList() {
        return imageList;
    }

    /**
     * @param imageList the imageList to set
     */
    public void setImageList(List<Instance> imageList) {
        this.imageList = imageList;
    }

    public String getStudyInstanceUID() {
        return StudyInstanceUID;
    }

    public void setStudyInstanceUID(String StudyInstanceUID) {
        this.StudyInstanceUID = StudyInstanceUID;
    }

    public String getSeriesDesc() {
        return seriesDesc;
    }

    public void setSeriesDesc(String seriesDesc) {
        this.seriesDesc = seriesDesc;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public int getSeriesRelatedInstance() {
        return seriesRelatedInstance;
    }

    public void setSeriesRelatedInstance(int seriesRelatedInstance) {
        this.seriesRelatedInstance = seriesRelatedInstance;
    }

    public String getBodyPartExamined() {
        return bodyPartExamined;
    }

    public void setBodyPartExamined(String bodyPartExamined) {
        this.bodyPartExamined = bodyPartExamined;
    }

    public String getInstanceUID() {
        return instanceUID;
    }

    public void setInstanceUID(String instanceUID) {
        this.instanceUID = instanceUID;
    }

    public boolean isMultiframe() {
        return multiframe;
    }

    public void setMultiframe(boolean multiframe) {
        this.multiframe = multiframe;
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

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideoStatus(boolean isVideo) {
        this.isVideo = isVideo;
    }
}