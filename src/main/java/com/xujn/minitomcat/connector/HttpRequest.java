package com.xujn.minitomcat.connector;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal immutable-plus-routing-state request object used by Phase 1 dispatch.
 *
 * <p>Key constraint: protocol parsing must be complete before the request enters the Engine.
 * Thread safety assumption: a request instance is confined to one worker thread.</p>
 */
public class HttpRequest {

    private final String method;
    private final String requestUri;
    private final String protocol;
    private final String host;
    private final Map<String, String> headers;
    private final Map<String, List<String>> parameters;
    private final byte[] body;
    private final Map<String, Object> attributes = new LinkedHashMap<>();
    private String contextPath;
    private String servletPath;

    public HttpRequest(
            String method,
            String requestUri,
            String protocol,
            String host,
            Map<String, String> headers,
            Map<String, List<String>> parameters,
            byte[] body
    ) {
        this.method = method;
        this.requestUri = requestUri;
        this.protocol = protocol;
        this.host = host;
        this.headers = Map.copyOf(headers);
        this.parameters = Map.copyOf(parameters);
        this.body = body == null ? new byte[0] : body.clone();
    }

    public String getMethod() {
        return method;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getHost() {
        return host;
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getParameter(String name) {
        List<String> values = parameters.get(name);
        return values == null || values.isEmpty() ? null : values.get(0);
    }

    public Map<String, List<String>> getParameters() {
        return parameters;
    }

    public byte[] getBody() {
        return body.clone();
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getServletPath() {
        return servletPath;
    }

    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
    }
}
