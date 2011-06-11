/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.rt.v8;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dojotoolkit.json.JSONParser;

public abstract class V8JavaBridge {
	private static Logger logger = Logger.getLogger("org.dojotoolkit.rt.v8");
	public static boolean v8Available = true;
	
	static {
		try {
			System.loadLibrary("v8javabridge");
		} catch (Throwable e) {
			logger.logp(Level.FINER, V8JavaBridge.class.getName(), "static initializer", "v8javabridge is unavailable",e);
			v8Available = false;
		}
	}
	protected native String runScriptInV8(String source) throws V8Exception;
	protected native String runScriptInV8WithCallbacks(String source, String[] callbacks) throws V8Exception;
	protected native String runScriptInV8WithExternalCallbacks(String source, String[] callbacks, Object external) throws V8Exception;
	
	protected List<Throwable> compileErrors = null;
	
	public V8JavaBridge() {
		compileErrors = new ArrayList<Throwable>();
	}
	
	public Object runScript(String source)  throws V8Exception {
		logger.logp(Level.FINER, getClass().getName(), "runScript", "source ["+source+"]");
		String jsonReturnString = runScriptInV8(source);
		Object returnValue = null;
		if (jsonReturnString != null) {
			try {
				returnValue = JSONParser.parse(new StringReader(jsonReturnString));
				logger.logp(Level.FINER, getClass().getName(), "runScript", "returnValue ["+jsonReturnString+"]");
			} catch (IOException e) {
				logger.logp(Level.SEVERE, getClass().getName(), "runScript", "IOException running source ["+source+"]", e);
			}
		}
		return returnValue;
	}
	
	public Object runScript(String source, String[] callbacks) throws V8Exception {
		logger.logp(Level.FINER, getClass().getName(), "runScript", "source ["+source+"]");
		String jsonReturnString = runScriptInV8WithCallbacks(source, callbacks);
		Object returnValue = null;
		if (jsonReturnString != null) {
			try {
				logger.logp(Level.FINER, getClass().getName(), "runScript", "returnValue ["+jsonReturnString+"]");
				returnValue = JSONParser.parse(new StringReader(jsonReturnString));
			} catch (IOException e) {
				logger.logp(Level.SEVERE, getClass().getName(), "runScript", "IOException running source ["+source+"]", e);
			}
		}
		return returnValue;
	}
	
	public Object runScript(String source, String[] callbacks, Object external) throws V8Exception {
		logger.logp(Level.FINER, getClass().getName(), "runScript", "source ["+source+"]");
		String jsonReturnString = runScriptInV8WithExternalCallbacks(source, callbacks, external);
		Object returnValue = null;
		if (jsonReturnString != null) {
			try {
				logger.logp(Level.FINER, getClass().getName(), "runScript", "returnValue ["+jsonReturnString+"]");
				returnValue = JSONParser.parse(new StringReader(jsonReturnString));
			} catch (IOException e) {
				logger.logp(Level.SEVERE, getClass().getName(), "runScript", "IOException running source ["+source+"]", e);
			}
		}
		return returnValue;
	}
	
	public void print(String[] msgs) {
		for (String msg : msgs) {
			System.out.println("["+msg+"]");
		}
	}
	
	public String readText(String uri) {
		logger.logp(Level.FINER, getClass().getName(), "readText", "readText on ["+uri+"]");
		try {
			return readResource(uri);
		}
		catch (IOException e) {
			logger.logp(Level.SEVERE, getClass().getName(), "readText", "IOException reading  source ["+uri+"]", e);
		}
		return null;
	}
	
	public void reportCompileError(Throwable error) {
		compileErrors.add(error);
	}
	
	public abstract String readResource(String path) throws IOException;

}
