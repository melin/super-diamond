package com.github.diamond.client.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.Charset;

/**
 * 客户端连接到server，发送client信息：superdiamond,projCode,profile\r\n
 * <p/>
 * Create on @2013-8-24 @下午10:31:29
 *
 * @author bsli@ustcinfo.com
 */
public class SendConnectInfoHandler extends ChannelInboundHandlerAdapter {

    private String clientMsg;

    private final Charset charset;

    public SendConnectInfoHandler(String clientMsg) {
        charset = Charset.forName("UTF-8");
        this.clientMsg = clientMsg;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String msg = clientMsg + "\r\n";
        ByteBuf encoded = Unpooled.copiedBuffer(msg, charset);
        ctx.channel().writeAndFlush(encoded);
    }
}
