package org.dcm4che2.iod.module.lut;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.iod.module.composite.GeneralImageModule;

/** Provide access to the various types of LUTs available in images,
 * both as raw values, and as ILut implementations that perform the lookups.
 * @author bwallace
 * @version $Revision$ $Date$
 * @since July 15, 2007

 */
public class LutModule extends GeneralImageModule {

	/** Create a LUT module object */
	public LutModule(DicomObject dcmobj) {
		super(dcmobj);
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
	public void setRescaleIntercept(float intercept) {
		dcmobj.putFloat(Tag.RescaleIntercept, VR.DS, intercept);
	}

	/**
	 * Get the rescale intercept value.
	 * @return Rescale intercept value.
	 */
	public float getRescaleIntercept() {
		return dcmobj.getFloat(Tag.RescaleIntercept);
	}

	/**
	 * m in the equation specified by Rescale Intercept (0028,1052).
	 * See C.8.11.3.1.2 for further explanation.
	 * 
	 * @param slope for the rescale intercept.
	 */
	public void setRescaleSlope(float slope) {
		dcmobj.putFloat(Tag.RescaleSlope, VR.DS, slope);
	}

	/**
	 * m in the equation specified by Rescale Intercept (0028,1052).
	 * 
	 * @return the recale slope.
	 */
	public float getRescaleSlope() {
		return dcmobj.getFloat(Tag.RescaleSlope);
	}

	/**
	 * Specifies the output units of Rescale Slope (0028,1053) and Rescale
	 * Intercept (0028,1052).
	 * <p>
	 * Enumerated Value: US = Unspecified
	 * Enumerated Value: OD = Optical Density
	 * Enumerated Value: HU = Houndsfield Units
	 * <p>
	 * See C.8.11.3.1.2 for further explanation.
	 * 
	 * @param type, US = Unspecified, OD optical density, HU houndsfield units.
	 */
	public void setRescaleType(String type) {
		dcmobj.putString(Tag.RescaleType, VR.CS, type);
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
		return dcmobj.getString(Tag.RescaleType);
	}

	/** Gets the modality LUT to use 
	 * @todo implement setModalityLut to set the modality lut instead.
	 */
	public ILut getModalityLut() {
		if (dcmobj.contains(Tag.RescaleSlope)) {
			return new RescaleLut(getRescaleSlope(), getRescaleIntercept(),
					getRescaleType());
		}
		return null;
	}

	/** Gets the named VOI LUT.  Null means the first one found.
	 * Throws a runtime exception if the explanation for a window level isn't associated
	 * with a window center and width (either they aren't present, or there aren't
	 * enough elemetns.)
	 * @param name to look for the VOI LUT for.
	 * @return the VOI LUT with explanation the same as name, or null if not found.
	 */
	public ILut getVOILut(String name) {
		String[] wlExplanations = getWindowCenterWidthExplanations();
		if (wlExplanations != null && wlExplanations.length > 0) {
			if (name == null)
				return new WindowLevelLut(getWindowCenter()[0],
						getWindowWidth()[0], wlExplanations[0]);
			for (int i = 0; i < wlExplanations.length; i++) {
				if (name.equals(wlExplanations[i])) {
					return new WindowLevelLut(getWindowCenter()[i],
							getWindowWidth()[i], wlExplanations[i]);
				}
			}
		}
		LUT[] luts = getVOILUTs();
		if (luts == null || luts.length == 0)
			return null;
		if (name == null) {
			throw new UnsupportedOperationException(
					"Still need to implement Lut class generally.");
		}
		for (int i = 0; i < luts.length; i++) {
			LUT lut = luts[i];
			if (name.equals(lut.getLUTExplanation())) {
				throw new UnsupportedOperationException(
						"Still need to implement Lut class generally.");
			}
		}
		// Not found.
		return null;
	}

	public LUT[] getVOILUTs() {
		return LUT.toLUTs(dcmobj.get(Tag.VOILUTSequence));
	}

	public void setVOILUTs(LUT[] luts) {
		updateSequence(Tag.VOILUTSequence, luts);
	}

	/**
	 * Defines a Window Center for display.
	 * <p>
	 * See C.8.11.3.1.5 for further explanation.
	 * <p>
	 * Required if Presentation Intent Type (0008,0068) is FOR PRESENTATION and
	 * VOI LUT Sequence (0028,3010) is not present. May also be present if VOI
	 * LUT Sequence (0028,3010) is present.
	 * @param centers associated with each window level (explanation).
	 */
	public void setWindowCenter(float[] centers) {
		dcmobj.putFloats(Tag.WindowCenter, VR.DS, centers);
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
		return dcmobj.getFloats(Tag.WindowCenter);
	}

	/**
	 * Window Width for display.
	 * <p>
	 * See C.8.11.3.1.5 for further explanation.
	 * <p>
	 * Required if Window Center (0028,1050) is sent.
	 * 
	 * @param windowWidths are the window widths to put into the dicom object.
	 */
	public void setWindowWidth(float[] widths) {
		dcmobj.putFloats(Tag.WindowWidth, VR.DS, widths);
	}

	/**
	 * Window Width for display.
	 * <p>
	 * See C.8.11.3.1.5 for further explanation.
	 * <p>
	 * Required if Window Center (0028,1050) is sent.
	 * 
	 * @return An array of window widths corresponding to the window center/width explanation.
	 */
	public float[] getWindowWidth() {
		return dcmobj.getFloats(Tag.WindowWidth);
	}

	/**
	 * Free form explanation of the meaning of the Window Center and Width.
	 * <p>
	 * Multiple values correspond to multiple Window Center and Width values.
	 * 
	 * @param explanations to associate with window levels.
	 */
	public void setWindowCenterWidthExplanation(String[] explanations) {
		dcmobj
				.putStrings(Tag.WindowCenterWidthExplanation, VR.LO,
						explanations);
	}

	/**
	 * Free form explanation of the meaning of the Window Center and Width.
	 * <p>
	 * Multiple values correspond to multiple Window Center and Width values.
	 * 
	 * @return an array of explanatory names for each window level (width/center)
	 */
	public String[] getWindowCenterWidthExplanations() {
		return dcmobj.getStrings(Tag.WindowCenterWidthExplanation);
	}

}
