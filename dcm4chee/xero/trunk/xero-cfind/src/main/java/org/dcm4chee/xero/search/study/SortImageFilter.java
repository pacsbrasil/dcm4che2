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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;

/**
 * This class sorts the images by instance number, and adds the position information into the image level data.
 * @author bwallace
 *
 */
public class SortImageFilter implements Filter<ResultsType>{

	/** Sort the returned results, by series number and by image number.
	 * @param filterItem contains information about the other filters, including the next filter.
	 * @param params are the parameters to use to determine the series.
	 * @return a patient object with sorted series and images.  Only series/images matching the params queries will be included.
	 */
	public ResultsType filter(FilterItem filterItem, Map<String, Object> params) {
		ResultsType results = (ResultsType) filterItem.callNextFilter(params);
		if( results==null ) return null;
		return sortSeriesAndImages(results);
	}

	/**
	 * Sorts the series and images, and adds in positional information.
	 * TODO: This is supposed to take into account multi-frame ordering and grouping, but some of the
	 * enhanced CT/MR ordering may not be supported yet. 
	 * @param results
	 * @return
	 */
    public static ResultsType sortSeriesAndImages(ResultsType results) {
	  List<PatientType> patients = results.getPatient();
		for(PatientType patient : patients) {
			List<StudyType> studies = patient.getStudy();
			for(StudyType study : studies) {
				sortSeries(study.getSeries());
			}
		}
		return results;
   }
	
	/** Sorts the series items, and then calls the sort on the image items.
	 * @param series are a list of series objects to sort by series number
	 */
	public static void sortSeries(List<SeriesType> series) {
		Collections.sort(series,new SeriesComparator());
		for(SeriesType aSeries : series) {
			sortImages(aSeries.getDicomObject());
		}
	}
	
	/** Sorts the image items 
	 * TODO Currently assumes that multi-frame images are fully sequential.
	 * @param images are the dicom objects to sort
	 */
	public static void sortImages(List<DicomObjectType> images) {
		Collections.sort(images,new DicomObjectComparator());
		int position = 0;
		for(DicomObjectType dot : images) {
		   if( dot instanceof ImageBeanMultiFrame ) {
			  dot.setPosition(position);
			  ImageBeanMultiFrame image = (ImageBeanMultiFrame) dot;
			  position += image.getNumberOfFrames();
		   }
		   else if( dot instanceof ImageBean ) {
			  dot.setPosition(position++);
		   }
		}
	}

}
