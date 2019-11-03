package org.xdef.impl.util.conv.xd2schemas.xsd;

import javafx.util.Pair;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.xdef.XDPool;
import org.xdef.impl.XDefinition;
import org.xdef.impl.util.conv.xd2schemas.XDPool2SchemaAdapter;
import org.xdef.impl.util.conv.xd2schemas.xsd.adapter.AbstractXd2XsdAdapter;
import org.xdef.impl.util.conv.xd2schemas.xsd.adapter.XD2XsdPostProcessingAdapter;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.XsdSchemaFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.XsdSchemaImportLocation;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.XsdAdapterCtx;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdNamespaceUtils;
import org.xdef.model.XMDefinition;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.AlgPhase.INITIALIZATION;
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.AlgPhase.PREPROCESSING;
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XsdLoggerDefs.*;

/**
 * Tra
 */
public class XDPool2XsdAdapter extends AbstractXd2XsdAdapter implements XDPool2SchemaAdapter<XmlSchemaCollection> {

    /**
     * Input XDPool used for transformation
     */
    private XDPool xdPool = null;

    /**
     * Target namespace per x-definition
     * Key:     x-definition name
     * Value:   namespace prefix, namespace URI
     */
    private Map<String, Pair<String, String>> xDefTargetNs = null;

    /**
     * X-definition without target namespace
     */
    private Set<String> xDefsWithoutNs = null;

    @Override
    public XmlSchemaCollection createSchemas(XDPool xdPool) {
        if (xdPool == null) {
            throw new IllegalArgumentException("xdPool = null");
        }

        this.xdPool = xdPool;
        adapterCtx = new XsdAdapterCtx();
        adapterCtx.init();

        init();

        for (XMDefinition xDef : xdPool.getXMDefinitions()) {
            XDef2XsdAdapter adapter = createXDefAdapter();
            adapter.createSchema(xDef);
        }

        XD2XsdPostProcessingAdapter postProcessingAdapter = new XD2XsdPostProcessingAdapter();
        postProcessingAdapter.setAdapterCtx(adapterCtx);
        postProcessingAdapter.process(xdPool);

        return adapterCtx.getXmlSchemaCollection();
    }

    private void init() {
        XsdLogger.print(LOG_INFO, PREPROCESSING, XSD_DPOOL_ADAPTER, "*** Initialize ***");

        initTargetNamespaces();
        initXsdSchemas();
        initSchemaLocations();
    }

    /**
     * Find target namespaces for all x-definitions from XPool
     */
    private void initTargetNamespaces() {
        XsdLogger.print(LOG_INFO, PREPROCESSING, XSD_DPOOL_ADAPTER, "Initialize target namespaces ...");

        xDefsWithoutNs = new HashSet<String>();
        xDefTargetNs = new HashMap<String, Pair<String, String>>();

        for (XMDefinition xDef : xdPool.getXMDefinitions()) {
            final String xDefName = xDef.getName();
            Boolean targetNamespaceError = false;

            Pair<String, String> targetNamespace = XsdNamespaceUtils.getSchemaTargetNamespace((XDefinition)xDef, targetNamespaceError);
            if (targetNamespace.getKey() != null && targetNamespace.getValue() != null) {
                if (xDefTargetNs.containsKey(xDefName)) {
                    XsdLogger.print(LOG_WARN, PREPROCESSING, XSD_DPOOL_ADAPTER,"Target namespace of x-definition is already defined. XDefinition=" + xDefName);
                } else {
                    xDefTargetNs.put(xDefName, new Pair(targetNamespace.getKey(), targetNamespace.getValue()));
                    XsdLogger.print(LOG_INFO, PREPROCESSING, XSD_DPOOL_ADAPTER,"Add target namespace to x-definition. " +
                            "XDefinition=" + xDefName + ", naPrefix=" + targetNamespace.getKey() + ", nsUri=" + targetNamespace.getValue());
                }
            } else {
                xDefsWithoutNs.add(xDefName);
                XsdLogger.print(LOG_INFO, PREPROCESSING, XSD_DPOOL_ADAPTER,"X-definition has no target namespace. XDefinition=" + xDefName);
            }
        }
    }

    private void initXsdSchemas() {
        XsdLogger.print(LOG_INFO, INITIALIZATION, XSD_DPOOL_ADAPTER,"Initialize xsd schemas ...");

        XsdSchemaFactory schemaFactory = new XsdSchemaFactory(adapterCtx);
        for (XMDefinition xDef : xdPool.getXMDefinitions()) {
            schemaFactory.createXsdSchema((XDefinition)xDef, xDefTargetNs.get(xDef.getName()));
        }
    }

    /**
     * Creates schema location context based on x-definition target namespace and x-definition name
     */
    private void initSchemaLocations() {
        XsdLogger.print(LOG_INFO, PREPROCESSING, XSD_DPOOL_ADAPTER,"Initialize schema locations ...");

        for (Map.Entry<String, Pair<String, String>> entry : xDefTargetNs.entrySet()) {
            final String nsUri = entry.getValue().getValue();
            final String xDefName = entry.getKey();
            adapterCtx.addSchemaLocation(nsUri, new XsdSchemaImportLocation(nsUri, xDefName));
        }

        for (String xDefName: xDefsWithoutNs) {
            final String nsUri = XsdNamespaceUtils.createNsUriFromXDefName(xDefName);
            adapterCtx.addSchemaLocation(nsUri, new XsdSchemaImportLocation(nsUri, xDefName));
            XsdLogger.print(LOG_DEBUG, PREPROCESSING, XSD_DPOOL_ADAPTER,"Creating nsUri from x-definition name. XDefinition=" + xDefName + ", NamespaceURI=" + nsUri);
        }
    }

    /**
     * Creates and initialize adapter for transformation of single x-definition
     * @return Transformation adapter
     */
    private XDef2XsdAdapter createXDefAdapter() {
        XDef2XsdAdapter adapter = new XDef2XsdAdapter();
        adapter.setAdapterCtx(adapterCtx);
        return adapter;
    }

}
