/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.server.util.rhino;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dojotoolkit.server.util.resource.ResourceLoader;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class RhinoJSMethods {
	private static Logger logger = Logger.getLogger("org.dojotoolkit.server.util.rhino");

	private static final String LOAD = "loadJS"; //$NON-NLS-1$
	private static final String READ_TEXT = "readText"; //$NON-NLS-1$
	private static final String PRINT = "print"; //$NON-NLS-1$
	private static final String RESOURCE_LOADER = "resourceLoader"; //$NON-NLS-1$
	private static final String CLASSLOADER = "classloader"; //$NON-NLS-1$
	private static final String DEBUG = "debug"; //$NON-NLS-1$
	private static final String LOAD_COMMON_JS_MODULE = "loadCommonJSModule"; //$NON-NLS-1$
	
	public static void initScope(ScriptableObject scope, ResourceLoader resourceLoader, RhinoClassLoader rhinoClassLoader, boolean debug) {
    	Method[] methods = RhinoJSMethods.class.getMethods();
    	for (int i = 0; i < methods.length; i++) {
    		if (methods[i].getName().equals(PRINT)) {
    			FunctionObject f = new FunctionObject(PRINT, methods[i], scope);
    			scope.defineProperty(PRINT, f, ScriptableObject.DONTENUM);
    		}
    		else if (methods[i].getName().equals(READ_TEXT)) {
    			FunctionObject f = new FunctionObject(READ_TEXT, methods[i], scope);
    			scope.defineProperty(READ_TEXT, f, ScriptableObject.DONTENUM);
    		}
    		else if (methods[i].getName().equals(LOAD)) {
    			FunctionObject f = new FunctionObject(LOAD, methods[i], scope);
    			scope.defineProperty(LOAD, f, ScriptableObject.DONTENUM);
    		}
    		else if (methods[i].getName().equals(LOAD_COMMON_JS_MODULE)) {
    			FunctionObject f = new FunctionObject(LOAD_COMMON_JS_MODULE, methods[i], scope);
    			scope.defineProperty(LOAD_COMMON_JS_MODULE, f, ScriptableObject.DONTENUM);
    		}
    	}
    	scope.associateValue(RESOURCE_LOADER, resourceLoader);
	    scope.associateValue(CLASSLOADER, rhinoClassLoader);
	    scope.associateValue(DEBUG, new Boolean(debug));
	}
	
    public static Object print(Context cx, Scriptable thisObj, Object[] args, Function funObj){
    	for (int i=0; i < args.length; i++) {
    		String s = Context.toString(args[i]);
    		System.out.println("javascript print ["+s+']'); //$NON-NLS-1$
    	}
    	return Context.getUndefinedValue();
    }

    public static Object readText(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    	ResourceLoader resourceLoader = (ResourceLoader)((ScriptableObject)thisObj).getAssociatedValue(RESOURCE_LOADER);
		String path = Context.toString(args[0]);
		logger.logp(Level.FINER, RhinoJSMethods.class.getName(), "readText", "Rading text from resource ["+path+"]");
		try {
			URI uri = new URI(path);
			path = uri.normalize().getPath();
			if (path.charAt(0) != '/') {
				path = '/'+path;
			}
		} catch (URISyntaxException e) {
			logger.logp(Level.SEVERE, RhinoJSMethods.class.getName(), "readText", "Failed to normalize ["+path+"]", e);
			e.printStackTrace();
		}
    	
		try {
			return resourceLoader.readResource(path);
		}
		catch (IOException e) {
			logger.logp(Level.SEVERE, RhinoJSMethods.class.getName(), "readText", "IOException on call to 'readText("+path+")", e);
		}
		return null;
    }
    
	public static Object loadJS(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    	Boolean debug = (Boolean)((ScriptableObject)thisObj).getAssociatedValue(DEBUG);
		if (debug) {
			return loadFromResource(cx, thisObj, args, funObj);
		}
		else {
			return loadFromClassLoader(cx, thisObj, args, funObj);
		}
	}
	
	public static Object loadFromResource(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    	ResourceLoader resourceLoader = (ResourceLoader)((ScriptableObject)thisObj).getAssociatedValue(RESOURCE_LOADER);
		String path = Context.toString(args[0]);
		logger.logp(Level.FINER, RhinoJSMethods.class.getName(), "loadFromResource", "Loading from resource ["+path+"]");
		try {
			URI uri = new URI(path);
			path = uri.normalize().getPath();
			if (path.charAt(0) != '/') {
				path = '/'+path;
			}
		} catch (URISyntaxException e) {
			logger.logp(Level.SEVERE, RhinoJSMethods.class.getName(), "loadFromResource", "Failed to normalize ["+path+"]", e);
		}
		try {
			return cx.evaluateString(thisObj, resourceLoader.readResource(path), path, 1, null);
		}
		catch (IOException e) {
			logger.logp(Level.SEVERE, RhinoJSMethods.class.getName(), "loadFromResource", "IOException on call to 'load("+path+")", e);
		}
		return null;
    }
	
	private static Object loadFromClassLoader(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    	RhinoClassLoader classloader = (RhinoClassLoader)((ScriptableObject)thisObj).getAssociatedValue(CLASSLOADER);
		String resource = Context.toString(args[0]);
		try {
			logger.logp(Level.FINER, RhinoJSMethods.class.getName(), "loadFromClassLoader", "Loading class for ["+resource+"]");
			URI uri = new URI(resource);
			resource = uri.normalize().getPath();
			if (resource.charAt(0) == '/') {
				resource = resource.substring(1);
			}
			if (resource.indexOf('.') != -1) {
				resource = resource.substring(0, resource.indexOf('.'));
			}
			resource = resource.replace('/', '.');
			logger.logp(Level.FINER, RhinoJSMethods.class.getName(), "loadFromClassLoader", "Normalized path = ["+resource+"]");
			return classloader.loadJS(resource, cx, thisObj);
			
		} catch (URISyntaxException e) {
			logger.logp(Level.SEVERE, RhinoJSMethods.class.getName(), "loadFromClassLoader", "Failed to normalize ["+resource+"]", e);
		}
		
		return null;
    }
	
	public static Object loadCommonJSModule(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    	RhinoClassLoader classloader = (RhinoClassLoader)((ScriptableObject)thisObj).getAssociatedValue("classloader");
		String resource = Context.toString(args[0]);
		try {
			logger.logp(Level.FINER, RhinoJSMethods.class.getName(), "loadCommonJSModule", "Loading class for ["+resource+"]");
			URI uri = new URI(resource);
			resource = uri.normalize().getPath();
			if (resource.charAt(0) == '/') {
				resource = resource.substring(1);
			}
			if (resource.indexOf('.') != -1) {
				resource = resource.substring(0, resource.indexOf('.'));
			}
			resource = resource.replace('/', '.');
			logger.logp(Level.FINER, RhinoJSMethods.class.getName(), "loadCommonJSModule", "Normalized path = ["+resource+"]");
			Scriptable moduleContext = Context.toObject(args[1], thisObj);
			return classloader.loadJS(resource, cx, moduleContext);
		} catch (URISyntaxException e) {
			logger.logp(Level.SEVERE, RhinoJSMethods.class.getName(), "loadCommonJSModule", "Failed to normalize ["+resource+"]", e);
		}
		return null;
	}
}
