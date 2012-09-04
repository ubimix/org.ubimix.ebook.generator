/**
 * 
 */
package org.ubimix.ebook.remote.presenter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kotelnikov
 */
public class PageUtils {

    public static final Map<Character, String> RESERVED_XML_CHARS = new HashMap<Character, String>();

    static {
        RESERVED_XML_CHARS.put('&', "&amp;");
        RESERVED_XML_CHARS.put('<', "&lt;");
        RESERVED_XML_CHARS.put('>', "&gt;");
        RESERVED_XML_CHARS.put('\"', "&quot;");
        RESERVED_XML_CHARS.put('\'', "&apos;");
    }

    /**
     * Escapes XML string.
     */
    public static String escapeXml(String s) {
        boolean advanced = true;
        boolean recognizeUnicodeChars = true;

        if (s != null) {
            int len = s.length();
            StringBuilder result = new StringBuilder(len);

            for (int i = 0; i < len; i++) {
                char ch = s.charAt(i);
                if (ch == '&') {
                    if ((advanced || recognizeUnicodeChars)
                        && (i < len - 2)
                        && (s.charAt(i + 1) == '#')) {
                        boolean isHex = Character.toLowerCase(s.charAt(i + 2)) == 'x';
                        int charIndex = i + (isHex ? 3 : 2);
                        int radix = isHex ? 16 : 10;
                        String unicode = "";
                        while (charIndex < len) {
                            char currCh = s.charAt(charIndex);
                            if (currCh == ';') {
                                break;
                            } else if (isValidInt(unicode + currCh, radix)) {
                                unicode += currCh;
                                charIndex++;
                            } else {
                                charIndex--;
                                break;
                            }
                        }

                        if (isValidInt(unicode, radix)) {
                            char unicodeChar = (char) Integer.parseInt(
                                unicode,
                                radix);
                            if (!isValidXmlChar(unicodeChar)) {
                                i = charIndex;
                            } else if (!isReservedXmlChar(unicodeChar)) {
                                result.append(recognizeUnicodeChars ? String
                                    .valueOf(unicodeChar) : "&#"
                                    + unicode
                                    + ";");
                                i = charIndex;
                            } else {
                                i = charIndex;
                                result.append("&#" + unicode + ";");
                            }
                        } else {
                            result.append("&amp;");
                        }
                    } else {
                        result.append("&amp;");
                    }
                } else if (isReservedXmlChar(ch)) {
                    result.append("&#" + (int) ch + ";");
                } else {
                    result.append(ch);
                }
            }

            return result.toString();
        }

        return null;
    }

    public static boolean isHexadecimalDigit(char ch) {
        return Character.isDigit(ch)
            || ch == 'A'
            || ch == 'a'
            || ch == 'B'
            || ch == 'b'
            || ch == 'C'
            || ch == 'c'
            || ch == 'D'
            || ch == 'd'
            || ch == 'E'
            || ch == 'e'
            || ch == 'F'
            || ch == 'f';
    }

    public static boolean isReservedXmlChar(char ch) {
        return RESERVED_XML_CHARS.containsKey(ch);
    }

    public static boolean isValidInt(String s, int radix) {
        try {
            Integer.parseInt(s, radix);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidXmlChar(char ch) {
        return ((ch >= 0x20) && (ch <= 0xD7FF))
            || (ch == 0x9)
            || (ch == 0xA)
            || (ch == 0xD)
            || ((ch >= 0xE000) && (ch <= 0xFFFD))
            || ((ch >= 0x10000) && (ch <= 0x10FFFF));
    }
}
