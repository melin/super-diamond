/**        
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.    
 */    
package com.github.diamond.client;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * Create on @2013-8-26 @上午10:00:54 
 * @author bsli@ustcinfo.com
 */
public class PropertiesConfigurationFactoryBeanTest {

	public static void main(String[] args) {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("bean.xml");
		
		PropertiesConfiguration configuration = PropertiesConfigurationFactoryBean.getPropertiesConfiguration();
	}

}
