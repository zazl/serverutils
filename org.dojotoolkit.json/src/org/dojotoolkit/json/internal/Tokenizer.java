/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.json.internal;

import java.io.IOException;
import java.io.Reader;

/**
 * Tokenizes a stream into JSON tokens.
 */
public class Tokenizer {

    private Reader reader;
    private int     lineNo;
    private int     colNo;
    private int     lastChar;
    
    /**
     * Create a new instance of this class.
     */
    public Tokenizer(Reader reader) throws IOException {
        super();
        
        this.reader    = reader;
        this.lineNo    = 0;
        this.colNo     = 0;
        this.lastChar  = '\n';
        
        readChar();
    }
    
    /**
     * Return the next token in the stream, returning Token.TokenEOF when finished.
     */
    public Token next() throws IOException {
        
        // skip whitespace
        while (Character.isWhitespace((char)lastChar)) {
            readChar();
        }
        
        // handle punctuation
        switch (lastChar) {
            case -1:  readChar(); return Token.TokenEOF;
            case '{': readChar(); return Token.TokenBraceL;
            case '}': readChar(); return Token.TokenBraceR;
            case '[': readChar(); return Token.TokenBrackL;
            case ']': readChar(); return Token.TokenBrackR;
            case ':': readChar(); return Token.TokenColon;
            case ',': readChar(); return Token.TokenComma;
            
            case '"':
            case '\'':
                String stringValue = readString();
                return new Token(stringValue);
                
            case '-':
            case '.':
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
                Number numberValue = readNumber();
                return new Token(numberValue);

            case 'n': 
            case 't':
            case 'f':
                String ident = readIdentifier();

                if (ident.equals("null"))  return Token.TokenNull;
                if (ident.equals("true"))  return Token.TokenTrue;
                if (ident.equals("false")) return Token.TokenFalse;
                
                throw new IOException("Unexpected identifier '" + ident + "' " + onLineCol());//NON-NLS
                
            default:
                throw new IOException("Unexpected character '" + lastChar + "' " + onLineCol());//NON-NLS
                
        }
        
    }

    /**
     * 
     */
    private String readString() throws IOException {
        StringBuffer sb    = new StringBuffer();
        int          delim = lastChar;
        int          l = lineNo;
        int          c = colNo;
        
        readChar();
        while ((-1 != lastChar) && (delim != lastChar)) {
            StringBuffer digitBuffer;
            
            if (lastChar != '\\') {
                sb.append((char)lastChar);
                readChar();
                continue;
            }
            
            readChar();
            
            switch (lastChar) {
                case 'b':  readChar(); sb.append('\b'); continue; 
                case 'f':  readChar(); sb.append('\f'); continue; 
                case 'n':  readChar(); sb.append('\n'); continue; 
                case 'r':  readChar(); sb.append('\r'); continue; 
                case 't':  readChar(); sb.append('\t'); continue; 
                case '\'': readChar(); sb.append('\''); continue; 
                case '"':  readChar(); sb.append('"');  continue; 
                case '\\': readChar(); sb.append('\\'); continue;
                case '/':  readChar(); sb.append('/');  continue;
                
                // hex constant
                // unicode constant
                case 'x':
                case 'u':
                    digitBuffer = new StringBuffer();
                    
                    int toRead = 2;
                    if (lastChar == 'u') toRead = 4;
                    
                    for (int i=0; i<toRead; i++) {
                        readChar();
                        if (!isHexDigit(lastChar)) throw new IOException("non-hex digit " + onLineCol());//NON-NLS
                        digitBuffer.append((char) lastChar);
                    }
                    readChar();
                    
                    try {
                        int digitValue = Integer.parseInt(digitBuffer.toString(), 16);
                        sb.append((char) digitValue);
                    }
                    catch (NumberFormatException e) {
                        throw new IOException("non-hex digit " + onLineCol());//NON-NLS
                    }
                    
                    break;
                    
                // octal constant
                default:
                    if (!isOctalDigit(lastChar)) throw new IOException("non-hex digit " + onLineCol());//NON-NLS
                
                    digitBuffer = new StringBuffer();
                    digitBuffer.append((char) lastChar);
                    
                    for (int i=0; i<2; i++) {
                        readChar();
                        if (!isOctalDigit(lastChar)) break;
                        
                        digitBuffer.append((char) lastChar);
                    }
                    
                    try {
                        int digitValue = Integer.parseInt(digitBuffer.toString(), 8);
                        sb.append((char) digitValue);
                    }
                    catch (NumberFormatException e) {
                        throw new IOException("non-hex digit " + onLineCol());//NON-NLS
                    }
            }
        }
        
        if (-1 == lastChar) {
            throw new IOException("String not terminated " + onLineCol(l,c));//NON-NLS
        }
        
        readChar();
        
        return sb.toString();
    }
    
    /**
     * Read a number.
     * 
     * (-)(1-9)(0-9)*           : decimal
     * (-)0(0-7)*               : octal
     * (-)0(x|X)(0-9|a-f|A-F)*  : hex
     * [digits][.digits][(E|e)[(+|-)]digits]     
     * */
    private Number readNumber() throws IOException {
        StringBuffer sb = new StringBuffer();
        int          l    = lineNo;
        int          c    = colNo;
        
        while (isDigitChar(lastChar)) {
            sb.append((char)lastChar);
            readChar();
        }
        
        // convert it!
        String string = sb.toString();
        try {
            if (-1 != string.indexOf('.')) {
                return Double.valueOf(string);
            }

            String sign = "";
            if (string.startsWith("-")) {
                sign = "-";
                string = string.substring(1);
            }
    
            if (string.toUpperCase().startsWith("0X")) {
                return Long.valueOf(sign + string.substring(2),16);
            }
            
            if (string.equals("0")) {
                return Long.valueOf(0);
            }
            
            if (string.startsWith("0")) {
                return Long.valueOf(sign + string.substring(1),8);
            }
            
            return Long.valueOf(sign + string,10);
        }
        
        catch (NumberFormatException e) {
            throw new IOException("Invalid number literal " + onLineCol(l,c));//NON-NLS
        }
        
    }

    /**
     * 
     */
    private boolean isHexDigit(int c) {
        switch (c) {
            case '0': case '1': case '2': case '3': case '4': 
            case '5': case '6': case '7': case '8': case '9':
            case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
            case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
                return true;
        }
        
        return false;
    }
    
    /**
     * 
     */
    private boolean isOctalDigit(int c) {
        switch (c) {
            case '0': case '1': case '2': case '3': case '4': 
            case '5': case '6': case '7': 
                return true;
        }
        
        return false;
    }
    
    /**
     * 
     */
    private boolean isDigitChar(int c) {
        switch (c) {
            case '0': case '1': case '2': case '3': case '4': 
            case '5': case '6': case '7': case '8': case '9':
            case '.':
            case 'e': case 'E':
            case 'x': case 'X':
            case '+': case '-':
                return true;
        }
        
        return false;
    }
    
    /**
     * only really need to handle 'null', 'true', and 'false' 
     */
    private String readIdentifier() throws IOException {
        StringBuffer sb = new StringBuffer();
        
        while ((-1 != lastChar) && Character.isLetter((char)lastChar)) {
            sb.append((char)lastChar);
            readChar();
        }
        
        return sb.toString();
    }
    
    /**
     * 
     */
    private void readChar() throws IOException {
        if ('\n' == lastChar) {
            this.colNo = 0;
            this.lineNo++;
        }
        
        lastChar = reader.read();
        if (-1 == lastChar) return ;
        
        colNo++;
    }
    
    /**
     * 
     */
    private String onLineCol(int line, int col) {
        return "on line " + line + ", column " + col;
    }
    
    /**
     * 
     */
    public String onLineCol() {
        return onLineCol(lineNo,colNo);
    }
}