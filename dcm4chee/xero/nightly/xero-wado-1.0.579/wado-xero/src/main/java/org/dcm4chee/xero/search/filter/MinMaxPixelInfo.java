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
package org.dcm4chee.xero.search.filter;

import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.image.VOIUtils;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.search.macro.MinMaxPixelMacro;
import org.dcm4chee.xero.search.macro.WindowLevelMacro;
import org.dcm4chee.xero.search.study.DicomObjectType;
import org.dcm4chee.xero.search.study.ImageBean;
import org.dcm4chee.xero.search.study.MacroItems;
import org.dcm4chee.xero.search.study.PatientType;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.dcm4chee.xero.search.study.SeriesType;
import org.dcm4chee.xero.search.study.StudyType;
import org.dcm4chee.xero.wado.DicomFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * If the minimum/maximum pixel value isn't already set, this class reads the
 * appropriate headers (hopefully already in memory), and gets the pixel range,
 * useable for window levelling etc.
 * 
 * @author bwallace
 * 
 */
public class MinMaxPixelInfo implements Filter<ResultsBean> {
   private static final Logger log = LoggerFactory.getLogger(MinMaxPixelInfo.class);

   public static final String MIN_MAX_PIXEL = "pixelInfo";

   /** Update any image beans with min/max pixel range information */
   public ResultsBean filter(FilterItem<ResultsBean> filterItem, Map<String, Object> params) {
     ResultsBean ret = filterItem.callNextFilter(params);
	  if (ret == null || !("true".equalsIgnoreCase((String) params.get(MIN_MAX_PIXEL))))
		 return ret;
	  log.debug("Adding min/max pixel info.");
	  for (PatientType pt : ret.getPatient()) {
		 for (StudyType st : pt.getStudy()) {
			for (SeriesType set : st.getSeries()) {
			   if (set == null) {
				  log.warn("Series should not be null...");
			   }
			   for (DicomObjectType dot : set.getDicomObject()) {
				  if (!(dot instanceof ImageBean))
					 continue;
				  ImageBean ib = (ImageBean) dot;
				  if (ib.getMacroItems().findMacro(MinMaxPixelMacro.class) != null)
					 continue;
				  log.debug("MinMaxPixelInfo on {}", ib.getObjectUID());
				  updateImage(filterItem, params, ib);
			   }
			}
		 }
	  }
	  return ret;
   }

   /**
    * This method uses the imageHeader filter to read the image header and then
    * calls the updatePixelRange on the DICOM version of this function.
    * 
    * @param fi
    * @param params
    * @param ib
    */
   protected void updateImage(FilterItem<?> fi, Map<String, Object> params, ImageBean ib) {
	  // Since we don't know what the dicom filter might add to the params,
	  // create a new one
	  DicomObject dobj = DicomFilter.callInstanceFilter(dicomImageHeader, params, ib.getObjectUID());
	  if (dobj == null) {
		 log.warn("Could not read dicom header for this object.");
		 return;
	  }
	  Integer frame = ib.getFrame();
	  MinMaxPixelMacro minMax = updatePixelRange(dobj, frame != null ? frame : 0, ib.getMacroItems());
	  if (minMax != null)
		 updateWindowLevel(dobj, null, frame != null ? frame : 0, ib.getMacroItems());
   }

   public static MinMaxPixelMacro updatePixelRange(DicomObject dobj, int frame, MacroItems macros) {
	  return updatePixelRange(dobj, null, frame, macros);
   }

   public static MinMaxPixelMacro updatePixelRange(DicomObject img, DicomObject pr, int frame, MacroItems macros) {
	  float[] cw = VOIUtils.getMinMaxWindowCenterWidth(img, pr, frame, null);
	  float minPixelValue = cw[0] - cw[1] / 2f;
	  float maxPixelValue = cw[0] + cw[1] / 2f;
	  MinMaxPixelMacro minMax = new MinMaxPixelMacro(minPixelValue, maxPixelValue);
	  macros.addMacro(minMax);
	  log.debug("Added {} to {}", minMax, macros);
	  return minMax;
   }

   private Filter<DicomObject> dicomImageHeader;

   /** Gets the filter that returns the dicom object image header */
	public Filter<DicomObject> getDicomImageHeader() {
   	return dicomImageHeader;
   }

	@MetaData(out="${ref:dicomImageHeader}")
	public void setDicomImageHeader(Filter<DicomObject> dicomImageHeader) {
   	this.dicomImageHeader = dicomImageHeader;
   }

   /**
    * This method updates the header with VOI window level defaults (if any)
    * using the first VOI LUT found. There should be some way of getting a
    * default window level if none is found...
    * 
    * @param minMax
    * @return The window level macro item added, or null if none.
    */
   public static WindowLevelMacro updateWindowLevel(DicomObject img, DicomObject pr, int frame, MacroItems macros) {
	  DicomObject voiObj = VOIUtils.selectVoiObject(img, pr, frame);
	  if (voiObj == null) {
		 log.debug("No VOI Object found - not setting window level info.");
		 return null;
	  }
	  String wc = voiObj.getString(Tag.WindowCenter);
	  String ww = voiObj.getString(Tag.WindowWidth);
	  if (wc == null || ww == null)
		 return null;
	  try {
		 float center = Float.parseFloat(wc);
		 float width = Float.parseFloat(ww);

		 WindowLevelMacro wl;
		 if (width == 0f) {
			log.debug("Image has no window width/center provided, not specifying WL.");
			return null;
		 } else {
			wl = new WindowLevelMacro(center, width, "");
			macros.addMacro(wl);
			return wl;
		 }
	  } catch (Exception e) {
		 log.warn("Caught exception when determining WL information:" + e);
		 return null;
	  }
   }
}
