/**
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.
 */

package com.github.diamond.client.event;

import com.github.diamond.client.util.NamedThreadFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Create on @2013-8-28 @下午9:25:08
 *
 * @author bsli@ustcinfo.com
 */
public class EventSource {
    private Collection<ConfigurationListener> listeners;

    private ExecutorService executorService =
            Executors.newSingleThreadExecutor(new NamedThreadFactory("config-event"));

    public EventSource() {
        initListeners();
    }

    public void addConfigurationListener(ConfigurationListener configurationListener) {
        checkListener(configurationListener);
        listeners.add(configurationListener);
    }

    public boolean removeConfigurationListener(ConfigurationListener configurationListener) {
        return listeners.remove(configurationListener);
    }

    public Collection<ConfigurationListener> getConfigurationListeners() {
        return Collections
                .unmodifiableCollection(new ArrayList<ConfigurationListener>(
                        listeners));
    }

    public void clearConfigurationListeners() {
        listeners.clear();
    }

    /**
     * 异步执行ConfigurationListener.
     *
     * @param type
     * @param propName
     * @param propValue
     */
    protected void fireEvent(EventType type, String propName, Object propValue) {
        final Iterator<ConfigurationListener> it = listeners.iterator();
        if (it.hasNext()) {
            final ConfigurationEvent event = createEvent(type, propName, propValue);
            while (it.hasNext()) {
                final ConfigurationListener listener = it.next();
                executorService.submit(new Runnable() {

                    @Override
                    public void run() {
                        /*//修复并发时线程等待导致while循环长时间不能结束
						it.next().configurationChanged(event);*/
                        listener.configurationChanged(event);
                    }
                });
            }
        }
    }

    protected ConfigurationEvent createEvent(EventType type, String propName, Object propValue) {
        return new ConfigurationEvent(this, type, propName, propValue);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        EventSource copy = (EventSource) super.clone();
        copy.initListeners();
        return copy;
    }

    private static void checkListener(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Listener must not be null!");
        }
    }

    private void initListeners() {
        listeners = new CopyOnWriteArrayList<ConfigurationListener>();
    }
}
