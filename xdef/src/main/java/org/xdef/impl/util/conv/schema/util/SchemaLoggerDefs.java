package org.xdef.impl.util.conv.schema.util;

/**
 * Schema transformation algorithm logging definitions
 */
public interface SchemaLoggerDefs {
    int LOG_NONE = 0;
    int LOG_ERROR = LOG_NONE + 1;
    int LOG_WARN = LOG_ERROR + 1;
    int LOG_INFO = LOG_WARN + 1;
    int LOG_DEBUG = LOG_INFO + 1;
    int LOG_TRACE = LOG_DEBUG + 1;
}
