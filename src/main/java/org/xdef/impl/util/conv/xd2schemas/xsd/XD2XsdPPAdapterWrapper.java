package org.xdef.impl.util.conv.xd2schemas.xsd;

import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.xdef.impl.XDefinition;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.XmlSchemaImportLocation;

import java.util.*;

/**
 * Transforms x-definition nodes into xsd nodes
 *
 * Creates new schemas based on post-processing via {@link #transformNodes}
 */
class XD2XsdPPAdapterWrapper extends AbstractXd2XsdAdapter {

    /**
     * Input x-definition used for transformation
     */
    private final XDefinition sourceXDefinition;

    /**
     * Original namespace context used in x-definition {@link #sourceXDefinition}
     */
    private NamespaceMap sourceNamespaceCtx = null;

    protected XD2XsdPPAdapterWrapper(int logLevel, XDefinition xDefinition) {
        this.logLevel = logLevel;
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
    protected void transformNodes(final Map<String, List<XNode>> allNodesToResolve) {
        Map<String, XmlSchemaImportLocation> schemasToResolve = (HashMap)((HashMap)adapterCtx.getExtraSchemaLocationsCtx()).clone();

        int lastSizeMap = schemasToResolve.size();

        while (!schemasToResolve.isEmpty()) {
            Iterator<Map.Entry<String, XmlSchemaImportLocation>> itr = schemasToResolve.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry<String, XmlSchemaImportLocation> schemaToResolve = itr.next();
                final String schemaTargetNsUri = schemaToResolve.getKey();
                List<XNode> nodesInSchemaToResolve = allNodesToResolve.get(schemaTargetNsUri);
                if (nodesInSchemaToResolve != null) {
                    if (!nodesInSchemaToResolve.isEmpty()) {
                        XD2XsdPPAdapter adapter = new XD2XsdPPAdapter(logLevel, sourceXDefinition);
                        adapter.setAdapterCtx(adapterCtx);
                        adapter.createOrUpdateSchema(new NamespaceMap((HashMap) sourceNamespaceCtx.clone()), allNodesToResolve, nodesInSchemaToResolve, schemaTargetNsUri, schemaToResolve.getValue());
                    }

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

}
