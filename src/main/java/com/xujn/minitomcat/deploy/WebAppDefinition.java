package com.xujn.minitomcat.deploy;

import java.util.List;

/**
 * Represents the fully parsed servlet deployment metadata for one Context.
 *
 * <p>Thread safety assumption: immutable after parsing completes.</p>
 */
public record WebAppDefinition(List<ServletDefinition> servletDefinitions) {

    public List<ServletDefinition> getServletDefinitions() {
        return servletDefinitions;
    }
}
