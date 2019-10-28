package org.xdef.impl.util.conv.xd2schemas.xsd.util;

import javafx.util.Pair;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.constants.Constants;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.xdef.XDConstants;
import org.xdef.impl.XDefinition;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.model.XMNode;

import java.util.Map;

import static org.xdef.impl.util.conv.xd2schemas.xsd.XD2XsdDefinitions.XSD_DEFAULT_SCHEMA_NAMESPACE_PREFIX;
import static org.xdef.impl.util.conv.xd2schemas.xsd.XD2XsdDefinitions.XSD_NAMESPACE_PREFIX_EMPTY;
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

            if (isDefaultNamespacePrefix(nsPrefix) || (targetNsPrefix != null && nsPrefix.equals(targetNsPrefix))) {
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

    /**
     * Returns true if node name is using different namespace than schema
     * @param nodeName
     * @param namespaceUri
     * @param schema
     * @return
     */
    public static boolean isNodeInDifferentNamespace(final String nodeName, final String namespaceUri, final XmlSchema schema) {
        return hasNamespace(nodeName) && (namespaceUri != null && !namespaceUri.equals(schema.getTargetNamespace()));
    }

    /**
     * Returns true if node name is using different namespace prefix than schema target namespace
     * @param nodeName
     * @param schema
     * @return
     */
    public static boolean isNodeInDifferentNamespacePrefix(final String nodeName, final XmlSchema schema) {
        String nodeNsPrefix = getNamespacePrefix(nodeName);
        return nodeNsPrefix != null && !nodeNsPrefix.equals(schema.getSchemaNamespacePrefix());
    }

    /**
     * Return true if reference is using different namespace prefix than schema target namespace
     * @param nodeRefPos
     * @param schema
     * @return
     */
    public static boolean isRefInDifferentNamespacePrefix(final String nodeRefPos, final XmlSchema schema) {
        final String refNsPrefix = getReferenceNamespacePrefix(nodeRefPos);
        return !XSD_NAMESPACE_PREFIX_EMPTY.equals(refNsPrefix) && !refNsPrefix.equals(schema.getSchemaNamespacePrefix());
    }

    /**
     * Return true if reference is in different x-definition
     * @param nodeRefPos
     * @param xdPos
     * @return
     */
    public static boolean isRefInDifferentSystem(final String nodeRefPos, final String xdPos) {
        final String nodeSystemId = getReferenceSystemId(xdPos);
        final String refSystemId = getReferenceSystemId(nodeRefPos);
        return !nodeSystemId.equals(refSystemId);
    }

    private static boolean hasNamespace(final String name) {
        return name.indexOf(':') != -1;
    }

    public static String getReferenceSystemId(final String reference) {
        int xdefSystemSeparatorPos = reference.indexOf('#');
        if (xdefSystemSeparatorPos != -1) {
            return reference.substring(0, xdefSystemSeparatorPos);
        }

        return null;
    }

    public static String getNamespacePrefix(final String name) {
        int nsPos = name.indexOf(':');
        if (nsPos != -1) {
            return name.substring(0, nsPos);
        }

        return null;
    }

    public static String getReferenceNamespacePrefix(final String refPos) {
        int xdefNamespaceSeparatorPos = refPos.indexOf(':');
        if (xdefNamespaceSeparatorPos == -1) {
            return XSD_NAMESPACE_PREFIX_EMPTY;
        }

        int xdefSystemSeparatorPos = refPos.indexOf('#');
        if (xdefSystemSeparatorPos == -1) {
            return XSD_NAMESPACE_PREFIX_EMPTY;
        }

        return refPos.substring(xdefSystemSeparatorPos + 1, xdefNamespaceSeparatorPos);
    }

    public static boolean isDefaultNamespacePrefix(final String prefix) {
        return Constants.XML_NS_PREFIX.equals(prefix)
                || Constants.XMLNS_ATTRIBUTE.equals(prefix)
                || XDConstants.XDEF_NS_PREFIX.equals(prefix);
    }

    /**
     * Returns true if name is using schema target namespace
     * @param schema
     * @param name
     * @return
     */
    public static boolean usingTargetNamespace(final XmlSchema schema, final String name) {
        return schema.getSchemaNamespacePrefix() != null && name.startsWith(schema.getSchemaNamespacePrefix() + ':');
    }

    public static Pair<String, String> getSchemaTargetNamespace(final XDefinition xDef, Boolean targetNamespaceError, int logLevel) {
        String targetNamespacePrefix = null;
        String targetNamespaceUri = null;
        boolean onlyRefs = false;

        // Get target namespace prefix based on root elements
        if (xDef._rootSelection != null) {
            for (Map.Entry<String, XNode> root : xDef._rootSelection.entrySet()) {
                final String rootName = root.getKey();
                String tmpNs = getNamespacePrefix(rootName);
                if (targetNamespacePrefix == null) {
                    targetNamespacePrefix = tmpNs;
                } else if (tmpNs != null && !targetNamespacePrefix.equals(tmpNs)) {
                    if (XsdLogger.isError(logLevel)) {
                        XsdLogger.printC(ERROR, XSD_UTILS, xDef, "Expected different namespace prefix. Expected=" + targetNamespacePrefix + ", Actual=" + tmpNs);
                    }
                    targetNamespaceError = true;
                    break;
                }

                if (onlyRefs == false && root.getValue().getKind() == XMNode.XMELEMENT) {
                    onlyRefs = ((XElement)root.getValue()).isReference();
                }
            }
        }

        if (targetNamespaceError == true) {
            return new Pair<String, String>(targetNamespacePrefix, targetNamespaceUri);
        }

        // Find target namespace URI based on x-definition namespaces
        if (targetNamespacePrefix != null) {
            for (Map.Entry<String, String> entry : xDef._namespaces.entrySet()) {
                if (targetNamespacePrefix.equals(entry.getKey())) {
                    targetNamespaceUri = entry.getValue();
                    break;
                }
            }
        }

        if (targetNamespacePrefix != null && targetNamespaceUri == null) {
            if (XsdLogger.isError(logLevel)) {
                XsdLogger.printC(ERROR, XSD_UTILS, xDef, "Target namespace URI has been not found for prefix. Prefix=" + targetNamespacePrefix);
            }
            targetNamespaceError = true;
        }

        if (targetNamespaceError == true) {
            return new Pair<String, String>(targetNamespacePrefix, targetNamespaceUri);
        }

        // Try to find default namespace
        if (targetNamespacePrefix == null && targetNamespaceUri == null) {
            for (Map.Entry<String, String> entry : xDef._namespaces.entrySet()) {
                if ("".equals(entry.getKey())) {
                    targetNamespacePrefix = entry.getKey();
                    targetNamespaceUri = entry.getValue();
                    break;
                }
            }
        }

        return new Pair<String, String>(targetNamespacePrefix, targetNamespaceUri);
    }

    public static String createNsUriFromXDefName(final String name) {
        return name;
    }

    public static String createExtraSchemaNameFromNsPrefix(final String nsPrefix) {
        return "external_" + nsPrefix;
    }

    public static String getNsPrefixFromExtraSchemaName(final String nsPrefix) {
        int pos = nsPrefix.lastIndexOf('_');
        if (pos != -1) {
            return nsPrefix.substring(pos + 1);
        }

        return nsPrefix;
    }

    public static String createRefNameDiffSystemAndNs(final String nsUri, final String refLocalName) {
        return "ext_" + nsUri + "_" + refLocalName;
    }

    public static String createRefLocalName(final String name) {
        return "loc_" + name;
    }

    public static XmlSchema getReferenceSchema(final XmlSchemaCollection xmlCollection, final String refSystemId, final String phase, int logLevel) {
        XmlSchema[] schemas = xmlCollection.getXmlSchema(refSystemId);
        if (schemas == null || schemas.length == 0) {
            if (XsdLogger.isWarn(logLevel)) {
                XsdLogger.printP(WARN, phase, "Schema with required name not found! Name=" + refSystemId);
            }
            return null;
        }

        if (schemas.length > 1) {
            if (XsdLogger.isWarn(logLevel)) {
                XsdLogger.printP(WARN, phase, "Multiple schemas with required name have been found! Name=" + refSystemId);
            }
        }

        return schemas[0];
    }

}
