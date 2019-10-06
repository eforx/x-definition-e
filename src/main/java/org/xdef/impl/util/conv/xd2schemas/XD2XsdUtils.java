package org.xdef.impl.util.conv.xd2schemas;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.constants.Constants;
import org.xdef.XDConstants;

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
        } else if ("gMonth".equals(parserName)) {
            return Constants.XSD_MONTH;
        } else if ("gMonthDay".equals(parserName)) {
            return Constants.XSD_MONTHDAY;
        } else if ("gYear".equals(parserName) || "ISOyear".equals(parserName)) {
            return Constants.XSD_YEAR;
        } else if ("gDay".equals(parserName)) {
            return Constants.XSD_DAY;
        } else if ("time".equals(parserName)) {
            return Constants.XSD_TIME;
        } else if ("dateTime".equals(parserName) || "ISOdateTime".equals(parserName)) {
            return Constants.XSD_DATETIME;
        } else if ("ISOyearMonth".equals(parserName)) {
            return Constants.XSD_YEARMONTH;
        } else if ("ISOdate".equals(parserName)) {
            return Constants.XSD_DATE;
        } else {

            System.out.println("Unknown reference type parser: "+ parserName);
        }

        return null;
    }

    // If name contains ":" or reference has different namespace, then element contains external reference
    public static boolean isExternalRef(final String nodeName, final String namespaceUri, final XmlSchema schema) {
        return nodeName.indexOf(':') != -1 && (namespaceUri != null && !namespaceUri.equals(schema.getTargetNamespace()));
    }

    public static String getReferenceSystemId(final String reference) {
        int xdefSystemSeparatorPos = reference.indexOf('#');
        if (xdefSystemSeparatorPos != -1) {
            return reference.substring(0, xdefSystemSeparatorPos);
        }

        return null;
    }

    public static String getReferenceName(final String reference) {
        int xdefNamespaceSeparatorPos = reference.indexOf(':');
        if (xdefNamespaceSeparatorPos != -1) {
            return reference.substring(xdefNamespaceSeparatorPos + 1);
        }

        int xdefSystemSeparatorPos = reference.indexOf('#');
        if (xdefSystemSeparatorPos != -1) {
            return reference.substring(xdefSystemSeparatorPos + 1);
        }

        return reference;
    }

    public static boolean isDefaultNamespacePrefix(final String prefix) {
        return Constants.XML_NS_PREFIX.equals(prefix)
                || Constants.XMLNS_ATTRIBUTE.equals(prefix)
                || XDConstants.XDEF_NS_PREFIX.equals(prefix);
    }
}
