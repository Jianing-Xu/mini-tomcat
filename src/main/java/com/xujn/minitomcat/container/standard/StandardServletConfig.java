package com.xujn.minitomcat.container.standard;

import com.xujn.minitomcat.deploy.ServletDefinition;
import com.xujn.minitomcat.servlet.ServletConfig;
import com.xujn.minitomcat.servlet.ServletContext;

/**
 * Immutable ServletConfig implementation built from a ServletDefinition.
 */
public class StandardServletConfig implements ServletConfig {

    private final ServletDefinition servletDefinition;
    private final ServletContext servletContext;

    public StandardServletConfig(ServletDefinition servletDefinition, ServletContext servletContext) {
        this.servletDefinition = servletDefinition;
        this.servletContext = servletContext;
    }

    @Override
    public String getServletName() {
        return servletDefinition.servletName();
    }

    @Override
    public String getInitParameter(String name) {
        return servletDefinition.initParameters().get(name);
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }
}
