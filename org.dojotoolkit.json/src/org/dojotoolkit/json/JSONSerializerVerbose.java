/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.json;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 */
class JSONSerializerVerbose extends JSONSerializer {

    private int indent = 0;
    
    /**
     * 
     */
    JSONSerializerVerbose(Writer writer) {
        super(writer);
    }

    /**
     * 
     */
    void space() throws IOException {
        writeRawString(" ");
    }
    
    /**
     * 
     */
    void newLine() throws IOException {
        writeRawString("\n");
    }
    
    /**
     * 
     */
    void indent() throws IOException {
        for (int i=0; i<indent; i++) writeRawString("  ");
    }
    
    /**
     * 
     */
    void indentPush() {
        indent++;
    }
    
    /**
     * 
     */
    void indentPop() {
        indent--;
        if (indent < 0) throw new IllegalStateException();
    }
    
    /**
     * 
     */
    List getPropertyNames(Map map) {
        List propertyNames = super.getPropertyNames(map);
        
        Collections.sort(propertyNames);
        
        return propertyNames;
    }
    
}
