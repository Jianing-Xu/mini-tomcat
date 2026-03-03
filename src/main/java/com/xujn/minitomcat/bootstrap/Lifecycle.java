package com.xujn.minitomcat.bootstrap;

/**
 * Defines the minimal lifecycle contract for all Phase 1 container components.
 *
 * <p>Thread safety assumption: callers serialize lifecycle transitions. Implementations
 * still guard internal state to reject duplicate transitions.</p>
 */
public interface Lifecycle {

    void init();

    void start();

    void stop();

    void destroy();

    LifecycleState getState();
}
