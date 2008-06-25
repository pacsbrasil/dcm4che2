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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.MemoryCacheFilter;
import org.dcm4chee.xero.search.DicomCFindFilter;
import org.dcm4chee.xero.search.macro.KeyObjectMacro;
import org.dcm4chee.xero.search.study.KeySelection;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter is a series level filter that adds any key images to the return
 * result, even if they come from another study. This organizes them into the
 * primary study.
 * 
 * @author bwallace
 * 
 */
public class KeyObjectSeries extends KeyObjectFilter {
   private static final Logger log = LoggerFactory.getLogger(KeyObjectSeries.class);

   private static final String[] EMPTY_STRING_ARR = new String[0];

   /**
     * Does a secondary query to add the missing items to the return result, and
     * ensures they are marked as key objects as well.
     */
   @Override
   protected void handleMissingItems(FilterItem<ResultsBean> filterItem, Map<String, Object> params, ResultsBean ret, KeyObjectMacro kom,
		 List<KeySelection> missing) {
	  Map<String, Object> newParams = new HashMap<String, Object>();
	  Set<String> uids = new HashSet<String>(newParams.size());
	  StringBuffer queryStr = new StringBuffer("&koUID=").append(params.get(KEY_UID));
	  for (KeySelection key : missing) {
		 if (uids.contains(key.getObjectUid()))
			continue;
		 uids.add(key.getObjectUid());
		 queryStr.append("&objectUID=").append(key.getObjectUid());
	  }
	  String[] uidArr = uids.toArray(EMPTY_STRING_ARR);
	  log.info("Querying for " + uidArr.length + " additional images: " + queryStr);
	  newParams.put("objectUID", uidArr);
	  newParams.put(MemoryCacheFilter.KEY_NAME, queryStr.toString());
	  newParams.put(DicomCFindFilter.EXTEND_RESULTS_KEY, ret);
	  filterItem.callNamedFilter("imageSearch", newParams);
	  List<KeySelection> stillMissing = assignKeyObjectMacro(ret, kom, missing);
	  if (stillMissing != null && !stillMissing.isEmpty()) {
		 log.warn("Could not find " + stillMissing.size() + " items referenced in key object.");
	  }
   }

}
