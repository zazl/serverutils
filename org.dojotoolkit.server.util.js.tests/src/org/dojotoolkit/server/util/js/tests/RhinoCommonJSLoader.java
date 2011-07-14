/*
    Copyright (c) 2004-2011, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.server.util.js.tests;

import java.io.IOException;

import org.dojotoolkit.server.util.resource.ResourceLoader;
import org.dojotoolkit.server.util.rhino.RhinoClassLoader;
import org.dojotoolkit.server.util.rhino.RhinoJSMethods;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

public class RhinoCommonJSLoader {
	private RhinoClassLoader rhinoClassLoader = null;
	private ResourceLoader resourceLoader = null;
	
	public RhinoCommonJSLoader(ResourceLoader resourceLoader) {
		this.rhinoClassLoader = new RhinoClassLoader(resourceLoader, RhinoClassLoader.class.getClassLoader()); 
		this.resourceLoader = resourceLoader;
	}
	
	public void run(String program, String root) throws IOException {
		Context ctx = null; 
		StringBuffer sb = new StringBuffer();
		sb.append("var realprint = print;\n print = function(msg, level) { realprint('level:'+level+' ' +msg); }\n");
        sb.append("root = '"+root+"';\n");
        sb.append("loadJS('/jsutil/commonjs/loader.js');\n");
        sb.append("require('"+program+"');\n");
		try {
			ctx = Context.enter();
			ScriptableObject scope = ctx.initStandardObjects();
			RhinoJSMethods.initScope(scope, resourceLoader, rhinoClassLoader, true);
			ctx.evaluateString(scope, sb.toString(), "CommonJSLoader", 1, null);
		}
		catch(Throwable t) {
			throw new IOException("Exception on load for ["+sb+"] : "+t.getMessage());
		}
		finally {
			Context.exit();
		}
	}
}
