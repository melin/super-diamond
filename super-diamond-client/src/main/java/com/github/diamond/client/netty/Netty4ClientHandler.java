/**        
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.    
 */    
package com.github.diamond.client.netty;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create on @2013-8-24 @下午10:31:29 
 * @author bsli@ustcinfo.com
 */
@Sharable
public class Netty4ClientHandler extends SimpleChannelInboundHandler<String> {

	private static final Logger logger = LoggerFactory.getLogger(Netty4ClientHandler.class);
    
    private final LinkedBlockingQueue<String> queue;

    public Netty4ClientHandler() {
    	queue = new LinkedBlockingQueue<String>();
	}

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String message) throws Exception {
    	queue.add(message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.info("Unexpected exception from downstream.", cause);
        ctx.close();
    }
    
    public String getMessage() {
		String message = null;
		try {
			message = queue.take();
		} catch (InterruptedException e) {
		}
		return message;
	}
}