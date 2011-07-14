/*
    Copyright (c) 2004-2011, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.server.util.js.tests;

import java.io.IOException;

import org.dojotoolkit.rt.v8.V8JavaBridge;
import org.dojotoolkit.server.util.resource.ResourceLoader;

public class V8CommonJSLoader extends V8JavaBridge {
	private ResourceLoader resourceLoader = null;
	
	public V8CommonJSLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public void run(String program, String root) throws IOException {
		StringBuffer sb = new StringBuffer();
		sb.append("var realprint = print;\n print = function(msg, level) { realprint('level:'+level+' ' +msg); }\n");
        sb.append("root = '"+root+"';\n");
        sb.append("loadJS('/jsutil/commonjs/loader.js');\n");
        sb.append("require('"+program+"');\n");
        sb.append("result = '{}';\n");
        try {
        	runScript(sb.toString());
        } catch (Throwable e) {
			if (compileErrors.size() > 0) {
				for (Throwable t : compileErrors) {
					t.printStackTrace();
				}
			}
			throw new IOException("Exception on compress for ["+sb+"] : "+e.getMessage());
        }
	}
	
	public String readResource(String path) throws IOException {
		return resourceLoader.readResource(path);
	}
}
