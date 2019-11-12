package org.xdef.impl.util.conv.xd2schemas.xsd.util;

import org.apache.ws.commons.schema.*;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.SchemaNodeFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.XsdElementFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.SchemaNode;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.XsdAdapterCtx;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.xsd.CXmlSchemaChoice;

import javax.xml.namespace.QName;
import java.util.*;

import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.AlgPhase.POSTPROCESSING;
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XsdLoggerDefs.*;

public class XsdPostProcessor {

    private final XsdAdapterCtx adapterCtx;

    public XsdPostProcessor(XsdAdapterCtx adapterCtx) {
        this.adapterCtx = adapterCtx;
    }

    public void processRefs() {
        XsdLogger.print(LOG_INFO, POSTPROCESSING, XSD_PP_PROCESOR,"*** Updating references ***");

        final List<SchemaNode> nodesToRemove = new ArrayList<SchemaNode>();

        for (Map.Entry<String, Map<String, SchemaNode>> systemRefEntry : adapterCtx.getNodes().entrySet()) {
            XsdLogger.print(LOG_INFO, POSTPROCESSING, XSD_PP_PROCESOR,"Updating references. System=" + systemRefEntry.getKey());

            final XmlSchema xmlSchema = adapterCtx.getSchema(systemRefEntry.getKey(), true, POSTPROCESSING);
            final XsdElementFactory xsdFactory = new XsdElementFactory(xmlSchema);
            final Set<String> schemaRootNodeNames = adapterCtx.getSchemaRootNodeNames(systemRefEntry.getKey());

            for (Map.Entry<String, SchemaNode> refEntry : systemRefEntry.getValue().entrySet()) {
                final SchemaNode node = refEntry.getValue();
                if (isTopElement(node)) {
                    // Process elements which are on top level but they are not root of x-definition
                    if (!adapterCtx.isPostProcessingNamespace(xmlSchema.getTargetNamespace()) && (schemaRootNodeNames == null || !schemaRootNodeNames.contains(node.getXdName()))) {
                        if (!node.hasAnyPointer()) {
                            nodesToRemove.add(node);
                            continue;
                        } else {
                            elementTopToComplex(node, xsdFactory);
                        }
                    } else if (node.toXsdElem().isRef()) {
                        elementRootRef(node, xsdFactory);
                    }
                }

                updateRefType(node);

                if (node.getReference() == null && isQualifiedTopElementWithUnqualifiedPtr(node)) {
                    elementRootDecomposition(node);
                }
            }
        }

        for (SchemaNode node : nodesToRemove) {
            adapterCtx.removeNode((XNode)node.getXdNode());
            XD2XsdUtils.removeItem(node.toXsdElem().getParent(), node.toXsdElem());
        }
    }

    private void elementRootDecomposition(final SchemaNode node) {
        XsdLogger.printP(LOG_INFO, POSTPROCESSING, (XNode)node.getXdNode(), "Decomposition of root element with pointers ...");

        final XmlSchemaElement xsdElem = node.toXsdElem();
        final XElement xDefEl = node.toXdElem();
        final String localName = xsdElem.getName();
        final String newLocalName = XsdNameUtils.newRootElemName(localName, xsdElem.getSchemaType());
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
            SchemaNode newSchemaNode = SchemaNodeFactory.createElementNode(xsdElem, xDefEl);
            newSchemaNode = adapterCtx.addOrUpdateNode(newSchemaNode);
            SchemaNode.createBinding(newSchemaNode, node);
        }

        if (schemaType == null) {
            XsdLogger.printP(LOG_WARN, POSTPROCESSING, (XNode)node.getXdNode(), "Schema type has been expected!");
            return;
        }

        updatePointers(node, newLocalName);
    }

    private void elementRootRef(final SchemaNode node, final XsdElementFactory xsdFactory) {
        SchemaNode refNode = node.getReference();
        if (refNode == null || (refNode.isXsdComplexType() && !node.hasAnyPointer())) {
            return;
        }

        XsdLogger.printP(LOG_INFO, POSTPROCESSING, (XNode)node.getXdNode(), "Updating top-level element reference ...");

        elementTopToComplex(node, xsdFactory);

        if (isTopElement(refNode)) {
            final String systemId = XsdNamespaceUtils.getReferenceSystemId(refNode.getXdNode().getXDPosition());
            XmlSchema xmlSchema = adapterCtx.getSchema(systemId, true, POSTPROCESSING);
            XsdElementFactory refXsdFactory = new XsdElementFactory(xmlSchema);
            if (refNode.toXsdElem().isRef()) {
                elementRootRef(refNode, refXsdFactory);
            } else {
                elementTopToComplex(refNode, refXsdFactory);
            }
        }
    }

    private void elementTopToComplex(final SchemaNode node, final XsdElementFactory xsdFactory) {
        XsdLogger.printP(LOG_INFO, POSTPROCESSING, (XNode)node.getXdNode(), "Converting top-level element to complex-type ...");

        final XmlSchemaElement xsdElem = node.toXsdElem();
        final XElement xDefEl = node.toXdElem();
        String newRefLocalName = XsdNameUtils.newTopLocalRefName(xDefEl.getName());

        // Creating complex content with extension to original reference
        XmlSchemaType schemaType = null;
        if (xsdElem.getRef().getTargetQName() != null) {
            schemaType = xsdFactory.createComplexTypeWithComplexExtension(newRefLocalName, xsdElem.getRef().getTargetQName());
        } else if (xsdElem.getSchemaTypeName() != null) {
            schemaType = xsdFactory.createComplextTypeWithSimpleExtension(newRefLocalName, xsdElem.getSchemaTypeName(), true);
        }

        // If element does not contain schema type, create new empty complex type
        if (schemaType == null) {
            schemaType = xsdFactory.createEmptyComplexType(true);
            schemaType.setName(newRefLocalName);
        }

        node.setXsdNode(schemaType);

        // Remove original element from schema
        XD2XsdUtils.removeItem(xsdElem.getParent(), xsdElem);

        updatePointers(node, newRefLocalName);
    }

    private static void updatePointers(final SchemaNode node, final String newLocalName) {
        // Update all pointers to element
        if (node.getPointers() != null) {
            for (SchemaNode ptrNode : node.getPointers()) {
                if (ptrNode.isXsdElem()) {
                    final XmlSchemaElement xsdPtrElem = ptrNode.toXsdElem();
                    final QName ptrQName = xsdPtrElem.getRef().getTargetQName();
                    if (ptrQName != null) {
                        if (xsdPtrElem.getForm() == XmlSchemaForm.UNQUALIFIED) {
                            xsdPtrElem.getRef().setTargetQName(null);
                            final QName newPtrQName = new QName(ptrQName.getNamespaceURI(), newLocalName);
                            final String newPtrElemName = XsdNameUtils.getReferenceName(ptrNode.getXdName());
                            xsdPtrElem.setName(newPtrElemName);
                            xsdPtrElem.setSchemaTypeName(newPtrQName);

                            XsdLogger.printP(LOG_INFO, POSTPROCESSING, (XNode) node.getXdNode(), "Change element reference to schema type." +
                                    " Elem=" + ptrNode.getXdName() + ", NewQName=" + newPtrQName + ", OldQName=" + ptrQName);
                        }
                    }
                } else if (ptrNode.isXsdComplexExt()) {
                    final XmlSchemaComplexContentExtension xsdPtrExt = ptrNode.toXsdComplexExt();
                    final QName ptrQName = xsdPtrExt.getBaseTypeName();
                    final QName newPtrQName = new QName(ptrQName.getNamespaceURI(), newLocalName);
                    xsdPtrExt.setBaseTypeName(newPtrQName);

                    XsdLogger.printP(LOG_INFO, POSTPROCESSING, (XNode) node.getXdNode(), "Change complex extension base." +
                            " Elem=" + ptrNode.getXdName() + ", NewQName=" + newPtrQName + ", OldQName=" + ptrQName);
                }
            }
        }
    }

    private static boolean isTopElement(final SchemaNode node) {
        return node.isXsdElem() && node.toXsdElem().isTopLevel();
    }

    private static boolean isTopElementWithPtr(final SchemaNode node) {
        return node.isXsdElem() && node.toXsdElem().isTopLevel() && node.hasAnyPointer();
    }

    private static boolean isQualifiedTopElementWithUnqualifiedPtr(final SchemaNode node) {
        if (isTopElementWithPtr(node)) {
            final XmlSchemaForm nodeSchema = node.toXsdElem().getForm();
            for (SchemaNode ptr : node.getPointers()) {
                if (ptr.isXsdElem()) {
                    final XmlSchemaForm ptrSchema = ptr.toXsdElem().getForm();
                    final boolean ptrHasNs = XsdNamespaceUtils.containsNsPrefix(ptr.getXdName());
                    if (!ptrHasNs && XmlSchemaForm.UNQUALIFIED.equals(ptrSchema) && XmlSchemaForm.QUALIFIED.equals(nodeSchema)) {
                        return true;
                    }
                } else if (ptr.isXsdComplexExt()) {
                    final boolean ptrHasNs = XsdNamespaceUtils.containsNsPrefix(ptr.getXdName());
                    if (!ptrHasNs) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static void updateRefType(final SchemaNode node) {
        if (node.getReference() != null) {
            if (node.getReference().isXsdElem() && node.isXsdElem() && node.toXsdElem().getRef().getTargetQName() == null) {
                // Reference element to element
                XmlSchemaElement xsdElem = node.toXsdElem();
                xsdElem.getRef().setNamedObject(null);
                xsdElem.getRef().setTargetQName(xsdElem.getSchemaTypeName());
                xsdElem.setSchemaTypeName(null);
            } else if (node.getReference().isXsdComplexType() && node.isXsdElem() && node.toXsdElem().getTargetQName() != null) {
                // Reference element to complex type
                XmlSchemaElement xsdElem = node.toXsdElem();
                xsdElem.setSchemaTypeName(xsdElem.getTargetQName());
                xsdElem.getRef().setTargetQName(null);
                xsdElem.setName(node.getXdName());
            }
        }
    }

    public static void elementComplexType(final XmlSchemaComplexType complexType, final XNode[] xChildrenNodes, final XElement defEl) {
        XsdLogger.printP(LOG_DEBUG, POSTPROCESSING, defEl, "Updating complex content of element");

        // if xs:all contains only unbounded elements, then we can use unbounded xs:choise
        {
            boolean allElementsUnbounded = true;
            boolean anyElementMultiple = false;
            boolean anyElementUnbound = false;
            int elementMaxOccursSum = 0;

            if (complexType.getParticle() instanceof XmlSchemaAll) {

                for (XNode xNode : xChildrenNodes) {
                    if (xNode.getKind() == XNode.XMELEMENT) {
                        if (!xNode.isMaxUnlimited() && !xNode.isUnbounded()) {
                            allElementsUnbounded = false;
                            elementMaxOccursSum += xNode.maxOccurs();
                            if (xNode.maxOccurs() > 1) {
                                anyElementMultiple = true;
                            }
                        } else {
                            anyElementUnbound = true;
                        }
                    }
                }

                if (allElementsUnbounded || anyElementUnbound || anyElementMultiple) {
                    XsdLogger.printP(LOG_DEBUG, POSTPROCESSING, defEl, "Complex content contains xs:all with only unbounded elements. Update to unbounded xs:choise.");

                    final XmlSchemaChoice newGroupChoice = new XmlSchemaChoice();
                    newGroupChoice.setAnnotation(XsdElementFactory.createAnnotation("Original group particle: all"));
                    if (allElementsUnbounded || anyElementUnbound) {
                        newGroupChoice.setMaxOccurs(Long.MAX_VALUE);

                        // Copy elements
                        for (XmlSchemaAllMember member : ((XmlSchemaAll)complexType.getParticle()).getItems()) {
                            newGroupChoice.getItems().add((XmlSchemaChoiceMember) member);
                        }
                    } else {
                        newGroupChoice.setMaxOccurs(elementMaxOccursSum);
                        newGroupChoice.setMinOccurs(complexType.getParticle().getMinOccurs());

                        // Copy elements
                        for (XmlSchemaAllMember member : ((XmlSchemaAll)complexType.getParticle()).getItems()) {
                            if (member instanceof XmlSchemaElement) {
                                ((XmlSchemaElement) member).setAnnotation(XsdElementFactory.createAnnotation(
                                        new LinkedList<String>(Arrays.asList(
                                                "Minimum occurrence: " + ((XmlSchemaElement)member).getMinOccurs(),
                                                "Maximum occurrence: " + ((XmlSchemaElement)member).getMaxOccurs())
                                        )));
                                ((XmlSchemaElement) member).setMaxOccurs(1);
                                if (((XmlSchemaElement)member).getMinOccurs() > 1) {
                                    ((XmlSchemaElement) member).setMinOccurs(1);
                                }
                            }
                            newGroupChoice.getItems().add((XmlSchemaChoiceMember) member);
                        }
                    }

                    complexType.setParticle(newGroupChoice);
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
