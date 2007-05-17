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

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A memory cache is a Map that throws things away based on how much memory is
 * being used, and implements a multiple level least recently used cache. Items
 * in L1 are most recent and are quickly thrown away. Items are promoted to L2
 * when the are used more than a certain number of times. The size of each level
 * can be configured independently, as can the maximum size item cached
 * (normally 10% of cache size). Lookup time is O(log n) where n is the number
 * of items in the cache. Contains is O(1). Note that this implementation is not
 * synchronized.
 * 
 * @author bwallace
 * 
 * @param <T>
 */
public class MemoryCache<K, V extends CacheItem> extends AbstractMap<K, V> {

	private static final Logger log = LoggerFactory.getLogger(MemoryCache.class);
	
	static public final long DEFAULT_SIZE = 1024 * 1024;

	/** By default, an item needs to be accessed first as a put, and then as get
	 * in order to be promoted
	 */
	static public final int DEFAULT_TRANSITION = 1;

	protected int levels;
	
	/** The default maximum age for any item is 5 minutes - after that, it gets reloaded regardless */
	protected long maxAge = 5*60*1000l;  

	protected long[] cacheSizes;

	protected int[] transitions;

	protected long[] currentSizes;

	/** Contains the items in least recently used ordering, to allow removal */
	protected SortedSet<InternalKey<K, V>>[] lruSets;

	/**
	 * findMap is used to lookup the information about an item - the value,
	 * where it is located etc.
	 */
	protected Map<K, InternalKey<K, V>> findMap = new HashMap<K, InternalKey<K, V>>();

	/**
	 * Create a memory cache with 2 levels, and 1 meg of space, transitioning on
	 * two or more requests to an L1 item.
	 */
	public MemoryCache() {
		this(new long[] { DEFAULT_SIZE, DEFAULT_SIZE },
				new int[] { DEFAULT_TRANSITION });
	}

	/**
	 * Create a memory cache with the given number of levels, sizes and
	 * transitions
	 */
	@SuppressWarnings("unchecked")
	public MemoryCache(long[] cacheSizes, int[] transitions) {
		this.levels = cacheSizes.length;
		currentSizes = new long[levels];
		this.cacheSizes = cacheSizes;
		if( transitions.length != levels - 1 ) {
			throw new IllegalArgumentException("Transitions length must be one less than the cacheSizes length.");
		}
		this.transitions = transitions;
		// Not sure why the type can't be declared here.
		lruSets = new SortedSet[levels];
		for (int i = 0; i < levels; i++) {
			lruSets[i] = new TreeSet<InternalKey<K, V>>();
		}
	}

	/** Get the matching item from the cache, if available, return null otherwise */
	@Override
	public V get(Object key) {
		InternalKey<K, V> value = findMap.get(key);
		if (value == null)
			return null;
		if( (System.currentTimeMillis() - value.getOriginalAge()) > maxAge ) {
			this.remove(key);
			return null;
		}
		int level = value.getLevel();
		lruSets[level].remove(value);
		// Must call the get value before adding it back in.
		V ret = value.getValue();
		if (level < levels - 1 && value.getAccessCount() >= transitions[level]) {
			currentSizes[level] -= value.getSize();
			level++;
			value.setLevel(level);
			currentSizes[level] += value.getSize();
		}
		lruSets[level].add(value);
		return ret;
	}
	
	/** Put the item into the cache, as long as the size is no more than
	 * 1/2 of the first level cache size.
	 */
	@Override
	public V put(K key, V value) {
		InternalKey<K, V> item = findMap.get(key);
		if (item != null) {
			remove(key);
		}
		InternalKey<K, V> putItem = new InternalKey<K, V>(key, value);
		long size = putItem.getSize();
		if (putItem.getSize() < (cacheSizes[0] / 2)) {
			findMap.put(key, putItem);
			lruSets[putItem.getLevel()].add(putItem);
			currentSizes[putItem.getLevel()] += putItem.getSize();
			emptyLRU(putItem.getLevel());
		}
		else {
			log.warn("Not caching value for "+key+" because it is too large:"+size+"/"+cacheSizes[0]);
		}
		if (item != null)
			return item.getValue();
		return null;
	}
	
	/** Removes extra items at the given level */
	protected void emptyLRU(int level) {
		while(currentSizes[level] > cacheSizes[level] ) {
			InternalKey<K,V> first = lruSets[level].first();
			remove(first.getKey());
		}
	}

	/** Remove an item from the cache */
	@Override
	public V remove(Object key) {
		InternalKey<K, V> item = findMap.remove(key);
		if (item == null)
			return null;
		lruSets[item.getLevel()].remove(item);
		currentSizes[item.getLevel()] -= item.getSize();
		return item.getValue();
	}

	/**
	 * Figure out if the key is present or not. Does NOT update most recently
	 * accessed.
	 */
	@Override
	public boolean containsKey(Object key) {
		return findMap.containsKey(key);
	}

	/**
	 * Unsupported - you don't want to iterate over the items as it isn't clear
	 * what that would mean in terms of the accessibility of the items.
	 */
	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		throw new UnsupportedOperationException("Entry set not defined yet.");
	}

	/** One stop shopping to set ALL the cache sizes at each level.  Total memory
	 * consumed could be as much as number of levels times the size provided. 
	 * This will cause items to be deleted if you decrease the size, but only LRU
	 * items, not items that are more than 10% of the new size.
	 */
	public void setCacheSizes(long size) {
		if( size<=0 ) throw new IllegalArgumentException("Illegal cache size "+size);
		for(int i=0; i<levels; i++ ) {
			cacheSizes[i] = size;
			emptyLRU(i);
		}
	}


/**
 * The internal item is used to determine which cache an item is located in and
 * last-access time etc. It is comparable so that the oldest item can be found.
 * 
 * @author bwallace
 * 
 * @param <T>
 */
static class InternalKey<K, V extends CacheItem> implements
		Comparable<InternalKey<K, V>> {
	private V value;

	private K key;

	private int increment;
	private static int currentIncrement = 0;
	
	private long originalAge = System.currentTimeMillis();
	
	private int accessCount = 0;

	private long size;

	private int level = 0;

	public InternalKey(K key, V item) {
		this.value = item;
		this.key = key;
		this.increment = nextIncrement();
		this.size = value.getSize();
	}

        /** Return the level of the cache 0...LEVELS that this item is located in */
	public int getLevel() {
		return level;
	}

	/**
	 * Get the value associated with this item. This WILL cause the item to be
	 * updated and the last accessed time to be incremented, so if it is in an
	 * ordered list etc it will need to be moved/repositioned appropriately. DO
	 * NOT call this while InternalKey is a member of a sorted set.
	 */
	public V getValue() {
		increment = nextIncrement();
		accessCount++;
		return value;
	}
	
	/** Gets the key associated with this item */
	public K getKey() {
		return key;
	}

	/** Get the size of this item */
	public long getSize() {
		return size;
	}
	
	/**
	 * Gets the original age
	 */
	public long getOriginalAge() {
		return originalAge;
	}

	/** Get the access count */
	public int getAccessCount() {
		return accessCount;
	}

	/** Equals is true ONLY on the key value, not on the current associated value */
	@Override
	public boolean equals(Object obj) {
		if( obj==null ) return false;
		return key.equals(((InternalKey<?, ?>) obj).getValue());
	}
	
	/** Just use the key hash code */
	@Override
	public int hashCode() {
		return key.hashCode();
	}

	/** Set the cache level - causes the access count to go to zero */
	public void setLevel(int level) {
		this.level = level;
		this.accessCount = 0;
	}
	
	/** Gets the next increment value - used to order cached items. */
	public static synchronized int nextIncrement() {
		currentIncrement ++;
		return currentIncrement;
	}

	/** Compare items by last accessed */
	public int compareTo(InternalKey<K, V> o) {
		// Cast should be safe since the times are current times, and we don't
		// expect
		// anything to be in the cache for many years.
		int ret = increment - o.increment;
		if (ret != 0)
			return ret;
		// Return something to be a constant ordering on the items - we only
		// care about identity, so default hashCode is good enough.
		return hashCode() - o.hashCode();
	}
}

}