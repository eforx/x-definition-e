package org.xdef.impl.util.conv.xd2schema.xsd.model;

import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.utils.XmlSchemaNamed;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.xd2schema.xsd.util.XsdLogger;
import org.xdef.impl.util.conv.xd2schema.xsd.util.XsdNameUtils;
import org.xdef.model.XMNode;

import javax.xml.namespace.QName;
import java.util.LinkedList;
import java.util.List;

import static org.xdef.impl.util.conv.xd2schema.xsd.definition.XsdLoggerDefs.LOG_INFO;
import static org.xdef.impl.util.conv.xd2schema.xsd.definition.XsdLoggerDefs.XSD_REFERENCE;

/**
 * Couples x-definition nodes with XSD nodes. Saves binding between element references.
 * Nodes are created in transformation phase and used for advanced post-processing.
 *
 * Supported types of x-definition nodes:
 *      element ({@link XElement})
 *      attribute ({@link org.xdef.impl.XData}, kind {@link XNode.XMATTRIBUTE})
 *
 * Supported types of XSD nodes:
 *      element ({@link XmlSchemaElement})
 *      attribute ({@link XmlSchemaAttribute})
 *      complex-type ({@link XmlSchemaComplexType})
 *      complex-content-extension ({@link XmlSchemaComplexContentExtension})
 *      group ({@link XmlSchemaGroup})
 */
public class SchemaNode {

    /**
     * X-definition position of node
     */
    private String xdPosition;

    /**
     * X-definition node
     */
    private XMNode xdNode;

    /**
     * XSD schema node
     */
    private XmlSchemaObjectBase xsdNode;

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

    public SchemaNode(String name, XmlSchemaObject xsdNode, XMNode xdNode) {
        this.xdPosition = name;
        this.xsdNode = xsdNode;
        this.xdNode = xdNode;
    }

    public String getXdPosition() {
        return xdPosition;
    }

    public XmlSchemaObjectBase getXsdNode() {
        return xsdNode;
    }

    /**
     * Sets XSD node and updates x-definition position
     * @param xsdNode   XSD node
     */
    public void setXsdNode(XmlSchemaObjectBase xsdNode) {
        this.xsdNode = xsdNode;

        if (xsdNode instanceof XmlSchemaNamed) {
            final XmlSchemaNamed xsdNamedNode = (XmlSchemaNamed)xsdNode;
            final QName qName = xsdNamedNode.getQName();
            if (qName != null) {
                final String nsPrefix = xsdNamedNode.getParent().getNamespaceContext().getPrefix(qName.getNamespaceURI());

                int systemDelPos = xdPosition.indexOf('#');
                if (nsPrefix != null) {
                    if (systemDelPos != -1) {
                        xdPosition = xdPosition.substring(0, systemDelPos + 1).concat(nsPrefix + ":" + xsdNamedNode.getName());
                    } else {
                        xdPosition = nsPrefix + ":" + xsdNamedNode.getName();
                    }
                } else {
                    if (systemDelPos != -1) {
                        xdPosition = xdPosition.substring(0, systemDelPos + 1).concat(xsdNamedNode.getName());
                    } else {
                        xdPosition = xsdNamedNode.getName();
                    }
                }
            }
        }
    }

    public XMNode getXdNode() {
        return xdNode;
    }

    public SchemaNode getReference() {
        return reference;
    }

    public List<SchemaNode> getPointers() {
        return pointers;
    }

    public void copyNodes(SchemaNode src) {
        this.xsdNode = src.xsdNode;
        this.xdNode = src.xdNode;
    }

    /**
     *
     * @return true if XSD node is element
     */
    public boolean isXsdElem() {
        return (xsdNode instanceof XmlSchemaElement);
    }

    /**
     *
     * @return true if XSD node is attribute
     */
    public boolean isXsdAttr() {
        return (xsdNode instanceof XmlSchemaAttribute);
    }

    /**
     *
     * @return true if XSD node is complex type
     */
    public boolean isXsdComplexType() {
        return (xsdNode instanceof XmlSchemaComplexType);
    }

    /**
     *
     * @return true if XSD node is complex content extension
     */
    public boolean isXsdComplexExt() {
        return (xsdNode instanceof XmlSchemaComplexContentExtension);
    }

    /**
     *
     * @return true if XSD node is group
     */
    public boolean isXsdGroup() {
        return (xsdNode instanceof XmlSchemaGroup);
    }

    public XmlSchemaElement toXsdElem() {
        return (XmlSchemaElement)xsdNode;
    }

    public XmlSchemaAttribute toXsdAttr() {
        return (XmlSchemaAttribute)xsdNode;
    }

    public XmlSchemaComplexType toXsdComplexType() {
        return (XmlSchemaComplexType)xsdNode;
    }

    public XmlSchemaComplexContentExtension toXsdComplexExt() {
        return (XmlSchemaComplexContentExtension)xsdNode;
    }

    public XmlSchemaGroup toXsdGroup() {
        return (XmlSchemaGroup)xsdNode;
    }

    /**
     *
     * @return true if x-definition node is element
     */
    public boolean isXdElem() {
        return xdNode != null && xdNode.getKind() == XNode.XMELEMENT;
    }

    /**
     *
     * @return true if x-definition node is attribute
     */
    public boolean isXdAttr() {
        return xdNode != null && xdNode.getKind() == XNode.XMATTRIBUTE;
    }

    public XElement toXdElem() {
        return (XElement)xdNode;
    }

    /**
     * @return x-definition node name
     */
    public String getXdName() {
        return xdNode.getName();
    }

    /**
     *
     * @return true if any node is referencing to current node
     */
    public boolean hasAnyPointer() {
        return pointers != null && !pointers.isEmpty();
    }

    /**
     *
     * @return true if node has reference
     */
    public boolean hasReference() {
        return reference != null;
    }

    /**
     * Add node which refers to current instance
     * @param ptr referencing node
     */
    private void addPointer(final SchemaNode ptr) {
        if (pointers == null) {
            pointers = new LinkedList<SchemaNode>();
        }

        pointers.add(ptr);
    }

    /**
     * Set reference definition
     * @param reference reference definition
     */
    private void setReference(final SchemaNode reference) {
        this.reference = reference;
    }

    /**
     * Creates binding between referencing node and reference definition
     * @param ptr
     * @param ref
     */
    public static void createBinding(final SchemaNode ptr, final SchemaNode ref) {
        XsdLogger.printG(LOG_INFO, XSD_REFERENCE, "Creating binding between nodes. From=" + ptr.getXdPosition() + ", To=" + ref.getXdPosition());

        ptr.setReference(ref);
        ref.addPointer(ptr);
    }


    /**
     * Get x-definition reference node path for post processing
     * @param refPos    x-definition reference node position
     * @return  position for post processing
     */
    public static String getPostProcessingReferenceNodePath(final String refPos) {
        int xdefSystemSeparatorPos = refPos.indexOf('/');
        if (xdefSystemSeparatorPos != -1) {
            return refPos.substring(xdefSystemSeparatorPos + 1);
        }

        return XsdNameUtils.getXNodePath(refPos);
    }

    /**
     * Get x-definition reference node position for post processing
     * @param systemId  XSD schema name
     * @param path
     * @return
     */
    public static String getPostProcessingNodePos(final String systemId, final String path) {
        return systemId + "#" + path;
    }
}
