package com.xujn.minitomcat.mapper;

import com.xujn.minitomcat.container.Context;
import com.xujn.minitomcat.container.Host;
import com.xujn.minitomcat.container.Wrapper;
import com.xujn.minitomcat.container.standard.StandardContext;
import com.xujn.minitomcat.container.standard.StandardHost;
import com.xujn.minitomcat.container.standard.StandardWrapper;
import com.xujn.minitomcat.deploy.ServletDefinition;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SimpleMapperTest {

    @Test
    void mapsHostContextAndWrapperWithPriorityRules() {
        StandardHost host = new StandardHost("localhost");
        StandardContext rootContext = new StandardContext("/");
        StandardContext appContext = new StandardContext("/app");
        StandardContext adminContext = new StandardContext("/app/admin");
        host.addChild(rootContext);
        host.addChild(appContext);
        host.addChild(adminContext);

        MappingRegistry registry = new MappingRegistry("localhost");
        registry.registerHost(host);
        registry.registerContext(host, rootContext);
        registry.registerContext(host, appContext);
        registry.registerContext(host, adminContext);

        StandardWrapper exact = wrapper("/demo", "demoServlet", appContext);
        StandardWrapper path = wrapper("/api/*", "apiServlet", appContext);
        StandardWrapper defaultWrapper = wrapper("/", "defaultServlet", appContext);
        appContext.addChild(exact);
        appContext.addChild(path);
        appContext.addChild(defaultWrapper);
        registry.registerWrapper(appContext, servletDefinition("demoServlet", "/demo"));
        registry.registerWrapper(appContext, servletDefinition("apiServlet", "/api/*"));
        registry.registerWrapper(appContext, servletDefinition("defaultServlet", "/"));

        StandardWrapper adminWrapper = wrapper("/panel", "adminServlet", adminContext);
        adminContext.addChild(adminWrapper);
        registry.registerWrapper(adminContext, servletDefinition("adminServlet", "/panel"));

        SimpleMapper mapper = new SimpleMapper(registry);
        Host mappedHost = mapper.mapHost("localhost:8080");
        assertEquals("localhost", mappedHost.getName());

        Context mappedContext = mapper.mapContext(host, "/app/admin/panel");
        assertEquals("/app/admin", mappedContext.getPath());

        Wrapper exactMapped = mapper.mapWrapper(appContext, "/app/demo");
        assertEquals("demoServlet", exactMapped.getName());

        Wrapper pathMapped = mapper.mapWrapper(appContext, "/app/api/users");
        assertEquals("apiServlet", pathMapped.getName());

        Wrapper defaultMapped = mapper.mapWrapper(appContext, "/app/other");
        assertEquals("defaultServlet", defaultMapped.getName());

        assertNull(mapper.mapWrapper(appContext, "/different"));
    }

    private StandardWrapper wrapper(String pattern, String name, StandardContext context) {
        return new StandardWrapper(servletDefinition(name, pattern), context.getServletContextInternal());
    }

    private ServletDefinition servletDefinition(String name, String pattern) {
        return new ServletDefinition(name, "examples.phase1basic.DemoServlet", Map.of(), List.of(pattern));
    }
}
