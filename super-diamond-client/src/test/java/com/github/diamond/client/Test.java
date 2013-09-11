/**
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.
 *
 * Test.java Create on 2013-7-11 下午4:48:04
 */
package com.github.diamond.client;
import javax.naming.ConfigurationException;

/**
 *
 * @author <a href="mailto:bsli@ustcinfo.com">li.binsong</a>
 *
 */
public class Test {

	/**
	 * @param args
	 * @throws ConfigurationException
	 */
	public static void main(String[] args) throws Exception {
		PropertiesConfiguration config = new PropertiesConfiguration("localhost", 8283, "test", "development");
		config.addConfigurationListener(new ConfigurationListenerTest());
		System.out.println(config.getString("jdbc.url"));
		
		System.in.read();
	}

}
