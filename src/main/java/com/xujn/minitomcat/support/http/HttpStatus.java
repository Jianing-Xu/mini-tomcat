package com.xujn.minitomcat.support.http;

/**
 * Defines the limited HTTP status set needed by Phase 1.
 */
public final class HttpStatus {

    public static final int OK = 200;
    public static final int NOT_FOUND = 404;
    public static final int INTERNAL_SERVER_ERROR = 500;
    public static final int METHOD_NOT_ALLOWED = 405;

    private HttpStatus() {
    }

    public static String reasonPhrase(int status) {
        return switch (status) {
            case OK -> "OK";
            case NOT_FOUND -> "Not Found";
            case INTERNAL_SERVER_ERROR -> "Internal Server Error";
            case METHOD_NOT_ALLOWED -> "Method Not Allowed";
            default -> "Unknown";
        };
    }
}
