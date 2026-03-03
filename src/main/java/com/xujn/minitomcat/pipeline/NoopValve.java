package com.xujn.minitomcat.pipeline;

import com.xujn.minitomcat.connector.HttpRequest;
import com.xujn.minitomcat.connector.HttpResponse;

/**
 * No-op valve used to keep the pipeline graph present without affecting Phase 1 execution.
 */
public class NoopValve implements Valve {

    @Override
    public void invoke(HttpRequest request, HttpResponse response, ValveContext context) {
        // Phase 1 intentionally leaves the pipeline inactive.
    }
}
