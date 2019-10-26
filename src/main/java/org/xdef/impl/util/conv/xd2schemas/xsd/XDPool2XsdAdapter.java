package org.xdef.impl.util.conv.xd2schemas.xsd;

import javafx.util.Pair;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.xdef.XDPool;
import org.xdef.impl.XDefinition;
import org.xdef.impl.util.conv.xd2schemas.XDPool2SchemaAdapter;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.XmlSchemaImportLocation;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XD2XsdUtils;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;
import org.xdef.model.XMDefinition;

import java.util.*;

import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.*;

public class XDPool2XsdAdapter implements XDPool2SchemaAdapter<XmlSchemaCollection> {

    private int logLevel = LOG_LEVEL_NONE;

    private Set<String> schemaNames = new HashSet<String>();
    private XDPool xdPool = null;

    /**
     * Key:     x-definition name
     * Value:   namespace prefix, namespace URI
     */
    private Map<String, Pair<String, String>> xDefTargetNs = new HashMap<String, Pair<String, String>>();

    /**
     * Key:     namespace URI
     * Value:   location
     */
    private Map<String, XmlSchemaImportLocation> importSchemaLocations = new HashMap<String, XmlSchemaImportLocation>();

    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    public final Set<String> getSchemaNames() {
        return schemaNames;
    }

    @Override
    public XmlSchemaCollection createSchemas(XDPool xdPool) {
        if (XsdLogger.isDebug(logLevel)) {
            XsdLogger.printC(DEBUG, CAT_XD_POOL, "Creating schemas ...");
        }

        if (xdPool == null) {
            throw new IllegalArgumentException("xdPool = null");
        }

        this.xdPool = xdPool;
        schemaNames.clear();

        XMDefinition xmDefinitions[] = xdPool.getXMDefinitions();
        XmlSchemaCollection xmlSchemaCollection = new XmlSchemaCollection();

        initNamespaceContext(xmDefinitions);

        for (int i = 0; i < xmDefinitions.length; i++) {
            Pair<String, XmlSchema> schemaPair = createSchema(xmlSchemaCollection, createAdapter(), xmDefinitions[i], i);
            if (!schemaNames.add(schemaPair.getKey())) {
                throw new RuntimeException("XSD schema with name " + schemaPair.getKey() + " already exists!");
            }
        }

        return xmlSchemaCollection;
    }

    private Pair<String, XmlSchema> createSchema(final XmlSchemaCollection xmlSchemaCollection, final XDef2XsdAdapter adapter, final XMDefinition xmDefinition, Integer index) {
        initAdapter(adapter);
        return adapter.createSchema(xmDefinition, xmlSchemaCollection);
    }

    private void initNamespaceContext(final XMDefinition xmDefinitions[]) {
        if (XsdLogger.isDebug(logLevel)) {
            XsdLogger.printP(DEBUG, PREPROCESSING, "Initialize namespace context ...");
        }

        xDefTargetNs.clear();
        Set<String> xDefsWithoutNs = new HashSet<String>();

        for (int i = 0; i < xmDefinitions.length; i++) {
            XDefinition xDef = ((XDefinition) xmDefinitions[i]);
            final String xDefName = xDef.getName();
            Boolean targetNamespaceError = false;

            Pair<String, String> targetNamespace = XD2XsdUtils.getSchemaTargetNamespace(xDef, targetNamespaceError);
            if (targetNamespace.getKey() != null && targetNamespace.getValue() != null) {
                addTargetNamespace(xDefName, targetNamespace.getKey(), targetNamespace.getValue());
                if (XsdLogger.isInfo(logLevel)) {
                    XsdLogger.printP(INFO, PREPROCESSING, "Add target namespace to x-definition. " +
                            "XDefinitionName=" + xDefName + ", naPrefix="  + targetNamespace.getKey() + ", nsUri=" + targetNamespace.getValue());
                }
            } else {
                xDefsWithoutNs.add(xDefName);
                if (XsdLogger.isInfo(logLevel)) {
                    XsdLogger.printP(INFO, PREPROCESSING, "X-definition has no target namespace. " +
                            "XDefinitionName=" + xDefName + ", naPrefix="  + targetNamespace.getKey() + ", nsUri=" + targetNamespace.getValue());
                }
            }
        }

        initImportLocations(xDefsWithoutNs);
    }

    private void initImportLocations(final Set<String> xDefsWithoutNs) {
        if (XsdLogger.isDebug(logLevel)) {
            XsdLogger.printP(DEBUG, PREPROCESSING, "Initialize imports ...");
        }

        importSchemaLocations.clear();

        for (Map.Entry<String, Pair<String, String>> entry : xDefTargetNs.entrySet()) {
            final String nsUri = entry.getValue().getValue();
            if (importSchemaLocations.containsKey(nsUri)) {
                if (XsdLogger.isWarn(logLevel)) {
                    XsdLogger.printP(INFO, PREPROCESSING, "Schema import already exists for namespace URI. NamespaceURI=" + nsUri);
                }
            } else {
                importSchemaLocations.put(nsUri, new XmlSchemaImportLocation(nsUri, entry.getKey()));
                if (XsdLogger.isInfo(logLevel)) {
                    XsdLogger.printP(INFO, PREPROCESSING, "Add schema import. NamespaceURI=" + nsUri + ", XDefinitionName=" + entry.getKey());
                }
            }
        }

        for (String xDefName: xDefsWithoutNs) {
            final String nsUri = XD2XsdUtils.createNsUriFromXDefName(xDefName);
            if (XsdLogger.isDebug(logLevel)) {
                XsdLogger.printP(DEBUG, PREPROCESSING, "Creating nsUri from x-definition name. XDefinitionName=" + xDefName + ", NamespaceURI=" + nsUri);
            }
            if (importSchemaLocations.containsKey(nsUri)) {
                if (XsdLogger.isWarn(logLevel)) {
                    XsdLogger.printP(WARN, PREPROCESSING, "Schema import already exists for namespace URI. NamespaceURI=" + nsUri);
                }
            } else {
                importSchemaLocations.put(nsUri, new XmlSchemaImportLocation(nsUri, xDefName));
                if (XsdLogger.isInfo(logLevel)) {
                    XsdLogger.printP(INFO, PREPROCESSING, "Add schema import. NamespaceURI=" + nsUri + ", XDefinitionName=" + xDefName);
                }
            }
        }
    }

    private XDef2XsdAdapter createAdapter() {
        XDef2XsdAdapter adapter = new XDef2XsdAdapter();
        adapter.setLogLevel(logLevel);
        return adapter;
    }

    private void initAdapter(final XDef2XsdAdapter adapter) {
        adapter.setSchemaNamespaceLocations(importSchemaLocations);
    }

    private void addTargetNamespace(final String xDefName, final String nsPrefix, final String nsUri) {
        if (xDefTargetNs.containsKey(xDefName)) {
            if (XsdLogger.isWarn(logLevel)) {
                XsdLogger.printP(WARN, PREPROCESSING, "Target namespace of x-definition is already defined. XDefinition=" + xDefName);
            }

            return;
        }

        xDefTargetNs.put(xDefName, new Pair(nsPrefix, nsUri));
    }

}
