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
package org.dcm4chee.xero.search;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.dcm4chee.xero.cycle.CyclePageAction;
import org.dcm4chee.xero.search.study.StudySearchConditionParser;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;

/** This class handles the search page results */
@Name("SearchPageAction")
public class SearchPageAction  extends CyclePageAction {
	@In
	FacesContext facesContext;

	@In(required=false, value="StudySearchCriteria")
	@Out(value="StudySearchCriteria", scope=ScopeType.CONVERSATION)
	SearchCriteria criteria;
	
	@In(required=false)
	@Out(required=false, scope=ScopeType.CONVERSATION)
	List<String> studySelection;

	static StudySearchConditionParser parser = new StudySearchConditionParser();
	
	/** This method sets up the various search conditions.  
	 * @return The submit type string.
	 */
	@SuppressWarnings("unchecked")
	@Begin(join=true)
	public String search() 
	{
		Map<String,String[]> map = facesContext.getExternalContext().getRequestParameterValuesMap();
		String[] submitValues = map.get("submit");
		String submit = (submitValues==null ? null : submitValues[0]);
		log.info("SearchPageAction called with submit="+submit);
		parseSearchCriteria(submit, map);
		parseStudySelection(submit, map);
		// We need to execute the cycle actions as well
		if( "Add".equals(submit) || "View".equals(submit) ) {
			addCycle(studySelection);
		}
		if( submit!=null ) return submit;
		return "success";
	}
	
	/** Generate the study selection from the map */
	void parseStudySelection(String submit, Map<String, String[]> map) {
		if( submit==null || submit.length()==0 ) return;
		String[] uids = map.get("study");
		if( uids==null ) {
			studySelection = Collections.emptyList();
		}
		else {
			studySelection = Arrays.asList(uids);
		}
	}

	/** Parses the search criteria from the map */
	void parseSearchCriteria(String submit, Map<String,?> map) {
		if( submit!=null ) {
			log.debug("Parsing criteria from provided values.");
			criteria = parser.parseFromMap(map);
		}
		else if(criteria==null) {
			log.debug("Resetting criteria, as it was null.");
			Map<String,String> defaultMap = new HashMap<String,String>();
			defaultMap.put("ModalitiesInStudy", "");
			defaultMap.put("PatientID", "");
			defaultMap.put("AccessionNumber", "");
			defaultMap.put("StudyDateTime", "");
			criteria = parser.parseFromMap(defaultMap);
		}
		log.debug("Found study search criteria #0",criteria.getXml());
		log.debug("Query parameters: #0", criteria.getURLParameters());
	}
}
