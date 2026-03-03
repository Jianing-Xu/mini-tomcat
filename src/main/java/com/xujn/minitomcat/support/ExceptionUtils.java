package com.xujn.minitomcat.support;

/**
 * Extracts concise diagnostic information from nested exceptions.
 */
public final class ExceptionUtils {

    private ExceptionUtils() {
    }

    public static Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }

    public static String rootCauseMessage(Throwable throwable) {
        Throwable root = rootCause(throwable);
        return root.getMessage() == null ? root.getClass().getSimpleName() : root.getMessage();
    }
}
