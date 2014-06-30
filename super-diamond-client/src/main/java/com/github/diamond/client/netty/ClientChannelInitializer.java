/**        
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.    
 */    
package com.github.diamond.client.netty;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

/**
 * Create on @2013-8-24 @上午10:23:23 
 * @author bsli@ustcinfo.com
 */
public class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {
    private static final StringDecoder DECODER = new StringDecoder(CharsetUtil.UTF_8);
    private static final StringEncoder ENCODER = new StringEncoder(CharsetUtil.UTF_8);
    private static final Netty4ClientHandler CLIENTHANDLER = new Netty4ClientHandler();
    
    private String clientMsg;
    
    public ClientChannelInitializer(String clientMsg) {
		this.clientMsg = clientMsg;
	}

	@Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast("sdf", new SendConnectInfoHandler(clientMsg));
        pipeline.addLast("framer", new DelimiterBasedFrameDecoder(1024 * 1024, false, Unpooled.wrappedBuffer("#end#\r\n".getBytes())));
        pipeline.addLast("decoder", DECODER);
        pipeline.addLast("encoder", ENCODER);

        // and then business logic.
        pipeline.addLast("handler", CLIENTHANDLER);
    }
    
    public Netty4ClientHandler getClientHandler() {
    	return CLIENTHANDLER;
    }
}