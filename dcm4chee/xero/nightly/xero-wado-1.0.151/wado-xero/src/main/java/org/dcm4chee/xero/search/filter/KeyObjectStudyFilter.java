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
 * Sebastian Mohan, Agfa HealthCare Inc., 
 * Portions created by the Initial Developer are Copyright (C) 2007
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Sebastian Mohan <sebastian.mohan@agfa.com>
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

import java.util.HashMap;
import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.MemoryCacheFilter;
import org.dcm4chee.xero.search.ResultFromDicom;
import org.dcm4chee.xero.search.macro.KeyObjectGspsMacro;
import org.dcm4chee.xero.search.study.ImageSearch;
import org.dcm4chee.xero.search.study.KeyObjectBean;
import org.dcm4chee.xero.search.study.KeySelection;
import org.dcm4chee.xero.search.study.PatientType;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.dcm4chee.xero.search.study.StudyBean;
import org.dcm4chee.xero.search.study.StudyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter adds information whether any GSPS asscoiated to the images
 * referred by the Key object. It set's at attribute kogsps=true if present.
 * 
 * @author smohan
 * 
 */
public class KeyObjectStudyFilter implements Filter<ResultsBean> {

	private static final String STUDY_UID = "studyUID";

	private static final String KO = "ko";

	private Filter<DicomObject> dicomFullHeader;

	private Filter<ResultFromDicom> imageSearch;

	@Override
	public ResultsBean filter(FilterItem<ResultsBean> filterItem,
			Map<String, Object> params) {
		String studyUID = (String) params.get(STUDY_UID);
		String ko = (String) params.get(KO);

		if (studyUID == null || ko == null) {
			return filterItem.callNextFilter(params);
		}
		ResultsBean ret = addKoGspsMacro(filterItem, params, studyUID);
		if (ret == null)
			return null;

		return ret;
	}

	/**
	 * 
	 * @param filterItem
	 * @param params
	 * @param studyUID
	 * @return
	 */
	private ResultsBean addKoGspsMacro(FilterItem<ResultsBean> filterItem,
			Map<String, Object> params, String studyUID) {
		ResultsBean ret = filterItem.callNextFilter(params);

		performImageLevelQuery(ret, studyUID, "KO");

		for (int patientI = 0; patientI < ret.getPatient().size(); patientI++) {
			PatientType patient = ret.getPatient().get(patientI);
			for (StudyType studyType : patient.getStudy()) {
				StudyBean study = (StudyBean) studyType;

				KeyObjectBean keyDicom = (KeyObjectBean) study.searchStudy("*",
						"KO");
				if (keyDicom == null) {
					continue;
				}

				KeyObjectBean kob = KeyObjectFilter.queryForKO(dicomFullHeader,
						params, keyDicom.getObjectUID(), ret);

				if (kob != null) {
					// Set the keyObjectGspsMacro to true if atleast one Key Object
					// image references GSPS UID
					for (KeySelection key : kob.getKeySelection()) {
						if (key.getGspsUid() != null) {
							KeyObjectGspsMacro gsm = (KeyObjectGspsMacro) study
									.getMacroItems().findMacro(
											KeyObjectGspsMacro.class);
							if (gsm == null) {
								study.getMacroItems().addMacro(
										new KeyObjectGspsMacro("true"));
							}
						}
					}
				}
			}
		}
		return ret;
	}

	/**
	 * Perform image level query for the given study UID and the modality
	 * 
	 * @param ret
	 * @return
	 */
	protected ResultsBean performImageLevelQuery(ResultsBean ret,
			String studyUID, String modality) {
		Map<String, Object> newParams = new HashMap<String, Object>();
		StringBuffer queryStr = new StringBuffer("studyUID=").append(studyUID);
		newParams.put("studyUID", studyUID);
		newParams.put("Modality", modality);
		newParams.put(MemoryCacheFilter.KEY_NAME, queryStr.toString());
		newParams.put(ImageSearch.EXTEND_RESULTS_KEY, ret);
		imageSearch.filter(null, newParams);
		return ret;
	}

	/**
	 * @return imageSearch
	 */
	public Filter<ResultFromDicom> getImageSearch() {
		return imageSearch;
	}

	/**
	 * Sets the filter to use for an image search.
	 * 
	 * @param imageSource
	 */
	@MetaData(out = "${ref:imageSource.imageSearch}")
	public void setImageSearch(Filter<ResultFromDicom> imageSearch) {
		this.imageSearch = imageSearch;
	}

	/**
	 * @return
	 */
	public Filter<DicomObject> getDicomFullHeader() {
		return dicomFullHeader;
	}

	/**
	 * Sets the full header filter - this returns all the fields, but not
	 * updated.
	 */
	@MetaData(out = "${ref:dicomFullHeader}")
	public void setDicomFullHeader(Filter<DicomObject> dicomFullHeader) {
		this.dicomFullHeader = dicomFullHeader;
	}
}
