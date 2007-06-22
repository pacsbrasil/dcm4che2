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

import java.util.List;
import java.util.Map;

import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows paged access to image data.  This class will call the next filter,
 * but then create a modified return value containing only a sub-set of the
 * items, for the given page and size of data.  
 * @author bwallace
 */
public class PagedImageFilter implements Filter<ResultsType> {
	private static final Logger log = LoggerFactory.getLogger(PagedImageFilter.class);
	static public final String POSITION_KEY = "Position";
	static public final String COUNT_KEY = "Count";
	static public final int DEFAULT_COUNT = 64;

	public PagedImageFilter() {
		log.debug("Loaded PagedImageFilter instance.");
	}
	
	/**
	 * Filter the results by image position and image count, returning
	 * a NEW instance of the overall result.
	 */
	public ResultsType filter(FilterItem filterItem, Map<String, Object> params) {
		int position = 0;
		int count = 0;
		if( params.containsKey(POSITION_KEY) ) {
			position = Integer.parseInt((String) params.get(POSITION_KEY));
			// A negative position might be specified by offset logic, so just use zero.
			if( position < 0 ) position = 0;
			count = DEFAULT_COUNT;
			if( params.containsKey(COUNT_KEY) ) {
				count = Integer.parseInt((String) params.get(COUNT_KEY));
			}
			log.debug("Paged image filter return items from "+position+","+count);
		}
		ResultsType results = (ResultsType) filterItem.callNextFilter(params);
		if( results==null ) return results;
		if( count>0 ) {
			results = decimate(results,position,count);
		}
		return results;		
	}

	/** Remove all images before the position element 
	 * and starting with position+count and later.
	 * @param results to remove images from, already sorted with non-viewables removed.
	 * @param position to start images from
	 * @param count of images to include information on.
	 * @return New decimated ResultsType instance.   
	 */
	protected ResultsType decimate(ResultsType results, int position, int count) {
		ResultsBean ret = new ResultsBean(results);
		ret.getPatient().clear();
		for(PatientType patient : results.getPatient()) {
			PatientType newPatient = new PatientBean(ret.getChildren(), patient);
			newPatient.getStudy().clear();
			ret.getPatient().add(newPatient);
		   	for(StudyType study : patient.getStudy() ) {
		   		StudyType newStudy = new StudyBean(ret.getChildren(), study);
		   		newStudy.getSeries().clear();
		   		newPatient.getStudy().add(newStudy);
		   		for(SeriesType series : study.getSeries() ) {
		   			newStudy.getSeries().add(decimate(ret.getChildren(), series,position,count));
		   		}
		   	}
		}
		return ret;
	}
	
	/** Remove all images before the position element 
	 * and starting with position+count and later.
	 * @param series to remove images from, already sorted with non-viewables removed.
	 * @param position to start images from
	 * @param count of images to include information on.
	 * @return Decimated copy of this object - or the original object if decimation not required.
	 */
	protected SeriesType decimate(Map<Object,Object> children, SeriesType series, int position, int count) {
		series.setViewable(series.getDicomObject().size());
		if( series.getDicomObject().size() <= position ) return series;
		SeriesType ret = new SeriesBean(children, series);
		List<DicomObjectType> original = ret.getDicomObject();
		if( position>0 ) original.subList(0,position-1).clear();
		if( count < original.size() ) original.subList(count,original.size()).clear();
		for(DicomObjectType dicomObject : original) {
			dicomObject.setInstanceNumber(null);
			if( dicomObject instanceof ImageType ) {
				ImageType image = (ImageType) dicomObject;
				image.setPosition(position++);
			}
		}
		return ret;
	}
	
}
