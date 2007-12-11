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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.MemoryCacheFilterBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows paged access to image data. This class will call the next filter, but
 * then create a modified return value containing only a sub-set of the items,
 * for the given page and size of data. The image data is passed/used unchanged,
 * so if there are one time modifications to it, they can be done after the
 * paging is done.
 * 
 * @author bwallace
 */
public class PagedImageFilter implements Filter<ResultsType> {
   private static final Logger log = LoggerFactory.getLogger(PagedImageFilter.class);

   static public final String POSITION_KEY = "Position";

   static public final String COUNT_KEY = "Count";
   
   static public final String CONTAINS_OBJECT_UID="containsObjectUID";
   
   static public final String CONTAINS_FRAME_NUMBER="containsFrameNumber";

   static public final int DEFAULT_COUNT = 64;

   public PagedImageFilter() {
	  log.debug("Loaded PagedImageFilter instance.");
   }

   /**
     * Filter the results by image position and image count, returning a NEW
     * instance of the overall result.
     */
   public ResultsType filter(FilterItem filterItem, Map<String, Object> params) {
	  int position = 0;
	  int count = 0;
	  Object[] page = MemoryCacheFilterBase.removeFromQuery(params,POSITION_KEY,COUNT_KEY, CONTAINS_OBJECT_UID, CONTAINS_FRAME_NUMBER);
	  String objectUID = null;
	  String frameNo = null;
	  if (page[0]!=null) {
		 position = Integer.parseInt((String) page[0]);
		 // A negative position might be specified by offset logic, so just
            // use zero.
		 if (position < 0)
			position = 0;
		 count = DEFAULT_COUNT;
		 if (page[1]!=null) {
			count = Integer.parseInt((String) page[1]);
		 }
		 log.debug("Paged image filter return items from " + position + "," + count);
	  }
	  else if( page[2]!=null ) {
		 objectUID = (String) page[2];
		 frameNo = (String) page[3];
		 log.debug("Looking for study/series containing "+objectUID+" frame "+frameNo);
		 Map<String,Object> newQueryParams = new HashMap<String,Object>(params);
		 newQueryParams.put("SOPInstanceUID", objectUID);
		 newQueryParams.put(MemoryCacheFilterBase.KEY_NAME, "SOPInstanceUID="+objectUID);
		 ResultsType results = (ResultsType) filterItem.callNextFilter(newQueryParams);
		 params.remove("SOPInstanceUID");
		 if( results==null ) return null;
		 assert results.getPatient().size()==1;
		 PatientType patient = results.getPatient().get(0);
		 assert patient.getStudy().size()==1;
		 StudyType study = patient.getStudy().get(0);
		 assert study.getSeries().size()==1;
		 SeriesType series = study.getSeries().get(0);
		 String seriesUID = series.getSeriesInstanceUID();
		 params.put("SeriesInstanceUID", seriesUID);
		 params.put(MemoryCacheFilterBase.KEY_NAME, "SeriesInstanceUID="+seriesUID);
		 log.debug("Looking for series UID "+seriesUID);
	  }
	  else {
		 log.debug("Not looking for paging image results - probably an ad-hoc query.");
	  }
	  ResultsType results = (ResultsType) filterItem.callNextFilter(params);
	  if( objectUID!=null && results!=null ) {
		 position = findPosition(results,objectUID,frameNo);
		 log.debug("Found position "+position);
		 if( position>=0 ) count = 5;
	  }
	  if (results == null)
		 return results;
	  if (count > 0 ) {
		 results = decimate(results, position, count);
	  }
	  return results;
   }

   /** Finds the position that the given object is at */
   private int findPosition(ResultsType results, String objectUID, String frameNo) {
	  String key = objectUID;
	  if( frameNo!=null ) key = key+","+frameNo;
	  Object result = ((ResultsBean) results).getChildren().get(key);
	  if( result==null && frameNo!=null ) {
		 result = ((ResultsBean) results).getChildren().get(objectUID);
	  }
	  if( result==null || !(result instanceof ImageBean)) return -1;
	  ImageBean image = (ImageBean) result;
	  int posn = image.getPosition();
	  if( frameNo!=null ) {
		 int frameNoI = Integer.parseInt(frameNo);
		 assert image.getNumberOfFrames() >= frameNoI;
		 assert frameNoI>=1;
		 return posn-1+frameNoI;
	  }
	  return posn;
   }

   /**
     * Remove all images before the position element and starting with
     * position+count and later.
     * 
     * @param results
     *            to remove images from, already sorted with non-viewables
     *            removed.
     * @param position
     *            to start images from
     * @param count
     *            of images to include information on.
     * @return New decimated ResultsType instance.
     */
   public ResultsType decimate(ResultsType results, int position, int count) {
	  ResultsBean ret = new ResultsBean(results);
	  ret.getPatient().clear();
	  for (PatientType patient : results.getPatient()) {
		 PatientType newPatient = new PatientBean(ret.getChildren(), patient);
		 newPatient.getStudy().clear();
		 ret.getPatient().add(newPatient);
		 for (StudyType study : patient.getStudy()) {
			StudyBean newStudy = new StudyBean(ret.getChildren(), (StudyBean) study);
			newStudy.getSeries().clear();
			newPatient.getStudy().add(newStudy);
			for (SeriesType series : study.getSeries()) {
			   newStudy.getSeries().add(decimate(newStudy, series, position, count));
			}
		 }
	  }
	  return ret;
   }

   /**
     * Remove all images before the position element and starting with position,
     * include all image frames to position+count. All image frames need to have
     * been expanded.
     * 
     * @param series
     *            to remove images from, already sorted with non-viewables
     *            removed.
     * @param position
     *            to start images from
     * @param count
     *            of images to include information on.
     * @return Decimated copy of this object - or the original object if
     *         decimation not required.
     */
   public SeriesType decimate(StudyBean newStudy, SeriesType series, int position, int count) {
	  List<DicomObjectType> original = series.getDicomObject();
	  int size = original.size();
	  if (size == 0)
		 return series;
	  DicomObjectType lastObj = series.getDicomObject().get(size - 1);
	  // If it isn't a series of images, don't care, and just return.
	  if (!(lastObj instanceof ImageBean))
		 return series;

	  SeriesType ret = new SeriesBean(newStudy, series);
	  List<DicomObjectType> modified = ret.getDicomObject();
	  int startPos = findPosition(modified, position);
	  int endPos = findPosition(modified, position + count - 1);
	  if (startPos < 0)
		 startPos = 0;
	  if (endPos < 0)
		 endPos = modified.size() - 1;
	  // Do the easy clear portion - those items that can just be bulk
        // cleared.
	  if( endPos+1<modified.size() ) modified.subList(endPos + 1, modified.size()).clear();
	  modified.subList(0, startPos).clear();
	  for(int i=0; i<modified.size(); i++ ) {
		 if( modified.get(i) instanceof ImageBeanMultiFrame ) {
			ImageBeanMultiFrame image = (ImageBeanMultiFrame) modified.get(i);
			log.debug("Splitting a multi-frame object with "+image.getNumberOfFrames()+" frames.");
			if( image.getNumberOfFrames()==1 ) continue;
			int startFrame = Math.max(1, position-image.getPosition()+1);
			int endFrame = Math.min(image.getNumberOfFrames(), position+count-image.getPosition());
			List<DicomObjectType> addList = new ArrayList<DicomObjectType>(endFrame-startFrame);
			for(int fr=startFrame; fr<=endFrame; fr++) {
			   addList.add(image.getImageFrame(fr));
			}
			modified.remove(i);
			modified.addAll(i, addList);
			// Add the actual number of frames added.
			i += addList.size()-1;
		 }
		 else {
			log.debug("Not splitting non-multi-frame object "+modified.get(i).getSOPInstanceUID());
		 }
	  }
	  return ret;
   }

   /**
     * Finds the position of the image bean with the given position item.
     * Assumes all elements are image beans.
     */
   private int findPosition(List<DicomObjectType> modified, int position) {
	  if( position<=0 ) return 0;
	  int min = 0;
	  int max = modified.size();
	  int size = max-1;
	  while(min<size) {
		 int test = (min+max)/2;
		 ImageBean image = (ImageBean) modified.get(test);
		 if( image.getPosition()>position ) {
			max = test;
		 }
		 else if( image.getPosition() +image.getNumberOfFrames() <= position ) {
			min = test;
		 }
		 else {
			return test;
		 }
	  }
	  return -1;
   }

}
