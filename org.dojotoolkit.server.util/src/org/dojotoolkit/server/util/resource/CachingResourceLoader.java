/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.server.util.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public abstract class CachingResourceLoader implements ResourceLoader {
	private Map<String, StringBuffer> cache = null;
	private Map<String, URL> timestampLookup = null;
	
	public CachingResourceLoader() {
		cache = new HashMap<String, StringBuffer>();
		timestampLookup = new HashMap<String, URL>();
	}

	public URL getResource(String path) throws IOException {
		return _getResource(normalizePath(path));
	}

	public synchronized long getTimestamp(String path) {
		URL url = timestampLookup.get(normalizePath(path));
		if (url != null) {
			try {
				return url.openConnection().getLastModified();
			} catch (IOException e) {
				return -1;
			}
		} else {
			return -1;
		}
	}
	
	public String readResource(String path) throws IOException {
		return readResource(path, true);
	}

	public String readResource(String path, boolean useCache)throws IOException {
		path = normalizePath(path);
		if (useCache) {
			synchronized (cache) {
				StringBuffer sb = cache.get(path);
				if (sb != null) {
					return sb.toString();
				}
			}
		}
		URL url = _getResource(path);
		if (url != null) {
			return _readResource(url, path, useCache);
		} else {
			return null;
		}
	}
	
	protected String _readResource(URL url, String path, boolean useCache) throws IOException {
		String resource = null;
		InputStream is = null;

		try {
			is = url.openStream();
			BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			StringBuffer sb = new StringBuffer();
			String line = "";
			while((line = r.readLine()) != null){
				sb.append(line);
				sb.append(System.getProperty("line.separator"));
			}
			sb = filter(sb, path, useCache);
			synchronized (cache) {
				cache.put(path, sb);
			}
			resource = sb.toString();
		} finally {
            if (is != null) {
                try {is.close();}catch (IOException e) {}
            }
		}
		
		return resource;
	}
	
	protected String normalizePath(String path) {
		try {
			URI uri = new URI(path);
			path = uri.normalize().getPath();
			if (path.charAt(0) != '/') {
				path = '/'+path;
			}
			return path;
		} catch (Exception e) {
			e.printStackTrace();
			return path;
		}
	}
	
	protected StringBuffer filter(StringBuffer sb, String path, boolean useCache) throws IOException {
		return sb;
	}
	
	protected synchronized void trackURL(String path, URL url) {
		timestampLookup.put(path, url);
	}
	
	protected abstract URL _getResource(String path) throws IOException;
}
