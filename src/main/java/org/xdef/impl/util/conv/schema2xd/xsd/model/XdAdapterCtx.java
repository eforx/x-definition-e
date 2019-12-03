package org.xdef.impl.util.conv.schema2xd.xsd.model;

import javafx.util.Pair;
import org.xdef.impl.util.conv.schema.util.XsdLogger;
import org.xdef.impl.util.conv.schema2xd.xsd.definition.Xsd2XdFeature;
import org.xdef.impl.util.conv.xd2schema.xsd.definition.XD2XsdFeature;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.xdef.impl.util.conv.schema.util.XsdLoggerDefs.*;
import static org.xdef.impl.util.conv.xd2schema.xsd.definition.AlgPhase.PREPROCESSING;

/**
 * Basic XSD context for transformation XSD schema to x-definition
 */
public class XdAdapterCtx {

    /**
     * Enabled algorithm features
     */
    final private Set<Xsd2XdFeature> features;

    /**
     * Target namespaces per x-definition
     * Key:     x-definition name
     * Value:   target namespace prefix, target namespace URI
     */
    private Map<String, Pair<String, String>> targetNamespaces;

    /**
     * Used namespaces per x-definition
     * Key:     x-definition name
     * Value:   target namespace prefix, target namespace URI
     */
    private Map<String, Map<String, String>> xDefNamespaces;

    /**
     * Target namespace URI per x-definition
     * Key:     target namespace URI
     * Value:   x-definition name
     */
    private Map<String, String> xDefTargetNamespaces;

    public XdAdapterCtx(Set<Xsd2XdFeature> features) {
        this.features = features;
    }

    /**
     * Initializes XD adapter context
     */
    public void init() {
        targetNamespaces = new HashMap<String, Pair<String, String>>();
        xDefNamespaces = new HashMap<String, Map<String, String>>();
        xDefTargetNamespaces = new HashMap<String, String>();
    }

    public void addTargetNamespace(final String xDefName, final Pair<String, String> targetNamespace) {
        if (targetNamespaces.containsKey(xDefName)) {
            XsdLogger.print(LOG_WARN, PREPROCESSING, XD_ADAPTER_CTX, "X-definition target namespace already exists. XDefinition=" + xDefName);
            return;
        }

        XsdLogger.print(LOG_INFO, PREPROCESSING, XD_ADAPTER_CTX, "Add x-definition target namespace. XDefinition=" + xDefName + ", TargetNamespace=" + targetNamespace);
        targetNamespaces.put(xDefName, targetNamespace);

        if (xDefTargetNamespaces.containsKey(targetNamespace.getValue())) {
            XsdLogger.print(LOG_WARN, PREPROCESSING, XD_ADAPTER_CTX, "X-definition using given namespace URI already exists. NamespaceURI=" + targetNamespace.getValue());
            return;
        }

        xDefTargetNamespaces.put(targetNamespace.getValue(), xDefName);
    }

    public Pair<String, String> getTargetNamespace(final String xDefName) {
        return targetNamespaces.get(xDefName);
    }

    public void addNamespace(final String xDefName, final String nsPrefix, final String nsUri) {
        Map<String, String> namespaces = xDefNamespaces.get(xDefName);
        if (namespaces == null) {
            namespaces = new HashMap<String, String>();
            xDefNamespaces.put(xDefName, namespaces);
        }

        if (xDefNamespaces.containsKey(nsUri)) {
            XsdLogger.print(LOG_WARN, PREPROCESSING, XD_ADAPTER_CTX, "X-definition namespace already exists. XDefinition=" + xDefName + ", NsPrefix=" + nsPrefix);
            return;
        }

        XsdLogger.print(LOG_INFO, PREPROCESSING, XD_ADAPTER_CTX, "Add x-definition namespace. XDefinition=" + xDefName + ", NsPrefix=" + nsPrefix + ", NsUri=" + nsUri);
        namespaces.put(nsUri, nsPrefix);
    }

    public Map<String, String> getNamespaces(final String xDefName) {
        return xDefNamespaces.get(xDefName);
    }

    public String getNamespacePrefix(final String xDefName, final String nsUri) {
        final Map<String, String> namespaces = xDefNamespaces.get(xDefName);
        if (namespaces == null) {
            return null;
        }

        return namespaces.get(nsUri);
    }

    public String getXDefByNamespace(final String nsUri) {
        return xDefTargetNamespaces.get(nsUri);
    }

    /**
     * Check if algorithm feature is enabled
     * @param feature       algorithm feature
     * @return  true if algorithm feature is enabled
     */
    public boolean hasEnableFeature(final Xsd2XdFeature feature) {
        return features.contains(feature);
    }
}
