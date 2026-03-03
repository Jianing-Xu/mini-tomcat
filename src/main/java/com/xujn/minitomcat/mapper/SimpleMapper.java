package com.xujn.minitomcat.mapper;

import com.xujn.minitomcat.container.Context;
import com.xujn.minitomcat.container.Host;
import com.xujn.minitomcat.container.Wrapper;
import java.util.Comparator;
import java.util.List;

/**
 * Implements the Phase 1 routing rules: host fallback, longest Context path,
 * and exact/path/default servlet precedence.
 *
 * <p>Thread safety assumption: reads immutable registry state after startup completes.</p>
 */
public class SimpleMapper implements Mapper {

    private final MappingRegistry registry;

    public SimpleMapper(MappingRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Host mapHost(String hostName) {
        String normalized = normalizeHost(hostName);
        Host host = normalized == null ? null : registry.findHost(normalized);
        if (host == null) {
            host = registry.findHost(registry.getDefaultHost());
        }
        return host;
    }

    @Override
    public Context mapContext(Host host, String requestUri) {
        if (host == null) {
            return null;
        }
        String normalizedRequestUri = normalizeRequestUri(requestUri);
        for (Context context : registry.findContexts(host)) {
            String contextPath = normalizeContextPath(context.getPath());
            if ("/".equals(contextPath)) {
                return context;
            }
            if (normalizedRequestUri.equals(contextPath) || normalizedRequestUri.startsWith(contextPath + "/")) {
                return context;
            }
        }
        return null;
    }

    @Override
    public Wrapper mapWrapper(Context context, String requestUri) {
        if (context == null) {
            return null;
        }
        String relativePath = toRelativePath(normalizeRequestUri(requestUri), normalizeContextPath(context.getPath()));
        if (relativePath == null) {
            return null;
        }
        List<WrapperMapping> mappings = registry.findWrappers(context);

        WrapperMapping exact = mappings.stream()
                .filter(mapping -> mapping.matchType() == MatchType.EXACT)
                .filter(mapping -> mapping.pattern().equals(relativePath))
                .findFirst()
                .orElse(null);
        if (exact != null) {
            return toWrapper(context, exact);
        }

        WrapperMapping path = mappings.stream()
                .filter(mapping -> mapping.matchType() == MatchType.PATH)
                .filter(mapping -> matchesPathPattern(mapping.pattern(), relativePath))
                .max(Comparator.comparingInt(mapping -> pathPrefix(mapping.pattern()).length()))
                .orElse(null);
        if (path != null) {
            return toWrapper(context, path);
        }

        WrapperMapping defaultMapping = mappings.stream()
                .filter(mapping -> mapping.matchType() == MatchType.DEFAULT)
                .findFirst()
                .orElse(null);
        return defaultMapping == null ? null : toWrapper(context, defaultMapping);
    }

    private Wrapper toWrapper(Context context, WrapperMapping mapping) {
        return (Wrapper) context.findChild(mapping.wrapperName());
    }

    private boolean matchesPathPattern(String pattern, String relativePath) {
        if (relativePath == null) {
            return false;
        }
        String prefix = pathPrefix(pattern);
        return relativePath.equals(prefix) || relativePath.startsWith(prefix + "/");
    }

    private String pathPrefix(String pattern) {
        return pattern.substring(0, pattern.length() - 2);
    }

    private String toRelativePath(String requestUri, String contextPath) {
        if (!"/".equals(contextPath)
                && !(requestUri.equals(contextPath) || requestUri.startsWith(contextPath + "/"))) {
            return null;
        }
        if ("/".equals(contextPath)) {
            return requestUri;
        }
        String relative = requestUri.substring(contextPath.length());
        return relative.isEmpty() ? "/" : relative;
    }

    private String normalizeHost(String hostName) {
        if (hostName == null || hostName.isBlank()) {
            return null;
        }
        int separator = hostName.indexOf(':');
        return separator >= 0 ? hostName.substring(0, separator) : hostName;
    }

    private String normalizeRequestUri(String requestUri) {
        if (requestUri == null || requestUri.isBlank()) {
            return "/";
        }
        return requestUri.startsWith("/") ? requestUri : "/" + requestUri;
    }

    private String normalizeContextPath(String contextPath) {
        if (contextPath == null || contextPath.isBlank() || "/".equals(contextPath)) {
            return "/";
        }
        return contextPath.endsWith("/") ? contextPath.substring(0, contextPath.length() - 1) : contextPath;
    }
}
