/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.json.internal;

/**
 *
 */
public class Token {
    
    static final public Token TokenEOF    = new Token();
    static final public Token TokenBraceL = new Token();
    static final public Token TokenBraceR = new Token();
    static final public Token TokenBrackL = new Token();
    static final public Token TokenBrackR = new Token();
    static final public Token TokenColon  = new Token();
    static final public Token TokenComma  = new Token();
    static final public Token TokenTrue   = new Token();
    static final public Token TokenFalse  = new Token();
    static final public Token TokenNull   = new Token();
    
    private String  valueString;
    private Number  valueNumber;
    
    
    /**
     * 
     */
    public Token() {
        super();
    }
    
    /**
     * 
     */
    public Token(String value) {
        super();
        
        valueString = value;
    }
    
    /**
     * 
     */
    public Token(Number value) {
        super();
        
        valueNumber = value;
    }
    
    /**
     * 
     */
    public String getString() {
        return valueString;
    }
    
    /**
     * 
     */
    public Number getNumber() {
        return valueNumber;
    }
    
    /**
     * 
     */
    public boolean isString() {
        return null != valueString;
    }
    
    /**
     * 
     */
    public boolean isNumber() {
        return null != valueNumber;
    }
    
    /**
     * 
     */
    public String toString() {
        if (this == TokenEOF)    return "Token: EOF";
        if (this == TokenBraceL) return "Token: {";
        if (this == TokenBraceR) return "Token: }";
        if (this == TokenBrackL) return "Token: [";
        if (this == TokenBrackR) return "Token: ]";
        if (this == TokenColon)  return "Token: :";
        if (this == TokenComma)  return "Token: ,";
        if (this == TokenTrue)   return "Token: true";
        if (this == TokenFalse)  return "Token: false";
        if (this == TokenNull)   return "Token: null";
        
        if (this.isNumber()) return "Token: Number - " + getNumber();
        if (this.isString()) return "Token: String - '" + getString() + "'";
        
        return "Token: unknown!!";
    }
}
