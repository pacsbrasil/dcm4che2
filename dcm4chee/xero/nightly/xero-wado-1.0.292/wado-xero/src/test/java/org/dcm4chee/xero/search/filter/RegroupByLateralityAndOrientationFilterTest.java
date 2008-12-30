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
 * Sebastian Mohan <sebastian.mnohan@agfa.com>
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

import static org.testng.Assert.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.dcm4chee.xero.search.study.ResultsBean;
import org.dcm4chee.xero.search.study.SeriesBean;
import org.dcm4chee.xero.search.study.SeriesType;
import org.dcm4chee.xero.search.study.StudyBean;
import org.testng.annotations.Test;

public class RegroupByLateralityAndOrientationFilterTest
{
   private ResultsBean createResultsBeanFromDICOM() throws IOException
   {
      ResultsBean results = new ResultsBean();
      results.addResult(DicomTestData.findDicomObject("regroup/MG/MG0001.dcm"));
      results.addResult(DicomTestData.findDicomObject("regroup/MG/MG0002.dcm"));
      results.addResult(DicomTestData.findDicomObject("regroup/MG/MG0003.dcm"));
      return results;
   }

   @Test
   public void shouldRegroup_ShouldOnlyRegroupIfParameterIsSet() throws IOException
   {
      ResultsBean results = createResultsBeanFromDICOM();
      results.getPatient().get(0).getStudy().get(0).setModalitiesInStudy("MG");
      RegroupByLateralityAndOrientationFilter regroup = new RegroupByLateralityAndOrientationFilter();
      assertTrue(regroup.shouldRegroup(results,Collections.singletonMap("regroup", (Object)"*")));
      assertTrue(regroup.shouldRegroup(results,Collections.singletonMap("regroup", (Object)"RegroupByLateralityAndOrientationFilter")));
      assertFalse(regroup.shouldRegroup(results,new HashMap<String,Object>()));
      assertFalse(regroup.shouldRegroup(results,null));
   }
   
   @Test
   public void shouldRegroup_ShouldOnlyRegroupIfMammoInModalitiesInStudy() throws IOException
   {
      Map<String,Object> allow = Collections.singletonMap("regroup", (Object)"*");
      ResultsBean results = createResultsBeanFromDICOM();

      RegroupByLateralityAndOrientationFilter regroup = new RegroupByLateralityAndOrientationFilter();
      assertTrue(regroup.shouldRegroup(results,allow));
      results.getPatient().get(0).getStudy().get(0).setModalitiesInStudy("CT");
      assertFalse(regroup.shouldRegroup(results,allow));      
      results.getPatient().get(0).getStudy().get(0).setModalitiesInStudy("MG");
      assertTrue(regroup.shouldRegroup(results,allow));

   }
   
   @Test
   public void testFilter_ShouldGroupByViewCodeAndOrientation_WhenParamaterIsSet() throws IOException
   {
      ResultsBean results = createResultsBeanFromDICOM();

      // Remember the location of SOP Instance UIDs
      StudyBean study = (StudyBean) results.getPatient().get(0).getStudy().get(0);
      assertEquals(study.getSeries().size(), 1);
      assertEquals(study.getSeries().get(0).getDicomObject().size(), 3);

      RegroupByLateralityAndOrientationFilter regroup = new RegroupByLateralityAndOrientationFilter();
      Map<String,Object> params = new HashMap<String, Object>();
      params.put("regroup", "*");
      regroup.filter(new SimpleFilterItem<ResultsBean>(results), params);

      assertEquals(study.getSeries().size(), 3);
      
      for(SeriesType seriesType : study.getSeries())
      {
         SeriesBean series = (SeriesBean)seriesType;
         assertTrue(series.isSynthetic(), "Generated series must be marked as synthetic");
         
         String description = series.getSeriesDescription();
         if(description.contains("L") &&  description.contains("CC"))
            assertEquals(series.getDicomObject().size(),1);
         else if(description.contains("R") && description.contains("CC"))
            assertEquals(series.getDicomObject().size(),1);
         else if(description.contains("R") && description.contains("MLO"))
            assertEquals(series.getDicomObject().size(),1);
         else
            fail("This study only has CC and MLO view codes.");
      }
   }
   
   @Test 
   public void testFilter_ShouldOnlyFilterMGStudies() throws IOException
   {
      ResultsBean results = createResultsBeanFromDICOM();

      // Load a MG study that should be sorted, but change the modality so that it is disregarded.
      StudyBean study = (StudyBean) results.getPatient().get(0).getStudy().get(0);
      for(SeriesType series : study.getSeries())
         series.setModality("CT");

      assertEquals(study.getSeries().size(), 1);
      assertEquals(study.getSeries().get(0).getDicomObject().size(), 3);

      RegroupByLateralityAndOrientationFilter regroup = new RegroupByLateralityAndOrientationFilter();
      Map<String,Object> params = new HashMap<String, Object>();
      params.put("regroup", "*");
      regroup.filter(new SimpleFilterItem<ResultsBean>(results), params);

      assertEquals(study.getSeries().size(), 1);
      assertEquals(study.getSeries().get(0).getDicomObject().size(), 3);
   }
  
   @Test 
   public void testFilter_ShouldCheckModalitiesInStudyBeforeRegrouping() throws IOException
   {
      ResultsBean results = createResultsBeanFromDICOM();

      // Load a MG study that should be sorted, but change the modality so that it is disregarded.
      StudyBean study = (StudyBean) results.getPatient().get(0).getStudy().get(0);
      study.setModalitiesInStudy("CT,MR,CR");
      
      assertEquals(study.getSeries().size(), 1);
      assertEquals(study.getSeries().get(0).getDicomObject().size(), 3);

      Map<String,Object> params = new HashMap<String, Object>();
      params.put("regroup", "*");
      RegroupByLateralityAndOrientationFilter regroup = new RegroupByLateralityAndOrientationFilter();
      regroup.filter(new SimpleFilterItem<ResultsBean>(results),params);

      assertEquals(study.getSeries().size(), 1);
      assertEquals(study.getSeries().get(0).getDicomObject().size(), 3);
   }

}
