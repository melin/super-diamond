/**        
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.    
 */

package com.github.diamond.client.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Create on @2013-8-24 @下午9:55:54
 * 
 * @author bsli@ustcinfo.com
 */
public class NamedThreadFactory implements ThreadFactory {

	private static final AtomicInteger POOL_SEQ = new AtomicInteger(1);

	private final AtomicInteger mThreadNum = new AtomicInteger(1);

	private final String mPrefix;

	private final boolean mDaemo;

	private final ThreadGroup mGroup;

	public NamedThreadFactory() {
		this("pool-" + POOL_SEQ.getAndIncrement(), false);
	}

	public NamedThreadFactory(String prefix) {
		this(prefix, false);
	}

	public NamedThreadFactory(String prefix, boolean daemo) {
		mPrefix = prefix + "-thread-";
		mDaemo = daemo;
		SecurityManager s = System.getSecurityManager();
		mGroup = (s == null) ? Thread.currentThread().getThreadGroup() : s
				.getThreadGroup();
	}

	public Thread newThread(Runnable runnable) {
		String name = mPrefix + mThreadNum.getAndIncrement();
		Thread ret = new Thread(mGroup, runnable, name, 0);
		ret.setDaemon(mDaemo);
		return ret;
	}

	public ThreadGroup getThreadGroup() {
		return mGroup;
	}
}