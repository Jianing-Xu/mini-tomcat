package com.xujn.minitomcat.container.standard;

import com.xujn.minitomcat.connector.HttpRequest;
import com.xujn.minitomcat.connector.HttpResponse;
import com.xujn.minitomcat.container.ContainerBase;
import com.xujn.minitomcat.container.Engine;
import com.xujn.minitomcat.container.Host;
import com.xujn.minitomcat.mapper.Mapper;
import com.xujn.minitomcat.support.http.HttpStatus;

/**
 * Standard Phase 1 Engine implementation.
 *
 * <p>Key constraint: it only chooses the Host and never performs application-level routing.
 * Thread safety assumption: mapper and child structure are immutable after startup.</p>
 */
public class StandardEngine extends ContainerBase implements Engine {

    private final String defaultHost;
    private Mapper mapper;

    public StandardEngine(String name, String defaultHost) {
        super(name);
        this.defaultHost = defaultHost;
    }

    @Override
    public String getDefaultHost() {
        return defaultHost;
    }

    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void invoke(HttpRequest request, HttpResponse response) {
        Host host = mapper.mapHost(request.getHost());
        if (host == null) {
            response.sendError(HttpStatus.NOT_FOUND,
                    "No host matched for host=" + request.getHost() + " requestUri=" + request.getRequestUri());
            return;
        }
        host.invoke(request, response);
    }
}
