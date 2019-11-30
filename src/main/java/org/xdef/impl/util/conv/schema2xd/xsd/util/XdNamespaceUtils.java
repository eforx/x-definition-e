package org.xdef.impl.util.conv.schema2xd.xsd.util;

import org.apache.ws.commons.schema.constants.Constants;

import static org.xdef.impl.util.conv.schema2xd.xsd.definition.Xsd2XdDefinitions.XSD_DEFAULT_NAMESPACE_PREFIX;

public class XdNamespaceUtils {

    /**
     * Checks if given namespace prefix is default for x-definition
     * @param prefix    namespace prefix
     * @return  return true if if given namespace prefix is default
     */
    public static boolean isDefaultNamespacePrefix(final String prefix) {
        return Constants.XML_NS_PREFIX.equals(prefix)
                || Constants.XMLNS_ATTRIBUTE.equals(prefix)
                || XSD_DEFAULT_NAMESPACE_PREFIX.equals(prefix);
    }

}
