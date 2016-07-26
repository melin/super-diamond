/**
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.
 */

package com.github.diamond.netty;

import com.alibaba.druid.support.json.JSONUtils;
import com.github.diamond.web.service.impl.ConfigServiceImpl;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Create on @2013-8-24 @上午10:05:25.
 *
 * @author bsli@ustcinfo.com
 */
@Sharable
public class DiamondServerHandler extends SimpleChannelInboundHandler<String> {

    public final String HEART_BEAT_MSG = "heartbeat";

    public static ConcurrentHashMap<ClientKey /*projcode+profile*/, List<ClientInfo>/*client address*/> clients =
            new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String /*client address*/, ChannelHandlerContext> channels =
            new ConcurrentHashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(DiamondServerHandler.class);

    private final Charset charset = Charset.forName("UTF-8");

    @Autowired
    private ConfigServiceImpl configServiceImpl;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info(ctx.channel().remoteAddress() + " 连接到服务器。");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void channelRead0(ChannelHandlerContext ctx, String request) throws Exception {
        String returnMsg = "";
        String clientAddr = ctx.channel().remoteAddress().toString();
        logger.info("连接的clientAddr为:{}, 请求内容为:{}", clientAddr, request);


        if (!StringUtils.isBlank(request)) {
            if (request.equals(HEART_BEAT_MSG)) {
                // TODO: handle client heartbeat
                returnMsg = HEART_BEAT_MSG;
            } else if (request.startsWith("superdiamond=") || request.startsWith("superdiamond,")) {
                String projCode;
                String profile;
                String modules;
                String clientVersion;

                logger.info("连接的clientAddr为：" + clientAddr);
                if (request.startsWith("superdiamond=")) {
                    request = request.substring("superdiamond=".length());
                    Map<String, String> params = (Map<String, String>) JSONUtils.parse(request);
                    projCode = params.get("projCode");
                    modules = params.get("modules");
                    profile = params.get("profile");
                    clientVersion = params.get("version");
                } else {
                    // 兼容老版本客户端
                    String[] projInfo = StringUtils.split(request, ",");
                    projCode = projInfo[1];
                    modules = "";
                    profile = projInfo[2];
                    clientVersion = "";
                    logger.warn("Old version client found, clientAddr: {}, projCode: {}, profile: {}", clientAddr, projCode, profile);
                }

                String[] moduleArr = StringUtils.split(modules, ",");

                ClientKey key = new ClientKey();
                key.setProjCode(projCode);
                key.setProfile(profile);
                key.setModuleArr(moduleArr);
                key.setVersion(clientVersion);

                List<ClientInfo> addrs = clients.get(key);
                if (addrs == null) {
                    addrs = new ArrayList<>();
                }

                ClientInfo clientInfo = new ClientInfo(clientAddr, new Date());
                addrs.add(clientInfo);
                clients.put(key, addrs);
                channels.put(clientAddr, ctx);

                if (StringUtils.isNotBlank(modules)) {
                    returnMsg = configServiceImpl.queryConfigs(projCode, moduleArr, profile, "");
                } else {
                    returnMsg = configServiceImpl.queryConfigs(projCode, profile, "");
                }
            }
        }

        sendMessage(ctx, returnMsg);
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

        for (List<ClientInfo> infos : clients.values()) {
            for (ClientInfo client : infos) {
                if (address.equals(client.getAddress())) {
                    infos.remove(client);
                    break;
                }
            }
        }

        logger.info(ctx.channel().remoteAddress() + " 断开连接。");
    }

    /**
     * 向客户端推送配置数据.
     *
     * @param projCode 项目编码
     * @param profile  项目环境
     */
    public void pushConfig(String projCode, String profile) {
        for (ClientKey key : clients.keySet()) {
            if (!"".equals(projCode)) {
                if (key.getProjCode().equals(projCode) && key.getProfile().equals(profile)) {
                    List<ClientInfo> addrs = clients.get(key);
                    if (addrs != null) {
                        for (ClientInfo client : addrs) {
                            ChannelHandlerContext ctx = channels.get(client.getAddress());
                            if (ctx != null) {
                                if (key.moduleArr.length == 0) {
                                    String config = configServiceImpl.queryConfigs(projCode, profile, "");
                                    sendMessage(ctx, config);
                                } else {
                                    String config = configServiceImpl.queryConfigs(projCode, key.getModuleArr(), profile, "");
                                    sendMessage(ctx, config);
                                }
                            }
                        }
                    }
                }
            } else {
                if (key.getProfile().equals(profile)) {
                    List<ClientInfo> addrs = clients.get(key);
                    if (addrs != null) {
                        for (ClientInfo client : addrs) {
                            ChannelHandlerContext ctx = channels.get(client.getAddress());
                            if (ctx != null) {
                                if (key.moduleArr.length == 0) {
                                    String config = configServiceImpl.queryConfigs(key.getProjCode(), profile, "");
                                    sendMessage(ctx, config);
                                } else {
                                    String config = configServiceImpl.queryConfigs(key.getProjCode(), key.getModuleArr(), profile, "");
                                    sendMessage(ctx, config);
                                }
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
        String version;

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

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
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
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ClientKey other = (ClientKey) obj;
            if (!Arrays.equals(moduleArr, other.moduleArr)) {
                return false;
            }
            if (profile == null) {
                if (other.profile != null) {
                    return false;
                }
            } else if (!profile.equals(other.profile)) {
                return false;
            }
            if (projCode == null) {
                if (other.projCode != null) {
                    return false;
                }
            } else if (!projCode.equals(other.projCode)) {
                return false;
            }
            return true;
        }
    }

    public static class ClientInfo {
        private String address;
        private Date connectTime;
        private Date lastUpdateTime;

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

        public Date getLastUpdateTime() {
            return lastUpdateTime;
        }

        public void setLastUpdateTime(Date lastUpdateTime) {
            this.lastUpdateTime = lastUpdateTime;
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
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ClientInfo other = (ClientInfo) obj;
            if (address == null) {
                if (other.address != null) {
                    return false;
                }
            } else if (!address.equals(other.address)) {
                return false;
            }

            return true;
        }

        private void updateClientInfo() {

        }
    }
}