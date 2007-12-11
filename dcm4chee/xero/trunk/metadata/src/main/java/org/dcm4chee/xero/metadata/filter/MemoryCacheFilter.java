package org.dcm4chee.xero.metadata.filter;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This class uses memory cache items that implement CacheItem directly to cache in-memory items.
 * */
public class MemoryCacheFilter<T extends CacheItem> extends MemoryCacheFilterBase<T> implements Filter<T>{
	private static Logger log = LoggerFactory.getLogger(MemoryCacheFilter.class);
		
	/**
	 * Return an item from memory cache if available, otherwise use the 
	 * next filter item to get the returned item.
	 */
	@SuppressWarnings("unchecked")
	public T filter(FilterItem filterItem, Map<String, Object> params) {
		String key = computeKey(params);
		boolean noCache = (params.get(NO_CACHE)!=null);
		T item = ((key!=null && !noCache) ? cache.get(key) : null);
		if( noCache) log.info("Not caching or using cache for "+key);
		else log.debug(cacheName+" looking for "+key+" found item "+item);
		if( item==null ) {
			item = (T) filterItem.callNextFilter(params);
			if( item!=null && key!=null && !noCache) cache.put(key,item);
		}
		return item;
	}



}
