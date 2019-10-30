package org.xdef.impl.util.conv.xd2schemas.xsd.util;

import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.utils.XmlSchemaNamed;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.SchemaRefNode;

import java.util.HashMap;
import java.util.Map;

import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.*;

public class XsdReferenceUtils {

    public static SchemaRefNode createNode(final XmlSchemaElement xsdElem, final XElement xDefEl) {
        return createNode(xDefEl.getName(), xsdElem, xDefEl);
    }

    public static SchemaRefNode createNode(final String localName, final XmlSchemaElement xsdElem, final XElement xDefEl) {
        return new SchemaRefNode(localName, xsdElem, xDefEl);
    }

    public static void createRefAndDef(final XElement xDefEl, final XmlSchemaElement xsdElem,
                                       final String refSystemId, final String refLocalName, final String refNodePath,
                                       final Map<String, Map<String, SchemaRefNode>> xsdRefs) {
        SchemaRefNode node = createNode(xsdElem, xDefEl);
        SchemaRefNode nodeRef = XsdReferenceUtils.createDef(refSystemId, refLocalName, refNodePath, xsdRefs);
        XsdReferenceUtils.addNode(node, xsdRefs, true);
        XsdReferenceUtils.createLink(node, nodeRef);
    }

    public static SchemaRefNode createDef(final String systemId, String localName, final String nodePath, final Map<String, Map<String, SchemaRefNode>> xsdRefs) {
        Map<String, SchemaRefNode> xsdSystemRefs = getSystemRefs(systemId, xsdRefs);

        SchemaRefNode ref = xsdSystemRefs.get(nodePath);
        if (ref == null) {
            ref = new SchemaRefNode(localName);
            xsdSystemRefs.put(nodePath, ref);
            XsdLogger.printG(LOG_INFO, XSD_REFERENCE, "Creating reference definition node. System=" + systemId + ", RefName=" + localName);
        }

        return ref;
    }

    public static void addNode(SchemaRefNode node, final Map<String, Map<String, SchemaRefNode>> xsdRefs, boolean hasRef) {
        final String xPos = node.getXdNode().getXDPosition();
        final String systemId = XsdNamespaceUtils.getReferenceSystemId(xPos);
        final String nodePath = XsdNameUtils.getReferenceNodePath(xPos);
        addNode(systemId, nodePath, node, xsdRefs, hasRef);
    }

    public static void addNode(final String systemId, String nodePath, SchemaRefNode node, final Map<String, Map<String, SchemaRefNode>> xsdRefs, boolean hasRef) {
        Map<String, SchemaRefNode> xsdSystemRefs = getSystemRefs(systemId, xsdRefs);

        final SchemaRefNode refOrig = xsdSystemRefs.get(nodePath);
        if (refOrig != null && refOrig.getXsdNode() != null) {
            XsdLogger.printG(LOG_DEBUG, XSD_REFERENCE, "Node with this name is already defined. System=" + systemId + ", Path=" + nodePath);
            return;
        }

        final String msg = hasRef ? " with reference" : "";
        if (refOrig != null) {
            refOrig.copy(node);
            XsdLogger.printG(LOG_INFO, XSD_REFERENCE, "Updating node" + msg + ". System=" + systemId + ", Path=" + nodePath);
        } else {
            xsdSystemRefs.put(nodePath, node);
            XsdLogger.printG(LOG_INFO, XSD_REFERENCE, "Creating node" + msg + ". System=" + systemId + ", Path=" + nodePath);
        }
    }

    public static void updateNode(final XNode xNode, final XmlSchemaNamed newXsdNode, final Map<String, Map<String, SchemaRefNode>> xsdRefs) {
        final String systemId = XsdNamespaceUtils.getReferenceSystemId(xNode.getXDPosition());
        final String nodePath = XsdNameUtils.getReferenceNodePath(xNode.getXDPosition());
        updateNode(systemId, nodePath, newXsdNode, xsdRefs);
    }

    public static void updateNode(final String systemId, String nodePath, final XmlSchemaNamed newXsdNode, final Map<String, Map<String, SchemaRefNode>> xsdRefs) {
        XsdLogger.printG(LOG_INFO, XSD_REFERENCE, "Updating xsd content of node. System=" + systemId + ", Path=" + nodePath);

        Map<String, SchemaRefNode> xsdSystemRefs = getSystemRefs(systemId, xsdRefs);

        final SchemaRefNode refOrig = xsdSystemRefs.get(nodePath);
        if (refOrig == null) {
            XsdLogger.printG(LOG_WARN, XSD_REFERENCE, "Node does not exist in system! System=" + systemId + ", Path=" + nodePath);
            return;
        }

        refOrig.setXsdNode(newXsdNode);
    }

    public static void createLink(SchemaRefNode ref, SchemaRefNode def) {
        ref.setReference(def);
        def.addRef(ref);
    }

    private static Map<String, SchemaRefNode> getSystemRefs(final String systemId, final Map<String, Map<String, SchemaRefNode>> xsdRefs) {
        Map<String, SchemaRefNode> xsdSystemRefs = xsdRefs.get(systemId);
        if (xsdSystemRefs == null) {
            xsdSystemRefs = new HashMap<String, SchemaRefNode>();
            xsdRefs.put(systemId, xsdSystemRefs);
        }

        return xsdSystemRefs;
    }
}
