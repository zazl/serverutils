/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.server.util.resource;

import java.io.IOException;
import java.net.URL;

/**
 * Implementations provide access to resources 
 *
 */
public interface ResourceLoader {
	/**
	 * @param path String value containing the path information
	 * @return URL that can be used to access the resource
	 * @throws IOException
	 */
	URL getResource(String path) throws IOException;
	/**
	 * @param path String value containing the path information
	 * @return String value containing the resource contents
	 * @throws IOException
	 */
	String readResource(String path) throws IOException;
	/**
	 * @param path String value containing the path information
	 * @param useCache flag indicating whether any cached value should be returned
	 * @return String value containing the resource contents
	 * @throws IOException
	 */
	String readResource(String path, boolean useCache) throws IOException;
	/**
	 * @param path String value containing the path information
	 * @return timestamp value or -1 if none is available
	 */
	long getTimestamp(String path);
}
