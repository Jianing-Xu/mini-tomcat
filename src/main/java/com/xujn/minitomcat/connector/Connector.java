package com.xujn.minitomcat.connector;

import com.xujn.minitomcat.bootstrap.Lifecycle;
import com.xujn.minitomcat.container.Container;

/**
 * Entry-point abstraction for accepting requests and handing them to the container tree.
 *
 * <p>Key constraint: Connector never owns routing semantics; it only delegates to the top container.
 * Thread safety assumption: container reference is configured once during bootstrap.</p>
 */
public interface Connector extends Lifecycle {

    void setContainer(Container container);

    Container getContainer();

    void handle(HttpRequest request, HttpResponse response);
}
