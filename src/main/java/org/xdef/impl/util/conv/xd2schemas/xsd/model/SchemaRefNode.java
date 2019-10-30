package org.xdef.impl.util.conv.xd2schemas.xsd.model;

import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.utils.XmlSchemaNamed;
import org.xdef.impl.XElement;
import org.xdef.model.XMNode;

import java.util.LinkedList;
import java.util.List;

public class SchemaRefNode {

    private final String name;

    /**
     *
     */
    private XmlSchemaNamed xsdNode;
    /**
     * X-reference node
     * Instance of XElement
     */
    private XMNode xdNode;

    private SchemaRefNode reference;
    private List<SchemaRefNode> pointers = null;

    public SchemaRefNode(String name) {
        this.name = name;
    }

    public SchemaRefNode(String name, XmlSchemaNamed xsdNode, XMNode xdNode) {
        this.name = name;
        this.xsdNode = xsdNode;
        this.xdNode = xdNode;
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

    public SchemaRefNode getReference() {
        return reference;
    }

    public void setReference(SchemaRefNode reference) {
        this.reference = reference;
    }

    public List<SchemaRefNode> getPointers() {
        return pointers;
    }

    public void addRef(SchemaRefNode ref) {
        if (pointers == null) {
            pointers = new LinkedList<SchemaRefNode>();
        }

        pointers.add(ref);
    }

    public void copy(SchemaRefNode src) {
        this.xsdNode = src.xsdNode;
        this.xdNode = src.xdNode;
    }

    public boolean isElem() {
        return (xsdNode instanceof XmlSchemaElement);
    }

    public XmlSchemaElement toXsdElem() {
        return (XmlSchemaElement)xsdNode;
    }

    public boolean hasAnyPointer() {
        return pointers != null && !pointers.isEmpty();
    }

    public XElement toXdElem() {
        return (XElement)xdNode;
    }
}
