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

package org.dcm4che2.iod.module.composite;

import java.util.Date;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.iod.module.macro.Code;
import org.dcm4che2.iod.module.macro.ImageSOPInstanceReferenceAndPurpose;
import org.dcm4che2.iod.module.macro.SOPInstanceReferenceAndPurpose;
import org.dcm4che2.iod.validation.ValidationContext;
import org.dcm4che2.iod.validation.ValidationResult;
import org.dcm4che2.iod.value.Flag;
import org.dcm4che2.iod.value.LossyImageCompression;
import org.dcm4che2.iod.value.PatientOrientation;
import org.dcm4che2.iod.value.PresentationLUTShape;

/**
 * Class to represent the General Image Module (C.7.6.1)
 * <p>
 * This class is the parent class for all Image Modules, as it contains
 * attributes that are image specific. It extends
 * {@link org.dcm4che2.iod.module.composite.ImagePixel}, so the child classes
 * have all necessary attributes to correctly describe images.
 * 
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Jun 9, 2006
 * 
 */
public class GeneralImageModule extends ImagePixel {

	public GeneralImageModule(DicomObject dcmobj) {
		super(dcmobj);
	}

	public void init() {
		super.init();
		setSamplesPerPixel(1);
		setPixelRepresentation(0);
	}

	public void validate(ValidationContext ctx, ValidationResult result) {
		super.validate(ctx, result);
		if (!PresentationLUTShape.isValidSoftCopy(getPresentationLUTShape())) {
			result.logInvalidValue(Tag.PRESENTATION_LUT_SHAPE, dcmobj);
		}
		if (!LossyImageCompression.isValid(getLossyImageCompression())) {
			result.logInvalidValue(Tag.LOSSY_IMAGE_COMPRESSION, dcmobj);
		}
		if (!Flag.isValid(getBurnedInAnnotation())) {
			result.logInvalidValue(Tag.BURNED_IN_ANNOTATION, dcmobj);
		}
	}

	/**
	 * A number that identifies this image.
	 * <p>
	 * Please not that this is an IS DICOM value, which is supposed to be
	 * encoded in JAVA as an int. Nevertheless, {@link String} has been chosen
	 * because:
	 * <ul>
	 * <li> I have already seen objects, which uses non-numeric values for this
	 * identifiers.
	 * <li>For identifiers, the non-numeric value may still of some
	 * use/information as opposed to e.g. a non-numeric Frame Number..
	 * </ul>
	 * <p>
	 * Type 2
	 * 
	 * @return
	 */
	public String getInstanceNumber() {
		return dcmobj.getString(Tag.INSTANCE_NUMBER);
	}

	/**
	 * A number that identifies this image.
	 * <p>
	 * Please not that this is an IS DICOM value, which is supposed to be
	 * encoded in JAVA as an int. Nevertheless, {@link String} has been chosen
	 * because:
	 * <ul>
	 * <li> I have already seen objects, which uses non-numeric values for this
	 * identifiers.
	 * <li>For identifiers, the non-numeric value may still of some
	 * use/information as opposed to e.g. a non-numeric Frame Number..
	 * </ul>
	 * <p>
	 * Type 2
	 * 
	 * @param s
	 */
	public void setInstanceNumber(String s) {
		dcmobj.putString(Tag.INSTANCE_NUMBER, VR.IS, s);
	}

	/**
	 * @see PatientOrientation
	 * @return
	 */
	public String[] getPatientOrientation() {
		return dcmobj.getStrings(Tag.PATIENT_ORIENTATION);
	}

	/**
	 * @see PatientOrientation
	 * @param s
	 */
	public void setPatientOrientation(String[] s) {
		dcmobj.putStrings(Tag.PATIENT_ORIENTATION, VR.CS, s);
	}

	public Date getContentDateTime() {
		return dcmobj.getDate(Tag.CONTENT_DATE, Tag.CONTENT_TIME);
	}

	public void setContentDateTime(Date d) {
		dcmobj.putDate(Tag.CONTENT_DATE, VR.DA, d);
		dcmobj.putDate(Tag.CONTENT_TIME, VR.TM, d);
	}

	public String[] getImageType() {
		return dcmobj.getStrings(Tag.IMAGE_TYPE);
	}

	public void setImageType(String[] s) {
		dcmobj.putStrings(Tag.IMAGE_TYPE, VR.CS, s);
	}

	/**
	 * A number identifying the single continuous gathering of data over a
	 * period of time that resulted in this image.
	 * <p>
	 * Please not that this is an IS DICOM value, which is supposed to be
	 * encoded in JAVA as an int. Nevertheless, {@link String} has been chosen
	 * because:
	 * <ul>
	 * <li> I have already seen objects, which uses non-numeric values for this
	 * identifiers.
	 * <li>For identifiers, the non-numeric value may still of some
	 * use/information as opposed to e.g. a non-numeric Frame Number..
	 * </ul>
	 * <p>
	 * Type 3
	 * 
	 * @return
	 */
	public String getAcquisitionNumber() {
		return dcmobj.getString(Tag.ACQUISITION_NUMBER);
	}

	/**
	 * A number identifying the single continuous gathering of data over a
	 * period of time that resulted in this image.
	 * <p>
	 * Please not that this is an IS DICOM value, which is supposed to be
	 * encoded in JAVA as an int. Nevertheless, {@link String} has been chosen
	 * because:
	 * <ul>
	 * <li> I have already seen objects, which uses non-numeric values for this
	 * identifiers.
	 * <li>For identifiers, the non-numeric value may still of some
	 * use/information as opposed to e.g. a non-numeric Frame Number..
	 * </ul>
	 * <p>
	 * Type 3
	 * 
	 * @param s
	 */
	public void setAcquisitionNumber(String s) {
		dcmobj.putString(Tag.ACQUISITION_NUMBER, VR.IS, s);
	}

	public Date getAcquisitionDateTime() {
		return dcmobj.getDate(Tag.ACQUISITION_DATE, Tag.ACQUISITION_TIME);
	}

	public void setAcquisitionDateTime(Date d) {
		dcmobj.putDate(Tag.ACQUISITION_DATE, VR.DA, d);
		dcmobj.putDate(Tag.ACQUISITION_TIME, VR.TM, d);
	}

	public Date getAcquisitionDatetime() {
		return dcmobj.getDate(Tag.ACQUISITION_DATETIME);
	}

	public void setAcquisitionDatetime(Date d) {
		dcmobj.putDate(Tag.ACQUISITION_DATETIME, VR.DT, d);
	}

	public ImageSOPInstanceReferenceAndPurpose[] getReferencedImages() {
		return ImageSOPInstanceReferenceAndPurpose
				.toImageSOPInstanceReferenceAndPurposes(dcmobj
						.get(Tag.REFERENCED_IMAGE_SEQUENCE));
	}

	public void setReferencedImages(ImageSOPInstanceReferenceAndPurpose[] sops) {
		updateSequence(Tag.REFERENCED_IMAGE_SEQUENCE, sops);
	}

	public String getDerivationDescription() {
		return dcmobj.getString(Tag.DERIVATION_DESCRIPTION);
	}

	public void setDerivationDescription(String s) {
		dcmobj.putString(Tag.DERIVATION_DESCRIPTION, VR.ST, s);
	}

	public Code[] getDerivationCodes() {
		return Code.toCodes(dcmobj.get(Tag.DERIVATION_CODE_SEQUENCE));
	}

	public void setDerivationCodes(Code[] codes) {
		updateSequence(Tag.DERIVATION_CODE_SEQUENCE, codes);
	}

	public SourceImage[] getSourceImages() {
		return SourceImage.toSourceImages(dcmobj.get(Tag.SOURCE_IMAGE_SEQUENCE));
	}

	public void setSourceImages(SourceImage[] sops) {
		updateSequence(Tag.SOURCE_IMAGE_SEQUENCE, sops);
	}

	public SOPInstanceReferenceAndPurpose[] getReferencedInstances() {
		return SOPInstanceReferenceAndPurpose
				.toSOPInstanceReferenceAndPurposes(dcmobj
						.get(Tag.REFERENCED_INSTANCE_SEQUENCE));
	}

	public void setReferencedInstances(SOPInstanceReferenceAndPurpose[] sops) {
		updateSequence(Tag.REFERENCED_INSTANCE_SEQUENCE, sops);
	}

	public int getImagesinAcquisition() {
		return dcmobj.getInt(Tag.IMAGES_IN_ACQUISITION);
	}

	public void setImagesinAcquisition(int i) {
		dcmobj.putInt(Tag.IMAGES_IN_ACQUISITION, VR.IS, i);
	}

	public String getImageComments() {
		return dcmobj.getString(Tag.IMAGE_COMMENTS);
	}

	public void setImageComments(String s) {
		dcmobj.putString(Tag.IMAGE_COMMENTS, VR.LT, s);
	}

	public String getQualityControlImage() {
		return dcmobj.getString(Tag.QUALITY_CONTROL_IMAGE);
	}

	public void setQualityControlImage(String s) {
		dcmobj.putString(Tag.QUALITY_CONTROL_IMAGE, VR.CS, s);
	}

	public String getBurnedInAnnotation() {
		return dcmobj.getString(Tag.BURNED_IN_ANNOTATION);
	}

	public void setBurnedInAnnotation(String s) {
		dcmobj.putString(Tag.BURNED_IN_ANNOTATION, VR.CS, s);
	}

	public String getLossyImageCompression() {
		return dcmobj.getString(Tag.LOSSY_IMAGE_COMPRESSION);
	}

	public void setLossyImageCompression(String s) {
		dcmobj.putString(Tag.LOSSY_IMAGE_COMPRESSION, VR.CS, s);
	}

	public float[] getLossyImageCompressionRatio() {
		return dcmobj.getFloats(Tag.LOSSY_IMAGE_COMPRESSION_RATIO);
	}

	public void setLossyImageCompression(float[] floats) {
		dcmobj.putFloats(Tag.LOSSY_IMAGE_COMPRESSION_RATIO, VR.DS, floats);
	}

	public String[] getLossyImageCompressionMethod() {
		return dcmobj.getStrings(Tag.LOSSY_IMAGE_COMPRESSION_METHOD);
	}

	public void setLossyImageCompressionMethod(String[] ss) {
		dcmobj.putStrings(Tag.LOSSY_IMAGE_COMPRESSION_METHOD, VR.CS, ss);
	}

	public ImagePixel getIconImage() {
		DicomObject item = dcmobj.getNestedDicomObject(Tag.ICON_IMAGE_SEQUENCE);
		return item != null ? new ImagePixel(item) : null;
	}

	public void setIconImage(ImagePixel icon) {
		updateSequence(Tag.ICON_IMAGE_SEQUENCE, icon);
	}

	public String getPresentationLUTShape() {
		return dcmobj.getString(Tag.PRESENTATION_LUT_SHAPE);
	}

	public void setPresentationLUTShape(String s) {
		dcmobj.putString(Tag.PRESENTATION_LUT_SHAPE, VR.CS, s);
	}

	public String getIrradiationEventUID() {
		return dcmobj.getString(Tag.IRRADIATION_EVENT_UID);
	}

	public void setIrradiationEventUID(String s) {
		dcmobj.putString(Tag.IRRADIATION_EVENT_UID, VR.UI, s);
	}

	public String getPixelDataProviderURL() {
		return dcmobj.getString(Tag.PIXEL_DATA_PROVIDER_URL);
	}

	public void setPixelDataProviderURL(String s) {
		dcmobj.putString(Tag.PIXEL_DATA_PROVIDER_URL, VR.UT, s);
	}
}
