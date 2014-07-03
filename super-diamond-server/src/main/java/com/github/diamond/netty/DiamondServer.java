/**        
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.    
 */    
package com.github.diamond.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Create on @2013-8-24 @上午10:03:59 
 * @author bsli@ustcinfo.com
 */
public class DiamondServer implements InitializingBean, DisposableBean {
	
	private static final Logger logger = LoggerFactory.getLogger(DiamondServer.class);
	
	private int port = 8283;
	
	private EventLoopGroup bossGroup = new NioEventLoopGroup(1);
	
	private EventLoopGroup workerGroup = new NioEventLoopGroup();
	
	private DiamondServerHandler serverHandler;

	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("==============================");
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
        	.channel(NioServerSocketChannel.class)
        	.option(ChannelOption.SO_BACKLOG, 1024)
        	.option(ChannelOption.SO_REUSEADDR, true)
        	.childHandler(new DiamondServerInitializer(serverHandler));
        
        b.bind("0.0.0.0", port).sync().channel();
        logger.info("启动 Diamond Netty Server, post={}", port);
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
