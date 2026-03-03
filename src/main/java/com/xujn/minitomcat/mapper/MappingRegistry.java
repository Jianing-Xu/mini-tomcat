package com.xujn.minitomcat.mapper;

import com.xujn.minitomcat.container.Context;
import com.xujn.minitomcat.container.Host;
import com.xujn.minitomcat.deploy.DeploymentException;
import com.xujn.minitomcat.deploy.ServletDefinition;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds the startup-built routing tables used by the mapper.
 *
 * <p>Key constraint: conflict detection happens only during registration.
 * Thread safety assumption: writes happen during startup, reads happen during runtime.</p>
 */
public class MappingRegistry {

    private final String defaultHost;
    private final Map<String, Host> hosts = new LinkedHashMap<>();
    private final Map<String, List<Context>> contextsByHost = new LinkedHashMap<>();
    private final Map<String, List<WrapperMapping>> wrappersByContext = new LinkedHashMap<>();

    public MappingRegistry(String defaultHost) {
        this.defaultHost = defaultHost;
    }

    public String getDefaultHost() {
        return defaultHost;
    }

    public void registerHost(Host host) {
        hosts.put(host.getName(), host);
        contextsByHost.computeIfAbsent(host.getName(), key -> new ArrayList<>());
    }

    public void registerContext(Host host, Context context) {
        List<Context> contexts = contextsByHost.computeIfAbsent(host.getName(), key -> new ArrayList<>());
        contexts.add(context);
        contexts.sort(Comparator.comparingInt((Context value) -> normalizeContextPath(value.getPath()).length()).reversed());
        wrappersByContext.computeIfAbsent(contextKey(context), key -> new ArrayList<>());
    }

    public void registerWrapper(Context context, ServletDefinition servletDefinition) {
        List<WrapperMapping> mappings = wrappersByContext.computeIfAbsent(contextKey(context), key -> new ArrayList<>());
        int order = mappings.size();
        for (String urlPattern : servletDefinition.urlPatterns()) {
            MatchType matchType = determineMatchType(urlPattern);
            for (WrapperMapping existing : mappings) {
                if (existing.matchType() == matchType
                        && existing.pattern().equals(urlPattern)
                        && !existing.wrapperName().equals(servletDefinition.servletName())) {
                    throw new DeploymentException(
                            "Duplicate servlet mapping detected for context=" + context.getPath()
                                    + " pattern=" + urlPattern
                                    + " existingServlet=" + existing.wrapperName()
                                    + " newServlet=" + servletDefinition.servletName()
                    );
                }
            }
            mappings.add(new WrapperMapping(urlPattern, matchType, servletDefinition.servletName(), order++));
        }
    }

    public Host findHost(String hostName) {
        return hosts.get(hostName);
    }

    public List<Context> findContexts(Host host) {
        return contextsByHost.getOrDefault(host.getName(), List.of());
    }

    public List<WrapperMapping> findWrappers(Context context) {
        return wrappersByContext.getOrDefault(contextKey(context), List.of());
    }

    public void clear() {
        hosts.clear();
        contextsByHost.clear();
        wrappersByContext.clear();
    }

    private MatchType determineMatchType(String urlPattern) {
        if ("/".equals(urlPattern)) {
            return MatchType.DEFAULT;
        }
        if (urlPattern.endsWith("/*")) {
            return MatchType.PATH;
        }
        return MatchType.EXACT;
    }

    private String contextKey(Context context) {
        String hostName = context.getParent() == null ? "" : context.getParent().getName();
        return hostName + "::" + normalizeContextPath(context.getPath());
    }

    private String normalizeContextPath(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }
        if ("/".equals(path)) {
            return path;
        }
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }
}
