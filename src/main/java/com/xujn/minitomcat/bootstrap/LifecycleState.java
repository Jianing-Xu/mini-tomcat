package com.xujn.minitomcat.bootstrap;

/**
 * Tracks lifecycle progress for core container components.
 */
public enum LifecycleState {
    NEW,
    INITIALIZED,
    STARTED,
    STOPPED,
    DESTROYED,
    FAILED
}
