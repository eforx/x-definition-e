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

    String CAT_XSD_BUILDER = "XsdBuilder";
    String CAT_XSD_ELEM_BUILDER = "XsdElementBuilder";
    String CAT_XSD_REF_BUILDER = "XsdReferenceBuilder";
    String CAT_XD_POOL = "XdPoolAdapter";
    String CAT_XD_DEF = "XdDefAdapter";
}
