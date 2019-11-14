package org.xdef.impl.util.conv.xd2schemas.xsd.adapter;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.xdef.XDPool;
import org.xdef.impl.XDefinition;
import org.xdef.impl.util.conv.xd2schemas.xsd.definition.XD2XsdFeature;
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
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XsdLoggerDefs.XSD_PP_ADAPTER;

public class XD2XsdPostProcessingAdapter extends AbstractXd2XsdAdapter {

    public void process(final XDPool xdPool) {
        if (!adapterCtx.hasEnableFeature(XD2XsdFeature.POSTPROCESSING)) {
            return;
        }

        XsdLogger.print(LOG_INFO, POSTPROCESSING, XSD_PP_ADAPTER,"*** Post-processing XDPool ***");

        final Set<String> updatedNamespaces = new HashSet<String>();
        processNodes(xdPool, updatedNamespaces);
        processReferences();
        processQNames(updatedNamespaces);
    }

    public void process(final XDefinition xDef) {
        if (!adapterCtx.hasEnableFeature(XD2XsdFeature.POSTPROCESSING)) {
            return;
        }

        XsdLogger.print(LOG_INFO, POSTPROCESSING, XSD_PP_ADAPTER,"*** Post-processing XDefinition ***");
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
        if (!adapterCtx.hasEnableFeature(XD2XsdFeature.POSTPROCESSING_EXTRA_SCHEMAS)) {
            return;
        }

        XsdLogger.printP(LOG_INFO, POSTPROCESSING, xDef,"Creating nodes ...");
        final XD2XsdExtraSchemaAdapter postProcessingAdapter = new XD2XsdExtraSchemaAdapter(xDef);
        final XmlSchema schema = adapterCtx.getSchema(xDef.getName(), true, POSTPROCESSING);
        postProcessingAdapter.setAdapterCtx(adapterCtx);
        postProcessingAdapter.setSourceNamespaceCtx((NamespaceMap) schema.getNamespaceContext(), schema.getSchemaNamespacePrefix());
        updatedNamespaces.addAll(postProcessingAdapter.transformNodes(adapterCtx.getNodesToBePostProcessed()));
    }

    private void processReferences() {
        if (!adapterCtx.hasEnableFeature(XD2XsdFeature.POSTPROCESSING_REFS)) {
            return;
        }

        XsdLogger.print(LOG_INFO, POSTPROCESSING, XSD_PP_ADAPTER,"Processing reference nodes ...");
        final XsdPostProcessor postProcessor = new XsdPostProcessor(adapterCtx);
        postProcessor.processRefs();
    }

    private void processQNames(final Set<String> updatedNamespaces) {
        XsdLogger.print(LOG_INFO, POSTPROCESSING, XSD_PP_ADAPTER,"Processing qualified names ...");
        for (String schemaNs : updatedNamespaces) {
            if (adapterCtx.isPostProcessingNamespace(schemaNs) && !adapterCtx.existsSchemaLocation(schemaNs)) {
                continue;
            }

            final String schemaName = adapterCtx.getSchemaNameByNamespace(schemaNs, true, POSTPROCESSING);
            final XmlSchema schema = adapterCtx.getSchema(schemaName, true, POSTPROCESSING);
            final Map<String, SchemaNode> nodes = adapterCtx.getNodes().get(schemaName);
            if (nodes != null && !nodes.isEmpty()) {
                for (SchemaNode n : nodes.values()) {
                    if (n.isXsdAttr()) {
                        XsdNameUtils.resolveAttributeQName(schema, n.toXsdAttr(), n.getXdName());
                        XsdNameUtils.resolveAttributeSchemaTypeQName(schema, n);
                    } else if (n.isXsdElem()) {
                        XsdNameUtils.resolveElementQName(schema, n.toXsdElem());
                        XsdNameUtils.resolveElementSchemaTypeQName(schema, n);
                    }
                }
            }
        }
    }
}
