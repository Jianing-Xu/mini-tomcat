package com.xujn.minitomcat.mapper;

import com.xujn.minitomcat.container.Context;
import com.xujn.minitomcat.container.Host;
import com.xujn.minitomcat.container.Wrapper;

/**
 * Resolves Host, Context, and Wrapper matches for an incoming request.
 *
 * <p>Key constraint: mapping decisions are pure lookups based on startup-built indexes.
 * Thread safety assumption: implementations are read-only during request dispatch.</p>
 */
public interface Mapper {

    Host mapHost(String hostName);

    Context mapContext(Host host, String requestUri);

    Wrapper mapWrapper(Context context, String requestUri);
}
