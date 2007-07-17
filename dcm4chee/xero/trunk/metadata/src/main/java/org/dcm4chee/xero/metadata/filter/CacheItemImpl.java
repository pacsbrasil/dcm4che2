package org.dcm4chee.xero.metadata.filter;

import java.util.Map;

public class CacheItemImpl<T> implements CacheItem {

	public static final String CACHE_SIZE = "CACHE SIZE";
	
	T value;
	long size;
	public CacheItemImpl(T value, long size) {
		this.value = value;
		this.size = size;
	}
	
	public CacheItemImpl(T value, Map<String,Object> params) {
		this(value, Long.parseLong((String) params.get(CACHE_SIZE)));
	}
	
	public long getSize() {
		return size;
	}
	public T getValue() {
		return value;
	}
}
