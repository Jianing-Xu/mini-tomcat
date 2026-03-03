package com.xujn.minitomcat.container.standard;

import com.xujn.minitomcat.servlet.ServletContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Backing implementation of the minimal application-scoped ServletContext.
 *
 * <p>Thread safety assumption: attributes may be accessed concurrently by request threads,
 * so storage uses a concurrent map.</p>
 */
public class StandardServletContext implements ServletContext {

    private final String contextPath;
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    public StandardServletContext(String contextPath) {
        this.contextPath = contextPath;
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        if (value == null) {
            attributes.remove(name);
            return;
        }
        attributes.put(name, value);
    }

    public void clear() {
        attributes.clear();
    }
}
