/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.server.util.osgi;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dojotoolkit.server.util.resource.CachingResourceLoader;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class OSGiResourceLoader extends CachingResourceLoader {
	private static Logger logger = Logger.getLogger("org.dojotoolkit.optimizer");
	
	private BundleContext bundleContext = null;
	private Bundle[] bundles = null;
	private String[] bundleIds = null;

	public OSGiResourceLoader(BundleContext bundleContext, String[] bundleIds) {
		this.bundleContext = bundleContext;
		this.bundleIds = bundleIds;
	}

	protected URL _getResource(String path) {
		if (bundles == null) {
			bundles = new Bundle[bundleIds.length];
			int i = 0;
			for (String bundleId : bundleIds) {
				bundles[i] = findBundle(bundleId);
				if (bundles[i] == null) {
					throw new RuntimeException("Bundle ["+bundleId+"] cannot be located");
				}
				i++;
			}
		}
		URL url = bundleContext.getBundle().getResource(path);
		if (url != null) {
			logger.logp(Level.FINER, getClass().getName(), "_getResource", "["+path+"] ["+url+"]");
			return url;
		}
		for (Bundle bundle : bundles) {
			url = bundle.getResource(path);
			if (url != null) {
				break;
			}
		}
		logger.logp(Level.FINER, getClass().getName(), "_getResource", "["+path+"] ["+url+"]");
		return url;
	}
	
	private Bundle findBundle(String symbolicName) {
		Bundle[] bundles = bundleContext.getBundles();
		Bundle bundle = null;
		
		for (Bundle b : bundles) {
			if (b.getSymbolicName().equals(symbolicName)) {
				bundle = b;
				break;
			}
		}
		return bundle;
	}
}
