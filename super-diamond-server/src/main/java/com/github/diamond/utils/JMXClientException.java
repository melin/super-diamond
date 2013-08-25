package com.github.diamond.utils;

public class JMXClientException extends Exception {

    private static final long serialVersionUID = -7410016800727397507L;


    public JMXClientException(String message) {
        super(message);
    }


    public JMXClientException(Throwable cause) {
        super(cause);
    }


    public JMXClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
