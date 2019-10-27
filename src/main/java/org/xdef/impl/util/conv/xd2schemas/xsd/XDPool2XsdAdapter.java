package org.xdef.impl.util.conv.xd2schemas.xsd;

import javafx.util.Pair;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.xdef.XDPool;
import org.xdef.impl.XDefinition;
import org.xdef.impl.util.conv.xd2schemas.XDPool2SchemaAdapter;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.XmlSchemaImportLocation;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.XsdAdapterCtx;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XD2XsdUtils;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdNamespaceUtils;
import org.xdef.model.XMDefinition;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.*;

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
        if (XsdLogger.isDebug(logLevel)) {
            XsdLogger.printC(DEBUG, XSD_DPOOL_ADAPTER, "Creating schemas ...");
        }

        if (xdPool == null) {
            throw new IllegalArgumentException("xdPool = null");
        }

        this.xdPool = xdPool;
        adapterCtx = new XsdAdapterCtx(logLevel);
        adapterCtx.init();

        initTargetNamespaces();
        initNamespaceCtx();
        initImportLocations();

        for (XMDefinition xDef : xdPool.getXMDefinitions()) {
            XDef2XsdAdapter adapter = createXDefAdapter();
            adapter.createSchema(xDef);
        }

        return adapterCtx.getXmlSchemaCollection();
    }

    /**
     * Load all namespaces from XPool x-definitions
     * Store them into namespaceCtx per x-definition
     */
    private void initNamespaceCtx() {
        if (XsdLogger.isDebug(logLevel)) {
            XsdLogger.printP(DEBUG, PREPROCESSING, "Initialize namespace context ...");
        }

        for (XMDefinition xDef : xdPool.getXMDefinitions()) {
            Pair<String, String> targetNamespace = xDefTargetNs.get(xDef.getName());
            NamespaceMap namespaceMap = XsdNamespaceUtils.createCtx();
            if (targetNamespace == null) {
                XsdNamespaceUtils.initCtx(namespaceMap, (XDefinition)xDef, null, null, PREPROCESSING, logLevel);
            } else {
                XsdNamespaceUtils.initCtx(namespaceMap, (XDefinition)xDef, targetNamespace.getKey(), targetNamespace.getValue(), PREPROCESSING, logLevel);
            }

            adapterCtx.addNamespaceCtx(xDef.getName(), namespaceMap);
        }
    }

    /**
     * Find target namespaces for all x-definitions from XPool
     *
     * Has to be called before {@link #initNamespaceCtx}
     */
    private void initTargetNamespaces() {
        if (XsdLogger.isDebug(logLevel)) {
            XsdLogger.printP(DEBUG, PREPROCESSING, "Initialize target namespaces ...");
        }

        xDefsWithoutNs = new HashSet<String>();
        xDefTargetNs = new HashMap<String, Pair<String, String>>();

        for (XMDefinition xDef : xdPool.getXMDefinitions()) {
            final String xDefName = xDef.getName();
            Boolean targetNamespaceError = false;

            Pair<String, String> targetNamespace = XsdNamespaceUtils.getSchemaTargetNamespace((XDefinition)xDef, targetNamespaceError, logLevel);
            if (targetNamespace.getKey() != null && targetNamespace.getValue() != null) {
                if (xDefTargetNs.containsKey(xDefName)) {
                    if (XsdLogger.isWarn(logLevel)) {
                        XsdLogger.printP(WARN, PREPROCESSING, "Target namespace of x-definition is already defined. XDefinition=" + xDefName);
                    }
                } else {
                    xDefTargetNs.put(xDefName, new Pair(targetNamespace.getKey(), targetNamespace.getValue()));
                    if (XsdLogger.isInfo(logLevel)) {
                        XsdLogger.printP(INFO, PREPROCESSING, "Add target namespace to x-definition. " +
                                "XDefinitionName=" + xDefName + ", naPrefix=" + targetNamespace.getKey() + ", nsUri=" + targetNamespace.getValue());
                    }
                }
            } else {
                xDefsWithoutNs.add(xDefName);
                if (XsdLogger.isInfo(logLevel)) {
                    XsdLogger.printP(INFO, PREPROCESSING, "X-definition has no target namespace. XDefinitionName=" + xDefName);
                }
            }
        }
    }

    /**
     * Creates schema location context based on x-definition target namespace and x-definition name
     */
    private void initImportLocations() {
        if (XsdLogger.isDebug(logLevel)) {
            XsdLogger.printP(DEBUG, PREPROCESSING, "Initialize imports ...");
        }

        for (Map.Entry<String, Pair<String, String>> entry : xDefTargetNs.entrySet()) {
            final String nsUri = entry.getValue().getValue();
            final String xDefName = entry.getKey();
            adapterCtx.addSchemaLocation(nsUri, new XmlSchemaImportLocation(nsUri, xDefName));
        }

        for (String xDefName: xDefsWithoutNs) {
            final String nsUri = XD2XsdUtils.createNsUriFromXDefName(xDefName);
            if (XsdLogger.isDebug(logLevel)) {
                XsdLogger.printP(DEBUG, PREPROCESSING, "Creating nsUri from x-definition name. XDefinitionName=" + xDefName + ", NamespaceURI=" + nsUri);
            }
            adapterCtx.addSchemaLocation(nsUri, new XmlSchemaImportLocation(nsUri, xDefName));
        }
    }

    /**
     * Creates and initialize adapter for transformation of single x-definition
     * @return Transformation adapter
     */
    private XDef2XsdAdapter createXDefAdapter() {
        XDef2XsdAdapter adapter = new XDef2XsdAdapter();
        adapter.setLogLevel(logLevel);
        adapter.setAdapterCtx(adapterCtx);
        return adapter;
    }

}
