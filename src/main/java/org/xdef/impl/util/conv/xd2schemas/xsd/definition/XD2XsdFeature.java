package org.xdef.impl.util.conv.xd2schemas.xsd.definition;

import java.util.EnumSet;

public enum XD2XsdFeature {
    XSD_ANNOTATION,                 // Output xsd schema will contain additional annotations
    XSD_DECIMAL_ANY_SEPARATOR,      // Output xsd schema will contain decimal type using any decimal separator
    XSD_ALL_UNBOUNDED,              // Output xsd schema will contain only unbounded xs:choice element, if source of the element is xd:mixed
    XSD_KEYS,                       // Output xsd schema will contain xs:unique element
    XSD_KEYS_AND_REFS,              // Output xsd schema will contain xs:key and xs:keyref elements

    POSTPROCESSING,                 // Transform algorithm will execute additional processing of output nodes
    POSTPROCESSING_EXTRA_SCHEMAS,   // Transform algorithm will execute additional processing of nodes that is in different namespace than x-definition using
    POSTPROCESSING_REFS,            // Transform algorithm will execute additional processing of node's references
    POSTPROCESSING_QNAMES,          // Transform algorithm will execute additional processing of node's QNames
    POSTPROCESSING_ALL_TO_CHOICE,   // Transform algorithm will execute additional processing of node's - checks positions of xs:all and convert them to xs:choice if need it
    POSTPROCESSING_MIXED;            // Transform algorithm will execute additional processing of node's - add mixed flag if need it


    public static EnumSet<XD2XsdFeature> POSTPROCESSING_FEATURES = EnumSet.of(
            POSTPROCESSING, POSTPROCESSING_EXTRA_SCHEMAS, POSTPROCESSING_REFS, POSTPROCESSING_QNAMES, POSTPROCESSING_ALL_TO_CHOICE, POSTPROCESSING_MIXED);
}
