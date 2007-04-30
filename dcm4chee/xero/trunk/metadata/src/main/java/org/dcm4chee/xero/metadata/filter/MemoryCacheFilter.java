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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class uses the MemoryCache to remember the returned data and use a 
 * cached instance instead.  
 * @author bwallace
 *
 */
public class MemoryCacheFilter<T extends FilterReturn<?> & CacheItem> implements Filter<T>{
	private static Logger log = LoggerFactory.getLogger(MemoryCacheFilter.class);

	/** The default item to lookup in params to find the key for the cached item. */
	public static final String KEY_NAME="queryStr";
	
	/** The key to use to enable or modify the caching */
	public static final String CACHE_CONTROL="cache-control";
	
	/** The value to use for cache-control to NOT cache the item */
	public static final String NO_CACHE="no-cache";

        public static final long DEFAULT_INITIAL_SIZE = 1024l*1024*10;
	
	protected String paramKeyName = KEY_NAME;
	
	MemoryCache<String, T> cache = new MemoryCache<String,T>();

	/** Create a memory cache filter item with a 10 mb initial size, 2 level
	 * cache.
	 */
	public MemoryCacheFilter() {
		log.info("Loading memory cache filter.");
		setCacheSizes(DEFAULT_INITIAL_SIZE);
	}
	
	/**
	 * Return an item from memory cache if available, otherwise use the 
	 * next filter item to get the returned item.
	 */
	@SuppressWarnings("unchecked")
	public T filter(FilterItem filterItem, Map<String, Object> params) {
		String key = computeKey(params);
		T item = cache.get(key);
		log.debug("Looking for key "+key+" was found? "+(item!=null));
		if( item==null ) {
			item = (T) filterItem.callNextFilter(params);
			if( item!=null && !NO_CACHE.equals(item.getParameter("cache-control"))) cache.put(key,item);
		}
		return item;
	}

	/** Override this method to use a different parameter value to get the 
	 * cached item.
	 * @param params
	 * @return
	 */
	protected String computeKey(Map<String,?> params) {
		Object okey = params.get(paramKeyName);
		if( okey instanceof String ) return (String) okey;
		if( okey instanceof String[] ) return ((String[]) okey)[0];
		return okey.toString();
	}
	
	/**
	 * Sets the size of the cache
	 */
	public void setCacheSizes(long size) {
		cache.setCacheSizes(size);
	}
}
