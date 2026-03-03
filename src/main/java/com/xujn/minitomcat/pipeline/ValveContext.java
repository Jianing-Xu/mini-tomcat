package com.xujn.minitomcat.pipeline;

import com.xujn.minitomcat.connector.HttpRequest;
import com.xujn.minitomcat.connector.HttpResponse;

/**
 * Provides sequential valve invocation when the pipeline becomes active in later phases.
 */
public interface ValveContext {

    void invokeNext(HttpRequest request, HttpResponse response);
}
