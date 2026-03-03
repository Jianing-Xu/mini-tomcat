package com.xujn.minitomcat.container;

import com.xujn.minitomcat.bootstrap.Lifecycle;
import com.xujn.minitomcat.connector.HttpRequest;
import com.xujn.minitomcat.connector.HttpResponse;
import com.xujn.minitomcat.pipeline.Pipeline;

/**
 * Common contract for all container nodes in the Phase 1 hierarchy.
 *
 * <p>Key constraint: runtime dispatch flows only downward through the container tree.
 * Thread safety assumption: child structure is immutable after startup.</p>
 */
public interface Container extends Lifecycle {

    String getName();

    Container getParent();

    void setParent(Container parent);

    void addChild(Container child);

    Container findChild(String name);

    Pipeline getPipeline();

    void invoke(HttpRequest request, HttpResponse response);
}
