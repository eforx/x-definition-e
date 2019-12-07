package org.xdef.impl.util.conv.schema.schema2xd.xsd.definition;

import org.xdef.XDConstants;

/**
 * Definitions of constant used in XSD -> x-definition transform algorithm
 */
public interface Xsd2XdDefinitions {

    String XD_NAMESPACE_URI = XDConstants.XDEF32_NS_URI;
    String XSD_DEFAULT_NAMESPACE_PREFIX = "xs";

    String XD_ELEM_POOL = "xd:collection";
    String XD_ELEM_XDEF = "xd:def";
    String XD_ELEM_DECLARATION = "xd:declaration";
    String XD_ELEM_SEQUENCE = "xd:sequence";
    String XD_ELEM_CHOICE = "xd:choice";
    String XD_ELEM_MIXED = "xd:mixed";
    String XD_ELEM_ANY = "xd:any";
    String XD_ELEM_TEXT_REF = "ref";

    String XD_ATTR_NAME = "xd:name";
    String XD_ATTR_ROOT_ELEMT = "xd:root";
    String XD_ATTR_SCRIPT = "xd:script";
    String XD_ATTR_TEXT = "xd:text";

}
