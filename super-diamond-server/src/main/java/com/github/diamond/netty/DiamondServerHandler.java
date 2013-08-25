/**        
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.    
 */    
package com.github.diamond.netty;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create on @2013-8-24 @上午10:05:25 
 * @author bsli@ustcinfo.com
 */
@Sharable
public class DiamondServerHandler extends SimpleChannelInboundHandler<String> {
	
	private ConcurrentHashMap<String /*projcode+profile*/, List<String> /*client address*/> clients = 
			new ConcurrentHashMap<String, List<String>>();
	
	private ConcurrentHashMap<String /*projcode+profile*/, ChannelHandlerContext /*client address*/> channels = 
			new ConcurrentHashMap<String, ChannelHandlerContext>();

    private static final Logger logger = LoggerFactory.getLogger(DiamondServerHandler.class);
    
    private static DiamondServerHandler instance = new DiamondServerHandler();
    
    private DiamondServerHandler() {};
    
    public static DiamondServerHandler getInstance() {
    	return instance;
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    	logger.info(ctx.channel().remoteAddress() + " 连接到服务器。");
    }
    
    @Override
    public void channelRead0(ChannelHandlerContext ctx, String request) throws Exception {
    	String response;
        if (request != null && request.startsWith("superdiamond")) {
        	String[] params = request.split(",");
        	
        	List<String> addrs = clients.get(params[1] + "-" + params[2]);
        	if(addrs == null) {
        		addrs = new ArrayList<String>();
        	}
        	addrs.add(ctx.channel().remoteAddress().toString());
        	clients.put(params[1] + "-" + params[2], addrs);
        	
        	channels.put(ctx.channel().remoteAddress().toString(), ctx);
        	
            response = "jdbc.url=jdbc:mysql://192.168.205.31:3306/usi_tdp?useUnicode=true&amp;characterEncoding=UTF-8\r\n";
            response += "jdbc.username=roo\r\n";
            response += "\r\n";
        } else {
        	response = "";
        }

        ctx.writeAndFlush(response);
    }
    
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    	ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    	super.channelInactive(ctx);
    	
    	channels.remove(ctx.channel().remoteAddress().toString());
    	
    	logger.info(ctx.channel().remoteAddress() + " 断开连接。");
    }
    
    /**
     * 向服务端推送配置数据。
     * 
     * @param projCode
     * @param profile
     * @param config
     */
    public void pushConfig(String projCode, String profile, String config) {
    	List<String> addrs = clients.get(projCode + "-" + profile);
    	if(addrs != null) {
    		for(String address : addrs) {
    			ChannelHandlerContext ctx = channels.get(address);
    			if(ctx != null) {
    				ctx.writeAndFlush(config);
    			} else {
    				addrs.remove(address);
    			}
    		}
    	}
    }
}