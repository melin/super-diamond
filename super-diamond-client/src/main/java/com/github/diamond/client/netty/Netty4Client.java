/**        
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.    
 */    
package com.github.diamond.client.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.diamond.client.util.NamedThreadFactory;

/**
 * Create on @2013-8-24 @下午6:48:30 
 * @author bsli@ustcinfo.com
 */
public class Netty4Client {
	private static final Logger logger = LoggerFactory.getLogger(Netty4Client.class);
	private String host;
	private int port;
	private int timeout = 1000;
    private int connectTimeout = 3000;
    
    private final EventLoopGroup group = new NioEventLoopGroup();
    private ClientChannelInitializer channelInitializer;
    private Bootstrap bootstrap;
    private volatile Channel channel;
    private volatile ChannelFuture future;  
    
    private volatile  ScheduledFuture<?> reconnectExecutorFuture = null;
    private long lastConnectedTime = System.currentTimeMillis();
    private final AtomicInteger reconnect_count = new AtomicInteger(0);
    private final AtomicBoolean reconnect_error_log_flag = new AtomicBoolean(false) ;
    //重连warning的间隔.(waring多少次之后，warning一次)
    private final int reconnect_warning_period = 1800;	
    private final long shutdown_timeout = 1000 * 60 * 15;
    private static final ScheduledThreadPoolExecutor reconnectExecutorService = new ScheduledThreadPoolExecutor(2, new NamedThreadFactory("ClientReconnectTimer", true));
    
    public Netty4Client(String host, int port, ClientChannelInitializer channelInitializer) throws Exception {
    	this.host = host;
		this.port = port;
		this.channelInitializer = channelInitializer;

		try {
            doOpen();
        } catch (Throwable t) {
            close();
            throw new Exception("Failed to start " + getClass().getSimpleName() + " " + NetUtils.getLocalAddress() 
                                        + " connect to the server " + host + ", cause: " + t.getMessage(), t);
        }
        try {
            connect();
                
            logger.info("Start " + getClass().getSimpleName() + " " + NetUtils.getLocalAddress() + " connect to the server " + host);
        } catch (Throwable t){
            throw new Exception("Failed to start " + getClass().getSimpleName() + " " + NetUtils.getLocalAddress() 
                    + " connect to the server " + host + ", cause: " + t.getMessage(), t);
        }
    }
    
    /*
	 * 使用时，循环调用该方法获取服务端返回的信息。
	 * receiveMessage是阻塞方法，如果没有消息会等待。
	 */
	public String receiveMessage() {
		return channelInitializer.getClientHandler().getMessage();
	}
	
	public String receiveMessage(long timeout) {
		return channelInitializer.getClientHandler().getMessage(timeout);
	}
    
    private void doOpen() throws Throwable {
    	bootstrap = new Bootstrap();
    	
    	bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
    	bootstrap.option(ChannelOption.TCP_NODELAY, true);

    	bootstrap.group(group)
     	.channel(NioSocketChannel.class)
     	.handler(channelInitializer);
    }
    
    private void doConnect() throws Throwable {
        long start = System.currentTimeMillis();
        future = bootstrap.connect(getConnectAddress());
        try{
            boolean ret = future.awaitUninterruptibly(getConnectTimeout(), TimeUnit.MILLISECONDS);
            
            if (ret && future.isSuccess()) {
                Channel newChannel = future.sync().channel();
                
                try {
                    // 关闭旧的连接
                    Channel oldChannel = Netty4Client.this.channel;
                    if (oldChannel != null) {
                        logger.info("Close old netty channel " + oldChannel + " on create new netty channel " + newChannel);
                        oldChannel.close();
                    }
                } finally {
                	Netty4Client.this.channel = newChannel;
                }
            } else if (future.cause() != null) {
                throw new Exception("client failed to connect to server "
                        + getRemoteAddress() + ", error message is:" + future.cause().getMessage(), future.cause());
            } else {
                throw new Exception("client failed to connect to server "
                        + getRemoteAddress() + " client-side timeout "
                        + getConnectTimeout() + "ms (elapsed: " + (System.currentTimeMillis() - start) + "ms) from netty client "
                        + NetUtils.getLocalHost());
            }
        }finally{
            if (! isConnected()) {
                future.cancel(true);
            }
        }
    }
    
    private void connect() throws Exception {
        try {
            if (isConnected()) {
                return;
            }
            initConnectStatusCheckCommand();
            doConnect();
            if (! isConnected()) {
                throw new Exception("Failed connect to server " + getRemoteAddress() + " from " + getClass().getSimpleName() + " "
                                            + NetUtils.getLocalHost() + ", cause: Connect wait timeout: " + getTimeout() + "ms.");
            } else {
            	logger.info("Successed connect to server " + getRemoteAddress() + " from " + getClass().getSimpleName() + " "
                                            + NetUtils.getLocalHost() + ", channel is " + this.channel);
            }
            
            reconnect_count.set(0);
            reconnect_error_log_flag.set(false);
        } catch (Throwable e) {
            logger.error("Failed connect to server " + getRemoteAddress() + " from " + getClass().getSimpleName() + " "
                                        + NetUtils.getLocalHost());
        }
    }
    
    public void close() {
        destroyConnectStatusCheckCommand();
        try {
            if (channel != null) {
                channel.close();
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
        
        try {
        	group.shutdownGracefully();
        } catch (Throwable t) {
            logger.warn(t.getMessage());
        }
    }
    
    private synchronized void destroyConnectStatusCheckCommand(){
        try {
            if (reconnectExecutorFuture != null && ! reconnectExecutorFuture.isDone()){
                reconnectExecutorFuture.cancel(true);
                reconnectExecutorService.purge();
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
    }
    
    public boolean isConnected() {
        if (channel == null)
            return false;
        return channel.isActive();
    }
    
    private synchronized void initConnectStatusCheckCommand(){
        if(reconnectExecutorFuture == null || reconnectExecutorFuture.isCancelled()){
            Runnable connectStatusCheckCommand =  new Runnable() {
                public void run() {
                    try {
                        if (! isConnected()) {
                            connect();
                        } else {
                            lastConnectedTime = System.currentTimeMillis();
                        }
                    } catch (Throwable t) { 
                        String errorMsg = "client reconnect to "+getRemoteAddress()+" find error . ";
                        if (System.currentTimeMillis() - lastConnectedTime > shutdown_timeout){
                            if (!reconnect_error_log_flag.get()){
                                reconnect_error_log_flag.set(true);
                                logger.error(errorMsg, t);
                                return ;
                            }
                        }
                        if ( reconnect_count.getAndIncrement() % reconnect_warning_period == 0){
                            logger.warn(errorMsg, t);
                        }
                    }
                }
            };
            reconnectExecutorFuture = reconnectExecutorService.scheduleWithFixedDelay(connectStatusCheckCommand, 2 * 1000, 2 * 1000, TimeUnit.MILLISECONDS);
        }
    }
    
    private InetSocketAddress getConnectAddress() {
        return new InetSocketAddress(this.host, this.port);
    }
    
    private String getRemoteAddress() {
        return host + ":" + port;
    }
    
    public int getTimeout() {
        return timeout;
    }
    
    public int getConnectTimeout() {
        return connectTimeout;
    }
}
