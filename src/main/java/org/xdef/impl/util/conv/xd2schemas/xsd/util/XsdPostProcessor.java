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
                if (refEntry.getValue().getReference() == null) {
                    continue;
                }

                final SchemaRefNode refNode = refEntry.getValue();
                if (isTopElementRef(refNode)) {
                    elementTopLevelRef(refNode, xsdFactory);
                } else if (isTopElementWithPointers(refNode)) {
                    // TODO: reference to root element
                }

                refType(refNode);
            }
        }
    }

    private void elementTopLevelRef(final SchemaRefNode node, final XsdElementFactory xsdFactory) {
        XsdLogger.printP(LOG_INFO, POSTPROCESSING, (XNode)node.getXdNode(), "Updating top-level element reference ...");

        elementTopToComplex(node, xsdFactory, true);

        SchemaRefNode refNode = node.getReference();

        if (refNode != null) {
            if (isTopElement(refNode)) {
                final String systemId = XsdNamespaceUtils.getReferenceSystemId(refNode.getXdNode().getXDPosition());
                XmlSchema xmlSchema = XsdNamespaceUtils.getSchema(schemaCollection, systemId, true, POSTPROCESSING);
                XsdElementFactory refXsdFactory = new XsdElementFactory(xmlSchema);
                if (refNode.toXsdElem().isRef()) {
                    elementTopLevelRef(refNode, refXsdFactory);
                } else {
                    elementTopToComplex(refNode, refXsdFactory, false);
                }
            }
        }
    }

    private void elementTopToComplex(final SchemaRefNode node, final XsdElementFactory xsdFactory, boolean ref) {
        XsdLogger.printP(LOG_INFO, POSTPROCESSING, (XNode)node.getXdNode(), "Converting top-level element to complex-type ...");

        final XmlSchemaElement xsdElem = (XmlSchemaElement)node.getXsdNode();
        final XElement xDefEl = (XElement)node.getXdNode();
        final String newRefLocalName = XsdNamespaceUtils.createRefLocalName(xDefEl.getName());

        // Creating complex content with extension to original reference
        XmlSchemaType schemaType;
        if (ref || xsdElem.getRef().getTargetQName() != null) {
            schemaType = xsdFactory.createComplexContentWithExtension(newRefLocalName, xsdElem.getRef().getTargetQName());
        } else {
            schemaType = xsdFactory.createComplextContentWithSimpleRestriction(newRefLocalName, xsdElem.getSchemaTypeName());
        }

        node.setXsdNode(schemaType);

        // Remove original element from schema
        xsdElem.getParent().getItems().remove(xsdElem);

        // Update all pointers to element
        if (node.getPointers() != null) {
            for (SchemaRefNode ptrNode : node.getPointers()) {
                if (ptrNode.isElem()) {
                    final XmlSchemaElement xsdPtrElem = ptrNode.toXsdElem();
                    QName ptrQName = xsdPtrElem.getRef().getTargetQName();
                    if (ptrQName != null) {
                        XsdLogger.printP(LOG_INFO, POSTPROCESSING, (XNode)node.getXdNode(), "Converting reference to type. Elem=" + ptrNode.getXdNode().getName());
                        xsdPtrElem.getRef().setTargetQName(null);
                        ptrQName = new QName(ptrQName.getNamespaceURI(), newRefLocalName);
                        xsdPtrElem.setName(ptrNode.getXdNode().getName());
                        xsdPtrElem.setSchemaTypeName(ptrQName);
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

    private static boolean isTopElementWithPointers(final SchemaRefNode node) {
        return node.isElem() && node.getXsdNode().isTopLevel() && node.getXdNode().getKind() == XNode.XMELEMENT && node.hasAnyPointer();
    }

    private static void refType(final SchemaRefNode node) {
        if (node.getReference() != null && node.getReference().isElem() && node.isElem() && node.toXsdElem().getRef().getTargetQName() == null) {
            XmlSchemaElement xsdElem = node.toXsdElem();
            if (xsdElem.getName() == null) {
                xsdElem.getRef().setTargetQName(xsdElem.getSchemaTypeName());
                xsdElem.setSchemaTypeName(null);
            } else {
                XsdLogger.printP(LOG_WARN, POSTPROCESSING, (XNode)node.getXdNode(), "Element cannot use QName, because already has a name!");
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
                //schema.getItems().remove(complexType.getContentModel());
                complexType.setContentModel(null);
                complexType.setMixed(true);
                complexType.setAnnotation(XsdElementFactory.createAnnotation("Text content has been originally restricted by x-definition"));
            }
        }
    }
}
