/**
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.
 */

package com.github.diamond.client.config;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used to read properties lines. These lines do not terminate
 * with new-line chars but rather when there is no backslash sign a the end of
 * the line. This is used to concatenate multiple lines for readability.
 */
public class PropertiesReader extends LineNumberReader {
    static final String COMMENT_CHARS = "#!";

    private static final char[] SEPARATORS = new char[]{'=', ':'};

    static final String DEFAULT_SEPARATOR = " = ";

    private static final int UNICODE_LEN = 4;

    private static final int HEX_RADIX = 16;

    /** The regular expression to parse the key and the value of a property. */
    private static final Pattern PROPERTY_PATTERN = Pattern
            .compile("(([\\S&&[^\\\\" + new String(SEPARATORS)
                    + "]]|\\\\.)*)(\\s*(\\s+|[" + new String(SEPARATORS)
                    + "])\\s*)(.*)");

    /** Constant for the index of the group for the key. */
    private static final int IDX_KEY = 1;

    /** Constant for the index of the group for the value. */
    private static final int IDX_VALUE = 5;

    /** Constant for the index of the group for the separator. */
    private static final int IDX_SEPARATOR = 3;

    /** Stores the comment lines for the currently processed property. */
    private List<String> commentLines;

    /** Stores the name of the last read property. */
    private String propertyName;

    /** Stores the value of the last read property. */
    private String propertyValue;

    /** Stores the property separator of the last read property. */
    private String propertySeparator = DEFAULT_SEPARATOR;

    public PropertiesReader(Reader reader) {
        super(reader);
        commentLines = new ArrayList<String>();
    }

    /**
     * Reads a property line. Returns null if Stream is at EOF. Concatenates
     * lines ending with "\". Skips lines beginning with "#" or "!" and empty
     * lines. The return value is a property definition (
     * <code>&lt;name&gt;</code> = <code>&lt;value&gt;</code>)
     */
    public String readProperty() throws IOException {
        commentLines.clear();
        StringBuilder buffer = new StringBuilder();

        while (true) {
            String line = readLine();
            if (line == null) {
                // EOF
                return null;
            }

            if (isCommentLine(line)) {
                commentLines.add(line);
                continue;
            }

            line = line.trim();

            if (checkCombineLines(line)) {
                line = line.substring(0, line.length() - 1);
                buffer.append(line);
            } else {
                buffer.append(line);
                break;
            }
        }
        return buffer.toString();
    }

    /**
     * Parses the next property from the input stream and stores the found name
     * and value in internal fields. These fields can be obtained using the
     * provided getter methods. The return value indicates whether EOF was
     * reached (<b>false</b>) or whether further properties are available
     * (<b>true</b>).
     */
    public boolean nextProperty() throws IOException {
        String line = readProperty();

        if (line == null) {
            return false; // EOF
        }

        parseProperty(line);
        return true;
    }

    /**
     * Returns the comment lines that have been read for the last property.
     */
    public List<String> getCommentLines() {
        return commentLines;
    }

    /**
     * Returns the name of the last read property. This method can be called
     * after {@link #nextProperty()} was invoked and its return value was
     * <b>true</b>.
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Returns the value of the last read property. This method can be called
     * after {@link #nextProperty()} was invoked and its return value was
     * <b>true</b>.
     */
    public String getPropertyValue() {
        return propertyValue;
    }

    /**
     * Returns the separator that was used for the last read property. The
     * separator can be stored so that it can later be restored when saving the
     * configuration.
     */
    public String getPropertySeparator() {
        return propertySeparator;
    }

    /**
     * Parses a line read from the properties file. This method is called for
     * each non-comment line read from the source file. Its task is to split the
     * passed in line into the property key and its value. The results of the
     * parse operation can be stored by calling the {@code initPropertyXXX()}
     * methods.
     */
    protected void parseProperty(String line) {
        String[] property = doParseProperty(line);
        initPropertyName(property[0]);
        initPropertyValue(property[1]);
        initPropertySeparator(property[2]);
    }

    /**
     * Sets the name of the current property. This method can be called by
     * {@code parseProperty()} for storing the results of the parse operation.
     * It also ensures that the property key is correctly escaped.
     */
    protected void initPropertyName(String name) {
        propertyName = unescapeJava(name);
    }

    /**
     * Sets the value of the current property. This method can be called by
     * {@code parseProperty()} for storing the results of the parse operation.
     * It also ensures that the property value is correctly escaped.
     */
    protected void initPropertyValue(String value) {
        propertyValue = unescapeJava(value);
    }

    /**
     * Sets the separator of the current property. This method can be called by
     * {@code parseProperty()}. It allows the associated layout object to keep
     * track of the property separators. When saving the configuration the
     * separators can be restored.
     */
    protected void initPropertySeparator(String value) {
        propertySeparator = value;
    }

    /**
     * Checks if the passed in line should be combined with the following. This
     * is true, if the line ends with an odd number of backslashes.
     */
    private static boolean checkCombineLines(String line) {
        return countTrailingBs(line) % 2 != 0;
    }

    /**
     * Parse a property line and return the key, the value, and the separator in
     * an array.
     */
    private static String[] doParseProperty(String line) {
        Matcher matcher = PROPERTY_PATTERN.matcher(line);

        String[] result = {"", "", ""};

        if (matcher.matches()) {
            result[0] = matcher.group(IDX_KEY).trim();
            result[1] = matcher.group(IDX_VALUE).trim();
            result[2] = matcher.group(IDX_SEPARATOR);
        }

        return result;
    }

    /**
     * Tests whether a line is a comment, i.e. whether it starts with a comment
     * character.
     */
    static boolean isCommentLine(String line) {
        String str = line.trim();
        // blanc lines are also treated as comment lines
        return str.length() < 1 || COMMENT_CHARS.indexOf(str.charAt(0)) >= 0;
    }

    /**
     * <p>
     * Unescapes any Java literals found in the {@code String} to a
     * {@code Writer}.
     * </p>
     * This is a slightly modified version of the
     * StringEscapeUtils.unescapeJava() function in commons-lang that doesn't
     * drop escaped separators (i.e '\,').
     */
    protected static String unescapeJava(String str) {
        if (str == null) {
            return null;
        }
        int sz = str.length();
        StringBuilder out = new StringBuilder(sz);
        StringBuilder unicode = new StringBuilder(UNICODE_LEN);
        boolean hadSlash = false;
        boolean inUnicode = false;
        for (int i = 0; i < sz; i++) {
            char ch = str.charAt(i);
            if (inUnicode) {
                // if in unicode, then we're reading unicode
                // values in somehow
                unicode.append(ch);
                if (unicode.length() == UNICODE_LEN) {
                    // unicode now contains the four hex digits
                    // which represents our unicode character
                    try {
                        int value = Integer.parseInt(unicode.toString(),
                                HEX_RADIX);
                        out.append((char) value);
                        unicode.setLength(0);
                        inUnicode = false;
                        hadSlash = false;
                    } catch (NumberFormatException nfe) {
                        throw new RuntimeException(
                                "Unable to parse unicode value: " + unicode,
                                nfe);
                    }
                }
                continue;
            }

            if (hadSlash) {
                // handle an escaped value
                hadSlash = false;

                if (ch == '\\') {
                    out.append('\\');
                } else if (ch == '\'') {
                    out.append('\'');
                } else if (ch == '\"') {
                    out.append('"');
                } else if (ch == 'r') {
                    out.append('\r');
                } else if (ch == 'f') {
                    out.append('\f');
                } else if (ch == 't') {
                    out.append('\t');
                } else if (ch == 'n') {
                    out.append('\n');
                } else if (ch == 'b') {
                    out.append('\b');
                } else if (ch == 'u') {
                    // uh-oh, we're in unicode country....
                    inUnicode = true;
                } else {
                    out.append(ch);
                }

                continue;
            } else if (ch == '\\') {
                hadSlash = true;
                continue;
            }
            out.append(ch);
        }

        if (hadSlash) {
            // then we're in the weird case of a \ at the end of the
            // string, let's output it anyway.
            out.append('\\');
        }

        return out.toString();
    }

    /**
     * Returns the number of trailing backslashes. This is sometimes needed for
     * the correct handling of escape characters.
     */
    private static int countTrailingBs(String line) {
        int bsCount = 0;
        for (int idx = line.length() - 1; idx >= 0 && line.charAt(idx) == '\\'; idx--) {
            bsCount++;
        }

        return bsCount;
    }
}