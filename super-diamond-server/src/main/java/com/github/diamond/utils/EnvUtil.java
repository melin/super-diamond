package com.github.diamond.utils;

import com.github.diamond.web.constant.Constants;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author libinsong1204@gmail.com
 */
public abstract class EnvUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnvUtil.class);

    private static String profile;
    private static String projectName;
    private static String projectBaseDir;
    private static String buildVersion;
    private static String buildTime;

    static {
        try {
            Configuration config = new PropertiesConfiguration("META-INF/res/env.properties");

            LOGGER.info("加载env.properties");

            profile = config.getString("spring.profiles.active");
            projectName = config.getString("project.name");
            projectBaseDir = config.getString("project.basedir");
            buildVersion = config.getString("build.version");
            buildTime = config.getString("build.time");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static String getSpringProfile() {
        return profile;
    }

    public static boolean isDevelopment() {
        if (profile != null && !profile.contains(Constants.PROFILE_TEST)
                && !profile.contains(Constants.PROFILE_PRODUCTION)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isTest() {
        if (profile != null && profile.contains(Constants.PROFILE_TEST)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isProduction() {
        if (profile != null && profile.contains(Constants.PROFILE_PRODUCTION)) {
            return true;
        } else {
            return false;
        }
    }

    public static String getProjectName() {
        return projectName;
    }

    public static String getProjectBaseDir() {
        return projectBaseDir;
    }

    public static String getBuildVersion() {
        return buildVersion;
    }

    public static String getBuildTime() {
        return buildTime;
    }

}
