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

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.search.study.PatientType;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.dcm4chee.xero.search.study.StudyType;

/**
 * Sort the metadata results in a convenient and predictable way for the 
 * client application:
 * 
 * <ol>
 * <li> Patients should be sorted alphabetically by Last name then First name.
 * <li> Studies should be sorted from most recent to oldest.
 * </ol>
 * 
 * @author Andrew Cowan (andrew.cowan@agfa.com)
 */
public class SortPatientsByNameStudiesByDateFilter implements Filter<ResultsBean>
{
   private Comparator<PatientType> patientNameComparator = new SortPatientsByNameComparator();
   private Comparator<StudyType> studyDateComparator = new SortStudiesReverseChronologicallyComparator();
   
   /**
    * Apply patient and study sorting to the results.
    * @see org.dcm4chee.xero.metadata.filter.Filter#filter(org.dcm4chee.xero.metadata.filter.FilterItem, java.util.Map)
    */
   public ResultsBean filter(FilterItem<ResultsBean> filterItem, Map<String, Object> params)
   {
      ResultsBean results = filterItem.callNextFilter(params);
      if (results == null)
         return null;
      
      Collections.sort(results.getPatient(),patientNameComparator);
      
      for(PatientType p : results.getPatient())
         Collections.sort(p.getStudy(),studyDateComparator);
      
      return results;
   }
   
   private class SortPatientsByNameComparator implements Comparator<PatientType>
   {
      /**
       * Sort the patients by DICOM name field.
       * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
       */
      public int compare(PatientType p1, PatientType p2)
      {
         String name1 = p1.getPatientName();
         String name2 = p2.getPatientName();
         
         if(name1==null) name1="";
         if(name2==null) name2="";
         
         return name1.compareToIgnoreCase(name2);
      }
   }

   private class SortStudiesReverseChronologicallyComparator implements Comparator<StudyType>
   {
      /**
       * Sort the studies by the DICOM date field.
       * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
       */
      public int compare(StudyType o1, StudyType o2)
      {
         // YYYMMdd ---> should always be alphabetically orderable.
         String dt1 = o1.getStudyDateTime();
         String dt2 = o2.getStudyDateTime();
         
         if(dt1==null) dt1 = "";
         if(dt2==null) dt2 = "";
         
         return dt2.compareTo(dt1);
      }
   }
}
