/**        
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.    
 */    
package com.github.diamond.client;

import java.util.Properties;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.Assert;

/**
 * Create on @2013-8-26 @上午9:29:52 
 * @author bsli@ustcinfo.com
 */
public class PropertiesConfigurationFactoryBean implements FactoryBean<Properties> {
	private static PropertiesConfiguration __configuration;
	
	private static boolean init = false;
	
	public PropertiesConfigurationFactoryBean(final String projCode, final String profile) {
		init = true;
		__configuration = new PropertiesConfiguration(projCode, profile);
	}
	
	public PropertiesConfigurationFactoryBean(String host, int port, final String projCode, final String profile) {
		init = true;
		__configuration = new PropertiesConfiguration(host, port, projCode, profile);
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
		if(!init) {
			throw new ConfigurationRuntimeException("PropertiesConfigurationFactoryBean 没有初始化");
		}
		return __configuration;
	}
}
