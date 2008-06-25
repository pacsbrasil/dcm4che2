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
package org.dcm4chee.xero.search;

import java.util.HashMap;
import java.util.Map;

import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.MemoryCacheFilter;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains utilies for calling the various search filters
 * @author bwallace
 */
public class SearchFilterUtils {
   private static final Logger log = LoggerFactory.getLogger(SearchFilterUtils.class);
   /** 
    * Calls the filter item to get the results for search for the given UID.  May use internal knowledge
    * about cached series level results to optimize this by not making actual calls to read the data.
    * If gspsUID is present, then this will cause the GSPS information to be embedded for the given image.
    * @param filterItem
    * @param params
    * @param uid
    * @param gspsUid
    * @return ResultsBean containing at least the given image bean, and GSPS information.
    */
   public static ResultsBean filterImage(FilterItem<?> filterItem, Map<String,Object> params, String uid, String presentationUID) {
	  Map<String,Object> newParams = new HashMap<String,Object>();
	  StringBuffer queryStr = new StringBuffer("&object=").append(uid).append("&presentationUID=").append(presentationUID);
	  newParams.put("objectUID", uid);
	  newParams.put("presentationUID", presentationUID);
	  String frame = (String) params.get("frameNumber");
	  if( frame!=null ) {
		 newParams.put("frameNumber", frame);
		 queryStr.append("&frameNumber="+frame);
	  }
	  newParams.put(MemoryCacheFilter.KEY_NAME,queryStr.toString());
	  log.info("BurnIn Query str for filter image is "+queryStr);
	  return (ResultsBean) filterItem.callNamedFilter("imageSearch", newParams);
   }
}
