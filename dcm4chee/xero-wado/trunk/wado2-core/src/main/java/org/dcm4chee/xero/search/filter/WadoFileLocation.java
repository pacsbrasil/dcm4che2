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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.metadata.filter.MemoryCacheFilter;
import org.dcm4chee.xero.search.AEProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class finds the location of the given dicom object as a WADO request
 * object.
 * 
 * @author bwallace
 * 
 */
public class WadoFileLocation implements Filter<URL> {
   private static final Logger log = LoggerFactory.getLogger(WadoFileLocation.class);
   
   public static final String DICOM_FILE_LOCATION = " DICOM_FILE_LOCATION";
   public static final String WADO_TYPE = "wado";
   public static final String XERO_WADO_TYPE = "xeroWado";

   protected static final String wadoRequired[] = new String[] { "studyUID", "seriesUID", "objectUID", };

   /** Figure out the location of the file, as referenced by SOP instance UID. */
   public URL filter(FilterItem<URL> filterItem, Map<String, Object> params) {
      URL ret = (URL) params.get(DICOM_FILE_LOCATION);
      if (ret != null)
         return ret;

      Map<String, Object> ae = AEProperties.getAE(params);
      String type = FilterUtil.getString(ae,"type");
      if (!(type==null || WADO_TYPE.equals(type) || XERO_WADO_TYPE.equals(type)) ) {
         return filterItem.callNextFilter(params);
      }
      try {
         String wadoUrl = createWadoUrl(params, ae, type);
         if( wadoUrl==null ) return filterItem.callNextFilter(params);
         ret = new URL(wadoUrl);
         params.put(MemoryCacheFilter.CACHE_SIZE, ret.toString().length()*2+32);
         log.debug("Returning WADO url: {}", ret);
         return ret;
      } catch (MalformedURLException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * Figures out the URL to use for the WADO request
    * 
    * @todo change the host to be configurable.
    */
   protected String createWadoUrl(Map<String, ?> args, Map<String,Object> ae, String type) {
      String wadoPath = FilterUtil.getString(ae,"wadoPath");
      if( wadoPath==null ) return null;
      StringBuffer ret = new StringBuffer(FilterUtil.getString(ae,"wadoPath"));
      ret.append("?requestType=WADO&contentType=application%2Fdicom");
      for (String key : wadoRequired) {
         Object value = args.get(key);
         String strValue = (value == null ? null : value.toString().trim());
         if (strValue == null || strValue.equals("")) {
            // Only object UID required for the Xero WADO service.
            if( (type==null || type.equals(XERO_WADO_TYPE)) && ! key.equals("objectUID") ) {
               ret.append("&").append(key).append("=1");
               continue;
            }
            throw new IllegalArgumentException("Required value " + key + " is missing for request.");
         }
         ret.append("&").append(key).append("=").append(strValue);
      }
      if( type==null || type.equals(XERO_WADO_TYPE) ) ret.append("&useOrig=true");
      return ret.toString();
   }

   @MetaData
   public int getPriority() {
      return -1;
   }
}
