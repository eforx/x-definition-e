package org.xdef.impl.util.conv.xd2schemas.xsd.util;

public interface XsdLoggerDefs {

    /**
     * Used for preparation process of transformation x-defintion to xsd
     */
    String INITIALIZATION = "Initialization";

    /**
     * Used extracting data from x-definition tree
     */
    String PREPROCESSING = "Pre-processing";

    /**
     * Used for tree transformation of x-definition elements
     */
    String TRANSFORMATION = "Transformation";

    /**
     * Used for later node transformation based on already created output
     */
    String POSTPROCESSING = "Post-processing";


    int LOG_LEVEL_NONE = 0;
    int LOG_LEVEL_ERROR = LOG_LEVEL_NONE + 1;
    int LOG_LEVEL_WARN = LOG_LEVEL_ERROR + 1;
    int LOG_LEVEL_INFO = LOG_LEVEL_WARN + 1;
    int LOG_LEVEL_DEBUG = LOG_LEVEL_INFO + 1;
    int LOG_LEVEL_TRACE = LOG_LEVEL_DEBUG + 1;

    String ERROR = "ERROR";
    String WARN = "WARN";
    String INFO = "INFO";
    String DEBUG = "DEBUG";
    String TRACE = "TRACE";

    String XSD_XDEF_ADAPTER = "XsdXDefinitionAdapter";
    String XSD_XDEF_EXTRA_ADAPTER = "XsdXDefinitionExtraAdapter";
    String XSD_ELEM_FACTORY = "XsdElementFactory";
    String XSD_DPOOL_ADAPTER = "XsdXPoolAdapter";
    String XSD_UTILS = "XsdUtils";
    String XSD_ADAPTER_CTX = "XsdAdapterCtx";
}
