/*
    Copyright (c) 2004-2011, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.server.util.js.tests;

import org.osgi.framework.BundleContext;

public class DeterminismTest extends CommonJSTest {
	public DeterminismTest(BundleContext bundleContext) {
		super(bundleContext, "/commonjs/tests/modules/1.0/determinism/");
	}
}
