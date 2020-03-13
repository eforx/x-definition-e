package org.xdef.impl.util.conv.schema.xd2schema.xsd.adapter;

import javafx.util.Pair;
import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.xdef.XDPool;
import org.xdef.impl.XDefinition;
import org.xdef.impl.util.conv.schema.util.SchemaLogger;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.Xd2XsdFeature;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.factory.XsdNodeFactory;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.model.SchemaNode;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.model.UniqueConstraint;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.util.Xd2XsdUtils;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.util.XsdNameUtils;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.util.XsdPostProcessor;
import org.xdef.model.XMDefinition;

import javax.xml.namespace.QName;
import java.util.*;

import static org.xdef.impl.util.conv.schema.util.SchemaLoggerDefs.*;
import static org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.AlgPhase.POSTPROCESSING;
import static org.xdef.impl.util.conv.schema.xd2schema.xsd.util.Xd2XsdLoggerDefs.XSD_PP_ADAPTER;

/**
 * Makes post processing transformation on created XSD documents
 */
public class Xd2XsdPostProcessingAdapter extends AbstractXd2XsdAdapter {

    /**
     * Set of post processing algorithms
     */
    private XsdPostProcessor postProcessor;

    /**
     * Run post processing on pool of x-definitions
     * Post processing features can be enabled by calling {@link #setFeatures(Set)} or {@link #addFeature(Xd2XsdFeature)}
     *
     * @param xdPool    pool of x-definitions to be processed
     */
    public void process(final XDPool xdPool) {
        if (!adapterCtx.hasEnableFeature(Xd2XsdFeature.POSTPROCESSING)) {
            return;
        }

        SchemaLogger.print(LOG_INFO, POSTPROCESSING, XSD_PP_ADAPTER,"*** Post-processing XDPool ***");

        postProcessor = new XsdPostProcessor(adapterCtx);
        final Set<String> updatedNamespaces = new HashSet<String>();
        processNodes(xdPool, updatedNamespaces);
        processReferences();
        processQNames(updatedNamespaces);
        createKeysAndRefs(xdPool);
    }

    /**
     * Run post processing on x-definition
     * Post processing features can be enabled by calling {@link #setFeatures(Set)} or {@link #addFeature(Xd2XsdFeature)}
     *
     * @param xDef  x-definition to be processed
     */
    public void process(final XDefinition xDef) {
        if (!adapterCtx.hasEnableFeature(Xd2XsdFeature.POSTPROCESSING)) {
            return;
        }

        SchemaLogger.print(LOG_INFO, POSTPROCESSING, XSD_PP_ADAPTER,"*** Post-processing XDefinition ***");

        postProcessor = new XsdPostProcessor(adapterCtx);
        final Set<String> updatedNamespaces = new HashSet<String>();
        if (!adapterCtx.getNodesToBePostProcessed().isEmpty() && !adapterCtx.getExtraSchemaLocationsCtx().isEmpty()) {
            processNodes(xDef, updatedNamespaces);
        }
        processReferences();
        processQNames(updatedNamespaces);
        createKeysAndRefs(xDef);
    }

    /**
     * Run post processing on x-definition pool
     * @param xdPool                x-definition pool to be processed
     * @param updatedNamespaces     already processed namespaces
     */
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
     * @param updatedNamespaces     already processed namespaces
     */
    private void processNodes(final XDefinition xDef, final Set<String> updatedNamespaces) {
        if (!adapterCtx.hasEnableFeature(Xd2XsdFeature.POSTPROCESSING_EXTRA_SCHEMAS)) {
            return;
        }

        SchemaLogger.printP(LOG_INFO, POSTPROCESSING, xDef,"Creating nodes ...");
        final Xd2XsdExtraSchemaAdapter postProcessingAdapter = new Xd2XsdExtraSchemaAdapter(xDef);
        final XmlSchema schema = adapterCtx.findSchema(xDef.getName(), true, POSTPROCESSING);
        postProcessingAdapter.setAdapterCtx(adapterCtx);
        postProcessingAdapter.setSourceNamespaceCtx((NamespaceMap) schema.getNamespaceContext(), schema.getSchemaNamespacePrefix());
        updatedNamespaces.addAll(postProcessingAdapter.transformNodes(adapterCtx.getNodesToBePostProcessed()));
    }

    /**
     * Updates XSD references which are breaking XSD document rules
     */
    private void processReferences() {
        if (!adapterCtx.hasEnableFeature(Xd2XsdFeature.POSTPROCESSING_REFS)) {
            return;
        }

        postProcessor.processRefs();
    }

    /**
     * Updates XSD attributes and elements QNames of schemas created by post processing
     * @param updatedNamespaces
     */
    private void processQNames(final Set<String> updatedNamespaces) {
        SchemaLogger.print(LOG_INFO, POSTPROCESSING, XSD_PP_ADAPTER,"Processing qualified names ...");
        for (String schemaNs : updatedNamespaces) {
            if (adapterCtx.isPostProcessingNamespace(schemaNs) && !adapterCtx.existsSchemaLocation(schemaNs)) {
                continue;
            }

            final String schemaName = adapterCtx.findSchemaNameByNamespace(schemaNs, true, POSTPROCESSING);
            final XmlSchema schema = adapterCtx.findSchema(schemaName, true, POSTPROCESSING);
            final Map<String, SchemaNode> nodes = adapterCtx.getNodes().get(schemaName);
            if (nodes != null && !nodes.isEmpty()) {
                for (SchemaNode n : nodes.values()) {
                    if (n.isXsdAttr()) {
                        XsdNameUtils.resolveAttributeQName(schema, n.toXsdAttr(), n.getXdName());
                        XsdNameUtils.resolveAttributeSchemaTypeQName(schema, n.toXsdAttr());
                    } else if (n.isXsdElem()) {
                        XsdNameUtils.resolveElementQName(schema, n.toXdElem(), n.toXsdElem(), adapterCtx);
                        XsdNameUtils.resolveElementSchemaTypeQName(schema, n.toXsdElem());
                    }
                }
            }
        }
    }

    /**
     * Creates unique constraints (xs:unique xs:key, xs:keyref) nodes based on gathered uniqueSet data from x-definition pool
     * @param xdPool    x-definition pool
     */
    private void createKeysAndRefs(final XDPool xdPool) {
        for (XMDefinition xDef : xdPool.getXMDefinitions()) {
            createKeysAndRefs((XDefinition)xDef);
        }
    }

    /**
     * Creates unique constraints (xs:unique xs:key, xs:keyref) nodes based on gathered uniqueSet data from x-definition
     * @param xDef    input x-definition where XSD elements should be inserted
     */
    private void createKeysAndRefs(final XDefinition xDef) {
        if (!adapterCtx.hasEnableFeature(Xd2XsdFeature.POSTPROCESSING_UNIQUE)) {
            return;
        }

        SchemaLogger.print(LOG_INFO, POSTPROCESSING, XSD_PP_ADAPTER,"Processing unique constraints ...");

        createRestrictionConstraints(xDef.getName(), "");
        createRestrictionConstraints(xDef.getName(), xDef.getName());
    }

    /**
     * Creates unique constraints xs:unique xs:key, xs:keyref) nodes based on gathered uniqueSet data from x-definition
     *
     * If constraint has no keyref or only unsupported keyref, then xs:unique is created instead of xs:key
     *
     * @param xDefName      x-definition name
     * @param systemId      source system id of unique set (empty for unique sets places in root of x-definition)
     */
    private void createRestrictionConstraints(final String xDefName, final String systemId) {
        final Map<String, List<UniqueConstraint>> uniqueInfoMap = adapterCtx.getSchemaUniqueConstraints(systemId);
        if (uniqueInfoMap == null || uniqueInfoMap.isEmpty()) {
            return;
        }

        SchemaLogger.print(LOG_INFO, POSTPROCESSING, XSD_PP_ADAPTER,"Creating unique constraints for x-definition. XDefName=" + xDefName + ", systemId=" + systemId);

        int i = 0;
        int j = 0;

        for (Map.Entry<String, List<UniqueConstraint>> uniqueInfoEntry : uniqueInfoMap.entrySet()) {
            final List<UniqueConstraint> uniqueInfoList = uniqueInfoEntry.getValue();
            if (uniqueInfoList == null || uniqueInfoList.isEmpty()) {
                continue;
            }

            for (UniqueConstraint u : uniqueInfoList) {
                SchemaLogger.print(LOG_INFO, POSTPROCESSING, XSD_PP_ADAPTER,"Creating unique constraint. UniqueSet=" + u.getPath());

                final Map<String, Map<String, List<Pair<String, XmlSchemaAttribute>>>> keys = u.getKeys();
                final Map<String, Map<String, List<Pair<String, XmlSchemaAttribute>>>> refs = u.getRefs();

                if (keys == null || keys.isEmpty()) {
                    SchemaLogger.print(LOG_DEBUG, POSTPROCESSING, XSD_PP_ADAPTER,"Unique constraint has no keys. UniqueSet=" + u.getPath());
                    continue;
                }

                for (Map.Entry<String, Map<String, List<Pair<String, XmlSchemaAttribute>>>> varKeys : keys.entrySet()) {
                    final String varName = varKeys.getKey();

                    if (varKeys.getValue().isEmpty()) {
                        SchemaLogger.print(LOG_INFO, POSTPROCESSING, XSD_PP_ADAPTER,"Unique set has no key for variable. UniqueSet=" + u.getPath() + "Variable=" + varName);
                        continue;
                    }

                    if (varKeys.getValue().size() > 1) {
                        SchemaLogger.print(LOG_INFO, POSTPROCESSING, XSD_PP_ADAPTER,"Unique set variable has key in different XPaths - not supported now. UniqueSet=" + u.getPath() + "Variable=" + varName);
                        continue;
                    }

                    final Map<String, List<Pair<String, XmlSchemaAttribute>>> varRefs = refs.get(varName);

                    for (Map.Entry<String, List<Pair<String, XmlSchemaAttribute>>> pathKeys : varKeys.getValue().entrySet()) {
                        if (pathKeys.getValue().size() > 1) {
                            SchemaLogger.print(LOG_INFO, POSTPROCESSING, XSD_PP_ADAPTER,"Unique set variable is used in multiple attributes - not supported now. UniqueSet=" + u.getPath() + "Variable=" + varName);
                            continue;
                        }

                        final String xPath = pathKeys.getKey();
                        final String xPathParentNode = getParentNodePath(uniqueInfoEntry.getKey(), xPath);
                        final SchemaNode rootSchemaNode = adapterCtx.findSchemaNode(xDefName, xPathParentNode);

                        if (rootSchemaNode == null) {
                            SchemaLogger.print(LOG_WARN, POSTPROCESSING, XSD_PP_ADAPTER, "Root node of unique set has not been found! UniqueSet=" + u.getPath() + ", XPath=" + xPathParentNode);
                        } else if (!rootSchemaNode.isXsdElem()) {
                            SchemaLogger.print(LOG_WARN, POSTPROCESSING, XSD_PP_ADAPTER, "Root node of unique set is not element!. UniqueSet=" + u.getPath() + ", XPath=" + xPathParentNode);
                        } else {
                            SchemaLogger.print(LOG_DEBUG, POSTPROCESSING, XSD_PP_ADAPTER,"Creating key/unique for unique set. UniqueSet=" + u.getPath() + ", Variable=" + varName);

                            final XmlSchemaElement rootElem = rootSchemaNode.toXsdElem();
                            final String keyFieldPath = buildFieldXPath(pathKeys.getValue());
                            final boolean createUnique = varRefs == null || varRefs.isEmpty();
                            final String keyName = "key_" + u.getName() + "_" + i;

                            SchemaLogger.print(LOG_DEBUG, POSTPROCESSING, XSD_PP_ADAPTER,"Unique set - key/unique. XPath=" + xPath + ", XPathParent=" + xPathParentNode);

                            final XmlSchemaIdentityConstraint identityConstraint = createUnique ? new XmlSchemaUnique() : new XmlSchemaKey();
                            identityConstraint.setName(keyName);
                            addConstraintInfo(identityConstraint, Xd2XsdUtils.relativeXPath(xPath, xPathParentNode), keyFieldPath);

                            if (uniqueInfoEntry.getKey().isEmpty() && systemId.isEmpty()) {
                                identityConstraint.setAnnotation(XsdNodeFactory.createAnnotation("Unique set was placed in global declaration inside x-definition", adapterCtx));
                            }

                            rootElem.getConstraints().add(identityConstraint);

                            for (Map.Entry<String, List<Pair<String, XmlSchemaAttribute>>> varRefEntry : varRefs.entrySet()) {
                                final String xPathRef = varRefEntry.getKey();

                                SchemaLogger.print(LOG_DEBUG, POSTPROCESSING, XSD_PP_ADAPTER,"Creating keyref for unique set. UniqueSet=" + u.getPath() + ", Variable=" + varName);

                                for (Pair<String, XmlSchemaAttribute> varRef : varRefEntry.getValue()) {
                                    final XmlSchemaKeyref ref = new XmlSchemaKeyref();
                                    ref.setName("ref_" + u.getName() + "_" + i + "_" + j);
                                    ref.setRefer(new QName(keyName));
                                    addConstraintInfo(ref, Xd2XsdUtils.relativeXPath(xPathRef, xPathParentNode), "@" + varRef.getKey());

                                    SchemaLogger.print(LOG_DEBUG, POSTPROCESSING, XSD_PP_ADAPTER,"Unique set - keyref. XPath=" + xPathRef + ", XPathParent=" + xPathParentNode);

                                    rootElem.getConstraints().add(ref);
                                    j++;
                                }
                            }

                            i++;
                        }
                    }
                }
            }
        }
    }

    /**
     * Get parent node of restriction constraint node (xs:selector value)
     * @param uniquePath    unique set path
     * @param xPath         xPath of constraint node
     * @return xPath of parent node of restriction constraints
     */
    private String getParentNodePath(final String uniquePath, final String xPath) {
        String res;
        if (!uniquePath.isEmpty()) {
            int splitPos = xPath.lastIndexOf('/');
            if (splitPos != -1) {
                res = xPath.substring(0, splitPos);
            } else {
                res = xPath;
            }
        } else {
            int splitPos = xPath.indexOf('/');
            if (splitPos != -1) {
                res = xPath.substring(0, splitPos);
            } else {
                res = xPath;
            }
        }

        return res;
    }

    /**
     * Creates xPath xs:field value for identity constraint
     * @param paths     XPath of restriction constrains nodes
     * @return xPath for xs:field
     */
    private String buildFieldXPath(final List<Pair<String, XmlSchemaAttribute>> paths) {
        final Set<String> fieldXPathSet = new HashSet<String>();

        for (Pair<String, XmlSchemaAttribute> keyPath : paths) {
            fieldXPathSet.add("@" + keyPath.getKey());
        }

        Iterator<String> keyPathItr = fieldXPathSet.iterator();
        final StringBuilder pathStringBuilder = new StringBuilder();
        pathStringBuilder.append(keyPathItr.next());
        while (keyPathItr.hasNext()) {
            pathStringBuilder.append("|");
            pathStringBuilder.append(keyPathItr.next());
        }

        return pathStringBuilder.toString();
    }

    /**
     * Creates, initializes and inserts xs:selector and xs:field nodes into constraint node
     * @param identityConstraint    Constraint node where created nodes will be placed
     * @param selectorXPath         xs:selector xpath value
     * @param fieldXPath            xs:field xpath value
     */
    private void addConstraintInfo(final XmlSchemaIdentityConstraint identityConstraint, final String selectorXPath, final String fieldXPath) {
        final XmlSchemaXPath xPathSelectorRef = new XmlSchemaXPath();
        final XmlSchemaXPath xPathFieldRef = new XmlSchemaXPath();

        identityConstraint.setSelector(xPathSelectorRef);
        identityConstraint.getFields().add(xPathFieldRef);

        xPathSelectorRef.setXPath(selectorXPath);
        xPathFieldRef.setXPath(fieldXPath);
    }
}