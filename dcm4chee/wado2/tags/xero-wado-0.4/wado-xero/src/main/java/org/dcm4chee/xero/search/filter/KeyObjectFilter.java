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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.search.macro.KeyObjectMacro;
import org.dcm4chee.xero.search.study.ImageBean;
import org.dcm4chee.xero.search.study.ImageBeanMultiFrame;
import org.dcm4chee.xero.search.study.KeyObjectBean;
import org.dcm4chee.xero.search.study.KeySelection;
import org.dcm4chee.xero.search.study.MacroMixIn;
import org.dcm4chee.xero.search.study.PatientType;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.dcm4chee.xero.search.study.SeriesBean;
import org.dcm4chee.xero.search.study.StudyBean;
import org.dcm4chee.xero.search.study.StudyType;
import org.dcm4chee.xero.wado.DicomFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter adds an indication of whether an image is a key object. It takes
 * a single koUID=X as a parameter, and adds that key object to the set of key
 * objects in the return value. It then marks all images that are found that
 * exist in the key object set with the key object, including the GSPS UID if
 * any. Thus, this filter should run AFTER the GspsUid filter. Some specific
 * handling: 1. If an image is found more than once, with a different GSPS UID,
 * then it will be "cloned" to a new UID. 2. If an image has no GSPS, then
 * whatever GSPS the user applies by default will be shown.
 * 
 * This is not the filter to use to display the key object set as a series tray
 * level display,
 * 
 * @see{KeyObjectSeries} for a filter at the series level.
 * @author bwallace
 * 
 */
public class KeyObjectFilter implements Filter<ResultsBean> {
   private static final Logger log = LoggerFactory.getLogger(KeyObjectFilter.class);

   public static final String KEY_UID = "koUID";

   public ResultsBean filter(FilterItem<ResultsBean> filterItem, Map<String, Object> params) {
	  String koUid = (String) params.get(KEY_UID);
	  if (koUid == null || koUid.equals("")) {
		 return filterItem.callNextFilter(params);
	  }
	  ResultsBean ret = addKeyObjectMacro(filterItem, params, koUid);
	  if (ret == null)
		 return null;

	  return ret;
   }

   /**
     * Gets the default search return, and then calls the key object macro
     * assignment.
     * 
     * @param filterItem
     * @param params
     * @param koUid
     * @return
     */
   protected ResultsBean addKeyObjectMacro(FilterItem<ResultsBean> filterItem, Map<String, Object> params, String koUid) {
	  // Don't remove the koUID as it is part of the cache key still
	  ResultsBean ret = filterItem.callNextFilter(params);
	  if (ret == null)
		 return null;
	  for (int patientI = 0; patientI<ret.getPatient().size(); patientI++) {
		 PatientType patient = ret.getPatient().get(patientI);
		 for (StudyType studyType : patient.getStudy()) {
			StudyBean study = (StudyBean) studyType;
			String studyKo = findKeyObject(study, koUid);
			if (studyKo == null)
			   continue;
			KeyObjectMacro kom = (KeyObjectMacro) ret.getMacroItems().findMacro(KeyObjectMacro.class);
			if (kom != null) {
			   if (kom.getKeyObject().equals(studyKo)) {
				  log.error("Looking for different key object " + koUid + " but already found a key object:" + kom.getKeyObject());
			   }
			   return ret;
			}

			log.info("Adding key object information for studyKo=" + studyKo);
			KeyObjectBean kob = queryForKO(filterItem, params, studyKo, ret);
			if (kob == null) {
			   kom = new KeyObjectMacro("ERROR:Not found " + koUid);
			   log.warn("Key object " + studyKo + " not found.");
			   study.getMacroItems().addMacro(kom);
			   continue;
			}
			kom = new KeyObjectMacro(studyKo);
			study.getMacroItems().addMacro(kom);

			List<KeySelection> missing = assignKeyObjectMacro(ret, kom, kob.getKeySelection());
			if (missing != null && !missing.isEmpty() ) {
			   handleMissingItems(filterItem, params, ret, kom, missing);
			}
		 }
	  }
	  return ret;
   }

   /** Returns the key object to use for the given study */
   private String findKeyObject(StudyBean study, String koUid) {
	  if( ! koUid.equals("*") ) return koUid;
	  KeyObjectBean kob = (KeyObjectBean) study.searchStudy(koUid, "KO");
	  if( kob==null ) return null;
	  return kob.getObjectUID();
   }

   /** Assigns the key object macro instance for the listed selection */
   public static List<KeySelection> assignKeyObjectMacro(ResultsBean ret, KeyObjectMacro kom, List<KeySelection> selection) {
	  List<KeySelection> missing = null;

	  // Setup series map for this object.
	  KeyObjectBean kob = (KeyObjectBean) ret.getChildren().get(kom.getKeyObject());
	  if( kob==null ) throw new NullPointerException("Key object bean should not be null.");
	  StudyBean study = kob.getSeriesBean().getStudyBean();
	  Map<String, SeriesBean> availSeries = new HashMap<String, SeriesBean>();
	  for (Object set : study.getSeries()) {
		 SeriesBean se = (SeriesBean) set;
		 availSeries.put(se.getSeriesInstanceUID(), se);
	  }

	  for (KeySelection key : selection) {
		 MacroMixIn mmi = (MacroMixIn) ret.getChildren().get(key.getObjectUid());
		 if (mmi == null) {
			if (missing == null)
			   missing = new ArrayList<KeySelection>();
			log.info("Missing child " + key.getObjectUid());
			missing.add(key);
			continue;
		 }
		 if (key.getFrame() != 0) {
			ImageBeanMultiFrame image = (ImageBeanMultiFrame) mmi;
			mmi = image.getImageFrame(key.getFrame());
		 }
		 // Might have already added one of these - if so, don't do it again.
		 if (mmi.getMacroItems().findMacro(KeyObjectMacro.class) != null)
			continue;
		 mmi.getMacroItems().addMacro(kom);

		 // TODO: Fix: Can only move images over to the right study, not
            // other types of objects.
		 // at least at the moment.
		 if (mmi instanceof ImageBean) {
			ImageBean image = (ImageBean) mmi;
			SeriesBean imgSeries = image.getSeriesBean();
			SeriesBean series = availSeries.get(imgSeries.getSeriesInstanceUID());
			// Identity comparison is acceptable here.
			if (series == null) {
			   series = new SeriesBean(study, imgSeries);
			   series.getDicomObject().clear();
			   series.setViewable(null);
			   study.getSeries().add(series);
			   log.info("Creating new synthetic series for key objects in " + study.getStudyInstanceUID() + " for series "
					 + series.getSeriesInstanceUID());
			   ret.getChildren().put("ko" + imgSeries.getSeriesInstanceUID(), series);
			   availSeries.put(series.getSeriesInstanceUID(), series);
			}
			if (series != imgSeries) {
			   log.info("Adding existing image to synthetic series.");
			   ImageBean addImage = image.clone(null);
			   addImage.setSeriesBean(series);
			   addImage.setPosition(null);
			   series.getDicomObject().add(addImage);
			}
		 }
		 if (key.getGspsUid() != null) {
			ImageBean image = (ImageBean) mmi;
			image.setGspsUID(key.getGspsUid());
		 }
	  }
	  return missing;
   }

   /** Handle any missing items - by default, does nothing */
   protected void handleMissingItems(FilterItem<ResultsBean> filterItem, Map<String, Object> params, ResultsBean ret, KeyObjectMacro kom,
		 List<KeySelection> missing) {
   }

   /**
     * This method queries for the specified KO object, adding it to the result.
     * Only one key object can be specified at a time. The object will be
     * returned, as well as being added to the results bean object.
     */
   public static KeyObjectBean queryForKO(FilterItem<ResultsBean> filterItem, Map<String, Object> params, String koUid, ResultsBean rb) {
	  DicomObject dobj = DicomFilter.filterDicomObject(filterItem, null, koUid);
	  if (dobj == null)
		 return null;
	  KeyObjectBean kob = (KeyObjectBean) rb.getChildren().get(koUid);
	  if (kob == null) {
		 rb.addResult(dobj);
		 kob = (KeyObjectBean) rb.getChildren().get(koUid);
		 assert kob != null;
	  } else {
		 kob.initKeySelection(dobj);
	  }
	  return kob;
   }

}
