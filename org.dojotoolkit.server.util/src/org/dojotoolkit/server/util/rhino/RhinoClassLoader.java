/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.server.util.rhino;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dojotoolkit.server.util.resource.ResourceLoader;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.optimizer.ClassCompiler;

public class RhinoClassLoader extends ClassLoader {
	private static Logger logger = Logger.getLogger("org.dojotoolkit.parser");
	private ResourceLoader resourceLoader = null;
	
	public RhinoClassLoader(ResourceLoader resourceLoader, ClassLoader parent) {
		super(parent);
		this.resourceLoader = resourceLoader;
	}
	
	public RhinoClassLoader(ResourceLoader resourceLoader) {
		this(resourceLoader, RhinoClassLoader.class.getClassLoader());
	}
	
    public synchronized Class<?> loadClass(String name) throws ClassNotFoundException {
		logger.logp(Level.FINER, getClass().getName(), "loadClass", "loading class for ["+name+"]");
		String _name = name.replace('-', '_');
    	Class<?> c = findLoadedClass(_name);
    	if (c != null) {
    		logger.logp(Level.FINER, getClass().getName(), "loadClass", "class already loaded for ["+_name+"]");
    		return c;
    	}
    	
    	try {
			c = getParent().loadClass(_name);
		} catch (ClassNotFoundException e) {
		}
    	
    	if (c == null) {
    		String fileName = name.replace('.', '/').replace('~', '.');
    		fileName += ".js";
			if (fileName.charAt(0) != '/') {
				fileName = '/'+fileName;
			}
    		logger.logp(Level.FINER, getClass().getName(), "loadClass", "Resolved filename for ["+name+"] ["+fileName+"]");
    		InputStream is = null;

			try {
				String resource = resourceLoader.readResource(fileName);
				String className = name.replace('-', '_').replace('~', '_'); 
				String _fileName = fileName.replace('-', '_').replace('~', '_');
	    		if (resource != null) {
					ClassCompiler classCompiler = new ClassCompiler(new CompilerEnvirons());
					Object[] classBytes = classCompiler.compileToClassFiles(resource, _fileName, 1, className);
					c = defineClass(className, (byte[])classBytes[1], 0, ((byte[])classBytes[1]).length);
					resolveClass(c);
	    		} else {
	    			throw new ClassNotFoundException(className);
	    		}
			} catch (Throwable e) {
				logger.logp(Level.SEVERE, getClass().getName(), "loadJS", "Exception while loading class for ["+name+"]", e);
    			throw new ClassNotFoundException(name);
			} finally {
	            if (is != null) {
	                try {is.close();}catch (IOException e) {}
	            }
			}
    	}
    	
    	return c;
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return loadClass(name);
    }
    
    public Object loadJS(String name, Context cx, Scriptable thisObj) {
		logger.logp(Level.FINER, getClass().getName(), "loadJS", "loading ["+name+"]");
    	Object jsObject = null;
    	try {
			Class<?> c = loadClass(name);
	        Script script = (Script) c.newInstance();
			jsObject = script.exec(cx, thisObj);
		} catch (ClassNotFoundException e) {
			logger.logp(Level.SEVERE, getClass().getName(), "loadJS", "ClassNotFoundException while loading javascript ["+name+"]", e);
		} catch (IllegalAccessException e) {
			logger.logp(Level.SEVERE, getClass().getName(), "loadJS", "IllegalAccessException while loading javascript ["+name+"]", e);
		} catch (InstantiationException e) {
			logger.logp(Level.SEVERE, getClass().getName(), "loadJS", "InstantiationException while loading javascript ["+name+"]", e);
		}
    	return jsObject;
    }
    
}
