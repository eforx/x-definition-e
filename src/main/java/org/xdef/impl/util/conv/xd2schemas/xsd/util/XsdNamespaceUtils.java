package org.xdef.impl.util.conv.xd2schemas.xsd.util;

import org.apache.ws.commons.schema.constants.Constants;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.xdef.impl.XDefinition;

import java.util.Map;

import static org.xdef.impl.util.conv.xd2schemas.xsd.XD2XsdDefinitions.XSD_DEFAULT_SCHEMA_NAMESPACE_PREFIX;
import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.*;

public class XsdNamespaceUtils {

    public static NamespaceMap createCtx() {
        NamespaceMap ctx = new NamespaceMap();
        // Default XSD namespace with prefix xs
        ctx.add(XSD_DEFAULT_SCHEMA_NAMESPACE_PREFIX, Constants.URI_2001_SCHEMA_XSD);
        return ctx;
    }

    /**
     * Namespace context initialization based on x-definition
     * @param xDef
     * @param logLevel
     */
    public static void initCtx(final NamespaceMap namespaceMap, final XDefinition xDef, final String targetNsPrefix, final String targetNsUri, final String phase, int logLevel) {
        // Target namespace
        if (targetNsPrefix != null && targetNsUri != null) {
            addNamespaceToCtx(namespaceMap, xDef.getName(), targetNsPrefix, targetNsUri, phase, logLevel);
        }

        for (Map.Entry<String, String> entry : xDef._namespaces.entrySet()) {
            final String nsPrefix = entry.getKey();
            final String nsUri = entry.getValue();

            if (XD2XsdUtils.isDefaultNamespacePrefix(nsPrefix) || (targetNsPrefix != null && nsPrefix.equals(targetNsPrefix))) {
                continue;
            }

            if (!namespaceMap.containsKey(nsPrefix)) {
                addNamespaceToCtx(namespaceMap, xDef.getName(), nsPrefix, nsUri, phase, logLevel);
            } else {
                if (XsdLogger.isWarn(logLevel)) {
                    XsdLogger.printP(WARN, phase, xDef, "Namespace has been already defined! Prefix=" + nsPrefix + ", Uri=" + nsUri);
                }
            }
        }
    }

    /**
     * Add new namespace to namespace context
     * @param namespaceMap  namespace storage
     * @param category
     * @param nsPrefix      namespace prefix
     * @param nsUri         namespace URI
     * @param phase
     * @param logLevel
     */
    public static void addNamespaceToCtx(final NamespaceMap namespaceMap, final String category, final String nsPrefix, final String nsUri, final String phase, int logLevel) {
        namespaceMap.add(nsPrefix, nsUri);
        if (XsdLogger.isDebug(logLevel)) {
            XsdLogger.print(DEBUG, phase, category, "Add namespace. Prefix=" + nsPrefix + ", Uri=" + nsUri);
        }
    }
}
