package com.xujn.minitomcat.connector;

/**
 * Represents the parsed HTTP request line.
 */
public record RequestLine(String method, String requestTarget, String protocol) {
}
