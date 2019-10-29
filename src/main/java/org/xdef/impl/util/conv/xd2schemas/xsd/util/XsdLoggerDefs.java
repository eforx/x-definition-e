package org.xdef.impl.util.conv.xd2schemas.xsd.util;

public interface XsdLoggerDefs {

    int LOG_NONE = 0;
    int LOG_ERROR = LOG_NONE + 1;
    int LOG_WARN = LOG_ERROR + 1;
    int LOG_INFO = LOG_WARN + 1;
    int LOG_DEBUG = LOG_INFO + 1;
    int LOG_TRACE = LOG_DEBUG + 1;

    String XSD_XDEF_ADAPTER = "XsdXDefinitionAdapter";
    String XSD_XDEF_EXTRA_ADAPTER = "XsdXDefinitionExtraAdapter";
    String XSD_ELEM_FACTORY = "XsdElementFactory";
    String XSD_DPOOL_ADAPTER = "XsdXPoolAdapter";
    String XSD_UTILS = "XsdUtils";
    String XSD_ADAPTER_CTX = "XsdAdapterCtx";
    String XSD_REFERENCE = "SchemaRefNode";
}
