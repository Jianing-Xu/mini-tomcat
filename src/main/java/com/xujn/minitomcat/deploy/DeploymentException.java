package com.xujn.minitomcat.deploy;

/**
 * Signals an unrecoverable deployment-time failure such as malformed web.xml or
 * duplicate servlet mappings.
 *
 * <p>Thread safety assumption: raised during startup while deployment metadata is still single-threaded.</p>
 */
public class DeploymentException extends RuntimeException {

    public DeploymentException(String message) {
        super(message);
    }

    public DeploymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
