package com.xujn.minitomcat.bootstrap;

import java.nio.file.Path;

/**
 * Command line bootstrap entry point used by examples and manual validation.
 */
public final class Bootstrap {

    private Bootstrap() {
    }

    public static MiniTomcat start(Path serverPropertiesPath) {
        MiniTomcat miniTomcat = new MiniTomcatBuilder().build(serverPropertiesPath);
        miniTomcat.start();
        return miniTomcat;
    }
}
