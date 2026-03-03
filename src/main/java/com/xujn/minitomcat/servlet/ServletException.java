package com.xujn.minitomcat.servlet;

/**
 * Signals a servlet level failure to the container.
 */
public class ServletException extends Exception {

    public ServletException(String message) {
        super(message);
    }

    public ServletException(String message, Throwable cause) {
        super(message, cause);
    }
}
