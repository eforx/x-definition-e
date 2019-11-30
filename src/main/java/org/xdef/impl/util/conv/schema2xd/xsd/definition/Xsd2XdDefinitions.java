package org.xdef.impl.util.conv.schema2xd.xsd.definition;

import org.xdef.XDConstants;

public interface Xsd2XdDefinitions {

    String XD_NAMESPACE_URI = XDConstants.XDEF32_NS_URI;

    String XD_ELEM_POOL = "xd:collection";
    String XD_ELEM_XDEF = "xd:def";
    String XD_ELEM_DECLARATION = "xd:declaration";
    String XD_ELEM_SEQUENCE = "xd:sequence";
    String XD_ELEM_CHOICE = "xd:choice";
    String XD_ELEM_MIXED = "xd:mixed";

    String XD_ATTR_NAME = "xd:name";
    String XD_ATTR_ROOT_ELEMT = "xd:root";
    String XD_ATTR_SCRIPT = "xd:script";

}
