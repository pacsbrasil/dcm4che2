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
import org.dcm4chee.xero.metadata.MetaDataUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class uses the MemoryCache to remember the returned data and use a 
 * cached instance instead.  
 * @author bwallace
 *
 */
public class MemoryCacheFilterBase<T extends CacheItem> implements MetaDataUser {
	private static Logger log = LoggerFactory.getLogger(MemoryCacheFilterBase.class);

	/** The default item to lookup in params to find the key for the cached item. */
	public static final String KEY_NAME="queryStr";
	
	/** The key to use to enable or modify the caching */
	public static final String CACHE_CONTROL="cache-control";
	
	/** The value to use for cache-control to NOT cache the item */
	public static final String NO_CACHE="no-cache";
	
	/** The key to use in the meta-data for the size */
	public static final String CACHE_SIZE="cacheSize";

	/** The default cache size - should probably come from meta-data
	 * TODO Change this to some appropriate value 
	 */
    public static final long DEFAULT_INITIAL_SIZE_BYTES = 1024l*1024*10;
	
	protected String paramKeyName = KEY_NAME;
	
	MemoryCache<String, T> cache = new MemoryCache<String,T>();

	/** Create a memory cache filter item with a 10 mb initial size, 2 level
	 * cache.
	 */
	public MemoryCacheFilterBase() {
		log.info("Loading memory cache filter.");
		setCacheSizes(DEFAULT_INITIAL_SIZE_BYTES);
	}
	
	/** Override this method to use a different parameter value to get the 
	 * cached item.
	 * @param params
	 * @return
	 */
	protected String computeKey(Map<String,?> params) {
		if( params==null ) throw new IllegalArgumentException("Params to filter and compute key should not be null.");		
		Object okey = params.get(paramKeyName);
		log.info("Looking for key in "+paramKeyName+" found "+okey);
		if( okey instanceof String ) return (String) okey;
		if( okey instanceof String[] ) throw new IllegalArgumentException("Memory cache key must be single valued.");
		if( okey==null ) {
			log.warn("Memory cache must have a key value.");
			return null;
		}
		return okey.toString();
	}
	
	/**
	 * Sets the size of the cache in bytes.
	 */
	public void setCacheSizes(long size) {
		cache.setCacheSizes(size);
	}

	public void setMetaData(MetaDataBean metaDataBean) {
		String keyName = (String) metaDataBean.getValue(KEY_NAME);
		log.info("Setting key name for parameter to "+keyName);
		if( keyName!=null ) paramKeyName = keyName;
		String cacheSize = (String) metaDataBean.getValue(CACHE_SIZE);
		if( cacheSize!=null ) setCacheSizes(Long.parseLong(cacheSize)); 
	}

   /**
     * This class removes the provided strings from the query string, updating
     * the map in place.
     */
    public static Object[] removeFromQuery(Map<String, Object> map,
    		String... removals) {
    	Object[] ret = new Object[removals.length];
    	String queryStr = (String) map.get(MemoryCacheFilter.KEY_NAME);
    	if (queryStr == null)
    		throw new IllegalArgumentException("Initiale query string must not be null.");
    	boolean removed = false;
    	StringBuffer sb = new StringBuffer(queryStr);
    	int i=0;
    	for (String remove : removals) {
    		ret[i++] = map.remove(remove);
    		int pos = sb.indexOf(remove + "=");
    		if (pos == -1)
    			continue;
    		int end = pos + remove.length();
    		if (pos > 0) {
    			if (sb.charAt(pos - 1) != '&')
    				continue;
    			pos--;
    		}
    		if (end < sb.length()) {
    			int nextAmp = sb.indexOf("&", end);
    			if (nextAmp == -1)
    				end = sb.length();
    			else
    				end = nextAmp;
    		}
    		sb.delete(pos, end);
    		removed = true;
    	}
    	// Clean it up at the beginning and end
    	if (sb.length() > 0) {
    		if (sb.charAt(0) == '&') {
    			sb.delete(0, 1);
    			removed = true;
    		}
    		if (sb.charAt(sb.length() - 1) == '&') {
    			sb.delete(sb.length()-1, sb.length());
    			removed = true;
    		}
    	}
    	if (removed) {
    		queryStr = sb.toString();
    		map.put(MemoryCacheFilter.KEY_NAME, queryStr);
    	}
    	return ret;
    }
}
