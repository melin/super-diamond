/**        
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.    
 */    
package com.github.diamond.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.druid.support.json.JSONUtils;
import com.github.diamond.web.service.ConfigService;

/**
 * Create on @2013-8-24 @上午10:05:25 
 * @author bsli@ustcinfo.com
 */
@Sharable
public class DiamondServerHandler extends SimpleChannelInboundHandler<String> {
	
	public static ConcurrentHashMap<ClientKey /*projcode+profile*/, List<ClientInfo> /*client address*/> clients = 
			new ConcurrentHashMap<ClientKey, List<ClientInfo>>();
	
	private ConcurrentHashMap<String /*client address*/, ChannelHandlerContext> channels = 
			new ConcurrentHashMap<String, ChannelHandlerContext>();

    private static final Logger logger = LoggerFactory.getLogger(DiamondServerHandler.class);
    
    private final Charset charset = Charset.forName("UTF-8");
    
    @Autowired
    private ConfigService configService;
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    	logger.info(ctx.channel().remoteAddress() + " 连接到服务器。");
    }
    
    @SuppressWarnings("unchecked")
	@Override
    public void channelRead0(ChannelHandlerContext ctx, String request) throws Exception {
    	String config;
        if (request != null && request.startsWith("superdiamond=")) {
        	request = request.substring("superdiamond=".length());
        	
        	Map<String, String> params = (Map<String, String>) JSONUtils.parse(request);
        	String projCode = params.get("projCode");
        	String modules = params.get("modules");
        	String[] moduleArr = StringUtils.split(modules, ",");
        	String profile = params.get("profile");
        	ClientKey key = new ClientKey();
        	key.setProjCode(projCode);
        	key.setProfile(profile);
        	key.setModuleArr(moduleArr);
        	//String version = params.get("version");
        	
        	List<ClientInfo> addrs = clients.get(key);
        	if(addrs == null) {
        		addrs = new ArrayList<ClientInfo>();
        	}
        	
        	String clientAddr = ctx.channel().remoteAddress().toString();
        	ClientInfo clientInfo = new ClientInfo(clientAddr, new Date());
        	addrs.add(clientInfo);
        	clients.put(key, addrs);
        	channels.put(clientAddr, ctx);
        	
        	if(StringUtils.isNotBlank(modules)) {
                config = configService.queryConfigs(projCode, moduleArr, profile, "");
        	} else {
        		config = configService.queryConfigs(projCode, profile, "");
        	}
        } else {
        	config = "";
        }

        sendMessage(ctx, config);
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
    	
    	String address = ctx.channel().remoteAddress().toString();
    	channels.remove(address);
    	
    	for(List<ClientInfo> infos : clients.values()) {
    		for(ClientInfo client : infos) {
    			if(address.equals(client.getAddress())) {
    				infos.remove(client);
    				break;
    			}
    		}
    	}
    	
    	logger.info(ctx.channel().remoteAddress() + " 断开连接。");
    }
    
    /**
     * 向服务端推送配置数据。
     * 
     * @param projCode
     * @param profile
     * @param config
     */
    public void pushConfig(String projCode, String profile, final String module) {
    	for(ClientKey key : clients.keySet()) {
    		if(key.getProjCode().equals(projCode) && key.getProfile().equals(profile)) {
				List<ClientInfo> addrs = clients.get(key);
		    	if(addrs != null) {
		    		for(ClientInfo client : addrs) {
		    			ChannelHandlerContext ctx = channels.get(client.getAddress());
		    			if(ctx != null) {
		    				if(key.moduleArr.length == 0) {
		    					String config = configService.queryConfigs(projCode, profile, "");
		    					sendMessage(ctx, config);
		    				} else if(ArrayUtils.contains(key.getModuleArr(), module)) {
		    					String config = configService.queryConfigs(projCode, key.getModuleArr(), profile, "");
    		    				sendMessage(ctx, config);
		    				}
		    			}
		    		}
		    	}
    		}
    	}
    }
    
    private void sendMessage(ChannelHandlerContext ctx, String config) {
    	byte[] bytes = config.getBytes(charset);
    	ByteBuf message = Unpooled.buffer(4 + bytes.length);
        message.writeInt(bytes.length);
        message.writeBytes(bytes);
        ctx.writeAndFlush(message);
	}
    
    public static class ClientKey {
    	String projCode;
    	String[] moduleArr;
    	String profile;
    	
		public String getProjCode() {
			return projCode;
		}
		public void setProjCode(String projCode) {
			this.projCode = projCode;
		}
		public String[] getModuleArr() {
			return moduleArr;
		}
		public void setModuleArr(String[] moduleArr) {
			this.moduleArr = moduleArr;
		}
		public String getProfile() {
			return profile;
		}
		public void setProfile(String profile) {
			this.profile = profile;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(moduleArr);
			result = prime * result
					+ ((profile == null) ? 0 : profile.hashCode());
			result = prime * result
					+ ((projCode == null) ? 0 : projCode.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ClientKey other = (ClientKey) obj;
			if (!Arrays.equals(moduleArr, other.moduleArr))
				return false;
			if (profile == null) {
				if (other.profile != null)
					return false;
			} else if (!profile.equals(other.profile))
				return false;
			if (projCode == null) {
				if (other.projCode != null)
					return false;
			} else if (!projCode.equals(other.projCode))
				return false;
			return true;
		}
    }
    
    public static class ClientInfo {
    	private String address;
    	private Date connectTime;
    	
    	public ClientInfo(String address, Date connectTime) {
			this.address = address;
			this.connectTime = connectTime;
		}
    	
		public String getAddress() {
			return address;
		}
		public void setAddress(String address) {
			this.address = address;
		}
		public Date getConnectTime() {
			return connectTime;
		}
		public void setConnectTime(Date connectTime) {
			this.connectTime = connectTime;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((address == null) ? 0 : address.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ClientInfo other = (ClientInfo) obj;
			if (address == null) {
				if (other.address != null)
					return false;
			} else if (!address.equals(other.address))
				return false;
			return true;
		}
    }
}