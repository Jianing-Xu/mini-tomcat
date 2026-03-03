package com.xujn.minitomcat.bootstrap;

import java.nio.file.Path;

/**
 * Holds the runtime configuration needed to assemble the Phase 1 server.
 */
public record WebServerConfig(
        int port,
        String host,
        String contextPath,
        int workerThreads,
        int backlog,
        Path webXmlPath
) {
}
