package org.xdef.impl.util.conv.xd2schemas.xsd.adapter;

import javafx.util.Pair;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaKey;
import org.apache.ws.commons.schema.XmlSchemaKeyref;
import org.apache.ws.commons.schema.XmlSchemaXPath;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.xdef.XDPool;
import org.xdef.impl.XDefinition;
import org.xdef.impl.util.conv.xd2schemas.xsd.definition.XD2XsdFeature;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.SchemaNode;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.UniqueConstraints;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XD2XsdUtils;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdNameUtils;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdPostProcessor;
import org.xdef.model.XMDefinition;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.AlgPhase.POSTPROCESSING;
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XsdLoggerDefs.LOG_INFO;
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XsdLoggerDefs.XSD_PP_ADAPTER;

public class XD2XsdPostProcessingAdapter extends AbstractXd2XsdAdapter {

    /**
     * Set of post processing algorithms
     */
    private XsdPostProcessor postProcessor;

    /**
     * Run post processing on pool of x-definitions
     * Post processing features can be enabled by calling {@link #setFeatures(Set)} or {@link #addFeature(XD2XsdFeature)}
     *
     * @param xdPool    pool of x-definitions to be processed
     */
    public void process(final XDPool xdPool) {
        if (!adapterCtx.hasEnableFeature(XD2XsdFeature.POSTPROCESSING)) {
            return;
        }

        XsdLogger.print(LOG_INFO, POSTPROCESSING, XSD_PP_ADAPTER,"*** Post-processing XDPool ***");

        postProcessor = new XsdPostProcessor(adapterCtx);
        final Set<String> updatedNamespaces = new HashSet<String>();
        processNodes(xdPool, updatedNamespaces);
        processReferences();
        processQNames(updatedNamespaces);
        createKeysAndRefs(xdPool);
    }

    /**
     * Run post processing on x-definition
     * Post processing features can be enabled by calling {@link #setFeatures(Set)} or {@link #addFeature(XD2XsdFeature)}
     *
     * @param xDef  x-definition to be processed
     */
    public void process(final XDefinition xDef) {
        if (!adapterCtx.hasEnableFeature(XD2XsdFeature.POSTPROCESSING)) {
            return;
        }

        XsdLogger.print(LOG_INFO, POSTPROCESSING, XSD_PP_ADAPTER,"*** Post-processing XDefinition ***");

        postProcessor = new XsdPostProcessor(adapterCtx);
        final Set<String> updatedNamespaces = new HashSet<String>();
        if (!adapterCtx.getNodesToBePostProcessed().isEmpty() && !adapterCtx.getExtraSchemaLocationsCtx().isEmpty()) {
            processNodes(xDef, updatedNamespaces);
        }
        processReferences();
        processQNames(updatedNamespaces);
        createKeysAndRefs(xDef);
    }

    private void processNodes(final XDPool xdPool, final Set<String> updatedNamespaces) {
        if (!adapterCtx.getNodesToBePostProcessed().isEmpty() && !adapterCtx.getExtraSchemaLocationsCtx().isEmpty()) {
            for (XMDefinition xDef : xdPool.getXMDefinitions()) {
                processNodes((XDefinition)xDef, updatedNamespaces);
            }
        }
    }

    /**
     * Creates XSD nodes which are originally located in different x-definition namespace
     * @param xDef                  x-definition source of XSD nodes
     * @param updatedNamespaces     processed namespaces
     */
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

    /**
     * Updates XSD references which are breaking XSD schema rules
     */
    private void processReferences() {
        if (!adapterCtx.hasEnableFeature(XD2XsdFeature.POSTPROCESSING_REFS)) {
            return;
        }

        postProcessor.processRefs();
    }

    /**
     * Updates XSD attributes and elements QNames of schemas created by post processing
     * @param updatedNamespaces
     */
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
                        XsdNameUtils.resolveElementQName(schema, n.toXdElem(), n.toXsdElem(), adapterCtx);
                        XsdNameUtils.resolveElementSchemaTypeQName(schema, n);
                    }
                }
            }
        }
    }

    private void createKeysAndRefs(final XDPool xdPool) {
        for (XMDefinition xDef : xdPool.getXMDefinitions()) {
            createKeysAndRefs((XDefinition)xDef);
        }
    }

    /**
     * Creates xs:unique, xs:key and xs:keyref XSD elements based on gathered uniqueSet data
     * @param xDef    input x-definition where XSD elements should be inserted
     */
    private void createKeysAndRefs(final XDefinition xDef) {
        if (!adapterCtx.hasEnableFeature(XD2XsdFeature.POSTPROCESSING_UNIQUE) && !adapterCtx.hasEnableFeature(XD2XsdFeature.POSTPROCESSING_KEYS_AND_REFS)) {
            return;
        }

        final Map<String, List<UniqueConstraints>> uniqueInfoMap = adapterCtx.getSchemUniqueInfo(xDef.getName());
        if (uniqueInfoMap == null || uniqueInfoMap.isEmpty()) {
            return;
        }
        final List<UniqueConstraints> uniqueInfoList = uniqueInfoMap.get("");

        if (uniqueInfoList != null && !uniqueInfoList.isEmpty()) {
            for (UniqueConstraints u : uniqueInfoList) {
                int i = 0;
                for (String keyPath : u.getKeys()) {
                    final XmlSchemaKey key = new XmlSchemaKey();
                    final Pair<String, String> xPath = XD2XsdUtils.xPathSplitByAttr(keyPath);
                    final XmlSchemaXPath xPathSelector = new XmlSchemaXPath();
                    final XmlSchemaXPath xPathField = new XmlSchemaXPath();

                    key.setName(u.getName() + "_" + i);
                    key.setSelector(xPathSelector);
                    key.getFields().add(xPathField);

//                    for (XmlSchemaElement xsdElem : rootElems) {
//                        xPathSelector.setXPath(XD2XsdUtils.relativeXPath(xPath.getKey(), xsdElem.getName()));
//                        xPathField.setXPath(XD2XsdUtils.relativeXPath(xPath.getValue(), xsdElem.getName()));
//                        xsdElem.getConstraints().add(key);
//                    }

                    int j = 0;
                    for (String refPath : u.getReferences()) {
                        final XmlSchemaKeyref keyRef = new XmlSchemaKeyref();
                        final Pair<String, String> keyRefxPath = XD2XsdUtils.xPathSplitByAttr(refPath);
                        final XmlSchemaXPath keyRefxPathSelector = new XmlSchemaXPath();
                        final XmlSchemaXPath keyRefxPathField = new XmlSchemaXPath();

                        keyRef.setName("ref_" + u.getName() + "_" + i + "_" + j);
                        keyRef.setRefer(new QName(key.getName()));
                        keyRef.setSelector(keyRefxPathSelector);
                        keyRef.getFields().add(keyRefxPathField);

//                        for (XmlSchemaElement xsdElem : rootElems) {
//                            keyRefxPathSelector.setXPath(XD2XsdUtils.relativeXPath(keyRefxPath.getKey(), xsdElem.getName()));
//                            keyRefxPathField.setXPath(XD2XsdUtils.relativeXPath(keyRefxPath.getValue(), xsdElem.getName()));
//                            xsdElem.getConstraints().add(keyRef);
//                        }

                        j++;
                    }
                    i++;
                }
            }
        }
//        for (XmlSchemaElement xsdElem : rootElems) {
//            final XmlSchemaKey key = new XmlSchemaKey();
//            key.setSelector();
//            xsdElem.getConstraints().add(key);
//        }
    }
}
