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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.search.study.DicomObjectType;
import org.dcm4chee.xero.search.study.ImageBean;
import org.dcm4chee.xero.search.study.ImageBeanMultiFrame;
import org.dcm4chee.xero.search.study.PatientType;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.dcm4chee.xero.search.study.SeriesBean;
import org.dcm4chee.xero.search.study.SeriesType;
import org.dcm4chee.xero.search.study.StudyType;

/**
 * Certain modalities (US / XA) will put all of the multiframe images into a single series. This is
 * inconvenient for the user and difficult to represent in the UI.
 * <p>
 * To address this issue we regroup the multiframe images into a separate series each by the Image
 * Number as specified in the DICOM Specification.
 * <p>
 * The new series UIDs should be of the form {series#}@{multiseries#}
 * 
 * @author Andrew Cowan (amidx)
 */
public class RegroupMultiframesByInstanceNumberFilter implements Filter<ResultsBean>
{
   private static final String NAME = RegroupMultiframesByInstanceNumberFilter.class.getSimpleName();
   
   public ResultsBean filter(FilterItem<ResultsBean> filterItem, Map<String, Object> params)
   {
      ResultsBean results = filterItem.callNextFilter(params);
      if(!shouldRegroup(params))
         return results;
      
      for(PatientType patient : results.getPatient())
         for(StudyType study : patient.getStudy())
            groupSeriesByInstanceNumber(study);
      
      return results;
   }
   
   protected void groupSeriesByInstanceNumber(StudyType study)
   {
      List<ImageBean> imagesToMove = new ArrayList<ImageBean>();
      for(SeriesType series : study.getSeries())
      {
         for(DicomObjectType object : series.getDicomObject())
         {
            if(object instanceof ImageBeanMultiFrame)
               imagesToMove.add((ImageBeanMultiFrame)object);
         }
      }
      
      if(!imagesToMove.isEmpty())
         moveImages(imagesToMove);
   }

   /**
    * Move the images into a new series which is grouped by instance number.
    */
   private void moveImages(List<? extends ImageBean> imagesToMove)
   {
      Map<Integer,SeriesBean> instanceNumberToSeries = new HashMap<Integer,SeriesBean>();
      for(ImageBean image : imagesToMove)
      {
         Integer instanceNumber = image.getInstanceNumber();
         SeriesBean series = instanceNumberToSeries.get(instanceNumber);
         if(series == null)
         {
            String uidPrefix = instanceNumber + "@";
            SeriesBean oldSeries = image.getSeriesBean();
            series = oldSeries.clone(uidPrefix);
            instanceNumberToSeries.put(instanceNumber, series);
         }

         series.move(image);
      }
   }
   
   /**
    * Determine if we should regroup this set of results.
    */
   protected boolean shouldRegroup(Map<String, Object> params)
   {
      if(params == null)
         return false;
      
      String regroup = (String)params.get("regroup");
      return regroup != null &&
         ( regroup.equals("*") || regroup.contains(NAME) || regroup.equalsIgnoreCase("true"));
   }

}
