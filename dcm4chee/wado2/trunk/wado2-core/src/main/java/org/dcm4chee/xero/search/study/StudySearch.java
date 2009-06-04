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
package org.dcm4chee.xero.search.study;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.dcm4che2.data.Tag;
import org.dcm4chee.xero.dicom.SOPClassUIDs;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.search.SearchCriteria;

/**
 * A C-Find filter that knows how to perform C-Find queries to get study level data.  Prefers to use the return all
 * private SOP class if available, otherwiseuses the regular study root retrieve. 
 * @author bwallace
 *
 */
public class StudySearch extends PatientSearch {
    private static final String STUDY_QUERY_LEVEL = "STUDY";
 
 
    // TODO - generate this list dynamically from the contents of the class...
    public static final Integer[] STUDY_RETURN_KEYS = {
		Tag.ConfidentialityCode,
        Tag.StudyDate,
        Tag.StudyTime,
        Tag.AccessionNumber,
        Tag.StudyID,
        Tag.StudyDescription,
        Tag.StudyInstanceUID,
        Tag.StudyStatusIDRET,
        Tag.NumberOfStudyRelatedInstances,
        Tag.NumberOfStudyRelatedSeries,
        Tag.ModalitiesInStudy,
        Tag.ReferringPhysicianName,
        Tag.NameOfPhysiciansReadingStudy,
        Tag.AdmittingDiagnosesDescription,
        Tag.StudyCommentsRET,
     };
    
    protected static Set<Integer> returnKeys = new HashSet<Integer>(Arrays.asList(STUDY_RETURN_KEYS));
    static {
        returnKeys.addAll(PatientSearch.returnKeys);
    }


	@Override
	protected String[] getCuids() {
		return SOPClassUIDs.CFindStudyLevel;
	}

	@Override
	protected String getQueryLevel() {
		return STUDY_QUERY_LEVEL;
	}

	@Override
	protected Set<Integer> getReturnKeys() {
		return StudySearch.returnKeys;
	}

	/**
	 * Set the filter that determines the search criteria to use for this query.
	 * Defaults to the study search condition - if you want series or image level queries, you had
	 * better modify this to use those explicitly, otherwise you will have to include a study level 
	 * criteria.
	 * 
	 * @param searchCondition
	 */
	@MetaData(out="${class:org.dcm4chee.xero.search.study.StudySearchConditionParser}")
   @Override
	public void setSearchParser(Filter<SearchCriteria> searchParser) {
   	super.setSearchParser(searchParser);
   }
}
