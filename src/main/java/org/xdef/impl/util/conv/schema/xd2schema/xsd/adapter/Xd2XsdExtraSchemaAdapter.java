package org.xdef.impl.util.conv.schema.xd2schema.xsd.adapter;

import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.xdef.impl.XDefinition;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.schema.util.SchemaLogger;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.factory.XsdNodeFactory;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.model.XsdSchemaImportLocation;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.util.XsdNamespaceUtils;

import java.util.*;

import static org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.AlgPhase.POSTPROCESSING;
import static org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.AlgPhase.PREPROCESSING;
import static org.xdef.impl.util.conv.schema.util.SchemaLoggerDefs.*;
import static org.xdef.impl.util.conv.schema.xd2schema.xsd.util.Xd2XsdLoggerDefs.XSD_XDEF_EXTRA_ADAPTER;

/**
 * Transforms x-definition nodes into xsd nodes
 *
 * Creates new schemas based on post-processing via {@link #transformNodes}
 */
public class Xd2XsdExtraSchemaAdapter extends AbstractXd2XsdAdapter {

    /**
     * Input x-definition used for transformation
     */
    private final XDefinition sourceXDefinition;

    /**
     * Original namespace context used in x-definition {@link #sourceXDefinition}
     */
    private NamespaceMap sourceNamespaceCtx = null;

    protected Xd2XsdExtraSchemaAdapter(XDefinition xDefinition) {
        this.sourceXDefinition = xDefinition;
    }

    /**
     * Set original (x-definition) namespace context
     * @param namespaceCtx
     * @param xsdTargetPrefix
     */
    public void setSourceNamespaceCtx(final NamespaceMap namespaceCtx, final String xsdTargetPrefix) {
        sourceNamespaceCtx = new NamespaceMap((HashMap)namespaceCtx.clone());
        sourceNamespaceCtx.remove(xsdTargetPrefix);
    }

    /**
     * Transform given x-definition nodes {@paramref allNodesToResolve} into XSD nodes and then insert them into related XSD documents
     * @param allNodesToResolve     nodes to be transformed
     */
    protected Set<String> transformNodes(final Map<String, Map<String, XNode>> allNodesToResolve) {
        SchemaLogger.printP(LOG_INFO, POSTPROCESSING, sourceXDefinition, "Transforming gathered nodes into extra schemas ...");

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

                final Map<String, XNode> nodesInSchemaToResolve = allNodesToResolve.get(schemaTargetNsUri);

                if (nodesInSchemaToResolve != null) {
                    // Filter nodes which should be resolved by current x-definition
                    final ArrayList<XNode> nodesToResolve = new ArrayList<XNode>(nodesInSchemaToResolve.values());
                    final Iterator<XNode> itr2 = nodesToResolve.iterator();
                    XNode n;
                    while (itr2.hasNext()) {
                        n = itr2.next();
                        if (!sourceSystemId.equals(XsdNamespaceUtils.getSystemIdFromXPos(n.getXDPosition()))) {
                            itr2.remove();
                        }
                    }

                    if (!nodesToResolve.isEmpty()) {
                        final SchemaAdapter adapter = new SchemaAdapter(sourceXDefinition);
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

    /**
     * Internal class for transformation of given x-definition nodes into related XSD document
     */
    class SchemaAdapter extends AbstractXd2XsdAdapter {

        /**
         * Input x-definition used for transformation
         */
        private final XDefinition sourceXDefinition;

        /**
         * Output XSD document
         */
        private XmlSchema schema = null;

        protected SchemaAdapter(XDefinition xDefinition) {
            this.sourceXDefinition = xDefinition;
        }

        /**
         * Creates or updates XSD document and then inserts into it given nodes
         * @param namespaceCtx              X-definition namespace context
         * @param nodesInSchemaToResolve    Nodes to be created
         * @param targetNsUri               XSD document target namespace URI
         * @param importLocation            XSD document location
         */
        protected void createOrUpdateSchema(final NamespaceMap namespaceCtx,
                                            final ArrayList<XNode> nodesInSchemaToResolve,
                                            final String targetNsUri,
                                            final XsdSchemaImportLocation importLocation) {
            SchemaLogger.printG(LOG_INFO, XSD_XDEF_EXTRA_ADAPTER, "====================");
            SchemaLogger.printG(LOG_INFO, XSD_XDEF_EXTRA_ADAPTER, "Post-processing XSD document. TargetNamespace=" + targetNsUri);
            SchemaLogger.printG(LOG_INFO, XSD_XDEF_EXTRA_ADAPTER, "====================");

            final String schemaName = createOrGetXsdSchema(namespaceCtx, targetNsUri, importLocation);

            final XsdNodeFactory xsdFactory = new XsdNodeFactory(schema, adapterCtx);
            final Xd2XsdTreeAdapter treeAdapter = new Xd2XsdTreeAdapter(schema, schemaName, xsdFactory, adapterCtx);
            final Xd2XsdReferenceAdapter referenceAdapter = new Xd2XsdReferenceAdapter(schema, schemaName, xsdFactory, treeAdapter, adapterCtx);

            treeAdapter.setPostProcessing();
            referenceAdapter.setPostProcessing();
            referenceAdapter.extractRefsAndImports(nodesInSchemaToResolve);

            transformNodes(treeAdapter, nodesInSchemaToResolve);
        }

        /**
         * Creates or find XSD document. Then initialize XSD document
         *
         * @param namespaceCtx
         * @param targetNsUri
         * @param importLocation
         * @return  instance of xml schema
         */
        private String createOrGetXsdSchema(final NamespaceMap namespaceCtx,
                                            final String targetNsUri,
                                            final XsdSchemaImportLocation importLocation) {
            final String schemaName = importLocation.getFileName();
            if (adapterCtx.existsSchemaLocation(targetNsUri)) {
                schema = adapterCtx.findSchema(schemaName, true, POSTPROCESSING);
            } else {
                schema = createOrGetXsdSchema(targetNsUri, schemaName);
                initSchemaNamespace(schemaName, namespaceCtx, targetNsUri, importLocation);
            }

            // TODO: based on top attributes/elements ... if attr -> then only attr qualified?
            initSchemaFormDefault();
            return schemaName;
        }

        /**
         * Creates or find XSD document
         * If schema already exists, return value is reference to already existing schema.
         *
         * @param targetNsUri   target namespace Uri
         * @param schemaName    XSD document name
         * @return instance of xml schema
         */
        private XmlSchema createOrGetXsdSchema(final String targetNsUri, final String schemaName) {
            XmlSchema schema = adapterCtx.findSchema(schemaName, false, POSTPROCESSING);

            if (schema == null) {
                adapterCtx.addSchemaName(schemaName);
                schema = new XmlSchema(targetNsUri, schemaName, adapterCtx.getXmlSchemaCollection());
                SchemaLogger.print(LOG_INFO, PREPROCESSING, schemaName, "Initialize new XSD document");
            } else {
                SchemaLogger.print(LOG_INFO, PREPROCESSING, schemaName, "Schema already exists");
            }

            return schema;
        }

        /**
         * Initializes XSD document namespace
         *
         * If schema namespace context already exist, then merge it with {@paramref namespaceCtx)
         *
         * @param schemaName        XSD document name
         * @param namespaceCtx      current x-definition namespace context
         * @param targetNsUri       XSD document target namespace URI
         * @param importLocation    XSD document location
         * @return
         */
        private void initSchemaNamespace(final String schemaName,
                                         final NamespaceMap namespaceCtx,
                                         final String targetNsUri,
                                         final XsdSchemaImportLocation importLocation) {
            SchemaLogger.printP(LOG_DEBUG, POSTPROCESSING, sourceXDefinition, "Initializing namespace context ...");

            // Namespace initialization
            final String targetNsPrefix = XsdNamespaceUtils.getNsPrefixFromExtraSchemaName(importLocation.getFileName());
            XsdNamespaceUtils.addNamespaceToCtx(namespaceCtx, targetNsPrefix, targetNsUri, schemaName, POSTPROCESSING);
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
        private void transformNodes(final Xd2XsdTreeAdapter treeAdapter, final ArrayList<XNode> nodes) {
            SchemaLogger.printG(LOG_INFO, XSD_XDEF_EXTRA_ADAPTER, "*** Transformation of x-definition tree to schema ***");

            for (XNode n : nodes) {
                final XmlSchemaObject xsdNode = treeAdapter.convertTree(n);
                if (xsdNode instanceof XmlSchemaElement) {
                    SchemaLogger.printP(LOG_INFO, POSTPROCESSING, n, "Add top-level element.");
                } else if (xsdNode instanceof XmlSchemaAttribute) {
                    SchemaLogger.printP(LOG_INFO, POSTPROCESSING, n, "Add top-level attribute.");
                }
            }
        }

    }
}
