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
 * Portions created by the Initial Developer are Copyright (C) 2009
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

import java.util.Map;
import java.util.regex.Pattern;

import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.search.AEProperties;
import org.dcm4chee.xero.search.DicomCFindFilter;
import org.dcm4chee.xero.search.ResultFromDicom;
import org.dcm4chee.xero.wado.WadoParams;

/**
 * Handles querying of multiple AEs. This is a fairly simple implementation that
 * queries sequentially. It is supported either by adding multiple AE names to
 * the query, or by adding to the ae configuration file a queryMultipleAE
 * parameter that is a backslash separated list of AE's to query.
 * 
 * TODO make this multi-threaded, possibly with feedback on results and/or
 * asynchronous output return.
 * 
 * @author bwallace
 * 
 */
public class MultipleAEFilter implements Filter<ResultFromDicom> {

	public static String QUERY_MULTIPLE_AE = "queryMultipleAE";

	private static final Pattern backslashSplit = Pattern.compile("\\\\");

	/** Queries the next AE sequentially */
	public ResultFromDicom filter(FilterItem<ResultFromDicom> filterItem,
			Map<String, Object> params) {
		String ae = FilterUtil.getString(params, WadoParams.AE);
		if (ae != null && ae.indexOf('\\') > 0) {
			String[] aes = backslashSplit.split(ae);
			ResultFromDicom rfd = null;
			for (String singleAe : aes) {
				singleAe = singleAe.trim();
				if (singleAe.length() == 0)
					continue;
				params.put(WadoParams.AE, singleAe);
				rfd = filter(filterItem, params);
				params.put(DicomCFindFilter.EXTEND_RESULTS_KEY, rfd);
			}
			return rfd;
		}

		Map<String, Object> aeprops = AEProperties.getAE(params);
		String multiAES = (String) aeprops.get(QUERY_MULTIPLE_AE);
		if (multiAES == null)
			return filterItem.callNextFilter(params);

		String[] aes = backslashSplit.split(multiAES);
		ResultFromDicom rfd = null;
		for (String singleAe : aes) {
			singleAe = singleAe.trim();
			if (singleAe.length() == 0)
				continue;
			params.put(WadoParams.AE, singleAe);
			rfd = filterItem.callNextFilter(params);
			params.put(DicomCFindFilter.EXTEND_RESULTS_KEY, rfd);
		}
		return rfd;
	}

}
