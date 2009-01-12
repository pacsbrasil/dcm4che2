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

import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4chee.xero.dicom.SOPClassUIDs;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.search.SearchCriteria;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

/** A C-Find searcher for series level data.
 * Uses the private SOP classes to get all the available series level data, if these are supported.
 *
 * @author bwallace
 */
public class SeriesSearch extends StudySearch{

	static final String SERIES_SEARCH_LEVEL = "SERIES";
	
    static protected final Integer[] SERIES_RETURN_KEYS = {
    	Tag.Modality,
        Tag.SeriesNumber,
        Tag.SeriesInstanceUID,
        Tag.NumberOfSeriesRelatedInstances,
        Tag.Manufacturer};
    
    protected static Set<Integer> returnKeys = new HashSet<Integer>(Arrays.asList(SERIES_RETURN_KEYS));
    
    static {
    	returnKeys.addAll(StudySearch.returnKeys);
    }

	@Override
	protected String[] getCuids() {
		return (String[])SOPClassUIDs.CFindSeriesLevel.toArray();
	}

	@Override
	protected String getQueryLevel() {
		return SERIES_SEARCH_LEVEL;
	}

	@Override
	protected Set<Integer> getReturnKeys() {
		return SeriesSearch.returnKeys;
	}

	/**
	 * Set the filter that determines the search criteria to use for this query.
	 * 
	 * @param searchCondition
	 */
	@Override
	@MetaData(out="${class:org.dcm4chee.xero.search.study.ImageSearchConditionParser}")
	public void setSearchParser(Filter<SearchCriteria> searchParser) {
   	super.setSearchParser(searchParser);
   }

}
