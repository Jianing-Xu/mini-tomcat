package com.xujn.minitomcat.container;

import com.xujn.minitomcat.connector.HttpRequest;
import com.xujn.minitomcat.connector.HttpResponse;
import com.xujn.minitomcat.servlet.Servlet;

/**
 * Terminal container that owns one servlet definition and instance lifecycle.
 */
public interface Wrapper extends Container {

    String getServletName();

    Servlet allocate();

    void deallocate(Servlet servlet);

    void invoke(HttpRequest request, HttpResponse response);
}
