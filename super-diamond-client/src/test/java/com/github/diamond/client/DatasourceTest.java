/**        
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.    
 */    
package com.github.diamond.client;

import com.github.diamond.client.event.ConfigurationEvent;
import com.github.diamond.client.event.ConfigurationListener;
import com.github.diamond.client.event.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * Create on @2013-9-11 @下午4:12:44 
 * @author bsli@ustcinfo.com
 */
public class DatasourceTest implements InitializingBean {
	private String url;
	private String redisHost;

	public String getUrl() {
		return url;
	}
	@Autowired
	private EncryptPropertyPlaceholderConfigurer propertyConfigurer;

	public void setUrl(String url) {
		this.url = url;
	}

	public String getRedisHost() {
		return redisHost;
	}

	public void setRedisHost(String redisHost) {
		this.redisHost = redisHost;
	}

	static final PropertiesConfiguration config = PropertiesConfigurationFactoryBean.getPropertiesConfiguration();

	private static Logger logger = LoggerFactory.getLogger(DatasourceTest.class);
	@Override
	public void afterPropertiesSet() throws Exception {
		config.addConfigurationListener(new ConfigurationListener() {
			@Override
			public void configurationChanged(ConfigurationEvent event) {
				if (event.getType().equals(EventType.CLEAR)) {
					return;
				} else if (event.getPropertyName().equals("jdbc.url")) {
					logger.info("redisHost修改配置触发到这里了");
					url = (String)event.getPropertyValue();
					List<Map<String,Object>> encryptPropList = propertyConfigurer.findEncryptProps(url);
					if(propertyConfigurer.isEnableEncrypt()&&encryptPropList.size()>0){
						try {
							for(int i=0;i<encryptPropList.size();i++) {
								String decryptValue = propertyConfigurer.decrypt(String.valueOf(encryptPropList.get(i)));
								url = org.apache.commons.lang.StringUtils.replace(url, "$[" + encryptPropList.get(i).get("encryptProp") + "]", decryptValue);
							}
						} catch (Exception ex) {
							logger.error("decrypt propertyName:" + url + " exception, properValue is:" + url);
						}
					}
					logger.info("修改配置触发到这里了,url的值为："+url);
				}
			}
		});
	}
}
