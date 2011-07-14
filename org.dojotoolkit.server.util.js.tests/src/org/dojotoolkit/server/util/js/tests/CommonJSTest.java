/*
    Copyright (c) 2004-2011, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.server.util.js.tests;

import org.dojotoolkit.server.util.resource.ResourceLoader;
import org.osgi.framework.BundleContext;

import junit.framework.TestCase;

public class CommonJSTest extends TestCase {
	private static final String[] ids = {
		"org.dojotoolkit.server.util.js",
		"org.dojotoolkit.server.util.js.tests",
	};
	protected ResourceLoader resourceLoader = null;
	protected BundleContext bundleContext = null;
	protected String testRoot = null;
	
	public CommonJSTest(BundleContext bundleContext, String testRoot) {
		this.bundleContext = bundleContext;
		this.testRoot = testRoot;
	}

	protected void setUp() throws Exception {
		resourceLoader = new OSGiResourceLoader(bundleContext, ids);
	}

	protected void runTest() throws Throwable {
		RhinoCommonJSLoader rhinoCommonJSLoader = new RhinoCommonJSLoader(resourceLoader);
		try {
			rhinoCommonJSLoader.run("program", testRoot);
		} catch (Exception e) {
			TestCase.fail(e.getMessage());
		}
		/*
		V8CommonJSLoader v8CommonJSLoader = new V8CommonJSLoader(resourceLoader);
		try {
			v8CommonJSLoader.run("program", testRoot);
		} catch (Exception e) {
			TestCase.fail(e.getMessage());
		}
		*/
	}
}
