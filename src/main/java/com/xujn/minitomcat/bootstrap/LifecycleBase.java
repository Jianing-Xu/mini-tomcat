package com.xujn.minitomcat.bootstrap;

/**
 * Provides guarded lifecycle transitions with idempotent semantics.
 *
 * <p>Thread safety assumption: lifecycle methods may be called from different threads,
 * so state changes are synchronized.</p>
 */
public abstract class LifecycleBase implements Lifecycle {

    private volatile LifecycleState state = LifecycleState.NEW;

    @Override
    public synchronized void init() {
        if (state != LifecycleState.NEW) {
            return;
        }
        try {
            initInternal();
            state = LifecycleState.INITIALIZED;
        } catch (RuntimeException ex) {
            state = LifecycleState.FAILED;
            throw ex;
        }
    }

    @Override
    public synchronized void start() {
        if (state == LifecycleState.STARTED) {
            return;
        }
        if (state == LifecycleState.NEW) {
            init();
        }
        try {
            startInternal();
            state = LifecycleState.STARTED;
        } catch (RuntimeException ex) {
            state = LifecycleState.FAILED;
            throw ex;
        }
    }

    @Override
    public synchronized void stop() {
        if (state != LifecycleState.STARTED) {
            return;
        }
        try {
            stopInternal();
            state = LifecycleState.STOPPED;
        } catch (RuntimeException ex) {
            state = LifecycleState.FAILED;
            throw ex;
        }
    }

    @Override
    public synchronized void destroy() {
        if (state == LifecycleState.DESTROYED) {
            return;
        }
        if (state == LifecycleState.STARTED) {
            stop();
        }
        try {
            destroyInternal();
            state = LifecycleState.DESTROYED;
        } catch (RuntimeException ex) {
            state = LifecycleState.FAILED;
            throw ex;
        }
    }

    @Override
    public LifecycleState getState() {
        return state;
    }

    protected void initInternal() {
        // default no-op
    }

    protected void startInternal() {
        // default no-op
    }

    protected void stopInternal() {
        // default no-op
    }

    protected void destroyInternal() {
        // default no-op
    }
}
