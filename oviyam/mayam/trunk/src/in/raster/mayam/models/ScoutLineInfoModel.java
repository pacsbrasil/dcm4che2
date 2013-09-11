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
 * @version 0.6
 *
 */
public class ScoutLineInfoModel {

    private String imagePosition;
    private String imageOrientation;
    private String imagePixelSpacing;
    private int imageRow;
    private int imageColumn;
    private String imageFrameofReferenceUID;
    private String imageReferenceSOPInstanceUID;
    private String imageType;
    private String sliceLocation;

    public ScoutLineInfoModel() {
    }

    public ScoutLineInfoModel(String imagePos, String imgOrientation, String pixelSpacing, int row, int column, String frameOfReferenceUid, String referencedSopUid, String imageType, String sliceLocation) {
        this.imagePosition = imagePos;
        this.imageOrientation = imgOrientation;
        this.imagePixelSpacing = pixelSpacing;
        this.imageRow = row;
        this.imageColumn = column;
        this.imageFrameofReferenceUID = frameOfReferenceUid;
        this.imageReferenceSOPInstanceUID = referencedSopUid;
        this.imageType = imageType;
        this.sliceLocation = sliceLocation;
    }

    public String getImageFrameofReferenceUID() {
        return imageFrameofReferenceUID;
    }

    public void setImageFrameofReferenceUID(String imageFrameofReferenceUID) {
        this.imageFrameofReferenceUID = imageFrameofReferenceUID;
    }

    public String getImageOrientation() {
        return imageOrientation;
    }

    public void setImageOrientation(String imageOrientation) {
        this.imageOrientation = imageOrientation;
    }

    public String getImagePixelSpacing() {
        return imagePixelSpacing;
    }

    public void setImagePixelSpacing(String imagePixelSpacing) {
        this.imagePixelSpacing = imagePixelSpacing;
    }

    public String getImagePosition() {
        return imagePosition;
    }

    public void setImagePosition(String imagePosition) {
        this.imagePosition = imagePosition;
    }

    public String getImageReferenceSOPInstanceUID() {
        return imageReferenceSOPInstanceUID;
    }

    public void setImageReferenceSOPInstanceUID(String imageReferenceSOPInstanceUID) {
        this.imageReferenceSOPInstanceUID = imageReferenceSOPInstanceUID;
    }

    public int getImageColumn() {
        return imageColumn;
    }

    public void setImageColumn(int imageColumn) {
        this.imageColumn = imageColumn;
    }

    public int getImageRow() {
        return imageRow;
    }

    public void setImageRow(int imageRow) {
        this.imageRow = imageRow;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    public String getSliceLocation() {
        return sliceLocation;
    }

    public void setSliceLocation(String sliceLocation) {
        this.sliceLocation = sliceLocation;
    }
}
