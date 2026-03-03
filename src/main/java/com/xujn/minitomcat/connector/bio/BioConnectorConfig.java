package com.xujn.minitomcat.connector.bio;

/**
 * Immutable SIMPLE_BIO connector settings.
 */
public record BioConnectorConfig(int port, int backlog, int workerThreads) {
}
