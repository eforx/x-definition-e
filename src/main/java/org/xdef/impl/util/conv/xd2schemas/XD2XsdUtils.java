package org.xdef.impl.util.conv.xd2schemas;

import org.apache.ws.commons.schema.constants.Constants;

import javax.xml.namespace.QName;

public class XD2XsdUtils {

    public static QName parserNameToQName(final String parserName) {
        if ("CDATA".equals(parserName)) {
            return Constants.XSD_STRING;
        } else if ("string".equals(parserName)) {
            return Constants.XSD_STRING;
        } else if ("int".equals(parserName)) {
            return Constants.XSD_INT;
        } else if ("long".equals(parserName)) {
            return Constants.XSD_LONG;
        } else if ("double".equals(parserName)) {
            return Constants.XSD_DOUBLE;
        } else if ("float".equals(parserName)) {
            return Constants.XSD_FLOAT;
        } else if ("enum".equals(parserName)) {
            return Constants.XSD_STRING;
        } else {
            System.out.println("Unknown reference type parser: "+ parserName);
        }

        return null;
    }
}
