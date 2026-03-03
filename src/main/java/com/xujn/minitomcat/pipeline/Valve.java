package com.xujn.minitomcat.pipeline;

import com.xujn.minitomcat.connector.HttpRequest;
import com.xujn.minitomcat.connector.HttpResponse;

/**
 * Reserved valve abstraction. Phase 1 keeps it as a structural extension point only.
 */
public interface Valve {

    void invoke(HttpRequest request, HttpResponse response, ValveContext context);
}
