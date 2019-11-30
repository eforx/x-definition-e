package org.xdef.impl.util.conv.schema2xd.xsd.definition;

/**
 * Features of XSD -> x-definition transform algorithm
 */
public enum Xsd2XdFeature {
    XD_EXPLICIT_OCCURRENCE,         // Output x-definition will contain "occurs 1" in nodes with cardinality 1
    XD_TEXT_OPTIONAL,               // Output x-definition will contain optional occurrence for text nodes converted from XSD extension

}
