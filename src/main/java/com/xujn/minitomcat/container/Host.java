package com.xujn.minitomcat.container;

/**
 * Virtual-host container responsible for choosing the target Context.
 */
public interface Host extends Container {

    String[] getAliases();
}
