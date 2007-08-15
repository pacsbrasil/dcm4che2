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
 * Bill Wallace, Agfa HealthCare Inc., 
 * Portions created by the Initial Developer are Copyright (C) 2007
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Bill Wallace <bill.wallace@agfa.com>
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
package org.dcm4chee.xero.image;

import java.util.HashMap;
import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.iod.module.lut.ILut;
import org.dcm4che2.iod.module.lut.LutModule;
import org.dcm4che2.iod.module.lut.RescaleLut;
import org.dcm4che2.iod.module.lut.VoiLutModule;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.search.study.DicomObjectType;
import org.dcm4chee.xero.search.study.ImageBean;
import org.dcm4chee.xero.search.study.PatientType;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.dcm4chee.xero.search.study.SeriesType;
import org.dcm4chee.xero.search.study.StudyType;
import org.dcm4chee.xero.search.study.WindowLevelMacro;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * If the minimum/maximum pixel value isn't already set, this class reads the appropriate
 * headers (hopefully already in memory), and gets the pixel range, useable for window levelling
 * etc.
 * @author bwallace
 *
 */
public class MinMaxPixelInfo  implements Filter<ResultsBean> {
    private static final Logger log = LoggerFactory.getLogger(MinMaxPixelInfo.class);
    
    /** Update any image beans with min/max pixel range information */
	public ResultsBean filter(FilterItem filterItem, Map<String, Object> params) {
		ResultsBean ret = (ResultsBean) filterItem.callNextFilter(params);
		if( ret==null ) return null;
		for( PatientType pt : ret.getPatient() ) {
			for(StudyType st : pt.getStudy() ) {
				for(SeriesType set : st.getSeries() ) {
					for(DicomObjectType dot : set.getDicomObject() ) {
						if( ! (dot instanceof ImageBean) ) continue;
						ImageBean ib = (ImageBean) dot;
						// If it has already had information added, eg from GSPS,
						// then don't update it again (ie don't read the header.)
						if( ib.getMacroItems().findMacro(MinMaxPixelMacro.class)!=null ) continue;
						log.info("MinMaxPixelInfo on "+ib.getSOPInstanceUID());
						updateImage(filterItem, params, ib);
					}
				}
			}
		}
		return ret;
	}

	/** This method uses the imageHeader filter to read the image header and then calls
	 * the updatePixelRange on the DICOM version of this function.
	 * @param fi
	 * @param params
	 * @param ib
	 */
	protected void updateImage(FilterItem fi, Map<String,Object> params, ImageBean ib) {
		// Since we don't know what the dicom filter might add to the params, create a new one
		Map<String,Object> newParams = new HashMap<String,Object>(params);
		newParams.put("objectUID", ib.getSOPInstanceUID());
		DicomObject dobj = (DicomObject) fi.callNamedFilter("dicom",newParams);
		if( dobj==null ) {
			log.warn("Could not read dicom header for this object.");
			return;
		}
		MinMaxPixelMacro minMax = updatePixelRange(dobj,ib);
		if( minMax!=null ) updateWindowLevel(dobj,ib, minMax);
	}
	
	/** This method takes an existing dicom object, and reads the appropriate min/max pixel values 
	 * and adds the information to the image bean.
	 */
	public static MinMaxPixelMacro updatePixelRange(DicomObject dobj, ImageBean ib) {
		LutModule lm = new LutModule(dobj);
		// It is harder to handle more samples, as then we need to know about the mode - so, ignore this for now.
		if( lm.getSamplesPerPixel()!=1 ) return null;
		
		ILut lut = lm.getModalityLutModule().getModalityLut();
		if(lut==null ) {
			log.debug("No modality LUT found for "+ib.getSOPInstanceUID());
			lut = new RescaleLut(1,0, "Identity");
		}
		float minPixelValue = lut.lookup(lm.minPossibleStoredValue());
		float maxPixelValue = lut.lookup(lm.maxPossibleStoredValue());
		MinMaxPixelMacro minMax = new MinMaxPixelMacro(minPixelValue, maxPixelValue);
		ib.getMacroItems().addMacro(minMax);
		return minMax;
	}
	
	/** This method updates the header with VOI  window level defaults (if any) using the first
	 * VOI LUT found, or a window level such that the entire image data is shown.
	 * TODO Change this to provide a voi lut name when there is a provided VOI Lut.
	 * @param minMax 
	 */
	public static void updateWindowLevel(DicomObject dobj, ImageBean ib, MinMaxPixelMacro minMax) {
		VoiLutModule voi = new VoiLutModule(dobj);
		float[] centers = voi.getWindowCenter();
	    float[] widths = voi.getWindowWidth();
		WindowLevelMacro wl;
		if( centers==null || widths==null ) {
		   wl = new WindowLevelMacro((minMax.getMaxPixel()+minMax.getMinPixel()+1)/2, minMax.getMaxPixel()-minMax.getMinPixel()+1,"Pixel Range WL");
		}
		else {
		   String[] explanations = voi.getWindowCenterWidthExplanations(); 
		   wl = new WindowLevelMacro(centers[0],widths[0],explanations[0]);
		}
		ib.getMacroItems().addMacro(wl);
	}
}
