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
import org.dcm4che2.iod.module.Module;

/**
 * 
 * A specialized class that represents the DX Image Module.
 * 
 * Table C.8-70 contains IOD Attributes that describe a DX Image by specializing
 * Attributes of the General Image and Image Pixel Modules, and adding
 * additional Attributes.
 * 
 * @author Antonio Magni <dcm4ceph@antoniomagni.org>
 * 
 */
public class DXImageModule extends Module {

	public DXImageModule(DicomObject dcmobj) {
		super(dcmobj);

		// From DICOM docs: Number of samples in this image. Shall have an
		// Enumerated Value of 1.
		setSamplesperPixel(1);

		// Data representation of the pixel samples. Shall have the Enumerated
		// Value: 0000H = Unsigned Integer.
		setPixelRepresentation(0x0000);

		// The value b in the relationship between
		// stored values (SV) in Pixel Data
		// (7FE0,0010) and the output units specified
		// in Rescale Type (0028,1054).
		// Output units = m*SV + b.
		// Enumerated Value: 0
		// See C.8.11.3.1.2 for further explanation.
		setRescaleIntercept("0");

		// m in the equation specified by Rescale
		// Intercept (0028,1052).
		// Enumerated Value: 1
		// See C.8.11.3.1.2 for further explanation.
		setRescaleSlope("1");

		// Specifies the output units of Rescale Slope
		// (0028,1053) and Rescale Intercept
		// (0028,1052).
		// Enumerated Value: US = Unspecified
		// See C.8.11.3.1.2 for further explanation.
		setRescaleType("US = Unspecified");
	}

	/**
	 * Image identification characteristics.
	 * 
	 * See C.8.11.3.1.1 for specialization.
	 * 
	 * @param s
	 */
	public void setImageType(String s) {
		dcmobj.putString(Tag.ImageType, VR.CS, s);
	}

	/**
	 * Image identification characteristics.
	 * 
	 * See C.8.11.3.1.1 for specialization.
	 * 
	 * @return
	 */
	public String getImageType() {
		return dcmobj.getString(Tag.ImageType);
	}

	/**
	 * Number of samples in this image. Shall have an Enumerated Value of 1.
	 * 
	 * @param i
	 *            Shall have an Enumerated Value of 1.
	 */
	private void setSamplesperPixel(int i) {
		dcmobj.putInt(Tag.SamplesperPixel, VR.US, i);
	}

	/**
	 * Number of samples in this image. Shall have an Enumerated Value of 1.
	 * 
	 * The setter of this method has been declared private, since this value
	 * cannot be anything else than 1. So, no option to change it.
	 * 
	 * @return will return 1.
	 */
	public int getSamplesperPixel() {
		return dcmobj.getInt(Tag.SamplesperPixel);
	}

	/**
	 * Specifies the intended interpretation of the pixel data.
	 * 
	 * @param s
	 *            Enumerated Values: MONOCHROME1 MONOCHROME2
	 */
	public void setPhotometricInterpretation(String s) {
		if (s != "MONOCHROME1" || s != "MONOCRHOME2")
			throw new UnsupportedOperationException(
					s
							+ " is not a permitted Photometric Interpretation enumerated value."
							+ "Must be either MONOCHROME1 or MONOCRHOME2");

		dcmobj.putString(Tag.PhotometricInterpretation, VR.CS, s);
	}

	/**
	 * Returns the intended interpretation of the pixel data.
	 * 
	 * @return
	 */
	public String getPhotometricInterpretation() {
		return dcmobj.getString(Tag.PhotometricInterpretation);
	}

	public void setBitsAllocated(int us) {
		if (us != 8 || us != 16)
			throw new UnsupportedOperationException(us
					+ " is not a permitted Bits Allocated enumerated Value."
					+ " Must be either 8 or 16.");

		dcmobj.putInt(Tag.BitsAllocated, VR.US, us);
	}

	public int getBitsAllocated() {
		return dcmobj.getInt(Tag.BitsAllocated);
	}

	/**
	 * Number of bits stored for each pixel sample.
	 * 
	 * Enumerated Values: 6 to 16
	 * 
	 * @param us
	 */
	public void setBitsStored(int us) {
		// TODO Makre sure this is not a mistake in DICOM, that only 6 and are
		// allowed.
		if (us != 6 || us != 16)
			throw new UnsupportedOperationException(us
					+ " is not a permitted Bits Stored enumerated Value."
					+ " Must be either 6 or 16.");

		dcmobj.putInt(Tag.BitsStored, VR.US, us);
	}

	public int getBitsStored() {
		return dcmobj.getInt(Tag.BitsStored);
	}

	/**
	 * Most significant bit for pixel sample data.
	 * 
	 * Shall have an Enumerated Value of one less than the value in Bit Stored
	 * (0028,0101).
	 * 
	 * @param us
	 */
	public void setHighBit(int us) {
		dcmobj.putInt(Tag.HighBit, VR.US, us);
	}

	/**
	 * Data representation of the pixel samples.
	 * 
	 * Shall have the Enumerated Value: 0000H = Unsigned Integer.
	 * 
	 * @param us
	 */
	private void setPixelRepresentation(int us) {
		dcmobj.putInt(Tag.PixelRepresentation, VR.US, us);
	}

	public int getPixelRepresentation() {
		return dcmobj.getInt(Tag.PixelRepresentation);
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
		if (s != "LIN" || s != "LOG")
			throw new UnsupportedOperationException(
					s
							+ " is not a permitted Photometric Interpretation enumerated value."
							+ "Must be either LIN or LOG");
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
		if (ss != 1 || ss != -1)
			throw new UnsupportedOperationException(
					ss
							+ " is not a permitted Pixel Intensity Relationship Sign enumerated Value."
							+ " Must be either 1 or -1.");

		dcmobj.putInt(Tag.PixelIntensityRelationshipSign, VR.SS, ss);
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
	 * @return
	 */
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
	private void setRescaleIntercept(String ds) {
		dcmobj.putString(Tag.RescaleIntercept, VR.DS, ds);
	}

	/**
	 * m in the equation specified by Rescale Intercept (0028,1052).
	 * 
	 * Enumerated Value: 1
	 * 
	 * See C.8.11.3.1.2 for further explanation.
	 * 
	 * @param ds
	 *            1
	 */
	private void setRescaleSlope(String ds) {
		dcmobj.putString(Tag.RescaleSlope, VR.DS, ds);
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
	 * Specifies an identity transformation for the Presentation LUT, other than
	 * to account for the value of Photometric Interpretation (0028,0004), such
	 * that the output of all grayscale transformations defined in the IOD
	 * containing this Module are defined to be P-Values.
	 * 
	 * Enumerated Values:
	 * 
	 * IDENTITY - output is in P-Values - shall be used if Photometric
	 * Interpretation (0028,0004) is MONOCHROME2.
	 * 
	 * INVERSE - output after inversion is in P-Values - shall be used if
	 * Photometric Interpretation (0028,0004) is MONOCHROME1.
	 * 
	 * See C.8.11.3.1.2 for further explanation.
	 * 
	 * @param cs
	 */
	public void setPresentationLUTShape(String cs) {
		if (cs != "IDENTITY" || cs != "INVERSE")
			throw new UnsupportedOperationException(
					cs
							+ " is not a permitted Presentation LUT Shape enumerated value."
							+ "Must be either LIN or LOG");

		dcmobj.putString(Tag.PresentationLUTShape, VR.CS, cs);

		// TODO Automatically change/set Photometric Interpretation and vice
		// versa according to DICOM
	}

	/**
	 * Specifies an identity transformation for the Presentation LUT, other than
	 * to account for the value of Photometric Interpretation (0028,0004), such
	 * that the output of all grayscale transformations defined in the IOD
	 * containing this Module are defined to be P-Values.
	 * 
	 * Enumerated Values:
	 * 
	 * IDENTITY - output is in P-Values - shall be used if Photometric
	 * Interpretation (0028,0004) is MONOCHROME2.
	 * 
	 * INVERSE - output after inversion is in P-Values - shall be used if
	 * Photometric Interpretation (0028,0004) is MONOCHROME1.
	 * 
	 * See C.8.11.3.1.2 for further explanation.
	 * 
	 * @return
	 */
	public String getPresentationLUTShape() {
		return dcmobj.getString(Tag.PresentationLUTShape);
	}

	/**
	 * Specifies whether an Image has undergone lossy compression.
	 * 
	 * Enumerated Values:
	 * 
	 * 00 = Image has NOT been subjected to lossy compression.
	 * 
	 * 01 = Image has been subjected to lossy compression.
	 * 
	 * See C.7.6.1.1.5 for further explanation.
	 * 
	 * @param cs
	 */
	public void setLossyImageCompression(String cs) {
		if (cs != "00" || cs != "01")
			throw new UnsupportedOperationException(
					cs
							+ " is not a permitted Lossy Image Compression enumerated value."
							+ "Must be either 00 or 01");

		dcmobj.putString(Tag.LossyImageCompression, VR.CS, cs);
	}

	/**
	 * Specifies whether an Image has undergone lossy compression.
	 * 
	 * Enumerated Values:
	 * 
	 * 00 = Image has NOT been subjected to lossy compression.
	 * 
	 * 01 = Image has been subjected to lossy compression.
	 * 
	 * See C.7.6.1.1.5 for further explanation.
	 * 
	 * @return
	 */
	public String getLossyImageCompression() {
		return dcmobj.getString(Tag.LossyImageCompression);
	}

	/**
	 * See C.7.6.1.1.5 for further explanation.
	 * 
	 * Required if Lossy Compression has been performed on the Image.
	 * 
	 * @param ds
	 */
	public void setLossyImageCompressionRatio(String ds) {
		dcmobj.putString(Tag.LossyImageCompressionRatio, VR.DS, ds);
	}

	/**
	 * See C.7.6.1.1.5 for further explanation.
	 * 
	 * Required if Lossy Compression has been performed on the Image.
	 * 
	 * @return
	 */
	public String getLossyImageCompressionRatio() {
		return dcmobj.getString(Tag.LossyImageCompressionRatio);
	}

	/**
	 * A text description of how this image was derived.
	 * 
	 * See C.8.11.3.1.4 for further explanation.
	 * 
	 * @param st
	 */
	public void setDerivationDescription(String st) {
		dcmobj.putString(Tag.DerivationDescription, VR.ST, st);
	}

	/**
	 * A text description of how this image was derived.
	 * 
	 * See C.8.11.3.1.4 for further explanation.
	 * 
	 * @return
	 */
	public String getDerivationDescription() {
		return dcmobj.getString(Tag.DerivationDescription);
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
	 * Patient direction of the rows and columns of the image.
	 * 
	 * See C.7.6.1.1.1 for further explanation.
	 * 
	 * @param cs
	 */
	public void setPatientOrientation(String cs) {
		dcmobj.putString(Tag.PatientOrientation, VR.CS, cs);
	}

	/**
	 * Patient direction of the rows and columns of the image.
	 * 
	 * See C.7.6.1.1.1 for further explanation.
	 * 
	 * @return
	 */
	public String getPatientOrientation() {
		return dcmobj.getString(Tag.PatientOrientation);
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
		if (cs != "YES" || cs != "NO")
			throw new UnsupportedOperationException(cs
					+ " is not a permitted Calibration Image enumerated value."
					+ "Must be either YES or NO");

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

	/**
	 * Indicates whether or not image contains sufficient burned in annotation
	 * to identify the patient and date the image was acquired.
	 * 
	 * Enumerated Values:
	 * 
	 * YES
	 * 
	 * NO
	 * 
	 * @param cs
	 */
	public void setBurnedInAnnotation(String cs) {
		if (cs != "YES" || cs != "NO")
			throw new UnsupportedOperationException(
					cs
							+ " is not a permitted Burned In Annotation enumerated value."
							+ "Must be either YES or NO");

		dcmobj.putString(Tag.BurnedInAnnotation, VR.CS, cs);
	}

	/**
	 * Indicates whether or not image contains sufficient burned in annotation
	 * to identify the patient and date the image was acquired.
	 * 
	 * Enumerated Values:
	 * 
	 * YES
	 * 
	 * NO
	 * 
	 * @return
	 */
	public String getBurnedInAnnotation() {
		return dcmobj.getString(Tag.BurnedInAnnotation);
	}

	// TODO The entire VOI LUT Sequence is missing. It should go in here.

	/**
	 * Defines a Window Center for display.
	 * 
	 * See C.8.11.3.1.5 for further explanation.
	 * 
	 * Required if Presentation Intent Type (0008,0068) is FOR PRESENTATION and
	 * VOI LUT Sequence (0028,3010) is not present. May also be present if VOI
	 * LUT Sequence (0028,3010) is present.
	 */
	public void setWindowCenter(String cs) {
		dcmobj.putString(Tag.WindowCenter, VR.CS, cs);
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
	public String getWindowCenter() {
		return dcmobj.getString(Tag.WindowCenter);
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
	public void setWindowWidth(String ds) {
		dcmobj.putString(Tag.WindowWidth, VR.DS, ds);
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
	public String getWindowWidth() {
		return dcmobj.getString(Tag.WindowWidth);
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
