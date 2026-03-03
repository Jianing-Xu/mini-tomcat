package com.xujn.minitomcat.container.standard;

import com.xujn.minitomcat.connector.HttpRequest;
import com.xujn.minitomcat.connector.HttpResponse;
import com.xujn.minitomcat.container.ContainerBase;
import com.xujn.minitomcat.container.Context;
import com.xujn.minitomcat.container.Wrapper;
import com.xujn.minitomcat.mapper.Mapper;
import com.xujn.minitomcat.support.http.HttpStatus;

/**
 * Standard Phase 1 Context implementation.
 *
 * <p>Key constraint: it owns servlet mappings for one application path and delegates
 * only to Wrappers.
 * Thread safety assumption: servlet context attributes are concurrent, child wrappers are read-only after startup.</p>
 */
public class StandardContext extends ContainerBase implements Context {

    private final String path;
    private final StandardServletContext servletContext;
    private Mapper mapper;

    public StandardContext(String path) {
        super(path);
        this.path = normalizeContextPath(path);
        this.servletContext = new StandardServletContext(this.path);
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Mapper getMapper() {
        return mapper;
    }

    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    public StandardServletContext getServletContextInternal() {
        return servletContext;
    }

    @Override
    public void invoke(HttpRequest request, HttpResponse response) {
        Wrapper wrapper = mapper.mapWrapper(this, request.getRequestUri());
        if (wrapper == null) {
            response.sendError(HttpStatus.NOT_FOUND,
                    "No servlet matched for host=" + request.getHost()
                            + " context=" + path
                            + " requestUri=" + request.getRequestUri());
            return;
        }
        request.setServletPath(extractServletPath(request.getRequestUri()));
        wrapper.invoke(request, response);
    }

    @Override
    protected void destroyInternal() {
        servletContext.clear();
        super.destroyInternal();
    }

    private String extractServletPath(String requestUri) {
        if ("/".equals(path)) {
            return requestUri;
        }
        String relativePath = requestUri.substring(path.length());
        return relativePath.isEmpty() ? "/" : relativePath;
    }

    private String normalizeContextPath(String value) {
        if (value == null || value.isBlank() || "/".equals(value)) {
            return "/";
        }
        String normalized = value.startsWith("/") ? value : "/" + value;
        return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }
}
