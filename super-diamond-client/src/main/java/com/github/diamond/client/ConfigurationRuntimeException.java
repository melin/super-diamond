/**
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.
 */

package com.github.diamond.client;

import org.apache.commons.lang.exception.NestableRuntimeException;

/**
 * Create on @2013-9-1 @下午9:21:17
 *
 * @author bsli@ustcinfo.com
 */
public class ConfigurationRuntimeException extends NestableRuntimeException {
    private static final long serialVersionUID = -7838702245512140996L;

    public ConfigurationRuntimeException() {
        super();
    }

    public ConfigurationRuntimeException(String message) {
        super(message);
    }

    public ConfigurationRuntimeException(Throwable cause) {
        super(cause);
    }

    public ConfigurationRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}