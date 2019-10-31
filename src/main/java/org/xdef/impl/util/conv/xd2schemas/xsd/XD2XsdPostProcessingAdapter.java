package org.xdef.impl.util.conv.xd2schemas.xsd;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.xdef.XDPool;
import org.xdef.impl.XDefinition;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.SchemaRefNode;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdNameUtils;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdNamespaceUtils;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdPostProcessor;
import org.xdef.model.XMDefinition;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.xdef.impl.util.conv.xd2schemas.xsd.util.AlgPhase.POSTPROCESSING;
import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.LOG_INFO;

class XD2XsdPostProcessingAdapter extends AbstractXd2XsdAdapter {

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
        XmlSchema schema = XsdNamespaceUtils.getSchema(adapterCtx.getXmlSchemaCollection(), xDef.getName(), true, POSTPROCESSING);
        postProcessingAdapter.setAdapterCtx(adapterCtx);
        postProcessingAdapter.setSourceNamespaceCtx((NamespaceMap) schema.getNamespaceContext(), schema.getSchemaNamespacePrefix());
        updatedNamespaces.addAll(postProcessingAdapter.transformNodes(adapterCtx.getNodesToBePostProcessed()));
    }

    private void processReferences() {
        XsdLogger.printP(LOG_INFO, POSTPROCESSING, "Processing reference nodes ...");
        XsdPostProcessor postProcessor = new XsdPostProcessor(adapterCtx.getXmlSchemaCollection(), adapterCtx.getNodeRefs());
        postProcessor.processRefs();
    }

    private void processQNames(final Set<String> updatedNamespaces) {
        XsdLogger.printP(LOG_INFO, POSTPROCESSING, "Processing qualified names ...");
        for (String schemaNs : updatedNamespaces) {
            String schemaName = XsdNamespaceUtils.getSchemaNameByNamespace(adapterCtx, schemaNs, true, POSTPROCESSING);
            XmlSchema schema = XsdNamespaceUtils.getSchema(adapterCtx.getXmlSchemaCollection(), schemaName, true, POSTPROCESSING);
            Map<String, SchemaRefNode> nodes = adapterCtx.getNodeRefs().get(schemaName);
            if (nodes != null && !nodes.isEmpty()) {
                for (SchemaRefNode n : nodes.values()) {
                    if (n.isAttr()) {
                        XsdNameUtils.resolveAttributeQName(schema, n.toXsdAttr(), n.getXdNode().getName());
                    } else if (n.isElem()) {
                        XsdNameUtils.resolveElementQName(schema, n.toXsdElem());
                    }
                }
            }
        }
    }
}
