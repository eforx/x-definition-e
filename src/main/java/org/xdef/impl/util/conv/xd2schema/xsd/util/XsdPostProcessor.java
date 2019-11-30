package org.xdef.impl.util.conv.xd2schema.xsd.util;

import javafx.util.Pair;
import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.xd2schema.xsd.definition.XD2XsdFeature;
import org.xdef.impl.util.conv.xd2schema.xsd.factory.SchemaNodeFactory;
import org.xdef.impl.util.conv.xd2schema.xsd.factory.XsdElementFactory;
import org.xdef.impl.util.conv.xd2schema.xsd.factory.XsdNameFactory;
import org.xdef.impl.util.conv.xd2schema.xsd.model.SchemaNode;
import org.xdef.impl.util.conv.xd2schema.xsd.model.XsdAdapterCtx;
import org.xdef.impl.util.conv.xd2schema.xsd.model.xsd.CXmlSchemaChoice;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.xdef.impl.util.conv.xd2schema.xsd.definition.AlgPhase.POSTPROCESSING;
import static org.xdef.impl.util.conv.xd2schema.xsd.definition.AlgPhase.TRANSFORMATION;
import static org.xdef.impl.util.conv.xd2schema.xsd.definition.XsdLoggerDefs.*;

/**
 * All partial transforming algorithms for post processing of nodes structures and linking
 */
public class XsdPostProcessor {

    private final XsdAdapterCtx adapterCtx;

    public XsdPostProcessor(XsdAdapterCtx adapterCtx) {
        this.adapterCtx = adapterCtx;
    }

    /**
     * Updates XSD references which are currently breaking XSD schema rules
     */
    public void processRefs() {
        XsdLogger.print(LOG_INFO, POSTPROCESSING, XSD_PP_PROCESOR,"*** Updating references ***");

        final List<SchemaNode> nodesToRemove = new ArrayList<SchemaNode>();

        for (Map.Entry<String, Map<String, SchemaNode>> systemRefEntry : adapterCtx.getNodes().entrySet()) {
            XsdLogger.print(LOG_INFO, POSTPROCESSING, XSD_PP_PROCESOR,"Updating references. System=" + systemRefEntry.getKey());

            final XmlSchema xmlSchema = adapterCtx.findSchema(systemRefEntry.getKey(), true, POSTPROCESSING);
            final XsdElementFactory xsdFactory = new XsdElementFactory(xmlSchema, adapterCtx);
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
            XD2XsdUtils.removeNode(node.toXsdElem().getParent(), node.toXsdElem());
        }
    }

    /**
     * Decomposition of XSD top-level qualified root element which is referenced by unqualified node
     * @param node  node to be decomposed
     */
    private void elementRootDecomposition(final SchemaNode node) {
        XsdLogger.printP(LOG_INFO, POSTPROCESSING, (XNode)node.getXdNode(), "Decomposition of root element with pointers ...");

        final XmlSchemaElement xsdElem = node.toXsdElem();

        if (xsdElem.getSchemaType() == null) {
            XsdLogger.printP(LOG_WARN, POSTPROCESSING, (XNode)node.getXdNode(), "Schema type has been expected!");
            return;
        }

        final XmlSchemaType schemaType = xsdElem.getSchemaType();
        final XElement xElem = node.toXdElem();
        final String localName = xsdElem.getName();
        String newLocalName = XsdNameFactory.createRootElemName(localName, schemaType);
        newLocalName = adapterCtx.getNameFactory().generateTopLevelName(xElem, newLocalName);
        final String elemNsUri = xsdElem.getParent().getNamespaceContext().getNamespaceURI(XsdNamespaceUtils.getNamespacePrefix(xElem.getName()));

        // Move element's schema type to top
        schemaType.setName(newLocalName);
        XD2XsdUtils.addSchemaTypeNode2TopLevel(xsdElem.getParent(), schemaType);
        node.setXsdNode(schemaType);

        xsdElem.setSchemaType(null);
        xsdElem.setSchemaTypeName(new QName(elemNsUri, newLocalName));

        SchemaNode newSchemaNode = SchemaNodeFactory.createElementNode(xsdElem, xElem);
        newSchemaNode = adapterCtx.addOrUpdateNode(newSchemaNode);
        SchemaNode.createBinding(newSchemaNode, node);

        updatePointers(node, newLocalName);
    }

    /**
     * Transform XSD top-level element node using reference to XSD complex type
     * @param node          node to be transformed
     * @param xsdFactory    XSD element factory
     */
    private void elementRootRef(final SchemaNode node, final XsdElementFactory xsdFactory) {
        final SchemaNode refNode = node.getReference();
        if (refNode == null || (refNode.isXsdComplexType() && !node.hasAnyPointer())) {
            return;
        }

        XsdLogger.printP(LOG_INFO, POSTPROCESSING, (XNode)node.getXdNode(), "Updating top-level element reference ...");

        elementTopToComplex(node, xsdFactory);

        if (isTopElement(refNode)) {
            final String systemId = XsdNamespaceUtils.getSystemIdFromXPos(refNode.getXdNode().getXDPosition());
            final XmlSchema xmlSchema = adapterCtx.findSchema(systemId, true, POSTPROCESSING);
            final XsdElementFactory refXsdFactory = new XsdElementFactory(xmlSchema, adapterCtx);
            if (refNode.toXsdElem().isRef()) {
                elementRootRef(refNode, refXsdFactory);
            } else {
                elementTopToComplex(refNode, refXsdFactory);
            }
        }
    }

    /**
     * Transform XSD element node to XSD complex type
     * @param node          node to be transformed
     * @param xsdFactory    XSD element factory
     */
    private void elementTopToComplex(final SchemaNode node, final XsdElementFactory xsdFactory) {
        XsdLogger.printP(LOG_INFO, POSTPROCESSING, (XNode)node.getXdNode(), "Converting top-level element to complex-type ...");

        final XmlSchemaElement xsdElem = node.toXsdElem();
        final XElement xElem = node.toXdElem();
        String newRefLocalName = adapterCtx.getNameFactory().findTopLevelName(xElem);
        if (newRefLocalName == null) {
            newRefLocalName = XsdNameFactory.createComplexRefName(xElem.getName());
            newRefLocalName = adapterCtx.getNameFactory().generateTopLevelName(xElem, newRefLocalName);
        }

        // Creating complex content with extension to original reference
        XmlSchemaType schemaType = null;
        if (xsdElem.getRef().getTargetQName() != null) {
            schemaType = xsdFactory.createComplexTypeWithComplexExtensionTop(newRefLocalName, xsdElem.getRef().getTargetQName());
        } else if (xsdElem.getSchemaTypeName() != null) {
            schemaType = xsdFactory.createComplexTypeWithSimpleExtensionTop(newRefLocalName, xsdElem.getSchemaTypeName());
        }

        // If element does not contain schema type, create new empty complex type
        if (schemaType == null) {
            schemaType = xsdFactory.createEmptyComplexType(true);
            if (schemaType.isTopLevel()) {

            }
            schemaType.setName(newRefLocalName);
        }

        node.setXsdNode(schemaType);

        // Remove original element from schema
        XD2XsdUtils.removeNode(xsdElem.getParent(), xsdElem);

        updatePointers(node, newRefLocalName);
    }

    /**
     * Update all elements referencing to given node which has been transformed previously
     * @param node              transformed node (be referenced)
     * @param newLocalName      new name of transformed node
     */
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

    /**
     * Check if given node is XSD top-level element node
     * @param node  XSD element node
     * @return  true if given node is XSD top-level element node
     */
    private static boolean isTopElement(final SchemaNode node) {
        return node.isXsdElem() && node.toXsdElem().isTopLevel();
    }

    /**
     * Check if given node is XSD top-level element node and has any pointer
     * @param node  XSD element node
     * @return  true if given node is XSD top-level element node and has any pointer
     */
    private static boolean isTopElementWithPtr(final SchemaNode node) {
        return node.isXsdElem() && node.toXsdElem().isTopLevel() && node.hasAnyPointer();
    }

    /**
     * Check if given node is XSD top-level qualified element node with any pointer from unqualified XSD node
     * @param node  XSD element node
     * @return  true if given node is XSD top-level qualified element node with any pointer from unqualified XSD node
     */
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

    /**
     * Updates reference type if necessary
     * @param node  XSD node
     */
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

    /**
     * Transform XSD complex type of XSD element node to be valid
     * @param complexType   XSD complex type
     * @param defEl         x-definition element node
     */
    public void elementComplexType(final XmlSchemaComplexType complexType, final XElement defEl) {
        XsdLogger.printP(LOG_DEBUG, POSTPROCESSING, defEl, "Updating complex content of element");

        if (complexType.getParticle() instanceof XmlSchemaAll) {
            final XmlSchemaChoice newGroupChoice = groupParticleAllToChoice((XmlSchemaAll)complexType.getParticle());
            if (newGroupChoice != null) {
                complexType.setParticle(newGroupChoice);
            }
        }

        // element contains simple content and particle -> XSD does not support restrictions for text if element contains elements
        // We have to use mixed attribute for root element and remove simple content
        if (adapterCtx.hasEnableFeature(XD2XsdFeature.POSTPROCESSING_MIXED)) {
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
                complexType.setAnnotation(XsdElementFactory.createAnnotation("Text content has been originally restricted by x-definition", adapterCtx));
            }
        }
    }

    /**
     * Transform given XSD group particle all to XSD group particle choice
     * @param groupParticleAll      XSD group particle all
     * @return XSD group particle choice node
     */
    private XmlSchemaChoice groupParticleAllToChoice(final XmlSchemaAll groupParticleAll) {
        if (!adapterCtx.hasEnableFeature(XD2XsdFeature.POSTPROCESSING_ALL_TO_CHOICE)) {
            return null;
        }

        boolean anyElementMultiple = false;
        boolean anyElementUnbound = false;

        for (XmlSchemaAllMember member : groupParticleAll.getItems()) {
            if (member instanceof XmlSchemaElement) {
                final XmlSchemaElement memberElem = (XmlSchemaElement) member;
                if (memberElem.getMaxOccurs() == Long.MAX_VALUE) {
                    anyElementUnbound = true;
                } else if (memberElem.getMaxOccurs() > 1) {
                    anyElementMultiple = true;
                }
            }
        }

        if (anyElementUnbound || anyElementMultiple) {
            return groupParticleAllToChoice(groupParticleAll, anyElementUnbound);
        }

        return null;
    }

    /**
     * Transform given XSD group particle all to XSD group particle choice
     * @param groupParticleAll      XSD group particle all
     * @param unbounded             flag, if member's occurrence should be calculated (otherwise will be unbounded)
     * @return  XSD group particle choice node
     */
    public XmlSchemaChoice groupParticleAllToChoice(final XmlSchemaAll groupParticleAll, boolean unbounded) {
        if (!adapterCtx.hasEnableFeature(XD2XsdFeature.POSTPROCESSING_ALL_TO_CHOICE)) {
            return null;
        }

        XsdLogger.printP(LOG_DEBUG, TRANSFORMATION, "Converting group particle xsd:all to xsd:choice ...");
        XsdLogger.printP(LOG_WARN, TRANSFORMATION, "!Lossy transformation! Node xsd:sequency/choice contains xsd:all node -> converting xsd:all node to xsd:choice!");

        final XmlSchemaChoice newGroupChoice = new XmlSchemaChoice();
        newGroupChoice.setAnnotation(XsdElementFactory.createAnnotation("Original group particle: all", adapterCtx));

        long elementMinOccursSum = 0;
        long elementMaxOccursSum = 0;

        // Calculate member occurrences
        if (!unbounded) {
            final Pair<Long, Long> memberOccurence = XD2XsdUtils.calculateGroupAllMembersOccurrence(groupParticleAll, adapterCtx);
            elementMinOccursSum = memberOccurence.getKey();
            elementMaxOccursSum = memberOccurence.getValue();
        } else {
            elementMinOccursSum = groupParticleAll.getMinOccurs();
            elementMaxOccursSum = Long.MAX_VALUE;
        }

        newGroupChoice.setMaxOccurs(elementMaxOccursSum);
        if (groupParticleAll.getMinOccurs() == 0) {
            newGroupChoice.setMinOccurs(0);
        } else {
            newGroupChoice.setMinOccurs(elementMinOccursSum);
        }
        copyAllMembersToChoice(groupParticleAll, newGroupChoice);
        return newGroupChoice;
    }

    /**
     * Transform XSD simple-type node content with empty restriction to given XSD attribute node
     * @param simpleTypeRestriction     XSD simple-type restriction node
     * @param attr                      XSD attribute node
     */
    public void simpleTypeRestrictionToAttr(final XmlSchemaSimpleTypeRestriction simpleTypeRestriction, final XmlSchemaAttribute attr) {
        if (simpleTypeRestriction.getFacets().isEmpty()) {
            attr.setSchemaTypeName(simpleTypeRestriction.getBaseTypeName());
            if (attr.getAnnotation() != null) {
                final List<XmlSchemaAnnotationItem> annotationItems = simpleTypeRestriction.getAnnotation().getItems();
                if (annotationItems != null && !annotationItems.isEmpty()) {
                    attr.getAnnotation().getItems().addAll(annotationItems);
                }
            } else {
                attr.setAnnotation(simpleTypeRestriction.getAnnotation());
            }
        }
    }

    /**
     * Copy all XSD group all members to XSD group choice
     * @param groupParticleAll      XSD group all node
     * @param schemaChoice          XSD group choice node
     */
    private void copyAllMembersToChoice(final XmlSchemaAll groupParticleAll, final XmlSchemaChoice schemaChoice) {
        XsdLogger.printP(LOG_DEBUG, TRANSFORMATION, "Converting group particle's members of xsd:all to xsd:choice");
        for (XmlSchemaAllMember member : groupParticleAll.getItems()) {
            allMemberToChoiceMember(member);
            schemaChoice.getItems().add((XmlSchemaChoiceMember) member);
        }
    }

    /**
     * Additional transformation of XSD choice member node, which has been originally transformed from XSD group all member node
     * @param member    XSD choice member node
     */
    public void allMemberToChoiceMember(final XmlSchemaObjectBase member) {
        if (!adapterCtx.hasEnableFeature(XD2XsdFeature.POSTPROCESSING_ALL_TO_CHOICE)) {
            return;
        }

        if (member instanceof XmlSchemaParticle) {
            final XmlSchemaParticle memberParticle = (XmlSchemaParticle)member;
            if (memberParticle.getMinOccurs() != 1 || memberParticle.getMaxOccurs() != 1) {
                final String minOcc = memberParticle.getMinOccurs() == Long.MAX_VALUE ? "unbounded" : String.valueOf(memberParticle.getMinOccurs());
                final String maxOcc = memberParticle.getMaxOccurs() == Long.MAX_VALUE ? "unbounded" : String.valueOf(memberParticle.getMaxOccurs());
                memberParticle.setAnnotation(XsdElementFactory.createAnnotation("Occurrence: [" + minOcc + ", " + maxOcc + "]", adapterCtx));
            }

            memberParticle.setMaxOccurs(1);
            memberParticle.setMinOccurs(1);
        }
    }

    /**
     * Transform XSD group all node to XSD group choice node
     * @param transformDirection    direction of transformation
     * @return XSD group choice node
     */
    public CXmlSchemaChoice groupParticleAllToChoice(final CXmlSchemaChoice.TransformDirection transformDirection) {
        if (!adapterCtx.hasEnableFeature(XD2XsdFeature.POSTPROCESSING_ALL_TO_CHOICE)) {
            return null;
        }
        final CXmlSchemaChoice newGroupChoice = new CXmlSchemaChoice(new XmlSchemaChoice());
        newGroupChoice.setTransformDirection(transformDirection);
        newGroupChoice.xsd().setAnnotation(XsdElementFactory.createAnnotation("Original group particle: all", adapterCtx));
        XsdLogger.printP(LOG_WARN, TRANSFORMATION, "!Lossy transformation! Node xsd:sequency/choice contains xsd:all node -> converting xsd:all node to xsd:choice!");
        return newGroupChoice;
    }
}
