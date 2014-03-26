package com.salesforce.phoenix.map.reduce;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.lang.UnhandledException;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.commons.lang.text.StrBuilder;

class CStringUnescape {
    public static String toCsvString(String str) {
        if (str == null) {
            return null;
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream(str.length());
            toCsvString(out, str);
            return out.toString("UTF-8");
        } catch (IOException ioe) {
            // this should never ever happen while writing to a StringWriter
            throw new UnhandledException(ioe);
        }
    }

    public static void toCsvString(ByteArrayOutputStream out, String str) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("The Writer must not be null");
        }
        if (str == null) {
            return;
        }
        int sz = str.length();
        StrBuilder unicode = new StrBuilder(4);
        StrBuilder hexadecimal = new StrBuilder(2);
        boolean hadSlash = false;
        boolean inUnicode = false;
        boolean inHexa = false;
        for (int i = 0; i < sz; i++) {
            char ch = str.charAt(i);
            if (inUnicode) {
                // if in unicode, then we're reading unicode
                // values in somehow
                unicode.append(ch);
                if (unicode.length() == 4) {
                    // unicode now contains the four hex digits
                    // which represents our unicode character
                    try {
                        int value = Integer.parseInt(unicode.toString(), 16);
                        out.write((char) value);
                        unicode.setLength(0);
                        inUnicode = false;
                        hadSlash = false;
                    } catch (NumberFormatException nfe) {
                        throw new NestableRuntimeException("Unable to parse unicode value: " + unicode, nfe);
                    }
                }
                continue;
            }
            if (inHexa) {
                hexadecimal.append(ch);
                if (hexadecimal.length() == 2) {
                    try {
                        int value = Integer.parseInt(hexadecimal.toString(), 16);
                        out.write((char) value);
                        hexadecimal.setLength(0);
                        inHexa = false;
                        hadSlash = false;
                    } catch (NumberFormatException nfe) {
                        throw new NestableRuntimeException("Unable to parse hexadecimal value: " + hexadecimal, nfe);
                    }
                }
                continue;
            }
            if (hadSlash) {
                // handle an escaped value
                hadSlash = false;
                switch (ch) {
                    case '\\':
                        out.write('\\');
                        out.write('\\');
                        break;
                    case '\'':
                        out.write('\'');
                        break;
                    case '\"':
                        out.write('\"');
                        out.write('\"');
                        break;
                    case 'r':
                        out.write('\r');
                        break;
                    case 'f':
                        out.write('\f');
                        break;
                    case 't':
                        out.write('\t');
                        break;
                    case 'n':
                        out.write('\n');
                        break;
                    case 'b':
                        out.write('\b');
                        break;
                    case 'u':
                    {
                        // uh-oh, we're in unicode country....
                        inUnicode = true;
                        break;
                    }
                    case 'x':
                    case 'X':
                    {
                        inHexa = true;
                        break;
                    }
                    default :
                        out.write(ch);
                        break;
                }
                continue;
            } else if (ch == '\\') {
                hadSlash = true;
                continue;
            }
            out.write(ch);
        }
        if (hadSlash) {
            // then we're in the weird case of a \ at the end of the
            // string, let's output it anyway.
            out.write('\\');
        }
    }
}