package org.xdef.impl.util.conv.xd2schemas.xsd.model;

import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.utils.XmlSchemaNamed;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;
import org.xdef.model.XMNode;

import java.util.LinkedList;
import java.util.List;

import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XsdLoggerDefs.LOG_INFO;
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XsdLoggerDefs.XSD_REFERENCE;

/**
 * Couples x-definition nodes with XSD nodes. Saves binding between element references.
 * Nodes are created in transformation phase and used for advanced post-processing.
 *
 * Supported types of x-definition nodes:
 *      element ({@link XElement})
 *      attribute ({@link org.xdef.impl.XData}, kind {@link XNode.XMATTRIBUTE})
 *
 * Supported types of XSd nodes:
 *      element ({@link XmlSchemaElement})
 *      attribute ({@link XmlSchemaAttribute})
 *      complex-type ({@link XmlSchemaComplexType})
 */
public class SchemaNode {

    /**
     * X-definition position of node
     */
    private final String xdPosition;

    /**
     * X-definition node
     */
    private XMNode xdNode;

    /**
     * XSD schema element
     */
    private XmlSchemaNamed xsdNode;

    /**
     * Referenced node
     */
    private SchemaNode reference = null;

    /**
     * Nodes which has reference to this node
     */
    private List<SchemaNode> pointers = null;

    public SchemaNode(String name) {
        this.xdPosition = name;
    }

    public SchemaNode(String name, XmlSchemaNamed xsdNode, XMNode xdNode) {
        this.xdPosition = name;
        this.xsdNode = xsdNode;
        this.xdNode = xdNode;
    }

    public String getXdPosition() {
        return xdPosition;
    }

    public XmlSchemaNamed getXsdNode() {
        return xsdNode;
    }

    public void setXsdNode(XmlSchemaNamed xsdNode) {
        this.xsdNode = xsdNode;
    }

    public XMNode getXdNode() {
        return xdNode;
    }

    public SchemaNode getReference() {
        return reference;
    }

    public void setReference(SchemaNode reference) {
        this.reference = reference;
    }

    public List<SchemaNode> getPointers() {
        return pointers;
    }

    public void addRef(SchemaNode ref) {
        if (pointers == null) {
            pointers = new LinkedList<SchemaNode>();
        }

        pointers.add(ref);
    }

    public void copy(SchemaNode src) {
        this.xsdNode = src.xsdNode;
        this.xdNode = src.xdNode;
    }

    public boolean isXsdElem() {
        return (xsdNode instanceof XmlSchemaElement);
    }

    public boolean isXsdAttr() {
        return (xsdNode instanceof XmlSchemaAttribute);
    }

    public boolean isXsdComplexType() {
        return (xsdNode instanceof XmlSchemaComplexType);
    }

    public XmlSchemaElement toXsdElem() {
        return (XmlSchemaElement)xsdNode;
    }

    public XmlSchemaAttribute toXsdAttr() {
        return (XmlSchemaAttribute)xsdNode;
    }

    public XmlSchemaComplexType toXsdComplex() {
        return (XmlSchemaComplexType)xsdNode;
    }

    public boolean isXdElem() {
        return xdNode != null && xdNode.getKind() == XNode.XMELEMENT;
    }

    public boolean isXdAttr() {
        return xdNode != null && xdNode.getKind() == XNode.XMATTRIBUTE;
    }

    public XElement toXdElem() {
        return (XElement)xdNode;
    }

    public String getXdName() {
        return xdNode.getName();
    }

    public boolean hasAnyPointer() {
        return pointers != null && !pointers.isEmpty();
    }

    public boolean hasReference() {
        return reference != null;
    }

    public static void createBinding(SchemaNode ref, SchemaNode def) {
        XsdLogger.printG(LOG_INFO, XSD_REFERENCE, "Creating binding between nodes. From=" + ref.getXdPosition() + ", To=" + def.getXdPosition());

        ref.setReference(def);
        def.addRef(ref);
    }
}
