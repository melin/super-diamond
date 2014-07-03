package com.github.diamond.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.GenericServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 打印工程版本信息
 * 
 * @author libinsong1204@gmail.com
 * @date 2012-3-1 下午1:20:50
 */
@SuppressWarnings("serial")
public class PrintProjectVersionServlet extends GenericServlet {
	private static final Logger logger = LoggerFactory.getLogger(PrintProjectVersionServlet.class);
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		StringBuilder sBuilder = new StringBuilder("\n");
		try {
            Enumeration<java.net.URL> urls;
            ClassLoader classLoader = findClassLoader();
            if (classLoader != null) {
                urls = classLoader.getResources("META-INF/res/env.properties");
            } else {
                urls = ClassLoader.getSystemResources("META-INF/res/env.properties");
            }
            
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    java.net.URL url = urls.nextElement();
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
                        Properties properties = new Properties();
                        properties.load(reader);
                        
                        sBuilder.append("项目名称：").append(properties.getProperty("project.name")).append(", ");
                        sBuilder.append("项目版本：").append(properties.getProperty("build.version")).append(", ");
                        sBuilder.append("构建时间：").append(properties.getProperty("build.time")).append(".\n");
                    } catch (Throwable t) {
                        logger.error(t.getMessage(), t);
                    }
                }
            }
		} catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
		
		String info = sBuilder.toString();
		System.out.println("===========================================================================================================================");
		System.out.println(info);
		System.out.println("===========================================================================================================================");
	}

	@Override
	public void service(ServletRequest req, ServletResponse res)
			throws ServletException, IOException {
	}
	
	private static ClassLoader findClassLoader() {
		return  PrintProjectVersionServlet.class.getClassLoader();
    }

}
