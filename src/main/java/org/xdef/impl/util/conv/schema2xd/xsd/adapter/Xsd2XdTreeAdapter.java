package org.xdef.impl.util.conv.schema2xd.xsd.adapter;

import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xdef.impl.util.conv.schema.util.XsdLogger;
import org.xdef.impl.util.conv.schema2xd.xsd.factory.XdAttributeFactory;
import org.xdef.impl.util.conv.schema2xd.xsd.factory.XdDeclarationFactory;
import org.xdef.impl.util.conv.schema2xd.xsd.factory.XdElementFactory;
import org.xdef.impl.util.conv.schema2xd.xsd.model.XdAdapterCtx;
import org.xdef.impl.util.conv.schema2xd.xsd.util.Xsd2XdUtils;
import org.xdef.impl.util.conv.xd2schema.xsd.model.XsdAdapterCtx;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;

import static org.xdef.impl.util.conv.schema.util.XsdLoggerDefs.LOG_INFO;
import static org.xdef.impl.util.conv.xd2schema.xsd.definition.AlgPhase.PREPROCESSING;
import static org.xdef.impl.util.conv.xd2schema.xsd.definition.AlgPhase.TRANSFORMATION;

public class Xsd2XdTreeAdapter {

    /**
     * Output x-definition name
     */
    final private String xDefName;

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
     * Output x-definition document
     */
    private Document doc;

    public Xsd2XdTreeAdapter(String xDefName, XdElementFactory xdFactory, XdAdapterCtx adapterCtx) {
        this.xDefName = xDefName;
        this.xdFactory = xdFactory;
        this.adapterCtx = adapterCtx;
        xdAttrFactory = new XdAttributeFactory(adapterCtx);
    }

    public void setDoc(Document doc) {
        this.doc = doc;
    }

    public String loadXsdRootNames(Map<QName, XmlSchemaElement> rootElements) {
        XsdLogger.print(LOG_INFO, PREPROCESSING, xDefName, "Loading root elements of XSD");

        if (rootElements == null) {
            return "";
        }

        final StringBuilder rootElemSb = new StringBuilder();
        for (XmlSchemaElement xsdElem : rootElements.values()) {
            if (rootElemSb.length() == 0) {
                rootElemSb.append(xsdElem.getName());
            } else {
                rootElemSb.append("|" + xsdElem.getName());
            }
        }

        return rootElemSb.toString();
    }

    public Node convertTree(final XmlSchemaObjectBase xsdNode, final boolean topLevel) {

        if (xsdNode instanceof XmlSchemaElement) {
            return createElement((XmlSchemaElement)xsdNode, topLevel);
        } else if (xsdNode instanceof XmlSchemaType) {
            if (topLevel) {
                final XdDeclarationFactory declarationFactory = new XdDeclarationFactory(xdFactory, adapterCtx);
                final Element xdDeclaration = declarationFactory.create((XmlSchemaType)xsdNode);

                return xdDeclaration;
            }
        } else if (xsdNode instanceof XmlSchemaGroupParticle) {
            return createParticle((XmlSchemaGroupParticle)xsdNode, topLevel);
        }

        return null;
    }

    private Node createElement(final XmlSchemaElement xsdElementNode, final boolean topLevel) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xsdElementNode, "Creating element ...");
        final Element xdElem = xdFactory.createEmptyElement(xsdElementNode);
        if (xsdElementNode.getSchemaType() != null) {
            if (xsdElementNode.getSchemaType() instanceof XmlSchemaComplexType) {
                final XmlSchemaComplexType xsdComplexType = (XmlSchemaComplexType)xsdElementNode.getSchemaType();
                addAttrsToElem(xdElem, xsdComplexType.getAttributes());

                if (xsdComplexType.getParticle() != null) {
                    xdElem.appendChild(convertTree(xsdComplexType.getParticle(), false));
                }

                if (xsdComplexType.getContentModel() != null) {
                    if (xsdComplexType.getContentModel() instanceof XmlSchemaSimpleContent) {
                        final XmlSchemaSimpleContent xsdSimpleContent = (XmlSchemaSimpleContent)xsdComplexType.getContentModel();
                        if (xsdSimpleContent.getContent() instanceof XmlSchemaSimpleContentExtension) {
                            final XmlSchemaSimpleContentExtension xsdSimpleExtension = (XmlSchemaSimpleContentExtension)xsdSimpleContent.getContent();
                            xdElem.setTextContent(xsdSimpleExtension.getBaseTypeName().getLocalPart() + "()");
                            addAttrsToElem(xdElem, xsdSimpleExtension.getAttributes());
                        }
                        // TODO: more types of extensions/restrictions
                    } else {
                        // TODO: XmlSchemaComplexContent?
                    }
                }
            } else if (xsdElementNode.getSchemaType() instanceof XmlSchemaSimpleType) {
                // TODO: implement
            }
        }

        if (xdElem != null) {
            xdAttrFactory.addOccurrence(xdElem, xsdElementNode);
        }

        return xdElem;
    }

    private Node createParticle(final XmlSchemaGroupParticle xsdParticleNode, final boolean topLevel) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xsdParticleNode, "Creating particle ...");
        Element xdParticle = null;
        if (xsdParticleNode instanceof XmlSchemaSequence) {
            final XmlSchemaSequence xsdSequence = (XmlSchemaSequence)xsdParticleNode;
            xdParticle = xdFactory.createEmptySequence();
            final List<XmlSchemaSequenceMember> xsdSequenceMembers = xsdSequence.getItems();
            if (xsdSequenceMembers != null && !xsdSequenceMembers.isEmpty()) {
                for (XmlSchemaSequenceMember xsdSequenceMember : xsdSequenceMembers) {
                    if (xsdSequenceMember instanceof XmlSchemaElement) {
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
                    if (xsdChoiceMember instanceof XmlSchemaElement) {
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
                    Xsd2XdUtils.addAttribute(xdElem, xsdAttr);
                }
            }
        }
    }
}
