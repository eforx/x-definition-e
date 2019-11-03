package org.xdef.impl.util.conv.xd2schemas.xsd.util;

import javafx.util.Pair;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.constants.Constants;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.xdef.XDConstants;
import org.xdef.impl.XDefinition;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.xd2schemas.xsd.definition.AlgPhase;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.XsdAdapterCtx;
import org.xdef.model.XMNode;

import java.util.Map;

import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XD2XsdDefinitions.XSD_NAMESPACE_PREFIX_EMPTY;
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XsdLoggerDefs.*;

public class XsdNamespaceUtils {

    /**
     * Add new namespace to namespace context
     * @param namespaceMap  namespace storage
     * @param category
     * @param nsPrefix      namespace prefix
     * @param nsUri         namespace URI
     * @param phase
     */
    public static void addNamespaceToCtx(final NamespaceMap namespaceMap, final String category, final String nsPrefix, final String nsUri, final AlgPhase phase) {
        namespaceMap.add(nsPrefix, nsUri);
        XsdLogger.print(LOG_DEBUG, phase, category, "Add namespace. Prefix=" + nsPrefix + ", Uri=" + nsUri);
    }

    /**
     * Returns true if node name is using different namespace than schema
     * @param nodeName
     * @param namespaceUri
     * @param schema
     * @return
     */
    public static boolean isNodeInDifferentNamespace(final String nodeName, final String namespaceUri, final XmlSchema schema) {
        return containsNsPrefix(nodeName) && (namespaceUri != null && !namespaceUri.equals(schema.getTargetNamespace()));
    }

    /**
     * Returns true if node name is using different namespace prefix than schema target namespace
     * @param xNode
     * @param schema
     * @return
     */
    public static boolean isNodeInDifferentNamespacePrefix(final XMNode xNode, final XmlSchema schema) {
        String nodeNsPrefix = getNamespacePrefix(xNode.getName());
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

    public static boolean containsNsPrefix(final String name) {
        return name.indexOf(':') != -1;
    }

    public static String getReferenceSystemId(final String refPos) {
        int systemSeparatorPos = refPos.indexOf('#');
        if (systemSeparatorPos != -1) {
            return refPos.substring(0, systemSeparatorPos);
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
     * Returns true if node name is using schema target namespace
     * @param schema
     * @param name
     * @return
     */
    public static boolean usingTargetNamespace(final XmlSchema schema, final String name) {
        return schema.getSchemaNamespacePrefix() != null && name.startsWith(schema.getSchemaNamespacePrefix() + ':');
    }

    public static boolean isValidNsUri(final String uri) {
        return uri != null && !uri.isEmpty();
    }

    public static Pair<String, String> getSchemaTargetNamespace(final XDefinition xDef, Boolean targetNamespaceError) {
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
                    XsdLogger.printG(LOG_ERROR, XSD_UTILS, xDef, "Expected different namespace prefix. Expected=" + targetNamespacePrefix + ", Actual=" + tmpNs);
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
            XsdLogger.printG(LOG_ERROR, XSD_UTILS, xDef, "Target namespace URI has been not found for prefix. Prefix=" + targetNamespacePrefix);
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

    public static String getNodeNamespaceUri(final XNode xData, final XsdAdapterCtx adapterCtx, final AlgPhase phase) {
        final String xDefPos = xData.getXDPosition();
        final String systemId = XsdNamespaceUtils.getReferenceSystemId(xDefPos);
        XmlSchema refSchema = adapterCtx.getSchema(systemId, true, phase);
        final String nsPrefix = XsdNamespaceUtils.getReferenceNamespacePrefix(xDefPos);
        final String nsUri = refSchema.getNamespaceContext().getNamespaceURI(nsPrefix);
        return nsUri;
    }

}
