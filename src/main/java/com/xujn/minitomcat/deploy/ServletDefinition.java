package com.xujn.minitomcat.deploy;

import java.util.List;
import java.util.Map;

/**
 * Captures one servlet declaration plus its mapped URL patterns from web.xml.
 *
 * <p>Thread safety assumption: immutable after construction.</p>
 */
public record ServletDefinition(
        String servletName,
        String servletClass,
        Map<String, String> initParameters,
        List<String> urlPatterns
) {
}
