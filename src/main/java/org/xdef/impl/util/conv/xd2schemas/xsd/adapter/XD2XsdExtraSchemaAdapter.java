package org.xdef.impl.util.conv.xd2schemas.xsd.adapter;

import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.xdef.impl.XDefinition;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.XsdElementFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.XsdSchemaImportLocation;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdNamespaceUtils;

import java.util.*;

import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.AlgPhase.POSTPROCESSING;
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.AlgPhase.PREPROCESSING;
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XsdLoggerDefs.*;
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XsdLoggerDefs.XSD_XDEF_EXTRA_ADAPTER;

/**
 * Transforms x-definition nodes into xsd nodes
 *
 * Creates new schemas based on post-processing via {@link #transformNodes}
 */
public class XD2XsdExtraSchemaAdapter extends AbstractXd2XsdAdapter {

    /**
     * Input x-definition used for transformation
     */
    private final XDefinition sourceXDefinition;

    /**
     * Original namespace context used in x-definition {@link #sourceXDefinition}
     */
    private NamespaceMap sourceNamespaceCtx = null;

    protected XD2XsdExtraSchemaAdapter(XDefinition xDefinition) {
        this.sourceXDefinition = xDefinition;
    }

    /**
     * Set original (x-definition source) namespace context
     * @param namespaceCtx
     * @param xsdTargetPrefix
     */
    public void setSourceNamespaceCtx(final NamespaceMap namespaceCtx, final String xsdTargetPrefix) {
        sourceNamespaceCtx = new NamespaceMap((HashMap)namespaceCtx.clone());
        sourceNamespaceCtx.remove(xsdTargetPrefix);
    }

    /**
     * Transform given nodes {@paramref allNodesToResolve} into xsd nodes and then insert them into related schemas
     * @param allNodesToResolve     nodes to be transformed
     */
    protected Set<String> transformNodes(final Map<String, Map<String, XNode>> allNodesToResolve) {
        XsdLogger.printP(LOG_INFO, POSTPROCESSING, sourceXDefinition, "Transforming gathered nodes into extra schemas ...");

        final String sourceSystemId = XsdNamespaceUtils.getSystemIdFromXPos(sourceXDefinition.getXDPosition());
        final Set<String> updatedNamespaces = new HashSet<String>();

        Map<String, XsdSchemaImportLocation> schemasToResolve = (HashMap)((HashMap)adapterCtx.getExtraSchemaLocationsCtx()).clone();
        int lastSizeMap = schemasToResolve.size();

        while (!schemasToResolve.isEmpty()) {
            Iterator<Map.Entry<String, XsdSchemaImportLocation>> itr = schemasToResolve.entrySet().iterator();
            while (itr.hasNext()) {
                final Map.Entry<String, XsdSchemaImportLocation> schemaToResolve = itr.next();
                final String schemaTargetNsUri = schemaToResolve.getKey();

                if (updatedNamespaces.contains(schemaTargetNsUri)) {
                    itr.remove();
                    continue;
                }

                Map<String, XNode> nodesInSchemaToResolve = allNodesToResolve.get(schemaTargetNsUri);

                if (nodesInSchemaToResolve != null) {
                    // Filter nodes which should be resolved by current x-definition
                    ArrayList<XNode> nodesToResolve = new ArrayList<XNode>(nodesInSchemaToResolve.values());
                    Iterator<XNode> itr2 = nodesToResolve.iterator();
                    XNode n;
                    while (itr2.hasNext()) {
                        n = itr2.next();
                        if (!sourceSystemId.equals(XsdNamespaceUtils.getSystemIdFromXPos(n.getXDPosition()))) {
                            itr2.remove();
                        }
                    }

                    if (!nodesToResolve.isEmpty()) {
                        SchemaAdapter adapter = new SchemaAdapter(sourceXDefinition);
                        adapter.setAdapterCtx(adapterCtx);
                        adapter.createOrUpdateSchema(new NamespaceMap((HashMap) sourceNamespaceCtx.clone()), nodesToResolve, schemaTargetNsUri, schemaToResolve.getValue());
                        updatedNamespaces.add(schemaTargetNsUri);
                    }

                    itr.remove();
                }
            }

            int currSchemasToResolve = adapterCtx.getExtraSchemaLocationsCtx().size();
            if (lastSizeMap < currSchemasToResolve) {
                schemasToResolve = (HashMap)((HashMap)adapterCtx.getExtraSchemaLocationsCtx()).clone();
            } else if (lastSizeMap <= schemasToResolve.size()) { // Prevent infinite loop - there is nothing to update
                break;
            }

            lastSizeMap = schemasToResolve.size();
        }

        return updatedNamespaces;
    }

    class SchemaAdapter extends AbstractXd2XsdAdapter {

        /**
         * Input x-definition used for transformation
         */
        private final XDefinition sourceXDefinition;

        /**
         * Output xsd schema
         */
        private XmlSchema schema = null;

        protected SchemaAdapter(XDefinition xDefinition) {
            this.sourceXDefinition = xDefinition;
        }

        protected void createOrUpdateSchema(final NamespaceMap namespaceCtx,
                                            final ArrayList<XNode> nodesInSchemaToResolve,
                                            final String targetNsUri,
                                            final XsdSchemaImportLocation importLocation) {
            XsdLogger.printG(LOG_INFO, XSD_XDEF_EXTRA_ADAPTER, "====================");
            XsdLogger.printG(LOG_INFO, XSD_XDEF_EXTRA_ADAPTER, "Post-processing xsd schema. TargetNamespace=" + targetNsUri);
            XsdLogger.printG(LOG_INFO, XSD_XDEF_EXTRA_ADAPTER, "====================");

            final String schemaName = createXsdSchema(namespaceCtx, targetNsUri, importLocation);

            final XsdElementFactory xsdFactory = new XsdElementFactory(schema, adapterCtx);
            final XD2XsdTreeAdapter treeAdapter = new XD2XsdTreeAdapter(schema, schemaName, xsdFactory, adapterCtx);
            final XD2XsdReferenceAdapter referenceAdapter = new XD2XsdReferenceAdapter(schema, schemaName, xsdFactory, treeAdapter, adapterCtx);

            treeAdapter.setPostProcessing();
            referenceAdapter.setPostProcessing();
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
        private String createXsdSchema(final NamespaceMap namespaceCtx,
                                       final String targetNsUri,
                                       final XsdSchemaImportLocation importLocation) {
            final String schemaName = importLocation.getFileName();
            if (adapterCtx.existsSchemaLocation(targetNsUri)) {
                schema = adapterCtx.getSchema(schemaName, true, POSTPROCESSING);
            } else {
                schema = createOrGetXsdSchema(targetNsUri, schemaName);
                initSchemaNamespace(schemaName, namespaceCtx, targetNsUri, importLocation);
            }

            // TODO: based on top attributes/elements ... if attr -> then only attr qualified?
            initSchemaFormDefault();

            return schemaName;
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
            XmlSchema schema = adapterCtx.getSchema(schemaName, false, POSTPROCESSING);

            if (schema == null) {
                adapterCtx.addSchemaName(schemaName);
                schema = new XmlSchema(targetNsUri, schemaName, adapterCtx.getXmlSchemaCollection());
                XsdLogger.print(LOG_INFO, PREPROCESSING, schemaName, "Initialize new XSD schema");
            } else {
                XsdLogger.print(LOG_INFO, PREPROCESSING, schemaName, "Schema already exists");
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
                                         final XsdSchemaImportLocation importLocation) {
            XsdLogger.printP(LOG_DEBUG, POSTPROCESSING, sourceXDefinition, "Initializing namespace context ...");

            // Namespace initialization
            final String targetNsPrefix = XsdNamespaceUtils.getNsPrefixFromExtraSchemaName(importLocation.getFileName());
            XsdNamespaceUtils.addNamespaceToCtx(namespaceCtx, schemaName, targetNsPrefix, targetNsUri, POSTPROCESSING);
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
        private void transformNodes(final XD2XsdTreeAdapter treeAdapter, final ArrayList<XNode> nodes) {
            XsdLogger.printG(LOG_INFO, XSD_XDEF_EXTRA_ADAPTER, "*** Transformation of x-definition tree to schema ***");

            for (XNode n : nodes) {
                XmlSchemaObject xsdNode = treeAdapter.convertTree(n);
                if (xsdNode instanceof XmlSchemaElement) {
                    XsdLogger.printP(LOG_INFO, POSTPROCESSING, n, "Add top-level element.");
                } else if (xsdNode instanceof XmlSchemaAttribute) {
                    XsdLogger.printP(LOG_INFO, POSTPROCESSING, n, "Add top-level attribute.");
                }
            }
        }

    }
}
