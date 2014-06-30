package com.github.diamond.client.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 客户端连接到server，发送client信息：superdiamond,projCode,profile\r\n
 * 
 * Create on @2013-8-24 @下午10:31:29 
 * @author bsli@ustcinfo.com
 */
public class SendConnectInfoHandler extends ChannelInboundHandlerAdapter {
	
	private String clientMsg;
    
    public SendConnectInfoHandler(String clientMsg) {
		this.clientMsg = clientMsg;
	}
    
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.channel().writeAndFlush(clientMsg + "\r\n");
	}
}
