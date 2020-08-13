package org.xdef.impl.util.conv.schema.xd2schema.xsd;

import javafx.util.Pair;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.xdef.XDPool;
import org.xdef.impl.XDefinition;
import org.xdef.impl.util.conv.schema.util.SchemaLogger;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.adapter.AbstractXd2XsdAdapter;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.adapter.Xd2XsdPostProcessingAdapter;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.factory.XsdSchemaFactory;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.model.XsdAdapterCtx;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.model.XsdSchemaImportLocation;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.util.XsdNamespaceUtils;
import org.xdef.model.XMDefinition;
import org.xdef.msg.XDEF;
import org.xdef.msg.XSD;
import org.xdef.sys.SRuntimeException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.xdef.impl.util.conv.schema.util.SchemaLoggerDefs.*;
import static org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.AlgPhase.INITIALIZATION;
import static org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.AlgPhase.PREPROCESSING;
import static org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.Xd2XsdDefinitions.XSD_NAMESPACE_URI_EMPTY;
import static org.xdef.impl.util.conv.schema.xd2schema.xsd.util.Xd2XsdLoggerDefs.XSD_DPOOL_ADAPTER;

/**
 * Transformation of given x-definition pool to collection of XSD documents
 */
public class XdPool2XsdAdapter extends AbstractXd2XsdAdapter implements XdPool2SchemaAdapter<XmlSchemaCollection> {

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
            throw new SRuntimeException(XDEF.XDEF715);
        }

        this.xdPool = xdPool;
        adapterCtx = new XsdAdapterCtx(features, reportWriter);
        adapterCtx.init();

        init();

        for (XMDefinition xDef : xdPool.getXMDefinitions()) {
            final XDef2XsdAdapter adapter = createXDefAdapter();
            adapter.createSchema(xDef);
        }

        final Xd2XsdPostProcessingAdapter postProcessingAdapter = new Xd2XsdPostProcessingAdapter();
        postProcessingAdapter.setAdapterCtx(adapterCtx);
        postProcessingAdapter.process(xdPool);

        return adapterCtx.getXmlSchemaCollection();
    }

    /**
     * Initializes transformation algorithm
     */
    private void init() {
        SchemaLogger.print(LOG_INFO, PREPROCESSING, XSD_DPOOL_ADAPTER, "*** Initialize ***");

        initTargetNamespaces();
        initXsdSchemas();
        initSchemaLocations();
    }

    /**
     * Gathers target namespaces for all x-definitions from source x-definition pool
     */
    private void initTargetNamespaces() {
        SchemaLogger.print(LOG_INFO, PREPROCESSING, XSD_DPOOL_ADAPTER, "Initialize target namespaces ...");

        xDefsWithoutNs = new HashSet<String>();
        xDefTargetNs = new HashMap<String, Pair<String, String>>();

        for (XMDefinition xDef : xdPool.getXMDefinitions()) {
            final String xDefName = xDef.getName();
            Pair<String, String> targetNamespace = XsdNamespaceUtils.getSchemaTargetNamespace((XDefinition)xDef, adapterCtx);
            if (targetNamespace.getKey() != null && targetNamespace.getValue() != null) {
                if (xDefTargetNs.containsKey(xDefName)) {
                    reportWriter.warning(XSD.XSD014, xDefName);
                    SchemaLogger.print(LOG_WARN, PREPROCESSING, XSD_DPOOL_ADAPTER,"Target namespace of x-definition is already defined. XDefinition=" + xDefName);
                } else {
                    xDefTargetNs.put(xDefName, new Pair(targetNamespace.getKey(), targetNamespace.getValue()));
                    SchemaLogger.print(LOG_INFO, PREPROCESSING, XSD_DPOOL_ADAPTER,"Add target namespace to x-definition. " +
                            "XDefinition=" + xDefName + ", naPrefix=" + targetNamespace.getKey() + ", nsUri=" + targetNamespace.getValue());
                }
            } else {
                xDefsWithoutNs.add(xDefName);
                SchemaLogger.print(LOG_INFO, PREPROCESSING, XSD_DPOOL_ADAPTER,"X-definition has no target namespace. XDefinition=" + xDefName);
            }
        }
    }

    /**
     * Initializes all XSD documents based on source x-definition from x-definition pool
     */
    private void initXsdSchemas() {
        SchemaLogger.print(LOG_INFO, INITIALIZATION, XSD_DPOOL_ADAPTER,"Initialize XSD documents ...");

        XsdSchemaFactory schemaFactory = new XsdSchemaFactory(adapterCtx);
        for (XMDefinition xDef : xdPool.getXMDefinitions()) {
            schemaFactory.createXsdSchema((XDefinition)xDef, xDefTargetNs.get(xDef.getName()));
        }
    }

    /**
     * Creates schema location context based on x-definition target namespace and x-definition name
     */
    private void initSchemaLocations() {
        SchemaLogger.print(LOG_INFO, PREPROCESSING, XSD_DPOOL_ADAPTER,"Initialize schema locations ...");

        for (Map.Entry<String, Pair<String, String>> entry : xDefTargetNs.entrySet()) {
            final String nsUri = entry.getValue().getValue();
            final String xDefName = entry.getKey();
            adapterCtx.addSchemaLocation(nsUri, new XsdSchemaImportLocation(nsUri, xDefName));
        }

        for (String xDefName : xDefsWithoutNs) {
            final String nsUri = XSD_NAMESPACE_URI_EMPTY;
            adapterCtx.addSchemaLocation(nsUri, new XsdSchemaImportLocation(nsUri, xDefName));
            SchemaLogger.print(LOG_DEBUG, PREPROCESSING, XSD_DPOOL_ADAPTER,"Creating nsUri from x-definition name. XDefinition=" + xDefName + ", NamespaceURI=" + nsUri);
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
