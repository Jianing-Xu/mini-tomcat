package com.xujn.minitomcat.bootstrap;

import com.xujn.minitomcat.connector.Connector;
import com.xujn.minitomcat.container.Engine;
import com.xujn.minitomcat.mapper.MappingRegistry;

/**
 * Owns the assembled connector and container tree for a runnable mini-tomcat instance.
 *
 * <p>Thread safety assumption: lifecycle is managed by a single bootstrap thread.</p>
 */
public class MiniTomcat extends LifecycleBase {

    private final Connector connector;
    private final Engine engine;
    private final MappingRegistry mappingRegistry;
    private final WebServerConfig serverConfig;

    public MiniTomcat(Connector connector, Engine engine, MappingRegistry mappingRegistry, WebServerConfig serverConfig) {
        this.connector = connector;
        this.engine = engine;
        this.mappingRegistry = mappingRegistry;
        this.serverConfig = serverConfig;
    }

    public Connector getConnector() {
        return connector;
    }

    public Engine getEngine() {
        return engine;
    }

    public WebServerConfig getServerConfig() {
        return serverConfig;
    }

    @Override
    protected void startInternal() {
        engine.start();
        connector.start();
    }

    @Override
    protected void stopInternal() {
        connector.stop();
        engine.stop();
        mappingRegistry.clear();
    }
}
