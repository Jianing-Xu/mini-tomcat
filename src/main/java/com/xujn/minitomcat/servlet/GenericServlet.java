package com.xujn.minitomcat.servlet;

/**
 * Convenience servlet base class that stores the assigned configuration.
 */
public abstract class GenericServlet implements Servlet {

    private ServletConfig servletConfig;

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.servletConfig = config;
        init();
    }

    public void init() throws ServletException {
        // default no-op
    }

    public ServletConfig getServletConfig() {
        return servletConfig;
    }

    public ServletContext getServletContext() {
        return servletConfig.getServletContext();
    }

    @Override
    public void destroy() {
        // default no-op
    }
}
