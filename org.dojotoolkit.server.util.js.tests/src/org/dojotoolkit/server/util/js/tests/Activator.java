/*
    Copyright (c) 2004-2011, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.server.util.js.tests;

import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	private BundleContext bundleContext = null;
	
	public void start(BundleContext bundleContext) throws Exception {
		this.bundleContext = bundleContext;
		new Thread(new TestsRunner()).start();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		this.bundleContext = null;
	}

	public class TestsRunner implements Runnable {
		public void run() {
			TestSuite suite = new TestSuite();
			
			suite.addTest(new AbsoluteTest(bundleContext));
			suite.addTest(new CyclicTest(bundleContext));
			suite.addTest(new DeterminismTest(bundleContext));
			suite.addTest(new ExactExportsTest(bundleContext));
			suite.addTest(new HasOwnPropertyTest(bundleContext));
			suite.addTest(new MethodTest(bundleContext));
			suite.addTest(new MissingTest(bundleContext));
			suite.addTest(new MonkeysTest(bundleContext));
			suite.addTest(new NestedTest(bundleContext));
			suite.addTest(new RelativeTest(bundleContext));
			suite.addTest(new TransitiveTest(bundleContext));
			
			TestRunner.run(suite);
		}
	}
}
