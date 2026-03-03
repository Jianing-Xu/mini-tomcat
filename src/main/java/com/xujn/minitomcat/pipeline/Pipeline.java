package com.xujn.minitomcat.pipeline;

import com.xujn.minitomcat.connector.HttpRequest;
import com.xujn.minitomcat.connector.HttpResponse;

/**
 * Reserved pipeline abstraction for future Phase 2 valve integration.
 */
public interface Pipeline {

    void addValve(Valve valve);

    Valve[] getValves();

    Valve getBasic();

    void setBasic(Valve valve);

    void invoke(HttpRequest request, HttpResponse response);
}
