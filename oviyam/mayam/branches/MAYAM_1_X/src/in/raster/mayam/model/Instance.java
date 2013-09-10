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
package in.raster.mayam.model;

import in.raster.mayam.util.measurement.Annotation;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.HashMap;

/**
 *
 * @author  BabuHussain
 * @version 0.5
 *
 */
public class Instance implements Serializable {
   
    private String sop_iuid;
    private String instance_no;
    private String filepath;
    private BufferedImage pixelData;
    private Annotation annotation;

    private String imagePosition;
    private String imageOrientation;
    private String pixelSpacing;
    private String []imageType;
    private int row;
    private int column;
    private String frameOfReferenceUID;
    private String referenceSOPInstanceUID;
    private boolean multiframe=false;
    private int currentFrameNum;
    private int totalNumFrames;
    private int seriesLevelIndex;
    private int instanceNumber;

    private String sliceLocation;

    private boolean encapsulatedPDF=false;

    private HashMap<Integer,Annotation> annotations=null;


    public Instance() {
        sop_iuid = "";
        instance_no = "";
        filepath = "";
        currentFrameNum=0;
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

    public BufferedImage getPixelData() {
        return pixelData;
    }

    public void setPixelData(BufferedImage pixelData) {
        this.pixelData = pixelData;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public void setAnnotation(Annotation annotation) {        
        this.annotation = annotation;
    }

    public int getColumn() {
        return column;
    }

    public String getFrameOfReferenceUID() {
        return frameOfReferenceUID;
    }

    public String getImageOrientation() {
        return imageOrientation;
    }

    public String getImagePosition() {
        return imagePosition;
    }

    public String[] getImageType() {
        return imageType;
    }

    public String getPixelSpacing() {
        return pixelSpacing;
    }

    public String getReferenceSOPInstanceUID() {
        return referenceSOPInstanceUID;
    }

    public int getRow() {
        return row;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public void setFrameOfReferenceUID(String frameOfReferenceUID) {
        this.frameOfReferenceUID = frameOfReferenceUID;
    }

    public void setImageOrientation(String imageOrientation) {
        this.imageOrientation = imageOrientation;
    }

    public void setImagePosition(String imagePosition) {
        this.imagePosition = imagePosition;
    }

    public void setImageType(String[] imageType) {
        this.imageType = imageType;
    }

    public void setPixelSpacing(String pixelSpacing) {
        this.pixelSpacing = pixelSpacing;
    }

    public void setReferenceSOPInstanceUID(String referenceSOPInstanceUID) {
        this.referenceSOPInstanceUID = referenceSOPInstanceUID;
    }

    public void setRow(int row) {
        this.row = row;
    }
    public boolean isMultiframe() {
        return multiframe;
    }

    public void setMultiframe(boolean multiframe) {
        this.multiframe = multiframe;
    }

    public HashMap<Integer, Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(HashMap<Integer, Annotation> annotations) {
        this.annotations = annotations;
    }
    /**
     * This method used to add the annotation objects of the selected frame
     * with the index value to the instance.
     * @param index
     * @param annotation
     */
    public void addMultiframeAnnotation(Integer index,Annotation annotation)
    {
        if(annotations==null)
        annotations=new HashMap<Integer, Annotation>();

        if(annotations.containsKey(index))
        annotations.remove(index);
        annotations.put(index, annotation);
    }
    /**
     * This method used to get the annotation objects of the specified frame
     * of the instance.
     * @param index
     * @return
     */
    public Annotation getMultiframeAnnotation(Integer index)
    {
        Annotation annotation=null;
        if(annotations!=null)
        {
            if(annotations.containsKey(index))
            annotation=annotations.get(index);
        }

        return annotation;
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

    public void setSeriesLevelIndex(int seriesLevelIndex)
    {
        this.seriesLevelIndex=seriesLevelIndex;
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
}