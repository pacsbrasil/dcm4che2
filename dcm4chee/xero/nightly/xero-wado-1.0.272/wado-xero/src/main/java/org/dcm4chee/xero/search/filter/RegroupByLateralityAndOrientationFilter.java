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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.search.study.DicomObjectType;
import org.dcm4chee.xero.search.study.EffectiveLaterality;
import org.dcm4chee.xero.search.study.ImageBean;
import org.dcm4chee.xero.search.study.Laterality;
import org.dcm4chee.xero.search.study.MammoImageFlipper;
import org.dcm4chee.xero.search.study.Orientation;
import org.dcm4chee.xero.search.study.PatientType;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.dcm4chee.xero.search.study.SeriesBean;
import org.dcm4chee.xero.search.study.SeriesType;
import org.dcm4chee.xero.search.study.StudyBean;
import org.dcm4chee.xero.search.study.StudyType;
import org.dcm4chee.xero.search.study.ViewCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Filter that will regroup Mammography studies into series of like position and orientation.
 * 
 * @author Andrew Cowan (amidx)
 */
public class RegroupByLateralityAndOrientationFilter implements Filter<ResultsBean>
{
   private static final Logger log = LoggerFactory.getLogger(RegroupByLateralityAndOrientationFilter.class);

   private static final String NAME = RegroupByLateralityAndOrientationFilter.class.getSimpleName();
   
   private EffectiveLaterality effectiveLaterality = new EffectiveLaterality();
   
   public ResultsBean filter(FilterItem<ResultsBean> filterItem, Map<String, Object> params)
   {
      ResultsBean results = filterItem.callNextFilter(params);
      if(!shouldRegroup(results,params))
         return results;
      
      log.info("Regrouping MG by laterality/view");
      String aeTitle = results.getAe();
      for(PatientType p : results.getPatient())
         for(StudyType s : p.getStudy())
            groupImagesByOrientationAndPosition((StudyBean)s,aeTitle);
      
      return results;
   }
   
   /**
    * Determine if we should regroup this set of results.
    * <p>
    * <ul>
    * <li>Parameters must contain the name of the filter or '*' for the 'regroup' key.
    * <li>The ModalitiesInStudy attribute must either contain MG or be empty (since this is not a required DICOM field
    * </ul>
    */
   protected boolean shouldRegroup(ResultsBean results,Map<String, Object> params)
   {
      if(params == null || results == null)
         return false;
      
      String regroup = (String)params.get("regroup");
      boolean parameterSet = regroup != null 
         && ( regroup.equals("*") || regroup.contains(NAME));
      
      boolean containsMG = false;
      for(PatientType p : results.getPatient())
         for(StudyType s : p.getStudy())
            if(containsMG = s.getModalitiesInStudy() == null || s.getModalitiesInStudy().contains("MG"))
               break;
      
      return parameterSet && containsMG;
   }

   protected void groupImagesByOrientationAndPosition(StudyBean study,String aeTitle)
   {
      List<ImageBean> imagesToMove = new ArrayList<ImageBean>();

      for(SeriesType series : study.getSeries())
      {
         if(!"MG".equals(series.getModality()))
               continue;
         
         for(DicomObjectType o : series.getDicomObject())
         {
            if(!(o instanceof ImageBean))
               continue;
            
            ImageBean i = (ImageBean)o;
            
            boolean viewCodeAvailable = ensureViewCodeIsAvailable(i,aeTitle);
            boolean lateralityAvailable = ensureLateralityIsAvailable(i,aeTitle);
            
            if(viewCodeAvailable && lateralityAvailable)
               imagesToMove.add(i);
         }
      }
      
      moveImages(imagesToMove);
   }
   

   /**
    * Check to make sure that the Laterality is available for this instance.
    * If the laterality is not available, then request the full header to get it.
    */
   private boolean ensureLateralityIsAvailable(ImageBean i,String aeTitle)
   {
      if(i == null)
         return false;
      
      DicomObject dicom = i.getCfindHeader();
      return Laterality.containsImageLaterality(dicom) || Orientation.containsPatientOrientation(dicom);
   }

   /**
    * Check if the ViewCode is available, and if it is not then pull the full header
    * and attach them.
    * @return Whether the ViewCode is available
    */
   private boolean ensureViewCodeIsAvailable(ImageBean i,String aeTitle)
   {
      if(i == null) 
         return false;
      
      DicomObject dicom = i.getCfindHeader();
      return ViewCode.containsViewCodeSequence(dicom);
   }

   /**
    * Move the indicated images to new series.
    * @param imagesToMove
    */
   protected void moveImages(Collection<ImageBean> imagesToMove)
   {
      Map<String,SeriesBean> descriptionToSeries = new HashMap<String,SeriesBean>();
      for(ImageBean i : imagesToMove)
      {
         String description = generateSeriesDescription(i);
         
         if(description == null) 
            continue;
         
         SeriesBean newSeries = descriptionToSeries.get(description);
         if(newSeries == null)
         {
            SeriesBean oldSeries = i.getSeriesBean();
            newSeries = oldSeries.clone(description);
            newSeries.setSeriesDescription(description);
            descriptionToSeries.put(description, newSeries);
         }
         
         newSeries.move(i);
      }
   }
   
   /**
    * Generate a series description of the form {Laterality} {View Code} i.e. "L CC" or "R MLO"
    */
   protected String generateSeriesDescription(ImageBean image)
   {
      if(image == null)
         return "";
      
      DicomObject dicom = image.getCfindHeader();
      Laterality laterality = effectiveLaterality.parseEffectiveLaterality(dicom);
      ViewCode viewCode = new ViewCode(dicom);
      return generateSeriesDescription(laterality, viewCode);
   }
   
   /**
    * Generate a series description of the form {Laterality} {View Code} i.e. "L CC" or "R MLO"
    */
   protected String generateSeriesDescription(Laterality laterality, ViewCode viewCode)
   {
      if(laterality == null || viewCode == null)
         return "";
      
      StringBuilder sb = new StringBuilder();
      sb.append(laterality.getDicomCode());
      sb.append(' ');
      sb.append(viewCode.getDescription());
      return sb.toString();
   }
}
