package org.xdef.impl.util.conv.schema.xd2schema.xsd.definition;

import java.util.EnumSet;

/**
 * Features of x-definition -> XSD transform algorithm
 */
public enum Xd2XsdFeature {
    XSD_ANNOTATION,                 // Output XSD document will contain additional annotations
    XSD_DECIMAL_ANY_SEPARATOR,      // Output XSD document will convert dec parser to string regular pattern if decimal separator is not dot
    XSD_ALL_UNBOUNDED,              // Output XSD document will contain only unbounded xs:choice element, if source of the element is xd:mixed
    XSD_NAME_COLISSION_DETECTOR,    // Generate new name if collision of names has been found on top-level of schema

    POSTPROCESSING,                 // Transform algorithm will execute additional processing of output nodes
    POSTPROCESSING_EXTRA_SCHEMAS,   // Transform algorithm will execute additional processing of nodes that is in different namespace than x-definition using
    POSTPROCESSING_REFS,            // Transform algorithm will execute additional processing of node's references
    POSTPROCESSING_QNAMES,          // Transform algorithm will execute additional processing of node's QNames
    POSTPROCESSING_ALL_TO_CHOICE,   // Transform algorithm will execute additional processing of node's - checks positions of xs:all and convert them to xs:choice if need it
    POSTPROCESSING_MIXED,           // Transform algorithm will execute additional processing of node's - add mixed flag if need it
    POSTPROCESSING_UNIQUE;          // Transform algorithm will execute additional processing based on gathered information from uniqueSets - creating xs:unique, xs:key and xs:keyref elements from uniqueSet which are using IDREF, IDREFS, CHKID

    public static EnumSet<Xd2XsdFeature> DEFAULT_POSTPROCESSING_FEATURES = EnumSet.of(
            POSTPROCESSING, POSTPROCESSING_EXTRA_SCHEMAS, POSTPROCESSING_REFS, POSTPROCESSING_QNAMES,
            POSTPROCESSING_ALL_TO_CHOICE, POSTPROCESSING_MIXED);
}
