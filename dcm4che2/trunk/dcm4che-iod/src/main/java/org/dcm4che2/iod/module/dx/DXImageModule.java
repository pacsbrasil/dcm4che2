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
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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

package org.dcm4che2.iod.module.dx;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.iod.module.composite.GeneralImageModule;
import org.dcm4che2.iod.module.lut.LUT;
import org.dcm4che2.iod.validation.ValidationContext;
import org.dcm4che2.iod.validation.ValidationResult;
import org.dcm4che2.iod.value.Flag;
import org.dcm4che2.iod.value.PixelIntensityRelationship;
import org.dcm4che2.iod.value.RescaleType;
import org.dcm4che2.iod.value.Sign;

/**
 * 
 * A specialized class that represents the DX Image Module.
 * <p>
 * Table C.8-70 contains IOD Attributes that describe a DX Image by specializing
 * Attributes of the General Image and Image Pixel Modules, and adding
 * additional Attributes.
 * <p>
 * This class is the son of
 * {@link org.dcm4che2.iod.module.composite.GeneralImageModule} and grandson of
 * {@link org.dcm4che2.iod.module.composite.ImagePixel}. Therefore, make use of
 * this class, and you will not need to worry about the other two modules
 * (C.7.3.1 and C.7.6.3).
 * 
 * @author Antonio Magni <dcm4ceph@antoniomagni.org>
 * @author Gunter Zeilinger<gunterze@gmail.com>
 * @version $Revision$ $Date$
 */
public class DXImageModule extends GeneralImageModule {

    public DXImageModule(DicomObject dcmobj) {
        super(dcmobj);
    }

    public void init() {
        super.init();
        setRescaleIntercept(0.f);
        setRescaleSlope(1.f);
        setRescaleType(RescaleType.US);
    }

    public void validate(ValidationContext ctx, ValidationResult result) {
        super.validate(ctx, result);
        if (!PixelIntensityRelationship
                .isValid(getPixelIntensityRelationship())) {
            result.logInvalidValue(Tag.PIXEL_INTENSITY_RELATIONSHIP, dcmobj);
        }
        if (!Sign.isValid(getPixelIntensityRelationshipSign())) {
            if (dcmobj.containsValue(Tag.PIXEL_INTENSITY_RELATIONSHIP_SIGN)) {
                result.logInvalidValue(Tag.PIXEL_INTENSITY_RELATIONSHIP_SIGN,
                        dcmobj);
            }
        }
        if (!Flag.isValid(getCalibrationImage())) {
            result.logInvalidValue(Tag.CALIBRATION_IMAGE, dcmobj);
        }
    }

    /**
     * The relationship between the Pixel sample values and the X-Ray beam
     * intensity.
     * <p>
     * Enumerated Values: LIN = Linearly proportional to X-Ray beam intensity
     * LOG = Logarithmically proportional to X- Ray beam intensity See
     * C.8.11.3.1.2 for further explanation.
     * 
     * @param s
     */
    public void setPixelIntensityRelationship(String s) {
        dcmobj.putString(Tag.PIXEL_INTENSITY_RELATIONSHIP, VR.CS, s);
    }

    public String getPixelIntensityRelationship() {
        return dcmobj.getString(Tag.PIXEL_INTENSITY_RELATIONSHIP);
    }

    /**
     * The sign of the relationship between the Pixel sample values stored in
     * Pixel Data (7FE0,0010) and the X-Ray beam intensity.
     * <p>
     * Enumerated Values; 1 = Lower pixel values correspond to less X-Ray beam
     * intensity -1 = Higher pixel values correspond to less X-Ray beam
     * intensity See C.8.11.3.1.2 for further explanation.
     * 
     * @param ss
     */
    public void setPixelIntensityRelationshipSign(int ss) {
        dcmobj.putInt(Tag.PIXEL_INTENSITY_RELATIONSHIP_SIGN, VR.SS, ss);
    }

    public int getPixelIntensityRelationshipSign() {
        return dcmobj.getInt(Tag.PIXEL_INTENSITY_RELATIONSHIP_SIGN);
    }

    /**
     * The value b in the relationship between stored values (SV) in Pixel Data
     * (7FE0,0010) and the output units specified in Rescale Type (0028,1054).
     * <p>
     * Output units = m*SV + b.
     * <p>
     * Enumerated Value: 0
     * <p>
     * See C.8.11.3.1.2 for further explanation.
     * 
     * @param ds
     *            0
     */
    public void setRescaleIntercept(float ds) {
        dcmobj.putFloat(Tag.RESCALE_INTERCEPT, VR.DS, ds);
    }

    public float getRescaleIntercept() {
        return dcmobj.getFloat(Tag.RESCALE_INTERCEPT);
    }

    /**
     * m in the equation specified by Rescale Intercept (0028,1052).
     * <p>
     * Enumerated Value: 1
     * <p>
     * See C.8.11.3.1.2 for further explanation.
     * 
     * @param f
     *            1
     */
    private void setRescaleSlope(float f) {
        dcmobj.putFloat(Tag.RESCALE_SLOPE, VR.DS, f);
    }

    /**
     * m in the equation specified by Rescale Intercept (0028,1052).
     * <p>
     * Enumerated Value: 1
     * <p>
     * See C.8.11.3.1.2 for further explanation.
     * 
     * @return 1
     */
    public String getRescaleSlope() {
        return dcmobj.getString(Tag.RESCALE_SLOPE);
    }

    /**
     * Specifies the output units of Rescale Slope (0028,1053) and Rescale
     * Intercept (0028,1052).
     * <p>
     * Enumerated Value: US = Unspecified
     * <p>
     * See C.8.11.3.1.2 for further explanation.
     * 
     * @param cs
     *            US = Unspecified
     */
    private void setRescaleType(String cs) {
        dcmobj.putString(Tag.RESCALE_TYPE, VR.CS, cs);
    }

    /**
     * Specifies the output units of Rescale Slope (0028,1053) and Rescale
     * Intercept (0028,1052).
     * <p>
     * Enumerated Value: US = Unspecified
     * <p>
     * See C.8.11.3.1.2 for further explanation.
     * 
     * @return
     */
    public String getRescaleType() {
        return dcmobj.getString(Tag.RESCALE_TYPE);
    }

    /**
     * Description Indicates any visual processing performed on the images prior
     * to exchange.
     * <p>
     * See C.8.11.3.1.3 for further explanation.
     * 
     * @param lo
     */
    public void setAcquisitionDeviceProcessingDescription(String lo) {
        dcmobj.putString(Tag.ACQUISITION_DEVICE_PROCESSING_DESCRIPTION, VR.LO, lo);
    }

    /**
     * Description Indicates any visual processing performed on the images prior
     * to exchange.
     * <p>
     * See C.8.11.3.1.3 for further explanation.
     * 
     * @return
     */
    public String getAcquisitionDeviceProcessingDescription() {
        return dcmobj.getString(Tag.ACQUISITION_DEVICE_PROCESSING_DESCRIPTION);
    }

    /**
     * Code representing the device-specific processing associated with the
     * image (e.g. Organ Filtering code)
     * <p>
     * Note: This Code is manufacturer specific but provides useful annotation
     * information to the knowledgeable observer.
     * 
     * @param lo
     */
    public void setAcquisitionDeviceProcessingCode(String lo) {
        dcmobj.putString(Tag.ACQUISITION_DEVICE_PROCESSING_CODE, VR.LO, lo);
    }

    /**
     * Code representing the device-specific processing associated with the
     * image (e.g. Organ Filtering code)
     * <p>
     * Note: This Code is manufacturer specific but provides useful annotation
     * information to the knowledgeable observer.
     * 
     * @return
     */
    public String getAcquisitionDeviceProcessingCode() {
        return dcmobj.getString(Tag.ACQUISITION_DEVICE_PROCESSING_DESCRIPTION);
    }

    /**
     * Indicates whether a reference object (phantom) of known size is present
     * in the image and was used for calibration.
     * <p>
     * 
     * Enumerated Values:
     * 
     * YES NO
     * <p>
     * Device is identified using the Device module. See C.7.6.12 for further
     * explanation.
     * 
     * @param cs
     */
    public void setCalibrationImage(String cs) {
        dcmobj.putString(Tag.CALIBRATION_IMAGE, VR.CS, cs);
    }

    /**
     * Indicates whether a reference object (phantom) of known size is present
     * in the image and was used for calibration.
     * 
     * <p>
     * Enumerated Values:
     * 
     * YES NO
     * <p>
     * Device is identified using the Device module. See C.7.6.12 for further
     * explanation.
     * 
     * @return
     */
    public String getCalibrationImage() {
        return dcmobj.getString(Tag.CALIBRATION_IMAGE);
    }

    public LUT[] getVOILUTs() {
        return LUT.toLUTs(dcmobj.get(Tag.VOI_LUT_SEQUENCE));
    }

    public void setVOILUTs(LUT[] luts) {
        updateSequence(Tag.VOI_LUT_SEQUENCE, luts);
    }

    /**
     * Defines a Window Center for display.
     * <p>
     * See C.8.11.3.1.5 for further explanation.
     * <p>
     * Required if Presentation Intent Type (0008,0068) is FOR PRESENTATION and
     * VOI LUT Sequence (0028,3010) is not present. May also be present if VOI
     * LUT Sequence (0028,3010) is present.
     */
    public void setWindowCenter(float[] floats) {
        dcmobj.putFloats(Tag.WINDOW_CENTER, VR.DS, floats);
    }

    /**
     * Defines a Window Center for display.
     * <p>
     * See C.8.11.3.1.5 for further explanation.
     * <p>
     * Required if Presentation Intent Type (0008,0068) is FOR PRESENTATION and
     * VOI LUT Sequence (0028,3010) is not present. May also be present if VOI
     * LUT Sequence (0028,3010) is present.
     * 
     * @return
     */
    public float[] getWindowCenter() {
        return dcmobj.getFloats(Tag.WINDOW_CENTER);
    }

    /**
     * Window Width for display.
     * <p>
     * See C.8.11.3.1.5 for further explanation.
     * <p>
     * Required if Window Center (0028,1050) is sent.
     * 
     * @param ds
     */
    public void setWindowWidth(float[] floats) {
        dcmobj.putFloats(Tag.WINDOW_WIDTH, VR.DS, floats);
    }

    /**
     * Window Width for display.
     * <p>
     * See C.8.11.3.1.5 for further explanation.
     * <p>
     * Required if Window Center (0028,1050) is sent.
     * 
     * @return
     */
    public float[] getWindowWidth() {
        return dcmobj.getFloats(Tag.WINDOW_WIDTH);
    }

    /**
     * Free form explanation of the meaning of the Window Center and Width.
     * <p>
     * Multiple values correspond to multiple Window Center and Width values.
     * 
     * @param lo
     */
    public void setWindowCenterWidthExplanation(String lo) {
        dcmobj.putString(Tag.WINDOW_CENTER_WIDTH_EXPLANATION, VR.LO, lo);
    }

    /**
     * Free form explanation of the meaning of the Window Center and Width.
     * <p>
     * Multiple values correspond to multiple Window Center and Width values.
     * 
     * @return
     */
    public String getWindowCenterWidthExplanation() {
        return dcmobj.getString(Tag.WINDOW_CENTER_WIDTH_EXPLANATION);
    }
}
