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

import static org.testng.Assert.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.dcm4chee.xero.search.study.DicomObjectType;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.dcm4chee.xero.search.study.SeriesType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
/**
 *
 * @author Andrew Cowan (amidx)
 */
public class RegroupMultiframesByInstanceNumberFilterTest
{
   
   @BeforeMethod
   public void setupParams()
   {
   }
   
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
  
    /**
     * Test method for {@link org.dcm4chee.xero.search.filter.RegroupMultiframesByInstanceNumberFilter#filter(org.dcm4chee.xero.metadata.filter.FilterItem, java.util.Map)}.
    * @throws IOException 
     */
    @Test
    public void testFilter_ShouldMoveMultiframesIntoNewSeries() throws IOException
    {
       ResultsBean results = createResultsBeanFromDICOM();
       RegroupMultiframesByInstanceNumberFilter regroup = new RegroupMultiframesByInstanceNumberFilter();
       Map<String,Object> params = new HashMap<String, Object>();
       params.put("regroup", "*");
       
       assertEquals(results.getPatient().get(0).getStudy().get(0).getSeries().size(),1);
       assertEquals(results.getPatient().get(0).getStudy().get(0).getSeries().get(0).getDicomObject().size(),9);
       regroup.filter(new SimpleFilterItem<ResultsBean>(results), params);
       assertEquals(results.getPatient().get(0).getStudy().get(0).getSeries().size(),9,
             "Multiframes should be broken out into separate series by instance number");
    }
    
    
    @Test
    public void testFilter_ShouldGroupMultiframesByInstanceNumber() throws IOException
    {
       // Add a second instance #5 value.  This should be grouped with the other one.
       ResultsBean results = createResultsBeanFromDICOM();
       results.addResult(DicomTestData.findDicomObject("regroup/US/us10_InstanceNumber5.dcm"));
       
       RegroupMultiframesByInstanceNumberFilter regroup = new RegroupMultiframesByInstanceNumberFilter();
       Map<String,Object> params = new HashMap<String, Object>();
       params.put("regroup", "*");
       
       assertEquals(results.getPatient().get(0).getStudy().get(0).getSeries().size(),1);
       assertEquals(results.getPatient().get(0).getStudy().get(0).getSeries().get(0).getDicomObject().size(),10);
       regroup.filter(new SimpleFilterItem<ResultsBean>(results), params);
       
       for(SeriesType series : results.getPatient().get(0).getStudy().get(0).getSeries())
       {
          Integer instanceNumber = null;
          for(DicomObjectType object : series.getDicomObject())
          {
             assertNotNull(object.getInstanceNumber());
             assertTrue(instanceNumber == null || instanceNumber.equals(object.getInstanceNumber()),
                   "The instance numbers in a series must all be the same");
          }
       }
       
       assertEquals(results.getPatient().get(0).getStudy().get(0).getSeries().size(),9,
             "Multiframes should be broken out into separate series by instance number");
    }

    @Test
    public void shouldRegroup_ShouldOnlyRegroupIfParameterIsSet() throws IOException
    {
       RegroupMultiframesByInstanceNumberFilter regroup = new RegroupMultiframesByInstanceNumberFilter();
       assertTrue(regroup.shouldRegroup(Collections.singletonMap("regroup", (Object)"*")));
       assertTrue(regroup.shouldRegroup(Collections.singletonMap("regroup", (Object)"RegroupMultiframesByInstanceNumberFilter")));
       assertFalse(regroup.shouldRegroup(new HashMap<String,Object>()));
       assertFalse(regroup.shouldRegroup(null));
    }
    
    
    
}
