/**
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.
 */

package com.github.diamond.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.CharsetUtil;

/**
 * Create on @2013-8-24 @上午10:21:08
 *
 * @author bsli@ustcinfo.com
 */
public class DiamondServerInitializer extends ChannelInitializer<SocketChannel> {
    private static final StringDecoder DECODER = new StringDecoder(CharsetUtil.UTF_8);
    private DiamondServerHandler serverHandler;

    public DiamondServerInitializer(DiamondServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast("framer", new LineBasedFrameDecoder(1024));
        pipeline.addLast("decoder", DECODER);

        pipeline.addLast("handler", serverHandler);
    }
}