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

import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.search.ResultFromDicom;
import org.dcm4chee.xero.search.study.DicomObjectType;
import org.dcm4chee.xero.search.study.GspsType;
import org.dcm4chee.xero.search.study.PatientType;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.dcm4chee.xero.search.study.SeriesType;
import org.dcm4chee.xero.search.study.StudyBean;
import org.dcm4chee.xero.search.study.StudyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a study/series level filter that performs lookups to figure out the default
 * GSPS object to apply to a study, if any. There are 3 ways to specify GSPS
 * status: gsps=* will find the latest GSPS and attempt to apply it.
 * 
 * gsps=<NAME>{,<NAME>}* will find the first named GSPS matching the pattern,
 * and attempt to apply it.
 * 
 * @author bwallace
 */
public class GspsDiscover implements Filter<ResultsBean> {
   private static final String[] STRING_ARRAY_TYPE = new String[0];

   private static final Logger log = LoggerFactory.getLogger(GspsDiscover.class);

   public static final String GSPS_KEY = "gsps";
   
   public GspsDiscover() {
	   log.info("Allocated a GspsDiscover.");
   }

   public ResultsBean filter(FilterItem<ResultsBean> filterItem, Map<String, Object> params) {
	  log.debug("Discovering if any GSPS is applicable.");
	  String gspsNames = (String) params.get(GSPS_KEY);
	  if (gspsNames == null) {
		 log.debug("Not applying GSPS discovery.");
		 return (ResultsBean) filterItem.callNextFilter(params);
	  }
	  log.debug("Looking for GSPS " + gspsNames + " - TODO fix name, looking for * right now.");
	  ResultsBean rb = (ResultsBean) filterItem.callNextFilter(params);
	  if (rb == null)
		 return null;
	  ResultsBean gspsResults = queryForGsps(imageSource, rb, null);
	  if (gspsResults == null)
		 return rb;
	  for (PatientType pt : rb.getPatient()) {
		 for (StudyType st : pt.getStudy()) {
			GspsType gspsType = findMostRecentGsps((StudyBean) st, gspsResults, gspsNames);
			if (gspsType != null) {
			   log.debug("Setting GSPS to apply for " + st.getStudyUID() + " to " + gspsType.getContentLabel());
			   st.setGspsLabel(gspsType.getContentLabel());
			}
		 }
	  }
	  return rb;
   }

   /** Finds the most recent GSPS matching the given content names */
   private GspsType findMostRecentGsps(StudyBean st, ResultsBean gspsResults, String gspsNames) {
	  log.debug("Looking for GSPS for study " + st.getStudyUID());
	  StudyBean gspsSb = (StudyBean) gspsResults.getChildren().get(st.getId());
	  if (gspsSb == null) {
		 log.debug("Didn't find any GSPS objects for study.");
		 return null;
	  }
	  int count = 0;
	  GspsType foundGsps = null;
	  for (SeriesType set : gspsSb.getSeries()) {
		 for (DicomObjectType dot : set.getDicomObject()) {
			if (!(dot instanceof GspsType)) {
			   log.warn("Found a non-GSPS child when looking at most recent PR:" + dot.getClass());
			   continue;
			}
			GspsType gsps = (GspsType) dot;
			count++;
			if (foundGsps == null) {
			   foundGsps = gsps;
			} else {
			   if (gsps.getPresentationDateTime().compare(foundGsps.getPresentationDateTime()) > 0) {
				  foundGsps = gsps;
			   }
			}
		 }
	  }
	  log.debug("Found " + count + " gsps objects for study " + st.getStudyUID() + " name:"
			+ (foundGsps != null ? foundGsps.getContentLabel() : " no GSPS"));
	  return foundGsps;
   }

   /**
     * This method queries for PR objects associated with any study in the
     * results bean, returning a new results bean object.  Will return null if no studies have PR objects.
     */
   public static ResultsBean queryForGsps(Filter<ResultFromDicom> imageSource, ResultsBean rb, Object presentationUID) {
	  Map<String, Object> prParams = new HashMap<String, Object>();
	  List<String> uids = new ArrayList<String>();
	  for (PatientType pt : rb.getPatient()) {
		 for (StudyType st : pt.getStudy()) {
			String modality = st.getModalitiesInStudy();
			if( modality.indexOf("PR")<0 ) continue;
			uids.add(st.getStudyUID());
		 }
	  }
	  if (uids.size() == 0)
		 return null;
	  prParams.put("Modality", "PR");
	  prParams.put("studyUID", uids.toArray(STRING_ARRAY_TYPE));
	  FilterUtil.computeQueryString(prParams, "Modality", "studyUID");
	  log.debug("Doing a search on {} Study UID's for PR objects uid[0]={}", uids.size(), uids.get(0));
	  ResultsBean gspsRB = (ResultsBean) imageSource.filter(null,prParams);
	  return gspsRB;
   }

   private Filter<ResultFromDicom> imageSource;

	public Filter<ResultFromDicom> getImageSource() {
   	return imageSource;
   }

	/**
	 * Sets the filter to use for an image search.
	 * @param imageSource
	 */
	@MetaData(out="${ref:imageSource}")
	public void setImageSource(Filter<ResultFromDicom> imageSource) {
   	this.imageSource = imageSource;
   }

}
