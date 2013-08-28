/**        
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.    
 */
package com.github.diamond.client;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.PropertyConverter;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.diamond.client.config.PropertiesReader;
import com.github.diamond.client.event.EventSource;
import com.github.diamond.client.event.EventType;
import com.github.diamond.client.netty.ClientChannelInitializer;
import com.github.diamond.client.netty.NamedThreadFactory;
import com.github.diamond.client.netty.Netty4Client;
import com.github.diamond.client.util.FileUtils;

/**
 * Create on @2013-8-25 @下午1:17:38
 * 
 * @author bsli@ustcinfo.com
 */
public class PropertiesConfiguration extends EventSource {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesConfiguration.class);

	private static char defaultListDelimiter = ',';

	private Map<String, Object> store = null;
	
	private Map<String, String> strStore = null;
	
	private Netty4Client client;
	
	private volatile boolean reloadable = true;
	
	private static final ExecutorService reloadExecutorService = Executors.newSingleThreadExecutor(new NamedThreadFactory("ReloadConfigExecutorService", true));
	
	/**
	 * 从jvm参数中获取 host和port值
	 * 
	 * @param projCode
	 * @param profile
	 */
	public PropertiesConfiguration(final String projCode, final String profile) {
		String host = System.getProperty("spuerdiamond.host", "localhost");
		int port = Integer.valueOf(System.getProperty("spuerdiamond.port", "5001"));
		
		init(host, port, projCode, profile);
	}

	public PropertiesConfiguration(String host, int port, final String projCode, final String profile) {
		init(host, port, projCode, profile);
	}
	
	private void init(String host, int port, final String projCode, final String profile) {
		final String clientMsg = "superdiamond," + projCode + "," + profile;
		try {
			client = new Netty4Client(host, port, new ClientChannelInitializer());
			
			if(client.isConnected()) {
				client.sendMessage(clientMsg);
				String message = client.receiveMessage();
				String versionStr = message.substring(0, message.indexOf("\r\n"));
				LOGGER.info("加载配置信息，项目编码：{}，Profile：{}, Version：{}", projCode, profile, versionStr.split(" = ")[1]);
				
				FileUtils.saveData(projCode, profile, message);
				if(message != null)
					load(new StringReader(message), false);
			} else {
				String message = FileUtils.readConfigFromLocal(projCode, profile);
				if(message != null) {
					String versionStr = message.substring(0, message.indexOf("\r\n"));
					LOGGER.info("加载本地备份配置信息，项目编码：{}，Profile：{}, Version：{}", projCode, profile, versionStr.split(" = ")[1]);
					
					load(new StringReader(message), false);
				} else
					throw new Exception("本地没有备份配置数据，PropertiesConfiguration 初始化失败。");
			}
			
			reloadExecutorService.submit(new Runnable() {
				
				@Override
				public void run() {
					while(reloadable) {
						try {
							if(client.isConnected()) {
								String message = client.receiveMessage();
								String versionStr = message.substring(0, message.indexOf("\r\n"));
								
								LOGGER.info("重新加载配置信息，项目编码：{}，Profile：{}, Version：{}", projCode, profile, versionStr.split(" = ")[1]);
								if(message != null) {
									FileUtils.saveData(projCode, profile, message);
									load(new StringReader(message), true);
								}
							} else {
								TimeUnit.SECONDS.sleep(1);
							}
						} catch(Exception e) {
							
						}
					}
				}
			});
		} catch (Exception e) {
			if(client != null) {
				client.close();
			}
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	public void close() {
		reloadable = false;
		
		if(client != null && client.isConnected())
			client.close();
	}

	/**
	 * 加载配置文件
	 * 
	 * @param in
	 * @param reload 初次初始化加载为false，服务端推送加载为true。
	 * @throws Exception
	 */
	public void load(Reader in, boolean reload) throws Exception {
		Map<String, Object> tmpStore = new LinkedHashMap<String, Object>();
		Map<String, String> tmpStrStore = new LinkedHashMap<String, String>();
		
		PropertiesReader reader = new PropertiesReader(in, defaultListDelimiter);
		try {
			while (reader.nextProperty()) {
				String key = reader.getPropertyName();
				String value = reader.getPropertyValue();
				tmpStrStore.put(key, value);
				propertyLoaded(key, value, tmpStore);
				if(reload) {
					String oldValue = strStore.remove(key);
					if(oldValue == null)
						fireEvent(EventType.ADD, key, value);
					else if(!oldValue.equals(value)) 
						fireEvent(EventType.UPDATE, key, value);
				}
			}
			
			if(reload) {
				for(String key : strStore.keySet()) {
					fireEvent(EventType.CLEAR, key, tmpStrStore.get(key));
				}
			}
		} catch (IOException ioex) {
			throw new Exception(ioex);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				;
			}
		}
		
		if(store != null)
			store.clear();
		
		if(strStore != null)
			strStore.clear();
		
		store = tmpStore;
		strStore = tmpStrStore;
	}
	
	private void propertyLoaded(String key, String value, Map<String, Object> tmpStore) {
		Iterator<?> it = PropertyConverter.toIterator(value, defaultListDelimiter);
		while (it.hasNext()) {
			addPropertyDirect(key, it.next(), tmpStore);
		}
	}

	private void addPropertyDirect(String key, Object value, Map<String, Object> tmpStore) {
		Object previousValue = tmpStore.get(key);

		if (previousValue == null) {
			tmpStore.put(key, value);
		} else if (previousValue instanceof List) {
			@SuppressWarnings("unchecked")
			List<Object> valueList = (List<Object>) previousValue;
			valueList.add(value);
		} else {
			List<Object> list = new ArrayList<Object>();
			list.add(previousValue);
			list.add(value);

			tmpStore.put(key, list);
		}
	}

	// --------------------------------------------------------------------
	
	private Object getProperty(String key) {
		return store.get(key);
	}
	
	public Properties getProperties() {
		Properties properties = new Properties();
		
		for(String key : store.keySet()) {
			properties.setProperty(key, getString(key));
		}
		return null;
	}

	public boolean getBoolean(String key) {
		Boolean b = getBoolean(key, null);
		if (b != null) {
			return b.booleanValue();
		} else {
			throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
		}
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		return getBoolean(key, BooleanUtils.toBooleanObject(defaultValue))
				.booleanValue();
	}

	public Boolean getBoolean(String key, Boolean defaultValue) {
		Object value = resolveContainerStore(key);

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
		Byte b = getByte(key, null);
		if (b != null) {
			return b.byteValue();
		} else {
			throw new NoSuchElementException('\'' + key + " doesn't map to an existing object");
		}
	}

	public byte getByte(String key, byte defaultValue) {
		return getByte(key, new Byte(defaultValue)).byteValue();
	}

	public Byte getByte(String key, Byte defaultValue) {
		Object value = resolveContainerStore(key);

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
		Double d = getDouble(key, null);
		if (d != null) {
			return d.doubleValue();
		} else {
			throw new NoSuchElementException('\'' + key
					+ "' doesn't map to an existing object");
		}
	}

	public double getDouble(String key, double defaultValue) {
		return getDouble(key, new Double(defaultValue)).doubleValue();
	}

	public Double getDouble(String key, Double defaultValue) {
		Object value = resolveContainerStore(key);

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
		Float f = getFloat(key, null);
		if (f != null) {
			return f.floatValue();
		} else {
			throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
		}
	}

	public float getFloat(String key, float defaultValue) {
		return getFloat(key, new Float(defaultValue)).floatValue();
	}

	public Float getFloat(String key, Float defaultValue) {
		Object value = resolveContainerStore(key);

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
		Integer i = getInteger(key, null);
		if (i != null) {
			return i.intValue();
		} else {
			throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
		}
	}

	public int getInt(String key, int defaultValue) {
		Integer i = getInteger(key, null);

		if (i == null) {
			return defaultValue;
		}

		return i.intValue();
	}

	public Integer getInteger(String key, Integer defaultValue) {
		Object value = resolveContainerStore(key);

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
		Long l = getLong(key, null);
		if (l != null) {
			return l.longValue();
		} else {
			throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
		}
	}

	public long getLong(String key, long defaultValue) {
		return getLong(key, new Long(defaultValue)).longValue();
	}

	public Long getLong(String key, Long defaultValue) {
		Object value = resolveContainerStore(key);

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
		Short s = getShort(key, null);
		if (s != null) {
			return s.shortValue();
		} else {
			throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
		}
	}

	public short getShort(String key, short defaultValue) {
		return getShort(key, new Short(defaultValue)).shortValue();
	}

	public Short getShort(String key, Short defaultValue) {
		Object value = resolveContainerStore(key);

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
		String s = getString(key, null);
		if (s != null) {
			return s;
		} else {
			return null;
		}
	}

	public String getString(String key, String defaultValue) {
		Object value = resolveContainerStore(key);

		if (value instanceof String) {
			return interpolate((String) value);
		} else {
			return interpolate(defaultValue);
		}
	}

	public String[] getStringArray(String key) {
		Object value = getProperty(key);

		String[] array;

		if (value instanceof String) {
			array = new String[1];

			array[0] = interpolate((String) value);
		} else if (value instanceof List) {
			List<?> list = (List<?>) value;
			array = new String[list.size()];

			for (int i = 0; i < array.length; i++) {
				array[i] = interpolate(ObjectUtils.toString(list.get(i), null));
			}
		} else {
			array = new String[0];
		} 
		
		return array;
	}

	public List<Object> getList(String key) {
		return getList(key, new ArrayList<Object>());
	}

	public List<Object> getList(String key, List<Object> defaultValue) {
		Object value = getProperty(key);
		List<Object> list;

		if (value instanceof String) {
			list = new ArrayList<Object>(1);
			list.add(interpolate((String) value));
		} else if (value instanceof List) {
			list = new ArrayList<Object>();
			List<?> l = (List<?>) value;

			// add the interpolated elements in the new list
			for (Object elem : l) {
				list.add(interpolate(elem));
			}
		} else {
			list = defaultValue;
		}
		return list;
	}

	protected Object resolveContainerStore(String key) {
		Object value = getProperty(key);
		if (value != null) {
			if (value instanceof Collection) {
				Collection<?> collection = (Collection<?>) value;
				value = collection.isEmpty() ? null : collection.iterator()
						.next();
			} else if (value.getClass().isArray() && Array.getLength(value) > 0) {
				value = Array.get(value, 0);
			}
		}

		return value;
	}

	protected String interpolate(String base) {
		Object result = interpolate((Object) base);
		return (result == null) ? null : result.toString();
	}

	protected Object interpolate(Object value) {
		// TODO 待完善
		return value;
	}
	
}
