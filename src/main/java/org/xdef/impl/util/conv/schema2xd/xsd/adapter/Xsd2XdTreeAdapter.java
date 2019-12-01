package org.xdef.impl.util.conv.schema2xd.xsd.adapter;

import javafx.util.Pair;
import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.constants.Constants;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xdef.impl.util.conv.schema.util.XsdLogger;
import org.xdef.impl.util.conv.schema2xd.xsd.factory.XdAttributeFactory;
import org.xdef.impl.util.conv.schema2xd.xsd.factory.XdDeclarationFactory;
import org.xdef.impl.util.conv.schema2xd.xsd.factory.XdElementFactory;
import org.xdef.impl.util.conv.schema2xd.xsd.factory.declaration.IDeclarationTypeFactory;
import org.xdef.impl.util.conv.schema2xd.xsd.model.XdAdapterCtx;
import org.xdef.impl.util.conv.schema2xd.xsd.util.Xsd2XdUtils;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;

import static org.xdef.impl.util.conv.schema.util.XsdLoggerDefs.*;
import static org.xdef.impl.util.conv.schema2xd.xsd.definition.Xsd2XdFeature.XD_TEXT_OPTIONAL;
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
                rootElemSb.append("|" + targetNsPrefix + xsdElem.getName());
            }
        }

        return rootElemSb.toString();
    }

    public Node convertTree(final XmlSchemaObjectBase xsdNode, final boolean topLevel) {
        if (xsdNode instanceof XmlSchemaElement) {
            return createElement((XmlSchemaElement)xsdNode, topLevel);
        } else if (xsdNode instanceof XmlSchemaType) {
            if (topLevel) {
                if (xsdNode instanceof XmlSchemaSimpleType) {
                    final Element xdDeclaration = xdDeclarationFactory.createTopDeclaration((XmlSchemaSimpleType) xsdNode);
                    return xdDeclaration;
                } else if (xsdNode instanceof XmlSchemaComplexType) {
                    return createTopNonRootElement((XmlSchemaComplexType)xsdNode);
                }
            }
        } else if (xsdNode instanceof XmlSchemaGroupParticle) {
            return createGroupParticle((XmlSchemaGroupParticle)xsdNode, topLevel);
        }

        return null;
    }

    private Node createElement(final XmlSchemaElement xsdElementNode, final boolean topLevel) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xsdElementNode, "Creating element ...");
        final Element xdElem = xdFactory.createEmptyElement(xsdElementNode, xDefName);
        final QName xsdElemQName = xsdElementNode.getSchemaTypeName();
        if (xsdElemQName != null && XSD_NAMESPACE_PREFIX_EMPTY.equals(xsdElemQName.getPrefix())) {
            if (xsdElementNode.getSchemaType() != null) {
                if (xsdElementNode.getSchemaType() instanceof XmlSchemaComplexType) {
                    XsdLogger.printP(LOG_INFO, TRANSFORMATION, xsdElementNode, "Element is referencing to complex type. Reference=" + xsdElemQName);
                    Xsd2XdUtils.addRefAttribute(xdElem, xsdElemQName);
                } else if (xsdElementNode.getSchemaType() instanceof XmlSchemaSimpleType) {
                    XsdLogger.printP(LOG_INFO, TRANSFORMATION, xsdElementNode, "Element is referencing to simple type. Reference=" + xsdElemQName);
                    final XmlSchemaSimpleType simpleType = (XmlSchemaSimpleType)xsdElementNode.getSchemaType();
                    if (simpleType.getContent() instanceof XmlSchemaSimpleTypeRestriction) {
                        // TODO: make as reference instead of copying restriction, t005 & t006
                        xdElem.setTextContent(xdDeclarationFactory.create((XmlSchemaSimpleTypeRestriction)simpleType.getContent(), null, IDeclarationTypeFactory.Mode.TEXT_DECL));
                    }
                }
            } else {
                XsdLogger.printP(LOG_WARN, TRANSFORMATION, xsdElementNode, "Element reference has not found! Reference=" + xsdElemQName);
            }
        } else if (xsdElementNode.getSchemaType() != null) {
            if (xsdElementNode.getSchemaType() instanceof XmlSchemaComplexType) {
                createElementFromComplex(xdElem, (XmlSchemaComplexType)xsdElementNode.getSchemaType());
            } else if (xsdElementNode.getSchemaType() instanceof XmlSchemaSimpleType) {
                final XmlSchemaSimpleType simpleType = (XmlSchemaSimpleType)xsdElementNode.getSchemaType();
                if (xsdElemQName != null && (Constants.XSD_NMTOKENS.equals(xsdElemQName) || Constants.XSD_IDREFS.equals(xsdElemQName))) {
                    xdElem.setTextContent(xdDeclarationFactory.createSimpleTextDeclaration(xsdElemQName));
                } else if (simpleType.getContent() instanceof XmlSchemaSimpleTypeRestriction) {
                    xdElem.setTextContent(xdDeclarationFactory.createTextDeclaration((XmlSchemaSimpleTypeRestriction)simpleType.getContent(), xsdElemQName));
                } else if (simpleType.getContent() instanceof XmlSchemaSimpleTypeUnion) {
                    xdElem.setTextContent(xdDeclarationFactory.createTextDeclaration((XmlSchemaSimpleTypeUnion)simpleType.getContent()));
                }
            }
        }

        if (xdElem != null) {
            xdAttrFactory.addOccurrence(xdElem, xsdElementNode);
            xdAttrFactory.addNillable(xdElem, xsdElementNode);
        }

        return xdElem;
    }

    private Node createTopNonRootElement(final XmlSchemaComplexType xsdComplexNode) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xsdComplexNode, "Creating top level non-root element ...");
        final Element xdElem = xdFactory.createEmptyElement(xsdComplexNode);
        createElementFromComplex(xdElem, xsdComplexNode);
        return xdElem;
    }

    private void createElementFromComplex(final Element xdElem, final XmlSchemaComplexType xsdComplexNode) {
        addAttrsToElem(xdElem, xsdComplexNode.getAttributes());

        if (xsdComplexNode.getParticle() != null) {
            xdElem.appendChild(convertTree(xsdComplexNode.getParticle(), false));
        }

        if (xsdComplexNode.getContentModel() != null) {
            if (xsdComplexNode.getContentModel() instanceof XmlSchemaSimpleContent) {
                final XmlSchemaSimpleContent xsdSimpleContent = (XmlSchemaSimpleContent)xsdComplexNode.getContentModel();
                if (xsdSimpleContent.getContent() instanceof XmlSchemaSimpleContentExtension) {
                    final XmlSchemaSimpleContentExtension xsdSimpleExtension = (XmlSchemaSimpleContentExtension)xsdSimpleContent.getContent();
                    if (adapterCtx.hasEnableFeature(XD_TEXT_OPTIONAL)) {
                        xdElem.setTextContent("optional " + xsdSimpleExtension.getBaseTypeName().getLocalPart() + "()");
                    } else {
                        xdElem.setTextContent(xsdSimpleExtension.getBaseTypeName().getLocalPart() + "()");
                    }
                    addAttrsToElem(xdElem, xsdSimpleExtension.getAttributes());
                }
                // TODO: more types of extensions/restrictions
            } else if (xsdComplexNode.getContentModel() instanceof XmlSchemaComplexContent) {
                final XmlSchemaComplexContent xsdComplexContent = (XmlSchemaComplexContent)xsdComplexNode.getContentModel();
                if (xsdComplexContent.getContent() instanceof XmlSchemaComplexContentExtension) {
                    final XmlSchemaComplexContentExtension xsdComplexExtension = (XmlSchemaComplexContentExtension)xsdComplexContent.getContent();
                    final QName baseType = xsdComplexExtension.getBaseTypeName();
                    if (baseType != null) {
                        Xsd2XdUtils.addRefAttribute(xdElem, baseType);
                    }

                    if (xsdComplexExtension.getParticle() != null) {
                        if (xsdComplexExtension.getParticle() instanceof XmlSchemaGroupParticle) {
                            xdElem.appendChild(createGroupParticle((XmlSchemaGroupParticle)xsdComplexExtension.getParticle(), false));
                        }
                    }

                    addAttrsToElem(xdElem, xsdComplexExtension.getAttributes());
                }
                // TODO: more types of extensions/restrictions
            }
        }
    }

    private Node createGroupParticle(final XmlSchemaGroupParticle xsdParticleNode, final boolean topLevel) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xsdParticleNode, "Creating particle ...");
        Element xdParticle = null;
        if (xsdParticleNode instanceof XmlSchemaSequence) {
            final XmlSchemaSequence xsdSequence = (XmlSchemaSequence)xsdParticleNode;
            xdParticle = xdFactory.createEmptySequence();
            final List<XmlSchemaSequenceMember> xsdSequenceMembers = xsdSequence.getItems();
            if (xsdSequenceMembers != null && !xsdSequenceMembers.isEmpty()) {
                for (XmlSchemaSequenceMember xsdSequenceMember : xsdSequenceMembers) {
                    if (xsdSequenceMember instanceof XmlSchemaElement || xsdSequenceMember instanceof XmlSchemaGroupParticle) {
                        xdParticle.appendChild(convertTree(xsdSequenceMember, false));
                    }
                }
            }
        } else if (xsdParticleNode instanceof XmlSchemaChoice) {
            final XmlSchemaChoice xsdChoice = (XmlSchemaChoice)xsdParticleNode;
            xdParticle = xdFactory.createEmptyChoice();
            final List<XmlSchemaChoiceMember> xsdChoiceMembers = xsdChoice.getItems();
            if (xsdChoiceMembers != null && !xsdChoiceMembers.isEmpty()) {
                for (XmlSchemaChoiceMember xsdChoiceMember : xsdChoiceMembers) {
                    if (xsdChoiceMember instanceof XmlSchemaElement || xsdChoiceMember instanceof XmlSchemaGroupParticle) {
                        xdParticle.appendChild(convertTree(xsdChoiceMember, false));
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
                        xdParticle.appendChild(convertTree(xsdAllMember, false));
                    }
                }
            }
        }

        if (xdParticle != null) {
            xdAttrFactory.addOccurrence(xdParticle, xsdParticleNode);
        }

        return xdParticle;
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
        if (XmlSchemaUse.OPTIONAL.equals(xsdAttr.getUse())) {
            valueBuilder.append("optional ");
        } else if (XmlSchemaUse.REQUIRED.equals(xsdAttr.getUse())) {
            valueBuilder.append("required ");
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
}
