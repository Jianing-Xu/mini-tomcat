package com.xujn.minitomcat.bootstrap;

import com.xujn.minitomcat.connector.Connector;
import com.xujn.minitomcat.connector.bio.BioConnector;
import com.xujn.minitomcat.connector.bio.BioConnectorConfig;
import com.xujn.minitomcat.container.standard.StandardContext;
import com.xujn.minitomcat.container.standard.StandardEngine;
import com.xujn.minitomcat.container.standard.StandardHost;
import com.xujn.minitomcat.container.standard.StandardWrapper;
import com.xujn.minitomcat.deploy.ServerDefinition;
import com.xujn.minitomcat.deploy.ServletDefinition;
import com.xujn.minitomcat.deploy.WebAppDefinition;
import com.xujn.minitomcat.deploy.WebXmlParser;
import com.xujn.minitomcat.mapper.MappingRegistry;
import com.xujn.minitomcat.mapper.SimpleMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Builds a complete Phase 1 server graph from configuration files.
 */
public class MiniTomcatBuilder {

    private final WebXmlParser webXmlParser = new WebXmlParser();

    public MiniTomcat build(Path serverPropertiesPath) {
        WebServerConfig serverConfig = loadServerConfig(serverPropertiesPath);
        WebAppDefinition webAppDefinition = webXmlParser.parse(serverConfig.webXmlPath());
        ServerDefinition serverDefinition = new ServerDefinition(serverConfig, webAppDefinition);

        MappingRegistry mappingRegistry = new MappingRegistry(serverDefinition.serverConfig().host());
        StandardEngine engine = new StandardEngine("engine", serverDefinition.serverConfig().host());
        StandardHost host = new StandardHost(serverDefinition.serverConfig().host());
        StandardContext context = new StandardContext(serverDefinition.serverConfig().contextPath());

        engine.addChild(host);
        host.addChild(context);

        for (ServletDefinition servletDefinition : webAppDefinition.getServletDefinitions()) {
            StandardWrapper wrapper = new StandardWrapper(servletDefinition, context.getServletContextInternal());
            context.addChild(wrapper);
        }

        mappingRegistry.registerHost(host);
        mappingRegistry.registerContext(host, context);
        for (ServletDefinition servletDefinition : webAppDefinition.getServletDefinitions()) {
            mappingRegistry.registerWrapper(context, servletDefinition);
        }

        SimpleMapper mapper = new SimpleMapper(mappingRegistry);
        engine.setMapper(mapper);
        host.setMapper(mapper);
        context.setMapper(mapper);

        BioConnectorConfig connectorConfig = new BioConnectorConfig(
                serverDefinition.serverConfig().port(),
                serverDefinition.serverConfig().backlog(),
                serverDefinition.serverConfig().workerThreads()
        );
        Connector connector = new BioConnector(connectorConfig);
        connector.setContainer(engine);
        return new MiniTomcat(connector, engine, mappingRegistry, serverDefinition.serverConfig());
    }

    private WebServerConfig loadServerConfig(Path serverPropertiesPath) {
        Path normalizedServerPropertiesPath = serverPropertiesPath.toAbsolutePath().normalize();
        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(normalizedServerPropertiesPath)) {
            properties.load(inputStream);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to load server properties from " + normalizedServerPropertiesPath, ex);
        }

        String host = properties.getProperty("server.host", "localhost");
        int port = Integer.parseInt(properties.getProperty("server.port", "8080"));
        String contextPath = properties.getProperty("server.contextPath", "/app");
        int workerThreads = Integer.parseInt(properties.getProperty("server.workerThreads", "8"));
        int backlog = Integer.parseInt(properties.getProperty("server.backlog", "50"));
        Path rawWebXmlPath = Path.of(properties.getProperty("server.webXml"));
        Path webXmlPath = rawWebXmlPath.isAbsolute()
                ? rawWebXmlPath.normalize()
                : normalizedServerPropertiesPath.getParent().resolve(rawWebXmlPath).normalize();

        return new WebServerConfig(port, host, contextPath, workerThreads, backlog, webXmlPath);
    }
}
