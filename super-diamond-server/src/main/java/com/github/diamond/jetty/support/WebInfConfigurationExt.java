package com.github.diamond.jetty.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;

import com.github.diamond.utils.SystemPropertyUtil;

/**
 * 只用于测试和生产环境，默认从WEB-INF/lib目录下加载jar，改成从BASE_HOME/lib目录下加载jar
 * 
 * @author libinsong1204@gmail.com
 *
 */
public class WebInfConfigurationExt extends WebInfConfiguration {
	
	@Override
	protected List<Resource> findJars(WebAppContext context) throws Exception {
		List<Resource> jarResources = new ArrayList<Resource>();
		List<Resource> webInfLibJars = findWebInfLibJars(context);
		if (webInfLibJars != null)
			jarResources.addAll(webInfLibJars);
		List<Resource> extraClasspathJars = findExtraClasspathJars(context);
		if (extraClasspathJars != null)
			jarResources.addAll(extraClasspathJars);
		return jarResources;
	}

	@Override
	protected List<Resource> findWebInfLibJars(WebAppContext context)
			throws Exception {
		//Resource baseHome = Resource.newResource(SystemPropertyUtil.get("BASE_HOME"));
		Resource baseHome = Resource.newResource("D:/");
		if (baseHome == null || !baseHome.exists())
			return null;

		List<Resource> jarResources = new ArrayList<Resource>();
		Resource web_inf_lib = baseHome.addPath("/lib");
		if (web_inf_lib.exists() && web_inf_lib.isDirectory()) {
			String[] files = web_inf_lib.list();
			for (int f = 0; files != null && f < files.length; f++) {
				try {
					Resource file = web_inf_lib.addPath(files[f]);
					String fnlc = file.getName().toLowerCase(Locale.ENGLISH);
					int dot = fnlc.lastIndexOf('.');
					String extension = (dot < 0 ? null : fnlc.substring(dot));
					if (extension != null
							&& (extension.equals(".jar") || extension
									.equals(".zip"))) {
						jarResources.add(file);
					}
				} catch (Exception ex) {
					//LOG.warn(Log.EXCEPTION, ex);
				}
			}
		}
		return jarResources;
	}

}
