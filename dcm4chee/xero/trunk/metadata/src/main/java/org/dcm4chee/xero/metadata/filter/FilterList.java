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
package org.dcm4chee.xero.metadata.filter;

import java.util.Map;

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.PreConfigMetaData;

/**
 * A filter list is a filter that is just a collection of child filters.  
 * 
 * @author bwallace
 *
 */
public class FilterList<T> implements Filter<T>, PreConfigMetaData<FilterListConfig>
{

	/** Filters the data.  
	 * 
	 * @param configData contains the pre-computed configuration data.  This data is figured
	 * out from the meta-data typically, and is used to optimize the filter performance.
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T filter(FilterItem filterItem, Map<String, ?> params) {
		FilterListConfig filterListConfig = (FilterListConfig) filterItem.getConfig();
		FilterItem firstFilter = filterListConfig.getFirstFilter();
		if( firstFilter!=null ) {
			Object ret = firstFilter.filter.filter(firstFilter, params);
			if( ret!=null ) return (T) ret;
		}
		return (T) filterItem.callNextFilter(params);
	}

	public FilterListConfig getConfigMetaData(MetaDataBean mdb) {
		return new FilterListConfig(mdb);
	}

}
