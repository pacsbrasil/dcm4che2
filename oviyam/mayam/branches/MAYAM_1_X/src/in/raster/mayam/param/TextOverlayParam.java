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
package in.raster.mayam.param;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author  BabuHussain
 * @version 0.5
 *
 */
public class TextOverlayParam {

    private String patientName = "";
    private String patientID = "";
    private String sex = "";
    private String institutionName = "";
    private String studyDate = "";
    private String studyTime = "";
    private String windowLevel = "";
    private String bodyPartExamined = "";
    private String slicePosition = "";
    private String patientPosition = "";
    private String windowWidth = "";
    private String xPosition = "";
    private String yPosition = "";
    private String pxValue = "";
    private String totalInstance = "";
    private int currentInstance = 1;
    private String zoomLevel = "";

    //added variables for MAYAM-61
    private String instanceNumber="";
    private String studyDescription="";
    private String seriesDescription="";
    SimpleDateFormat sourceDateFormat = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat destinationDateFormat = new SimpleDateFormat("yyyy/MM/dd");
    private String framePosition = "";
    private String imageSize = "";
    private String viewSize = "";

    public TextOverlayParam() {
    }

    public String getBodyPartExamined() {
        return bodyPartExamined;
    }

    public void setBodyPartExamined(String bodyPartExamined) {
        this.bodyPartExamined = bodyPartExamined;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public String getPatientID() {
        return patientID;
    }

    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientPosition() {
        return patientPosition;
    }

    public void setPatientPosition(String patientPosition) {
        this.patientPosition = patientPosition;
    }

    public String getPxValue() {
        return pxValue;
    }

    public void setPxValue(String pxValue) {
        this.pxValue = pxValue;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getSlicePosition() {
        return slicePosition;
    }

    public void setSlicePosition(String slicePosition) {
        this.slicePosition = slicePosition;
    }

    public String getStudyDate() {
        return studyDate;
    }

    public void setStudyDate(String studyDate) {
        try {
            Date d = sourceDateFormat.parse(studyDate);
            this.studyDate = destinationDateFormat.format(d);
        } catch (ParseException ex) {
            this.studyDate = "";
        }
    }

    public String getStudyTime() {
        return studyTime;
    }

    public void setStudyTime(String studyTime) {
        this.studyTime = studyTime;
    }

    public String getWindowLevel() {
        return windowLevel;
    }

    public void setWindowLevel(String windowLevel) {
        this.windowLevel = windowLevel;
    }

    public String getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(String windowWidth) {
        this.windowWidth = windowWidth;
    }

    public void setWindowingParameter(String WL, String WW) {
        this.windowLevel = WL;
        this.windowWidth = WW;
    }

    public void setProbeParameters(String[] probeParameters) {
        this.xPosition = probeParameters[0];
        this.yPosition = probeParameters[1];
        this.pxValue = probeParameters[2];

    }

    public String getXPosition() {
        return xPosition;
    }

    public void setXPosition(String xPosition) {
        this.xPosition = xPosition;
    }

    public String getYPosition() {
        return yPosition;
    }

    public void setYPosition(String yPosition) {
        this.yPosition = yPosition;
    }

    public String getTotalInstance() {
        return totalInstance;
    }

    public void setTotalInstance(String totalInstance) {
        this.totalInstance = totalInstance;
    }

    public int getCurrentInstance() {
        return currentInstance;
    }

    public void setCurrentInstance(int currentInstance) {
        this.currentInstance = currentInstance;
    }

    public String getZoomLevel() {
        return zoomLevel;
    }

    public void setZoomLevel(String zoomLevel) {
        this.zoomLevel = zoomLevel;
    }

    public String getFramePosition() {
        return framePosition;
    }

    public void setFramePosition(String framePosition) {
        this.framePosition = framePosition;
    }

    public String getInstanceNumber() {
        return instanceNumber;
    }

    public void setInstanceNumber(String instanceNumber) {
        this.instanceNumber = instanceNumber;
    }

    public String getSeriesDescription() {
        return seriesDescription;
    }

    public void setSeriesDescription(String seriesDescription) {
        this.seriesDescription = seriesDescription;
    }

    public String getStudyDescription() {
        return studyDescription;
    }

    public void setStudyDescription(String studyDescription) {
        this.studyDescription = studyDescription;
    }
    
    public String getImageSize() {
        return imageSize;
    }

    public void setImageSize(String imageSize) {
        this.imageSize = imageSize;
    }

    public String getViewSize() {
        return viewSize;
    }

    public void setViewSize(String viewSize) {
        this.viewSize = viewSize;
    }
}
