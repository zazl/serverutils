/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.server.util.resource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class CachingContentFilter implements ContentFilter {
	private ResourceLoader resourceLoader = null;
	private Map<String, CacheEntry> cache = null;
	protected Map<String, Object> lockMap = null;

	public CachingContentFilter(ResourceLoader resourceLoader) {
		cache = Collections.synchronizedMap(new HashMap<String, CacheEntry>());
		lockMap = new HashMap<String, Object>();
		this.resourceLoader = resourceLoader;
	}
	
	public String filter(String content, String path) {
		Object lock = null;
		synchronized (lockMap) {
			lock = lockMap.get(path);
			if (lock == null) {
				lock = new Object();
				lockMap.put(path, lock);
			}
		}
		
		synchronized (lock) {
			long currentTimestamp = resourceLoader.getTimestamp(path);
			CacheEntry cacheEntry = cache.get(path);
			if (cacheEntry == null || currentTimestamp != cacheEntry.timestamp) {
				cacheEntry = new CacheEntry(_runFilter(content, path), currentTimestamp);
				cache.put(path, cacheEntry);
			}
			return cacheEntry.filteredContents;
		}
	}

	private class CacheEntry {
		long timestamp = -1;
		String filteredContents = null;
		
		public CacheEntry(String filteredContents, long timestamp) {
			this.filteredContents = filteredContents;
			this.timestamp = timestamp;
		}
	}
	
	protected abstract String _runFilter(String content, String path);
}
