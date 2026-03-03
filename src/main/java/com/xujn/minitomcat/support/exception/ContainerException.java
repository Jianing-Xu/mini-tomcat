package com.xujn.minitomcat.support.exception;

/**
 * Represents a runtime container failure while dispatching a request.
 */
public class ContainerException extends RuntimeException {

    public ContainerException(String message) {
        super(message);
    }

    public ContainerException(String message, Throwable cause) {
        super(message, cause);
    }
}
