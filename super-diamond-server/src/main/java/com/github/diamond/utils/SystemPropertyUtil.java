package com.github.diamond.utils;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 代码来自 netty5
 * 
 * @author libinsong1204@gmail.com
 *
 */
public class SystemPropertyUtil {
	@SuppressWarnings("all")
    private static boolean initializedLogger;
    private static final Logger logger;
    private static boolean loggedException;

    static {
        logger = LoggerFactory.getLogger(SystemPropertyUtil.class);
        initializedLogger = true;
    }

    public static boolean contains(String key) {
        return get(key) != null;
    }

    public static String get(String key) {
        return get(key, null);
    }

    public static String get(final String key, String def) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        if (key.isEmpty()) {
            throw new IllegalArgumentException("key must not be empty.");
        }

        String value = null;
        try {
            if (System.getSecurityManager() == null) {
                value = System.getProperty(key);
            } else {
                value = AccessController.doPrivileged(new PrivilegedAction<String>() {
                    @Override
                    public String run() {
                        return System.getProperty(key);
                    }
                });
            }
        } catch (Exception e) {
            if (!loggedException) {
            	logger.warn("Unable to retrieve a system property '" + key + "'; default values will be used.", e);
                loggedException = true;
            }
        }

        if (value == null) {
            return def;
        }

        return value;
    }

    public static boolean getBoolean(String key, boolean def) {
        String value = get(key);
        if (value == null) {
            return def;
        }

        value = value.trim().toLowerCase();
        if (value.isEmpty()) {
            return true;
        }

        if ("true".equals(value) || "yes".equals(value) || "1".equals(value)) {
            return true;
        }

        if ("false".equals(value) || "no".equals(value) || "0".equals(value)) {
            return false;
        }

        logger.warn(
                "Unable to parse the boolean system property '" + key + "':" + value + " - " +
                        "using the default value: " + def);

        return def;
    }

    private static final Pattern INTEGER_PATTERN = Pattern.compile("-?[0-9]+");

    public static int getInt(String key, int def) {
        String value = get(key);
        if (value == null) {
            return def;
        }

        value = value.trim().toLowerCase();
        if (INTEGER_PATTERN.matcher(value).matches()) {
            try {
                return Integer.parseInt(value);
            } catch (Exception e) {
                // Ignore
            }
        }

        logger.warn(
                "Unable to parse the integer system property '" + key + "':" + value + " - " +
                        "using the default value: " + def);

        return def;
    }

    public static long getLong(String key, long def) {
        String value = get(key);
        if (value == null) {
            return def;
        }

        value = value.trim().toLowerCase();
        if (INTEGER_PATTERN.matcher(value).matches()) {
            try {
                return Long.parseLong(value);
            } catch (Exception e) {
                // Ignore
            }
        }

        logger.warn(
                "Unable to parse the long integer system property '" + key + "':" + value + " - " +
                        "using the default value: " + def);

        return def;
    }

    private SystemPropertyUtil() {
    }
}