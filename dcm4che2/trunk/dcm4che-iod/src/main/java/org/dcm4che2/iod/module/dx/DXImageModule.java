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
 * Gunter Zeilinger <gunterze@gmail.com>
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
 * 
 * Table C.8-70 contains IOD Attributes that describe a DX Image by specializing
 * Attributes of the General Image and Image Pixel Modules, and adding
 * additional Attributes.
 * 
 * @author Antonio Magni <dcm4ceph@antoniomagni.org>
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * 
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
        if (!PixelIntensityRelationship.isValid(getPixelIntensityRelationship())) {
            result.logInvalidValue(Tag.PixelIntensityRelationship, dcmobj);
        }
        if (!Sign.isValid(getPixelIntensityRelationshipSign())) {
            if (dcmobj.containsValue(Tag.PixelIntensityRelationshipSign)) {
                result.logInvalidValue(Tag.PixelIntensityRelationshipSign, dcmobj);
            }
        }
        if (!Flag.isValid(getCalibrationImage())) {
            result.logInvalidValue(Tag.CalibrationImage, dcmobj);
        }
    }
    
	/**
	 * The relationship between the Pixel sample values and the X-Ray beam
	 * intensity.
	 * 
	 * Enumerated Values: LIN = Linearly proportional to X-Ray beam intensity
	 * LOG = Logarithmically proportional to X- Ray beam intensity See
	 * C.8.11.3.1.2 for further explanation.
	 * 
	 * @param s
	 */
	public void setPixelIntensityRelationship(String s) {
		dcmobj.putString(Tag.PixelIntensityRelationship, VR.CS, s);
	}

	public String getPixelIntensityRelationship() {
		return dcmobj.getString(Tag.PixelIntensityRelationship);
	}

	/**
	 * The sign of the relationship between the Pixel sample values stored in
	 * Pixel Data (7FE0,0010) and the X-Ray beam intensity.
	 * 
	 * Enumerated Values; 1 = Lower pixel values correspond to less X-Ray beam
	 * intensity -1 = Higher pixel values correspond to less X-Ray beam
	 * intensity See C.8.11.3.1.2 for further explanation.
	 * 
	 * @param ss
	 */
	public void setPixelIntensityRelationshipSign(int ss) {
		dcmobj.putInt(Tag.PixelIntensityRelationshipSign, VR.SS, ss);
	}

	public int getPixelIntensityRelationshipSign() {
		return dcmobj.getInt(Tag.PixelIntensityRelationshipSign);
	}

	/**
	 * The value b in the relationship between stored values (SV) in Pixel Data
	 * (7FE0,0010) and the output units specified in Rescale Type (0028,1054).
	 * 
	 * Output units = m*SV + b.
	 * 
	 * Enumerated Value: 0
	 * 
	 * See C.8.11.3.1.2 for further explanation.
	 * 
	 * @param ds
	 *            0
	 */
	public void setRescaleIntercept(float ds) {
		dcmobj.putFloat(Tag.RescaleIntercept, VR.DS, ds);
	}
    
    public float getRescaleIntercept() {
        return dcmobj.getFloat(Tag.RescaleIntercept);
    }

	/**
	 * m in the equation specified by Rescale Intercept (0028,1052).
	 * 
	 * Enumerated Value: 1
	 * 
	 * See C.8.11.3.1.2 for further explanation.
	 * 
	 * @param f
	 *            1
	 */
	private void setRescaleSlope(float f) {
		dcmobj.putFloat(Tag.RescaleSlope, VR.DS, f);
	}

	/**
	 * m in the equation specified by Rescale Intercept (0028,1052).
	 * 
	 * Enumerated Value: 1
	 * 
	 * See C.8.11.3.1.2 for further explanation.
	 * 
	 * @return 1
	 */
	public String getRescaleSlope() {
		return dcmobj.getString(Tag.RescaleSlope);
	}

	/**
	 * Specifies the output units of Rescale Slope (0028,1053) and Rescale
	 * Intercept (0028,1052).
	 * 
	 * Enumerated Value: US = Unspecified
	 * 
	 * See C.8.11.3.1.2 for further explanation.
	 * 
	 * @param cs
	 *            US = Unspecified
	 */
	private void setRescaleType(String cs) {
		dcmobj.putString(Tag.RescaleType, VR.CS, cs);
	}

	/**
	 * Specifies the output units of Rescale Slope (0028,1053) and Rescale
	 * Intercept (0028,1052).
	 * 
	 * Enumerated Value: US = Unspecified
	 * 
	 * See C.8.11.3.1.2 for further explanation.
	 * 
	 * @return
	 */
	public String getRescaleType() {
		return dcmobj.getString(Tag.RescaleType);
	}

	/**
	 * Description Indicates any visual processing performed on the images prior
	 * to exchange.
	 * 
	 * See C.8.11.3.1.3 for further explanation.
	 * 
	 * @param lo
	 */
	public void setAcquisitionDeviceProcessingDescription(String lo) {
		dcmobj.putString(Tag.AcquisitionDeviceProcessingDescription, VR.LO, lo);
	}

	/**
	 * Description Indicates any visual processing performed on the images prior
	 * to exchange.
	 * 
	 * See C.8.11.3.1.3 for further explanation.
	 * 
	 * @return
	 */
	public String getAcquisitionDeviceProcessingDescription() {
		return dcmobj.getString(Tag.AcquisitionDeviceProcessingDescription);
	}

	/**
	 * Code representing the device-specific processing associated with the
	 * image (e.g. Organ Filtering code)
	 * 
	 * Note: This Code is manufacturer specific but provides useful annotation
	 * information to the knowledgeable observer.
	 * 
	 * @param lo
	 */
	public void setAcquisitionDeviceProcessingCode(String lo) {
		dcmobj.putString(Tag.AcquisitionDeviceProcessingCode, VR.LO, lo);
	}

	/**
	 * Code representing the device-specific processing associated with the
	 * image (e.g. Organ Filtering code)
	 * 
	 * Note: This Code is manufacturer specific but provides useful annotation
	 * information to the knowledgeable observer.
	 * 
	 * @return
	 */
	public String getAcquisitionDeviceProcessingCode() {
		return dcmobj.getString(Tag.AcquisitionDeviceProcessingCode);
	}


	/**
	 * Indicates whether a reference object (phantom) of known size is present
	 * in the image and was used for calibration.
	 * 
	 * 
	 * Enumerated Values:
	 * 
	 * YES NO
	 * 
	 * Device is identified using the Device module. See C.7.6.12 for further
	 * explanation.
	 * 
	 * @param cs
	 */
	public void setCalibrationImage(String cs) {
		dcmobj.putString(Tag.CalibrationImage, VR.CS, cs);
	}

	/**
	 * Indicates whether a reference object (phantom) of known size is present
	 * in the image and was used for calibration.
	 * 
	 * 
	 * Enumerated Values:
	 * 
	 * YES NO
	 * 
	 * Device is identified using the Device module. See C.7.6.12 for further
	 * explanation.
	 * 
	 * @return
	 */
	public String getCalibrationImage() {
		return dcmobj.getString(Tag.CalibrationImage);
	}

    public LUT[] getVOILUTs() {
        return LUT.toLUTs(dcmobj.get(Tag.VOILUTSequence));
    }

    public void setVOILUTs(LUT[] luts) {
        updateSequence(Tag.VOILUTSequence, luts);
    }    

	/**
	 * Defines a Window Center for display.
	 * 
	 * See C.8.11.3.1.5 for further explanation.
	 * 
	 * Required if Presentation Intent Type (0008,0068) is FOR PRESENTATION and
	 * VOI LUT Sequence (0028,3010) is not present. May also be present if VOI
	 * LUT Sequence (0028,3010) is present.
	 */
	public void setWindowCenter(float[] floats) {
		dcmobj.putFloats(Tag.WindowCenter, VR.DS, floats);
	}

	/**
	 * Defines a Window Center for display.
	 * 
	 * See C.8.11.3.1.5 for further explanation.
	 * 
	 * Required if Presentation Intent Type (0008,0068) is FOR PRESENTATION and
	 * VOI LUT Sequence (0028,3010) is not present. May also be present if VOI
	 * LUT Sequence (0028,3010) is present.
	 * 
	 * @return
	 */
	public float[] getWindowCenter() {
		return dcmobj.getFloats(Tag.WindowCenter);
	}

	/**
	 * Window Width for display.
	 * 
	 * See C.8.11.3.1.5 for further explanation.
	 * 
	 * Required if Window Center (0028,1050) is sent.
	 * 
	 * @param ds
	 */
	public void setWindowWidth(float[] floats) {
		dcmobj.putFloats(Tag.WindowWidth, VR.DS, floats);
	}

	/**
	 * Window Width for display.
	 * 
	 * See C.8.11.3.1.5 for further explanation.
	 * 
	 * Required if Window Center (0028,1050) is sent.
	 * 
	 * @return
	 */
	public float[] getWindowWidth() {
		return dcmobj.getFloats(Tag.WindowWidth);
	}

	/**
	 * Free form explanation of the meaning of the Window Center and Width.
	 * 
	 * Multiple values correspond to multiple Window Center and Width values.
	 * 
	 * @param lo
	 */
	public void setWindowCenterWidthExplanation(String lo) {
		dcmobj.putString(Tag.WindowCenterWidthExplanation, VR.LO, lo);
	}

	/**
	 * Free form explanation of the meaning of the Window Center and Width.
	 * 
	 * Multiple values correspond to multiple Window Center and Width values.
	 * 
	 * @return
	 */
	public String getWindowCenterWidthExplanation() {
		return dcmobj.getString(Tag.WindowCenterWidthExplanation);
	}


}
