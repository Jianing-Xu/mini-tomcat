package com.xujn.minitomcat.support.exception;

/**
 * Indicates the response was already committed and cannot be overwritten.
 */
public class ResponseCommittedException extends RuntimeException {

    public ResponseCommittedException(String message) {
        super(message);
    }
}
