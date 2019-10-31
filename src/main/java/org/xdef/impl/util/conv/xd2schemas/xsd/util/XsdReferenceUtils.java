package org.xdef.impl.util.conv.xd2schemas.xsd.util;

import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.utils.XmlSchemaNamed;
import org.xdef.impl.XData;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.SchemaRefNode;

import java.util.HashMap;
import java.util.Map;

import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.*;

public class XsdReferenceUtils {

    public static SchemaRefNode createElementNode(final XmlSchemaElement xsdElem, final XElement xDefEl) {
        return createElementNode(xDefEl.getXDPosition(), xsdElem, xDefEl);
    }

    private static SchemaRefNode createElementNode(final String localName, final XmlSchemaElement xsdElem, final XElement xDefEl) {
        return new SchemaRefNode(localName, xsdElem, xDefEl);
    }

    public static SchemaRefNode createAttributeNode(final XmlSchemaAttribute xsdAttr, final XData xData) {
        return createAttributeNode(xData.getXDPosition(), xsdAttr, xData);
    }

    private static SchemaRefNode createAttributeNode(final String localName, final XmlSchemaAttribute xsdAttr, final XData xData) {
        return new SchemaRefNode(localName, xsdAttr, xData);
    }

    public static void createElemRefAndDef(final XElement xDefEl, final XmlSchemaElement xsdElem,
                                           final String refSystemId, final String refNodePos, final String refNodePath,
                                           final Map<String, Map<String, SchemaRefNode>> xsdRefs) {
        SchemaRefNode node = createElementNode(xsdElem, xDefEl);
        SchemaRefNode nodeRef = XsdReferenceUtils.createDef(refSystemId, refNodePos, refNodePath, xsdRefs);
        node = XsdReferenceUtils.addNode(node, xsdRefs, true);
        XsdReferenceUtils.createLink(node, nodeRef);
    }

    public static void createElemRefAndDef(final XElement xDefEl, final XmlSchemaElement xsdElem,
                                           final String systemId, String nodePath,
                                           final String refSystemId, final String refNodePos, final String refNodePath,
                                           final Map<String, Map<String, SchemaRefNode>> xsdRefs) {
        SchemaRefNode node = createElementNode(xsdElem, xDefEl);
        SchemaRefNode nodeRef = XsdReferenceUtils.createDef(refSystemId, refNodePos, refNodePath, xsdRefs);
        node = XsdReferenceUtils.addNode(systemId, nodePath, node, xsdRefs, true);
        XsdReferenceUtils.createLink(node, nodeRef);
    }

    public static SchemaRefNode createDef(final String systemId, final String nodePos, final String nodePath, final Map<String, Map<String, SchemaRefNode>> xsdRefs) {
        Map<String, SchemaRefNode> xsdSystemRefs = getSystemRefs(systemId, xsdRefs);
        final String localName = XsdNameUtils.getReferenceName(nodePos);
        SchemaRefNode ref = xsdSystemRefs.get(nodePath);
        if (ref == null) {
            ref = new SchemaRefNode(nodePos);
            xsdSystemRefs.put(nodePath, ref);
            XsdLogger.printG(LOG_INFO, XSD_REFERENCE, "Creating reference definition node. System=" + systemId + ", RefName=" + localName);
        } else {
            XsdLogger.printG(LOG_DEBUG, XSD_REFERENCE, "Reference definition of node already exists. System=" + systemId + ", RefName=" + localName);
        }

        return ref;
    }

    public static SchemaRefNode addNode(SchemaRefNode node, final Map<String, Map<String, SchemaRefNode>> xsdRefs, boolean hasRef) {
        final String xPos = node.getXdNode().getXDPosition();
        final String systemId = XsdNamespaceUtils.getReferenceSystemId(xPos);
        final String nodePath = XsdNameUtils.getReferenceNodePath(xPos);
        return addNode(systemId, nodePath, node, xsdRefs, hasRef);
    }

    public static SchemaRefNode addNode(final String systemId, String nodePath, SchemaRefNode node, final Map<String, Map<String, SchemaRefNode>> xsdRefs, boolean hasRef) {
        Map<String, SchemaRefNode> xsdSystemRefs = getSystemRefs(systemId, xsdRefs);

        final SchemaRefNode refOrig = xsdSystemRefs.get(nodePath);
        if (refOrig != null && refOrig.getXsdNode() != null) {
            XsdLogger.printG(LOG_DEBUG, XSD_REFERENCE, "Node with this name is already defined. System=" + systemId + ", Path=" + nodePath + ", Node=" + node.getName());
            return node;
        }

        final String msg = hasRef ? " (with reference)" : "";
        if (refOrig != null) {
            refOrig.copy(node);
            XsdLogger.printG(LOG_INFO, XSD_REFERENCE, "Updating node" + msg + ". System=" + systemId + ", Path=" + nodePath + ", Node=" + node.getName());
            return refOrig;
        } else {
            xsdSystemRefs.put(nodePath, node);
            XsdLogger.printG(LOG_INFO, XSD_REFERENCE, "Creating node" + msg + ". System=" + systemId + ", Path=" + nodePath + ", Node=" + node.getName());
            return node;
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
        XsdLogger.printG(LOG_INFO, XSD_REFERENCE, "Creating link between nodes. From=" + ref.getName() + ", To=" + def.getName());

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
