package com.github.diamond.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import java.lang.management.ManagementFactory;

import java.util.HashMap;
import java.util.Map;

import javax.management.InstanceAlreadyExistsException;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;




/**
 * @author libinsong1204@gmail.com
 */
public final class JMXUtils {

    public static ObjectName register(String name, Object mbean) {
        try {
            ObjectName objectName = new ObjectName(name);

            MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

            try {
                mbeanServer.registerMBean(mbean, objectName);
            } catch (InstanceAlreadyExistsException ex) {
                mbeanServer.unregisterMBean(objectName);
                mbeanServer.registerMBean(mbean, objectName);
            }

            return objectName;
        } catch (JMException e) {
            throw new IllegalArgumentException(name, e);
        }
    }

    public static void unregister(String name) {
        try {
            MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

            mbeanServer.unregisterMBean(new ObjectName(name));
        } catch (JMException e) {
            throw new IllegalArgumentException(name, e);
        }

    }

    private static final String[] THROWABLE_COMPOSITE_INDEX_NAMES = {"message", "class", "stackTrace"};
    private static final String[] THROWABLE_COMPOSITE_INDEX_DESCRIPTIONS = {"message", "class", "stackTrace"};
    private static final OpenType<?>[] THROWABLE_COMPOSITE_INDEX_TYPES = new OpenType<?>[]
            {SimpleType.STRING, SimpleType.STRING, SimpleType.STRING};

    private static CompositeType THROWABLE_COMPOSITE_TYPE = null;

    public static CompositeType getThrowableCompositeType() throws JMException {
        if (THROWABLE_COMPOSITE_TYPE == null) {
            THROWABLE_COMPOSITE_TYPE = new CompositeType("Throwable", "Throwable", THROWABLE_COMPOSITE_INDEX_NAMES,
                    THROWABLE_COMPOSITE_INDEX_DESCRIPTIONS,
                    THROWABLE_COMPOSITE_INDEX_TYPES);
        }

        return THROWABLE_COMPOSITE_TYPE;
    }

    public static CompositeData getErrorCompositeData(Throwable error) throws JMException {
        if (error == null) {
            return null;
        }

        Map<String, Object> map = new HashMap<String, Object>();

        map.put("class", error.getClass().getName());
        map.put("message", error.getMessage());

        map.put("stackTrace", getStackTrace(error));

        return new CompositeDataSupport(getThrowableCompositeType(), map);
    }

    private static String getStackTrace(Throwable ex) {
        StringWriter buf = new StringWriter();
        ex.printStackTrace(new PrintWriter(buf));

        return buf.toString();
    }
}
