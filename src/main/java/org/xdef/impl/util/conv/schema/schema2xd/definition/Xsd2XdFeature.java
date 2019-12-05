package org.xdef.impl.util.conv.schema.schema2xd.definition;

/**
 * Features of XSD -> x-definition transform algorithm
 */
public enum Xsd2XdFeature {
    XD_EXPLICIT_OCCURRENCE,         // Output x-definition will contain "occurs 1" in nodes with cardinality 1
    XD_TEXT_REQUIRED,               // Output x-definition will contain required occurrence for text nodes converted from XSD extension
    XD_MIXED_REQUIRED,              // Output x-definition will contain required occurrence for mixed text in element nodes

}
