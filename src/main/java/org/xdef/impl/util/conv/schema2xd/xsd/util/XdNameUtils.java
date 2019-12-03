package org.xdef.impl.util.conv.schema2xd.xsd.util;

import org.xdef.impl.util.conv.schema2xd.xsd.model.XdAdapterCtx;

import javax.xml.namespace.QName;

public class XdNameUtils {

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
}
