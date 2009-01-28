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

import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.search.AEProperties;
import org.dcm4chee.xero.search.ResultFromDicom;
import org.dcm4chee.xero.search.study.DicomObjectInterface;
import org.dcm4chee.xero.search.study.DicomObjectType;
import org.dcm4chee.xero.search.study.ImageBean;
import org.dcm4chee.xero.search.study.PatientType;
import org.dcm4chee.xero.search.study.ReportBean;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.dcm4chee.xero.search.study.SeriesType;
import org.dcm4chee.xero.search.study.StudyType;
import org.dcm4chee.xero.wado.DicomFilter;
import org.dcm4chee.xero.wado.WadoParams;

/**
 * This filter allows the actual DICOM header to be read to get missing results
 * information from the DICOM header instead of from the C-Find. It is triggered
 * by the image instance having Rows/Columns of -1, AND the AE being configured
 * to run this.
 * 
 * @author bwallace
 * 
 */
public class PartialImageInfoFix implements Filter<ResultsBean> {

   public static final String PartialImageInfoFix_KEY = "partialImageInfoFix";

   AEProperties aeProperties = AEProperties.getInstance();

   /** Adds Rows,Columns back in if required */
   public ResultsBean filter(FilterItem<ResultsBean> filterItem, Map<String, Object> params) {
      ResultsBean ret = filterItem.callNextFilter(params);
      Map<String, Object> aep = AEProperties.getAE(params);
      if (ret != null && aep != null && (
            FilterUtil.getBoolean(aep, PartialImageInfoFix_KEY) 
            || "mvf".equals(aep.get("type")))) {
         fixPartialImage(params, ret);
      }
      return ret;
   }

   /** Does the iteration over the return results to fix the items */
   protected void fixPartialImage(Map<String, Object> params, ResultsBean ret) {
      String ae = FilterUtil.getString(params, WadoParams.AE);
      for (PatientType pt : ret.getPatient()) {
         for (StudyType st : pt.getStudy()) {
            for (SeriesType se : st.getSeries()) {
               for (DicomObjectType dot : se.getDicomObject()) {
                  if (dot instanceof ImageBean) {
                     ImageBean ib = (ImageBean) dot;
                     if (ib.getRows() >= 0)
                        continue;
                  } else if (dot instanceof ReportBean) {
                     ReportBean rb = (ReportBean) dot;
                     if( rb.getConceptMeaning()!=null ) continue;
                  }
                  DicomObject ds = DicomFilter.callInstanceFilter(dicomImageHeader, (DicomObjectInterface) dot, ae);
                  if (ds == null)
                     continue;
                  // Call the add result a subsequent time.
                  ((ResultFromDicom) dot).addResult(ds);
               }
            }
         }
      }
   }

   private Filter<DicomObject> dicomImageHeader;

   /** Gets the filter that returns the dicom object image header */
   public Filter<DicomObject> getDicomImageHeader() {
      return dicomImageHeader;
   }

   @MetaData(out = "${ref:dicomImageHeader}")
   public void setDicomImageHeader(Filter<DicomObject> dicomImageHeader) {
      this.dicomImageHeader = dicomImageHeader;
   }

}
