package org.xdef.impl.util.conv.xd2schemas.xsd.factory;

import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.xdef.impl.XData;
import org.xdef.impl.XElement;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.SchemaNode;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.XsdAdapterCtx;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdNameUtils;

import java.util.Map;

import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XsdLoggerDefs.*;

public class SchemaNodeFactory {

    public static SchemaNode createElementNode(final XmlSchemaElement xsdElem, final XElement xDefEl) {
        return createElementNode(xDefEl.getXDPosition(), xsdElem, xDefEl);
    }

    private static SchemaNode createElementNode(final String nodePos, final XmlSchemaElement xsdElem, final XElement xDefEl) {
        return new SchemaNode(nodePos, xsdElem, xDefEl);
    }

    public static SchemaNode createAttributeNode(final XmlSchemaAttribute xsdAttr, final XData xData) {
        return createAttributeNode(xData.getXDPosition(), xsdAttr, xData);
    }

    private static SchemaNode createAttributeNode(final String nodePos, final XmlSchemaAttribute xsdAttr, final XData xData) {
        return new SchemaNode(nodePos, xsdAttr, xData);
    }

    public static void createElemRefAndDef(final XElement xDefEl, final XmlSchemaElement xsdElem,
                                           final String refSystemId, final String refNodePos, final String refNodePath,
                                           final XsdAdapterCtx adapterCtx) {
        SchemaNode node = createElementNode(xsdElem, xDefEl);
        SchemaNode nodeRef = createDef(refSystemId, refNodePos, refNodePath, adapterCtx);
        node = adapterCtx.addOrUpdateNode(node);
        SchemaNode.createBinding(node, nodeRef);
    }

    public static void createElemRefAndDef(final XElement xDefEl, final XmlSchemaElement xsdElem,
                                           final String systemId, String nodePath,
                                           final String refSystemId, final String refNodePos, final String refNodePath,
                                           final XsdAdapterCtx adapterCtx) {
        SchemaNode node = createElementNode(xsdElem, xDefEl);
        SchemaNode nodeRef = createDef(refSystemId, refNodePos, refNodePath, adapterCtx);
        node = adapterCtx.addOrUpdateNode(systemId, nodePath, node);
        SchemaNode.createBinding(node, nodeRef);
    }

    public static SchemaNode createDef(final String systemId, final String nodePos, final String nodePath, final XsdAdapterCtx adapterCtx) {
        Map<String, SchemaNode> xsdSystemRefs = adapterCtx.getSystemRefs(systemId);
        final String localName = XsdNameUtils.getReferenceName(nodePos);
        SchemaNode ref = xsdSystemRefs.get(nodePath);
        if (ref == null) {
            ref = new SchemaNode(nodePos);
            xsdSystemRefs.put(nodePath, ref);
            XsdLogger.printG(LOG_INFO, XSD_REFERENCE, "Creating reference definition node. System=" + systemId + ", RefName=" + localName);
        } else {
            XsdLogger.printG(LOG_DEBUG, XSD_REFERENCE, "Reference definition of node already exists. System=" + systemId + ", RefName=" + localName);
        }

        return ref;
    }


}
