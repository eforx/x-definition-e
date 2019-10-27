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
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XD2XsdUtils;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdNamespaceUtils;
import org.xdef.model.XMNode;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.*;

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
    private Set<String> systemIdImports;

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

    public final Set<String> getSystemIdImports() {
        return systemIdImports;
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
        systemIdImports = new HashSet<String>();
        extractRefsAndImports(xDef);
    }

    private void extractRefsAndImports(XDefinition xDef) {
        if (XsdLogger.isInfo(logLevel)) {
            XsdLogger.printP(INFO, PREPROCESSING, xDef, "Creating definition of references ...");
        }

        final Set<XMNode> processed = new HashSet<XMNode>();
        final XElement[] elems = xDef.getXElements();

        // Extract all simple types and imports
        if (XsdLogger.isInfo(logLevel)) {
            XsdLogger.printP(INFO, PREPROCESSING, xDef, "Extracting simple references and imports ...");
        }

        for (int i = 0; i < elems.length; i++) {
            extractSimpleRefsAndImports(elems[i], processed, false);
        }

        // Extract all complex types
        if (XsdLogger.isInfo(logLevel)) {
            XsdLogger.printP(INFO, PREPROCESSING, xDef, "Extracting complex references ...");
        }

        for (int i = 0; i < elems.length; i++) {
            if (!treeAdapter.getXdRootNames().contains(elems[i].getName())) {
                extractElementRefs(elems[i]);
            }
        }
    }

    public void extractRefsAndImports(List<XNode> nodes) {
        simpleTypeReferences = new HashSet<String>();
        namespaceImports = new HashSet<String>();
        systemIdImports = new HashSet<String>();

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
            /*if (n.getKind() == XNode.XMELEMENT) {
                extractElementRefs(n);
            }*/
        }
    }

    private void extractElementRefs(final XNode xNode) {
        if (XsdLogger.isDebug(logLevel)) {
            XsdLogger.printP(DEBUG, PREPROCESSING, xNode, "Creating definition of reference");
        }

        XmlSchemaElement xsdElem = (XmlSchemaElement) treeAdapter.convertTree(xNode);
        XmlSchemaType elementType = xsdElem.getSchemaType();
        if (elementType == null) {
            XD2XsdUtils.addElement(schema, xsdElem);
            if (XsdLogger.isInfo(logLevel)) {
                XsdLogger.printP(INFO, PREPROCESSING, xNode, "Add definition of reference as element. Element=" + xsdElem.getName());
            }
        } else if (elementType instanceof XmlSchemaType) {
            elementType.setName(xsdElem.getName());
            XD2XsdUtils.addRefType(schema, elementType);
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
                    addSimpleTypeReference((XData)attrs[i], true);
                }

                if (xDefEl.isReference()) {
                    if (XsdNamespaceUtils.isRefInDifferentNamespace(xDefEl.getName(), xDefEl.getNSUri(), schema)) {
                        addSchemaImportFromElem(xDefEl.getNSUri(), xDefEl.getReferencePos());
                    } else if (XsdNamespaceUtils.isRefInDifferentSystem(xDefEl.getReferencePos(), xDefEl.getXDPosition())) {
                        addSchemaImport(xDefEl.getReferencePos());
                    }
                } else {
                    // Element is not reference but name contains different namespace prefix -> we will have to create reference in new namespace in post-processing
                    if (XsdNamespaceUtils.isInDifferentNamespace(xDefEl.getName(), schema) && postProcessing == false) {
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
                            addPostprocessingSchemaImportFromElem(nsPrefix, nsUri);
                        }
                    }
                }

                int childrenCount = xDefEl._childNodes.length;
                for (int i = 0; i < xDefEl._childNodes.length; i++) {
                    if (xDefEl._childNodes[i].getKind() == XNode.XMTEXT && childrenCount > 1) {
                        addSimpleTypeReference((XData) xDefEl._childNodes[i], false);
                    } else {
                        extractSimpleRefsAndImports(xDefEl._childNodes[i], processed, xDefEl.isReference() || XsdNamespaceUtils.isInDifferentNamespace(xDefEl.getName(), schema));
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

    private void addSimpleTypeReference(final XData xData, boolean isAttrRef) {
        String refTypeName = xData.getRefTypeName();

        // Simple type node
        if (refTypeName != null && simpleTypeReferences.add(refTypeName)) {
            xsdFactory.creatSimpleTypeTop(xData, refTypeName);
            if (XsdLogger.isInfo(logLevel)) {
                XsdLogger.printP(INFO, PREPROCESSING, xData, "Creating simple type definition from reference. ReferenceName=" + refTypeName);
            }
            return;
        }

        if (isAttrRef == false && refTypeName == null && XD2XsdUtils.getDefaultSimpleParserQName(xData) == null && xData.getValueTypeName() != null) {
            refTypeName = XD2XsdUtils.createNameFromParser(xData);
            if (refTypeName != null && simpleTypeReferences.add(refTypeName)) {
                xsdFactory.creatSimpleTypeTop(xData, refTypeName);
                if (XsdLogger.isInfo(logLevel)) {
                    XsdLogger.printP(INFO, PREPROCESSING, xData, "Creating simple type reference from parser. ReferenceName=" + refTypeName);
                }
                return;
            }
        }

        final String importNamespace = xData.getNSUri();
        if (importNamespace != null && XsdNamespaceUtils.isRefInDifferentNamespace(xData.getName(), importNamespace, schema)) {
            addSchemaImportFromSimpleType(XsdNamespaceUtils.getNamespacePrefix(xData.getName()), importNamespace);
        }
    }

    private void addSchemaImport(final String refName) {
        final String refSystemId = XsdNamespaceUtils.getReferenceSystemId(refName);

        if (refSystemId == null || !systemIdImports.add(refSystemId)) {
            return;
        }

        if (schemaLocations.containsKey(refSystemId)) {
            if (XsdLogger.isInfo(logLevel)) {
                XsdLogger.printP(INFO, PREPROCESSING, "Add schema import. SchemaName=" + refSystemId);
            }

            // TODO: Search for target namespace?
            xsdFactory.createSchemaImport(schema, refSystemId, schemaLocations.get(refSystemId).buildLocalition(refSystemId));
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

    private void addPostprocessingSchemaImportFromElem(final String nsPrefix, final String nsUri) {
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
        final String schemaName = XD2XsdUtils.createExternalSchemaNameFromNsPrefix(nsPrefix);
        postprocessedSchemaLocations.put(nsUri, new XmlSchemaImportLocation(nsUri, schemaName));

        if (XsdLogger.isInfo(logLevel)) {
            XsdLogger.printP(INFO, PREPROCESSING, "Add external namespace import. NamespaceURI=" + nsUri + ", SchemaName=" + schemaName);
        }

        xsdFactory.createSchemaImport(schema, nsUri, postprocessedSchemaLocations.get(nsUri).buildLocalition(null));
    }

}
