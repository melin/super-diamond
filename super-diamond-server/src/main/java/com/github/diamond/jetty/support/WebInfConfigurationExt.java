package com.github.diamond.jetty.support;

import com.github.diamond.utils.SystemPropertyUtil;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 只用于测试和生产环境，默认从WEB-INF/lib目录下加载jar，改成从BASE_HOME/lib目录下加载jar
 *
 * @author libinsong1204@gmail.com
 */
public class WebInfConfigurationExt extends WebInfConfiguration {

    private  static  Logger logger = LoggerFactory.getLogger(WebInfConfigurationExt.class);

    @Override
    protected List<Resource> findJars(WebAppContext context) throws Exception {
        List<Resource> jarResources = new ArrayList<Resource>();
        List<Resource> webInfLibJars = findWebInfLibJars(context);
        if (webInfLibJars != null) {
            jarResources.addAll(webInfLibJars);
        }
        List<Resource> extraClasspathJars = findExtraClasspathJars(context);
        if (extraClasspathJars != null) {
            jarResources.addAll(extraClasspathJars);
        }
        return jarResources;
    }

    @Override
    protected List<Resource> findWebInfLibJars(WebAppContext context)
            throws Exception {
        Resource baseHome = Resource.newResource(SystemPropertyUtil.get("BASE_HOME"));
        if (baseHome == null || !baseHome.exists()) {
            return null;
        }

        List<Resource> jarResources = new ArrayList<Resource>();
        Resource webInfLib = baseHome.addPath("/lib");
        if (webInfLib.exists() && webInfLib.isDirectory()) {
            String[] files = webInfLib.list();
            for (int f = 0; files != null && f < files.length; f++) {
                try {
                    Resource file = webInfLib.addPath(files[f]);
                    String fnlc = file.getName().toLowerCase(Locale.ENGLISH);
                    int dot = fnlc.lastIndexOf('.');
                    String extension = (dot < 0 ? null : fnlc.substring(dot));
                    if (extension != null && (".jar".equals(extension) || ".zip".equals(extension))) {
                        jarResources.add(file);
                    }
                } catch (Exception ex) {
                    logger.warn("", ex);
                }
            }
        }
        return jarResources;
    }

}
