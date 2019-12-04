package org.xdef.impl.util.conv.schema.xd2schema.factory;

import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaGroupRef;
import org.xdef.impl.XData;
import org.xdef.impl.XElement;
import org.xdef.impl.util.conv.schema.util.SchemaLogger;
import org.xdef.impl.util.conv.schema.xd2schema.model.SchemaNode;
import org.xdef.impl.util.conv.schema.xd2schema.model.XsdAdapterCtx;
import org.xdef.impl.util.conv.schema.xd2schema.util.XsdNameUtils;

import java.util.Map;

import static org.xdef.impl.util.conv.schema.util.SchemaLoggerDefs.*;

/**
 * Factory which is creating schema nodes, which pairs up x-definition nodes with XSD nodes
 */
public class SchemaNodeFactory {

    /**
     * Creates schema node and reference schema node based on element node
     * Creates binding between schema element node and reference definition schema node
     *
     * @param xElem         x-definition element node
     * @param xsdElem       XSD element node
     * @param refSystemId   reference node system identifier
     * @param refNodePos    reference node x-position
     * @param refNodePath   reference node path
     * @param adapterCtx    XSD adapter context
     */
    public static void createElemRefAndDef(final XElement xElem, final XmlSchemaElement xsdElem,
                                           final String refSystemId, final String refNodePos, final String refNodePath,
                                           final XsdAdapterCtx adapterCtx) {
        SchemaNode node = createElementNode(xsdElem, xElem);
        final SchemaNode nodeRef = createDefNode(refSystemId, refNodePos, refNodePath, adapterCtx);
        node = adapterCtx.addOrUpdateNode(node);
        SchemaNode.createBinding(node, nodeRef);
    }

    /**
     * Creates schema node and reference definition schema node based on element node which is using different namespace
     * Creates binding between schema element node and reference definition schema node
     *
     * @param xElem         x-definition element node
     * @param xsdElem       XSD element node
     * @param systemId      x-definition element node system identifier
     * @param nodePath      x-definition element node path
     * @param refSystemId   reference node system identifier
     * @param refNodePos    reference node x-position
     * @param refNodePath   reference node path
     * @param adapterCtx    XSD adapter context
     */
    public static void createElemRefAndDefDiffNamespace(final XElement xElem, final XmlSchemaElement xsdElem,
                                                        final String systemId, String nodePath,
                                                        final String refSystemId, final String refNodePos, final String refNodePath,
                                                        final XsdAdapterCtx adapterCtx) {
        SchemaNode node = createElementNode(xsdElem, xElem);
        final SchemaNode nodeRef = createDefNode(refSystemId, refNodePos, refNodePath, adapterCtx);
        node = adapterCtx.addOrUpdateNode(systemId, nodePath, node);
        SchemaNode.createBinding(node, nodeRef);
    }

    /**
     * Creates schema node and reference schema node based on x-definition element and XSD complex content extension
     * Creates binding between schema complex node and reference definition schema node
     *
     * @param xElem             x-definition element node
     * @param xsdComplexExt     XSD complex content extension
     * @param refSystemId       reference node system identifier
     * @param refNodePos        reference node x-position
     * @param refNodePath       reference node path
     * @param adapterCtx        XSD adapter context
     */
    public static void createComplexExtRefAndDef(final XElement xElem, final XmlSchemaComplexContentExtension xsdComplexExt,
                                           final String refSystemId, final String refNodePos, final String refNodePath,
                                           final XsdAdapterCtx adapterCtx) {
        SchemaNode node = createComplexExtNode(xElem, xsdComplexExt);
        final SchemaNode nodeRef = createDefNode(refSystemId, refNodePos, refNodePath, adapterCtx);
        node = adapterCtx.addOrUpdateNode(node);
        SchemaNode.createBinding(node, nodeRef);
    }

    /**
     * Creates schema node based on element node
     * @param xsdElem   XSD element node
     * @param xElem     x-definition element node
     * @return schema node
     */
    public static SchemaNode createElementNode(final XmlSchemaElement xsdElem, final XElement xElem) {
        return new SchemaNode(xElem.getXDPosition(), xsdElem, xElem);
    }

    /**
     * Creates schema node based on attribute node
     * @param xsdAttr       XSD attribute node
     * @param xDataAttr     x-definition attribute node
     * @return schema node
     */
    public static SchemaNode createAttributeNode(final XmlSchemaAttribute xsdAttr, final XData xDataAttr) {
        return new SchemaNode(xDataAttr.getXDPosition(), xsdAttr, xDataAttr);
    }

    /**
     * Creates schema node based on x-definition element node and XSD group reference
     * @param xElem         x-definition element node
     * @param xsdGroupRef   XSD group reference node
     * @param nodeRef       reference schema node
     * @param adapterCtx    XSD adapter context
     */
    public static void createGroupRefNode(final XElement xElem, final XmlSchemaGroupRef xsdGroupRef,
                                          final SchemaNode nodeRef, final XsdAdapterCtx adapterCtx) {
        SchemaNode node = createGroupRefNode(xElem, xsdGroupRef);
        node = adapterCtx.addOrUpdateNode(node);
        SchemaNode.createBinding(node, nodeRef);
    }

    /**
     * Creates schema node based on x-definition element node and XSD complex content extension
     * @param xElem             x-definition element node
     * @param xsdComplexExt     XSD complex content extension node
     * @return schema node
     */
    private static SchemaNode createComplexExtNode(final XElement xElem, final XmlSchemaComplexContentExtension xsdComplexExt) {
        return new SchemaNode(xElem.getXDPosition(), xsdComplexExt, xElem);
    }

    /**
     * Creates schema node based on x-definition element node and XSD group reference
     * @param xElem             x-definition element node
     * @param xsdGroupRef       XSD group reference node
     * @return
     */
    private static SchemaNode createGroupRefNode(final XElement xElem, final XmlSchemaGroupRef xsdGroupRef) {
        return new SchemaNode(xElem.getXDPosition(), xsdGroupRef, xElem);
    }

    /**
     * Creates schema node of reference definition
     * @param systemId      definition system identified
     * @param nodePos       definition x-position
     * @param nodePath      definition path
     * @param adapterCtx    XSD adapter context
     * @return schema node
     */
    private static SchemaNode createDefNode(final String systemId, final String nodePos, final String nodePath, final XsdAdapterCtx adapterCtx) {
        final Map<String, SchemaNode> xsdSystemRefs = adapterCtx.getSchemaNodes(systemId);
        final String localName = XsdNameUtils.getReferenceName(nodePos);
        SchemaNode ref = xsdSystemRefs.get(nodePath);
        if (ref == null) {
            ref = new SchemaNode(nodePos);
            xsdSystemRefs.put(nodePath, ref);
            SchemaLogger.printG(LOG_INFO, XSD_REFERENCE, "Creating reference definition node. System=" + systemId + ", RefName=" + localName);
        } else {
            SchemaLogger.printG(LOG_DEBUG, XSD_REFERENCE, "Reference definition of node already exists. System=" + systemId + ", RefName=" + localName);
        }

        return ref;
    }

}
