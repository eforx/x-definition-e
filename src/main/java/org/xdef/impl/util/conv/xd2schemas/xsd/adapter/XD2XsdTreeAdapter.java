package org.xdef.impl.util.conv.xd2schemas.xsd.adapter;

import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.xdef.XDPool;
import org.xdef.impl.XData;
import org.xdef.impl.XDefinition;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.SchemaNodeFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.XsdElementFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.XsdNameFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.SchemaNode;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.UniqueConstraints;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.XsdAdapterCtx;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.XsdSchemaImportLocation;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.xsd.CXmlSchemaChoice;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.xsd.CXmlSchemaGroupParticle;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.xsd.CXmlSchemaSequence;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.*;
import org.xdef.model.XMData;
import org.xdef.model.XMNode;
import org.xdef.model.XMVariable;
import org.xdef.model.XMVariableTable;

import javax.xml.namespace.QName;
import java.util.*;

import static org.xdef.impl.compile.CompileBase.UNIQUESET_M_VALUE;
import static org.xdef.impl.compile.CompileBase.UNIQUESET_VALUE;
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.AlgPhase.*;
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XD2XsdDefinitions.XD_PARSER_EQ;
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XD2XsdDefinitions.XSD_NAMESPACE_PREFIX_EMPTY;
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XsdLoggerDefs.*;

public class XD2XsdTreeAdapter {

    /**
     * Output XSD schema
     */
    final private XmlSchema schema;

    /**
     * Output XSD schema name
     */
    final private String schemaName;

    /**
     * XSD element factory
     */
    final private XsdElementFactory xsdFactory;

    /**
     * XSD adapter context
     */
    final private XsdAdapterCtx adapterCtx;

    /**
     * XSD post processor for advanced transformation
     */
    final private XsdPostProcessor postProcessor;

    /**
     * Flag if post processing phase has been reached
     */
    private boolean isPostProcessingPhase = false;

    /**
     * Already processed nodes. We dont want to process same node multiple times (ie. references)
     */
    private Set<XMNode> xdProcessedNodes = null;

    public XD2XsdTreeAdapter(XmlSchema schema, String schemaName, XsdElementFactory xsdFactory, XsdAdapterCtx adapterCtx) {
        this.schema = schema;
        this.schemaName = schemaName;
        this.xsdFactory = xsdFactory;
        this.adapterCtx = adapterCtx;
        this.postProcessor = new XsdPostProcessor(adapterCtx);
    }

    /**
     * Set flag if post processing phase has been reached
     */
    public void setPostProcessing() {
        this.isPostProcessingPhase = true;
    }

    /**
     * Gather names of all x-definition root element nodes
     * @param xDef  input x-definition
     */
    public void loadXdefRootNames(final XDefinition xDef) {
        XsdLogger.printP(LOG_INFO, PREPROCESSING, xDef, "Loading root of x-definition");
        if (xDef._rootSelection != null) {
            for (String rootName : xDef._rootSelection.keySet()) {
                adapterCtx.addRootNodeName(schemaName, rootName);
                XsdLogger.printP(LOG_DEBUG, PREPROCESSING, xDef, "Add root name. Name=" + rootName);
            }
        }
    }

    /**
     * Gather data of all global and local uniqueSets used by x-definition
     * @param xDef  input x-definition
     */
    public void loadXdefRootUniqueSets(final XDefinition xDef) {
        loadUniqueSets(xDef, xDef.getXDPool().getVariableTable(), "");
    }

    /**
     * Gather data of uniqueSets used by x-definition element node
     * @param xElem input x-definition element node
     */
    public void loadElementUniqueSets(final XElement xElem) {
        loadUniqueSets(xElem.getDefinition(), xElem._vartable, XsdNameUtils.getXNodePath(xElem.getXDPosition()));
    }

    /**
     * Gather data of uniqueSets from variable table
     * @param xNode             container of variable table
     * @param varTable          input variable table
     * @param varTablePath      path of variable table
     */
    private void loadUniqueSets(final XNode xNode, final XMVariableTable varTable, final String varTablePath) {
        if (varTable != null && varTable.size() > 0) {
            XsdLogger.printP(LOG_INFO, PREPROCESSING, xNode, "Loading unique sets of x-definition");
            for (XMVariable xmVariable : varTable.toArray()) {
                if ((xmVariable.getType() == UNIQUESET_M_VALUE || xmVariable.getType() == UNIQUESET_VALUE) && xmVariable.getOffset() != -1) {
                    adapterCtx.addUniqueInfo(XsdNameUtils.getReferenceName(xmVariable.getName()), XsdNamespaceUtils.getSystemIdFromXPos(xmVariable.getName()), varTablePath);
                }
            }
        }
    }

    /**
     * Transforms x-definition node tree into XSD node tree.
     * @param xNode     root of x-definition tree
     * @return root of XSD node tree, may return null
     */
    public XmlSchemaObject convertTree(XNode xNode) {
        xdProcessedNodes = new HashSet<XMNode>();
        return convertTreeInt(xNode, true);
    }

    /**
     * Transforms x-definition node tree into XSD node tree.
     * @param xNode     root of x-definition tree
     * @param topLevel  flag if x-definition node is placed on top level
     * @return root of XSD node tree, may return null
     */
    private XmlSchemaObject convertTreeInt(final XNode xNode, boolean topLevel) {
        if (!xdProcessedNodes.add(xNode)) {
            XsdLogger.printP(LOG_DEBUG, TRANSFORMATION, xNode, "Already processed. This node should be reference definition");
            return null;
        }

        short xdElemKind = xNode.getKind();
        switch (xdElemKind) {
            case XNode.XMATTRIBUTE: {
                return createAttribute((XData) xNode, topLevel);
            }
            case XNode.XMTEXT: {
                XsdLogger.printP(LOG_INFO, TRANSFORMATION, xNode, "Creating simple (text) content ...");
                return xsdFactory.createSimpleContentWithExtension((XData)xNode);
            }
            case XNode.XMELEMENT: {
                return createElement((XElement) xNode, topLevel);
            }
            case XNode.XMSELECTOR_END:
                return null;
            case XNode.XMSEQUENCE:
            case XNode.XMMIXED:
            case XNode.XMCHOICE:
                XsdLogger.printP(LOG_DEBUG, TRANSFORMATION, xNode, "Processing Particle node. Particle=" + XD2XsdUtils.particleXKindToString(xdElemKind));
                return xsdFactory.createGroupParticle(xNode);
            case XNode.XMDEFINITION: {
                XsdLogger.printP(LOG_WARN, TRANSFORMATION, xNode, "XDefinition node has to be only pre-processed!");
                return null;
            }
            default: {
                XsdLogger.printP(LOG_WARN, TRANSFORMATION, xNode, "Unknown type of node. NodeType=" + xdElemKind);
            }
        }

        return null;
    }

    /**
     * Creates XSD attribute node based on x-definition attribute node
     * @param xData     x-definition source (attribute node)
     * @param topLevel  flag if source node is placed on top level
     * @return XSD attribute node
     */
    private XmlSchemaAttribute createAttribute(final XData xData, boolean topLevel) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xData, "Creating attribute ...");

        final XmlSchemaAttribute attr = xsdFactory.createEmptyAttribute(xData, topLevel);
        final String refNsUri = xData.getNSUri();
        final String nodeName = xData.getName();
        if (XsdNamespaceUtils.isNodeInDifferentNamespace(nodeName, refNsUri, schema)) {
            final String localName = XsdNameUtils.getReferenceName(nodeName);
            final String nsPrefix = XsdNamespaceUtils.getNamespacePrefix(xData.getName());
            final String nsUri = schema.getNamespaceContext().getNamespaceURI(nsPrefix);

            attr.getRef().setTargetQName(new QName(refNsUri, localName));

            XsdLogger.printP(LOG_INFO, TRANSFORMATION, xData, "Creating attribute reference from different namespace. " +
                    "Name=" + xData.getName() + ", Namespace=" + refNsUri);

            // Attribute is referencing to new namespace, which will be created in post-processing
            if (adapterCtx.isPostProcessingNamespace(nsUri)) {
                adapterCtx.addNodeToPostProcessing(nsUri, xData);
            } else {
                SchemaNode node = SchemaNodeFactory.createAttributeNode(attr, xData);
                adapterCtx.addOrUpdateNode(node);
            }
        } else {
            final UniqueConstraints uniqueConstraints = adapterCtx.findUniqueInfo(xData);
            if (uniqueConstraints != null) {
                attr.setAnnotation(XsdElementFactory.createAnnotation("Original part of uniqueSet: " + uniqueConstraints.getPath(), adapterCtx));
            }
            attr.setName(xData.getName());
            QName qName;

            if (xData.getRefTypeName() != null && uniqueConstraints == null) {
                String refTypeName = adapterCtx.getNameFactory().findTopLevelName(xData, false);
                if (refTypeName == null) {
                    refTypeName = XsdNameFactory.createLocalSimpleTypeName(xData);
                    adapterCtx.getNameFactory().addTopSimpleTypeName(xData, refTypeName);
                }

                String nsPrefix = XsdNamespaceUtils.getReferenceNamespacePrefix(refTypeName);
                if (topLevel && isPostProcessingPhase && XSD_NAMESPACE_PREFIX_EMPTY.equals(nsPrefix) && XsdNamespaceUtils.containsNsPrefix(xData.getName())) {
                    nsPrefix = schema.getSchemaNamespacePrefix();
                }

                final String nsUri = schema.getNamespaceContext().getNamespaceURI(nsPrefix);
                attr.setSchemaTypeName(new QName(nsUri, refTypeName));
                XsdLogger.printP(LOG_INFO, TRANSFORMATION, xData, "Creating attribute reference in same namespace/x-definition." +
                        " Name=" + xData.getName() + ", Type=" + attr.getSchemaTypeName());
            } else if ((qName = XD2XsdParserMapping.getDefaultSimpleParserQName(xData, adapterCtx)) != null) {
                attr.setSchemaTypeName(qName);
                XsdLogger.printP(LOG_INFO, TRANSFORMATION, xData, "Content of attribute contains only XSD datatype. " +
                        "Element=" + xData.getName() + ", Type=" + qName);
            } else if (XD_PARSER_EQ.equals(xData.getParserName())) {
                qName = XD2XsdParserMapping.getDefaultParserQName(xData.getValueTypeName(), adapterCtx);
                final String fixedValue = xData.getFixedValue();
                if (fixedValue != null) {
                    attr.setFixedValue(fixedValue);
                }
                attr.setSchemaTypeName(qName);
                XsdLogger.printP(LOG_INFO, TRANSFORMATION, xData, "Content of attribute contains datatype with fixed value. " +
                        "Element=" + xData.getName() + ", Type=" + qName + ", FixedValue=" + fixedValue);
            } else {
                // Attributes using unique set should not contain simple-type with name
                if (uniqueConstraints != null) {
                    final String parserName = xData.getParserName();
                    qName = XD2XsdParserMapping.getDefaultParserQName(parserName, adapterCtx);
                    attr.setSchemaTypeName(qName);
                } else {
                    final XmlSchemaSimpleType simpleType = xsdFactory.createSimpleType(xData, attr.getName());
                    if (simpleType.getContent() instanceof XmlSchemaSimpleTypeRestriction) {
                        postProcessor.simpleTypeRestrictionToAttr((XmlSchemaSimpleTypeRestriction)simpleType.getContent(), attr);
                    }
                    if (attr.getSchemaTypeName() == null) {
                        attr.setSchemaType(simpleType);
                    }
                }
            }

            final String defaultValue = xData.getDefaultValue();
            if (defaultValue != null) {
                attr.setDefaultValue(defaultValue);
            }

            XsdNameUtils.resolveAttributeQName(schema, attr, xData.getName());

            SchemaNode node = SchemaNodeFactory.createAttributeNode(attr, xData);
            adapterCtx.addOrUpdateNode(node);
        }

        return attr;
    }

    /**
     * Creates XSD element node based on x-definition element node
     * @param xElem     x-definition source (element node)
     * @param topLevel  flag if source node is placed on top level
     * @return XSD element node
     */
    private XmlSchemaObject createElement(final XElement xElem, boolean topLevel) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xElem, "Creating element ...");

        loadElementUniqueSets(xElem);

        final XmlSchemaElement xsdElem = xsdFactory.createEmptyElement(xElem, topLevel);

        if (!XD2XsdUtils.isAnyElement(xElem) && (xElem.isReference() || xElem.getReferencePos() != null)) {
            final QName referenceQName = getRefQName(xElem);
            if (xElem.isReference()) {
                createElementReference(xElem, referenceQName, xsdElem);
            } else {
                createElementExtendedReference(xElem, referenceQName, topLevel, xsdElem);
            }
        } else {
            // Element is not reference but name contains different namespace -> we will have to create reference in new namespace in post-processing
            if (XsdNamespaceUtils.isNodeInDifferentNamespacePrefix(xElem, schema)) {
                createElementInDiffNamespace(xElem, xsdElem);
            } else {

                if (XD2XsdUtils.isAnyElement(xElem)) {
                    final XmlSchemaAny xsdAny = xsdFactory.createAny(xElem);
                    // TODO: should has zero occurs by default?
                    xsdAny.setMinOccurs(0);
                    xsdAny.setProcessContent(XmlSchemaContentProcessing.LAX);
                    if (xElem._attrs.size() > 0 || xElem._childNodes.length > 0) {
                        xsdAny.setAnnotation(XsdElementFactory.createAnnotation("Original any element contains children nodes/attributes", adapterCtx));
                        XsdLogger.printP(LOG_WARN, TRANSFORMATION, xElem, "!Lossy transformation! Any type with attributes/children nodes is not supported!");
                    }

                    return xsdAny;
                } else if (topLevel && XD2XsdUtils.containsAnyElement(xElem)) {
                    XsdLogger.printP(LOG_WARN, TRANSFORMATION, xElem, "Any element cannot be root element of xsd!");
                }

                fillXsdElement(xElem, topLevel, xsdElem);
            }
        }

        return xsdElem;
    }

    /**
     * Creates XSD element node using reference
     * @param xElem     x-definition source (element node)
     * @param refQName  reference QName
     * @param xsdElem   XSD element node which will be filled (output)
     */
    private void createElementReference(final XElement xElem, final QName refQName, final XmlSchemaElement xsdElem) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xElem, "Creating element reference ...");

        final String refXPos = xElem.getReferencePos();
        final String xPos = xElem.getXDPosition();

        final String refSystemId = XsdNamespaceUtils.getSystemIdFromXPos(refXPos);
        final String refLocalName = XsdNameUtils.getReferenceName(refXPos);

        if (XsdNamespaceUtils.isNodeInDifferentNamespace(xElem.getName(), xElem.getNSUri(), schema)) {
            xsdElem.getRef().setTargetQName(refQName);
            XsdLogger.printP(LOG_INFO, TRANSFORMATION, xElem, "Element referencing to different namespace." +
                    "Name=" + xElem.getName() + ", RefQName=" + xsdElem.getRef().getTargetQName());
        } else if (XsdNamespaceUtils.isRefInDifferentNamespacePrefix(refXPos, schema)) {
            xsdElem.getRef().setTargetQName(refQName);
            XsdLogger.printP(LOG_INFO, TRANSFORMATION, xElem, "Element referencing to different x-definition and namespace" +
                    " XDefinition=" + refSystemId + ", RefQName=" + xsdElem.getRef().getTargetQName());
        } else if (XsdNamespaceUtils.isRefInDifferentSystem(refXPos, xPos)) {
            xsdElem.getRef().setTargetQName(refQName);
            XsdLogger.printP(LOG_INFO, TRANSFORMATION, xElem, "Element referencing to different x-definition. XDefinition=" + refSystemId);
        } else if (XD2XsdUtils.isAnyElement(xElem)) {
            xsdElem.getRef().setTargetQName(refQName);
        } else {
            xsdElem.setName(xElem.getName());
            xsdElem.setSchemaTypeName(refQName);
            XsdNameUtils.resolveElementQName(schema, xElem, xsdElem, adapterCtx);
            XsdLogger.printP(LOG_INFO, TRANSFORMATION, xElem, "Element referencing to same namespace/x-definition. Name=" + refLocalName);
        }

        final String refNodePath = XsdNameUtils.getXNodePath(refXPos);
        SchemaNodeFactory.createElemRefAndDef(xElem, xsdElem, refSystemId, refXPos, refNodePath, adapterCtx);
    }

    /**
     * Creates XSD element node using reference, where source x-definition node is extending the reference
     * @param xElem     x-definition source (element node)
     * @param refQName  reference QName
     * @param topLevel  flag if source node is placed on top level
     * @param xsdElem   XSD element node which will be filled (output)
     */
    private void createElementExtendedReference(final XElement xElem, final QName refQName, boolean topLevel, final XmlSchemaElement xsdElem) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xElem, "Creating element extended reference ...");

        final String refXPos = xElem.getReferencePos();
        final XDPool xdPool = xElem.getXDPool();
        boolean usingExtension = false;

        xsdElem.setName(xElem.getName());
        XsdNameUtils.resolveElementQName(schema, xElem, xsdElem, adapterCtx);

        if (xdPool == null) {
            XsdLogger.printP(LOG_ERROR, TRANSFORMATION, xElem, "XDPool is not set!");
        } else {
            XMNode xRefNode = xdPool.findModel(refXPos);
            if (xRefNode.getKind() != XNode.XMELEMENT) {
                XsdLogger.printP(LOG_ERROR, TRANSFORMATION, xElem, "Reference to node type element is expected!");
            } else {
                final XElement xRefElem = (XElement)xRefNode;
                final List<XData> extAttrs = new ArrayList<XData>();
                final List<XNode> extNodes = new ArrayList<XNode>();

                // Get only extending attrs and children nodes
                {
                    if (xElem._attrs.size() > xRefElem._attrs.size()) {
                        final XData[] xRefAttrs = xRefElem.getXDAttrs();
                        for (XData xAttr : xElem.getXDAttrs()) {
                            boolean duplicated = false;
                            for (XData xRefAttr : xRefAttrs) {
                                if (xRefAttr.getName().equals(xAttr.getName())) {
                                    duplicated = true;
                                    break;
                                }
                            }

                            if (!duplicated) {
                                extAttrs.add(xAttr);
                            }
                        }
                    }

                    if (xElem._childNodes.length > xRefElem._childNodes.length) {
                        final XNode[] xRefNodes = xRefElem._childNodes;
                        for (XNode xNode : xElem._childNodes) {
                            boolean duplicated = false;
                            for (XNode xRefChildNode : xRefNodes) {
                                if (xRefChildNode.getName().equals(xNode.getName())) {
                                    duplicated = true;
                                    break;
                                }
                            }

                            if (!duplicated) {
                                extNodes.add(xNode);
                            }
                        }
                    }
                }

                if (extNodes.size() > 0 || extAttrs.size() > 0) {
                    usingExtension = true;

                    final XmlSchemaComplexContentExtension complexContentExtension = xsdFactory.createEmptyComplexContentExtension(refQName);
                    final XmlSchemaComplexContent complexContent = xsdFactory.createComplexContent(complexContentExtension);
                    final XmlSchemaComplexType complexType = createComplexType(extAttrs.toArray(new XData[extAttrs.size()]), extNodes.toArray(new XNode[extNodes.size()]), xElem, topLevel);

                    complexContentExtension.setParticle(complexType.getParticle());
                    complexContentExtension.getAttributes().addAll(complexType.getAttributes());
                    complexType.setParticle(null);
                    complexType.getAttributes().clear();

                    complexType.setContentModel(complexContent);
                    xsdElem.setSchemaType(complexType);

                    final String refNodePath = XsdNameUtils.getXNodePath(refXPos);
                    final String refSystemId = XsdNamespaceUtils.getSystemIdFromXPos(refXPos);
                    SchemaNodeFactory.createComplexExtRefAndDef(xElem, complexContentExtension, refSystemId, refXPos, refNodePath, adapterCtx);
                }
            }
        }

        if (!usingExtension) {
            fillXsdElement(xElem, topLevel, xsdElem);
        }
    }

    /**
     * Creates XSD element where source x-definition node is using different namespace
     * @param xElem     x-definition source (element node)
     * @param xsdElem   XSD element node which will be filled (output)
     */
    private void createElementInDiffNamespace(final XElement xElem, final XmlSchemaElement xsdElem) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xElem, "Creating element in different namespace ...");

        final String localName = XsdNameUtils.getReferenceName(xElem.getName());
        final String xDefPos = xElem.getXDPosition();
        final String nodePath = XsdNameUtils.getXNodePath(xDefPos);
        String nsPrefix = XsdNamespaceUtils.getNamespacePrefix(xElem.getName());
        String nsUri = schema.getNamespaceContext().getNamespaceURI(nsPrefix);

        // Post-processing
        if (XsdNamespaceUtils.isValidNsUri(nsUri)) {
            xsdElem.getRef().setTargetQName(new QName(nsUri, localName));
            final XsdSchemaImportLocation importLocation = adapterCtx.getPostProcessingNsImport(nsUri);
            if (importLocation != null) {
                final String refSystemId = importLocation.getFileName();
                adapterCtx.addNodeToPostProcessing(nsUri, xElem);
                final String refNodePath = XsdNameUtils.getPostProcessingReferenceNodePath(xElem.getXDPosition());
                final String refNodePos = XsdNameUtils.getPostProcessingNodePos(refSystemId, refNodePath);
                SchemaNodeFactory.createElemRefAndDefDiffNamespace(xElem, xsdElem, schemaName, nodePath, refSystemId, refNodePos, refNodePath, adapterCtx);
            } else {
                XsdLogger.printP(LOG_WARN, TRANSFORMATION, xElem, "Element is in different namespace which is not marked for post-processing! Namespace=" + nsUri);
            }
        } else {
            nsUri = XsdNamespaceUtils.getNodeNamespaceUri(xElem, adapterCtx, TRANSFORMATION);

            if (XsdNamespaceUtils.isValidNsUri(nsUri)) {
                final String systemId = XsdNamespaceUtils.getSystemIdFromXPos(xDefPos);
                xsdElem.getRef().setTargetQName(new QName(nsUri, localName));
                SchemaNodeFactory.createElemRefAndDefDiffNamespace(xElem, xsdElem, schemaName, nodePath, systemId, xDefPos, nodePath, adapterCtx);
            } else {
                nsPrefix = XsdNamespaceUtils.getReferenceNamespacePrefix(xDefPos);
                XsdLogger.printP(LOG_ERROR, TRANSFORMATION, xElem, "Element referencing to unknown namespace! NamespacePrefix=" + nsPrefix);
            }
        }
    }

    /**
     * Creates content of standard XSD element node
     * @param xElem     x-definition source (element node)
     * @param topLevel  flag if source node is placed on top level
     * @param xsdElem   XSD element node which will be filled (output)
     */
    private void fillXsdElement(final XElement xElem, boolean topLevel, final XmlSchemaElement xsdElem) {
        xsdElem.setName(XsdNameUtils.getName(xElem));
        XsdNameUtils.resolveElementQName(schema, xElem, xsdElem, adapterCtx);

        // If element contains only data, we dont have to create complexType
        if (xElem._attrs.size() == 0 && xElem._childNodes.length == 1 && xElem._childNodes[0].getKind() == XNode.XMTEXT) {
            addSimpleTypeToElem(xsdElem, (XData) xElem._childNodes[0]);
        } else {
            final XmlSchemaComplexType complexType = createComplexType(xElem.getXDAttrs(), xElem._childNodes, xElem, topLevel);
            if (complexType.getContentModel() != null || complexType.getAttributes().size() > 0 || complexType.getParticle() != null) {
                xsdElem.setType(complexType);
            }
        }

        SchemaNode node = SchemaNodeFactory.createElementNode(xsdElem, xElem);
        if (isPostProcessingPhase) {
            String nsPrefix = XsdNamespaceUtils.getNamespacePrefix(xElem.getName());
            String nsUri = schema.getNamespaceContext().getNamespaceURI(nsPrefix);
            final XsdSchemaImportLocation importLocation = adapterCtx.getPostProcessingNsImport(nsUri);
            if (importLocation != null) {
                final String systemId = importLocation.getFileName();
                adapterCtx.addOrUpdateNodeInDiffNs(node, systemId);
            } else {
                adapterCtx.addOrUpdateNode(node);
            }
        } else {
            adapterCtx.addOrUpdateNode(node);
        }

    }

    /**
     * Determines reference QName based on x-definition element node
     * @param xElem x-definition source (element node)
     * @return reference QName
     */
    private QName getRefQName(final XElement xElem) {
        XsdLogger.printP(LOG_DEBUG, TRANSFORMATION, xElem, "Creating element reference QName ...");

        final String refXPos = xElem.getReferencePos();
        final String xPos = xElem.getXDPosition();

        final String refSystemId = XsdNamespaceUtils.getSystemIdFromXPos(refXPos);
        final String refLocalName = XsdNameUtils.getReferenceName(refXPos);

        if (XsdNamespaceUtils.isNodeInDifferentNamespace(xElem.getName(), xElem.getNSUri(), schema)) {
            final String nsUri = xElem.getNSUri();
            final String nsPrefix = schema.getNamespaceContext().getPrefix(nsUri);
            if (nsPrefix == null) {
                final XmlSchema refSchema = adapterCtx.getSchema(refSystemId, true, TRANSFORMATION);
                final String refNsPrefix = XsdNamespaceUtils.getReferenceNamespacePrefix(refXPos);
                final String refNsUri = refSchema.getNamespaceContext().getNamespaceURI(refNsPrefix);
                if (!XsdNamespaceUtils.isValidNsUri(refNsUri)) {
                    XsdLogger.printP(LOG_ERROR, TRANSFORMATION, xElem, "Element referencing to unknown namespace! NamespacePrefix=" + nsPrefix);
                } else {
                    XsdNamespaceUtils.addNamespaceToCtx((NamespaceMap) schema.getNamespaceContext(), null, refNsPrefix, refNsUri, POSTPROCESSING);
                }
            }

            return new QName(nsUri, xElem.getName());
        } else if (XsdNamespaceUtils.isRefInDifferentNamespacePrefix(refXPos, schema)) {
            final XmlSchema refSchema = adapterCtx.getSchema(refSystemId, true, TRANSFORMATION);
            final String refNsPrefix = XsdNamespaceUtils.getReferenceNamespacePrefix(refXPos);
            final String nsUri = refSchema.getNamespaceContext().getNamespaceURI(refNsPrefix);
            return new QName(nsUri, refLocalName);
        } else if (XsdNamespaceUtils.isRefInDifferentSystem(refXPos, xPos)) {
            return new QName(XSD_NAMESPACE_PREFIX_EMPTY, refLocalName);
        } else if (XD2XsdUtils.isAnyElement(xElem)) {
            final String refNamespace = XsdNamespaceUtils.getReferenceNamespacePrefix(refXPos);
            String anyLocalName = refLocalName;
            final int anyPos = anyLocalName.indexOf("$any/$any");
            if (anyPos != -1) {
                anyLocalName = anyLocalName.substring(0, anyPos);
            }
            return new QName(refNamespace, anyLocalName);
        } else {
            final String refNamespace = XsdNamespaceUtils.getReferenceNamespacePrefix(refXPos);
            return new QName(refNamespace, refLocalName);
        }
    }

    /**
     * Creates simple type from x-definition text node and fill XSD element node by created simple type
     * @param xsdElem       XSD element node
     * @param xDataText     x-definition text node
     */
    private void addSimpleTypeToElem(final XmlSchemaElement xsdElem, final XData xDataText) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xDataText, "Creating simple type of element. Element=" + xsdElem.getName());

        final QName qName = XD2XsdParserMapping.getDefaultSimpleParserQName(xDataText, adapterCtx);
        if (qName != null) {
            xsdElem.setSchemaTypeName(qName);
            XsdLogger.printP(LOG_DEBUG, TRANSFORMATION, xDataText, "Content of element contains only XSD datatype" +
                    "Element=" + xsdElem.getName() + ", DataType=" + qName.getLocalPart());
        } else {
            xsdElem.setType(xsdFactory.createSimpleType(xDataText, xsdElem.getName()));
        }
    }

    /**
     * Creates XSD complex type node based on input parameters
     * @param xAttrs            x-definition attribute nodes
     * @param xChildrenNodes    x-definition children nodes
     * @param xElem             x-definition element node
     * @param topLevel          flag if source node is placed on top level
     * @return XSD complex type node
     */
    private XmlSchemaComplexType createComplexType(final XData[] xAttrs, final XNode[] xChildrenNodes, final XElement xElem, boolean topLevel) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xElem, "Creating complex type of element ...");

        final XmlSchemaComplexType complexType = xsdFactory.createEmptyComplexType(false);
        final Stack<XmlSchemaParticle> particleStack = new Stack<XmlSchemaParticle>();
        XmlSchemaParticle currParticle = null;
        boolean groupRefNodes = false;
        int stackPopCounter = 0;

        // Convert all children nodes
        for (XNode xnChild : xChildrenNodes) {
            short childrenKind = xnChild.getKind();
            // Skip all reference children nodes
            if (groupRefNodes == true) {
                if (childrenKind == XNode.XMSELECTOR_END) {
                    groupRefNodes = false;
                }
                continue;
            }
            // Particle nodes (sequence, choice, all)
            if (childrenKind == XNode.XMSEQUENCE || childrenKind == XNode.XMMIXED || childrenKind == XNode.XMCHOICE) {
                XmlSchemaParticle newParticle = null;
                // Create group reference
                if (childrenKind == XNode.XMMIXED && !xnChild.getXDPosition().contains(xElem.getXDPosition())) {
                    newParticle = createGroupReference(xChildrenNodes, currParticle, particleStack, xElem);
                }

                if (newParticle == null) {
                    XsdLogger.printP(LOG_INFO, TRANSFORMATION, xElem, "Creating particle to complex content of element. Particle=" + XD2XsdUtils.particleXKindToString(childrenKind));
                    final CXmlSchemaGroupParticle newGroupParticle = (CXmlSchemaGroupParticle) convertTreeInt(xnChild, false);
                    stackPopCounter += updateGroupParticles(particleStack, currParticle, newGroupParticle);
                    currParticle = particleStack.peek();
                } else if (newParticle instanceof XmlSchemaGroupRef) {
                    currParticle = newParticle;
                    groupRefNodes = true;
                }
            } else if (childrenKind == XNode.XMTEXT) { // Simple value node
                XmlSchemaSimpleContent simpleContent = (XmlSchemaSimpleContent) convertTreeInt(xnChild, false);
                if (complexType.getContentModel() != null) {
                    XsdLogger.printP(LOG_WARN, TRANSFORMATION, xElem, "Complex type already has simple content!");
                } else if (simpleContent != null && simpleContent.getContent() instanceof XmlSchemaSimpleContentExtension) {
                    XsdLogger.printP(LOG_DEBUG, TRANSFORMATION, xElem, "Add simple content with attributes to complex content of element.");

                    complexType.setContentModel(simpleContent);

                    for (int j = 0; j < xAttrs.length; j++) {
                        if (simpleContent.getContent() instanceof XmlSchemaSimpleContentExtension)
                            ((XmlSchemaSimpleContentExtension) simpleContent.getContent()).getAttributes().add((XmlSchemaAttributeOrGroupRef) convertTreeInt(xAttrs[j], false));
                    }
                } else {
                    XsdLogger.printP(LOG_WARN, TRANSFORMATION, xElem, "Content of XText is not simple!");
                }
            } else if (childrenKind == XNode.XMSELECTOR_END) {
                if (stackPopCounter > 0) {
                    stackPopCounter--;
                } else {
                    if (!particleStack.empty()) {
                        currParticle = particleStack.pop();
                        if (currParticle instanceof CXmlSchemaChoice && ((CXmlSchemaChoice) currParticle).hasTransformDirection()) {
                            ((CXmlSchemaChoice) currParticle).updateOccurence(adapterCtx);
                        }

                        if (!particleStack.empty()) {
                            currParticle = particleStack.peek();
                        }
                    } else {
                        XsdLogger.printP(LOG_WARN, TRANSFORMATION, xElem, "Group particle stack is empty, but it should not be!");
                    }
                }
            } else {

                if (childrenKind == XNode.XMELEMENT && topLevel && !((XElement)xnChild).isReference() && XD2XsdUtils.isAnyElement((XElement)xnChild)) {
                    XElement xElemChild = ((XElement)xnChild);
                    if (!xElemChild._attrs.isEmpty()) {
                        for (XMData attr : xElemChild.getAttrs()) {
                            complexType.getAttributes().add((XmlSchemaAttributeOrGroupRef) convertTreeInt((XData)attr, false));
                        }
                    }
                }

                XmlSchemaObject xsdChild = convertTreeInt(xnChild, false);
                if (xsdChild != null) {
                    XsdLogger.printP(LOG_DEBUG, TRANSFORMATION, xElem, "Add child to particle of element.");
                    currParticle = createDefaultParticleGroup(currParticle, particleStack, xElem);
                    addNodeToParticleGroup(currParticle, xsdChild);
                }
            }
        }

        if (currParticle != null) {
            complexType.setParticle(currParticle instanceof CXmlSchemaGroupParticle ? ((CXmlSchemaGroupParticle) currParticle).xsd() : currParticle);
        }

        postProcessor.elementComplexType(complexType, xElem);

        if (complexType.getContentModel() == null) {
            XsdLogger.printP(LOG_DEBUG, TRANSFORMATION, xElem, "Add attributes to complex content of element");

            for (int i = 0; i < xAttrs.length; i++) {
                complexType.getAttributes().add((XmlSchemaAttributeOrGroupRef) convertTreeInt(xAttrs[i], false));
            }
        }

        return complexType;
    }

    /**
     * Creates xs:group reference based on x-definition mixed node with reference
     * @param xChildrenNodes
     * @param currGroup
     * @param groups
     * @param defEl
     * @return  null if node is not using reference
     *          instance of XmlSchemaGroupRef if node is only child of element {@code defEl}
     *          instance of XmlSchemaParticle if node is not only child of element {@code defEl}
     */
    private XmlSchemaParticle createGroupReference(final XNode[] xChildrenNodes,
                                                   XmlSchemaParticle currGroup,
                                                   final Stack<XmlSchemaParticle> groups,
                                                   final XElement defEl) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, defEl, "Creating group reference");
        final String systemId = XsdNamespaceUtils.getSystemIdFromXPos(xChildrenNodes[0].getXDPosition());
        String refNodePath = XsdNameUtils.getXNodePath(xChildrenNodes[0].getXDPosition());
        if (refNodePath.endsWith("/$mixed")) {
            refNodePath = refNodePath.substring(0, refNodePath.lastIndexOf("/"));
        }

        final SchemaNode refNode = adapterCtx.getSchemaNode(systemId, refNodePath);

        if (refNode == null || refNode.getXsdNode() == null) {
            XsdLogger.printP(LOG_ERROR, TRANSFORMATION, defEl, "X-definition mixed type is reference, but no reference in XSD has been found! Path=" + xChildrenNodes[0].getXDPosition());
        } else if (!refNode.isXsdGroup()) {
            XsdLogger.printP(LOG_ERROR, TRANSFORMATION, defEl, "XSD mixed type reference is not complex type! Path=" + xChildrenNodes[0].getXDPosition());
        } else {
            final XmlSchemaGroup group = refNode.toXsdGroup();
            final XmlSchemaGroupRef groupRef = xsdFactory.createGroupRef(group.getQName());

            SchemaNodeFactory.createGroupRefNode(defEl, groupRef, refNode, adapterCtx);

            if (refNode.isXdElem() && refNode.toXdElem()._childNodes.length == xChildrenNodes.length) {
                return groupRef;
            } else {
                currGroup = createDefaultParticleGroup(currGroup, groups, defEl);
                addNodeToParticleGroup(currGroup, groupRef);

                if (group.getParticle() instanceof XmlSchemaAll) {
                    final CXmlSchemaChoice newGroupChoice = new CXmlSchemaChoice(postProcessor.groupParticleAllToChoice((XmlSchemaAll)group.getParticle(), false));
                    if (newGroupChoice != null) {
                        group.setParticle(newGroupChoice.xsd());
                        // We have to use occurence on groupRef element
                        groupRef.setMinOccurs(newGroupChoice.xsd().getMinOccurs());
                        groupRef.setMaxOccurs(newGroupChoice.xsd().getMaxOccurs());
                        newGroupChoice.xsd().setMinOccurs(1);
                        newGroupChoice.xsd().setMaxOccurs(1);
                    }
                }

                return currGroup;
            }
        }

        return null;
    }

    /**
     * Transforms group particles to be valid by XSD rules
     * @param particleStack     group particles stack
     * @param prev              previous particle
     * @param newGroupParticle  currently created new group particle
     * @return number of particles which has been popped-out from group particle stack
     */
    private int updateGroupParticles(final Stack<XmlSchemaParticle> particleStack, XmlSchemaParticle prev, final CXmlSchemaGroupParticle newGroupParticle) {
        int stackPopCounter = 0;
        particleStack.push(newGroupParticle);

        do {
            XmlSchemaParticle curr = particleStack.peek();

            if (prev == null || (prev instanceof CXmlSchemaGroupParticle) == false || (curr instanceof CXmlSchemaGroupParticle) == false) {
                break;
            }

            final CXmlSchemaGroupParticle cCurr = (CXmlSchemaGroupParticle) curr;
            final CXmlSchemaGroupParticle cPrev = (CXmlSchemaGroupParticle) prev;
            boolean merge = false;

            if (cCurr.xsd() instanceof XmlSchemaAll) {
                if (cPrev.xsd() instanceof XmlSchemaAll) {
                    cCurr.addItems(cPrev.getItems());
                    merge = true;
                } else {
                    final CXmlSchemaChoice newGroupChoice = postProcessor.groupParticleAllToChoice(CXmlSchemaChoice.TransformDirection.BOTTOM_UP);
                    if (newGroupChoice != null) {
                        replaceLastGroupParticle(particleStack, newGroupChoice);
                    }
                }
            } else if (cPrev.xsd() instanceof XmlSchemaAll) {
                final CXmlSchemaChoice newGroupChoice = postProcessor.groupParticleAllToChoice(CXmlSchemaChoice.TransformDirection.TOP_DOWN);
                if (newGroupChoice != null) {
                    particleStack.pop();
                    replaceLastGroupParticle(particleStack, newGroupChoice);
                    pushGroupParticleToStack(particleStack, newGroupChoice, newGroupParticle);
                }
            } else {
                if (prev != null) {
                    addNodeToParticleGroup(prev, newGroupParticle);
                }
            }

            if (!merge) {
                break;
            }

            stackPopCounter++;
            particleStack.pop();
            prev = replaceLastGroupParticle(particleStack, cCurr);
        } while (!particleStack.empty());

        return stackPopCounter;
    }

    /**
     * Replaces the newest particle in particles stack by {@paramref newGroupParticle}
     * @param particleStack         particle stack
     * @param newGroupParticle      new group particle which will be inserted into stack
     * @return  previous particle
     */
    private static XmlSchemaParticle replaceLastGroupParticle(final Stack<XmlSchemaParticle> particleStack, final CXmlSchemaGroupParticle newGroupParticle) {
        XmlSchemaParticle prev = null;
        if (!particleStack.empty()) {
            particleStack.pop();
            prev = particleStack.empty() ? null : particleStack.peek();
            if (prev != null) {
                addNodeToParticleGroup(prev, newGroupParticle);
            }
        }

        particleStack.push(newGroupParticle);
        return prev;
    }

    /**
     * Creates default particle if no group particle is created yet
     * @param currParticle      current particle
     * @param particleStack     particle stack
     * @param xElem             parent x-definition element node
     * @return  if currParticle == null, then newly created instance of CXmlSchemaSequence
     *          else currParticle
     */
    private XmlSchemaParticle createDefaultParticleGroup(XmlSchemaParticle currParticle, final Stack<XmlSchemaParticle> particleStack, final XElement xElem) {
        if (currParticle == null) {
            XsdLogger.printP(LOG_DEBUG, TRANSFORMATION, xElem, "Particle group is undefined. Creating sequence particle by default.");
            currParticle = new CXmlSchemaSequence(new XmlSchemaSequence());
            particleStack.push(currParticle);
        }

        return currParticle;
    }

    /**
     * Push new group particle into particle stack and set the group particle as child of current particle
     * @param particleStack     particle stack
     * @param currParticle      current particle
     * @param newGroupParticle  new group particle
     */
    private static void pushGroupParticleToStack(final Stack<XmlSchemaParticle> particleStack, final XmlSchemaParticle currParticle, final CXmlSchemaGroupParticle newGroupParticle) {
        addNodeToParticleGroup(currParticle, newGroupParticle);
        particleStack.push(newGroupParticle);
    }

    /**
     * Insert XSD node into current particle
     * @param currParticle  current particle
     * @param xsdNode       XSD node
     */
    private static void addNodeToParticleGroup(final XmlSchemaParticle currParticle, XmlSchemaObject xsdNode) {
        if (xsdNode instanceof CXmlSchemaGroupParticle) {
            xsdNode = ((CXmlSchemaGroupParticle)xsdNode).xsd();
        }

        if (currParticle instanceof CXmlSchemaGroupParticle) {
            ((CXmlSchemaGroupParticle)currParticle).addItem(xsdNode);
        }
    }

}
