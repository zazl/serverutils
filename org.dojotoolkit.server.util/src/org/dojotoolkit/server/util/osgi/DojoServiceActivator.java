/*
    Copyright (c) 2004-2011, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.server.util.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class DojoServiceActivator implements BundleActivator {
	protected DojoServicesInitializer servicesInitializer = null;

	public DojoServiceActivator() {
		servicesInitializer = new DojoServicesInitializer();
	}
	
	public void start(BundleContext context) throws Exception {
		servicesInitializer.start(context);
	}

	public void stop(BundleContext context) throws Exception {
		servicesInitializer.stop(context);
	}
}
