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
import org.dcm4chee.xero.search.ResultFromDicom;
import org.dcm4chee.xero.search.macro.KeyObjectGspsMacro;
import org.dcm4chee.xero.search.macro.KeyObjectMacro;
import org.dcm4chee.xero.search.study.DicomObjectInterface;
import org.dcm4chee.xero.search.study.KeyObjectBean;
import org.dcm4chee.xero.search.study.KeySelection;
import org.dcm4chee.xero.search.study.PatientType;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.dcm4chee.xero.search.study.StudyBean;
import org.dcm4chee.xero.search.study.StudyType;
import org.dcm4chee.xero.wado.DicomFilter;
import org.dcm4chee.xero.wado.WadoParams;
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
	private static final Logger log = LoggerFactory.getLogger(KeyObjectStudyFilter.class);
	
	private static final String KO = "ko";

	private Filter<DicomObject> dicomFullHeader;

	private Filter<ResultFromDicom> imageSearch;

	public ResultsBean filter(FilterItem<ResultsBean> filterItem,
			Map<String, Object> params) {
		String ko = (String) params.get(KO);

		ResultsBean ret = filterItem.callNextFilter(params);
		if (ko == null) {
			return ret;
		}
		addKoGspsMacro(filterItem, params, ret);
		return ret;
	}

	/**
	 * Perform image level search to find the latest KeyObject. If the KeyObject
	 * found set koUId and the koGSPS=true if the key image has gsps references
	 * 
	 * @param filterItem
	 * @param params
	 * @param studyUID
	 *            can be <tt>null</tt>
	 * @return
	 */
	private void addKoGspsMacro(FilterItem<ResultsBean> filterItem,
			Map<String, Object> params, ResultsBean ret) {
		StringBuffer studyUID = null;
		Map<String,StudyBean> studies=null;

		for (int i = 0; i < ret.getPatient().size(); i++) {
			PatientType patient = ret.getPatient().get(i);
			for (StudyType studyType : patient.getStudy()) {
				StudyBean study = (StudyBean) studyType;
				if (study.getModalitiesInStudy().indexOf("KO") > -1) {
					if( studies==null ) {
						studies =new HashMap<String,StudyBean>();
						studyUID = new StringBuffer();
					} else {
						studyUID.append("\\");
					}
					studyUID.append(study.getStudyUID());
					studies.put(study.getStudyUID(),study);
				}
			}
		}
		if( studies==null ) return;
		
		String ae = (String) params.get(WadoParams.AE);
		log.debug("Searching for study UID's {}",studyUID);
		ResultsBean imageResult = performImageLevelQuery(studyUID.toString(), "KO", ae);
		searchAndAddMacro(params,imageResult, studies);
	}

	/**
	 * Search for the latest KeyObject under all studies in the image result and
	 * assign it to the passed in study if the uid matches
	 * 
	 * @param params
	 * @param imageResult
	 *            <tt>ResultsBean</tt> for the image level query
	 * @param study
	 */
	private void searchAndAddMacro(Map<String, Object> params,
			ResultsBean imageResult, Map<String,StudyBean> studies) {
		log.debug("In image results, there are {} patients", imageResult.getPatient().size());
		for (PatientType patient : imageResult.getPatient()) {
			log.debug("In patient {} there are {} studies.", patient.getPatientIdentifier(),patient.getStudy().size());
			for (StudyType imgStudyType : patient.getStudy()) {
				StudyBean imgStudy = (StudyBean) imgStudyType;
				StudyBean resultStudy = studies.get(imgStudy.getStudyUID());
				if( resultStudy==null ) {
					log.warn("Returned study {} but not found in original query?",imgStudy.getStudyUID());
					continue;
				}
				DicomObjectInterface kobDicom = (DicomObjectInterface) imgStudy
						.searchStudy((String) params.get(KO), "KO");
					
				if (kobDicom != null) {
					log.debug("Found key object info for study {}", resultStudy.getStudyUID());
					DicomObject dobj = DicomFilter.callInstanceFilter(
							dicomFullHeader,  kobDicom, (String) params.get(WadoParams.AE));
					if( dobj==null ) {
						log.warn("Unable to read key object uid "+kobDicom.getObjectUID());
						continue;
					}
					KeyObjectBean kob = (KeyObjectBean) kobDicom;
					kob.initKeySelection(dobj);
					addMacros(resultStudy, kob);
				} else {
					log.warn("Unable to find key object bean for study {}",resultStudy.getStudyUID());
				}
			}
		}
	}

	/**
	 * Set the <tt>KeyObjectMacro</tt> to <tt>StudyBean</tt>. Set the
	 * <tt>KeyObjectGspsMacro</tt> to true if atleast one Key image references a
	 * GSPS UID
	 * 
	 * @param study
	 * @param kob
	 */
	private void addMacros(StudyBean study, KeyObjectBean kob) {
		KeyObjectMacro kom = (KeyObjectMacro) study.getMacroItems().findMacro(
				KeyObjectMacro.class);
		if (kom == null) {
			study.getMacroItems().addMacro(
					new KeyObjectMacro(kob.getObjectUID()));
		}

		// Check for GSPS uids and assign KeyObjectGspsMacro if present
		for (KeySelection key : kob.getKeySelection()) {
			if (key.getGspsUid() != null) {
				KeyObjectGspsMacro gspsm = (KeyObjectGspsMacro) study
						.getMacroItems().findMacro(KeyObjectGspsMacro.class);
				if (gspsm == null) {
					study.getMacroItems().addMacro(
							new KeyObjectGspsMacro("true"));
				}
			}
		}
	}

	/**
	 * Perform image level query for the given study UID and the modality
	 * 
	 * @param ret
	 * @return
	 */
	private ResultsBean performImageLevelQuery(String studyUID, String modality, String ae) {
		Map<String, Object> newParams = new HashMap<String, Object>();
		newParams.put("studyUID", studyUID);
		newParams.put("Modality", modality);
		if( ae!=null ) newParams.put("ae", ae);
		return (ResultsBean) imageSearch.filter(null, newParams);
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
