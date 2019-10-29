package org.xdef.impl.util.conv.xd2schemas.xsd;

import org.apache.ws.commons.schema.*;
import org.xdef.impl.XData;
import org.xdef.impl.XDefinition;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.XsdElementFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.XmlSchemaImportLocation;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.*;
import org.xdef.model.XMNode;

import javax.xml.namespace.QName;
import java.util.*;

import static org.xdef.impl.util.conv.xd2schemas.xsd.XD2XsdDefinitions.XD_PARSER_EQ;
import static org.xdef.impl.util.conv.xd2schemas.xsd.XD2XsdDefinitions.XSD_NAMESPACE_PREFIX_EMPTY;
import static org.xdef.impl.util.conv.xd2schemas.xsd.util.AlgPhase.PREPROCESSING;
import static org.xdef.impl.util.conv.xd2schemas.xsd.util.AlgPhase.TRANSFORMATION;
import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.*;

class XDTree2XsdAdapter {

    final private XmlSchema schema;
    final private XsdElementFactory xsdFactory;


    private Map<String, XmlSchemaImportLocation> postprocessedSchemaLocations;

    /**
     * Nodes which will be created in post-procession
     * Key:     namespace URI
     * Value:   nodes
     */
    private Map<String, List<XNode>> postprocessedNodes;

    private Set<XMNode> xdProcessedNodes = null;
    private List<String> xdRootNames = null;

    protected XDTree2XsdAdapter(XmlSchema schema, XsdElementFactory xsdFactory) {
        this.schema = schema;
        this.xsdFactory = xsdFactory;
    }

    protected void initPostprocessing(final Map<String, List<XNode>> postprocessedNodes, final Map<String, XmlSchemaImportLocation> postprocessedSchemaLocations) {
        this.postprocessedNodes = postprocessedNodes != null ? postprocessedNodes : new HashMap<String, List<XNode>>();
        this.postprocessedSchemaLocations = postprocessedSchemaLocations;
    }

    protected List<String> getXdRootNames() {
        return xdRootNames;
    }

    public final Map<String, List<XNode>> getPostprocessedNodes() {
        return postprocessedNodes;
    }

    protected void loadXdefRootNames(final XDefinition def) {
        XsdLogger.printP(LOG_INFO, PREPROCESSING, def, "Loading root of x-definitions");

        xdRootNames = new ArrayList<String>();
        if (def._rootSelection != null) {
            for (String rootName : def._rootSelection.keySet()) {
                xdRootNames.add(rootName);
                XsdLogger.printP(LOG_DEBUG, PREPROCESSING, def, "Add root name. Name=" + rootName);
            }
        }
    }

    protected XmlSchemaObject convertTree(XNode xn) {
        xdProcessedNodes = new HashSet<XMNode>();
        return convertTreeInt(xn, true);
    }

    private XmlSchemaObject convertTreeInt(XNode xn, boolean topLevel) {

        if (!xdProcessedNodes.add(xn)) {
            XsdLogger.printP(LOG_DEBUG, TRANSFORMATION, xn, "Already processed. This node is reference probably");
            return null;
        }

        short xdElemKind = xn.getKind();
        switch (xdElemKind) {
            case XNode.XMATTRIBUTE: {
                return createAttribute((XData) xn);
            }
            case XNode.XMTEXT: {
                XsdLogger.printP(LOG_INFO, TRANSFORMATION, xn, "Creating simple content ...");
                return xsdFactory.createSimpleContent((XData)xn);
            }
            case XNode.XMELEMENT: {
                return createElement((XElement) xn, topLevel);
            }
            case XNode.XMSELECTOR_END:
                return null;
            case XNode.XMSEQUENCE:
            case XNode.XMMIXED:
            case XNode.XMCHOICE:
                XsdLogger.printP(LOG_DEBUG, TRANSFORMATION, xn, "Processing Particle node. Particle=" + XD2XsdUtils.particleXKindToString(xdElemKind));
                return xsdFactory.createGroupParticle(xdElemKind, xn.getOccurence());
            case XNode.XMDEFINITION: {
                XsdLogger.printP(LOG_ERROR, TRANSFORMATION, xn, "XDefinition node has to be only pre-processed!");
                return null;
            }
            default: {
                XsdLogger.printP(LOG_ERROR, TRANSFORMATION, xn, "Unknown type of node. NodeType=" + xdElemKind);
            }
        }

        return null;
    }

    private XmlSchemaAttribute createAttribute(final XData xData) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xData, "Creating attribute ...");

        XmlSchemaAttribute attr = new XmlSchemaAttribute(schema, false);
        final String refNsUri = xData.getNSUri();
        final String nodeName = xData.getName();
        if (refNsUri != null && XsdNamespaceUtils.isNodeInDifferentNamespace(nodeName, refNsUri, schema)) {
            final String localName = XsdNameUtils.getReferenceName(nodeName);
            final String nsPrefix = XsdNamespaceUtils.getNamespacePrefix(xData.getName());
            final String nsUri = schema.getNamespaceContext().getNamespaceURI(nsPrefix);

            // Attribute is referencing to new namespace, which will be created in post-processing
            if (postprocessedSchemaLocations.get(nsUri) != null) {
                addPostprocessingNode(nsUri, xData);
            }

            attr.getRef().setTargetQName(new QName(refNsUri, localName));
            XsdLogger.printP(LOG_INFO, TRANSFORMATION, xData, "Creating attribute reference from different namespace." +
                    "Name=" + xData.getName() + ", Namespace=" + xData.getNSUri());
        } else {
            attr.setName(xData.getName());
            QName qName;

            if (xData.getRefTypeName() != null) {
                final String nsPrefix = XsdNamespaceUtils.getReferenceNamespacePrefix(xData.getRefTypeName());
                final String nsUri = schema.getNamespaceContext().getNamespaceURI(nsPrefix);
                attr.setSchemaTypeName(new QName(nsUri, xData.getRefTypeName()));
                XsdLogger.printP(LOG_INFO, TRANSFORMATION, xData, "Creating attribute reference in same namespace/x-definition." +
                        " Name=" + xData.getName() + ", Type=" + attr.getSchemaTypeName());
            } else if ((qName = XD2XsdUtils.getDefaultSimpleParserQName(xData)) != null) {
                attr.setSchemaTypeName(qName);
                XsdLogger.printP(LOG_INFO, TRANSFORMATION, xData, "Content of attribute contains only XSD datatype" +
                        "Element=" + xData.getName() + ", Type=" + qName);
            } else if (XD_PARSER_EQ.equals(xData.getParserName())) {
                qName = XD2XsdUtils.getDefaultQName(xData.getValueTypeName());
                attr.setFixedValue(xData.getFixedValue());
                attr.setSchemaTypeName(qName);
                XsdLogger.printP(LOG_INFO, TRANSFORMATION, xData, "Content of attribute contains datatype with fixed value" +
                        "Element=" + xData.getName() + ", Type=" + qName);
            } else {
                attr.setSchemaType(xsdFactory.creatSimpleType(xData));
            }

            XsdNameUtils.resolveAttributeQName(schema, attr, xData.getName());
        }

        if (xData.isOptional() || xData.getOccurence().isOptional()) {
            attr.setUse(XmlSchemaUse.OPTIONAL);
        } else if (xData.isRequired() || xData.getOccurence().isRequired()) {
            attr.setUse(XmlSchemaUse.REQUIRED);
        }

        return attr;
    }

    /**
     * Creates xs:element based on x-definition element (XNode.XMELEMENT)
     * @param xDefEl
     * @return
     */
    private XmlSchemaObject createElement(final XElement xDefEl, boolean topLevel) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xDefEl, "Creating element ...");

        XmlSchemaElement xsdElem = xsdFactory.createEmptyElement(xDefEl, topLevel);

        if (xDefEl.isReference()) {
            final String refPos = xDefEl.getReferencePos();
            if (XsdNamespaceUtils.isNodeInDifferentNamespace(xDefEl.getName(), xDefEl.getNSUri(), schema)) {
                xsdElem.getRef().setTargetQName(new QName(xDefEl.getNSUri(), xDefEl.getName()));
                XsdLogger.printP(LOG_INFO, TRANSFORMATION, xDefEl, "Creating element reference to different namespace." +
                        "Name=" + xDefEl.getName() + ", Namespace=" + xsdElem.getRef().getTargetQName());
            } else if (XsdNamespaceUtils.isRefInDifferentNamespacePrefix(refPos, schema)) {
                final String refSystemId = XsdNamespaceUtils.getReferenceSystemId(refPos);
                final String refLocalName = XsdNameUtils.getReferenceName(refPos);
                XmlSchema refSchema = XsdNamespaceUtils.getReferenceSchema(schema.getParent(), refSystemId, true, TRANSFORMATION);
                final String refNsPrefix = XsdNamespaceUtils.getReferenceNamespacePrefix(refPos);
                final String nsUri = refSchema.getNamespaceContext().getNamespaceURI(refNsPrefix);

                if (topLevel) {
                    xsdElem.getRef().setTargetQName(new QName(nsUri, refLocalName));
                } else {
                    XsdLogger.printP(LOG_INFO, TRANSFORMATION, xDefEl, "Creating element reference to different x-definition and namespace" +
                            " Name=" + xDefEl.getName() + ", XDefinition=" + refSystemId + ", Namespace=" + xsdElem.getRef().getTargetQName());
                    XsdPostProcessor.elementRef2Complex(xsdElem, xDefEl, refNsPrefix, nsUri, refLocalName, xsdFactory);
                }
            } else if (XsdNamespaceUtils.isRefInDifferentSystem(refPos, xDefEl.getXDPosition())) {
                final String refSystemId = XsdNamespaceUtils.getReferenceSystemId(refPos);
                final String refLocalName = XsdNameUtils.getReferenceName(refPos);

                xsdElem.getRef().setTargetQName(new QName(XSD_NAMESPACE_PREFIX_EMPTY, refLocalName));
                XsdLogger.printP(LOG_INFO, TRANSFORMATION, xDefEl, "Creating element reference from different x-definition." +
                        " Name=" + xDefEl.getName() + ", XDefinition=" + refSystemId);
            } else {
                final String localName = XsdNameUtils.getReferenceName(refPos);
                final String refNamespace = XsdNamespaceUtils.getReferenceNamespacePrefix(refPos);

                xsdElem.setName(xDefEl.getName());
                xsdElem.setSchemaTypeName(new QName(refNamespace, localName));
                XsdNameUtils.resolveElementQName(schema, xsdElem);
                //xsdElem.getRef().setTargetQName(new QName(XSD_NAMESPACE_PREFIX_EMPTY, localName));
                XsdLogger.printP(LOG_INFO, TRANSFORMATION, xDefEl, "Creating element schema type name in same namespace/x-definition" +
                        "Name=" + localName);
            }
        } else {
            // Element is not reference but name contains different namespace -> we will have to create reference in new namespace in post-processing
            if (XsdNamespaceUtils.isNodeInDifferentNamespacePrefix(xDefEl.getName(), schema)) {
                final String localName = XsdNameUtils.getReferenceName(xDefEl.getName());
                final String nsPrefix = XsdNamespaceUtils.getNamespacePrefix(xDefEl.getName());
                final String nsUri = schema.getNamespaceContext().getNamespaceURI(nsPrefix);
                if (nsUri == null || nsUri.isEmpty()) {
                    XsdLogger.printP(LOG_ERROR, TRANSFORMATION, xDefEl, "Element refers to unknown namespace!" +
                            " NamespacePrefix=" + nsPrefix);
                } else {
                    xsdElem.getRef().setTargetQName(new QName(nsUri, localName));
                    addPostprocessingNode(nsUri, xDefEl);
                }
            } else {
                xsdElem.setName(xDefEl.getName());

                // If element contains only data, we dont have to create complexType
                if (xDefEl.getXDAttrs().length == 0 && xDefEl._childNodes.length == 1 && xDefEl._childNodes[0].getKind() == XNode.XMTEXT) {
                    addSimpleContentToElem(xsdElem, (XData) xDefEl._childNodes[0]);
                } else {
                    addComplexContentToElem(xsdElem, xDefEl);
                }

                XsdNameUtils.resolveElementQName(schema, xsdElem);
            }
        }

        return xsdElem;
    }

    private void addSimpleContentToElem(final XmlSchemaElement xsdElem, final XData xd) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xd, "Creating simple content of element. " +
                "Element=" + xsdElem.getName());

        final QName qName = XD2XsdUtils.getDefaultSimpleParserQName(xd);
        if (qName != null) {
            xsdElem.setSchemaTypeName(qName);
            XsdLogger.printP(LOG_DEBUG, TRANSFORMATION, xd, "Content of element contains only XSD datatype" +
                    "Element=" + xsdElem.getName() + ", DataType=" + qName.getLocalPart());
        } else {
            xsdElem.setType(xsdFactory.creatSimpleType(xd));
        }
    }

    private void addComplexContentToElem(final XmlSchemaElement xsdElem, final XElement defEl) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, defEl, "Creating complex content of element...");

        XmlSchemaComplexType complexType = xsdFactory.createEmptyComplexType(false);

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
                    // TODO: XD->XSD Solve?
                    XsdLogger.printP(LOG_WARN, TRANSFORMATION, defEl, "Contains multiple particle group inside element!");
                }

                XsdLogger.printP(LOG_DEBUG, TRANSFORMATION, defEl, "Creating particle to complex content of element. Particle=" + XD2XsdUtils.particleXKindToString(childrenKind));
                group = (XmlSchemaGroupParticle) convertTreeInt(xnChild, false);
            } else if (childrenKind == XNode.XMTEXT) { // Simple value node
                XmlSchemaSimpleContent simpleContent = (XmlSchemaSimpleContent) convertTreeInt(xnChild, false);
                if (simpleContent != null && simpleContent.getContent() instanceof XmlSchemaSimpleContentExtension) {
                    XsdLogger.printP(LOG_DEBUG, TRANSFORMATION, defEl, "Add simple content with attributes to complex content of element.");

                    complexType.setContentModel(simpleContent);

                    for (int j = 0; j < attrs.length; j++) {
                        if (simpleContent.getContent() instanceof XmlSchemaSimpleContentExtension)
                            ((XmlSchemaSimpleContentExtension) simpleContent.getContent()).getAttributes().add((XmlSchemaAttributeOrGroupRef) convertTreeInt(attrs[j], false));
                    }

                    hasSimpleContent = true;
                } else {
                    XsdLogger.printP(LOG_WARN, TRANSFORMATION, defEl, "Content of XText is not simple!");
                }
            } else {
                XmlSchemaObject xsdChild = convertTreeInt(xnChild, false);
                if (xsdChild != null) {
                    XsdLogger.printP(LOG_DEBUG, TRANSFORMATION, defEl, "Add child to particle of element.");

                    // x-definition has no required group
                    if (group == null) {
                        group = new XmlSchemaSequence();
                        XsdLogger.printP(LOG_DEBUG, TRANSFORMATION, defEl, "Particle is undefined. Creating sequence particle.");
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

        XsdPostProcessor.elementComplexContent(defEl, complexType);

        if (hasSimpleContent == false) {
            XsdLogger.printP(LOG_DEBUG, TRANSFORMATION, defEl, "Add attributes to complex content of element");

            for (int i = 0; i < attrs.length; i++) {
                complexType.getAttributes().add((XmlSchemaAttributeOrGroupRef) convertTreeInt(attrs[i], false));
            }
        }

        xsdElem.setType(complexType);
    }

    private void addPostprocessingNode(final String nsUri, final XNode xNode) {
        if (postprocessedNodes.containsKey(nsUri)) {
            postprocessedNodes.get(nsUri).add(xNode);
        } else {
            postprocessedNodes.put(nsUri, new ArrayList<XNode>(Arrays.asList(xNode)));
        }
    }

}
