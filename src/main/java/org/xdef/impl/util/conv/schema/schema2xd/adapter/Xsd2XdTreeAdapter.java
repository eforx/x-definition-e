package org.xdef.impl.util.conv.schema.schema2xd.adapter;

import javafx.util.Pair;
import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.constants.Constants;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;
import org.w3c.dom.Element;
import org.xdef.impl.util.conv.schema.util.SchemaLogger;
import org.xdef.impl.util.conv.schema.schema2xd.factory.XdAttributeFactory;
import org.xdef.impl.util.conv.schema.schema2xd.factory.XdDeclarationFactory;
import org.xdef.impl.util.conv.schema.schema2xd.factory.XdElementFactory;
import org.xdef.impl.util.conv.schema.schema2xd.factory.declaration.IDeclarationTypeFactory;
import org.xdef.impl.util.conv.schema.schema2xd.model.XdAdapterCtx;
import org.xdef.impl.util.conv.schema.schema2xd.util.XdNamespaceUtils;
import org.xdef.impl.util.conv.schema.schema2xd.util.Xsd2XdUtils;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;

import static org.xdef.impl.util.conv.schema.schema2xd.definition.Xsd2XdDefinitions.XD_ATTR_TEXT;
import static org.xdef.impl.util.conv.schema.util.SchemaLoggerDefs.*;
import static org.xdef.impl.util.conv.schema.schema2xd.definition.Xsd2XdFeature.XD_TEXT_REQUIRED;
import static org.xdef.impl.util.conv.schema.xd2schema.definition.AlgPhase.PREPROCESSING;
import static org.xdef.impl.util.conv.schema.xd2schema.definition.AlgPhase.TRANSFORMATION;
import static org.xdef.impl.util.conv.schema.xd2schema.definition.Xd2XsdDefinitions.XSD_NAMESPACE_PREFIX_EMPTY;

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
     * X-definition XML element factory
     */
    final private XdElementFactory xdFactory;

    /**
     * XSD adapter context
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

    /**
     * X-definition target namespace prefix
     */
    private String targetNsPrefix;

    public Xsd2XdTreeAdapter(String xDefName, XmlSchema schema, XdElementFactory xdFactory, XdAdapterCtx adapterCtx) {
        this.xDefName = xDefName;
        this.schema = schema;
        this.xdFactory = xdFactory;
        this.adapterCtx = adapterCtx;
        xdDeclarationFactory = new XdDeclarationFactory(schema, xdFactory);
        xdAttrFactory = new XdAttributeFactory(adapterCtx, xdDeclarationFactory);
    }

    public String loadXsdRootNames(Map<QName, XmlSchemaElement> rootElements) {
        SchemaLogger.print(LOG_INFO, PREPROCESSING, xDefName, "Loading root elements of XSD");

        if (rootElements == null) {
            return "";
        }

        targetNsPrefix = "";
        final Pair<String, String> targetNamespace = adapterCtx.getTargetNamespace(xDefName);
        if (targetNamespace != null && targetNamespace.getKey() != null && !targetNamespace.getKey().isEmpty()) {
            targetNsPrefix = targetNamespace.getKey() + ":";
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

    public void convertTree(final XmlSchemaObjectBase xsdNode, Element parentNode) {
        if (xsdNode instanceof XmlSchemaElement) {
            createElement((XmlSchemaElement)xsdNode, parentNode);
        } else if (xsdNode instanceof XmlSchemaType) {
            if (xsdNode instanceof XmlSchemaSimpleType) {
                parentNode.appendChild(xdDeclarationFactory.createTopDeclaration((XmlSchemaSimpleType) xsdNode));
            } else if (xsdNode instanceof XmlSchemaComplexType) {
                createTopNonRootElement((XmlSchemaComplexType)xsdNode, parentNode);
            }
        } else if (xsdNode instanceof XmlSchemaGroupParticle) {
            createGroupParticle((XmlSchemaGroupParticle)xsdNode, parentNode, false);
        } else if (xsdNode instanceof XmlSchemaGroup) {
            createGroup((XmlSchemaGroup)xsdNode, parentNode);
        } else if (xsdNode instanceof XmlSchemaGroupRef) {
            createGroupRef((XmlSchemaGroupRef)xsdNode, parentNode);
        } else if (xsdNode instanceof XmlSchemaAny) {
            createAny((XmlSchemaAny)xsdNode, parentNode);
        }
    }

    private void createElement(final XmlSchemaElement xsdElementNode, Element parentNode) {
        SchemaLogger.printP(LOG_INFO, TRANSFORMATION, xsdElementNode, "Creating element ...");
        final Element xdElem = xdFactory.createElement(xsdElementNode, xDefName);

        final QName xsdElemQName = xsdElementNode.getSchemaTypeName();
        if (xsdElemQName != null && XSD_NAMESPACE_PREFIX_EMPTY.equals(xsdElemQName.getPrefix())) {
            // TODO: Make reference if possible!
//            final String xDefRefName = Xsd2XdUtils.getReferenceSchemaName(schema.getParent(), xsdElemQName, adapterCtx, false);
//            if (xDefRefName != null) {
//                final Element xdTextRefElem = xdFactory.createTextRef();
//                if (!externalRef(xsdElemQName, xDefRefName, xdTextRefElem)) {
//                    Xsd2XdUtils.addAttrRef(xdTextRefElem, xsdElemQName);
//                }
//                xdElem.appendChild(xdTextRefElem);
//            } else
                {

                if (xsdElementNode.getSchemaType() != null) {
                    if (xsdElementNode.getSchemaType() instanceof XmlSchemaComplexType) {
                        SchemaLogger.printP(LOG_INFO, TRANSFORMATION, xsdElementNode, "Element is referencing to complex type. Reference=" + xsdElemQName);
                        if (!externalRef(xsdElemQName, xdElem, false)) {
                            xdAttrFactory.addAttrRef(xdElem, xsdElemQName);
                        }

                        final XmlSchemaComplexType complexType = XdNamespaceUtils.getReferenceComplexType(schema.getParent(), xsdElemQName);
                        if (complexType != null && complexType.isMixed()) {
                            xdAttrFactory.addAttrText(xdElem);
                        }
                    } else if (xsdElementNode.getSchemaType() instanceof XmlSchemaSimpleType) {
                        SchemaLogger.printP(LOG_INFO, TRANSFORMATION, xsdElementNode, "Element is referencing to simple type. Reference=" + xsdElemQName);
                        final XmlSchemaSimpleType simpleType = (XmlSchemaSimpleType) xsdElementNode.getSchemaType();
                        if (simpleType.getContent() instanceof XmlSchemaSimpleTypeRestriction) {
                            final QName baseType = ((XmlSchemaSimpleTypeRestriction) simpleType.getContent()).getBaseTypeName();
                            if (baseType != null) {
                                final Element xdTextRefElem = xdFactory.createTextRef();
                                if (externalRef(baseType, xdTextRefElem, true)) {
                                    xdElem.appendChild(xdTextRefElem);
                                } else {
                                    xdElem.setTextContent(xdDeclarationFactory.create((XmlSchemaSimpleTypeRestriction) simpleType.getContent(), null, IDeclarationTypeFactory.Mode.TEXT_DECL));
                                }
                            }
                        }
                    }
                } else {
                    SchemaLogger.printP(LOG_WARN, TRANSFORMATION, xsdElementNode, "Element reference has not found! Reference=" + xsdElemQName);
                }
            }
        } else if (xsdElementNode.getSchemaType() != null) {
            if (xsdElementNode.getSchemaType() instanceof XmlSchemaComplexType) {
                createElementFromComplex(xdElem, (XmlSchemaComplexType)xsdElementNode.getSchemaType());
            } else if (xsdElementNode.getSchemaType() instanceof XmlSchemaSimpleType) {
                final XmlSchemaSimpleType simpleType = (XmlSchemaSimpleType)xsdElementNode.getSchemaType();
                if (xsdElemQName != null && (Constants.XSD_NMTOKENS.equals(xsdElemQName) || Constants.XSD_IDREFS.equals(xsdElemQName))) {
                    xdElem.setTextContent(xdDeclarationFactory.createSimpleTextDeclaration(xsdElemQName));
                } else {
                    xdElem.setTextContent(xdDeclarationFactory.createTextDeclaration(simpleType.getContent(), xsdElemQName));
                }
            }
        }

        if (xdElem != null) {
            xdAttrFactory.addOccurrence(xdElem, xsdElementNode);
            xdAttrFactory.addAttrNillable(xdElem, xsdElementNode);
        }

        parentNode.appendChild(xdElem);
    }

    private void createTopNonRootElement(final XmlSchemaComplexType xsdComplexNode, Element parentNode) {
        SchemaLogger.printP(LOG_INFO, TRANSFORMATION, xsdComplexNode, "Creating top level non-root element ...");
        final Element xdElem = xdFactory.createEmptyElement(xsdComplexNode, xDefName);
        createElementFromComplex(xdElem, xsdComplexNode);
        parentNode.appendChild(xdElem);
    }

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

    private void createGroup(final XmlSchemaGroup xsdGroupNode, Element parentNode) {
        SchemaLogger.printP(LOG_DEBUG, TRANSFORMATION, xsdGroupNode, "Creating group.");

        final Element group = xdFactory.createEmptyNamedMixed(xsdGroupNode.getName());
        if (xsdGroupNode.getParticle() != null) {
            createGroupParticle(xsdGroupNode.getParticle(), group, false);
        }
        parentNode.appendChild(group);
    }

    private void createGroupRef(final XmlSchemaGroupRef xsdGroupRefNode, Element parentNode) {
        SchemaLogger.printP(LOG_DEBUG, TRANSFORMATION, xsdGroupRefNode, "Creating group reference.");

        // TODO: mixed ref cannot be part of sequence/choice/mixed? requires advanced processing
        final XmlSchemaGroup group = Xsd2XdUtils.getGroupByQName(schema, xsdGroupRefNode.getRefName());
        if (group != null) {
            // Copy group content into element
            convertTree(group.getParticle(), parentNode);
            xdAttrFactory.addOccurrence((Element) parentNode.getLastChild(), xsdGroupRefNode);
        } else {
            final Element groupRef = xdFactory.createEmptyMixed();
            XdAttributeFactory.addAttrRef(groupRef, xsdGroupRefNode.getRefName());
            SchemaLogger.printP(LOG_ERROR, TRANSFORMATION, xsdGroupRefNode, "Group reference possible inside sequence/choice/mixed node");
            if (xsdGroupRefNode.getMaxOccurs() > 1) {
                SchemaLogger.printP(LOG_ERROR, TRANSFORMATION, xsdGroupRefNode, "Group reference is using multiple occurence - prohibited in x-definition.");
            }

            parentNode.appendChild(groupRef);
        }
    }

    private void createAny(final XmlSchemaAny xsdAnyNode, Element parentNode) {
        SchemaLogger.printP(LOG_DEBUG, TRANSFORMATION, xsdAnyNode, "Creating any.");
        final Element xdAny = xdFactory.createEmptyAny();
        xdAttrFactory.addOccurrence(xdAny, xsdAnyNode);
        parentNode.appendChild(xdAny);
    }

    private void addAttrsToElem(final Element xdElem, final List<XmlSchemaAttributeOrGroupRef> xsdAttrs) {
        if (xsdAttrs != null) {
            for (XmlSchemaAttributeOrGroupRef xsdAttrRef : xsdAttrs) {
                if (xsdAttrRef instanceof XmlSchemaAttribute) {
                    final XmlSchemaAttribute xsdAttr = (XmlSchemaAttribute)xsdAttrRef;
                    final String attribute = xdAttrFactory.createAttribute(xsdAttr);
                    XdAttributeFactory.addAttr(xdElem, xsdAttr, attribute, xDefName, adapterCtx);
                }
            }
        }
    }

    private boolean externalRef(final QName baseType, final Element xdNode, final boolean simple) {
        if (baseType.getNamespaceURI() != null && !baseType.getNamespaceURI().equals(schema.getTargetNamespace())) {
            final String xDefRefName = XdNamespaceUtils.getReferenceSchemaName(schema.getParent(), baseType, adapterCtx, simple);
            return externalRef(baseType, xDefRefName, xdNode);
        }

        return false;
    }

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
