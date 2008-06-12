package org.dcm4chee.xero.metadata.filter;

import static org.dcm4chee.xero.metadata.servlet.MetaDataServlet.nanoTimeToString;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.MetaDataUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class uses memory cache items that implement CacheItem directly to cache
 * in-memory items.
 */
public class MemoryCacheFilter<T> implements Filter<T>, MetaDataUser {

   private static Logger log = LoggerFactory.getLogger(MemoryCacheFilter.class);

   /** The default item to lookup in params to find the key for the cached item. */
   public static final String KEY_NAME = "queryStr";

   /** The key to use to enable or modify the caching */
   public static final String CACHE_CONTROL = "cache-control";

   /**
     * The metadata item to define how long to keep the object open for -
     * default to zero
     */
   public static final String CLOSE_AGE = "closeAge";

   /** The value to use for cache-control to NOT cache the item */
   public static final String NO_CACHE = "no-cache";

   /**
     * The key to use in the meta-data for the size, and in the params to denote
     * the size of the cached item.
     */
   public static final String CACHE_SIZE = "cacheSize";

   protected String paramKeyName = KEY_NAME;

   protected String cacheName, closeAge;
   
   MemoryCache<String, T> cache = new MemoryCache<String, T>();

   /**
     * Return an item from memory cache if available, otherwise use the next
     * filter item to get the returned item.
     * 
     * Desired behaviour: 1. If NO_CACHE is true then ignore the queue, just
     * call the next item. 2. Size the queue to cacheSize 3. Have a maximum age
     * of maxAge, and throw things away after that age. 4. Have a close after
     * time of closeAge. 5. If lockDuplicate is true, the create a lock and wait
     * for the response on a given item if a second thread requests the same
     * item.
     */
   public T filter(FilterItem<T> filterItem, Map<String, Object> params) {
	  boolean noCache = (params.get(NO_CACHE) != null);
	  if (noCache) {
		 return filterItem.callNextFilter(params);
	  }
	  String key = computeKey(params);
	  SizeableFuture sf = new SizeableFutureFilter<T>(filterItem, params);

	  long start = System.nanoTime();
	  log.debug("Looking in cache "+cacheName +" for " + key);
	  T ret = cache.get(key, sf);
	  long dur = System.nanoTime() - start;
      if( log.isDebugEnabled() ) {
	     String msg = "Found in "+cacheName+" value " + key + "=" + ret + " in " + nanoTimeToString(dur);
	     log.debug(msg);
      }
	  if (dur > 1000000000)
		 log.warn("More than 1 second to fetch " + key);
	  return ret;
   }

   /**
     * Override this method to use a different parameter value to get the cached
     * item.
     * 
     * @param params
     * @return
     */
   protected String computeKey(Map<String, ?> params) {
	  if (params == null)
		 throw new IllegalArgumentException("Params to filter and compute key should not be null.");
	  Object okey = params.get(paramKeyName);
	  log.debug("Looking for key in " + paramKeyName + " found " + okey);
	  if (okey instanceof String)
		 return (String) okey;
	  if (okey instanceof String[])
		 throw new IllegalArgumentException("Memory cache key must be single valued.");
	  if (okey == null) {
		 log.warn("Memory cache can't cache a null key value, when looking for key " + paramKeyName);
		 return null;
	  }
	  return okey.toString();
   }

   /**
     * Sets the size of the cache in bytes.
     */
   public void setCacheSize(long size) {
	  cache.setCacheSize(size);
   }

   public void setMetaData(MetaDataBean metaDataBean) {
	  String keyName = (String) metaDataBean.getValue(KEY_NAME);
	  log.info("Setting key name for parameter to " + keyName);
	  if (keyName != null)
		 paramKeyName = keyName;
	  String cacheSize = (String) metaDataBean.getValue(CACHE_SIZE);
	  if (cacheSize != null)
		 setCacheSize(Long.parseLong(cacheSize));
	  cacheName = metaDataBean.getPath();
	  cache.cacheName = cacheName;
	  closeAge = (String) metaDataBean.getValue(CLOSE_AGE);
	  if (closeAge != null)
		 cache.setCloseAge(Long.parseLong(closeAge));
   }

   /**
     * Adds the given pairs of String/value entries to the parameter map and
     * query key. Must be pairs of strings.
     */
   public static void addToQuery(Map<String, Object> map, String... additions) {
	  if ((additions.length % 2) == 1) {
		 throw new IllegalArgumentException("Query values must be paired.");
	  }
	  String queryStr = (String) map.get(MemoryCacheFilter.KEY_NAME);
	  if (queryStr == null)
		 queryStr = "";
	  StringBuffer sb = new StringBuffer(queryStr);
	  for (int i = 0; i < additions.length; i += 2) {
		 if (map.containsKey(additions[i])) {
			String oldKey = "&" + additions[i] + "=" + map.get(additions[i]);
			int pos = sb.indexOf(oldKey);
			if (pos < 0) {
			   oldKey = "?" + oldKey.substring(1);
			   pos = sb.indexOf(oldKey);
			}
			if (pos < 0) {
			   log.warn("Unable to find old key value " + oldKey);
			} else {
			   sb.delete(pos, pos+oldKey.length());
			}
		 }
		 map.put(additions[i], additions[i + 1]);
		 sb.append("&").append(additions[i]).append('=').append(additions[i + 1]);
		 map.put(MemoryCacheFilter.KEY_NAME,sb.toString());
	  }
   }

   /**
     * This class removes the provided strings from the query string, updating
     * the map in place.
     */
   public static Object[] removeFromQuery(Map<String, Object> map, String... removals) {
	  Object[] ret = new Object[removals.length];
	  String queryStr = (String) map.get(MemoryCacheFilter.KEY_NAME);
	  if (queryStr == null)
		 throw new IllegalArgumentException("Initial query string must not be null.");
	  boolean removed = false;
	  StringBuffer sb = new StringBuffer(queryStr);
	  int i = 0;
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
			sb.delete(sb.length() - 1, sb.length());
			removed = true;
		 }
	  }
	  if (removed) {
		 queryStr = sb.toString();
		 map.put(MemoryCacheFilter.KEY_NAME, queryStr);
	  }
	  return ret;
   }

   /**
     * Get a future item from the next filter item, and be able to say how bit
     * it is.
     * 
     * @author bwallace
     * 
     * @param <T>
     */
   static class SizeableFutureFilter<T> implements SizeableFuture<T> {
	  private long size;

	  FilterItem filterItem;

	  Map<String, Object> params;

	  T value;

	  SizeableFutureFilter(FilterItem filterItem, Map<String, Object> params) {
		 this.filterItem = filterItem;
		 this.params = params;
	  }

	  public boolean cancel(boolean arg0) {
		 throw new UnsupportedOperationException();
	  }

	  @SuppressWarnings("unchecked")
	  public T get() throws InterruptedException, ExecutionException {
		 value = (T) filterItem.callNextFilter(params);
		 if( value==null ) {
			log.info("Couldn't find value for sizeable future");
			return null;
		 }
		 size = FilterUtil.getLong(params,CACHE_SIZE,-1);
		 if (size >= 0) {
			if( size==0 ) {
			   log.warn("Size provided is 0.");
			}
		 } else {
			size = ((CacheItem) value).getSize();
			if( size==0 ) {
			   log.warn("Size returned by cache item "+value+" is zero.");
			}
		 }
		 return value;
	  }

	  public T get(long arg0, TimeUnit arg1) throws InterruptedException, ExecutionException, TimeoutException {
		 throw new UnsupportedOperationException();
	  }

	  public boolean isCancelled() {
		 return false;
	  }

	  public boolean isDone() {
		 throw new UnsupportedOperationException();
	  }

	  public long getSize() {
		 return size;
	  }

   };
}
