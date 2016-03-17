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
import com.github.diamond.client.util.FileUtils;
import com.github.diamond.client.util.NamedThreadFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesConfiguration.class);

    private StrSubstitutor substitutor;

    private Map<String, String> store = null;

    private Netty4Client client;

    private volatile boolean reloadable = true;

    private static final ExecutorService reloadExecutorService = Executors.newSingleThreadExecutor(
            new NamedThreadFactory("ReloadConfigExecutorService", true));

    private static String _host;
    private static int _port = 0;
    private static String _projCode;
    private static String _profile;
    private static String _modules;
    private static String _encryptPropNames;
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
        _encryptPropNames = getEncryptPropNames();
        _localFilePath = getlocalFilePath();

        connectServer(_host, _port, _projCode, _profile, _modules, _encryptPropNames, _localFilePath);
        substitutor = new StrSubstitutor(createInterpolator());
    }

    /**
     * 从jvm参数中获取 host和port值.
     *
     * @param projCode
     * @param profile
     */
    public PropertiesConfiguration(final String projCode, final String profile) {
        _host = getHost();
        _port = getPort();
        _projCode = projCode;
        _profile = profile;
        _modules = "";
        _encryptPropNames = "";
        _localFilePath = "";

        connectServer(_host, _port, _projCode, _profile, _modules, _encryptPropNames, _localFilePath);
        substitutor = new StrSubstitutor(createInterpolator());
    }

    /**
     * 从jvm参数中获取 host和port值.
     *
     * @param projCode
     * @param profile
     */
    public PropertiesConfiguration(final String projCode, final String profile, String modules, String encryptPropNames, String localFilePath) {
        _host = getHost();
        _port = getPort();
        _projCode = projCode;
        _profile = profile;
        _modules = modules;
        _encryptPropNames = encryptPropNames;
        _localFilePath = localFilePath;
        connectServer(_host, _port, _projCode, _profile, _modules, _encryptPropNames, _localFilePath);
        substitutor = new StrSubstitutor(createInterpolator());
    }

    public PropertiesConfiguration(String host, int port, final String projCode, final String profile) {
        _host = host;
        _port = port;
        _projCode = projCode;
        _profile = profile;
        _modules = "";
        _encryptPropNames = "";
        _localFilePath = "";

        connectServer(_host, _port, _projCode, _profile, _modules, _encryptPropNames, _localFilePath);
        substitutor = new StrSubstitutor(createInterpolator());
    }

    public PropertiesConfiguration(String host, int port, final String projCode,
                                   final String profile, String modules,
                                   String encryptPropNames, String localFilePath) {
        _host = host;
        _port = port;
        _projCode = projCode;
        _profile = profile;
        _modules = modules;
        _encryptPropNames = encryptPropNames;
        _localFilePath = localFilePath;

        connectServer(_host, _port, _projCode, _profile, _modules, _encryptPropNames, _localFilePath);
        substitutor = new StrSubstitutor(createInterpolator());
    }

    protected void connectServer(final String host, final int port, final String projCode,
                                 final String profile, final String modules, final String encryptPropNames,
                                 final String localFilePath) {
        Assert.notNull(projCode, "连接superdiamond， projCode不能为空");

        final String clientMsg = "superdiamond={\"projCode\": \"" + projCode + "\", \"profile\": \"" + profile + "\", "
                + "\"modules\": \"" + modules + "\", \"version\": \"1.1.0\", \"encryptPropNames\":\"" + encryptPropNames + "\"}";
        try {
            client = new Netty4Client(host, port, new ClientChannelInitializer(clientMsg));

            if (client.isConnected()) {
                String message = client.receiveMessage(FIRST_CONNECT_TIMEOUT);

                if (StringUtils.isNotBlank(message)) {
                    String versionStr = message.substring(0, message.indexOf("\r\n"));
                    LOGGER.info("加载配置信息，项目编码：{}，Profile：{}, Version：{}", projCode, profile, versionStr.split(" = ")[1]);

                    FileUtils.saveData(projCode, profile, message, localFilePath);
                    load(new StringReader(message), false);
                } else {
                    throw new ConfigurationRuntimeException("从服务器端获取配置信息为空，Client 请求信息为：" + clientMsg);
                }
            } else {
                String message = FileUtils.readConfigFromLocal(projCode, profile, localFilePath);
                if (message != null) {
                    String versionStr = message.substring(0, message.indexOf("\r\n"));
                    LOGGER.info("加载本地备份配置信息，项目编码：{}，Profile：{}, Version：{}", projCode, profile, versionStr.split(" = ")[1]);

                    load(new StringReader(message), false);
                } else {
                    throw new ConfigurationRuntimeException("本地没有备份配置数据，PropertiesConfiguration 初始化失败。");
                }
            }

            reloadExecutorService.submit(new Runnable() {

                @Override
                public void run() {
                    while (reloadable) {
                        try {
                            if (client.isConnected()) {
                                String message = client.receiveMessage();

                                if (message != null) {
                                    String versionStr = message.substring(0, message.indexOf("\r\n"));
                                    LOGGER.info("重新加载配置信息，项目编码：{}，Profile：{}, Version：{}", projCode, profile, versionStr.split(" = ")[1]);
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
                String newValue = replaceSystemEnvProps(value);
                tmpStore.put(key, newValue);
                if (reload) {
                    String oldValue = store.remove(key);
                    if (oldValue == null) {
                        fireEvent(EventType.ADD, key, newValue);
                    } else if (!oldValue.equals(newValue)) {
                        fireEvent(EventType.UPDATE, key, newValue);
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

    public static String getProjCode() {
        if (StringUtils.isNotBlank(_projCode)) {
            return _projCode;
        }

        _projCode = System.getenv("SUPERDIAMOND_PROJCODE");
        if (StringUtils.isBlank(_projCode)) {
            return System.getProperty("superdiamond.projcode");
        } else {
            return _projCode;
        }
    }

    public static String getProfile() {
        if (StringUtils.isNotBlank(_profile)) {
            return _profile;
        }

        _profile = System.getenv("SUPERDIAMOND_PROFILE");
        if (StringUtils.isBlank(_profile)) {
            return System.getProperty("superdiamond.profile", "development");
        } else {
            return _profile;
        }
    }

    public static String getModules() {
        _modules = System.getenv("SUPERDIAMOND_MODULES");
        if (_modules != null) {
            return _modules;
        }
        _modules = System.getProperty("superdiamond.modules");
        if (_modules != null) {
            return _modules;
        } else {
            return "";
        }
    }

    public static String getEncryptPropNames() {
        _encryptPropNames = System.getenv("SUPERDIAMOND_ENCRYPTPROPNAMES");
        if (_encryptPropNames != null) {
            return _encryptPropNames;
        }
        _encryptPropNames = System.getProperty("superdiamond.encryptPropNames");
        if (_encryptPropNames != null) {
            return _encryptPropNames;
        } else {
            return "";
        }
    }

    public static String getlocalFilePath() {
        _encryptPropNames = System.getenv("SUPERDIAMOND_LOCALFILEPATH");
        if (_encryptPropNames != null) {
            return _encryptPropNames;
        }
        _encryptPropNames = System.getProperty("superdiamond.localFilePath");
        if (_encryptPropNames != null) {
            return _encryptPropNames;
        } else {
            return "";
        }
    }

    public static String getHost() {
        if (StringUtils.isNotBlank(_host)) {
            return _host;
        }

        _host = System.getenv("SUPERDIAMOND_HOST");
        if (StringUtils.isBlank(_host)) {
            return System.getProperty("superdiamond.host", "localhost");
        } else {
            return _host;
        }
    }

    public static int getPort() {
        if (_port > 1) {
            return _port;
        }

        if (StringUtils.isBlank(System.getenv("SUPERDIAMOND_PORT"))) {
            return Integer.valueOf(System.getProperty("superdiamond.port", "8283"));
        } else {
            return Integer.valueOf(System.getenv("SUPERDIAMOND_PORT"));
        }
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
        if (var != null) {
            return var;
        } else {
            return null;
        }
    }

    public String getString(String key, String defaultValue) {
        String value = getProperty(key);

        if (value instanceof String) {
            return interpolate((String) value);
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

    public List<Map<String, Object>> findSystemEnvProps(String value) {
        List<Map<String, Object>> list = new ArrayList<>();
        while (value.length() > 3) {
            Map<String, Object> map = new HashMap<>();
            int start = org.apache.commons.lang.StringUtils.indexOf(value, "$<");
            if (start != -1 && start < value.length() - 1) {
                int end = org.apache.commons.lang.StringUtils.indexOf(value, '>');
                String var = value.substring(start + 2, end);
                map.put("systemEnvProp", var);
                list.add(map);
                if (end != -1 && end < value.length() - 2) {
                    value = value.substring(end + 1);
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        return list;
    }

    public String replaceSystemEnvProps(String value) {
        List<Map<String, Object>> systemEnvPropList = findSystemEnvProps(value);
        for (int i = 0; i < systemEnvPropList.size(); i++) {
            value = StringUtils.replace(value, "$<" + systemEnvPropList.get(i).get("systemEnvProp") + ">",
                    System.getenv(String.valueOf(systemEnvPropList.get(i).get("systemEnvProp"))));
        }
        return value;
    }
}

