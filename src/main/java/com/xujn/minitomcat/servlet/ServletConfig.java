package com.xujn.minitomcat.servlet;

/**
 * Exposes immutable servlet definition metadata to a servlet instance.
 */
public interface ServletConfig {

    String getServletName();

    String getInitParameter(String name);

    ServletContext getServletContext();
}
