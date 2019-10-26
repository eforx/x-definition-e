package org.xdef.impl.util.conv.xd2schemas.xsd;

import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.xdef.impl.XDefinition;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.XsdElementFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.XmlSchemaImportLocation;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XD2XsdUtils;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdNamespaceUtils;

import java.util.*;

import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.*;

public class XD2XsdExtraSchemaAdapter {

    private final int logLevel;
    private final Set<String> schemaNames;
    private final XDefinition xDefinition;
    private final Map<String, XmlSchemaImportLocation> schemaLocations;
    private final XmlSchemaCollection xmlSchemaCollection;

    private NamespaceMap defaultExtraNsCtx = null;

    private XmlSchema schema = null;
    private XsdElementFactory xsdBuilder = null;

    public XD2XsdExtraSchemaAdapter(int logLevel, Set<String> schemaNames, XDefinition xDefinition, Map<String, XmlSchemaImportLocation> schemaLocations, XmlSchemaCollection xmlSchemaCollection) {
        this.logLevel = logLevel;
        this.schemaNames = schemaNames;
        this.xDefinition = xDefinition;
        this.schemaLocations = schemaLocations;
        this.xmlSchemaCollection = xmlSchemaCollection;
    }

    public void setNamespaceCtx(final NamespaceMap namespaceCtx, final String xsdTargetPrefix) {
        defaultExtraNsCtx = new NamespaceMap((HashMap)namespaceCtx.clone());
        defaultExtraNsCtx.remove(xsdTargetPrefix);
    }

    public void createExtraSchemas(final Map<String, List<XNode>> extraNodes, final Map<String, XmlSchemaImportLocation> extraSchemaLocations) {
        Map<String, XmlSchemaImportLocation> schemasToResolve = (HashMap)((HashMap)extraSchemaLocations).clone();

        int lastSizeMap = schemasToResolve.size();

        while (!schemasToResolve.isEmpty()) {
            Iterator<Map.Entry<String, XmlSchemaImportLocation>> itr = schemasToResolve.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry<String, XmlSchemaImportLocation> entry = itr.next();
                if (createSchema(new NamespaceMap((HashMap) defaultExtraNsCtx.clone()), extraNodes, extraSchemaLocations, entry.getKey(), entry.getValue())) {
                    itr.remove();
                }
            }

            // Prevent infinite loop - there is nothing to update
            if (lastSizeMap <= schemasToResolve.size()) {
                break;
            }

            lastSizeMap = schemasToResolve.size();
        }
    }

    private boolean createSchema(final NamespaceMap namespaceCtx,
                                 final Map<String, List<XNode>> extraNodes,
                                 final Map<String, XmlSchemaImportLocation> extraSchemaLocations,
                                 final String targetNsUri,
                                 final XmlSchemaImportLocation importLocation) {

        List<XNode> nodes = extraNodes.get(targetNsUri);
        if (nodes == null) {
            return false;
        }

        if (nodes.isEmpty()) {
            return true;
        }

        if (XsdLogger.isInfo(logLevel)) {
            XsdLogger.printC(INFO, XSD_XDEF_EXTRA_ADAPTER, "====================");
            XsdLogger.printC(INFO, XSD_XDEF_EXTRA_ADAPTER, "Creating extra xsd schema. TargetNamespace=" + targetNsUri);
            XsdLogger.printC(INFO, XSD_XDEF_EXTRA_ADAPTER, "====================");
        }

        this.schema = null;
        this.xsdBuilder = null;

        // Initialize XSD schema
        initSchema(namespaceCtx, targetNsUri, importLocation);

        XDTree2XsdAdapter treeAdapter = new XDTree2XsdAdapter(logLevel, schema, xsdBuilder, extraNodes, extraSchemaLocations);

        // Extract all used references in x-definition
        XD2XsdReferenceAdapter referenceAdapter = new XD2XsdReferenceAdapter(logLevel, xsdBuilder, treeAdapter, schema, schemaLocations, extraSchemaLocations, true);
        referenceAdapter.extractRefsAndImports(nodes);

        // Convert x-definition tree to XSD tree
        convertXdef(treeAdapter, nodes);

        return true;
    }

    private void initSchema(final NamespaceMap namespaceCtx,
                            final String targetNsUri,
                            final XmlSchemaImportLocation importLocation) {

        final String schemaName = importLocation.getFileName();

        if (!schemaNames.add(schemaName)) {
            if (XsdLogger.isError(logLevel)) {
                XsdLogger.printP(ERROR, POSTPROCESSING, xDefinition, "Schema with this name has been already processed! Name=" + schemaName);
            }

            throw new IllegalArgumentException("X-definition name duplication");
        }

        final String targetNsPrefix = XD2XsdUtils.getNsPrefixFromExternalSchemaName(importLocation.getFileName());

        XmlSchema[] schemas = xmlSchemaCollection.getXmlSchema(schemaName);
        if (schemas != null) {
            if (schemas.length == 1) {
                schema = schemas[0];
            } else if (schemas.length > 1) {
                if (XsdLogger.isWarn(logLevel)) {
                    XsdLogger.printP(WARN, POSTPROCESSING, xDefinition, "Schema with this name has been already created! Name=" + schemaName);
                }
            }
        }

        if (schema == null) {
            schema = new XmlSchema(targetNsUri, schemaName, xmlSchemaCollection);
            if (XsdLogger.isInfo(logLevel)) {
                XsdLogger.printP(INFO, POSTPROCESSING, xDefinition, "Initialize extra XSD schema");
            }
        }

        xsdBuilder = new XsdElementFactory(logLevel, schema);

        if (XsdLogger.isDebug(logLevel)) {
            XsdLogger.printP(DEBUG, POSTPROCESSING, xDefinition, "Initializing namespace context ...");
        }

        // Namespace initialization
        XsdNamespaceUtils.addNamespaceToCtx(namespaceCtx, schemaName, targetNsPrefix, targetNsUri, POSTPROCESSING, logLevel);
        schema.setSchemaNamespacePrefix(targetNsPrefix);

        NamespaceMap currNamespaceCtx = (NamespaceMap)schema.getNamespaceContext();
        // Schema has already namespace context -> merge it
        if (currNamespaceCtx != null) {
            currNamespaceCtx.putAll(namespaceCtx);
        }

        // Set attributeFormDefault and elementFormDefault
        schema.setElementFormDefault(XmlSchemaForm.QUALIFIED);
        schema.setAttributeFormDefault(XmlSchemaForm.QUALIFIED);

        schema.setNamespaceContext(currNamespaceCtx != null ? currNamespaceCtx : namespaceCtx);
    }

    private void convertXdef(final XDTree2XsdAdapter treeAdapter, final List<XNode> nodes) {
        if (XsdLogger.isInfo(logLevel)) {
            XsdLogger.printC(INFO, XSD_XDEF_EXTRA_ADAPTER, "Transform x-definition tree to schema ...");
        }

        List<XNode> nodesToResolve = (ArrayList)((ArrayList)nodes).clone();

        for (XNode n : nodesToResolve) {
            XmlSchemaObject xsdNode = treeAdapter.convertTree(n);
            if (xsdNode instanceof XmlSchemaElement) {
                XmlSchemaElement xsdElem = (XmlSchemaElement)xsdNode;

                // Reset occurs - reference element can not contains occurrence info
                xsdElem.setMinOccurs(1);
                xsdElem.setMaxOccurs(1);
                XD2XsdUtils.addElement(schema, xsdElem);
                if (XsdLogger.isInfo(logLevel)) {
                    XsdLogger.printP(INFO, POSTPROCESSING, n, "Add definition of reference as element. Element=" + xsdElem.getName());
                }
            } else if (xsdNode instanceof XmlSchemaAttribute) {
                XmlSchemaAttribute xsdAttr = (XmlSchemaAttribute)xsdNode;
                // Reset usage
                xsdAttr.setUse(XmlSchemaUse.NONE);
                XD2XsdUtils.addAttr(schema, xsdAttr);
            }
        }
    }

}
