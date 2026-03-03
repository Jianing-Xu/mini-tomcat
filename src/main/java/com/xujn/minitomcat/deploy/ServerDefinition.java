package com.xujn.minitomcat.deploy;

import com.xujn.minitomcat.bootstrap.WebServerConfig;

/**
 * Couples server runtime configuration with the parsed web application definition.
 */
public record ServerDefinition(WebServerConfig serverConfig, WebAppDefinition webAppDefinition) {
}
