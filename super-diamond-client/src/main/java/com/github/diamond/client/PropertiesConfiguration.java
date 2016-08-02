/**
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.
 */

package com.github.diamond.client;

import com.github.diamond.client.config.ConfigurationInterpolator;
import com.github.diamond.client.config.PropertiesReader;
import com.github.diamond.client.config.PropertyConverter;
import com.github.diamond.client.event.EventSource;
import com.github.diamond.client.event.EventType;
import com.github.diamond.client.netty.ClientChannelInitializer;
import com.github.diamond.client.netty.Netty4Client;
import com.github.diamond.client.util.EnvUtil;
import com.github.diamond.client.util.FileUtils;
import com.github.diamond.client.util.NamedThreadFactory;
import com.github.diamond.client.util.PropertiesFileUtils;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Create on @2013-8-25 @下午1:17:38
 *
 * @author bsli@ustcinfo.com
 */
public class PropertiesConfiguration extends EventSource {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesConfiguration.class);


    private StrSubstitutor substitutor;

    private Map<String, String> store = null;

    private Netty4Client client = null;

    private Netty4Client clientBak = null;

    private volatile boolean reloadable = true;

    private static final ExecutorService reloadExecutorService = Executors.newSingleThreadExecutor(
            new NamedThreadFactory("ReloadConfigExecutorService", true));
    private static final ExecutorService reloadExecutorServiceBak = Executors.newSingleThreadExecutor(
            new NamedThreadFactory("ReloadConfigExecutorServiceBak", true));

    /***
     * 服务端地址
     */
    private static String _host;

    /**
     * 服务端连接端口
     */
    private static int _port = 0;

    /**
     * 项目编码
     */
    private static String _projCode;

    /**
     * 项目profile
     */
    private static String _profile;

    /**
     * 模块列表
     */
    private static String _modules;

    /**
     * 本地缓存文件路径
     */
    private static String _localFilePath;

    private static final long FIRST_CONNECT_TIMEOUT = 2;

    /**
     * 从jvm参数中获取 projCode、profile、host和port值.
     */
    public PropertiesConfiguration() {
        _host = getHost();
        _port = getPort();
        _projCode = getProjCode();
        _profile = getProfile();
        _modules = getModules();
        _localFilePath = getLocalFilePath();

        connectServer(_host, _port, _projCode, _profile, _modules, _localFilePath);
        substitutor = new StrSubstitutor(createInterpolator());
    }

    /**
     * 从jvm参数中获取 projCode.
     *
     * @param projCode
     */
    public PropertiesConfiguration(final String projCode) {
        _host = getHost();
        _port = getPort();
        _projCode = projCode;
        _profile = getProfile();
        _modules = StringUtils.isBlank(getModules()) ? "" : getModules();
        _localFilePath = StringUtils.isBlank(getLocalFilePath()) ? "" : getLocalFilePath();

        connectServer(_host, _port, _projCode, _profile, _modules, _localFilePath);
        substitutor = new StrSubstitutor(createInterpolator());
    }

    /**
     * 从jvm参数中获取 projCode, profile.
     *
     * @param projCode
     * @param profile
     */
    public PropertiesConfiguration(final String projCode, final String profile) {
        _host = getHost();
        _port = getPort();
        _projCode = projCode;
        _profile = profile;
        _modules = StringUtils.isBlank(getModules()) ? "" : getModules();
        _localFilePath = StringUtils.isBlank(getLocalFilePath()) ? "" : getLocalFilePath();

        connectServer(_host, _port, _projCode, _profile, _modules, _localFilePath);
        substitutor = new StrSubstitutor(createInterpolator());
    }

    /**
     * 从jvm参数中获取 projCode, profile, modules.
     *
     * @param projCode
     * @param profile
     * @param modules
     */
    public PropertiesConfiguration(final String projCode, final String profile, final String modules) {
        _host = getHost();
        _port = getPort();
        _projCode = projCode;
        _profile = profile;
        _modules = modules;
        _localFilePath = StringUtils.isBlank(getLocalFilePath()) ? "" : getLocalFilePath();

        connectServer(_host, _port, _projCode, _profile, _modules, _localFilePath);
        substitutor = new StrSubstitutor(createInterpolator());
    }

    /**
     * 从jvm参数中获取 host和port值.
     *
     * @param projCode
     * @param profile
     */
    public PropertiesConfiguration(final String projCode, final String profile, String modules, String localFilePath) {
        _host = getHost();
        _port = getPort();
        _projCode = projCode;
        _profile = profile;
        _modules = modules;
        _localFilePath = localFilePath;
        connectServer(_host, _port, _projCode, _profile, _modules, _localFilePath);
        substitutor = new StrSubstitutor(createInterpolator());
    }

    public PropertiesConfiguration(String host, int port, final String projCode, final String profile) {
        _host = host;
        _port = port;
        _projCode = projCode;
        _profile = profile;
        _modules = StringUtils.isBlank(getModules()) ? "" : getModules();
        _localFilePath = StringUtils.isBlank(getLocalFilePath()) ? "" : getLocalFilePath();

        connectServer(_host, _port, _projCode, _profile, _modules, _localFilePath);
        substitutor = new StrSubstitutor(createInterpolator());
    }


    public PropertiesConfiguration(String host, int port, final String projCode, final String profile, final String modules) {
        _host = host;
        _port = port;
        _projCode = projCode;
        _profile = profile;
        _modules = modules;
        _localFilePath = StringUtils.isBlank(getLocalFilePath()) ? "" : getLocalFilePath();

        connectServer(_host, _port, _projCode, _profile, _modules, _localFilePath);
        substitutor = new StrSubstitutor(createInterpolator());
    }

    public PropertiesConfiguration(String host, int port, final String projCode,
                                   final String profile, String modules,
                                   String localFilePath) {
        _host = host;
        _port = port;
        _projCode = projCode;
        _profile = profile;
        _modules = modules;
        _localFilePath = localFilePath;

        connectServer(_host, _port, _projCode, _profile, _modules, _localFilePath);
        substitutor = new StrSubstitutor(createInterpolator());
    }

    protected void connectServer(final String host, final int port, final String projCode,
                                 final String profile, final String modules,
                                 final String localFilePath) {
        Assert.notNull(projCode, "连接superdiamond， projCode不能为空");

        final String clientMsg = "superdiamond={\"projCode\": \"" + projCode + "\", \"profile\": \"" + profile + "\", "
                + "\"modules\": \"" + modules + "\", \"version\": \"" + EnvUtil.getBuildVersion() +" \"}";
        String[] hostArr = StringUtils.split(host, ",");
        try {
            if (hostArr.length >= 2) {
                client = new Netty4Client(hostArr[0], port, new ClientChannelInitializer(clientMsg));
                clientBak = new Netty4Client(hostArr[1], port, new ClientChannelInitializer(clientMsg));
            } else if (hostArr.length == 1) {
                client = new Netty4Client(hostArr[0], port, new ClientChannelInitializer(clientMsg));
            }
            if (client != null && client.isConnected()) {
                String message = client.receiveMessage(FIRST_CONNECT_TIMEOUT);
                if (StringUtils.isNotBlank(message)) {
                    String versionStr = message.substring(0, message.indexOf("\r\n"));
                    logger.info("加载配置信息，项目编码：{}，Profile：{}, Version：{}", projCode, profile, versionStr.split(" = ")[1]);

                    FileUtils.saveData(projCode, profile, message, localFilePath);
                    load(new StringReader(message), false);
                } else {
                    throw new ConfigurationRuntimeException("从服务器端获取配置信息为空，Client 请求信息为：" + clientMsg);
                }
            } else if (clientBak != null && clientBak.isConnected()) {
                String message = clientBak.receiveMessage(FIRST_CONNECT_TIMEOUT);
                if (StringUtils.isNotBlank(message)) {
                    String versionStr = message.substring(0, message.indexOf("\r\n"));
                    logger.info("加载配置信息，项目编码：{}，Profile：{}, Version：{}", projCode, profile, versionStr.split(" = ")[1]);

                    FileUtils.saveData(projCode, profile, message, localFilePath);
                    load(new StringReader(message), false);
                } else {
                    throw new ConfigurationRuntimeException("从服务器端获取配置信息为空，Client 请求信息为：" + clientMsg);
                }
            } else {
                String message = FileUtils.readConfigFromLocal(projCode, profile, localFilePath);
                if (message != null) {
                    String versionStr = message.substring(0, message.indexOf("\r\n"));
                    logger.info("加载本地备份配置信息，项目编码：{}，Profile：{}, Version：{}", projCode, profile, versionStr.split(" = ")[1]);

                    load(new StringReader(message), false);
                } else {
                    throw new ConfigurationRuntimeException("本地没有备份配置数据，PropertiesConfiguration 初始化失败。");
//                    createClientBak(_hostBak, port, projCode, profile, clientMsg, localFilePath);
                }
            }


            reloadExecutorService.submit(new Runnable() {

                @Override
                public void run() {
                    while (reloadable) {
                        try {
                            if (client.isConnected()) {
                                String message = client.receiveMessage();

                                if (StringUtils.isNotBlank(message)) {

                                    if(message.equals(Netty4Client.HEART_BEAT_MSG)) {
                                        // TODO: handle heartbeat response message
                                    } else {
                                        String versionStr = message.substring(0, message.indexOf("\r\n"));
                                        logger.info("重新加载配置信息，项目编码：{}，Profile：{}, Version：{}", projCode, profile, versionStr.split(" = ")[1]);
                                        FileUtils.saveData(projCode, profile, message, localFilePath);
                                        load(new StringReader(message), true);
                                    }
                                }
                            } else {
                                TimeUnit.SECONDS.sleep(1);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            if (hostArr.length >= 2) {
                reloadExecutorServiceBak.submit(new Runnable() {

                    @Override
                    public void run() {
                        while (reloadable) {
                            try {
                                if (clientBak.isConnected()) {
                                    String message = clientBak.receiveMessage();

                                    if (message != null) {
                                        String versionStr = message.substring(0, message.indexOf("\r\n"));
                                        logger.info("重新加载配置信息，项目编码：{}，Profile：{}, Version：{}", projCode, profile, versionStr.split(" = ")[1]);
                                        FileUtils.saveData(projCode, profile, message, localFilePath);
                                        load(new StringReader(message), true);
                                    }
                                } else {
                                    TimeUnit.SECONDS.sleep(1);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }

        } catch (Exception e) {
            if (client != null) {
                client.close();
            }
            throw new ConfigurationRuntimeException(e.getMessage(), e);
        }
    }

    public void close() {
        reloadable = false;

        if (client != null && client.isConnected()) {
            client.close();
        }
    }

    public void load(String config) throws ConfigurationRuntimeException {
        load(new StringReader(config), false);
    }

    /**
     * 加载配置文件.
     *
     * @param in
     * @param reload 初次初始化加载为false，服务端推送加载为true。
     * @throws Exception
     */
    public void load(Reader in, boolean reload) throws ConfigurationRuntimeException {
        Map<String, String> tmpStore = new LinkedHashMap<String, String>();

        PropertiesReader reader = new PropertiesReader(in);
        try {
            while (reader.nextProperty()) {
                String key = reader.getPropertyName();
                String value = reader.getPropertyValue();
                tmpStore.put(key, value);
                if (reload) {
                    String oldValue = store.remove(key);
                    if (oldValue == null) {
                        fireEvent(EventType.ADD, key, value);
                    } else if (!oldValue.equals(value)) {
                        fireEvent(EventType.UPDATE, key, value);
                    }
                }
            }

            if (reload) {
                for (String key : store.keySet()) {
                    fireEvent(EventType.CLEAR, key, store.get(key));
                }
            }
        } catch (IOException ioex) {
            throw new ConfigurationRuntimeException(ioex);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                ;
            }
        }

        if (store != null) {
            store.clear();
        }

        store = tmpStore;
    }

    /**
     * 获取服务端连接host
     */
    public static String getHost() {
        if (StringUtils.isNotBlank(_host)) {
            return _host;
        }

        if (PropertiesFileUtils.getInstance().propertiesLoaded()) {
            try {
                _host = String.valueOf(PropertiesFileUtils.getInstance().getProperty(Constants.HOST_KEY));
                if (StringUtils.isNotBlank(_host)) {
                    logger.info("get host from local properties file success, host is:" + _host);
                }
            } catch (Exception e) {
                logger.error("getHost exception", e);
            }
        }

        if (StringUtils.isBlank(_host)) {
            _host = System.getProperty(Constants.HOST_KEY);
            if (StringUtils.isNotBlank(_host)) {
                logger.info("get host from JVM property success, host is:" + _host);
            }
        }

        if (StringUtils.isBlank(_host)) {
            _host = System.getenv(Constants.HOST_ENV_NAME);
            if (StringUtils.isNotBlank(_host)) {
                logger.info("get host from ENV success, host is:" + _host);
            }
        }

        return _host;
    }

    /**
     * 获取服务端连接port
     */
    public static int getPort() {
        if (_port > 1) {
            return _port;
        }

        if (PropertiesFileUtils.getInstance().propertiesLoaded()) {
            try {
                _port = Integer.valueOf(String.valueOf(PropertiesFileUtils.getInstance().getProperty(Constants.PORT_KEY)));
                if (_port > 0) {
                    logger.info("get port from local properties file success, port is:" + _port);
                }
            } catch (Exception e) {
                logger.error("getPort exception", e);
            }
        }

        if (_port == 0) {
            _port = Integer.valueOf(System.getProperty(Constants.PORT_KEY, "0"));
            if (_port > 0) {
                logger.info("get port from JVM property success, port is:" + _port);
            }
        }

        if (_port == 0) {
            _port = Integer.valueOf(System.getenv(Constants.PORT_ENV_NAME));
            if (_port > 0) {
                logger.info("get port from ENV  success, port is:" + _port);
            }
        }

        return _port;
    }

    public static String getProjCode() {
        if (StringUtils.isNotBlank(_projCode)) {
            return _projCode;
        }

        if (PropertiesFileUtils.getInstance().propertiesLoaded()) {
            try {
                Object val = PropertiesFileUtils.getInstance().getProperty(Constants.PROJECT_CODE_KEY);
                _projCode = val == null ? "" : String.valueOf(val);
                if (StringUtils.isNotBlank(_projCode)) {
                    logger.info("get project code from local properties file success, project code is:" + _projCode);
                }
            } catch (Exception e) {
                logger.error("getProjCode  exception", e);
            }
        }

        if (StringUtils.isBlank(_projCode)) {
            _projCode = System.getProperty(Constants.PROJECT_CODE_KEY);
            if (StringUtils.isNotBlank(_projCode)) {
                logger.info("get project code from JVM property success, project code is:" + _projCode);
            }
        }

        if (StringUtils.isBlank(_projCode)) {
            _projCode = System.getenv(Constants.PROJECT_CODE_ENV_NAME);
            if (StringUtils.isNotBlank(_projCode)) {
                logger.info("get project code from ENV  success, project code is:" + _projCode);
            }
        }

        return _projCode;
    }

    public static String getProfile() {
        if (StringUtils.isNotBlank(_profile)) {
            return _profile;
        }

        if (PropertiesFileUtils.getInstance().propertiesLoaded()) {
            try {
                _profile = String.valueOf(PropertiesFileUtils.getInstance().getProperty(Constants.PROFILE_KEY));
                if (StringUtils.isNotBlank(_profile)) {
                    logger.info("get profile from local properties file success, profile is:" + _profile);
                }
            } catch (Exception e) {
                logger.error("getProfile  exception", e);
            }
        }

        if (StringUtils.isBlank(_profile)) {
            _profile = System.getProperty(Constants.PROFILE_KEY);
            if (StringUtils.isNotBlank(_profile)) {
                logger.info("get profile from JVM property success, profile is:" + _profile);
            }
        }

        if (StringUtils.isBlank(_profile)) {
            _profile = System.getenv(Constants.PROFILE_ENV_NAME);
            if (StringUtils.isNotBlank(_profile)) {
                logger.info("get profile from ENV  success, profile  is:" + _profile);
            }
        }

        return _profile;
    }

    public static String getModules() {

        if (StringUtils.isNotBlank(_modules)) {
            return _modules;
        }

        if (PropertiesFileUtils.getInstance().propertiesLoaded()) {
            try {
                _modules = String.valueOf(PropertiesFileUtils.getInstance().getProperty(Constants.MODULES_KEY));
                if (StringUtils.isNotBlank(_modules)) {
                    logger.info("get modules from local properties file success, modules is:" + _modules);
                }
            } catch (Exception e) {
                logger.error("getModules  exception", e);
            }
        }

        if (StringUtils.isBlank(_modules)) {
            _modules = System.getProperty(Constants.MODULES_KEY);
            if (StringUtils.isNotBlank(_modules)) {
                logger.info("get modules from JVM property success, modules is:" + _modules);
            }
        }

        if (StringUtils.isBlank(_modules)) {
            _modules = System.getenv(Constants.MODULES_ENV_NAME);
            if (StringUtils.isNotBlank(_modules)) {
                logger.info("get modules from ENV success, modules is:" + _modules);
            }
        }

        return StringUtils.isBlank(_modules) ? "" : _modules;
    }

    public static String getLocalFilePath() {

        if (StringUtils.isNotBlank(_localFilePath)) {
            return _localFilePath;
        }

        if (PropertiesFileUtils.getInstance().propertiesLoaded()) {
            try {
                _localFilePath = String.valueOf(PropertiesFileUtils.getInstance().getProperty(Constants.LOCAL_FILE_PATH_KEY));
                if (StringUtils.isNotBlank(_localFilePath)) {
                    logger.info("get localFilePath from local properties file success, localFilePath is:" + _localFilePath);
                }
            } catch (Exception e) {
                logger.error("getLocalFilePath  exception", e);
            }
        }


        if (StringUtils.isBlank(_localFilePath)) {
            _localFilePath = System.getProperty(Constants.LOCAL_FILE_PATH_KEY);
            if (StringUtils.isNotBlank(_localFilePath)) {
                logger.info("get localFilePath from JVM property success, localFilePath is:" + _localFilePath);
            }
        }

        if (StringUtils.isBlank(_localFilePath)) {
            _localFilePath = System.getenv(Constants.MODULES_ENV_NAME);
            if (StringUtils.isNotBlank(_localFilePath)) {
                logger.info("get localFilePath from ENV success, localFilePath is:" + _localFilePath);
            }
        }

        return StringUtils.isBlank(_localFilePath) ? "" : _localFilePath;
    }

    private String getProperty(String key) {
        return store.get(key);
    }

    public Properties getProperties() {
        Properties properties = new Properties();

        for (String key : store.keySet()) {
            properties.setProperty(key, getString(key));
        }
        return properties;
    }

    public boolean getBoolean(String key) {
        Boolean var = getBoolean(key, null);
        if (var != null) {
            return var.booleanValue();
        } else {
            throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return getBoolean(key, BooleanUtils.toBooleanObject(defaultValue))
                .booleanValue();
    }

    public Boolean getBoolean(String key, Boolean defaultValue) {
        String value = getProperty(key);

        if (value == null) {
            return defaultValue;
        } else {
            try {
                return PropertyConverter.toBoolean(interpolate(value));
            } catch (ConversionException e) {
                throw new ConversionException('\'' + key + "' doesn't map to a Boolean object", e);
            }
        }
    }

    public byte getByte(String key) {
        Byte var = getByte(key, null);
        if (var != null) {
            return var.byteValue();
        } else {
            throw new NoSuchElementException('\'' + key + " doesn't map to an existing object");
        }
    }

    public byte getByte(String key, byte defaultValue) {
        return getByte(key, new Byte(defaultValue)).byteValue();
    }

    public Byte getByte(String key, Byte defaultValue) {
        String value = getProperty(key);

        if (value == null) {
            return defaultValue;
        } else {
            try {
                return PropertyConverter.toByte(interpolate(value));
            } catch (ConversionException e) {
                throw new ConversionException('\'' + key + "' doesn't map to a Byte object", e);
            }
        }
    }

    public double getDouble(String key) {
        Double var = getDouble(key, null);
        if (var != null) {
            return var.doubleValue();
        } else {
            throw new NoSuchElementException('\'' + key
                    + "' doesn't map to an existing object");
        }
    }

    public double getDouble(String key, double defaultValue) {
        return getDouble(key, new Double(defaultValue)).doubleValue();
    }

    public Double getDouble(String key, Double defaultValue) {
        String value = getProperty(key);

        if (value == null) {
            return defaultValue;
        } else {
            try {
                return PropertyConverter.toDouble(interpolate(value));
            } catch (ConversionException e) {
                throw new ConversionException('\'' + key + "' doesn't map to a Double object", e);
            }
        }
    }

    public float getFloat(String key) {
        Float var = getFloat(key, null);
        if (var != null) {
            return var.floatValue();
        } else {
            throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
        }
    }

    public float getFloat(String key, float defaultValue) {
        return getFloat(key, new Float(defaultValue)).floatValue();
    }

    public Float getFloat(String key, Float defaultValue) {
        String value = getProperty(key);

        if (value == null) {
            return defaultValue;
        } else {
            try {
                return PropertyConverter.toFloat(interpolate(value));
            } catch (ConversionException e) {
                throw new ConversionException('\'' + key + "' doesn't map to a Float object", e);
            }
        }
    }

    public int getInt(String key) {
        Integer var = getInteger(key, null);
        if (var != null) {
            return var.intValue();
        } else {
            throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
        }
    }

    public int getInt(String key, int defaultValue) {
        Integer var = getInteger(key, null);

        if (var == null) {
            return defaultValue;
        }

        return var.intValue();
    }

    public Integer getInteger(String key, Integer defaultValue) {
        String value = getProperty(key);

        if (value == null) {
            return defaultValue;
        } else {
            try {
                return PropertyConverter.toInteger(interpolate(value));
            } catch (ConversionException e) {
                throw new ConversionException('\'' + key + "' doesn't map to an Integer object", e);
            }
        }
    }

    public long getLong(String key) {
        Long var = getLong(key, null);
        if (var != null) {
            return var.longValue();
        } else {
            throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
        }
    }

    public long getLong(String key, long defaultValue) {
        return getLong(key, new Long(defaultValue)).longValue();
    }

    public Long getLong(String key, Long defaultValue) {
        String value = getProperty(key);

        if (value == null) {
            return defaultValue;
        } else {
            try {
                return PropertyConverter.toLong(interpolate(value));
            } catch (ConversionException e) {
                throw new ConversionException('\'' + key + "' doesn't map to a Long object", e);
            }
        }
    }

    public short getShort(String key) {
        Short var = getShort(key, null);
        if (var != null) {
            return var.shortValue();
        } else {
            throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
        }
    }

    public short getShort(String key, short defaultValue) {
        return getShort(key, new Short(defaultValue)).shortValue();
    }

    public Short getShort(String key, Short defaultValue) {
        String value = getProperty(key);

        if (value == null) {
            return defaultValue;
        } else {
            try {
                return PropertyConverter.toShort(interpolate(value));
            } catch (ConversionException e) {
                throw new ConversionException('\'' + key + "' doesn't map to a Short object", e);
            }
        }
    }

    public String getString(String key) {
        String var = getString(key, null);
        return var;
    }

    public String getString(String key, String defaultValue) {
        String value = getProperty(key);

        if (value instanceof String) {
            return interpolate(value);
        } else {
            return interpolate(defaultValue);
        }
    }

    protected String interpolate(String value) {
        Object result = substitutor.replace(value);
        return (result == null) ? null : result.toString();
    }

    protected ConfigurationInterpolator createInterpolator() {
        ConfigurationInterpolator interpol = new ConfigurationInterpolator();
        interpol.setDefaultLookup(new StrLookup() {
            @Override
            public String lookup(String var) {
                String prop = getProperty(var);
                return (prop != null) ? prop : null;
            }
        });
        return interpol;
    }
}

