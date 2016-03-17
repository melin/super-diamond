/**
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.
 */

package com.github.diamond.client;

import com.github.diamond.client.event.ConfigurationListener;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Properties;

/**
 * Create on @2013-8-26 @上午9:29:52 
 * @author bsli@ustcinfo.com
 */
public class PropertiesConfigurationFactoryBean implements FactoryBean<Properties> {
    private static PropertiesConfiguration __configuration;

    private static boolean init = false;

    public PropertiesConfigurationFactoryBean() {
        this(null);
    }

    public PropertiesConfigurationFactoryBean(List<ConfigurationListener> listeners) {
        init = true;
        __configuration = new PropertiesConfiguration();

        if (listeners != null) {
            for (ConfigurationListener listener : listeners) {
                __configuration.addConfigurationListener(listener);
            }
        }
    }

    public PropertiesConfigurationFactoryBean(final String projCode, final String profile, final String modules) {
        this(projCode, profile, modules, null);
    }

    public PropertiesConfigurationFactoryBean(final String projCode, final String profile, final String modules,
                                              List<ConfigurationListener> listeners) {
        init = true;
        __configuration = new PropertiesConfiguration(projCode, profile);

        if (listeners != null) {
            for (ConfigurationListener listener : listeners) {
                __configuration.addConfigurationListener(listener);
            }
        }
    }

    public PropertiesConfigurationFactoryBean(String host, int port, final String projCode, final String profile) {
        this(host, port, projCode, profile, "", "", "", null);
    }

    public PropertiesConfigurationFactoryBean(String host, int port, final String projCode, final String profile, final String modules) {
        this(host, port, projCode, profile, modules, "", "", null);
    }

    public PropertiesConfigurationFactoryBean(String host, int port, final String projCode,
                                              final String profile, final String modules,
                                              final String encryptPropNames, final String localFilePath) {
        this(host, port, projCode, profile, modules, encryptPropNames, localFilePath, null);
    }

    public PropertiesConfigurationFactoryBean(String host, int port, final String projCode,
                                              final String profile, final String modules,
                                              final String encryptPropNames, final String localFilePath,
                                              List<ConfigurationListener> listeners) {
        init = true;
        __configuration = new PropertiesConfiguration(host, port, projCode, profile, modules, encryptPropNames, localFilePath);

        if (listeners != null) {
            for (ConfigurationListener listener : listeners) {
                __configuration.addConfigurationListener(listener);
            }
        }
    }

    @Override
    public Properties getObject() throws Exception {
        Assert.notNull(__configuration);
        return __configuration.getProperties();
    }

    @Override
    public Class<?> getObjectType() {
        return Properties.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public static PropertiesConfiguration getPropertiesConfiguration() {
        if (!init) {
            throw new ConfigurationRuntimeException("PropertiesConfigurationFactoryBean 没有初始化");
        }
        return __configuration;
    }
}
