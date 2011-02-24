/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.json;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Converts Java objects into encoded <a href="http://www.ietf.org/rfc/rfc4627.txt">JSON</a>.
 * See {@link JSONParser} for the mapping rules.
 */
public class JSONSerializer {

    private Writer writer;

    /**
     * Serialize an object to a Writer as JSON. 
     */
    static public void serialize(Writer writer, Object object, boolean verbose) throws IOException {
        JSONSerializer JSONSerializer = verbose ? new JSONSerializerVerbose(writer) : new JSONSerializer(writer);
        JSONSerializer.write(object);
        writer.flush();
    }
    
    /**
     * Serialize an object to a Writer as JSON, specifying whether to be verbose or not. 
     */
    static public void serialize(Writer writer, Object object) throws IOException {
        serialize(writer,object,false);
    }
    
    /**
     * Create a JSONSerializer on the specified output stream.
     * Note that you should create 
     */
    JSONSerializer(Writer writer) {
        super();
        
        this.writer = writer;
    }

    /**
     * 
     */
    JSONSerializer write(Object object) throws IOException {
        if (null == object) return writeNull();
        if (object instanceof Number)    return writeNumber((Number) object);
        if (object instanceof Character) return writeCharacter((Character) object);
        if (object instanceof String)    return writeString((String) object);
        if (object instanceof Boolean)   return writeBoolean((Boolean) object);
        if (object instanceof Map)       return writeObject((Map) object);
        if (object instanceof List)      return writeArray((List) object);

        throw new JSONSerializeException("attempting to serialize unserializable object: '" + object + "'");//NON-NLS
    }
    
    /**
     * 
     */
    JSONSerializer writeRawString(String s) throws IOException {
        writer.write(s);
        
        return this;
    }

    /**
     * 
     */
    JSONSerializer writeNull() throws IOException {
        writeRawString("null");
        
        return this;
    }

    /**
     * 
     */
    JSONSerializer writeNumber(Number value) throws IOException {
        if (null == value) return writeNull();
        
        if (value instanceof Float) {
            if (Float.NaN               == value.floatValue()) return writeNull();
            if (Float.NEGATIVE_INFINITY == value.floatValue()) return writeNull();
            if (Float.POSITIVE_INFINITY == value.floatValue()) return writeNull();
        }
        
        if (value instanceof Double) {
            if (Double.NaN               == value.doubleValue()) return writeNull();
            if (Double.NEGATIVE_INFINITY == value.doubleValue()) return writeNull();
            if (Double.POSITIVE_INFINITY == value.doubleValue()) return writeNull();
        }
        
        writeRawString(value.toString());
        
        return this;
    }

    /**
     * 
     */
    JSONSerializer writeCharacter(Character value) throws IOException {
        if (null == value) return writeNull();
        
        return writeNumber(new Integer(value.charValue()));
    }
    
    /**
     * 
     */
    JSONSerializer writeBoolean(Boolean value) throws IOException {
        if (null == value) return writeNull();
        
        writeRawString(value.toString());
        
        return this;
    }

    /**
     * 
     */
    String rightAlignedZero(String s, int len) {
        if (len == s.length()) return s;
        
        StringBuffer sb = new StringBuffer(s);
        
        while (sb.length() < len) {
            sb.insert(0, '0');
        }
        
        return sb.toString();
    }
    
    /**
     * 
     */
    JSONSerializer writeString(String value) throws IOException {
        if (null == value) return writeNull();
        
        writer.write('"');

        char[] chars = value.toCharArray();
        
        for (int i=0; i<chars.length; i++) {
            char c = chars[i];
            switch (c) {
                case  '"': writer.write("\\\""); break;
                case '\'': writer.write("\\'");  break;
                case '\\': writer.write("\\\\"); break;
                case    0: writer.write("\\0"); break;
                case '\b': writer.write("\\b"); break;
                case '\t': writer.write("\\t"); break;
                case '\n': writer.write("\\n"); break;
                case 0x0B: writer.write("\\v"); break;
                case '\f': writer.write("\\f"); break;
                case '\r': writer.write("\\r"); break;
                default:
                    if ((c >= 32) && (c <= 126)) {
                        writer.write(c);
                    }
                    else if (c < 255) {
                        writer.write("\\x");
                        writer.write(rightAlignedZero(Integer.toHexString(c),2));
                    }
                    else {
                        writer.write("\\u");
                        writer.write(rightAlignedZero(Integer.toHexString(c),4));
                    }
            }
        }
        
        writer.write('"');

        return this;
    }

    /**
     * 
     */
    JSONSerializer writeObject(Map object) throws IOException {
        if (null == object) return writeNull();

        // write header
        writeRawString("{");
        indentPush();
        
        List propertyNames = getPropertyNames(object);
        for (Iterator iter=propertyNames.iterator(); iter.hasNext(); ) {
            Object key = iter.next();
            if (!(key instanceof String)) throw new JSONSerializeException("attempting to serialize object with an invalid property name: '" + key + "'" );//NON-NLS
            
            Object value = object.get(key);
            if (!isValidObject(value)) throw new JSONSerializeException("attempting to serialize object with an invalid property value: '" + value + "'");//NON-NLS
            
            newLine();
            indent();
//          writeRawString("\"");
            writeString((String)key);
            writeRawString(":");
            space();
            write(value);
            
            if (iter.hasNext()) writeRawString(",");
        }
        
        // write trailer
        indentPop();
        newLine();
        indent();
        writeRawString("}");
        
        return this;
    }

    /**
     * 
     */
    JSONSerializer writeArray(List value) throws IOException {
        if (null == value) return writeNull();
        
        // write header
        writeRawString("[");
        indentPush();
        
        for (Iterator iter=value.iterator(); iter.hasNext(); ) {
            Object element = iter.next();
            if (!isValidObject(element)) throw new JSONSerializeException("attempting to serialize array with an invalid element: '" + value + "'");//NON-NLS
            
            newLine();
            indent();
            write(element);
            
            if (iter.hasNext()) writeRawString(",");
        }
        
        // write trailer
        indentPop();
        newLine();
        indent();
        writeRawString("]");
        
        return this;
    }

    /**
     * 
     */
    private boolean isValidObject(Object object) {
        if (null == object) return true;
        return isValidType(object.getClass());
    }
    
    /**
     * Return whether the class is a valid type of value for a property.
     */
    private static boolean isValidType(Class clazz) {
        if (null == clazz) throw new IllegalArgumentException();
        
        if (String.class  == clazz) return true;
        if (Boolean.class == clazz) return true;
        if (Map.class.isAssignableFrom(clazz)) return true;
        if (List.class.isAssignableFrom(clazz)) return true;
        if (Number.class.isAssignableFrom(clazz)) return true;
        if (Character.class.isAssignableFrom(clazz)) return true;
        
        return false;
    }
    
    //---------------------------------------------------------------
    // pretty printing overridables
    //---------------------------------------------------------------
    
    /**
     * 
     */
    void space() throws IOException {
    }
    
    /**
     * 
     */
    void newLine() throws IOException {
    }
    
    /**
     * 
     */
    void indent() throws IOException {
    }
    
    /**
     * 
     */
    void indentPush() {
    }
    
    /**
     * 
     */
    void indentPop() {
    }
    
    /**
     * 
     */
    List getPropertyNames(Map map) {
        return new ArrayList(map.keySet());
    }
    
}
