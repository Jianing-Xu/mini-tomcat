package com.xujn.minitomcat.container.standard;

import com.xujn.minitomcat.connector.HttpRequest;
import com.xujn.minitomcat.connector.HttpResponse;
import com.xujn.minitomcat.container.ContainerBase;
import com.xujn.minitomcat.container.Context;
import com.xujn.minitomcat.container.Host;
import com.xujn.minitomcat.mapper.Mapper;
import com.xujn.minitomcat.support.http.HttpStatus;

/**
 * Standard Phase 1 Host implementation.
 *
 * <p>Key constraint: it only chooses the Context inside the selected virtual host.
 * Thread safety assumption: aliases and mapper are immutable after startup.</p>
 */
public class StandardHost extends ContainerBase implements Host {

    private final String[] aliases;
    private Mapper mapper;

    public StandardHost(String name) {
        this(name, new String[0]);
    }

    public StandardHost(String name, String[] aliases) {
        super(name);
        this.aliases = aliases;
    }

    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public String[] getAliases() {
        return aliases;
    }

    @Override
    public void invoke(HttpRequest request, HttpResponse response) {
        Context context = mapper.mapContext(this, request.getRequestUri());
        if (context == null) {
            response.sendError(HttpStatus.NOT_FOUND,
                    "No context matched for host=" + request.getHost() + " requestUri=" + request.getRequestUri());
            return;
        }
        request.setContextPath(context.getPath());
        context.invoke(request, response);
    }
}
