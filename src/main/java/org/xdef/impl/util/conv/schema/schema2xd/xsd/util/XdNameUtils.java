package org.xdef.impl.util.conv.schema.schema2xd.xsd.util;

import org.xdef.impl.util.conv.schema.schema2xd.xsd.model.XdAdapterCtx;

import javax.xml.namespace.QName;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XdNameUtils {

    static private final Pattern XSD_NAME_PATTERN_1 = Pattern.compile("(.+)(?:\\.xsd)");
    static private final Pattern XSD_NAME_PATTERN_2 = Pattern.compile(".*[\\/|\\\\](.+)(?:\\.xsd)");

    public static String getLocalName(final String qName) {
        final int nsPrefixPos = qName.indexOf(':');
        if (nsPrefixPos != -1) {
            return qName.substring(nsPrefixPos + 1, qName.length());
        }

        return qName;
    }

    public static String createQualifiedName(final QName qName, final String xDefName, final XdAdapterCtx xdAdapterCtx) {
        final String nsPrefix = xdAdapterCtx.getNamespacePrefix(xDefName, qName.getNamespaceURI());
        if (nsPrefix == null || nsPrefix.isEmpty()) {
            return qName.getLocalPart();
        }

        return nsPrefix + ":" + qName.getLocalPart();
    }

    public static String createQualifiedName(final QName qName) {
        final String nsPrefix = qName.getPrefix();
        if (nsPrefix == null || nsPrefix.isEmpty()) {
            return qName.getLocalPart();
        }

        return nsPrefix + ":" + qName.getLocalPart();
    }

    public static String getSchemaName(final String schemaLocation) {
        Matcher matcher = XSD_NAME_PATTERN_2.matcher(schemaLocation);
        if (!matcher.matches()) {
            matcher = XSD_NAME_PATTERN_1.matcher(schemaLocation);
        }

        if (matcher.matches()) {
            return matcher.group(1);
        }

        return schemaLocation;
    }
}
