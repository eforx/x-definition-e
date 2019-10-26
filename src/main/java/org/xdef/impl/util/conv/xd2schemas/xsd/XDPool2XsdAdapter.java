package org.xdef.impl.util.conv.xd2schemas.xsd;

import javafx.util.Pair;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.xdef.XDPool;
import org.xdef.impl.XDefinition;
import org.xdef.impl.util.conv.xd2schemas.XDPool2SchemaAdapter;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.XmlSchemaImportLocation;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XD2XsdUtils;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdNamespaceUtils;
import org.xdef.model.XMDefinition;

import java.util.*;

import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.*;

public class XDPool2XsdAdapter implements XDPool2SchemaAdapter<XmlSchemaCollection> {

    private int logLevel = LOG_LEVEL_NONE;

    /**
     * Names of created xsd schemas
     */
    private Set<String> schemaNames = new HashSet<String>();
    private XDPool xdPool = null;

    /**
     * Namespace context per x-definition
     * Key:     x-definition name
     * Value:   namespace context
     */
    private Map<String, NamespaceMap> namespaceCtx = null;

    /**
     * Target namespace per x-definition
     * Key:     x-definition name
     * Value:   namespace prefix, namespace URI
     */
    private Map<String, Pair<String, String>> xDefTargetNs = null;

    /**
     * Schemas created based on x-definition
     * Key:     namespace URI
     * Value:   location
     */
    private Map<String, XmlSchemaImportLocation> schemaLocations = null;

    /**
     * Extra schemas which are created based on namespaces within x-definition
     * Key:     schema namespace URI
     * Value:   schema location
     */
    private Map<String, XmlSchemaImportLocation> extraSchemaLocations = new HashMap<String, XmlSchemaImportLocation>();

    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    public final Set<String> getSchemaNames() {
        return schemaNames;
    }

    public final Map<String, NamespaceMap> getNamespaceCtx() {
        return namespaceCtx;
    }

    public final Map<String, XmlSchemaImportLocation> getSchemaLocations() {
        return schemaLocations;
    }

    public final Map<String, XmlSchemaImportLocation> getExtraSchemaLocations() {
        return extraSchemaLocations;
    }

    @Override
    public XmlSchemaCollection createSchemas(XDPool xdPool) {
        if (XsdLogger.isDebug(logLevel)) {
            XsdLogger.printC(DEBUG, XSD_DPOOL_ADAPTER, "Creating schemas ...");
        }

        if (xdPool == null) {
            throw new IllegalArgumentException("xdPool = null");
        }

        this.xdPool = xdPool;
        schemaNames.clear();

        XMDefinition xmDefinitions[] = xdPool.getXMDefinitions();
        XmlSchemaCollection xmlSchemaCollection = new XmlSchemaCollection();

        Set<String> xDefsWithoutNs = initTargetNamespaces(xmDefinitions);
        initNamespaceCtx(xmDefinitions);
        initImportLocations(xDefsWithoutNs);

        for (int i = 0; i < xmDefinitions.length; i++) {
            XDef2XsdAdapter adapter = createAdapter();
            adapter.createSchema(xmDefinitions[i], xmlSchemaCollection);
            for (String schemaName : adapter.getSchemaNames()) {
                if (!schemaNames.add(schemaName)) {
                    throw new RuntimeException("XSD schema with name " + schemaName + " already exists!");
                }
            }
        }

        return xmlSchemaCollection;
    }

    private void initNamespaceCtx(final XMDefinition xmDefinitions[]) {
        if (XsdLogger.isDebug(logLevel)) {
            XsdLogger.printP(DEBUG, PREPROCESSING, "Initialize namespace context ...");
        }

        namespaceCtx = new HashMap<String, NamespaceMap>();

        for (int i = 0; i < xmDefinitions.length; i++) {
            XDefinition xDef = ((XDefinition) xmDefinitions[i]);

            Pair<String, String> targetNamespace = xDefTargetNs.get(xDef.getName());
            NamespaceMap namespaceMap = XsdNamespaceUtils.createCtx();
            if (targetNamespace == null) {
                XsdNamespaceUtils.initCtx(namespaceMap, xDef, null, null, PREPROCESSING, logLevel);
            } else {
                XsdNamespaceUtils.initCtx(namespaceMap, xDef, targetNamespace.getKey(), targetNamespace.getValue(), PREPROCESSING, logLevel);
            }

            namespaceCtx.put(xDef.getName(), namespaceMap);
        }
    }

    private Set<String> initTargetNamespaces(final XMDefinition xmDefinitions[]) {
        if (XsdLogger.isDebug(logLevel)) {
            XsdLogger.printP(DEBUG, PREPROCESSING, "Initialize target namespaces ...");
        }

        Set<String> xDefsWithoutNs = new HashSet<String>();
        xDefTargetNs = new HashMap<String, Pair<String, String>>();

        for (int i = 0; i < xmDefinitions.length; i++) {
            XDefinition xDef = ((XDefinition) xmDefinitions[i]);
            final String xDefName = xDef.getName();
            Boolean targetNamespaceError = false;

            Pair<String, String> targetNamespace = XsdNamespaceUtils.getSchemaTargetNamespace(xDef, targetNamespaceError);
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
                    XsdLogger.printP(INFO, PREPROCESSING, "X-definition has no target namespace. " +
                            "XDefinitionName=" + xDefName + ", naPrefix="  + targetNamespace.getKey() + ", nsUri=" + targetNamespace.getValue());
                }
            }
        }

        return xDefsWithoutNs;
    }

    private void initImportLocations(final Set<String> xDefsWithoutNs) {
        if (XsdLogger.isDebug(logLevel)) {
            XsdLogger.printP(DEBUG, PREPROCESSING, "Initialize imports ...");
        }

        schemaLocations = new HashMap<String, XmlSchemaImportLocation>();

        for (Map.Entry<String, Pair<String, String>> entry : xDefTargetNs.entrySet()) {
            final String nsUri = entry.getValue().getValue();
            if (schemaLocations.containsKey(nsUri)) {
                if (XsdLogger.isWarn(logLevel)) {
                    XsdLogger.printP(INFO, PREPROCESSING, "Schema import already exists for namespace URI. NamespaceURI=" + nsUri);
                }
            } else {
                schemaLocations.put(nsUri, new XmlSchemaImportLocation(nsUri, entry.getKey()));
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
            if (schemaLocations.containsKey(nsUri)) {
                if (XsdLogger.isWarn(logLevel)) {
                    XsdLogger.printP(WARN, PREPROCESSING, "Schema import already exists for namespace URI. NamespaceURI=" + nsUri);
                }
            } else {
                schemaLocations.put(nsUri, new XmlSchemaImportLocation(nsUri, xDefName));
                if (XsdLogger.isInfo(logLevel)) {
                    XsdLogger.printP(INFO, PREPROCESSING, "Add schema import. NamespaceURI=" + nsUri + ", XDefinitionName=" + xDefName);
                }
            }
        }
    }

    private XDef2XsdAdapter createAdapter() {
        XDef2XsdAdapter adapter = new XDef2XsdAdapter();
        adapter.setLogLevel(logLevel);
        adapter.setPoolAdapter(this);
        return adapter;
    }

}
