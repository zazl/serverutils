/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.json;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dojotoolkit.json.internal.Token;
import org.dojotoolkit.json.internal.Tokenizer;

/**
 * Converts encoded <a href="http://www.ietf.org/rfc/rfc4627.txt">JSON</a> into Java objects.  
 * Specifically, 
 * <ul>
 * <li>JSON Objects are converted to Maps
 * <li>JSON Arrays are converted to Lists
 * <li>JSON Strings are converted to Strings
 * <li>JSON Numbers are converted to either Long or Double, depending on
 * whether they are integral or not.
 * <li>JSON Booleans are converted to Booleans
 * <li>nulls are converted to null
 * </ul>
 * <p>Note that this parser reads ahead one character, if it can, and
 * that read-ahead character will be thrown away at the end of parsing if
 * not part of the JSON representation.
 */
public class JSONParser {

    private Tokenizer tokenizer;
    private Token     lastToken;

    /**
     * 
     */
    static public Object parse(Reader reader) throws IOException {
        JSONParser parser = new JSONParser(reader);
        return parser.parseValue();
    }
    
    /**
     * 
     */
    private JSONParser(Reader reader) throws IOException {
        super();
        
        this.tokenizer = new Tokenizer(reader);
        this.lastToken = tokenizer.next();
    }

    /**
     * 
     */
    private Object parseMap() throws IOException {
        Map result = new HashMap();
        
        if (lastToken != Token.TokenBraceL) throw new JSONParseException("Expecting '{' " + tokenizer.onLineCol());//NON-NLS
        lastToken = tokenizer.next();
        
        while (true) {
            if (lastToken == Token.TokenEOF) throw new JSONParseException("Unterminated object " + tokenizer.onLineCol());//NON-NLS
            
            if (lastToken == Token.TokenBraceR) {
                lastToken = tokenizer.next();
                break;
            }
            
            if (!lastToken.isString()) throw new JSONParseException("Expecting string key " + tokenizer.onLineCol());//NON-NLS
            String key = lastToken.getString();
            
            lastToken = tokenizer.next();
            if (lastToken != Token.TokenColon) throw new JSONParseException("Expecting colon " + tokenizer.onLineCol());//NON-NLS
            
            lastToken = tokenizer.next();
            Object val = parseValue();
            
            result.put(key, val);
            
            if (lastToken == Token.TokenComma) {
                lastToken = tokenizer.next();
            }
            
            else if (lastToken != Token.TokenBraceR) {
                throw new JSONParseException("expecting either ',' or '}' " + tokenizer.onLineCol());//NON-NLS
            }
        }
        
        return result;
    }
    
    /**
     * 
     */
    private List parseArray() throws IOException {
        List result = new ArrayList();
        
        if (lastToken != Token.TokenBrackL) throw new JSONParseException("Expecting '[' " + tokenizer.onLineCol());//NON-NLS
        lastToken = tokenizer.next();
        
        while (true) {
            if (lastToken == Token.TokenEOF) throw new JSONParseException("Unterminated array " + tokenizer.onLineCol());//NON-NLS
            
            if (lastToken == Token.TokenBrackR) {
                lastToken = tokenizer.next();
                break;
            }
            
            Object val = parseValue();
            
            result.add(val);
            
            if (lastToken == Token.TokenComma) {
                lastToken = tokenizer.next();
            }
            else if (lastToken != Token.TokenBrackR) {
                throw new JSONParseException("expecting either ',' or ']' " + tokenizer.onLineCol());//NON-NLS
            }
        }
        
        return result;
    }
    
    /**
     * 
     */
    private Object parseValue() throws IOException {
        if (lastToken == Token.TokenEOF) throw new JSONParseException("Expecting property value " + tokenizer.onLineCol());//NON-NLS
        
        if (lastToken.isNumber()) {
            Object result = lastToken.getNumber();
            lastToken = tokenizer.next();
            return result;
        }
        
        if (lastToken.isString()) {
            Object result = lastToken.getString();
            lastToken = tokenizer.next();
            return result;
        }
        
        if (lastToken == Token.TokenFalse) {
            lastToken = tokenizer.next();
            return Boolean.FALSE;
        }
        
        if (lastToken == Token.TokenTrue) {
            lastToken = tokenizer.next();
            return Boolean.TRUE;
        }
        
        if (lastToken == Token.TokenNull) {
            lastToken = tokenizer.next();
            return null;
        }
        
        if (lastToken == Token.TokenBrackL) return parseArray();
        if (lastToken == Token.TokenBraceL) return parseMap();
        
        throw new JSONParseException("Invalid token " + tokenizer.onLineCol());//NON-NLS
    }
    
}
