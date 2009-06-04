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

import java.util.HashMap;
import java.util.Map;

import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.search.study.DicomObjectType;
import org.dcm4chee.xero.search.study.GspsBean;
import org.dcm4chee.xero.search.study.ImageType;
import org.dcm4chee.xero.search.study.SeriesType;
import org.dcm4chee.xero.search.study.PatientType;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.dcm4chee.xero.search.study.StudyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This class is an image level filter that sets gspsUid on image objects.   
 * It takes a gsps=NAME, presentationUID=uid{,uid}* or koUID=uid in the params list and applies the most current GSPS
 * or the named key object GSPS to the images.
 * It does this by looking for:
 * 	Modality=KO
 *  Content Label=<GSPS-NAME>
 *  Study UID=<STUDY-UID>
 * If there is more than one study, it does it as a grouped filter for all studies at once.
 * It then uses the GSPS objects created under the results tree to associate images with GSPS objects.
 * These objects have a list of associated images, by object UID/frame number.
 */
public class GspsUid  implements Filter<ResultsBean> {
	private static final Logger log = LoggerFactory.getLogger(GspsUid.class);

	public ResultsBean filter(FilterItem<ResultsBean> filterItem, Map<String, Object> params) {
		String gspsName = (String) params.get(GspsDiscover.GSPS_KEY);
		Object presentationUID = params.get("presentationUID");
		if( (gspsName==null || gspsName.equals("*")) && presentationUID==null) {
			log.debug("Not applying GSPS Uid lookup.");
			return (ResultsBean) filterItem.callNextFilter(params);
		}
		log.debug("Looking for GSPS "+gspsName+" presentation UIDs "+presentationUID);
		ResultsBean rb = (ResultsBean) filterItem.callNextFilter(params);
		if( rb==null ) return null;
		// This can be restricted to a specific set of UID's
		ResultsBean gspsResults = GspsDiscover.queryForGsps(filterItem,rb, presentationUID);
		if( gspsResults==null ) return rb;
		
		Map<String,GspsBean> uidToGsps = new HashMap<String,GspsBean>();
		for( PatientType pt : gspsResults.getPatient() ) {
			for(StudyType st : pt.getStudy() ) {
				for(SeriesType se : st.getSeries() ) {
					for(DicomObjectType dot : se.getDicomObject()) {
						if( ! (dot instanceof GspsBean) ) continue;
						GspsBean gsps = (GspsBean) dot;
						if( gspsName!=null && !gsps.getContentLabel().equals(gspsName)) continue;
						log.debug("Add GSPS "+gsps.getObjectUID() + " to  gspsResults map.");
						for(String uid : gsps.getReferencedSOPInstance() ) {
							GspsBean oldGsps = uidToGsps.get(uid);
							if( oldGsps==null || oldGsps.getPresentationDateTime().compare(gsps.getPresentationDateTime())<0 ) {
								log.debug("Putting a GSPS match for "+uid+" to "+gsps.getObjectUID());
								uidToGsps.put(uid,gsps);
							}
						}
					}
				}
			}
		}
		
		addGspsUids(rb, uidToGsps);
		return rb;
	}

	private void addGspsUids(ResultsBean rb, Map<String, GspsBean> uidToGsps) {
		for( PatientType pt : rb.getPatient() ) {
			for(StudyType st : pt.getStudy() ) {
				for(SeriesType se : st.getSeries() ) {
					for(DicomObjectType dot : se.getDicomObject()) {
						if( !(dot instanceof ImageType) ) continue;
						GspsBean gspsBean = uidToGsps.get(dot.getObjectUID());
						if( gspsBean==null ) {
							log.debug("GSPS not found for "+dot.getObjectUID());
							continue;
						}
						ImageType image = (ImageType) dot;
						image.setGspsUID(gspsBean.getObjectUID());
					}
				}
			}
		}
	}

}
