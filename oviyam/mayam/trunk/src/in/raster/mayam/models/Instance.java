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

/**
 *
 * @author BabuHussain
 * @version 0.5
 *
 */
public class Instance {

    private String sop_iuid;
    private String sopClassUid;
    private String instance_no;
    private String filepath;
    private boolean multiframe = false;
    private int currentFrameNum;
    private int totalNumFrames;
    private int seriesLevelIndex;
    private int instanceNumber;
    private String sliceLocation;
    private boolean encapsulatedPDF = false;

    public Instance() {
        sop_iuid = "";
        instance_no = "";
        filepath = "";
        currentFrameNum = 0;
    }

    public Instance(String fileUrl, String sopUid, String instanceNo, boolean isEnacpsulatedDoc, String sopClassUid, boolean isMultiframe) {
        this.filepath = fileUrl;
        this.sop_iuid = sopUid;
        this.instance_no = instanceNo;
        this.encapsulatedPDF = isEnacpsulatedDoc;
        this.sopClassUid = sopClassUid;
        this.multiframe = isMultiframe;
    }

    /**
     * @return the sop_iuid
     */
    public String getSop_iuid() {
        return sop_iuid;
    }

    /**
     * @param sop_iuid the sop_iuid to set
     */
    public void setSop_iuid(String sop_iuid) {
        this.sop_iuid = sop_iuid;
    }

    /**
     * @return the instance_no
     */
    public String getInstance_no() {
        return instance_no;
    }

    /**
     * @param instance_no the instance_no to set
     */
    public void setInstance_no(String instance_no) {
        this.instance_no = instance_no;
    }

    /**
     * @return the filepath
     */
    public String getFilepath() {
        return filepath;
    }

    /**
     * @param filepath the filepath to set
     */
    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public boolean isMultiframe() {
        return multiframe;
    }

    public void setMultiframe(boolean multiframe) {
        this.multiframe = multiframe;
    }

    public int getCurrentFrameNum() {
        return currentFrameNum;
    }

    public void setCurrentFrameNum(int currentFrameNum) {
        this.currentFrameNum = currentFrameNum;
    }

    public int getTotalNumFrames() {
        return totalNumFrames;
    }

    public void setTotalNumFrames(int totalNumFrames) {
        this.totalNumFrames = totalNumFrames;
    }

    public int getSeriesLevelIndex() {
        return seriesLevelIndex;
    }

    public void setSeriesLevelIndex(int seriesLevelIndex) {
        this.seriesLevelIndex = seriesLevelIndex;
    }

    public int getInstanceNumber() {
        return instanceNumber;
    }

    public void setInstanceNumber(int instanceNumber) {
        this.instanceNumber = instanceNumber;
    }

    public String getSliceLocation() {
        return sliceLocation;
    }

    public void setSliceLocation(String sliceLocation) {
        this.sliceLocation = sliceLocation;
    }

    public boolean isEncapsulatedPDF() {
        return encapsulatedPDF;
    }

    public void setEncapsulatedPDF(boolean encapsulatedPDF) {
        this.encapsulatedPDF = encapsulatedPDF;
    }

    public String getSopClassUid() {
        return sopClassUid;
    }

    public void setSopClassUid(String sopClassUid) {
        this.sopClassUid = sopClassUid;
    }
}