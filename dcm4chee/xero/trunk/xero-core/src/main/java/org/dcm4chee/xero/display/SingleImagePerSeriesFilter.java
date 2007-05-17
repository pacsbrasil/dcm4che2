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
package org.dcm4chee.xero.display;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.search.DicomCFindFilter;
import org.dcm4chee.xero.search.study.PatientType;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.dcm4chee.xero.search.study.ResultsType;
import org.dcm4chee.xero.search.study.SeriesBean;
import org.dcm4chee.xero.search.study.SeriesType;
import org.dcm4chee.xero.search.study.StudyType;
import org.jboss.seam.annotations.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Modify the query parameters on series searches so that image data for 1 image
 * is returned on every query - this allows a thumbnail to be shown for that
 * single image.
 * @author bwallace
 *
 */
@Name("metadata.seriesFilter.singleImagePerSeriesFilter")
public class SingleImagePerSeriesFilter implements Filter<Object>{
	private static final String[] EMPTY_STRING_ARRAY = new String[0];

	private static Logger log = LoggerFactory.getLogger(SingleImagePerSeriesFilter.class.getName());

	private static final String INSTANCE_NUMBER = "InstanceNumber";

	/** Adds an InstanceNumber search criteria */
	public Object filter(FilterItem filterItem, Map<String, Object> params) {
		if( params.containsKey(INSTANCE_NUMBER) ) {
			log.info("Search already has instance number, not filtering series.");
			return filterItem.callNextFilter(params);
		}
		
		ResultsBean rb = (ResultsBean) filterItem.callNamedFilter("seriesSource",params);
		if( rb==null ) return null;

		// Now, re-do the same query at the image level
		rb = extendWithInstanceImage(filterItem, params, rb); 
		
		List<SeriesType> childless = findChildless(rb);
		if( childless==null ) return rb;
		return extendWithOtherInstances(filterItem, params, rb, childless);
	}
	
	/**
	 * 
	 * @param filterItem
	 * @param params
	 * @param rb to extend
	 * @param childless series to extend
	 * @return
	 */
	protected ResultsBean extendWithOtherInstances(FilterItem filterItem, Map<String, Object> params, ResultsBean rb, List<SeriesType> childless) {
		log.info("Found "+childless.size()+" series with no child with instance number=1.");
		List<String> uidsToSearch = new ArrayList<String>(childless.size());
		for(SeriesType se : childless) {
			if( ((SeriesBean) se).getNumberOfSeriesRelatedInstances() < 10 ) {
				uidsToSearch.add(se.getSeriesInstanceUID());
			}
			else {
				// TODO - figure out what to do here.  This one is really rather ugly
				log.warn("There are too many children to be able to extend series "+se.getSeriesInstanceUID() + " of modality "+se.getModality());
			}
		}
		if( uidsToSearch.size()>0 ) {
			String[] seriesUids = uidsToSearch.toArray(EMPTY_STRING_ARRAY);
			Object origValue = params.put("SeriesInstanceUID", seriesUids);
			Object updated = filterItem.callNextFilter(params);
			assert updated==rb;
			if( origValue==null ) params.remove("SeriesInstanceUID");
			else params.put("SeriesInstanceUID", origValue);
		}
		return rb;
	}

	protected ResultsBean extendWithInstanceImage(FilterItem filterItem, Map<String, Object> params, ResultsBean rb) {
		// This will cause rb to be extended instead of a new instance being created.
		params.put(DicomCFindFilter.EXTEND_RESULTS_KEY, rb);
		log.debug("Filtering by adding an instance number=1 as a first guess.");
		params.put(INSTANCE_NUMBER, "1");
		Object ret = filterItem.callNextFilter(params);
		assert ret==rb;
		params.remove(INSTANCE_NUMBER);
		return rb;
	}

	/**
	 * Returns a list of all childless series.
	 * @param ResultsBean to search for series containing no children
	 * @return null if everyone has a child, or a List of all Series Bean's containing no children.
	 */
	List<SeriesType> findChildless(ResultsType results) {
		List<SeriesType> ret = null;
		for(PatientType pat : results.getPatient() ) {
			for(StudyType study : pat.getStudy() ) {
				for(SeriesType series : study.getSeries() ) {
					if( series.getDicomObject().size()==0 ) {
						if( ret==null ) ret = new ArrayList<SeriesType>();
						ret.add(series);
					}
				}
			}
		}
		return ret;
	}

	/** Returns the default priority of this filter. */
	@MetaData
	public int getPriority()
	{
		return 15;
	}
}
