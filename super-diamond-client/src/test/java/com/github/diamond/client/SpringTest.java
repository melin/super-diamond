package com.github.diamond.client;

import com.github.diamond.client.event.ConfigurationEvent;
import com.github.diamond.client.event.ConfigurationListener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.Properties;

/**
 * Author: yuwang@iflytek.com
 * Date: 2016/6/30 18:29
 */
public class SpringTest {

    public static void main(String[] args) throws Exception {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("bean.xml");
        PropertiesConfigurationFactoryBean bean = (PropertiesConfigurationFactoryBean)applicationContext.getBean("localProperties");
        PropertiesConfiguration properties = bean.getPropertiesConfiguration();
        properties.getString("jdbcurl");
        System.in.read();
    }
}
