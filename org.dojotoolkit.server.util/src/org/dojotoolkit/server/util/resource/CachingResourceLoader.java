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
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class CachingResourceLoader implements ResourceLoader {
	private static Logger logger = Logger.getLogger("org.dojotoolkit.server.util");
	private Map<String, StringBuffer> cache = null;
	private Map<String, URLTimestamp> timestampLookup = null;
	
	public CachingResourceLoader() {
		cache = new HashMap<String, StringBuffer>();
		timestampLookup = new HashMap<String, URLTimestamp>();
	}

	public URL getResource(String path) throws IOException {
		path = normalizePath(path);
		URL url = _getResource(path);
		if (url != null) {
			synchronized (timestampLookup) {
				long timestamp = -1;
				try {
					timestamp = url.openConnection().getLastModified();
				} catch (IOException e) {
					logger.logp(Level.INFO, getClass().getName(), "getResource", "Unable to obtain a last modified volue for path ["+path+"]");					
				}
	
				timestampLookup.put(path, new URLTimestamp(url, timestamp));
			}
		}
		return url;
	}

	public synchronized long getTimestamp(String path) {
		return (_getTimestamp(normalizePath(path)));
	}
	
	private synchronized long _getTimestamp(String path) {
		URLTimestamp urlTimestamp = timestampLookup.get(path);
		if (urlTimestamp != null) {
			try {
				return urlTimestamp.url.openConnection().getLastModified();
			} catch (IOException e) {
				logger.logp(Level.INFO, getClass().getName(), "_getTimestamp", "Unable to obtain a last modified volue for path ["+path+"]");					
				return -1;
			}
		} else {
			return -1;
		}
	}
	
	public String readResource(String path) throws IOException {
		path = normalizePath(path);
		boolean useCache = true;
		if (useCache) {
			synchronized (timestampLookup) {
				URLTimestamp urlTimestamp = timestampLookup.get(path);
				if (urlTimestamp != null) {
					try {
						long lastModified = urlTimestamp.url.openConnection().getLastModified();
						if (lastModified != urlTimestamp.lastModified) {
							urlTimestamp.lastModified = lastModified;
							useCache = false;
						}
					} catch (IOException e) {
						logger.logp(Level.INFO, getClass().getName(), "readResource", "Unable to obtain a last modified volue for path ["+path+"]");					
					}
				}
			}
		}
		if (useCache) {
			synchronized (cache) {
				StringBuffer sb = cache.get(path);
				if (sb != null) {
					return sb.toString();
				}
			}
		}
		URL url = getResource(path);
		if (url != null) {
			return _readResource(url, path);
		} else {
			return null;
		}
	}
	
	protected String _readResource(URL url, String path) throws IOException {
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
			sb = filter(sb, path);
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
	
	protected StringBuffer filter(StringBuffer sb, String path) throws IOException {
		return sb;
	}
	
	protected abstract URL _getResource(String path) throws IOException;
	
	private class URLTimestamp {
		public URL url = null;
		public long lastModified = -1;
		
		public URLTimestamp(URL url, long lastModified) {
			this.url = url;
			this.lastModified = lastModified;
		}
	}
}
