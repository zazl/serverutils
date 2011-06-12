/*
    Copyright (c) 2004-2011, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.server.util.resource;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.dojotoolkit.server.util.resource.CachingResourceLoader;

public class MultiRootResourceLoader extends CachingResourceLoader {
	private File[] roots = null;

	public MultiRootResourceLoader(File[] roots) {
		this.roots = roots;
	}
	
	protected URL _getResource(String path) throws IOException {
		for (File root : roots) {
			URL url = searchRoot(root, path);
			if (url != null) {
				return url;
			}
		}
		return null;
	}
	
	private URL searchRoot(File root, String path) throws IOException {
		File resourceFile = new File(root, path);
		if (resourceFile.exists()) {
			return resourceFile.toURI().toURL();
		} else {
			return null;
		}
	}
}
