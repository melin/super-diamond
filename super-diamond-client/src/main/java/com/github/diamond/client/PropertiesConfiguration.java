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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.PropertyConverter;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.ObjectUtils;

import com.github.diamond.client.config.PropertiesReader;
import com.github.diamond.client.netty.ClientChannelInitializer;
import com.github.diamond.client.netty.NamedThreadFactory;
import com.github.diamond.client.netty.Netty4Client;

/**
 * Create on @2013-8-25 @下午1:17:38
 * 
 * @author bsli@ustcinfo.com
 */
public class PropertiesConfiguration {

	private static char defaultListDelimiter = ',';

	private Map<String, Object> store = new LinkedHashMap<String, Object>();
	
	private Netty4Client client;
	
	private volatile boolean reloadable = true;
	
	private static final ExecutorService reloadExecutorService = Executors.newSingleThreadExecutor(new NamedThreadFactory("ReloadConfigExecutorService", true));

	public PropertiesConfiguration(String host, int port, String projCode, String profile) {
		final String clientMsg = "superdiamond," + projCode + "," + profile;
		try {
			client = new Netty4Client(host, port, new ClientChannelInitializer());
			
			if(client.isConnected()) {
				client.sendMessage(clientMsg);
				String message = client.receiveMessage();
				if(message != null)
					load(new StringReader(message));
			} else {
				throw new RuntimeException("连接失败");
			}
			
			reloadExecutorService.submit(new Runnable() {
				
				@Override
				public void run() {
					while(reloadable) {
						try {
							if(client.isConnected()) {
								String message = client.receiveMessage();
								if(message != null)
									load(new StringReader(message));
							} else {
								TimeUnit.SECONDS.sleep(1);
							}
						} catch(Exception e) {
							
						}
					}
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	public void close() {
		reloadable = false;
		
		if(client != null && client.isConnected())
			client.close();
	}

	public void load(Reader in) throws Exception {
		store.clear();
		
		PropertiesReader reader = new PropertiesReader(in, defaultListDelimiter);
		try {
			while (reader.nextProperty()) {
				propertyLoaded(reader.getPropertyName(),
						reader.getPropertyValue());
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
	}

	private void propertyLoaded(String key, String value) {
		Iterator<?> it = PropertyConverter.toIterator(value,
				defaultListDelimiter);
		while (it.hasNext()) {
			addPropertyDirect(key, it.next());
		}
	}

	private void addPropertyDirect(String key, Object value) {
		synchronized (PropertiesConfiguration.class) {
			Object previousValue = getProperty(key);

			if (previousValue == null) {
				store.put(key, value);
			} else if (previousValue instanceof List) {
				@SuppressWarnings("unchecked")
				List<Object> valueList = (List<Object>) previousValue;
				valueList.add(value);
			} else {
				List<Object> list = new ArrayList<Object>();
				list.add(previousValue);
				list.add(value);

				store.put(key, list);
			}
		}
	}

	public Object getProperty(String key) {
		return store.get(key);
	}

	// --------------------------------------------------------------------

	public boolean getBoolean(String key) {
		Boolean b = getBoolean(key, null);
		if (b != null) {
			return b.booleanValue();
		} else {
			throw new NoSuchElementException('\'' + key
					+ "' doesn't map to an existing object");
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
				throw new ConversionException('\'' + key
						+ "' doesn't map to a Boolean object", e);
			}
		}
	}

	public byte getByte(String key) {
		Byte b = getByte(key, null);
		if (b != null) {
			return b.byteValue();
		} else {
			throw new NoSuchElementException('\'' + key
					+ " doesn't map to an existing object");
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
				throw new ConversionException('\'' + key
						+ "' doesn't map to a Byte object", e);
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
				throw new ConversionException('\'' + key
						+ "' doesn't map to a Double object", e);
			}
		}
	}

	public float getFloat(String key) {
		Float f = getFloat(key, null);
		if (f != null) {
			return f.floatValue();
		} else {
			throw new NoSuchElementException('\'' + key
					+ "' doesn't map to an existing object");
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
				throw new ConversionException('\'' + key
						+ "' doesn't map to a Float object", e);
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
				throw new ConversionException('\'' + key
						+ "' doesn't map to an Integer object", e);
			}
		}
	}

	public long getLong(String key) {
		Long l = getLong(key, null);
		if (l != null) {
			return l.longValue();
		} else {
			throw new NoSuchElementException('\'' + key
					+ "' doesn't map to an existing object");
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
				throw new ConversionException('\'' + key
						+ "' doesn't map to a Long object", e);
			}
		}
	}

	public short getShort(String key) {
		Short s = getShort(key, null);
		if (s != null) {
			return s.shortValue();
		} else {
			throw new NoSuchElementException('\'' + key
					+ "' doesn't map to an existing object");
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
				throw new ConversionException('\'' + key
						+ "' doesn't map to a Short object", e);
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
