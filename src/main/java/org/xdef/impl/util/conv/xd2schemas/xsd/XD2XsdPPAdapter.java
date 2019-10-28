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

/**
 * Transforms x-definition nodes into xsd nodes
 */
class XD2XsdPPAdapter extends AbstractXd2XsdAdapter {

    /**
     * Input x-definition used for transformation
     */
    private final XDefinition sourceXDefinition;

    /**
     * Output xsd schema
     */
    private XmlSchema schema = null;

    protected XD2XsdPPAdapter(int logLevel, XDefinition xDefinition) {
        this.logLevel = logLevel;
        this.sourceXDefinition = xDefinition;
    }

    protected void createOrUpdateSchema(final NamespaceMap namespaceCtx,
                                        final Map<String, List<XNode>> allNodesToResolve,
                                        final List<XNode> nodesInSchemaToResolve,
                                        final String targetNsUri,
                                        final XmlSchemaImportLocation importLocation) {
        if (XsdLogger.isInfo(logLevel)) {
            XsdLogger.printC(INFO, XSD_XDEF_EXTRA_ADAPTER, "====================");
            XsdLogger.printC(INFO, XSD_XDEF_EXTRA_ADAPTER, "Post-processing xsd schema. TargetNamespace=" + targetNsUri);
            XsdLogger.printC(INFO, XSD_XDEF_EXTRA_ADAPTER, "====================");
        }

        createXsdSchema(namespaceCtx, targetNsUri, importLocation);

        XsdElementFactory xsdBuilder = new XsdElementFactory(logLevel, schema);

        XDTree2XsdAdapter treeAdapter = new XDTree2XsdAdapter(logLevel, schema, xsdBuilder);
        treeAdapter.initPostprocessing(allNodesToResolve, adapterCtx.getExtraSchemaLocationsCtx());

        XD2XsdReferenceAdapter referenceAdapter = new XD2XsdReferenceAdapter(logLevel, schema, xsdBuilder, treeAdapter, adapterCtx.getSchemaLocationsCtx());
        referenceAdapter.initPostprocessing(adapterCtx.getExtraSchemaLocationsCtx(), true);
        referenceAdapter.extractRefsAndImports(nodesInSchemaToResolve);

        transformNodes(treeAdapter, nodesInSchemaToResolve);
    }

    /**
     * Creates and initialize XSD schema
     *
     * @param namespaceCtx
     * @param targetNsUri
     * @param importLocation
     * @return  instance of xml schema
     */
    private void createXsdSchema(final NamespaceMap namespaceCtx,
                                 final String targetNsUri,
                                 final XmlSchemaImportLocation importLocation) {

        final String schemaName = importLocation.getFileName();
        schema = createOrGetXsdSchema(targetNsUri, schemaName);
        initSchemaNamespace(schemaName, namespaceCtx, targetNsUri, importLocation);
        initSchemaFormDefault();
    }

    /**
     * Creates XSD schema
     * If schema already exists, return value is reference to already exists schema.
     *
     * @param targetNsUri   target namespace Uri
     * @param schemaName    xsd schema name
     * @return  instance of xml schema
     */
    private XmlSchema createOrGetXsdSchema(final String targetNsUri, final String schemaName) {
        XmlSchema schema = null;

        XmlSchema[] schemas = adapterCtx.getXmlSchemaCollection().getXmlSchema(schemaName);
        if (schemas != null) {
            if (schemas.length == 1) {
                schema = schemas[0];
            } else if (schemas.length > 1) {
                if (XsdLogger.isWarn(logLevel)) {
                    XsdLogger.printP(WARN, POSTPROCESSING, sourceXDefinition, "Schema with this name has been already created! Name=" + schemaName);
                }

                schema = schemas[0];
            }
        }

        if (schema == null) {
            adapterCtx.addSchemaName(schemaName);
            schema = new XmlSchema(targetNsUri, schemaName, adapterCtx.getXmlSchemaCollection());
            if (XsdLogger.isInfo(logLevel)) {
                XsdLogger.print(INFO, POSTPROCESSING, schemaName, "Initialize extra XSD schema");
            }
        } else {
            if (XsdLogger.isInfo(logLevel)) {
                XsdLogger.print(INFO, POSTPROCESSING, schemaName, "Schema already exists");
            }
        }

        return schema;
    }

    /**
     * Initializes xsd schema namespace
     *
     * If schema namespace context already exist, then merge it with {@paramref namespaceCtx)
     *
     * @param schemaName
     * @param namespaceCtx      current namespace context
     * @param targetNsUri
     * @param importLocation
     * @return
     */
    private void initSchemaNamespace(final String schemaName,
                                     final NamespaceMap namespaceCtx,
                                     final String targetNsUri,
                                     final XmlSchemaImportLocation importLocation) {
        if (XsdLogger.isDebug(logLevel)) {
            XsdLogger.printP(DEBUG, POSTPROCESSING, sourceXDefinition, "Initializing namespace context ...");
        }

        // Namespace initialization
        final String targetNsPrefix = XsdNamespaceUtils.getNsPrefixFromExtraSchemaName(importLocation.getFileName());
        XsdNamespaceUtils.addNamespaceToCtx(namespaceCtx, schemaName, targetNsPrefix, targetNsUri, POSTPROCESSING, logLevel);
        schema.setSchemaNamespacePrefix(targetNsPrefix);

        NamespaceMap currNamespaceCtx = (NamespaceMap)schema.getNamespaceContext();
        // Schema has already namespace context -> merge it
        if (currNamespaceCtx != null) {
            currNamespaceCtx.putAll(namespaceCtx);
            schema.setNamespaceContext(currNamespaceCtx);
        } else {
            schema.setNamespaceContext(namespaceCtx);
        }
    }

    /**
     * Sets attributeFormDefault and elementFormDefault
     */
    private void initSchemaFormDefault() {
        schema.setElementFormDefault(XmlSchemaForm.QUALIFIED);
        schema.setAttributeFormDefault(XmlSchemaForm.QUALIFIED);
    }

    /**
     * Transforms given x-definition nodes into xsd elements and insert them into {@link #schema}
     * @param treeAdapter       transformation algorithm
     * @param nodes             source nodes to transform
     */
    private void transformNodes(final XDTree2XsdAdapter treeAdapter, final List<XNode> nodes) {
        if (XsdLogger.isInfo(logLevel)) {
            XsdLogger.printC(INFO, XSD_XDEF_EXTRA_ADAPTER, "*** Transformation of x-definition tree to schema ***");
        }

        List<XNode> nodesToResolve = (ArrayList)((ArrayList)nodes).clone();

        for (XNode n : nodesToResolve) {
            XmlSchemaObject xsdNode = treeAdapter.convertTree(n);
            if (xsdNode instanceof XmlSchemaElement) {
                XmlSchemaElement xsdElem = (XmlSchemaElement)xsdNode;

                // Reset occurs - reference element can not contains occurrence info
                xsdElem.setMinOccurs(1);
                xsdElem.setMaxOccurs(1);

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
