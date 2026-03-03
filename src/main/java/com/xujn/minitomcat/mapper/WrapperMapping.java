package com.xujn.minitomcat.mapper;

/**
 * Stores one URL pattern to wrapper binding inside a Context.
 *
 * <p>Thread safety assumption: immutable after startup registration.</p>
 */
public record WrapperMapping(String pattern, MatchType matchType, String wrapperName, int order) {
}
