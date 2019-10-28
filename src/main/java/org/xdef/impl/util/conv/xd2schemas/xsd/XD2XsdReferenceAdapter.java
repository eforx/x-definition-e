package org.xdef.impl.util.conv.xd2schemas.xsd;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.xdef.impl.XData;
import org.xdef.impl.XDefinition;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.XsdElementFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.XmlSchemaImportLocation;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.*;
import org.xdef.model.XMNode;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.*;
import static org.xdef.model.XMNode.XMATTRIBUTE;

class XD2XsdReferenceAdapter {

    private final int logLevel;
    private final XmlSchema schema;
    private final XsdElementFactory xsdFactory;
    private final XDTree2XsdAdapter treeAdapter;
    private final Map<String, XmlSchemaImportLocation> schemaLocations;

    /**
     * Post-processing for extra schemas
     */
    private Map<String, XmlSchemaImportLocation> postprocessedSchemaLocations;
    private boolean postProcessing;

    private Set<String> simpleTypeReferences;
    private Set<String> namespaceImports;
    /**
     * X-definition without target namespace
     */
    private Set<String> namespaceIncludes;

    protected XD2XsdReferenceAdapter(
            int logLevel,
            XmlSchema schema,
            XsdElementFactory xsdFactory,
            XDTree2XsdAdapter treeAdapter,
            Map<String, XmlSchemaImportLocation> schemaLocations) {
        this.logLevel = logLevel;
        this.schema = schema;
        this.xsdFactory = xsdFactory;
        this.treeAdapter = treeAdapter;
        this.schemaLocations = schemaLocations;
    }

    protected void initPostprocessing(final Map<String, XmlSchemaImportLocation> postprocessedSchemaLocations, boolean postProcessing) {
        this.postprocessedSchemaLocations = postprocessedSchemaLocations;
        this.postProcessing = postProcessing;
    }

    /**
     * Creates following nodes from x-definition:
     *      simpleType      - attribute type
     *      complexType     - element type
     *      import          - used namespaces in reference of attributes and elements
     * @param xDef
     */
    protected void createRefsAndImports(XDefinition xDef) {
        simpleTypeReferences = new HashSet<String>();
        namespaceImports = new HashSet<String>();
        namespaceIncludes = new HashSet<String>();
        extractRefsAndImports(xDef);
    }

    private void extractRefsAndImports(XDefinition xDef) {
        if (XsdLogger.isInfo(logLevel)) {
            XsdLogger.printP(INFO, PREPROCESSING, xDef, "*** Creating definition of references and schemas import/include ***");
        }

        final Set<XMNode> processed = new HashSet<XMNode>();

        // Extract all simple types and imports
        if (XsdLogger.isInfo(logLevel)) {
            XsdLogger.printP(INFO, PREPROCESSING, xDef, "Extracting simple references and imports ...");
        }

        for (XElement elem : xDef.getXElements()) {
            extractSimpleRefsAndImports(elem, processed, false);
        }

        // Extract all complex types
        if (XsdLogger.isInfo(logLevel)) {
            XsdLogger.printP(INFO, PREPROCESSING, xDef, "Extracting complex references ...");
        }

        for (XElement elem : xDef.getXElements()) {
            if (!treeAdapter.getXdRootNames().contains(elem.getName())) {
                extractElementRefs(elem);
            }
        }
    }

    public void extractRefsAndImports(List<XNode> nodes) {
        simpleTypeReferences = new HashSet<String>();
        namespaceImports = new HashSet<String>();
        namespaceIncludes = new HashSet<String>();

        final Set<XMNode> processed = new HashSet<XMNode>();

        for (XNode n : nodes) {
            // Extract all simple types and imports
            if (XsdLogger.isInfo(logLevel)) {
                XsdLogger.printP(INFO, POSTPROCESSING, n, "Extracting simple references and imports ...");
            }

            extractSimpleRefsAndImports(n, processed, false);

            // Extract all complex types
            if (XsdLogger.isInfo(logLevel)) {
                XsdLogger.printP(INFO, PREPROCESSING, n, "Extracting complex references ...");
            }

            // TODO: Should be used?
            /*
            if (n.getKind() == XNode.XMELEMENT) {
                XElement xElem = (XElement)n;
                for (XNode childNode : xElem._childNodes) {
                    if (childNode.getKind() == XNode.XMELEMENT) {
                        extractElementRefs(childNode);
                    }
                }
            }
            */
        }
    }

    private void extractElementRefs(final XNode xNode) {
        if (XsdLogger.isDebug(logLevel)) {
            XsdLogger.printP(DEBUG, PREPROCESSING, xNode, "Creating definition of reference");
        }

        XmlSchemaElement xsdElem = (XmlSchemaElement) treeAdapter.convertTree(xNode);
        XmlSchemaType elementType = xsdElem.getSchemaType();
        if (elementType == null) {
            if (XsdLogger.isInfo(logLevel)) {
                XsdLogger.printP(INFO, PREPROCESSING, xNode, "Add definition of reference as element. Element=" + xsdElem.getName());
            }
            if (xsdElem.getRef().getTargetQName() != null) {
                XsdPostProcessor.elementTopLevelRef(xsdElem, (XElement)xNode, xsdFactory);
            }
        } else if (elementType instanceof XmlSchemaType) {
            elementType.setName(xsdElem.getName());
            XD2XsdUtils.addSchemaType(schema, elementType);
            schema.getItems().remove(xsdElem);
            if (XsdLogger.isInfo(logLevel)) {
                XsdLogger.printP(INFO, PREPROCESSING, xNode, "Add definition of reference as complex/simple type. Element=" + xsdElem.getName());
            }
        }
    }

    private void extractSimpleRefsAndImports(XNode xn, final Set<XMNode> processed, boolean parentRef) {

        if (!processed.add(xn)) {
            if (XsdLogger.isDebug(logLevel)) {
                XsdLogger.printP(DEBUG, PREPROCESSING, xn, "Already processed. This node is reference probably");
            }
            return;
        }

        switch (xn.getKind()) {
            case XNode.XMELEMENT: {
                if (XsdLogger.isDebug(logLevel)) {
                    XsdLogger.printP(DEBUG, PREPROCESSING, xn, "Processing XMElement node");
                }

                XElement xDefEl = (XElement)xn;
                XMNode[] attrs = xDefEl.getXDAttrs();

                for (int i = 0; i < attrs.length; i++) {
                    addSimpleTypeReference((XData)attrs[i]);
                }

                boolean isRef = false;

                if (xDefEl.isReference()) {
                    final String refPos = xDefEl.getReferencePos();
                    if (XsdNamespaceUtils.isNodeInDifferentNamespace(xDefEl.getName(), xDefEl.getNSUri(), schema)) {
                        addSchemaImportFromElem(xDefEl.getNSUri(), refPos);
                    } else if (XsdNamespaceUtils.isRefInDifferentNamespacePrefix(refPos, schema)) {
                        final String refSystemId = XsdNamespaceUtils.getReferenceSystemId(refPos);
                        XmlSchema refSchema = XsdNamespaceUtils.getReferenceSchema(schema.getParent(), refSystemId, PREPROCESSING, logLevel);
                        if (refSchema != null) {
                            final String refNsPrefix = XsdNamespaceUtils.getReferenceNamespacePrefix(refPos);
                            final String nsUri = refSchema.getNamespaceContext().getNamespaceURI(refNsPrefix);
                            addSchemaImportFromElem(nsUri, refPos);
                        }
                    } else if (XsdNamespaceUtils.isRefInDifferentSystem(refPos, xDefEl.getXDPosition())) {
                        addSchemaInclude(refPos);
                    } // else {} // Reference in same x-definition and same namespace

                    isRef = true;
                } else {
                    // Element is not reference but name contains different namespace prefix -> we will have to create reference in new namespace in post-processing
                    if (XsdNamespaceUtils.isNodeInDifferentNamespacePrefix(xDefEl.getName(), schema) && postProcessing == false) {
                        final String nsPrefix = XsdNamespaceUtils.getNamespacePrefix(xDefEl.getName());
                        final String nsUri = schema.getNamespaceContext().getNamespaceURI(nsPrefix);
                        if (nsUri == null || nsUri.isEmpty()) {
                            if (parentRef == false && XsdLogger.isError(logLevel)) {
                                XsdLogger.printP(ERROR, TRANSFORMATION, xDefEl, "Element refers to unknown namespace!" +
                                        " NamespacePrefix=" + nsPrefix);
                            }

                            if (parentRef == true) {
                                return;
                            }
                        } else {
                            addPostProcessingSchemaImportFromElem(nsPrefix, nsUri);
                        }

                        isRef = true;
                    }
                }

                if (isRef == false) {
                    int childrenCount = xDefEl._childNodes.length;
                    for (XNode xChild : xDefEl._childNodes) {
                        if (xChild.getKind() == XNode.XMTEXT && (childrenCount > 1 || ((XData) xChild).getRefTypeName() != null)) {
                            addSimpleTypeReference((XData) xChild);
                        } else {
                            extractSimpleRefsAndImports(xChild, processed, xDefEl.isReference() || XsdNamespaceUtils.isNodeInDifferentNamespacePrefix(xDefEl.getName(), schema));
                        }
                    }
                }

                return;
            }
            case XNode.XMDEFINITION: {
                if (XsdLogger.isDebug(logLevel)) {
                    XsdLogger.printP(DEBUG, PREPROCESSING, xn, "Processing XDefinition node");
                }

                XDefinition def = (XDefinition)xn;
                XElement[] elems = def.getXElements();
                for (int i = 0; i < elems.length; i++){
                    extractSimpleRefsAndImports(elems[i], processed, false);
                }
                return;
            }
        }
    }

    private void addSimpleTypeReference(final XData xData) {
        String refTypeName = xData.getRefTypeName();
        final boolean isAttrRef = xData.getKind() == XMATTRIBUTE;

        // Simple type node
        if (refTypeName != null && simpleTypeReferences.add(refTypeName)) {
            xsdFactory.creatSimpleTypeTop(xData, refTypeName);
            if (XsdLogger.isInfo(logLevel)) {
                XsdLogger.printP(INFO, TRANSFORMATION, xData, "Creating simple type definition from reference. Name=" + refTypeName);
            }
            return;
        }

        if (isAttrRef == false && refTypeName == null && XD2XsdUtils.getDefaultSimpleParserQName(xData) == null && xData.getValueTypeName() != null) {
            refTypeName = XsdNameUtils.createRefNameFromParser(xData);
            if (refTypeName != null && simpleTypeReferences.add(refTypeName)) {
                xsdFactory.creatSimpleTypeTop(xData, refTypeName);
                if (XsdLogger.isInfo(logLevel)) {
                    XsdLogger.printP(INFO, TRANSFORMATION, xData, "Creating simple type reference from parser. Name=" + refTypeName);
                }
                return;
            }
        }

        final String importNamespace = xData.getNSUri();
        if (importNamespace != null && XsdNamespaceUtils.isNodeInDifferentNamespace(xData.getName(), importNamespace, schema)) {
            addSchemaImportFromSimpleType(XsdNamespaceUtils.getNamespacePrefix(xData.getName()), importNamespace);
        }
    }

    private void addSchemaInclude(final String refName) {
        final String refSystemId = XsdNamespaceUtils.getReferenceSystemId(refName);

        if (refSystemId == null || !namespaceIncludes.add(refSystemId)) {
            return;
        }

        if (schemaLocations.containsKey(refSystemId)) {
            if (XsdLogger.isInfo(logLevel)) {
                XsdLogger.printP(INFO, PREPROCESSING, "Add schema include. SchemaName=" + refSystemId);
            }

            xsdFactory.createSchemaInclude(schema, schemaLocations.get(refSystemId).buildLocalition(refSystemId));
        } else {
            if (XsdLogger.isWarn(logLevel)) {
                XsdLogger.printP(WARN, PREPROCESSING, "Required schema import has not been found! SchemaName=" + refSystemId);
            }
        }
    }

    private void addSchemaImportFromElem(final String nsUri, final String refName) {
        if (nsUri == null || !namespaceImports.add(nsUri)) {
            return;
        }

        if (schemaLocations.containsKey(nsUri)) {
            if (XsdLogger.isInfo(logLevel)) {
                XsdLogger.printP(INFO, PREPROCESSING, "Add namespace import. NamespaceURI=" + nsUri);
            }

            xsdFactory.createSchemaImport(schema, nsUri, schemaLocations.get(nsUri).buildLocalition(XsdNamespaceUtils.getReferenceSystemId(refName)));
        } else {
            if (XsdLogger.isWarn(logLevel)) {
                XsdLogger.printP(WARN, PREPROCESSING, "Required schema import has not been found! NamespaceURI=" + nsUri);
            }
        }
    }

    private void addSchemaImportFromSimpleType(final String nsPrefix, final String nsUri) {
        if (nsUri == null || !namespaceImports.add(nsUri)) {
            return;
        }

        if (schemaLocations.containsKey(nsUri)) {
            if (XsdLogger.isInfo(logLevel)) {
                XsdLogger.printP(INFO, PREPROCESSING, "Add namespace import. NamespaceURI=" + nsUri);
            }

            xsdFactory.createSchemaImport(schema, nsUri, schemaLocations.get(nsUri).buildLocalition(null));
        } else if (postprocessedSchemaLocations != null) {
            if (!postprocessedSchemaLocations.containsKey(nsUri)) {
                addPostProcessingSchemaImport(nsPrefix, nsUri);
            } else if (postProcessing) {
                xsdFactory.createSchemaImport(schema, nsUri, postprocessedSchemaLocations.get(nsUri).buildLocalition(null));
            }
        }
    }

    private void addPostProcessingSchemaImportFromElem(final String nsPrefix, final String nsUri) {
        if (nsUri == null || !namespaceImports.add(nsUri)) {
            return;
        }

        if (postprocessedSchemaLocations != null) {
            if (!postprocessedSchemaLocations.containsKey(nsUri)) {
                addPostProcessingSchemaImport(nsPrefix, nsUri);
            } else {
                xsdFactory.createSchemaImport(schema, nsUri, postprocessedSchemaLocations.get(nsUri).buildLocalition(null));
            }
        }
    }

    private void addPostProcessingSchemaImport(final String nsPrefix, final String nsUri) {
        final String schemaName = XsdNamespaceUtils.createExtraSchemaNameFromNsPrefix(nsPrefix);
        postprocessedSchemaLocations.put(nsUri, new XmlSchemaImportLocation(nsUri, schemaName));

        if (XsdLogger.isInfo(logLevel)) {
            XsdLogger.printP(INFO, PREPROCESSING, "Add external namespace import. NamespaceURI=" + nsUri + ", SchemaName=" + schemaName);
        }

        xsdFactory.createSchemaImport(schema, nsUri, postprocessedSchemaLocations.get(nsUri).buildLocalition(null));
    }

}
