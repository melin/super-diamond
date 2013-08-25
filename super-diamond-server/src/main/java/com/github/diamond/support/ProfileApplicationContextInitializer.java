package com.github.diamond.support;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

public class ProfileApplicationContextInitializer implements
		ApplicationContextInitializer<ConfigurableApplicationContext> {

	private static final Logger _logger = LoggerFactory
			.getLogger(ProfileApplicationContextInitializer.class);

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		try {
			ClassPathResource resource = new ClassPathResource("META-INF/res/profile.properties");
			Properties properties = PropertiesLoaderUtils.loadProperties(resource);

			String profile = properties.getProperty("spring.profiles.active");

			if (profile == null) {
				profile = "development";
			}

			applicationContext.getEnvironment().setActiveProfiles(new String[] { profile });
			_logger.info("Active spring profile: {}", profile);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}