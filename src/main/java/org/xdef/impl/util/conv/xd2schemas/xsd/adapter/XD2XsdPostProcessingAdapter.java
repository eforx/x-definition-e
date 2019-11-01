package org.xdef.impl.util.conv.xd2schemas.xsd.adapter;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.xdef.XDPool;
import org.xdef.impl.XDefinition;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.SchemaNode;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdNameUtils;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdPostProcessor;
import org.xdef.model.XMDefinition;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.AlgPhase.POSTPROCESSING;
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XsdLoggerDefs.LOG_INFO;

public class XD2XsdPostProcessingAdapter extends AbstractXd2XsdAdapter {

    public void process(final XDPool xdPool) {
        XsdLogger.printP(LOG_INFO, POSTPROCESSING, "*** Post-processing XDPool ***");

        final Set<String> updatedNamespaces = new HashSet<String>();
        processNodes(xdPool, updatedNamespaces);
        processReferences();
        processQNames(updatedNamespaces);
    }

    public void process(final XDefinition xDef) {
        XsdLogger.printP(LOG_INFO, POSTPROCESSING, "*** Post-processing XDefinition ***");
        final Set<String> updatedNamespaces = new HashSet<String>();
        if (!adapterCtx.getNodesToBePostProcessed().isEmpty() && !adapterCtx.getExtraSchemaLocationsCtx().isEmpty()) {
            processNodes(xDef, updatedNamespaces);
        }
        processReferences();
        processQNames(updatedNamespaces);
    }

    private void processNodes(final XDPool xdPool, final Set<String> updatedNamespaces) {
        if (!adapterCtx.getNodesToBePostProcessed().isEmpty() && !adapterCtx.getExtraSchemaLocationsCtx().isEmpty()) {
            for (XMDefinition xDef : xdPool.getXMDefinitions()) {
                processNodes((XDefinition)xDef, updatedNamespaces);
            }
        }
    }

    private void processNodes(final XDefinition xDef, final Set<String> updatedNamespaces) {
        XsdLogger.printP(LOG_INFO, POSTPROCESSING, xDef,"Creating nodes ...");
        XD2XsdExtraSchemaAdapter postProcessingAdapter = new XD2XsdExtraSchemaAdapter(xDef);
        XmlSchema schema = adapterCtx.getSchema(xDef.getName(), true, POSTPROCESSING);
        postProcessingAdapter.setAdapterCtx(adapterCtx);
        postProcessingAdapter.setSourceNamespaceCtx((NamespaceMap) schema.getNamespaceContext(), schema.getSchemaNamespacePrefix());
        updatedNamespaces.addAll(postProcessingAdapter.transformNodes(adapterCtx.getNodesToBePostProcessed()));
    }

    private void processReferences() {
        XsdLogger.printP(LOG_INFO, POSTPROCESSING, "Processing reference nodes ...");
        XsdPostProcessor postProcessor = new XsdPostProcessor(adapterCtx);
        postProcessor.processRefs();
    }

    private void processQNames(final Set<String> updatedNamespaces) {
        XsdLogger.printP(LOG_INFO, POSTPROCESSING, "Processing qualified names ...");
        for (String schemaNs : updatedNamespaces) {
            String schemaName = adapterCtx.getSchemaNameByNamespace(schemaNs, true, POSTPROCESSING);
            XmlSchema schema = adapterCtx.getSchema(schemaName, true, POSTPROCESSING);
            Map<String, SchemaNode> nodes = adapterCtx.getNodes().get(schemaName);
            if (nodes != null && !nodes.isEmpty()) {
                for (SchemaNode n : nodes.values()) {
                    if (n.isXsdAttr()) {
                        XsdNameUtils.resolveAttributeQName(schema, n.toXsdAttr(), n.getXdName());
                    } else if (n.isXsdElem()) {
                        XsdNameUtils.resolveElementQName(schema, n.toXsdElem());
                    }
                }
            }
        }
    }
}
