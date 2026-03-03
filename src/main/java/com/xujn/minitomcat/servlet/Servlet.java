package com.xujn.minitomcat.servlet;

import com.xujn.minitomcat.connector.HttpRequest;
import com.xujn.minitomcat.connector.HttpResponse;

/**
 * Minimal servlet contract equivalent to the Phase 1 design.
 *
 * <p>Thread safety assumption: a single servlet instance may serve multiple requests,
 * so implementations must be thread-safe or stateless.</p>
 */
public interface Servlet {

    void init(ServletConfig config) throws ServletException;

    void service(HttpRequest request, HttpResponse response) throws ServletException;

    void destroy();
}
