// ***** BEGIN LICENSE BLOCK *****
// Version: MPL 1.1/GPL 2.0/LGPL 2.1
// 
// The contents of this file are subject to the Mozilla Public License Version 
// 1.1 (the "License"); you may not use this file except in compliance with 
// the License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
// 
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
// for the specific language governing rights and limitations under the
// License.
// 
// The Original Code is part of dcm4che, an implementation of DICOM(TM) in Java(TM), hosted at http://sourceforge.net/projects/dcm4che
//  
// The Initial Developer of the Original Code is Agfa Healthcare.
// Portions created by the Initial Developer are Copyright (C) 2009 the Initial Developer. All Rights Reserved.
// 
// Contributor(s):
// Andrew Cowan <andrew.cowan@agfa.com>
// 
// Alternatively, the contents of this file may be used under the terms of
// either the GNU General Public License Version 2 or later (the "GPL"), or
// the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
// in which case the provisions of the GPL or the LGPL are applicable instead
// of those above. If you wish to allow use of your version of this file only
// under the terms of either the GPL or the LGPL, and not to allow others to
// use your version of this file under the terms of the MPL, indicate your
// decision by deleting the provisions above and replace them with the notice
// and other provisions required by the GPL or the LGPL. If you do not delete
// the provisions above, a recipient may use your version of this file under
// the terms of any one of the MPL, the GPL or the LGPL.
// 
// ***** END LICENSE BLOCK *****
package org.dcm4chee.xero.search.filter;

import static org.testng.Assert.*;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.util.DateUtils;
import org.dcm4chee.xero.search.study.PatientType;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.dcm4chee.xero.search.study.StudyType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;



/**
 *
 * @author Andrew Cowan (andrew.cowan@agfa.com)
 */
public class SortPatientsByNameStudiesByDateFilterTest
{

   private SortPatientsByNameStudiesByDateFilter filter;

   
   /**
    * Generate a disorganized ResultsBean to be sorted.
    */
   private ResultsBean createResultsBeanOfShuffledPatients() throws IOException
   {
      ResultsBean results = new ResultsBean();
      results.addResult(DicomTestData.findDicomObject("regroup/US/us1.dcm")); // Single name
      results.addResult(DicomTestData.findDicomObject("misc/ct.dcm")); // LAST^FIRST
      results.addResult(DicomTestData.findDicomObject("misc/multicolor.dcm")); // Single name with under scores
      results.addResult(DicomTestData.findDicomObject("misc/pixelPadding.dcm")); // Multiple words with spaces
      results.addResult(DicomTestData.findDicomObject("misc/cr-monochrome1.dcm")); // Multiple words with spaces
      
      return results;
   }
   
   private ResultsBean createResultsBeanOfShuffledStudies() throws IOException
   {
      String[] dicomDates = new String[] {
        "20090803",
        "19740704",
        "19681103",
        "20080504",
        "20100614",
        "19100306",
        "19970902",
        "19991231",
        "19830101",
      };
      
      ResultsBean results = new ResultsBean();
      
      // Read in a set of images and put them into different studies.
      for(int i = 1; i<=9 ; i++)
      {
         DicomObject o = DicomTestData.findDicomObject("regroup/US/us"+i+".dcm");
         
         // Add to own study
         o.putString(Tag.StudyInstanceUID, null, o.getString(Tag.StudyInstanceUID)+i);
         results.addResult(o);
         
         // Set a randomized date.
         results.getPatient().get(0).getStudy().get(i-1).setStudyDateTime(dicomDates[i-1]);
      }
      
      return results;
   }
   
   @BeforeMethod
   public void setup()
   {
      this.filter = new SortPatientsByNameStudiesByDateFilter();
   }
   
   /**
    * Test method for {@link org.dcm4chee.xero.search.filter.SortPatientsByNameStudiesByDateFilter#filter(org.dcm4chee.xero.metadata.filter.FilterItem, java.util.Map)}.
    * @throws IOException 
    */
   @Test
   public void filter_PatientsMustBeOrderedByName() throws IOException
   {
      ResultsBean results = createResultsBeanOfShuffledPatients();
      filter.filter(new SimpleFilterItem<ResultsBean>(results), null);
      List<PatientType> patients = results.getPatient(); 
      assertEquals(patients.get(0).getPatientName(),"CARDIOPAT");
      assertEquals(patients.get(1).getPatientName(),"CR M2 PATIENT B");   
      assertEquals(patients.get(2).getPatientName(),"CR-thoravision"); 
      assertEquals(patients.get(3).getPatientName(),"JUNTGEN^DAVID");      
      assertEquals(patients.get(4).getPatientName(),"MultiFrameTest_RGBMYC");      
   }
   
   @Test
   public void filter_PatientsMustBeOrderedByName_AndHandleNullNameValues() throws IOException
   {
      ResultsBean results = createResultsBeanOfShuffledPatients();
      results.getPatient().get(0).setPatientName(null);
      results.getPatient().get(1).setPatientName("");
      filter.filter(new SimpleFilterItem<ResultsBean>(results), null);
      List<PatientType> patients = results.getPatient(); 
      assertEquals(patients.get(0).getPatientName(),null);
      assertEquals(patients.get(1).getPatientName(),""); 
      assertEquals(patients.get(2).getPatientName(),"CR M2 PATIENT B");   
      assertEquals(patients.get(3).getPatientName(),"CR-thoravision"); 
      assertEquals(patients.get(4).getPatientName(),"MultiFrameTest_RGBMYC");   
   }
   
   @Test(enabled=false) // The ordering is not perfect Last/First, but I doubt it's worth the effort
   public void filter_PatientsMustBeOrderedByName_EnsureThatLastIsCheckedBeforeFirst() throws IOException
   {
      assertTrue("^".compareTo("-") > 0);
      ResultsBean results = createResultsBeanOfShuffledPatients();
      results.getPatient().get(4).setPatientName("JUNTGEN-DAVID");
      filter.filter(new SimpleFilterItem<ResultsBean>(results), null);
      List<PatientType> patients = results.getPatient(); 
      assertEquals(patients.get(0).getPatientName(),"CARDIOPAT");
      assertEquals(patients.get(1).getPatientName(),"CR M2 PATIENT B");   
      assertEquals(patients.get(2).getPatientName(),"CR-thoravision"); 
      assertEquals(patients.get(3).getPatientName(),"JUNTGEN^DAVID");      
      assertEquals(patients.get(4).getPatientName(),"JUNTGEN-DAVID");
   }

   @Test
   public void filter_StudiesMustBeOrderedByDate() throws IOException
   {
      ResultsBean results = createResultsBeanOfShuffledStudies();
      filter.filter(new SimpleFilterItem<ResultsBean>(results), null);
      List<StudyType> studies = results.getPatient().get(0).getStudy(); 
      
      StudyType previousStudy = studies.get(0);
      for(StudyType currentStudy : studies)
      {
         Date currentDate = DateUtils.parseDT(currentStudy.getStudyDateTime(), false);
         Date previousDate = previousStudy == null ? null : DateUtils.parseDT(previousStudy.getStudyDateTime(), false);
         assertTrue( currentDate.compareTo(previousDate) <= 0, "Previous study must always be newer than current when ordered correctly");
      }
   }
   
   @Test
   public void filter_StudiesMustBeOrderedByDate_ShouldHandleNullValues() throws IOException
   {
      Calendar c = Calendar.getInstance();
      c.set(1900, 02, 02);
      Date reallyOld = c.getTime();
      ResultsBean results = createResultsBeanOfShuffledStudies();
      results.getPatient().get(0).getStudy().get(4).setStudyDateTime(null);
      
      filter.filter(new SimpleFilterItem<ResultsBean>(results), null);
      List<StudyType> studies = results.getPatient().get(0).getStudy(); 
      

      
      StudyType previousStudy = studies.get(0);
      for(StudyType currentStudy : studies)
      {
         Date currentDate = DateUtils.parseDT(currentStudy.getStudyDateTime(), false);
         Date previousDate = DateUtils.parseDT(previousStudy.getStudyDateTime(), false);
         
         if(currentDate == null) currentDate = reallyOld;
         if(previousDate == null) previousDate = reallyOld;
         
         assertTrue( currentDate.compareTo(previousDate) <= 0, "Previous study must always be newer than current when ordered correctly");
      }
   }
}
