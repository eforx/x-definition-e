package org.xdef.impl.util.conv.schema2xd.xsd.adapter;

import javafx.util.Pair;
import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.constants.Constants;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;
import org.w3c.dom.Element;
import org.xdef.impl.util.conv.schema.util.XsdLogger;
import org.xdef.impl.util.conv.schema2xd.xsd.factory.XdAttributeFactory;
import org.xdef.impl.util.conv.schema2xd.xsd.factory.XdDeclarationFactory;
import org.xdef.impl.util.conv.schema2xd.xsd.factory.XdElementFactory;
import org.xdef.impl.util.conv.schema2xd.xsd.factory.declaration.IDeclarationTypeFactory;
import org.xdef.impl.util.conv.schema2xd.xsd.model.XdAdapterCtx;
import org.xdef.impl.util.conv.schema2xd.xsd.util.Xsd2XdUtils;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.xdef.impl.util.conv.schema.util.XsdLoggerDefs.*;
import static org.xdef.impl.util.conv.schema2xd.xsd.definition.Xsd2XdFeature.XD_TEXT_REQUIRED;
import static org.xdef.impl.util.conv.xd2schema.xsd.definition.AlgPhase.PREPROCESSING;
import static org.xdef.impl.util.conv.xd2schema.xsd.definition.AlgPhase.TRANSFORMATION;
import static org.xdef.impl.util.conv.xd2schema.xsd.definition.XD2XsdDefinitions.XSD_NAMESPACE_PREFIX_EMPTY;

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
        xdAttrFactory = new XdAttributeFactory(adapterCtx);
        xdDeclarationFactory = new XdDeclarationFactory(schema, xdFactory, adapterCtx);
    }

    public String loadXsdRootNames(Map<QName, XmlSchemaElement> rootElements) {
        XsdLogger.print(LOG_INFO, PREPROCESSING, xDefName, "Loading root elements of XSD");

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
            createGroupParticle((XmlSchemaGroupParticle)xsdNode, parentNode);
        } else if (xsdNode instanceof XmlSchemaGroup) {
            createGroup((XmlSchemaGroup)xsdNode, parentNode);
        } else if (xsdNode instanceof XmlSchemaGroupRef) {
            createGroupRef((XmlSchemaGroupRef)xsdNode, parentNode);
        } else if (xsdNode instanceof XmlSchemaAny) {
            createAny((XmlSchemaAny)xsdNode, parentNode);
        }
    }

    private void createElement(final XmlSchemaElement xsdElementNode, Element parentNode) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xsdElementNode, "Creating element ...");
        final Element xdElem = xdFactory.createElement(xsdElementNode, xDefName);
        final QName xsdElemQName = xsdElementNode.getSchemaTypeName();
        if (xsdElemQName != null && XSD_NAMESPACE_PREFIX_EMPTY.equals(xsdElemQName.getPrefix())) {
            // TODO: Make reference if possible!
//            final String xDefRefName = Xsd2XdUtils.getReferenceSchemaName(schema.getParent(), xsdElemQName, adapterCtx, false);
//            if (xDefRefName != null) {
//                final Element xdTextRefElem = xdFactory.createTextRef();
//                if (!externalRef(xsdElemQName, xDefRefName, xdTextRefElem)) {
//                    Xsd2XdUtils.addRefAttribute(xdTextRefElem, xsdElemQName);
//                }
//                xdElem.appendChild(xdTextRefElem);
//            } else
                {

                if (xsdElementNode.getSchemaType() != null) {
                    if (xsdElementNode.getSchemaType() instanceof XmlSchemaComplexType) {
                        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xsdElementNode, "Element is referencing to complex type. Reference=" + xsdElemQName);
                        if (!externalRef(xsdElemQName, xdElem, false)) {
                            Xsd2XdUtils.addRefAttribute(xdElem, xsdElemQName);
                        }
                    } else if (xsdElementNode.getSchemaType() instanceof XmlSchemaSimpleType) {
                        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xsdElementNode, "Element is referencing to simple type. Reference=" + xsdElemQName);
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
                    XsdLogger.printP(LOG_WARN, TRANSFORMATION, xsdElementNode, "Element reference has not found! Reference=" + xsdElemQName);
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
            xdAttrFactory.addNillable(xdElem, xsdElementNode);
        }

        parentNode.appendChild(xdElem);
    }

    private void createTopNonRootElement(final XmlSchemaComplexType xsdComplexNode, Element parentNode) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xsdComplexNode, "Creating top level non-root element ...");
        final Element xdElem = xdFactory.createEmptyElement(xsdComplexNode, xDefName);
        createElementFromComplex(xdElem, xsdComplexNode);
        parentNode.appendChild(xdElem);
    }

    private void createElementFromComplex(final Element xdElem, final XmlSchemaComplexType xsdComplexNode) {
        addAttrsToElem(xdElem, xsdComplexNode.getAttributes());

        // TODO: mixed
        if (xsdComplexNode.getParticle() != null) {
            convertTree(xsdComplexNode.getParticle(), xdElem);
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
                            Xsd2XdUtils.addRefAttribute(xdElem, baseType);
                        }
                    }

                    if (xsdComplexExtension.getParticle() != null) {
                        if (xsdComplexExtension.getParticle() instanceof XmlSchemaGroupParticle) {
                            createGroupParticle((XmlSchemaGroupParticle)xsdComplexExtension.getParticle(), xdElem);
                        }
                    }

                    addAttrsToElem(xdElem, xsdComplexExtension.getAttributes());
                }
                // TODO: more types of extensions/restrictions?
            }
        }
    }

    private void createGroupParticle(final XmlSchemaGroupParticle xsdParticleNode, Element parentNode) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xsdParticleNode, "Creating group particle ...");
        Element xdParticle = null;
        if (xsdParticleNode instanceof XmlSchemaSequence) {
            final XmlSchemaSequence xsdSequence = (XmlSchemaSequence)xsdParticleNode;
            xdParticle = xdFactory.createEmptySequence();
            final List<XmlSchemaSequenceMember> xsdSequenceMembers = xsdSequence.getItems();
            if (xsdSequenceMembers != null && !xsdSequenceMembers.isEmpty()) {
                for (XmlSchemaSequenceMember xsdSequenceMember : xsdSequenceMembers) {
                    if (xsdSequenceMember instanceof XmlSchemaParticle) {
                        convertTree(xsdSequenceMember, xdParticle);
                    }
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
        }

        parentNode.appendChild(xdParticle);
    }

    private void createGroup(final XmlSchemaGroup xsdGroupNode, Element parentNode) {
        XsdLogger.printP(LOG_DEBUG, TRANSFORMATION, xsdGroupNode, "Creating group.");

        final Element group = xdFactory.createEmptyNamedMixed(xsdGroupNode.getName());
        if (xsdGroupNode.getParticle() != null) {
            createGroupParticle(xsdGroupNode.getParticle(), group);
        }
        parentNode.appendChild(group);
    }

    private void createGroupRef(final XmlSchemaGroupRef xsdGroupRefNode, Element parentNode) {
        XsdLogger.printP(LOG_DEBUG, TRANSFORMATION, xsdGroupRefNode, "Creating group reference.");

        // TODO: mixed ref cannot be part of sequence/choice/mixed? requires advanced processing
        final XmlSchemaGroup group = Xsd2XdUtils.getGroupByQName(schema, xsdGroupRefNode.getRefName());
        if (group != null) {
            // Copy group content into element
            convertTree(group.getParticle(), parentNode);
            xdAttrFactory.addOccurrence((Element) parentNode.getLastChild(), xsdGroupRefNode);
        } else {
            final Element groupRef = xdFactory.createEmptyMixed();
            Xsd2XdUtils.addRefAttribute(groupRef, xsdGroupRefNode.getRefName());
            XsdLogger.printP(LOG_ERROR, TRANSFORMATION, xsdGroupRefNode, "Group reference possible inside sequence/choice/mixed node");
            if (xsdGroupRefNode.getMaxOccurs() > 1) {
                XsdLogger.printP(LOG_ERROR, TRANSFORMATION, xsdGroupRefNode, "Group reference is using multiple occurence - prohibited in x-definition.");
            }

            parentNode.appendChild(groupRef);
        }
    }

    private void createAny(final XmlSchemaAny xsdAnyNode, Element parentNode) {
        XsdLogger.printP(LOG_DEBUG, TRANSFORMATION, xsdAnyNode, "Creating any.");
        final Element xdAny = xdFactory.createEmptyAny();
        xdAttrFactory.addOccurrence(xdAny, xsdAnyNode);
        parentNode.appendChild(xdAny);
    }

    private void addAttrsToElem(final Element xdElem, final List<XmlSchemaAttributeOrGroupRef> xsdAttrs) {
        if (xsdAttrs != null) {
            for (XmlSchemaAttributeOrGroupRef xsdAttrRef : xsdAttrs) {
                if (xsdAttrRef instanceof XmlSchemaAttribute) {
                    final XmlSchemaAttribute xsdAttr = (XmlSchemaAttribute)xsdAttrRef;
                    final String attribute = createAttribute(xsdAttr);
                    Xsd2XdUtils.addAttribute(xdElem, xsdAttr, attribute, xDefName, adapterCtx);
                }
            }
        }
    }

    private String createAttribute(final XmlSchemaAttribute xsdAttr) {
        XsdLogger.printP(LOG_DEBUG, TRANSFORMATION, xsdAttr, "Creating attribute.");

        final StringBuilder valueBuilder = new StringBuilder();
        if (XmlSchemaUse.REQUIRED.equals(xsdAttr.getUse())) {
            valueBuilder.append("required ");
        } else {
            valueBuilder.append("optional ");
        }

        if (xsdAttr.getSchemaTypeName() != null) {
            valueBuilder.append(xsdAttr.getSchemaTypeName().getLocalPart() + "()");
        } else if (xsdAttr.getSchemaType() != null) {
            if (xsdAttr.getSchemaType().getContent() instanceof XmlSchemaSimpleTypeRestriction) {
                valueBuilder.append(xdDeclarationFactory.create((XmlSchemaSimpleTypeRestriction)xsdAttr.getSchemaType().getContent(), null, IDeclarationTypeFactory.Mode.DATATYPE_DECL));
            }
        }

        if (xsdAttr.getDefaultValue() != null && !xsdAttr.getDefaultValue().isEmpty()) {
            valueBuilder.append("; default \"" + xsdAttr.getDefaultValue() + "\"");
        }

        return valueBuilder.toString();
    }

    private boolean externalRef(final QName baseType, final Element xdNode, final boolean simple) {
        if (baseType.getNamespaceURI() != null && !baseType.getNamespaceURI().equals(schema.getTargetNamespace())) {
            final String xDefRefName = Xsd2XdUtils.getReferenceSchemaName(schema.getParent(), baseType, adapterCtx, simple);
            return externalRef(baseType, xDefRefName, xdNode);
        }

        return false;
    }

    private boolean externalRef(final QName baseType, final String xDefRefName, final Element xdNode) {
        if (baseType.getNamespaceURI() != null && !baseType.getNamespaceURI().equals(schema.getTargetNamespace())) {
            if (xDefRefName != null && !xDefRefName.equals(xDefName)) {
                Xsd2XdUtils.addRefInDiffXDefAttribute(xdNode, xDefRefName, baseType);
                return true;
            }
        }

        return false;
    }
}
