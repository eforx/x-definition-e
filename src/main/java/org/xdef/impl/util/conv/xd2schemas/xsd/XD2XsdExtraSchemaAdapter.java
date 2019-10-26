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
import org.xdef.model.XMDefinition;

import java.lang.reflect.Array;
import java.util.*;

import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.*;

public class XD2XsdExtraSchemaAdapter {

    private final int logLevel;
    private final Set<String> schemaNames;

    private XDefinition xDefinition = null;
    private XmlSchema schema = null;
    private XsdElementFactory xsdBuilder = null;

    public XD2XsdExtraSchemaAdapter(int logLevel, final Set<String> schemaNames) {
        this.logLevel = logLevel;
        this.schemaNames = schemaNames;
    }

    public static void createExtraSchemas(final XMDefinition xdef,
                                          final String xsdTargetPrefix,
                                          final NamespaceMap namespaceCtx,
                                          final Map<String, List<XNode>> extraNodes,
                                          final Map<String, XmlSchemaImportLocation> schemaLocations,
                                          final Map<String, XmlSchemaImportLocation> extraSchemaLocations,
                                          final XmlSchemaCollection xmlSchemaCollection,
                                          final Set<String> schemaNames,
                                          final int logLevel) {
        XD2XsdExtraSchemaAdapter schemaAdapter = new XD2XsdExtraSchemaAdapter(logLevel, schemaNames);

        NamespaceMap defaultExtraNsCtx = new NamespaceMap((HashMap)namespaceCtx.clone());
        defaultExtraNsCtx.remove(xsdTargetPrefix);

        Map<String, XmlSchemaImportLocation> schemasToResolve = (HashMap)((HashMap)extraSchemaLocations).clone();

        int lastSizeMap = schemasToResolve.size();

        while (!schemasToResolve.isEmpty()) {
            Iterator<Map.Entry<String, XmlSchemaImportLocation>> itr = schemasToResolve.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry<String, XmlSchemaImportLocation> entry = itr.next();
                if (schemaAdapter.createSchema((XDefinition) xdef, new NamespaceMap((HashMap) defaultExtraNsCtx.clone()), extraNodes, schemaLocations, extraSchemaLocations, xmlSchemaCollection, entry.getKey(), entry.getValue())) {
                    itr.remove();
                }
            }

            // Prevent infinite loop
            if (lastSizeMap <= schemasToResolve.size()) {
                if (XsdLogger.isWarn(logLevel)) {
                    XsdLogger.printP(WARN, POSTPROCESSING, (XDefinition)xdef, "Possible infinity loop in post-processing of extra schemas!");
                }
                break;
            }

            lastSizeMap = schemasToResolve.size();
        }
    }

    private boolean createSchema(final XDefinition xDef,
                              final NamespaceMap namespaceCtx,
                              final Map<String, List<XNode>> extraNodes,
                              final Map<String, XmlSchemaImportLocation> schemaLocations,
                              final Map<String, XmlSchemaImportLocation> extraSchemaLocations,
                              final XmlSchemaCollection xmlSchemaCollection,
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

        this.xDefinition = xDef;

        // Initialize XSD schema
        initSchema(xDef, namespaceCtx, targetNsUri, xmlSchemaCollection, importLocation);

        XDTree2XsdAdapter treeAdapter = new XDTree2XsdAdapter(logLevel, schema, xsdBuilder, extraNodes, extraSchemaLocations);

        // Extract all used references in x-definition
        XD2XsdReferenceAdapter referenceAdapter = new XD2XsdReferenceAdapter(logLevel, xsdBuilder, treeAdapter, schema, schemaLocations, extraSchemaLocations, true);
        referenceAdapter.extractRefsAndImports(nodes);

        // Convert x-definition tree to XSD tree
        convertXdef(treeAdapter, nodes);

        return true;
    }

    private void initSchema(final XDefinition xDef,
                            final NamespaceMap namespaceCtx,
                            final String targetNsUri,
                            final XmlSchemaCollection xmlSchemaCollection,
                            final XmlSchemaImportLocation importLocation) {

        if (XsdLogger.isInfo(logLevel)) {
            XsdLogger.printP(INFO, POSTPROCESSING, xDef, "Initialize extra XSD schema");
        }

        final String schemaName = importLocation.getFileName();

        if (!schemaNames.add(schemaName)) {
            if (XsdLogger.isError(logLevel)) {
                XsdLogger.printP(ERROR, POSTPROCESSING, xDef, "X-definition with this name has been already processed! Name=" + xDef.getName());
            }

            throw new IllegalArgumentException("X-definition name duplication");
        }

        final String targetNsPrefix = XD2XsdUtils.getNsPrefixFromExternalSchemaName(importLocation.getFileName());

        schema = new XmlSchema(targetNsUri, schemaName, xmlSchemaCollection);

        xsdBuilder = new XsdElementFactory(logLevel, schema);

        if (XsdLogger.isDebug(logLevel)) {
            XsdLogger.printP(DEBUG, POSTPROCESSING, xDef, "Initializing namespace context ...");
        }

        // Namespace initialization
        XsdNamespaceUtils.addNamespaceToCtx(namespaceCtx, schemaName, targetNsPrefix, targetNsUri, POSTPROCESSING, logLevel);
        schema.setSchemaNamespacePrefix(targetNsPrefix);

        // Set attributeFormDefault and elementFormDefault
        schema.setElementFormDefault(XmlSchemaForm.QUALIFIED);
        schema.setAttributeFormDefault(XmlSchemaForm.QUALIFIED);

        schema.setNamespaceContext(namespaceCtx);
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
