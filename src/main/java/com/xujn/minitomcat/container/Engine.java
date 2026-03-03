package com.xujn.minitomcat.container;

/**
 * Top-level container responsible for choosing the target Host.
 */
public interface Engine extends Container {

    String getDefaultHost();
}
