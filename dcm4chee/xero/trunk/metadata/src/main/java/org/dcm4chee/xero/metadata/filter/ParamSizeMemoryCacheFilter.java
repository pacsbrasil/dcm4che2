package org.dcm4chee.xero.metadata.filter;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This class extens the memory cache filter, but stores items based on a size parameter
 * contained in the params map, AFTER the item is retrieved.
 * @author bwallace
 *
 * @param <T>
 */
public class ParamSizeMemoryCacheFilter<T> extends MemoryCacheFilterBase<CacheItemImpl<T>> implements Filter<T>
{
	private static Logger log = LoggerFactory.getLogger(MemoryCacheFilterBase.class);

	/** Cache a CacheItemImpl instead of the actual item */
	@Override
	@SuppressWarnings("unchecked")
	public T filter(FilterItem filterItem, Map<String, Object> params) {
		String key = computeKey(params);
		CacheItemImpl<T> item = (key!=null ? cache.get(key) : null);
		log.info("Looking for key "+key+" item "+item);
		if( item==null ) {
			T newValue = (T) filterItem.callNextFilter(params);
			if( newValue!=null ) item = new CacheItemImpl<T>(newValue,params);
			if( item!=null && key!=null ) cache.put(key,item);
		}
		if( item==null ) return null;
		return item.getValue();
	}
	

}
