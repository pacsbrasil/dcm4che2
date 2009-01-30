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

import static org.testng.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dcm4chee.xero.search.filter.DicomTestData;
import org.dcm4chee.xero.search.filter.SimpleFilterItem;
import org.dcm4chee.xero.search.filter.SortImageFilter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class PagedImageFilterTest
{
   private String expectedStudyUID = "1.2.840.113543.6.6.1.1.4.2684384646680552649.20104256288";
   private String expectedSeriesUID = "1.2.840.113543.6.6.1.1.5.2684384646680552649.20104256288";
   private String queryStr = "studyUID="+expectedStudyUID+"&seriesUID="+expectedSeriesUID;
   
   private Map<String,Object> params;
   
   private ResultsBean createResultsBeanFromDICOM() throws IOException
   {
      ResultsBean results = new ResultsBean();
      results.addResult(DicomTestData.findDicomObject("regroup/US/us1.dcm"));
      results.addResult(DicomTestData.findDicomObject("regroup/US/us2.dcm"));
      results.addResult(DicomTestData.findDicomObject("regroup/US/us3.dcm"));
      results.addResult(DicomTestData.findDicomObject("regroup/US/us4.dcm"));
      results.addResult(DicomTestData.findDicomObject("regroup/US/us5.dcm"));
      results.addResult(DicomTestData.findDicomObject("regroup/US/us6.dcm"));
      results.addResult(DicomTestData.findDicomObject("regroup/US/us7.dcm"));
      results.addResult(DicomTestData.findDicomObject("regroup/US/us8.dcm"));
      results.addResult(DicomTestData.findDicomObject("regroup/US/us9.dcm"));
      return results;
   }   
   
   private ResultsBean createSortedResultsBeanFromDICOM()  throws IOException
   {
      ResultsBean results = createResultsBeanFromDICOM();
      SortImageFilter sortFilter = new SortImageFilter();
      return sortFilter.filter(new SimpleFilterItem<ResultsBean>(results), params);
   }
   
   @BeforeMethod
   public void setup()
   {
      params = new HashMap<String, Object>();
      params.put("queryStr", queryStr);
      params.put("studyUID",expectedStudyUID);
      params.put("seriesUID", expectedSeriesUID);
   }
   
   @Test
   public void findPosition_ShouldFindThePositionInSmallList() throws IOException
   {
      ResultsBean results = createSortedResultsBeanFromDICOM();
      List<DicomObjectType> objects = results.getPatient().get(0).getStudy().get(0).getSeries().get(0).getDicomObject();
      
      for(DicomObjectType o : objects)
      {
         Integer position = o.getPosition();
         int index = PagedImageFilter.findPosition(objects, position);
         assertEquals(objects.get(index).getPosition(),position);
      }
   }
   
   @Test
   public void findPosition_ShouldReturnNegativeOneWhenPassedAnUnknownPosition() throws IOException
   {
      ResultsBean results = createSortedResultsBeanFromDICOM();
      List<DicomObjectType> objects = results.getPatient().get(0).getStudy().get(0).getSeries().get(0).getDicomObject();
      int index = PagedImageFilter.findPosition(objects, 1867);
      assertEquals(index,-1);
      
   }

   
   @Test
   public void filter_ShouldReturnOnlyRequestedObjectsThatAreRequested() throws IOException
   {
      int count = 4;

      params.put(PagedImageFilter.POSITION_KEY, "0");
      params.put(PagedImageFilter.COUNT_KEY,Integer.toString(count));
      
      
      ResultsBean results = createSortedResultsBeanFromDICOM();
      
      PagedImageFilter pagedFilter = new PagedImageFilter();
      ResultsType pagedResults = pagedFilter.filter(new SimpleFilterItem<ResultsType>(results), params);
      
      assertFalse(params.containsKey("seriesUID"), "SeriesUID should be stripped out");
      assertEquals(pagedResults.getPatient().get(0).getStudy().get(0).getSeries().get(0).getDicomObject().size(),count);
   }
   
   /**
    * Test method for {@link org.dcm4chee.xero.search.study.PagedImageFilter#filter(org.dcm4chee.xero.metadata.filter.FilterItem, java.util.Map)}.
    */
   @Test
   public void removeSeriesParameterIfStudyUIDPresent_SeriesUIDShouldBeRemovedIfStudyUIDAvailable()
   {
      PagedImageFilter filter = new PagedImageFilter();
      String actualSeriesUID = filter.removeSeriesParameterIfStudyUIDPresent(params);
      assertEquals(actualSeriesUID,expectedSeriesUID);
      assertFalse(params.containsKey("seriesUID"));
      assertFalse(params.get("queryStr").toString().contains(expectedSeriesUID));
   }
   
   /**
    * Test method for {@link org.dcm4chee.xero.search.study.PagedImageFilter#filter(org.dcm4chee.xero.metadata.filter.FilterItem, java.util.Map)}.
    */
   @Test
   public void removeSeriesParameterIfStudyUIDPresent_SeriesUIDShouldBeRetainedIfStudyUIDIsMissing()
   {
      params.remove("studyUID");
      
      PagedImageFilter filter = new PagedImageFilter();
      filter.removeSeriesParameterIfStudyUIDPresent(params);
      
      assertTrue(params.containsKey("seriesUID"));
   }

}
