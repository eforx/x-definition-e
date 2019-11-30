package org.xdef.impl.util.conv.schema2xd.xsd.util;

public class XdNameUtils {

    public static String getLocalName(final String qName) {
        final int nsPrefixPos = qName.indexOf(':');
        if (nsPrefixPos != -1) {
            return qName.substring(nsPrefixPos + 1, qName.length());
        }

        return qName;
    }
}
