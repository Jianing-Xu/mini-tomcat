package com.xujn.minitomcat.mapper;

import com.xujn.minitomcat.container.Context;
import com.xujn.minitomcat.container.Host;
import com.xujn.minitomcat.container.Wrapper;

/**
 * Bundles the resolved routing result for diagnostics and future expansion.
 */
public record MappingResult(Host host, Context context, Wrapper wrapper) {
}
