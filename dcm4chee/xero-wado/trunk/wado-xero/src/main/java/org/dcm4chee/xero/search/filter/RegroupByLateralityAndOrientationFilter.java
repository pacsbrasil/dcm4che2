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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4chee.xero.metadata.MetaData;
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
import org.dcm4chee.xero.wado.DicomFilter;
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
   
   private Filter<DicomObject> dicomFullHeader;
   private EffectiveLaterality effectiveLaterality;
   private MammoImageFlipper flipper;
   
   public RegroupByLateralityAndOrientationFilter()
   {
      log.info("RegroupByLateralityAndOrientationFilter created.");
      
      this.effectiveLaterality = new EffectiveLaterality();
      this.flipper = new MammoImageFlipper();
   }
   


   /** Gets the filter that returns the dicom object image header */
   public Filter<DicomObject> getDicomFullHeader() {
      return dicomFullHeader;
   }

   /** Sets the full header filter - this returns all the fields, but not updated. */
   @MetaData(out="${ref:dicomFullHeader}")
   public void setDicomFullHeader(Filter<DicomObject> dicomFullHeader) {
      this.dicomFullHeader = dicomFullHeader;
   }
   
   public ResultsBean filter(FilterItem<ResultsBean> filterItem, Map<String, Object> params)
   {
      ResultsBean results = filterItem.callNextFilter(params);
      if(!shouldRegroup(results,params))
         return results;
      
      String aeTitle = results.getAe();
      
      for(PatientType p : results.getPatient())
         for(StudyType s : p.getStudy())
            groupImagesByOrientationAndPosition((StudyBean)s, aeTitle);
      
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
         && ( regroup.equals("*") || regroup.contains(NAME) || regroup.equalsIgnoreCase("true"));
      
      boolean containsMG = false;
      if(parameterSet)
         for(PatientType p : results.getPatient())
            for(StudyType s : p.getStudy())
               if(containsMG = s.getModalitiesInStudy() == null || s.getModalitiesInStudy().contains("MG"))
                  break;
      
      return containsMG;
   }

   protected void groupImagesByOrientationAndPosition(StudyBean study,String aeTitle)
   {
      List<ImageBean> imagesToMove = new ArrayList<ImageBean>();

      for(SeriesType series : study.getSeries())
      {
         if("MG".equals(series.getModality()))
         {
            for(DicomObjectType o : series.getDicomObject())
            {
               if(!(o instanceof ImageBean))
                  continue;
               imagesToMove.add((ImageBean)o);
            }
         }
      }
      
      moveImages(imagesToMove,aeTitle);
      sortSeries(study);
   }
   
   /**
    * Ensure that the View Code sequence and either image laterality or patient orientation 
    * are available in the DicomObject.
    */
   private boolean ensureDicomElementsAvailable(DicomObject dicom)
   {
      if(dicom == null)
         return false;
      
      boolean viewCodeAvailable = ViewCode.containsViewCodeSequence(dicom);
      boolean lateralityAvailable = Laterality.containsImageLaterality(dicom) || Orientation.containsPatientOrientation(dicom);
      
      boolean dicomElementsAvailable = viewCodeAvailable && lateralityAvailable;
      
      if(! dicomElementsAvailable )
      {
         log.debug("Insufficient DICOM elements to regroup: ViewCode={}, Laterality={}",
               viewCodeAvailable,lateralityAvailable);
      }
      
      return dicomElementsAvailable;
   }

   /**
    * Move the indicated images to new series.
    * @param imagesToMove
    */
   protected void moveImages(Collection<ImageBean> imagesToMove, String aeTitle)
   {
      Map<String,SeriesBean> descriptionToSeries = new HashMap<String,SeriesBean>();
      for(ImageBean i : imagesToMove)
      {
         DicomObject dicom = retrieveDicomObjectWithRequiredElements(i,aeTitle);
         
         // Flip the image if it is not properly oriented.
         if(flipper.isFlipRequired(dicom))
            flipper.flip(i);
         
         String description = generateSeriesDescription(i,dicom);
         
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
    * Sort the series into the correct order:  L before R.  CC before MLO.
    */
   protected void sortSeries(StudyBean study)
   {
      Collections.sort(study.getSeries(),new SeriesDescriptionComparator());
   }


   /**
    * Generate a series description of the form {Laterality} {View Code} i.e. "L CC" or "R MLO"
    */
   protected String generateSeriesDescription(ImageBean image,DicomObject dicom)
   {
      if(image == null)
         return "";
      
      String seriesDescription;
      if(dicom != null)
      {
         Laterality laterality = effectiveLaterality.parseEffectiveLaterality(dicom);
         ViewCode viewCode = new ViewCode(dicom);
         seriesDescription = generateSeriesDescription(laterality, viewCode);
      }
      else
      {
         log.warn("Insufficient DICOM elements to regroup objectUID={}",image.getObjectUID());
         seriesDescription = null;
      }
      
      return seriesDescription;
   }
   
   /**
    * Retrieve the DICOM Object that contains the elements required to perform
    * the regrouping of this image.
    * @return a DicomObject with all required fields, or NULL if non could be found.
    */
   private DicomObject retrieveDicomObjectWithRequiredElements(ImageBean image, String aeTitle)
   {
      DicomObject dicom = image.getCfindHeader();
      boolean dicomElementsAvailable = ensureDicomElementsAvailable(dicom);
      if(! dicomElementsAvailable  && dicomFullHeader != null)
      {
         log.debug("Insufficent DICOM elements in C-FIND response.  Header will be retrieved.");
         dicom = DicomFilter.callInstanceFilter(dicomFullHeader, image ,aeTitle);
         dicomElementsAvailable = ensureDicomElementsAvailable(dicom);
      }
      
      if(! dicomElementsAvailable )
         dicom = null;
      
      return dicom;
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
   
   
   /**
    * Comparator that will move series based on the content of their 
    *
    * @author Andrew Cowan (amidx)
    */
   private static class SeriesDescriptionComparator implements Comparator<SeriesType>
   {
      public int compare(SeriesType s1, SeriesType s2)
      {
         String d1 = s1.getSeriesDescription();
         String d2 = s2.getSeriesDescription();
         
         if(d1 == d2)
            return 0;
         else if(d1 == null)
            return -1;
         else if(d2 == null)
            return 1;
         else
            return compareDescriptions(d1,d2);
         
      }

      private int compareDescriptions(String d1, String d2)
      {
         int compare = 0;
         
         char o1 = d1.charAt(0);
         char o2 = d2.charAt(0);
         
         String l1 = d1.substring(2);
         String l2 = d2.substring(2);
         
         if(l1.equals(l2))
            if(o1 == o2)
               compare = 0;
            else
               compare = o1 == 'L' ? -1 : 1;
         else if(l1.equals("CC") && l2.equals("MLO"))
            compare = -1;
         else if(l1.equals("MLO") && l2.equals("CC"))
            compare = 1;
         else
            compare = 0;
         
         return compare;
      }
   }
}
