package com.xujn.minitomcat.support.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Creates named non-daemon worker threads for connector request processing.
 */
public class NamedThreadFactory implements ThreadFactory {

    private final String prefix;
    private final AtomicInteger sequence = new AtomicInteger(1);

    public NamedThreadFactory(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(runnable, prefix + "-" + sequence.getAndIncrement());
        thread.setDaemon(false);
        return thread;
    }
}
