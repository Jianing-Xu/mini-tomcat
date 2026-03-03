package com.xujn.minitomcat.servlet;

import com.xujn.minitomcat.connector.HttpRequest;
import com.xujn.minitomcat.connector.HttpResponse;
import com.xujn.minitomcat.support.http.HttpStatus;

/**
 * Minimal HTTP servlet helper for examples and Phase 1 request dispatching.
 */
public abstract class HttpServlet extends GenericServlet {

    @Override
    public void service(HttpRequest request, HttpResponse response) throws ServletException {
        String method = request.getMethod();
        if ("GET".equalsIgnoreCase(method)) {
            doGet(request, response);
            return;
        }
        if ("POST".equalsIgnoreCase(method)) {
            doPost(request, response);
            return;
        }
        response.sendError(HttpStatus.METHOD_NOT_ALLOWED, "Unsupported method " + method);
    }

    protected void doGet(HttpRequest request, HttpResponse response) throws ServletException {
        response.sendError(HttpStatus.METHOD_NOT_ALLOWED, "GET not implemented");
    }

    protected void doPost(HttpRequest request, HttpResponse response) throws ServletException {
        response.sendError(HttpStatus.METHOD_NOT_ALLOWED, "POST not implemented");
    }
}
