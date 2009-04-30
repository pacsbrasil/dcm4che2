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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dcm4che2.data.DicomObject;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.search.DicomCFindFilter;
import org.dcm4chee.xero.search.ResultFromDicom;
import org.dcm4chee.xero.search.macro.KeyObjectMacro;
import org.dcm4chee.xero.search.macro.OriginalStudyMacro;
import org.dcm4chee.xero.search.study.DicomObjectType;
import org.dcm4chee.xero.search.study.ImageBean;
import org.dcm4chee.xero.search.study.ImageBeanMultiFrame;
import org.dcm4chee.xero.search.study.KeyObjectBean;
import org.dcm4chee.xero.search.study.KeySelection;
import org.dcm4chee.xero.search.study.MacroItems;
import org.dcm4chee.xero.search.study.MacroMixIn;
import org.dcm4chee.xero.search.study.PatientType;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.dcm4chee.xero.search.study.SeriesBean;
import org.dcm4chee.xero.search.study.StudyBean;
import org.dcm4chee.xero.search.study.StudyType;
import org.dcm4chee.xero.wado.DicomFilter;
import org.dcm4chee.xero.wado.WadoParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter adds an indication of whether an image is a key object. It takes
 * a single koUID=X as a parameter, and adds that key object to the set of key
 * objects in the return value. It then marks all images that are found that
 * exist in the key object set with the key object, including the GSPS UID if
 * any. Thus, this filter should run AFTER the GspsUid filter. Some specific
 * handling: 1. If an image is found more than once, with a different GSPS UID,
 * then it will be "cloned" to a new UID. 2. If an image has no GSPS, then
 * whatever GSPS the user applies by default will be shown.
 * 
 * This is not the filter to use to display the key object set as a series tray
 * level display,
 * 
 * @see{KeyObjectSeries for a filter at the series level.
 * @author bwallace
 * 
 */
public class KeyObjectFilter implements Filter<ResultsBean> {
   private static final Logger log = LoggerFactory.getLogger(KeyObjectFilter.class);

   public static final String KEY_UID = "koUID";

   private static final String[] EMPTY_STRING_ARR = new String[0];

   private Filter<DicomObject> dicomFullHeader;

   private Filter<ResultFromDicom> imageSource;

   public ResultsBean filter(FilterItem<ResultsBean> filterItem, Map<String, Object> params) {
      String koUid = (String) params.get(KEY_UID);
      if (koUid == null || koUid.equals("")) {
         return filterItem.callNextFilter(params);
      }

      ResultsBean ret = null;
      if (koUid.equals("*")) {
         // Include information on every key object
         ret = filterItem.callNextFilter(params);
         ret = addKeyObjectMacro(filterItem, params, koUid, ret);
      } else {
         // Construct a custom object set containing only the referenced
         // images, SR, KO and PR series.
         ret = queryForModality(filterItem, params, "KO", ret);
         ret = queryForModality(filterItem, params, "SR", ret);
         ret = queryForModality(filterItem, params, "PR", ret);
         addKeyObjectMacro(filterItem, params, koUid, ret);
      }
      return ret;
   }

   /**
    * Queries for a particular modality, adding it to the base set of objects
    */
   protected ResultsBean queryForModality(FilterItem<ResultsBean> filterItem, Map<String, Object> params, String modality,
         ResultsBean ret) {
      if (ret != null)
         params.put(DicomCFindFilter.EXTEND_RESULTS_KEY, ret);
      params.put(WadoParams.MODALITY, modality);
      ret = filterItem.callNextFilter(params);
      return ret;
   }

   /**
    * Gets the default search return, and then calls the key object macro
    * assignment.
    * 
    * @param filterItem
    * @param params
    * @param queryKoUid
    * @return
    */
   protected ResultsBean addKeyObjectMacro(FilterItem<ResultsBean> filterItem, Map<String, Object> params, String queryKoUid,
         ResultsBean ret) {
      // Don't remove the koUID as it is part of the cache key still
      if (ret == null)
         return null;
      List<KeyObjectMacro> koms = new ArrayList<KeyObjectMacro>();
      ResultsBean imgRes = null;
      for (int patientI = 0; patientI < ret.getPatient().size(); patientI++) {
         PatientType patient = ret.getPatient().get(patientI);
         for (StudyType studyType : patient.getStudy()) {
            StudyBean study = (StudyBean) studyType;
            if (queryKoUid.equals("*")) {
               // search for all Key Objects under this study
               List<DicomObjectType> kobs = study.searchStudy("KO");
               for (DicomObjectType objT : kobs) {
                  String koUidDiscovered = objT.getObjectUID();
                  log.info("Adding key object information for studyKo={}", queryKoUid);
                  KeyObjectBean kob = queryForKO(dicomFullHeader, params, koUidDiscovered, ret);
                  if (kob == null) {
                     KeyObjectMacro kom = new KeyObjectMacro("ERROR:Not found " + koUidDiscovered);
                     log.warn("Key object " + koUidDiscovered + " not found.");
                     study.getMacroItems().addMacro(kom);
                     continue;
                  }
                  KeyObjectMacro komDiscovered = new KeyObjectMacro(koUidDiscovered);
                  checkAndAddKeyObjectMacro(komDiscovered, study.getMacroItems());
                  koms.add(komDiscovered);
               }
            } else {
               KeyObjectBean kob = queryForKO(dicomFullHeader, params, queryKoUid, ret);
               if (kob == null) {
                  KeyObjectMacro kom = new KeyObjectMacro("ERROR:Not found " + queryKoUid);
                  log.warn("Key object " + queryKoUid + " not found.");
                  study.getMacroItems().addMacro(kom);
                  continue;
               }
               KeyObjectMacro komDiscovered = new KeyObjectMacro(queryKoUid);
               checkAndAddKeyObjectMacro(komDiscovered, study.getMacroItems());
               koms.add(komDiscovered);
               
               // check if there is any Key image missing in the ResultsBean to be fetched from other studies
               List<KeySelection> missing = getMissingKOSelectionImages(koms, ret);
               if (missing != null && !missing.isEmpty()) {
                  imgRes = queryForMissingImages(filterItem, params, ret, missing);
               }
            }
         }
      }

      assignKeyObjectMacroToImageSelection(ret, koms, imgRes);

      return ret;
   }

   /** Gets the filter that returns the dicom object image header */
   public Filter<DicomObject> getDicomFullHeader() {
      return dicomFullHeader;
   }

   /**
    * Sets the full header filter - this returns all the fields, but not
    * updated.
    */
   @MetaData(out = "${ref:dicomFullHeader}")
   public void setDicomFullHeader(Filter<DicomObject> dicomFullHeader) {
      this.dicomFullHeader = dicomFullHeader;
   }

   /** Assigns the key object macro instance for the listed selection 
	 */
   public static List<KeySelection> assignKeyObjectMacro(ResultsBean ret, KeyObjectMacro kom, List<KeySelection> selection) {
      List<KeySelection> missing = null;

      // Setup series map for this object.
      KeyObjectBean kob = (KeyObjectBean) ret.getChildren().get(kom.getKeyObject());
      if (kob == null)
         throw new NullPointerException("Key object bean should not be null.");
      StudyBean study = kob.getSeriesBean().getStudyBean();
      String koStudyUID = study.getStudyUID();
      Map<String, SeriesBean> availSeries = new HashMap<String, SeriesBean>();
      for (Object set : study.getSeries()) {
         SeriesBean se = (SeriesBean) set;
         availSeries.put(se.getSeriesUID(), se);
      }

      for (KeySelection key : selection) {
         MacroMixIn mmi = (MacroMixIn) ret.getChildren().get(key.getObjectUid());
         if (mmi == null) {
            if (missing == null)
               missing = new ArrayList<KeySelection>();
            log.info("Missing child " + key.getObjectUid());
            missing.add(key);
            continue;
         }
         if (key.getFrame() != 0) {
            ImageBeanMultiFrame image = (ImageBeanMultiFrame) mmi;
            mmi = image.getImageFrame(key.getFrame());
         }
         // Might have already added one of these - if so, don't do it again.
         KeyObjectMacro existingMacro = (KeyObjectMacro) mmi.getMacroItems().findMacro(KeyObjectMacro.class);

         if (existingMacro != null) {
            if (kom.getKeyObject().equals(existingMacro.getKeyObject())) {
               continue;
            } else {
               mmi.getMacroItems().removeMacro(existingMacro);
               mmi.getMacroItems().addMacro(new KeyObjectMacro(existingMacro.getKeyObject() + "," + kom.getKeyObject()));
            }
         } else {
            mmi.getMacroItems().addMacro(kom);
         }

         // TODO: Fix: Can only move images over to the right study, not
         // other types of objects.
         // at least at the moment.
         if (mmi instanceof ImageBean) {
            ImageBean image = (ImageBean) mmi;
            SeriesBean imgSeries = image.getSeriesBean();
            SeriesBean series = availSeries.get(imgSeries.getSeriesUID());
            // Identity comparison is acceptable here.
            if (series == null) {
               series = new SeriesBean(study, imgSeries);
               series.getDicomObject().clear();
               series.setViewable(null);
               study.getSeries().add(series);
               log.info("Creating new synthetic series for key objects in " + study.getStudyUID() + " for series "
                     + series.getSeriesUID());
               ret.getChildren().put("ko" + imgSeries.getSeriesUID(), series);
               availSeries.put(series.getSeriesUID(), series);
            }
            if (series != imgSeries) {
               log.info("Adding existing image to synthetic series.");
               ImageBean addImage = image.clone(null);
               addImage.setSeriesBean(series);
               addImage.setPosition(null);
               series.getDicomObject().add(addImage);
            }

            String imgSeriesStudyUID = imgSeries.getStudyBean().getStudyUID();
            if (!imgSeriesStudyUID.equals(koStudyUID)) {
               if (series.getMacroItems().findMacro(OriginalStudyMacro.class) == null) {
                  series.getMacroItems().addMacro(new OriginalStudyMacro(imgSeriesStudyUID));
               }
            }
         }
         if (key.getGspsUid() != null) {
            ImageBean image = (ImageBean) mmi;
            image.setGspsUID(key.getGspsUid());
         }
      }
      return missing;
   }

   /**
    * Check if koUID entry is already present, if present and not equal append
    * to the existing one
    * 
    * @param kom
    * @param macroItems
    * @return
    */
   private static boolean checkAndAddKeyObjectMacro(KeyObjectMacro kom, MacroItems macroItems) {
      KeyObjectMacro existingMacro = (KeyObjectMacro) macroItems.findMacro(KeyObjectMacro.class);
      if (existingMacro != null) {
         if (kom.getKeyObject().equals(existingMacro.getKeyObject())) {
            return false;
         } else {
            macroItems.removeMacro(existingMacro);
            macroItems.addMacro(new KeyObjectMacro(existingMacro.getKeyObject() + "," + kom.getKeyObject()));
         }
      } else {
         macroItems.addMacro(kom);
      }
      return true;
   }

   /**
    * @param koms
    * @param ret
    * @return
    */
   private List<KeySelection> getMissingKOSelectionImages(List<KeyObjectMacro> koms, ResultsBean ret) {
      List<KeySelection> missing = null;
      for (KeyObjectMacro kom : koms) {
         KeyObjectBean kob = (KeyObjectBean) ret.getChildren().get(kom.getKeyObject());
         for (KeySelection key : kob.getKeySelection()) {
            MacroMixIn mmi = (MacroMixIn) ret.getChildren().get(key.getObjectUid());
            if (mmi == null) {
               if (missing == null)
                  missing = new ArrayList<KeySelection>();
               log.debug("Missing child " + key.getObjectUid());
               missing.add(key);
               continue;
            }
         }
      }
      return missing;
   }

   /**
    * Set the <tt>KeyObjectMacro</tt> to the Key selections.
    * 
    * @param ret
    * @param koms
    * @param imgRes
    */
   public static void assignKeyObjectMacroToImageSelection(ResultsBean ret, List<KeyObjectMacro> koms, ResultsBean imgRes) {
      for (KeyObjectMacro kom : koms) {
         KeyObjectBean kob = (KeyObjectBean) ret.getChildren().get(kom.getKeyObject());
         if (kob == null)
            throw new NullPointerException("Key object bean should not be null.");
         StudyBean study = kob.getSeriesBean().getStudyBean();
         String koStudyUID = study.getStudyUID();

         Map<String, SeriesBean> availSeries = getAvailableSeries(study);

         for (KeySelection key : kob.getKeySelection()) {
            MacroMixIn mmi = getKeyImage(ret, imgRes, key);
            if (mmi == null) {
               log.warn("Missing child " + key.getObjectUid());
               continue;
            }

            if (key.getFrame() != 0) {
               ImageBeanMultiFrame image = (ImageBeanMultiFrame) mmi;
               mmi = image.getImageFrame(key.getFrame());
            }

            if (!checkAndAddKeyObjectMacro(kom, mmi.getMacroItems())) {
               continue;
            }

            // TODO: Fix: Can only move images over to the right study, not
            // other types of objects.
            // at least at the moment.
            if (mmi instanceof ImageBean) {
               ImageBean image = (ImageBean) mmi;
               SeriesBean imgSeries = image.getSeriesBean();
               SeriesBean series = availSeries.get(imgSeries.getSeriesUID());
               // Identity comparison is acceptable here.
               if (series == null) {
                  series = new SeriesBean(study, imgSeries);
                  series.getDicomObject().clear();
                  series.setViewable(null);
                  study.getSeries().add(series);
                  log.info("Creating new synthetic series for key objects in " + study.getStudyUID() + " for series "
                        + series.getSeriesUID());
                  ret.getChildren().put("ko" + imgSeries.getSeriesUID(), series);
                  availSeries.put(series.getSeriesUID(), series);
               }
               
               if (isImageFoundInSeries(image, series)) {
                  continue;
               }
               
               if (series != imgSeries) {
                  log.info("Adding existing image to synthetic series.");
                  ImageBean addImage = image.clone(null);
                  addImage.setSeriesBean(series);
                  addImage.setPosition(null);
                  
                  series.getDicomObject().add(addImage);
               }

               String imgSeriesStudyUID = imgSeries.getStudyBean().getStudyUID();
               if (!imgSeriesStudyUID.equals(koStudyUID)) {
                  if (series.getMacroItems().findMacro(OriginalStudyMacro.class) == null) {
                     series.getMacroItems().addMacro(new OriginalStudyMacro(imgSeriesStudyUID));
                  }
               }
            }
            if (key.getGspsUid() != null) {
               ImageBean image = (ImageBean) mmi;
               image.setGspsUID(key.getGspsUid());
            }
         }
      }
   }

   private static boolean isImageFoundInSeries(ImageBean image, SeriesBean series) {
      boolean present = false;
      for (DicomObjectType obj : series.getDicomObject())  {
         if (obj.getObjectUID().equals(image.getObjectUID()))
               present = true;
      }
      return present;
   }

   /**
    * fetch image from the ret if not present fetch from imgRes. Return
    * <tt>null</tt> if no image found.
    * 
    * @param ret
    * @param imgRes
    * @param key
    * @return
    */
   private static MacroMixIn getKeyImage(ResultsBean ret, ResultsBean imgRes, KeySelection key) {
      MacroMixIn mmi = (MacroMixIn) ret.getChildren().get(key.getObjectUid());
      if (mmi == null) {
         if (imgRes != null) {
            mmi = (MacroMixIn) imgRes.getChildren().get(key.getObjectUid());
         }
      }
      return mmi;
   }

   /**
    * @param study
    * @return
    */
   private static Map<String, SeriesBean> getAvailableSeries(StudyBean study) {
      Map<String, SeriesBean> availSeries = new HashMap<String, SeriesBean>();
      for (Object set : study.getSeries()) {
         SeriesBean se = (SeriesBean) set;
         availSeries.put(se.getSeriesUID(), se);
      }
      return availSeries;
   }

   /** Handle any missing items - by default, does nothing unless items are missing. 
     */
   protected ResultsBean queryForMissingImages(FilterItem<ResultsBean> filterItem, Map<String, Object> params, ResultsBean ret,
         List<KeySelection> missing) {
      Map<String, Object> newParams = new HashMap<String, Object>();
      
      Map<String,Map<String,Set<String>>> queries = new HashMap<String,Map<String,Set<String>>>();

      for (KeySelection key : missing) {
         String seriesUid = key.getSeriesUid();
         String studyUid = key.getStudyUid();
         if( studyUid==null ) {
        	 studyUid="";
        	 log.warn("Key object does not specify a study UID."+params.get("koUID"));
         }
         if( seriesUid==null ) {
        	 seriesUid="";
        	 log.warn("Key object does not specify a series UID."+params.get("koUID"));
         }
         
         Map<String,Set<String>> series = queries.get(studyUid);
         if( series==null ) {
        	 series = new HashMap<String,Set<String>>();
        	 queries.put(studyUid,series);
         }
         Set<String> uids = series.get(seriesUid);
         if( uids==null ) {
        	 uids = new HashSet<String>();
        	 series.put(seriesUid,uids);
         }
         
         if (uids.contains(key.getObjectUid()))
            continue;
         uids.add(key.getObjectUid());
      }
      
      String ae = (String) params.get(WadoParams.AE);
      if( ae!=null ) {
    	  log.info("Querying ae "+ae);
          newParams.put(WadoParams.AE,ae);
      }
      log.debug("Putting extend results key into new params:"+ret);
      newParams.put(DicomCFindFilter.EXTEND_RESULTS_KEY,ret);
      for(String studyUid : queries.keySet()) {
    	  Map<String,Set<String>> series = queries.get(studyUid);
          if( !studyUid.isEmpty() ) {
              newParams.put(WadoParams.STUDY_UID, studyUid);
          }
    	  for(String seriesUid : series.keySet()) {
    	      if( !seriesUid.isEmpty()) {
    	          newParams.put(WadoParams.SERIES_UID, seriesUid);
    	      }
    		  Set<String> uids = series.get(seriesUid);
    	      String[] uidArr = uids.toArray(EMPTY_STRING_ARR);
    	      newParams.put(WadoParams.OBJECT_UID, uidArr);
   	          int startSize = ret.getChildren().size();
   	          imageSource.filter(null, newParams);
   	          int endSize = ret.getChildren().size();
   	          if( (endSize-startSize)<uidArr.length) {
   	        	  log.warn("Added {} children - expected to add {} (or perhaps 1 more)", endSize-startSize, uidArr.length);
   	   	          log.warn("Object UID queries &studyUID="+studyUid + "&seriesUID="+seriesUid+"&objectUID="+ toString(uidArr));
   	          }
    	  }
      }
      return ret;
   }
   
   public static String toString(String[] arr) {
       StringBuffer ret = new StringBuffer();
       boolean first = true;
       for(String s : arr) {
          if( !first ) ret.append("\\");
          first = false;
          ret.append(s);
       }
       return ret.toString();
   }

   /**
    * This method queries for the specified KO object, adding it to the result.
    * Only one key object can be specified at a time. The object will be
    * returned, as well as being added to the results bean object.
    */
   public static KeyObjectBean queryForKO(Filter<DicomObject> dicomHeaderFilter, Map<String, Object> params, String koUid,
         ResultsBean rb) {
      DicomObject dobj = DicomFilter.callInstanceFilter(dicomHeaderFilter, params, koUid);
      if (dobj == null)
         return null;
      KeyObjectBean kob = (KeyObjectBean) rb.getChildren().get(koUid);
      if (kob == null) {
         rb.addResult(dobj);
         kob = (KeyObjectBean) rb.getChildren().get(koUid);
         assert kob != null;
      } else {
         kob.initKeySelection(dobj);
      }
      return kob;
   }

   /**
    * @return imagesource
    */
   public Filter<ResultFromDicom> getImageSource() {
      return imageSource;
   }

   /**
    * Sets the filter to use for an image search.
    * 
    * @param imageSource
    */
   @MetaData(out = "${ref:imageSource.imageSearch}")
   public void setImageSource(Filter<ResultFromDicom> imageSource) {
      this.imageSource = imageSource;
   }
}
