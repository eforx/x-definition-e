package org.xdef.impl.util.conv.xd2schemas.xsd;

import org.apache.ws.commons.schema.*;
import org.xdef.impl.XData;
import org.xdef.impl.XDefinition;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.xd2schemas.xsd.builder.XsdElementBuilder;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XD2XsdUtils;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;
import org.xdef.model.XMNode;

import javax.xml.namespace.QName;
import java.util.*;

import static org.xdef.impl.util.conv.xd2schemas.xsd.XD2XsdDefinitions.XD_PARSER_EQ;
import static org.xdef.impl.util.conv.xd2schemas.xsd.XD2XsdDefinitions.XSD_NAMESPACE_PREFIX_EMPTY;
import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.*;

class XDTree2XsdAdapter {

    final private int logLevel;
    final private XmlSchema schema;
    final private XsdElementBuilder xsdBuilder;

    private Set<XMNode> xdProcessedNodes = null;
    private List<String> xdRootNames = null;

    protected XDTree2XsdAdapter(int logLevel, XmlSchema schema, XsdElementBuilder xsdBuilder) {
        this.logLevel = logLevel;
        this.schema = schema;
        this.xsdBuilder = xsdBuilder;
    }

    protected List<String> getXdRootNames() {
        return xdRootNames;
    }

    protected void loadXdefRootNames(final XDefinition def) {
        if (XsdLogger.isInfo(logLevel)) {
            XsdLogger.printP(INFO, PREPROCESSING, def, "Loading root of x-definitions");
        }
        xdRootNames = new ArrayList<String>();
        if (def._rootSelection != null && def._rootSelection.size() > 0) {
            Iterator<String> e = def._rootSelection.keySet().iterator();
            while (e.hasNext()) {
                final String rootName = e.next();
                xdRootNames.add(rootName);
                if (XsdLogger.isDebug(logLevel)) {
                    XsdLogger.printP(DEBUG, PREPROCESSING, def, "Add root name. Name=" + rootName);
                }
            }
        }
    }

    protected XmlSchemaObject convertTree(XNode xn) {
        xdProcessedNodes = new HashSet<XMNode>();
        return convertTreeInt(xn);
    }

    private XmlSchemaObject convertTreeInt(XNode xn) {

        if (!xdProcessedNodes.add(xn)) {
            if (XsdLogger.isDebug(logLevel)) {
                XsdLogger.printP(DEBUG, TRANSFORMATION, xn, "Already processed. This node is reference probably");
            }
            return null;
        }

        short xdElemKind = xn.getKind();
        switch (xdElemKind) {
            case XNode.XMATTRIBUTE: {
                XData xd = (XData) xn;
                if (XsdLogger.isDebug(logLevel)) {
                    XsdLogger.printP(DEBUG, TRANSFORMATION, xn, "Processing XMAttr node");
                }
                return createAttribute(xd);
            }
            case XNode.XMTEXT: {
                XData xd = (XData) xn;
                if (XsdLogger.isDebug(logLevel)) {
                    XsdLogger.printP(DEBUG, TRANSFORMATION, xn, "Processing XMText node");
                }

                return xsdBuilder.createSimpleContent(xd);
            }
            case XNode.XMELEMENT: {
                XElement xd = (XElement) xn;
                if (XsdLogger.isDebug(logLevel)) {
                    XsdLogger.printP(DEBUG, TRANSFORMATION, xn, "Processing XMElement node");
                }

                return createElement(xd);
            }
            case XNode.XMSELECTOR_END:
                return null;
            case XNode.XMSEQUENCE:
            case XNode.XMMIXED:
            case XNode.XMCHOICE:
                if (XsdLogger.isDebug(logLevel)) {
                    XsdLogger.printP(DEBUG, TRANSFORMATION, xn, "Processing Particle node. Particle=" + XD2XsdUtils.particleXKindToString(xdElemKind));
                }
                return xsdBuilder.createGroupParticle(xdElemKind, xn.getOccurence());
            case XNode.XMDEFINITION: {
                if (XsdLogger.isError(logLevel)) {
                    XsdLogger.printP(ERROR, TRANSFORMATION, xn, "XDefinition node has to be only pre-processed!");
                }
                return null;
            }
            default: {
                if (XsdLogger.isError(logLevel)) {
                    XsdLogger.printP(ERROR, TRANSFORMATION, xn, "Unknown type of node. NodeType=" + xdElemKind);
                }
            }
        }

        return null;
    }

    private XmlSchemaAttribute createAttribute(final XData xData) {
        XmlSchemaAttribute attr = new XmlSchemaAttribute(schema, false);
        final String importNamespace = xData.getNSUri();
        final String nodeName = xData.getName();
        if (importNamespace != null && XD2XsdUtils.isRefInDifferentNamespace(nodeName, importNamespace, schema)) {
            attr.getRef().setTargetQName(new QName(importNamespace, nodeName));
            if (XsdLogger.isInfo(logLevel)) {
                XsdLogger.printP(INFO, TRANSFORMATION, xData, "Creating attribute reference from different namespace." +
                        "Name=" + xData.getName() + ", Namespace=" + xData.getNSUri());
            }
        } else {
            attr.setName(xData.getName());
            QName qName;

            // TODO: Handling of reference namespaces?
            if (xData.getRefTypeName() != null) {
                attr.setSchemaTypeName(new QName(XSD_NAMESPACE_PREFIX_EMPTY, xData.getRefTypeName()));
                if (XsdLogger.isInfo(logLevel)) {
                    XsdLogger.printP(INFO, TRANSFORMATION, xData, "Creating attribute reference in same namespace/x-definition" +
                            "Name=" + xData.getName());
                }
            } else if ((qName = XD2XsdUtils.getDefaultSimpleParserQName(xData)) != null) {
                attr.setSchemaTypeName(qName);
                if (XsdLogger.isInfo(logLevel)) {
                    XsdLogger.printP(INFO, TRANSFORMATION, xData, "Content of attribute contains only XSD datatype" +
                            "Element=" + xData.getName() + ", DataType=" + qName.getLocalPart());
                }
            } else if (XD_PARSER_EQ.equals(xData.getParserName())) {
                qName = XD2XsdUtils.getDefaultQName(xData.getValueTypeName());
                // TODO: Where to get fixed value
                //attr.setFixedValue("1.0");
                // TODO: Possible to use non-default xsd types?
                attr.setSchemaTypeName(qName);
                if (XsdLogger.isInfo(logLevel)) {
                    XsdLogger.printP(INFO, TRANSFORMATION, xData, "Content of attribute contains datatype with fixed value" +
                            "Element=" + xData.getName() + ", DataType=" + qName.getLocalPart());
                }
            } else {
                attr.setSchemaType(xsdBuilder.creatSimpleType(xData));
            }

            XD2XsdUtils.resolveAttributeQName(schema, attr, xData.getName());
        }

        if (xData.isOptional() || xData.getOccurence().isOptional()) {
            attr.setUse(XmlSchemaUse.OPTIONAL);
        } else if (xData.isRequired() || xData.getOccurence().isRequired()) {
            attr.setUse(XmlSchemaUse.REQUIRED);
        }

        return attr;
    }

    private XmlSchemaObject createElement(final XElement xDefElem) {
        XmlSchemaElement xsdElem = xsdBuilder.createEmptyElement(xDefElem);

        if (xDefElem.isReference()) {
            if (XD2XsdUtils.isRefInDifferentNamespace(xDefElem.getName(), xDefElem.getNSUri(), schema)) {
                xsdElem.getRef().setTargetQName(new QName(xDefElem.getNSUri(), xDefElem.getName()));
                if (XsdLogger.isInfo(logLevel)) {
                    XsdLogger.printP(INFO, TRANSFORMATION, xDefElem, "Creating element reference from different namespace." +
                            "Name=" + xDefElem.getName() + ", Namespace=" + xDefElem.getNSUri());
                }
            } else if (XD2XsdUtils.isRefInDifferentSystem(xDefElem.getReferencePos(), xDefElem.getXDPosition())) {
                final String refXDefinitionName = XD2XsdUtils.getReferenceSystemId(xDefElem.getReferencePos());
                // TODO: Validate target namespace?
                xsdElem.getRef().setTargetQName(new QName(refXDefinitionName, xDefElem.getName()));
                if (XsdLogger.isInfo(logLevel)) {
                    XsdLogger.printP(INFO, TRANSFORMATION, xDefElem, "Creating element reference from different x-definition." +
                            "Name=" + xDefElem.getName() + ", Namespace=" + refXDefinitionName);
                }
            } else {
                xsdElem.setName(xDefElem.getName());
                // TODO: reference namespace?
                final String localName = XD2XsdUtils.getReferenceName(xDefElem.getReferencePos());
                xsdElem.setSchemaTypeName(new QName(XSD_NAMESPACE_PREFIX_EMPTY, localName));
                XD2XsdUtils.resolveElementQName(schema, xsdElem);
                if (XsdLogger.isInfo(logLevel)) {
                    XsdLogger.printP(INFO, TRANSFORMATION, xDefElem, "Creating element schema type name in same namespace/x-definition" +
                            "Name=" + localName);
                }
            }
        } else {

            xsdElem.setName(xDefElem.getName());

            // If element contains only data, we dont have to create complexType
            if (xDefElem.getXDAttrs().length == 0 && xDefElem._childNodes.length == 1 && xDefElem._childNodes[0].getKind() == XNode.XMTEXT) {
                addSimpleContentToElem(xsdElem, (XData)xDefElem._childNodes[0]);
            } else {
                addComplexContentToElem(xsdElem, xDefElem);
            }

            XD2XsdUtils.resolveElementQName(schema, xsdElem);
        }

        return xsdElem;
    }

    private void addSimpleContentToElem(final XmlSchemaElement xsdElem, final XData xd) {
        if (XsdLogger.isInfo(logLevel)) {
            XsdLogger.printP(INFO, TRANSFORMATION, xd, "Creating simple content of element. " +
                    "Element=" + xsdElem.getName());
        }

        // TODO: Should we lookup for simple type reference, which is equal to current simple type?
        final QName qName = XD2XsdUtils.getDefaultSimpleParserQName(xd);
        if (qName != null) {
            xsdElem.setSchemaTypeName(qName);
            if (XsdLogger.isDebug(logLevel)) {
                XsdLogger.printP(DEBUG, TRANSFORMATION, xd, "Content of element contains only XSD datatype" +
                        "Element=" + xsdElem.getName() + ", DataType=" + qName.getLocalPart());
            }
        } else {
            xsdElem.setType(xsdBuilder.creatSimpleType(xd));
        }
    }

    private void addComplexContentToElem(final XmlSchemaElement xsdElem, final XElement defEl) {
        if (XsdLogger.isInfo(logLevel)) {
            XsdLogger.printP(INFO, TRANSFORMATION, defEl, "Creating complex content of element.");
        }

        XmlSchemaComplexType complexType = xsdBuilder.createEmptyComplexType();

        XmlSchemaGroupParticle group = null;
        boolean hasSimpleContent = false;
        XData[] attrs = defEl.getXDAttrs();

        // Convert all children nodes
        for (int i = 0; i < defEl._childNodes.length; i++) {
            XNode xnChild = defEl._childNodes[i];
            short childrenKind = xnChild.getKind();
            // Particle nodes (sequence, choice, all)
            if (childrenKind == XNode.XMSEQUENCE || childrenKind == XNode.XMMIXED || childrenKind == XNode.XMCHOICE) {
                if (complexType.getParticle() != null) {
                    // TODO: XD->XSD Solve
                    if (XsdLogger.isWarn(logLevel)) {
                        XsdLogger.printP(WARN, TRANSFORMATION, defEl, "Contains multiple particle group inside element!");
                    }
                }
                group = (XmlSchemaGroupParticle) convertTreeInt(xnChild);

                if (XsdLogger.isDebug(logLevel)) {
                    XsdLogger.printP(DEBUG, TRANSFORMATION, defEl, "Add particle to complex content of element. Particle=" + XD2XsdUtils.particleXKindToString(childrenKind));
                }
            } else if (childrenKind == XNode.XMTEXT) { // Simple value node
                XmlSchemaSimpleContent simpleContent = (XmlSchemaSimpleContent) convertTreeInt(xnChild);
                if (simpleContent != null && simpleContent.getContent() instanceof XmlSchemaSimpleContentExtension) {
                    if (XsdLogger.isDebug(logLevel)) {
                        XsdLogger.printP(DEBUG, TRANSFORMATION, defEl, "Add simple content with attributes to complex content of element.");
                    }

                    complexType.setContentModel(simpleContent);

                    for (int j = 0; j < attrs.length; j++) {
                        if (simpleContent.getContent() instanceof XmlSchemaSimpleContentExtension)
                            ((XmlSchemaSimpleContentExtension) simpleContent.getContent()).getAttributes().add((XmlSchemaAttributeOrGroupRef) convertTreeInt(attrs[j]));
                    }

                    hasSimpleContent = true;
                } else {
                    if (XsdLogger.isWarn(logLevel)) {
                        XsdLogger.printP(WARN, TRANSFORMATION, defEl, "Content of XText is not simple!");
                    }
                }
            } else {
                XmlSchemaObject xsdChild = convertTreeInt(xnChild);
                if (xsdChild != null) {
                    if (XsdLogger.isDebug(logLevel)) {
                        XsdLogger.printP(DEBUG, TRANSFORMATION, defEl, "Add child to particle of element.");
                    }

                    // x-definition has no required group
                    if (group == null) {
                        group = new XmlSchemaSequence();

                        if (XsdLogger.isDebug(logLevel)) {
                            XsdLogger.printP(DEBUG, TRANSFORMATION, defEl, "Particle is undefined. Creating sequence particle.");
                        }
                    }

                    if (group instanceof XmlSchemaSequence) {
                        ((XmlSchemaSequence) group).getItems().add((XmlSchemaSequenceMember) xsdChild);
                    } else if (group instanceof XmlSchemaChoice) {
                        ((XmlSchemaChoice) group).getItems().add((XmlSchemaChoiceMember) xsdChild);
                    } else if (group instanceof XmlSchemaAll) {
                        ((XmlSchemaAll) group).getItems().add((XmlSchemaAllMember) xsdChild);
                    }
                }
            }
        }

        if (group != null) {
            complexType.setParticle(group);
        }

        postProcessComplexContent(defEl, complexType);

        if (hasSimpleContent == false) {
            if (XsdLogger.isDebug(logLevel)) {
                XsdLogger.printP(DEBUG, TRANSFORMATION, defEl, "Add attributes to complex content of element");
            }

            for (int i = 0; i < attrs.length; i++) {
                complexType.getAttributes().add((XmlSchemaAttributeOrGroupRef) convertTreeInt(attrs[i]));
            }
        }

        xsdElem.setType(complexType);
    }

    private void postProcessComplexContent(final XElement defEl, final XmlSchemaComplexType complexType) {
        if (XsdLogger.isDebug(logLevel)) {
            XsdLogger.printP(DEBUG, POSTPROCESSING, defEl, "Updating complex content of element");
        }

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
                    if (XsdLogger.isDebug(logLevel)) {
                        XsdLogger.printP(DEBUG, POSTPROCESSING, defEl, "Complex content contains xs:all with only unbounded elements. Update to unbounded xs:choise.");
                    }

                    XmlSchemaChoice group = new XmlSchemaChoice();
                    group.setMaxOccurs(Long.MAX_VALUE);

                    // Copy elements
                    for (XmlSchemaAllMember member : ((XmlSchemaAll)complexType.getParticle()).getItems()) {
                        group.getItems().add((XmlSchemaChoiceMember) member);
                    }

                    complexType.setParticle(group);
                } else if (anyElementUnbounded) {
                    // TODO: XD->XSD Solve
                    if (XsdLogger.isError(logLevel)) {
                        XsdLogger.printP(ERROR, POSTPROCESSING, defEl, "xs:all contains element which has maxOccurs higher than 1");
                    }

                }
            }
        }

        // element contains simple content and particle -> XSD does not support restrictions for text if element contains elements
        // We have to use mixed attribute for root element and remove simple content
        {
            if (complexType.getParticle() != null && complexType.getContentModel() != null && complexType.getContentModel() instanceof XmlSchemaSimpleContent) {
                if (XsdLogger.isWarn(logLevel)) {
                    XsdLogger.printP(WARN, POSTPROCESSING, defEl, "!Lossy transformation! Remove simple content from element due to existence of complex content. Use mixed attr.");
                }

                complexType.setContentModel(null);
                complexType.setMixed(true);
                complexType.setAnnotation(XsdElementBuilder.createAnnotation("Text content has been originally restricted by x-definition"));
            }
        }
    }

}
