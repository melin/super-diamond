package com.github.diamond.client.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * Author: yuwang@iflytek.com
 * Date: 2016/6/30 10:40
 */
public class PropertiesFileUtils {

    private final static Logger logger = LoggerFactory.getLogger(PropertiesFileUtils.class);

    private final static String PROPERTIES_FILE_NAME = "superdiamond.properties";
    private static PropertiesFileUtils instance;
    private Properties properties;

    private PropertiesFileUtils() {
        properties = loadProperties(PROPERTIES_FILE_NAME);
    }

    public static PropertiesFileUtils getInstance() {
        if (instance == null) {
            instance = new PropertiesFileUtils();
        }

        return instance;
    }

    private static Properties loadProperties(String fileName) {
        Properties properties = new Properties();
        if (fileName.startsWith("/")) {
            try {
                FileInputStream input = new FileInputStream(fileName);
                try {
                    properties.load(input);
                } finally {
                    input.close();
                }
            } catch (Throwable e) {
                logger.warn("Failed to load " + fileName + " file from " + fileName + "(ingore this file): " + e.getMessage(), e);
            }
            return properties;
        }

        List<URL> list = new ArrayList<URL>();
        try {
            Enumeration<URL> urls = getClassLoader().getResources(fileName);
            list = new ArrayList<java.net.URL>();
            while (urls.hasMoreElements()) {
                list.add(urls.nextElement());
            }
        } catch (Throwable t) {
            logger.warn("Fail to load " + fileName + " file: " + t.getMessage(), t);
        }


        logger.info("load " + fileName + " properties file from " + list);

        for (java.net.URL url : list) {
            try {
                Properties p = new Properties();
                InputStream input = url.openStream();
                if (input != null) {
                    try {
                        p.load(input);
                        properties.putAll(p);
                    } finally {
                        try {
                            input.close();
                        } catch (Throwable t) {
                        }
                    }
                }
            } catch (Throwable e) {
                logger.warn("Fail to load " + fileName + " file from " + url + "(ingore this file): " + e.getMessage(), e);
            }
        }

        return properties;
    }

    /**
     * get class loader
     *
     * @return class loader
     */
    public static ClassLoader getClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back to system class loader...
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = PropertiesFileUtils.class.getClassLoader();
        }
        return cl;
    }

    public boolean propertiesLoaded() {
        return properties != null && properties.size() > 0;
    }

    public Object getProperty(String key) {
        if (propertiesLoaded()) {
            return properties.getProperty(key);
        }

        return null;
    }

}
