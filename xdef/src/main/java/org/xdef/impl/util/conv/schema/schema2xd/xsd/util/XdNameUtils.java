package org.xdef.impl.util.conv.schema.schema2xd.xsd.util;

import org.xdef.impl.util.conv.schema.schema2xd.xsd.model.XdAdapterCtx;

import javax.xml.namespace.QName;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utils related to working with node name, reference name and qualified name
 */
public class XdNameUtils {

    static private final Pattern XSD_NAME_PATTERN_1 = Pattern.compile("(.+)(?:\\.xsd)");
    static private final Pattern XSD_NAME_PATTERN_2 = Pattern.compile(".*[\\/|\\\\](.+)(?:\\.xsd)");

    /**
     * Parses local part of string qualified name
     * @param qName     qualified name
     * @return local part if is part of qualified name, otherwise whole qualified name
     */
    public static String getLocalName(final String qName) {
        final int nsPrefixPos = qName.indexOf(':');
        if (nsPrefixPos != -1) {
            return qName.substring(nsPrefixPos + 1);
        }

        return qName;
    }

    /**
     * Creates string qualified name from given arguments.
     *
     * If qualified name is using unknown namespace URI in given x-definition, then output will not contain namespace prefix.
     *
     * @param qName             qualified name
     * @param xDefName          x-definition name
     * @param xdAdapterCtx      x-definition adapter context
     * @return string qualified name
     */
    public static String createQualifiedName(final QName qName, final String xDefName, final XdAdapterCtx xdAdapterCtx) {
        final String nsPrefix = xdAdapterCtx.findNamespacePrefix(xDefName, qName.getNamespaceURI());
        if (nsPrefix == null || nsPrefix.isEmpty()) {
            return qName.getLocalPart();
        }

        return nsPrefix + ":" + qName.getLocalPart();
    }

    /**
     * Creates string qualified name from given qualified name.
     * @param qName     qualified name
     * @return string qualified name
     */
    public static String createQualifiedName(final QName qName) {
        final String nsPrefix = qName.getPrefix();
        if (nsPrefix == null || nsPrefix.isEmpty()) {
            return qName.getLocalPart();
        }

        return nsPrefix + ":" + qName.getLocalPart();
    }

    /**
     * Parses XSD document name from XSD document location
     * @param schemaLocation    XSD document location
     * @return XSD document name
     */
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
