package org.xdef.impl.util.conv.schema.schema2xd.xsd.adapter;

import javafx.util.Pair;
import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.constants.Constants;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;
import org.w3c.dom.Element;
import org.xdef.impl.util.conv.schema.schema2xd.xsd.factory.XdDeclarationBuilder;
import org.xdef.impl.util.conv.schema.util.SchemaLogger;
import org.xdef.impl.util.conv.schema.schema2xd.xsd.factory.XdAttributeFactory;
import org.xdef.impl.util.conv.schema.schema2xd.xsd.factory.XdDeclarationFactory;
import org.xdef.impl.util.conv.schema.schema2xd.xsd.factory.XdNodeFactory;
import org.xdef.impl.util.conv.schema.schema2xd.xsd.factory.declaration.IDeclarationTypeFactory;
import org.xdef.impl.util.conv.schema.schema2xd.xsd.model.XdAdapterCtx;
import org.xdef.impl.util.conv.schema.schema2xd.xsd.util.XdNamespaceUtils;
import org.xdef.impl.util.conv.schema.schema2xd.xsd.util.Xsd2XdUtils;
import org.xdef.msg.XSD;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;

import static org.xdef.impl.util.conv.schema.util.SchemaLoggerDefs.*;
import static org.xdef.impl.util.conv.schema.schema2xd.xsd.definition.Xsd2XdFeature.XD_TEXT_REQUIRED;
import static org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.AlgPhase.PREPROCESSING;
import static org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.AlgPhase.TRANSFORMATION;
import static org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.Xd2XsdDefinitions.XSD_NAMESPACE_PREFIX_EMPTY;

/**
 * Transforms XSD tree node structure to x-definition tree node structure
 */
public class Xsd2XdTreeAdapter {

    /**
     * Output x-definition name
     */
    final private String xDefName;

    /**
     * Input schema used for transformation
     */
    private final XmlSchema schema;

    /**
     * X-definition XML node factory
     */
    final private XdNodeFactory xdFactory;

    /**
     * X-definition adapter context
     */
    final private XdAdapterCtx adapterCtx;

    /**
     * X-definition XML attribute factory
     */
    final private XdAttributeFactory xdAttrFactory;

    /**
     * X-definition XML declaration factory
     */
    final private XdDeclarationFactory xdDeclarationFactory;

    public Xsd2XdTreeAdapter(String xDefName, XmlSchema schema, XdNodeFactory xdFactory, XdAdapterCtx adapterCtx) {
        this.xDefName = xDefName;
        this.schema = schema;
        this.xdFactory = xdFactory;
        this.adapterCtx = adapterCtx;
        xdDeclarationFactory = new XdDeclarationFactory(schema, xdFactory, adapterCtx);
        xdAttrFactory = new XdAttributeFactory(adapterCtx, xdDeclarationFactory);
    }

    /**
     * Gathers names of all XSD top level element nodes
     * @return concatenate names in required format of x-definition
     */
    public String loadXsdRootElementNames() {
        SchemaLogger.print(LOG_INFO, PREPROCESSING, xDefName, "Loading root elements of XSD");

        String targetNsPrefix = "";
        final Pair<String, String> targetNamespace = adapterCtx.findTargetNamespace(xDefName);
        if (targetNamespace != null && targetNamespace.getKey() != null && !targetNamespace.getKey().isEmpty()) {
            targetNsPrefix = targetNamespace.getKey() + ":";
        }

        final Map<QName, XmlSchemaElement> rootElements = schema.getElements();
        if (rootElements == null) {
            return "";
        }

        final StringBuilder rootElemSb = new StringBuilder();
        for (XmlSchemaElement xsdElem : rootElements.values()) {
            if (rootElemSb.length() == 0) {
                rootElemSb.append(targetNsPrefix + xsdElem.getName());
            } else {
                rootElemSb.append(" | " + targetNsPrefix + xsdElem.getName());
            }
        }

        return rootElemSb.toString();
    }

    /**
     * Transforms XSD node tree into x-definition node tree.
     * @param xsdNode       XSD document node
     * @param parentNode    parent x-definition node
     */
    public void convertTree(final XmlSchemaObjectBase xsdNode, final Element parentNode) {
        if (xsdNode instanceof XmlSchemaElement) {
            createElement((XmlSchemaElement)xsdNode, parentNode);
        } else if (xsdNode instanceof XmlSchemaType) {
            if (xsdNode instanceof XmlSchemaSimpleType) {
                XdDeclarationBuilder builder = xdDeclarationFactory.createBuilder()
                        .setSimpleType((XmlSchemaSimpleType)xsdNode)
                        .setParentNode(parentNode)
                        .setType(IDeclarationTypeFactory.Type.TOP_DECL);

                xdDeclarationFactory.createDeclaration(builder);
            } else if (xsdNode instanceof XmlSchemaComplexType) {
                createTopNonRootElement((XmlSchemaComplexType)xsdNode, parentNode);
            }
        } else if (xsdNode instanceof XmlSchemaGroupParticle) {
            createGroupParticle((XmlSchemaGroupParticle)xsdNode, parentNode, false);
        } else if (xsdNode instanceof XmlSchemaGroup) {
            createElementGroup((XmlSchemaGroup)xsdNode, parentNode);
        } else if (xsdNode instanceof XmlSchemaGroupRef) {
            createElementGroupRef((XmlSchemaGroupRef)xsdNode, parentNode);
        } else if (xsdNode instanceof XmlSchemaAny) {
            createAny((XmlSchemaAny)xsdNode, parentNode);
        }
    }

    /**
     * Creates x-definition element node based od XSD element node
     * @param xsdElementNode    XSD element node
     * @param parentNode        parent x-definition node
     */
    private void createElement(final XmlSchemaElement xsdElementNode, final Element parentNode) {
        SchemaLogger.printP(LOG_INFO, TRANSFORMATION, xsdElementNode, "Creating element ...");
        final Element xdElem = xdFactory.createElement(xsdElementNode, xDefName);

        final QName xsdElemQName = xsdElementNode.getSchemaTypeName();
        if (xsdElemQName != null && XSD_NAMESPACE_PREFIX_EMPTY.equals(xsdElemQName.getPrefix())) {
            if (xsdElementNode.getSchemaType() != null) {
                if (xsdElementNode.getSchemaType() instanceof XmlSchemaComplexType) {
                    SchemaLogger.printP(LOG_INFO, TRANSFORMATION, xsdElementNode, "Element is referencing to complex type. Reference=" + xsdElemQName);
                    if (!externalRef(xsdElemQName, xdElem, false)) {
                        xdAttrFactory.addAttrRef(xdElem, xsdElemQName);
                    }
                } else if (xsdElementNode.getSchemaType() instanceof XmlSchemaSimpleType) {
                    SchemaLogger.printP(LOG_INFO, TRANSFORMATION, xsdElementNode, "Element is referencing to simple type. Reference=" + xsdElemQName);
                    final XmlSchemaSimpleType simpleType = (XmlSchemaSimpleType) xsdElementNode.getSchemaType();
                    if (simpleType.getContent() instanceof XmlSchemaSimpleTypeRestriction) {
                        final XdDeclarationBuilder b = xdDeclarationFactory.createBuilder()
                                .setSimpleType(simpleType)
                                .setBaseType(xsdElemQName)
                                .setType(IDeclarationTypeFactory.Type.TEXT_DECL);

                        xdElem.setTextContent(xdDeclarationFactory.createDeclarationContent(b));
                    }
                }
            } else {
                adapterCtx.getReportWriter().warning(XSD.XSD211, xsdElemQName);
                SchemaLogger.printP(LOG_WARN, TRANSFORMATION, xsdElementNode, "Element reference has not found! Reference=" + xsdElemQName);
            }
        } else if (xsdElementNode.getSchemaType() != null) {
            if (xsdElementNode.getSchemaType() instanceof XmlSchemaComplexType) {
                createElementFromComplex(xdElem, (XmlSchemaComplexType)xsdElementNode.getSchemaType());
            } else if (xsdElementNode.getSchemaType() instanceof XmlSchemaSimpleType) {
                final XmlSchemaSimpleType simpleType = (XmlSchemaSimpleType)xsdElementNode.getSchemaType();
                if (xsdElemQName != null && (Constants.XSD_NMTOKENS.equals(xsdElemQName) || Constants.XSD_IDREFS.equals(xsdElemQName))) {
                    xdElem.setTextContent(xdDeclarationFactory.createSimpleTextDeclaration(xsdElemQName));
                } else {
                    final XdDeclarationBuilder b = xdDeclarationFactory.createBuilder()
                            .setSimpleType(simpleType)
                            .setBaseType(xsdElemQName)
                            .setType(IDeclarationTypeFactory.Type.TEXT_DECL);

                    xdElem.setTextContent(xdDeclarationFactory.createDeclarationContent(b));
                }
            }
        }

        if (xdElem != null) {
            xdAttrFactory.addOccurrence(xdElem, xsdElementNode);
            xdAttrFactory.addAttrNillable(xdElem, xsdElementNode);
        }

        parentNode.appendChild(xdElem);
    }

    /**
     * Creates x-definition element node based od XSD complex schema type node
     * This transformation is always used only for top level XSD nodes
     * @param xsdComplexNode    XSD top level complex schema type node
     * @param parentNode        parent x-definition node
     */
    private void createTopNonRootElement(final XmlSchemaComplexType xsdComplexNode, final Element parentNode) {
        SchemaLogger.printP(LOG_INFO, TRANSFORMATION, xsdComplexNode, "Creating top level non-root element ...");
        final Element xdElem = xdFactory.createEmptyElement(xsdComplexNode, xDefName);
        createElementFromComplex(xdElem, xsdComplexNode);
        parentNode.appendChild(xdElem);
    }

    /**
     * Creates x-definition element node based od XSD complex schema type node
     * @param xdElem            x-definition node, which will be filled
     * @param xsdComplexNode    XSD complex schema type node
     */
    private void createElementFromComplex(final Element xdElem, final XmlSchemaComplexType xsdComplexNode) {
        addAttrsToElem(xdElem, xsdComplexNode.getAttributes());

        if (xsdComplexNode.getParticle() != null) {
            if (xsdComplexNode.getParticle() instanceof XmlSchemaGroupParticle) {
                createGroupParticle((XmlSchemaGroupParticle)xsdComplexNode.getParticle(), xdElem, xsdComplexNode.isMixed());
            } else {
                convertTree(xsdComplexNode.getParticle(), xdElem);
            }
        }

        if (xsdComplexNode.isMixed()) {
            xdAttrFactory.addAttrText(xdElem);
        }

        if (xsdComplexNode.getContentModel() != null) {
            if (xsdComplexNode.getContentModel() instanceof XmlSchemaSimpleContent) {
                final XmlSchemaSimpleContent xsdSimpleContent = (XmlSchemaSimpleContent)xsdComplexNode.getContentModel();
                if (xsdSimpleContent.getContent() instanceof XmlSchemaSimpleContentExtension) {
                    final XmlSchemaSimpleContentExtension xsdSimpleExtension = (XmlSchemaSimpleContentExtension)xsdSimpleContent.getContent();
                    final QName baseType = xsdSimpleExtension.getBaseTypeName();
                    if (baseType != null) {
                        if (!externalRef(baseType, xdElem, false)) {
                            if (adapterCtx.hasEnableFeature(XD_TEXT_REQUIRED)) {
                                xdElem.setTextContent("required " + baseType.getLocalPart() + "()");
                            } else {
                                xdElem.setTextContent("optional " + baseType.getLocalPart() + "()");
                            }
                        }
                    }
                    addAttrsToElem(xdElem, xsdSimpleExtension.getAttributes());
                }
                // TODO: more types of extensions/restrictions?
            } else if (xsdComplexNode.getContentModel() instanceof XmlSchemaComplexContent) {
                final XmlSchemaComplexContent xsdComplexContent = (XmlSchemaComplexContent)xsdComplexNode.getContentModel();
                if (xsdComplexContent.getContent() instanceof XmlSchemaComplexContentExtension) {
                    final XmlSchemaComplexContentExtension xsdComplexExtension = (XmlSchemaComplexContentExtension)xsdComplexContent.getContent();
                    final QName baseType = xsdComplexExtension.getBaseTypeName();
                    if (baseType != null) {
                        if (!externalRef(baseType, xdElem, false)) {
                            XdAttributeFactory.addAttrRef(xdElem, baseType);
                        }
                    }

                    if (xsdComplexExtension.getParticle() != null) {
                        if (xsdComplexExtension.getParticle() instanceof XmlSchemaGroupParticle) {
                            createGroupParticle((XmlSchemaGroupParticle)xsdComplexExtension.getParticle(), xdElem, false);
                        }
                    }

                    addAttrsToElem(xdElem, xsdComplexExtension.getAttributes());
                }
                // TODO: more types of extensions/restrictions?
            }
        }
    }

    /**
     * Creates x-definition particle node based od XSD particle node
     *
     * Possible created output nodes: xd:sequence, xd:choice, xd:mixed
     *
     * @param xsdParticleNode   XSD group particle node
     * @param parentNode        x-definition node, which will be filled
     * @param mixed             flag, if attribute xd:text should be created
     */
    private void createGroupParticle(final XmlSchemaGroupParticle xsdParticleNode, final Element parentNode, final boolean mixed) {
        SchemaLogger.printP(LOG_INFO, TRANSFORMATION, xsdParticleNode, "Creating group particle ...");
        Element xdParticle = null;
        if (xsdParticleNode instanceof XmlSchemaSequence) {
            final XmlSchemaSequence xsdSequence = (XmlSchemaSequence)xsdParticleNode;
            xdParticle = xdFactory.createEmptySequence();
            final List<XmlSchemaSequenceMember> xsdSequenceMembers = xsdSequence.getItems();
            if (xsdSequenceMembers != null && !xsdSequenceMembers.isEmpty()) {
                // If xs:sequence contains only element nodes, then remove sequence from x-definition
                boolean onlyElems = true;
                for (XmlSchemaSequenceMember xsdSequenceMember : xsdSequenceMembers) {
                    if (!(xsdSequenceMember instanceof XmlSchemaElement)) {
                        onlyElems = false;
                        break;
                    }
                }

                if (onlyElems) {
                    xdParticle = parentNode;
                }

                for (XmlSchemaSequenceMember xsdSequenceMember : xsdSequenceMembers) {
                    if (xsdSequenceMember instanceof XmlSchemaParticle) {
                        convertTree(xsdSequenceMember, xdParticle);
                    }
                }

                if (onlyElems) {
                    xdParticle = null;
                }
            }
        } else if (xsdParticleNode instanceof XmlSchemaChoice) {
            final XmlSchemaChoice xsdChoice = (XmlSchemaChoice)xsdParticleNode;
            xdParticle = xdFactory.createEmptyChoice();
            final List<XmlSchemaChoiceMember> xsdChoiceMembers = xsdChoice.getItems();
            if (xsdChoiceMembers != null && !xsdChoiceMembers.isEmpty()) {
                for (XmlSchemaChoiceMember xsdChoiceMember : xsdChoiceMembers) {
                    if (xsdChoiceMember instanceof XmlSchemaParticle) {
                        convertTree(xsdChoiceMember, xdParticle);
                    }
                }
            }
        } else if (xsdParticleNode instanceof XmlSchemaAll) {
            final XmlSchemaAll xsdAll = (XmlSchemaAll)xsdParticleNode;
            xdParticle = xdFactory.createEmptyMixed();
            final List<XmlSchemaAllMember> xsdAllMembers = xsdAll.getItems();
            if (xsdAllMembers != null && !xsdAllMembers.isEmpty()) {
                for (XmlSchemaAllMember xsdAllMember : xsdAllMembers) {
                    if (xsdAllMember instanceof XmlSchemaElement) {
                        convertTree(xsdAllMember, xdParticle);
                    }
                }
            }
        }

        if (xdParticle != null) {
            xdAttrFactory.addOccurrence(xdParticle, xsdParticleNode);
            if (mixed) {
                xdAttrFactory.addAttrText(xdParticle);
            }

            parentNode.appendChild(xdParticle);
        }
    }

    /**
     * Creates x-definition group of elements (xd:mixed) node based od XSD group node
     * @param xsdGroupNode      XSD group node
     * @param parentNode        x-definition node, which will be filled
     */
    private void createElementGroup(final XmlSchemaGroup xsdGroupNode, final Element parentNode) {
        SchemaLogger.printP(LOG_DEBUG, TRANSFORMATION, xsdGroupNode, "Creating group.");

        final Element group = xdFactory.createEmptyNamedMixed(xsdGroupNode.getName());
        if (xsdGroupNode.getParticle() != null) {
            createGroupParticle(xsdGroupNode.getParticle(), group, false);
        }

        parentNode.appendChild(group);
    }

    /**
     * Creates x-definition group reference (xd:mixed) node based od XSD group reference node
     * @param xsdGroupRefNode       XSD group reference node
     * @param parentNode            x-definition node, which will be filled
     */
    private void createElementGroupRef(final XmlSchemaGroupRef xsdGroupRefNode, final Element parentNode) {
        SchemaLogger.printP(LOG_DEBUG, TRANSFORMATION, xsdGroupRefNode, "Creating group reference.");

        // TODO: mixed ref cannot be part of sequence/choice/mixed? requires advanced processing
        final XmlSchemaGroup group = Xsd2XdUtils.findGroupByQName(schema, xsdGroupRefNode.getRefName());
        if (group != null) {
            // Copy group content into element
            convertTree(group.getParticle(), parentNode);
            xdAttrFactory.addOccurrence((Element) parentNode.getLastChild(), xsdGroupRefNode);
        } else {
            final Element groupRef = xdFactory.createEmptyMixed();
            XdAttributeFactory.addAttrRef(groupRef, xsdGroupRefNode.getRefName());
            adapterCtx.getReportWriter().warning(XSD.XSD212);
            SchemaLogger.printP(LOG_WARN, TRANSFORMATION, xsdGroupRefNode, "Group reference possible inside sequence/choice/mixed node");
            if (xsdGroupRefNode.getMaxOccurs() > 1) {
                adapterCtx.getReportWriter().error(XSD.XSD203);
                SchemaLogger.printP(LOG_ERROR, TRANSFORMATION, xsdGroupRefNode, "Group reference is using multiple occurence - prohibited in x-definition.");
            }

            parentNode.appendChild(groupRef);
        }
    }

    /**
     * Creates x-definition any node based od XSD any node
     * @param xsdAnyNode        XSD any node
     * @param parentNode        x-definition node, which will be filled
     */
    private void createAny(final XmlSchemaAny xsdAnyNode, final Element parentNode) {
        SchemaLogger.printP(LOG_DEBUG, TRANSFORMATION, xsdAnyNode, "Creating any.");
        final Element xdAny = xdFactory.createEmptyAny();
        xdAttrFactory.addOccurrence(xdAny, xsdAnyNode);
        parentNode.appendChild(xdAny);
    }

    /**
     * Transform and add given XSD attributes {@paramref xsdAttrs} to x-definition element node.
     * @param xdElem        x-definition element node
     * @param xsdAttrs      XSD attribute nodes
     */
    private void addAttrsToElem(final Element xdElem, final List<XmlSchemaAttributeOrGroupRef> xsdAttrs) {
        if (xsdAttrs != null) {
            for (XmlSchemaAttributeOrGroupRef xsdAttrRef : xsdAttrs) {
                if (xsdAttrRef instanceof XmlSchemaAttribute) {
                    xdAttrFactory.addAttr(xdElem, (XmlSchemaAttribute)xsdAttrRef, xDefName);
                }
            }
        }
    }

    /**
     * Creates x-definition attribute defining reference
     * @param baseType      reference qualified name
     * @param xdNode        x-definition element node
     * @param simple        flag, if reference is originally pointing to simple schema type in XSD document
     * @return true if reference attribute has been successfully created
     */
    private boolean externalRef(final QName baseType, final Element xdNode, final boolean simple) {
        if (baseType.getNamespaceURI() != null && !baseType.getNamespaceURI().equals(schema.getTargetNamespace())) {
            final String xDefRefName = XdNamespaceUtils.findReferenceSchemaName(schema.getParent(), baseType, adapterCtx, simple);
            return externalRef(baseType, xDefRefName, xdNode);
        }

        return false;
    }

    /**
     * Creates x-definition attribute defining reference
     * @param baseType      reference qualified name
     * @param xDefRefName   name of reference x-definition
     * @param xdNode        x-definition element node
     * @return
     */
    private boolean externalRef(final QName baseType, final String xDefRefName, final Element xdNode) {
        if (baseType.getNamespaceURI() != null && !baseType.getNamespaceURI().equals(schema.getTargetNamespace())) {
            if (xDefRefName != null && !xDefRefName.equals(xDefName)) {
                XdAttributeFactory.addAttrRefInDiffXDef(xdNode, xDefRefName, baseType);
                return true;
            }
        }

        return false;
    }
}
