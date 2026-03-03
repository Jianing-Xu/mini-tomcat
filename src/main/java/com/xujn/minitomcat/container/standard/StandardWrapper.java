package com.xujn.minitomcat.container.standard;

import com.xujn.minitomcat.connector.HttpRequest;
import com.xujn.minitomcat.connector.HttpResponse;
import com.xujn.minitomcat.container.ContainerBase;
import com.xujn.minitomcat.container.Wrapper;
import com.xujn.minitomcat.deploy.ServletDefinition;
import com.xujn.minitomcat.servlet.Servlet;
import com.xujn.minitomcat.servlet.ServletException;
import com.xujn.minitomcat.support.ExceptionUtils;
import com.xujn.minitomcat.support.http.HttpStatus;

/**
 * Standard Phase 1 Wrapper implementation.
 *
 * <p>Key constraint: one Wrapper owns exactly one servlet instance.
 * Thread safety assumption: multiple request threads may race to first-use initialization,
 * so servlet creation is guarded by a dedicated monitor.</p>
 */
public class StandardWrapper extends ContainerBase implements Wrapper {

    private final ServletDefinition servletDefinition;
    private final StandardServletContext servletContext;
    private final Object lifecycleMonitor = new Object();
    private volatile Servlet servletInstance;
    private volatile boolean initialized;

    public StandardWrapper(ServletDefinition servletDefinition, StandardServletContext servletContext) {
        super(servletDefinition.servletName());
        this.servletDefinition = servletDefinition;
        this.servletContext = servletContext;
    }

    @Override
    public String getServletName() {
        return servletDefinition.servletName();
    }

    @Override
    public Servlet allocate() {
        if (initialized && servletInstance != null) {
            return servletInstance;
        }
        synchronized (lifecycleMonitor) {
            if (initialized && servletInstance != null) {
                return servletInstance;
            }
            Servlet servlet = instantiateServlet();
            try {
                // Wrapper controls the only init transition to keep lifecycle deterministic.
                servlet.init(new StandardServletConfig(servletDefinition, servletContext));
            } catch (ServletException ex) {
                throw new IllegalStateException("Servlet init failed for servlet=" + servletDefinition.servletName()
                        + " cause=" + ExceptionUtils.rootCauseMessage(ex), ex);
            }
            servletInstance = servlet;
            initialized = true;
            return servletInstance;
        }
    }

    @Override
    public void deallocate(Servlet servlet) {
        // Phase 1 uses a single shared servlet instance, so there is nothing to release per request.
    }

    @Override
    public void invoke(HttpRequest request, HttpResponse response) {
        Servlet servlet = allocate();
        try {
            servlet.service(request, response);
        } catch (ServletException ex) {
            if (response.isCommitted()) {
                System.err.println("Committed response preserved for servlet=" + servletDefinition.servletName()
                        + " host=" + request.getHost()
                        + " context=" + request.getContextPath()
                        + " requestUri=" + request.getRequestUri()
                        + " cause=" + ExceptionUtils.rootCauseMessage(ex));
                return;
            }
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Servlet invocation failed for servlet=" + servletDefinition.servletName()
                            + " host=" + request.getHost()
                            + " context=" + request.getContextPath()
                            + " requestUri=" + request.getRequestUri()
                            + " cause=" + ExceptionUtils.rootCauseMessage(ex));
        }
    }

    @Override
    protected void destroyInternal() {
        if (initialized && servletInstance != null) {
            servletInstance.destroy();
        }
    }

    private Servlet instantiateServlet() {
        try {
            Class<?> servletClass = Class.forName(servletDefinition.servletClass());
            Object instance = servletClass.getDeclaredConstructor().newInstance();
            return (Servlet) instance;
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(
                    "Failed to instantiate servlet class=" + servletDefinition.servletClass()
                            + " servlet=" + servletDefinition.servletName(), ex);
        }
    }
}
