package org.xdef.impl.util.conv.xd2schemas.xsd;

import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.xdef.impl.XData;
import org.xdef.impl.XDefinition;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.XsdElementFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.SchemaRefNode;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.XmlSchemaImportLocation;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.*;
import org.xdef.model.XMNode;

import javax.xml.namespace.QName;
import java.util.*;

import static org.xdef.impl.util.conv.xd2schemas.xsd.XD2XsdDefinitions.XD_PARSER_EQ;
import static org.xdef.impl.util.conv.xd2schemas.xsd.XD2XsdDefinitions.XSD_NAMESPACE_PREFIX_EMPTY;
import static org.xdef.impl.util.conv.xd2schemas.xsd.util.AlgPhase.*;
import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.*;

class XDTree2XsdAdapter {

    final private XmlSchema schema;
    final private String schemaName;
    final private XsdElementFactory xsdFactory;
    final private Map<String, Map<String, SchemaRefNode>> xsdReferences;

    private Map<String, XmlSchemaImportLocation> postProcessedSchemaLocations;

    /**
     * Nodes which will be created in post-procession
     * Key:     namespace URI
     * Value:   nodes
     */
    private Map<String, List<XNode>> nodesToBePostProcessed;

    private Set<XMNode> xdProcessedNodes = null;
    private List<String> xdRootNames = null;

    protected XDTree2XsdAdapter(XmlSchema schema, String schemaName, XsdElementFactory xsdFactory, Map<String, Map<String, SchemaRefNode>> xsdReferences) {
        this.schema = schema;
        this.schemaName = schemaName;
        this.xsdFactory = xsdFactory;
        this.xsdReferences = xsdReferences;
    }

    protected void initPostprocessing(final Map<String, List<XNode>> postprocessedNodes, final Map<String, XmlSchemaImportLocation> postprocessedSchemaLocations) {
        this.nodesToBePostProcessed = postprocessedNodes != null ? postprocessedNodes : new HashMap<String, List<XNode>>();
        this.postProcessedSchemaLocations = postprocessedSchemaLocations;
    }

    protected List<String> getXdRootNames() {
        return xdRootNames;
    }

    public final Map<String, List<XNode>> getNodesToBePostProcessed() {
        return nodesToBePostProcessed;
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

        XmlSchemaAttribute attr = xsdFactory.createEmptyAttribute(xData, false);
        final String refNsUri = xData.getNSUri();
        final String nodeName = xData.getName();
        if (refNsUri != null && XsdNamespaceUtils.isNodeInDifferentNamespace(nodeName, refNsUri, schema)) {
            final String localName = XsdNameUtils.getReferenceName(nodeName);
            final String nsPrefix = XsdNamespaceUtils.getNamespacePrefix(xData.getName());
            final String nsUri = schema.getNamespaceContext().getNamespaceURI(nsPrefix);

            // Attribute is referencing to new namespace, which will be created in post-processing
            if (postProcessedSchemaLocations.get(nsUri) != null) {
                addNodeToPostProcessing(nsUri, xData);
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
            final String refXPos = xDefEl.getReferencePos();
            final String xPos = xDefEl.getXDPosition();

            final String refSystemId = XsdNamespaceUtils.getReferenceSystemId(refXPos);
            final String refLocalName = XsdNameUtils.getReferenceName(refXPos);

            if (XsdNamespaceUtils.isNodeInDifferentNamespace(xDefEl.getName(), xDefEl.getNSUri(), schema)) {
                final String nsUri = xDefEl.getNSUri();
                final String nsPrefix = schema.getNamespaceContext().getPrefix(nsUri);
                if (nsPrefix == null) {
                    XmlSchema refSchema = XsdNamespaceUtils.getSchema(schema.getParent(), refSystemId, true, TRANSFORMATION);
                    final String refNsPrefix = XsdNamespaceUtils.getReferenceNamespacePrefix(refXPos);
                    final String refNsUri = refSchema.getNamespaceContext().getNamespaceURI(refNsPrefix);
                    if (refNsUri == null || refNsUri.isEmpty()) {
                        XsdLogger.printP(LOG_ERROR, TRANSFORMATION, xDefEl, "Element referencing to unknown namespace! NamespacePrefix=" + nsPrefix);
                    } else {
                        XsdNamespaceUtils.addNamespaceToCtx((NamespaceMap)schema.getNamespaceContext(), null, refNsPrefix, refNsUri, POSTPROCESSING);
                    }
                }
                final QName qName = new QName(nsUri, xDefEl.getName());

                xsdElem.getRef().setTargetQName(qName);

                XsdLogger.printP(LOG_INFO, TRANSFORMATION, xDefEl, "Element referencing to different namespace." +
                        "Name=" + xDefEl.getName() + ", RefQName=" + xsdElem.getRef().getTargetQName());
            } else if (XsdNamespaceUtils.isRefInDifferentNamespacePrefix(refXPos, schema)) {
                XmlSchema refSchema = XsdNamespaceUtils.getSchema(schema.getParent(), refSystemId, true, TRANSFORMATION);
                final String refNsPrefix = XsdNamespaceUtils.getReferenceNamespacePrefix(refXPos);
                final String nsUri = refSchema.getNamespaceContext().getNamespaceURI(refNsPrefix);

                xsdElem.getRef().setTargetQName(new QName(nsUri, refLocalName));

                XsdLogger.printP(LOG_INFO, TRANSFORMATION, xDefEl, "Element referencing to different x-definition and namespace" +
                        " XDefinition=" + refSystemId + ", RefQName=" + xsdElem.getRef().getTargetQName());
            } else if (XsdNamespaceUtils.isRefInDifferentSystem(refXPos, xPos)) {
                final QName refQName = new QName(XSD_NAMESPACE_PREFIX_EMPTY, refLocalName);

                xsdElem.getRef().setTargetQName(refQName);

                XsdLogger.printP(LOG_INFO, TRANSFORMATION, xDefEl, "Element referencing to different x-definition. XDefinition=" + refSystemId);
            } else {
                final String refNamespace = XsdNamespaceUtils.getReferenceNamespacePrefix(refXPos);
                final QName refQName = new QName(refNamespace, refLocalName);

                xsdElem.setName(xDefEl.getName());
                xsdElem.setSchemaTypeName(refQName);
                XsdNameUtils.resolveElementQName(schema, xsdElem);

                XsdLogger.printP(LOG_INFO, TRANSFORMATION, xDefEl, "Element referencing to same namespace/x-definition. Name=" + refLocalName);
            }

            final String refNodePath = XsdNameUtils.getReferenceNodePath(refXPos);
            XsdReferenceUtils.createRefAndDef(xDefEl, xsdElem, refSystemId, refXPos, refNodePath, xsdReferences);
        } else {
            // Element is not reference but name contains different namespace -> we will have to create reference in new namespace in post-processing
            if (XsdNamespaceUtils.isNodeInDifferentNamespacePrefix(xDefEl.getName(), schema)) {
                final String localName = XsdNameUtils.getReferenceName(xDefEl.getName());
                String nsPrefix = XsdNamespaceUtils.getNamespacePrefix(xDefEl.getName());
                String nsUri = schema.getNamespaceContext().getNamespaceURI(nsPrefix);

                // Post-processing
                if (nsUri != null && !nsUri.isEmpty()) {
                    xsdElem.getRef().setTargetQName(new QName(nsUri, localName));
                    addNodeToPostProcessing(nsUri, xDefEl);
                } else {
                    final String xDefPos = xDefEl.getXDPosition();
                    final String systemId = XsdNamespaceUtils.getReferenceSystemId(xDefPos);
                    XmlSchema refSchema = XsdNamespaceUtils.getSchema(schema.getParent(), systemId, true, PREPROCESSING);
                    nsPrefix = XsdNamespaceUtils.getReferenceNamespacePrefix(xDefPos);
                    nsUri = refSchema.getNamespaceContext().getNamespaceURI(nsPrefix);
                    if (nsUri == null || nsUri.isEmpty()) {
                        XsdLogger.printP(LOG_ERROR, TRANSFORMATION, xDefEl, "Element referencing to unknown namespace! NamespacePrefix=" + nsPrefix);
                    } else {
                        xsdElem.getRef().setTargetQName(new QName(nsUri, localName));

                        final String refNodePath = XsdNameUtils.getReferenceNodePath(xDefPos);
                        XsdReferenceUtils.createRefAndDef(xDefEl, xsdElem, schemaName, refNodePath, systemId, xDefPos, refNodePath, xsdReferences);
                    }
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

                SchemaRefNode node = XsdReferenceUtils.createNode(xsdElem, xDefEl);
                XsdReferenceUtils.addNode(node, xsdReferences, false);
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

        Stack<XmlSchemaGroupParticle> groups = new Stack<XmlSchemaGroupParticle>();
        XmlSchemaGroupParticle currGroup = null;

        boolean hasSimpleContent = false;
        XData[] attrs = defEl.getXDAttrs();

        // Convert all children nodes
        for (XNode xnChild : defEl._childNodes) {
            short childrenKind = xnChild.getKind();
            // Particle nodes (sequence, choice, all)
            if (childrenKind == XNode.XMSEQUENCE || childrenKind == XNode.XMMIXED || childrenKind == XNode.XMCHOICE) {
                XsdLogger.printP(LOG_DEBUG, TRANSFORMATION, defEl, "Creating particle to complex content of element. Particle=" + XD2XsdUtils.particleXKindToString(childrenKind));
                XmlSchemaGroupParticle newGroup = (XmlSchemaGroupParticle) convertTreeInt(xnChild, false);
                if (currGroup != null) {
                    addNodeToParticleGroup(currGroup, newGroup);
                }
                groups.push(newGroup);
                currGroup = newGroup;
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
            } else if (childrenKind == XNode.XMSELECTOR_END) {
                currGroup = groups.pop();
                if (!groups.empty()) {
                    currGroup = groups.peek();
                }
            } else {
                XmlSchemaObject xsdChild = convertTreeInt(xnChild, false);
                if (xsdChild != null) {
                    XsdLogger.printP(LOG_DEBUG, TRANSFORMATION, defEl, "Add child to particle of element.");

                    // x-definition has no particle group yet
                    if (currGroup == null) {
                        XsdLogger.printP(LOG_DEBUG, TRANSFORMATION, defEl, "Particle group is undefined. Creating sequence particle by default.");
                        currGroup = new XmlSchemaSequence();
                        groups.push(currGroup);
                    }

                    addNodeToParticleGroup(currGroup, xsdChild);
                }
            }
        }

        if (currGroup != null) {
            complexType.setParticle(currGroup);
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

    private void addNodeToPostProcessing(final String nsUri, final XNode xNode) {
        if (nodesToBePostProcessed.containsKey(nsUri)) {
            nodesToBePostProcessed.get(nsUri).add(xNode);
        } else {
            nodesToBePostProcessed.put(nsUri, new ArrayList<XNode>(Arrays.asList(xNode)));
        }
    }

    private void addNodeToParticleGroup(final XmlSchemaGroupParticle currGroup, final XmlSchemaObject xsdNode) {
        if (currGroup instanceof XmlSchemaSequence) {
            ((XmlSchemaSequence) currGroup).getItems().add((XmlSchemaSequenceMember) xsdNode);
        } else if (currGroup instanceof XmlSchemaChoice) {
            ((XmlSchemaChoice) currGroup).getItems().add((XmlSchemaChoiceMember) xsdNode);
        } else if (currGroup instanceof XmlSchemaAll) {
            ((XmlSchemaAll) currGroup).getItems().add((XmlSchemaAllMember) xsdNode);
        }
    }

}
