package com.xujn.minitomcat.servlet;

/**
 * Minimal application scope storage for a deployed context.
 */
public interface ServletContext {

    String getContextPath();

    Object getAttribute(String name);

    void setAttribute(String name, Object value);
}
