package org.xdef.impl.util.conv.xd2schema.xsd.adapter;

import org.apache.ws.commons.schema.*;
import org.xdef.impl.XData;
import org.xdef.impl.XDefinition;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.schema.util.XsdLogger;
import org.xdef.impl.util.conv.xd2schema.xsd.factory.XsdElementFactory;
import org.xdef.impl.util.conv.xd2schema.xsd.factory.XsdNameFactory;
import org.xdef.impl.util.conv.xd2schema.xsd.model.UniqueConstraint;
import org.xdef.impl.util.conv.xd2schema.xsd.model.XsdAdapterCtx;
import org.xdef.impl.util.conv.xd2schema.xsd.model.XsdSchemaImportLocation;
import org.xdef.impl.util.conv.xd2schema.xsd.util.*;
import org.xdef.model.XMNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.xdef.impl.util.conv.xd2schema.xsd.definition.AlgPhase.PREPROCESSING;
import static org.xdef.impl.util.conv.xd2schema.xsd.definition.AlgPhase.TRANSFORMATION;
import static org.xdef.impl.util.conv.schema.util.XsdLoggerDefs.*;
import static org.xdef.model.XMNode.XMATTRIBUTE;

public class XD2XsdReferenceAdapter {

    private final XmlSchema schema;
    private final String schemaName;
    private final XsdElementFactory xsdFactory;
    private final XD2XsdTreeAdapter treeAdapter;
    private final XsdAdapterCtx adapterCtx;

    private boolean isPostProcessingPhase = false;

    private Set<String> simpleTypeReferences;
    private Set<String> namespaceImports;
    /**
     * X-definition without target namespace
     */
    private Set<String> namespaceIncludes;

    public XD2XsdReferenceAdapter(XmlSchema schema, String schemaName, XsdElementFactory xsdFactory, XD2XsdTreeAdapter treeAdapter, XsdAdapterCtx adapterCtx) {
        this.schema = schema;
        this.schemaName = schemaName;
        this.xsdFactory = xsdFactory;
        this.treeAdapter = treeAdapter;
        this.adapterCtx = adapterCtx;
    }

    public void setPostProcessing() {
        this.isPostProcessingPhase = true;
    }

    /**
     * Creates following XSD nodes from x-definition nodes:
     *      simpleType      - attribute, text
     *      complexType     - element
     *      group           - mixed
     *      import          - used namespaces in reference of attributes and elements
     * @param xDef  input x-definition
     */
    public void createRefsAndImports(XDefinition xDef) {
        simpleTypeReferences = new HashSet<String>();
        namespaceImports = new HashSet<String>();
        namespaceIncludes = new HashSet<String>();
        extractRefsAndImports(xDef);
    }

    /**
     * Creates following XSD nodes from x-definition nodes:
     *      simpleType      - attribute, text
     *      import          - used namespaces in reference of attributes and elements
     * @param nodes list of x-definition nodes
     */
    public void extractRefsAndImports(final ArrayList<XNode> nodes) {
        simpleTypeReferences = new HashSet<String>();
        namespaceImports = new HashSet<String>();
        namespaceIncludes = new HashSet<String>();

        final Set<XMNode> processed = new HashSet<XMNode>();

        for (XNode n : nodes) {
            // Extract all simple types and imports
            XsdLogger.printP(LOG_INFO, PREPROCESSING, n, "Extracting simple references and imports ...");
            extractSimpleRefsAndImports(n, processed, false);

            // TODO: Should be used?
            // Extract all complex types
            /*
            if (n.getKind() == XNode.XMELEMENT) {
                XsdLogger.printP(LOG_INFO, PREPROCESSING, n, "Extracting complex references ...");
                XElement xElem = (XElement)n;
                for (XNode childNode : xElem._childNodes) {
                    if (childNode.getKind() == XNode.XMELEMENT) {
                        extractTopLevelElementRefs(childNode);
                    }
                }
            }
            */
        }
    }

    /**
     * Extracts references and imports from given x-definition
     * @param xDef  input x-definition
     */
    private void extractRefsAndImports(final XDefinition xDef) {
        XsdLogger.printP(LOG_INFO, PREPROCESSING, xDef, "*** Creating definition of references and schemas import/include ***");

        final Set<XMNode> processed = new HashSet<XMNode>();

        // Extract all simple types and imports
        XsdLogger.printP(LOG_INFO, PREPROCESSING, xDef, "Extracting simple references and imports ...");
        for (XElement elem : xDef.getXElements()) {
            extractSimpleRefsAndImports(elem, processed, false);
        }

        // Extract all complex types
        XsdLogger.printP(LOG_INFO, PREPROCESSING, xDef, "Extracting complex references ...");
        final Set<String> rootNodeNames = adapterCtx.getSchemaRootNodeNames(schemaName);
        for (XElement elem : xDef.getXElements()) {
            if (rootNodeNames == null || !rootNodeNames.contains(elem.getName())) {
                transformTopLevelElem(elem);
            }
        }
    }

    /**
     * Transform top-level x-definition element node into XSD node (element, complex-type, simple-type, group)
     * @param xNode
     */
    private void transformTopLevelElem(final XNode xNode) {
        XsdLogger.printP(LOG_DEBUG, PREPROCESSING, xNode, "Creating definition of reference");

        final XmlSchemaElement xsdElem = (XmlSchemaElement) treeAdapter.convertTree(xNode);
        final XmlSchemaType elementType = xsdElem.getSchemaType();

        if (elementType == null) {
            XsdLogger.printP(LOG_INFO, PREPROCESSING, xNode, "Add definition of reference as element. Name=" + xsdElem.getName());
        } else if (elementType instanceof XmlSchemaType) {
            if (xNode.getName().endsWith("$mixed") && elementType instanceof XmlSchemaComplexType) {
                // Convert xd:mixed to group
                final XmlSchemaGroup schemaGroup = xsdFactory.createEmptyGroup(xsdElem.getName());
                schemaGroup.setParticle((XmlSchemaGroupParticle)((XmlSchemaComplexType)elementType).getParticle());
                adapterCtx.updateNode(xNode, schemaGroup);
                XD2XsdUtils.removeNode(schema, xsdElem);
                XsdLogger.printP(LOG_INFO, PREPROCESSING, xNode, "Add definition of group. Name=" + xsdElem.getName());
            } else {
                // Move schema type (complex-type/simple-type) to top-level and remove original element
                elementType.setName(xsdElem.getName());
                adapterCtx.updateNode(xNode, elementType);
                XD2XsdUtils.addSchemaTypeNode2TopLevel(schema, elementType);
                XD2XsdUtils.removeNode(schema, xsdElem);
                XsdLogger.printP(LOG_INFO, PREPROCESSING, xNode, "Add definition of reference as complex/simple type. Name=" + xsdElem.getName());
            }
        }
    }

    /**
     * Extract simple-type references and schema imports from x-definition tree.
     * @param xNode         root of x-definition tree
     * @param processed     already processed nodes
     * @param parentRef     flag if parent is node using reference
     */
    private void extractSimpleRefsAndImports(final XNode xNode, final Set<XMNode> processed, boolean parentRef) {
        if (!processed.add(xNode)) {
            XsdLogger.printP(LOG_DEBUG, PREPROCESSING, xNode, "Already processed. This node is reference probably");
            return;
        }

        switch (xNode.getKind()) {
            case XMATTRIBUTE: {
                processSimpleTypeReference((XData)xNode);
                break;
            }
            case XNode.XMELEMENT: {
                XsdLogger.printP(LOG_DEBUG, PREPROCESSING, xNode, "Processing XMElement node. Node=" + xNode.getName());

                final XElement xElem = (XElement)xNode;
                boolean isRef = false;
                treeAdapter.loadElementUniqueSets(xElem);

                if (xElem.isReference() || xElem.getReferencePos() != null) {
                    final String refPos = xElem.getReferencePos();
                    final String nodeNsUri = xElem.getNSUri();
                    if (XsdNamespaceUtils.isNodeInDifferentNamespace(xElem.getName(), nodeNsUri, schema)) {
                        addSchemaImportFromElem(nodeNsUri, refPos);
                    } else if (XsdNamespaceUtils.isRefInDifferentNamespacePrefix(refPos, schema)) {
                        final String refSystemId = XsdNamespaceUtils.getSystemIdFromXPos(refPos);
                        XmlSchema refSchema = adapterCtx.findSchema(refSystemId, true, PREPROCESSING);
                        final String refNsPrefix = XsdNamespaceUtils.getReferenceNamespacePrefix(refPos);
                        final String nsUri = refSchema.getNamespaceContext().getNamespaceURI(refNsPrefix);
                        if (!XsdNamespaceUtils.isValidNsUri(nsUri)) {
                            XsdLogger.printP(LOG_ERROR, PREPROCESSING, xElem, "Element referencing to unknown namespace! NamespacePrefix=" + refNsPrefix);
                        } else {
                            addSchemaImportFromElem(nsUri, refPos);
                        }
                    } else if (XsdNamespaceUtils.isRefInDifferentSystem(refPos, xElem.getXDPosition())) {
                        addSchemaInclude(refPos);
                    } // else {} // Reference in same x-definition and same namespace

                    isRef = true;
                } else {
                    // Element is not reference but name contains different namespace prefix -> we will have to create reference in new namespace in post-processing
                    if (XsdNamespaceUtils.isNodeInDifferentNamespacePrefix(xElem, schema) && isPostProcessingPhase == false) {
                        String nsPrefix = XsdNamespaceUtils.getNamespacePrefix(xElem.getName());
                        String nsUri = schema.getNamespaceContext().getNamespaceURI(nsPrefix);

                        // Post-processing
                        if (XsdNamespaceUtils.isValidNsUri(nsUri)) {
                            XsdSchemaImportLocation importLocation = adapterCtx.getSchemaLocationsCtx().get(nsUri);
                            if (importLocation != null) {
                                addPostProcessingSchema(nsUri, importLocation);
                            } else {
                                addPostProcessingSchemaImport(nsPrefix, nsUri);
                            }
                        } else {
                            final String xDefPos = xElem.getXDPosition();
                            nsUri = XsdNamespaceUtils.getNodeNamespaceUri(xElem, adapterCtx, PREPROCESSING);

                            if (XsdNamespaceUtils.isValidNsUri(nsUri)) {
                                addSchemaImportFromElem(nsUri, xDefPos);
                            } else {
                                if (parentRef == false) {
                                    nsPrefix = XsdNamespaceUtils.getReferenceNamespacePrefix(xDefPos);
                                    XsdLogger.printP(LOG_ERROR, PREPROCESSING, xElem, "Element referencing to unknown namespace! NamespacePrefix=" + nsPrefix);
                                }
                            }
                        }

                        isRef = true;
                    }
                }

                if (isRef == false) {
                    XMNode[] attrs = xElem.getXDAttrs();
                    for (int i = 0; i < attrs.length; i++) {
                        processSimpleTypeReference((XData)attrs[i]);
                    }

                    int childrenCount = xElem._childNodes.length;
                    for (XNode xChild : xElem._childNodes) {
                        if (xChild.getKind() == XNode.XMTEXT && (childrenCount > 1 || ((XData) xChild).getRefTypeName() != null)) {
                            processSimpleTypeReference((XData) xChild);
                        } else {
                            extractSimpleRefsAndImports(xChild, processed, xElem.isReference() || XsdNamespaceUtils.isNodeInDifferentNamespacePrefix(xElem, schema));
                        }
                    }
                }

                break;
            }
            case XNode.XMDEFINITION: {
                XsdLogger.printP(LOG_DEBUG, PREPROCESSING, xNode, "Processing XDefinition node. Node=" + xNode.getName());

                XDefinition def = (XDefinition)xNode;
                XElement[] elems = def.getXElements();
                for (int i = 0; i < elems.length; i++){
                    extractSimpleRefsAndImports(elems[i], processed, false);
                }
                break;
            }
        }
    }

    /**
     * Process simple-type XSD reference.
     * Insert x-definition node into post processing queue if it is using different namespace.
     *
     * @param xData attribute/text node using reference
     */
    private void processSimpleTypeReference(final XData xData) {
        // Element is not reference but name contains different namespace prefix -> we will have to create reference in new namespace in post-processing
        if (XsdNamespaceUtils.isNodeInDifferentNamespacePrefix(xData, schema) && isPostProcessingPhase == false) {
            final String nsPrefix = XsdNamespaceUtils.getNamespacePrefix(xData.getName());
            final String nsUri = schema.getNamespaceContext().getNamespaceURI(nsPrefix);

            // Post-processing
            if (nsUri != null && !nsUri.isEmpty()) {
                final XsdSchemaImportLocation importLocation = adapterCtx.getSchemaLocationsCtx().get(nsUri);
                if (importLocation != null) {
                    addPostProcessingSchema(nsUri, importLocation);
                }
            }
        } else {
            final boolean isAttrRef = xData.getKind() == XMATTRIBUTE;

            if (isAttrRef == true) {
                final UniqueConstraint uniqueConstraint = adapterCtx.findUniqueConst(xData);
                // Do not create reference if attribute is using unique set
                if (uniqueConstraint != null) {
                    adapterCtx.addVarToUniqueConst(xData, uniqueConstraint);
                    return;
                }
            }

            String refTypeName = adapterCtx.getNameFactory().findTopLevelName(xData, false);
            if (refTypeName == null) {
                refTypeName = XsdNameFactory.createLocalSimpleTypeName(xData);
                adapterCtx.getNameFactory().addTopSimpleTypeName(xData, refTypeName);
            }

            if (refTypeName != null && simpleTypeReferences.add(refTypeName)) {
                xsdFactory.createSimpleTypeTop(xData, refTypeName);
                XsdLogger.printP(LOG_INFO, TRANSFORMATION, xData, "Creating simple type definition of reference. Name=" + refTypeName);
                return;
            }

            if (!isAttrRef && refTypeName == null && XD2XsdParserMapping.getDefaultSimpleParserQName(xData, adapterCtx) == null && xData.getValueTypeName() != null) {
                refTypeName = XsdNameUtils.createRefNameFromParser(xData, adapterCtx);
                if (refTypeName != null && simpleTypeReferences.add(refTypeName)) {
                    xsdFactory.createSimpleTypeTop(xData, refTypeName);
                    XsdLogger.printP(LOG_INFO, TRANSFORMATION, xData, "Creating simple type reference from parser. Name=" + refTypeName);
                    return;
                }
            }
        }

        final String nodeNsUri = xData.getNSUri();
        if (nodeNsUri != null && XsdNamespaceUtils.isNodeInDifferentNamespace(xData.getName(), nodeNsUri, schema)) {
            addSchemaImportFromSimpleType(XsdNamespaceUtils.getNamespacePrefix(xData.getName()), nodeNsUri);
        }
    }

    /**
     * Add XSD schema include.
     * @param refPos    reference position of x-definition node
     */
    private void addSchemaInclude(final String refPos) {
        final String refSystemId = XsdNamespaceUtils.getSystemIdFromXPos(refPos);

        if (refSystemId == null || !namespaceIncludes.add(refSystemId)) {
            return;
        }

        if (adapterCtx.getSchemaLocationsCtx().containsKey(refSystemId)) {
            XsdLogger.printP(LOG_INFO, PREPROCESSING, "Add schema include. SchemaName=" + refSystemId);
            xsdFactory.createSchemaInclude(schema, adapterCtx.getSchemaLocationsCtx().get(refSystemId).buildLocation(refSystemId));
        } else {
            XsdLogger.printP(LOG_WARN, PREPROCESSING, "Required schema import has not been found! SchemaName=" + refSystemId);
        }
    }

    /**
     * Add XSD schema import based on x-definition element node.
     * @param nsUri     x-definition node namespace URI
     * @param refPos    x-definition reference position
     */
    private void addSchemaImportFromElem(final String nsUri, final String refPos) {
        if (nsUri == null || !namespaceImports.add(nsUri)) {
            return;
        }

        if (adapterCtx.getSchemaLocationsCtx().containsKey(nsUri)) {
            XsdLogger.printP(LOG_INFO, PREPROCESSING, "Add namespace import. NamespaceURI=" + nsUri);
            xsdFactory.createSchemaImport(schema, nsUri, adapterCtx.getSchemaLocationsCtx().get(nsUri).buildLocation(XsdNamespaceUtils.getSystemIdFromXPos(refPos)));
        } else {
            XsdLogger.printP(LOG_WARN, PREPROCESSING, "Required schema import has not been found! NamespaceURI=" + nsUri);
        }
    }

    /**
     * Add XSD schema import based on attribute/text x-definition node
     * @param nsPrefix  x-definition node namespace prefix
     * @param nsUri     x-definition node namespace URI
     */
    private void addSchemaImportFromSimpleType(final String nsPrefix, final String nsUri) {
        if (nsUri == null || !namespaceImports.add(nsUri)) {
            return;
        }

        if (adapterCtx.getSchemaLocationsCtx().containsKey(nsUri)) {
            XsdLogger.printP(LOG_INFO, PREPROCESSING, "Add namespace import. NamespaceURI=" + nsUri);
            xsdFactory.createSchemaImport(schema, nsUri, adapterCtx.getSchemaLocationsCtx().get(nsUri).buildLocation(null));
        } else if (adapterCtx.getExtraSchemaLocationsCtx() != null) {
            if (!adapterCtx.getExtraSchemaLocationsCtx().containsKey(nsUri)) {
                addPostProcessingSchemaImportInt(nsPrefix, nsUri);
            } else if (isPostProcessingPhase) {
                XsdLogger.printP(LOG_INFO, PREPROCESSING, "Add namespace import. NamespaceURI=" + nsUri);
                xsdFactory.createSchemaImport(schema, nsUri, adapterCtx.getExtraSchemaLocationsCtx().get(nsUri).buildLocation(null));
            }
        }
    }

    /**
     * Add XSD schema import of post processed schema
     * @param nsPrefix  schema namespace prefix
     * @param nsUri     schema namespace URI
     */
    private void addPostProcessingSchemaImport(final String nsPrefix, final String nsUri) {
        if (nsUri == null || !namespaceImports.add(nsUri)) {
            return;
        }

        addPostProcessingSchemaImportInt(nsPrefix, nsUri);
    }

    /**
     * Add XSD schema import of post processed schema
     * @param nsPrefix  schema namespace prefix
     * @param nsUri     schema namespace URI
     */
    private void addPostProcessingSchemaImportInt(final String nsPrefix, final String nsUri) {
        if (adapterCtx.getExtraSchemaLocationsCtx() != null) {
            if (!adapterCtx.getExtraSchemaLocationsCtx().containsKey(nsUri)) {
                final String schemaName = XsdNamespaceUtils.createExtraSchemaNameFromNsPrefix(nsPrefix);
                XsdLogger.printP(LOG_INFO, PREPROCESSING, "Add external namespace import. NamespaceURI=" + nsUri + ", SchemaName=" + schemaName);
                adapterCtx.getExtraSchemaLocationsCtx().put(nsUri, new XsdSchemaImportLocation(nsUri, schemaName));
                XsdLogger.printP(LOG_INFO, PREPROCESSING, "Add external schema to post-process queue. NamespaceURI=" + nsUri + ", SchemaName=" + schemaName);
                xsdFactory.createSchemaImport(schema, nsUri, adapterCtx.getExtraSchemaLocationsCtx().get(nsUri).buildLocation(null));
            } else {
                xsdFactory.createSchemaImport(schema, nsUri, adapterCtx.getExtraSchemaLocationsCtx().get(nsUri).buildLocation(null));
            }
        }
    }

    /**
     * Insert schema namespace into post processing queue
     * @param nsUri                     schema namespace URI
     * @param schemaImportLocation      schema location
     */
    private void addPostProcessingSchema(final String nsUri, final XsdSchemaImportLocation schemaImportLocation) {
        if (adapterCtx.getExtraSchemaLocationsCtx().containsKey(nsUri)) {
            return;
        }

        XsdLogger.printP(LOG_INFO, PREPROCESSING, "Add schema to post-process queue. NamespaceURI=" + nsUri);
        adapterCtx.getExtraSchemaLocationsCtx().put(nsUri, schemaImportLocation);
    }

}
