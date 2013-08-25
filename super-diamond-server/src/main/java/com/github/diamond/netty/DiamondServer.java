/**        
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.    
 */    
package com.github.diamond.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Create on @2013-8-24 @上午10:03:59 
 * @author bsli@ustcinfo.com
 */
public class DiamondServer implements InitializingBean, DisposableBean {
	
	private static final Logger logger = LoggerFactory.getLogger(DiamondServer.class);
	
	private int port = 5001;
	
	private EventLoopGroup bossGroup = new NioEventLoopGroup();
	
	private EventLoopGroup workerGroup = new NioEventLoopGroup();
	
	private DiamondServerHandler serverHandler;

	@Override
	public void afterPropertiesSet() throws Exception {
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
         .channel(NioServerSocketChannel.class)
         .childHandler(new DiamondServerInitializer(serverHandler));

        b.bind(port).sync().channel();
        logger.info("启动 Diamond Netty Server");
	}

	@Override
	public void destroy() throws Exception {
		if(bossGroup != null)
			bossGroup.shutdownGracefully();
		
		if(workerGroup != null)
			workerGroup.shutdownGracefully();
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public DiamondServerHandler getServerHandler() {
		return serverHandler;
	}

	public void setServerHandler(DiamondServerHandler serverHandler) {
		this.serverHandler = serverHandler;
	}
}
