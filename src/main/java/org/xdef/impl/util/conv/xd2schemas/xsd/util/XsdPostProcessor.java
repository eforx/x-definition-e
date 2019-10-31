package org.xdef.impl.util.conv.xd2schemas.xsd.util;

import org.apache.ws.commons.schema.*;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.XsdElementFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.SchemaRefNode;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;

import static org.xdef.impl.util.conv.xd2schemas.xsd.util.AlgPhase.POSTPROCESSING;
import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.*;

public class XsdPostProcessor {

    private final XmlSchemaCollection schemaCollection;
    private final Map<String, Map<String, SchemaRefNode>> nodeRefs;


    public XsdPostProcessor(XmlSchemaCollection schemaCollection, Map<String, Map<String, SchemaRefNode>> nodeRefs) {
        this.schemaCollection = schemaCollection;
        this.nodeRefs = nodeRefs;
    }

    public void processRefs() {
        XsdLogger.printP(LOG_INFO, POSTPROCESSING, "*** Updating references ***");

        for (Map.Entry<String, Map<String, SchemaRefNode>> systemRefEntry : nodeRefs.entrySet()) {
            XsdLogger.printP(LOG_INFO, POSTPROCESSING, "Updating references. System=" + systemRefEntry.getKey());

            XmlSchema xmlSchema = XsdNamespaceUtils.getSchema(schemaCollection, systemRefEntry.getKey(), true, POSTPROCESSING);
            XsdElementFactory xsdFactory = new XsdElementFactory(xmlSchema);

            for (Map.Entry<String, SchemaRefNode> refEntry : systemRefEntry.getValue().entrySet()) {
                final SchemaRefNode refNode = refEntry.getValue();
                if (refNode.getReference() != null) {
                    if (isTopElementRef(refNode)) {
                        elementTopLevelRef(refNode, xsdFactory);
                    }

                    refType(refNode);
                } else if (isQualifiedTopElementWithUnqualifiedPtr(refNode)) {
                    elementRootDecomposition(refNode);
                }
            }
        }
    }

    private void elementRootDecomposition(final SchemaRefNode node) {
        XsdLogger.printP(LOG_INFO, POSTPROCESSING, (XNode)node.getXdNode(), "Decomposition of root element with pointers ...");

        final XmlSchemaElement xsdElem = (XmlSchemaElement)node.getXsdNode();
        final XElement xDefEl = (XElement)node.getXdNode();
        final String localName = xsdElem.getName();
        final String newLocalName = XsdNamespaceUtils.createNewRootElemName(localName, xsdElem.getSchemaType());
        final String elemNsUri = xsdElem.getParent().getNamespaceContext().getNamespaceURI(XsdNamespaceUtils.getNamespacePrefix(xDefEl.getName()));

        // Move element's schema type to top
        XmlSchemaType schemaType = null;
        if (xsdElem.getSchemaType() != null) {
            schemaType = xsdElem.getSchemaType();
            schemaType.setName(newLocalName);
            XD2XsdUtils.addSchemaType(xsdElem.getParent(), schemaType);
            node.setXsdNode(schemaType);

            xsdElem.setSchemaType(null);
            xsdElem.setSchemaTypeName(new QName(elemNsUri, newLocalName));
            SchemaRefNode newXsdNode = XsdReferenceUtils.createElementNode(xsdElem, xDefEl);
            XsdReferenceUtils.createLink(newXsdNode, node);
            XsdReferenceUtils.addNode(newXsdNode, nodeRefs, true);
        }

        if (schemaType == null) {
            XsdLogger.printP(LOG_WARN, POSTPROCESSING, (XNode)node.getXdNode(), "Schema type has been expected!");
            return;
        }

        updatePointers(node, newLocalName);
    }

    private void elementTopLevelRef(final SchemaRefNode node, final XsdElementFactory xsdFactory) {
        XsdLogger.printP(LOG_INFO, POSTPROCESSING, (XNode)node.getXdNode(), "Updating top-level element reference ...");

        elementTopToComplex(node, xsdFactory);

        SchemaRefNode refNode = node.getReference();

        if (refNode != null) {
            if (isTopElement(refNode)) {
                final String systemId = XsdNamespaceUtils.getReferenceSystemId(refNode.getXdNode().getXDPosition());
                XmlSchema xmlSchema = XsdNamespaceUtils.getSchema(schemaCollection, systemId, true, POSTPROCESSING);
                XsdElementFactory refXsdFactory = new XsdElementFactory(xmlSchema);
                if (refNode.toXsdElem().isRef()) {
                    elementTopLevelRef(refNode, refXsdFactory);
                } else {
                    elementTopToComplex(refNode, refXsdFactory);
                }
            }
        }
    }

    private void elementTopToComplex(final SchemaRefNode node, final XsdElementFactory xsdFactory) {
        XsdLogger.printP(LOG_INFO, POSTPROCESSING, (XNode)node.getXdNode(), "Converting top-level element to complex-type ...");

        final XmlSchemaElement xsdElem = (XmlSchemaElement)node.getXsdNode();
        final XElement xDefEl = (XElement)node.getXdNode();
        String newRefLocalName = XsdNamespaceUtils.createRefLocalName(xDefEl.getName());

        // Creating complex content with extension to original reference
        XmlSchemaType schemaType = null;
        if (xsdElem.getRef().getTargetQName() != null) {
            schemaType = xsdFactory.createComplexContentWithExtension(newRefLocalName, xsdElem.getRef().getTargetQName());
        } else if (xsdElem.getSchemaTypeName() != null) {
            schemaType = xsdFactory.createComplextContentWithSimpleExtension(newRefLocalName, xsdElem.getSchemaTypeName(), true);
        }

        if (schemaType == null) {
            XsdLogger.printP(LOG_WARN, POSTPROCESSING, (XNode)node.getXdNode(), "Schema type has been expected!");
            return;
        }

        node.setXsdNode(schemaType);

        // Remove original element from schema
        XD2XsdUtils.removeItem(xsdElem.getParent(), xsdElem);

        updatePointers(node, newRefLocalName);
    }

    private static void updatePointers(final SchemaRefNode node, final String newLocalName) {
        // Update all pointers to element
        if (node.getPointers() != null) {
            for (SchemaRefNode ptrNode : node.getPointers()) {
                if (ptrNode.isElem()) {
                    final XmlSchemaElement xsdPtrElem = ptrNode.toXsdElem();
                    QName ptrQName = xsdPtrElem.getRef().getTargetQName();
                    if (ptrQName != null) {
                        XsdLogger.printP(LOG_INFO, POSTPROCESSING, (XNode) node.getXdNode(), "Converting reference to type. Elem=" + ptrNode.getXdNode().getName());
                        if (xsdPtrElem.getForm() == XmlSchemaForm.UNQUALIFIED) {
                            xsdPtrElem.getRef().setTargetQName(null);
                            ptrQName = new QName(ptrQName.getNamespaceURI(), newLocalName);
                            final String newPtrElemName = XsdNameUtils.getReferenceName(ptrNode.getXdNode().getName());
                            xsdPtrElem.setName(newPtrElemName);
                            xsdPtrElem.setSchemaTypeName(ptrQName);
                        }
                    }
                }
            }
        }
    }

    private static boolean isTopElementRef(final SchemaRefNode node) {
        return node.isElem() && node.getXsdNode().isTopLevel() && node.getXdNode().getKind() == XNode.XMELEMENT && node.toXsdElem().isRef();
    }

    private static boolean isTopElement(final SchemaRefNode node) {
        return node.isElem() && node.getXsdNode().isTopLevel() && node.getXdNode().getKind() == XNode.XMELEMENT;
    }

    private static boolean isTopElementWithPtr(final SchemaRefNode node) {
        return node.isElem() && node.getXsdNode().isTopLevel() && node.getXdNode().getKind() == XNode.XMELEMENT && node.hasAnyPointer();
    }

    private static boolean isQualifiedTopElementWithUnqualifiedPtr(final SchemaRefNode node) {
        if (isTopElementWithPtr(node)) {
            final XmlSchemaForm nodeSchema = node.toXsdElem().getForm();
            for (SchemaRefNode ptr : node.getPointers()) {
                if (ptr.isElem()) {
                    final XmlSchemaForm ptrSchema = ptr.toXsdElem().getForm();
                    final boolean ptrHasNs = XsdNamespaceUtils.hasNamespace(ptr.getXdNode().getName());
                    if (!ptrHasNs && XmlSchemaForm.UNQUALIFIED.equals(ptrSchema) && XmlSchemaForm.QUALIFIED.equals(nodeSchema)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static void refType(final SchemaRefNode node) {
        if (node.getReference() != null) {
            if (node.getReference().isElem() && node.isElem() && node.toXsdElem().getRef().getTargetQName() == null) {
                // Reference element to element
                XmlSchemaElement xsdElem = node.toXsdElem();
                if (xsdElem.getName() == null) {
                    xsdElem.getRef().setTargetQName(xsdElem.getSchemaTypeName());
                    xsdElem.setSchemaTypeName(null);
                } else {
                    XsdLogger.printP(LOG_WARN, POSTPROCESSING, (XNode) node.getXdNode(), "Element cannot use QName, because already has a name!");
                }
            } else if (node.getReference().isComplexType() && node.isElem() && node.toXsdElem().getTargetQName() != null) {
                // Reference element to complex type
                XmlSchemaElement xsdElem = node.toXsdElem();
                xsdElem.setSchemaTypeName(xsdElem.getTargetQName());
                xsdElem.getRef().setTargetQName(null);
                xsdElem.setName(node.toXdElem().getName());
            }
        }
    }

    public static void elementComplexContent(final XElement defEl, final XmlSchemaComplexType complexType) {
        XsdLogger.printP(LOG_DEBUG, POSTPROCESSING, defEl, "Updating complex content of element");

        // if xs:all contains only unbounded elements, then we can use unbounded xs:choise
        {
            boolean allElementsUnbounded = true;
            boolean anyElementUnbounded = false;

            if (complexType.getParticle() instanceof XmlSchemaAll) {

                for (XNode xNode : defEl._childNodes) {
                    if (xNode.getKind() == XNode.XMELEMENT) {
                        if (!xNode.isMaxUnlimited() && !xNode.isUnbounded()) {
                            allElementsUnbounded = false;
                        } else if (xNode.maxOccurs() > 1) {
                            anyElementUnbounded = true;
                        }
                    }
                }

                if (allElementsUnbounded) {
                    XsdLogger.printP(LOG_DEBUG, POSTPROCESSING, defEl, "Complex content contains xs:all with only unbounded elements. Update to unbounded xs:choise.");

                    XmlSchemaChoice group = new XmlSchemaChoice();
                    group.setMaxOccurs(Long.MAX_VALUE);

                    // Copy elements
                    for (XmlSchemaAllMember member : ((XmlSchemaAll)complexType.getParticle()).getItems()) {
                        group.getItems().add((XmlSchemaChoiceMember) member);
                    }

                    complexType.setParticle(group);
                } else if (anyElementUnbounded) {
                    // TODO: XD->XSD Solve?
                    XsdLogger.printP(LOG_ERROR, POSTPROCESSING, defEl, "xs:all contains element which has maxOccurs higher than 1");
                }
            }
        }

        // element contains simple content and particle -> XSD does not support restrictions for text if element contains elements
        // We have to use mixed attribute for root element and remove simple content
        {
            if (complexType.getParticle() != null && complexType.getContentModel() != null && complexType.getContentModel() instanceof XmlSchemaSimpleContent) {
                XsdLogger.printP(LOG_WARN, POSTPROCESSING, defEl, "!Lossy transformation! Remove simple content from element due to existence of complex content. Use mixed attr.");

                // Copy attributes from simple content
                XmlSchemaContent content = complexType.getContentModel().getContent();
                if (content instanceof XmlSchemaSimpleContentExtension) {
                    List attrs = ((XmlSchemaSimpleContentExtension) content).getAttributes();
                    if (attrs != null && !attrs.isEmpty()) {
                        complexType.getAttributes().addAll(attrs);
                    }
                }

                // TODO: remove by reference handler
                //XD2XsdUtils.removeItem(schema, complexType.getContentModel());
                complexType.setContentModel(null);
                complexType.setMixed(true);
                complexType.setAnnotation(XsdElementFactory.createAnnotation("Text content has been originally restricted by x-definition"));
            }
        }
    }
}
