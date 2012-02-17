/*
    Copyright (c) 2004-2012, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.server.util.rhino;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.dojotoolkit.server.util.resource.ResourceLoader;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

public class ASTCache {
	private Map<String, ASTCacheEntry> astCache = null;
	private Map<String, Object> lockMap = null;	

	public ASTCache() {
		astCache = Collections.synchronizedMap(new HashMap<String, ASTCacheEntry>());
		lockMap = new HashMap<String, Object>();
	}
	
	public Scriptable getAst(String uri, Function createASTFunction, ResourceLoader resourceLoader, Context cx, Scriptable thisObj) {
		Object lock = null;
		synchronized (lockMap) {
			lock = lockMap.get(uri);
			if (lock == null) {
				lock = new Object();
				lockMap.put(uri, lock);
			}
		}
		
		synchronized (lock) {
			ASTCacheEntry astCacheEntry = astCache.get(uri);
			
			if (astCacheEntry != null) {
				if (resourceLoader.getTimestamp(uri) != astCacheEntry.timestamp) {
					astCache.remove(uri);
					astCacheEntry = null;
				}
			}
			if (astCacheEntry == null) {
				Object o = createASTFunction.call(cx, thisObj, thisObj, new Object[]{});
				Scriptable ast = Context.toObject(o, thisObj);
				astCacheEntry = new ASTCacheEntry(ast, resourceLoader.getTimestamp(uri));
				astCache.put(uri, astCacheEntry);
			}
			return astCacheEntry.ast;
		}
	}
	
	public static class ASTCacheEntry {
		public Scriptable ast = null;
		public long timestamp = -1;
		
		public ASTCacheEntry(Scriptable ast, long timestamp) {
			this.ast = ast;
			this.timestamp = timestamp;
		}
	}
}
