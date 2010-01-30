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
 * Portions created by the Initial Developer are Copyright (C) 2008
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

package org.dcm4chee.xero.wado.multi;

import static org.dcm4chee.xero.wado.WadoParams.OBJECT_UID;
import static org.dcm4chee.xero.wado.WadoParams.SERIES_UID;
import static org.dcm4chee.xero.wado.WadoParams.STUDY_UID;

import java.util.Iterator;
import java.util.Map;

import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;
import org.dcm4chee.xero.search.study.DicomObjectType;
import org.dcm4chee.xero.search.study.PatientType;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.dcm4chee.xero.search.study.SeriesType;
import org.dcm4chee.xero.search.study.StudyType;
import org.dcm4chee.xero.wado.WadoParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Series UID It Filter allows the multi-part/mixed type WADO responses to
 * specify only a series UID or study UID and get back all the values for the
 * given type.
 * 
 * @author bwallace
 */
public class SeriesUidItFilter implements Filter<Iterator<ServletResponseItem>> {
   private static final Logger log = LoggerFactory.getLogger(SeriesUidItFilter.class);

   Filter<ResultsBean> imageFilter;

   /** Return an iterator over all the requested items at the series level */
   public Iterator<ServletResponseItem> filter(FilterItem<Iterator<ServletResponseItem>> filterItem, Map<String, Object> params) {
      if (params.containsKey("objectUID")) {
         log.debug("Not iterating over series - already has an object UID specified.");
         return filterItem.callNextFilter(params);
      }
      if (!(params.containsKey(STUDY_UID) || params.containsKey(SERIES_UID))) {
         log.warn("No study UID and series UID provided.");
         return null;
      }

      StringBuffer objects = new StringBuffer();
      ResultsBean rt = filterSeriesImage(imageFilter, params);
      if (rt == null || rt.getPatient().size() == 0 || rt.getPatient().get(0).getStudy().size() == 0) {
         log.info("No results found for query, so no return values.");
         return null;
      }
      PatientType pt = rt.getPatient().get(0);
      if (rt.getPatient().size() > 1 || pt.getStudy().size() > 1) {
         log.info("More than 1 study requested - only allowed to request 1 study at a time.");
         return null;
      }
      StudyType st = pt.getStudy().get(0);
      boolean first = true;
      for (SeriesType set : st.getSeries()) {
         for (DicomObjectType dot : set.getDicomObject()) {
            if (first) {
               first = false;
            } else {
               objects.append('\\');
            }
            objects.append(dot.getObjectUID());
         }
      }
      String objectUids = objects.toString();
      log.info("Returning objects for {}", objectUids);
      params.put(OBJECT_UID, objectUids);

      return filterItem.callNextFilter(params);
   }

   public Filter<ResultsBean> getImageFilter() {
      return imageFilter;
   }

   /** Sets the image source to allow finding the image UID's to respond with. */
   @MetaData(out = "${ref:imageSource}")
   public void setImageFilter(Filter<ResultsBean> imageFilter) {
      this.imageFilter = imageFilter;
   }

   /** Finds the correct study return value */
   private ResultsBean filterSeriesImage(Filter<ResultsBean> imageFilter, Map<String, Object> params) {
      Map<String, Object> childParams = FilterUtil.copyParams(params,WadoParams.AE, 
            WadoParams.STUDY_UID, WadoParams.SERIES_UID);
      return imageFilter.filter(null, childParams);
   }
}
