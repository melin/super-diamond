package com.github.diamond.jetty;

import com.github.diamond.jetty.support.WebInfConfigurationExt;
import com.github.diamond.utils.NetUtils;
import com.github.diamond.utils.SystemPropertyUtil;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.FragmentConfiguration;
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebXmlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.File;

/**
 * 启动Jetty服务器。
 *
 * @author libinsong1204@gmail.com
 */
public class JettyServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(JettyServer.class);

    private static int maxThreads;
    private static int minThreads;
    private static int serverPort;
    private static String serverHost;

    static {
        try {
            org.apache.commons.configuration.Configuration config =
                    new PropertiesConfiguration("META-INF/res/jetty.properties");

            LOGGER.info("加载jetty.properties");

            maxThreads = config.getInt("thread.pool.max.size", 100);
            minThreads = config.getInt("thread.pool.min.size", 10);
            serverPort = config.getInt("server.port", 8080);
            serverHost = config.getString("server.host");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        Thread.currentThread().setContextClassLoader(JettyServer.class.getClassLoader());

        QueuedThreadPool pool = creatThreadPool();
        final Server server = new Server(pool);

        WebAppContext context = new WebAppContext();
        context.setResourceBase(SystemPropertyUtil.get("BASE_HOME") + File.separator + "webapp");
        //context.setResourceBase("H:\\codes\\opensources\\github\\super-diamond\\super-diamond-server\\src\\main\\webapp");
        context.setContextPath("/superdiamond");
        context.setConfigurations(new Configuration[]{
                new AnnotationConfiguration(), new WebInfConfigurationExt(),
                new WebXmlConfiguration(), new MetaInfConfiguration(),
                new FragmentConfiguration(), new JettyWebXmlConfiguration()});
        context.setThrowUnavailableOnStartupException(true);
        context.setParentLoaderPriority(true);
        context.setClassLoader(JettyServer.class.getClassLoader());
        server.setHandler(context);

        createServerConnector(server);

        addShutdownHook(server);
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * 创建线程池.
     *
     * @return
     */
    private static QueuedThreadPool creatThreadPool() {
        QueuedThreadPool pool = new QueuedThreadPool();
        pool.setMaxThreads(maxThreads);
        pool.setMinThreads(minThreads);
        pool.setIdleTimeout(60000);
        pool.setDetailedDump(false);
        return pool;
    }

    private static void createServerConnector(Server server) {
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSendServerVersion(false);
        HttpConnectionFactory factory = new HttpConnectionFactory(httpConfig);

        ServerConnector connector = new ServerConnector(server, factory);
        connector.setPort(serverPort);

        if (!StringUtils.hasText(serverHost)) {
            serverHost = NetUtils.getLocalHost();
        }
        connector.setHost(serverHost);

        LOGGER.info("jetty host={}, port={}", serverHost, serverPort);

        connector.setReuseAddress(true);
        connector.setAcceptQueueSize(1024);//backlog值
        connector.setIdleTimeout(30000);
        connector.setSoLingerTime(-1);

        server.addConnector(connector);
    }

    /**
     * 注册hook程序，保证线程能够完整执行。使用：kill -15 pid 关闭进程.
     */
    private static void addShutdownHook(final Server server) {
        //为了保证TaskThread不在中途退出，添加ShutdownHook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                LOGGER.info("收到关闭信号，hook起动，开始检测线程状态 ...");

                try {
                    server.stop();
                    server.destroy();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }

                System.out.println("================服务器停止成功================");
            }
        });
    }

    public static String getServerHost() {
        return serverHost;
    }
}
