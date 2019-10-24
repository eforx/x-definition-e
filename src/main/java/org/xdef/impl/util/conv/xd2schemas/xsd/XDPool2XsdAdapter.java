package org.xdef.impl.util.conv.xd2schemas.xsd;

import javafx.util.Pair;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.xdef.XDPool;
import org.xdef.impl.XDefinition;
import org.xdef.impl.util.conv.xd2schemas.XDPool2SchemaAdapter;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.XmlSchemaImportLocation;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XD2XsdUtils;
import org.xdef.model.XMDefinition;

import java.util.*;

public class XDPool2XsdAdapter implements XDPool2SchemaAdapter<XmlSchemaCollection> {

    private boolean verbose = false;
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

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public final Set<String> getSchemaNames() {
        return schemaNames;
    }

    public final Map<String, Pair<String, String>> getxDefTargetNs() {
        return xDefTargetNs;
    }

    @Override
    public XmlSchemaCollection createSchemas(XDPool xdPool, String[] xdefNames) {
        if (xdPool == null) {
            throw new IllegalArgumentException("xdPool = null");
        }

        this.xdPool = xdPool;
        schemaNames.clear();

        XMDefinition xmDefinitions[] = xdPool.getXMDefinitions();
        List<String> xdefNamesFilterList = Arrays.asList(xdefNames);
        XmlSchemaCollection xmlSchemaCollection = new XmlSchemaCollection();

        initNamespaceContext(xmDefinitions);

        int j = 0;
        for (int i = 0; i < xmDefinitions.length; i++) {
            if (xmDefinitions[i].getName().equals(xdefNamesFilterList.contains(i))) {
                Pair<String, XmlSchema> schemaPair = createSchema(xmlSchemaCollection, createAdapter(), xmDefinitions[i], j++);
                if (!schemaNames.add(schemaPair.getKey())) {
                    throw new RuntimeException("XSD schema with name " + schemaPair.getKey() + " already exists!");
                }
            }
        }

        return xmlSchemaCollection;
    }

    @Override
    public XmlSchemaCollection createSchemas(XDPool xdPool, Integer[] xdefIndexes) {
        if (xdPool == null) {
            throw new IllegalArgumentException("xdPool = null");
        }

        this.xdPool = xdPool;
        schemaNames.clear();

        XMDefinition xmDefinitions[] = xdPool.getXMDefinitions();
        List<Integer> xdefIndexesFilterList = Arrays.asList(xdefIndexes);
        XmlSchemaCollection xmlSchemaCollection = new XmlSchemaCollection();

        initNamespaceContext(xmDefinitions);

        int j = 0;
        for (int i = 0; i < xmDefinitions.length; i++) {
            if (xdefIndexesFilterList.contains(i)) {
                Pair<String, XmlSchema> schemaPair = createSchema(xmlSchemaCollection, createAdapter(), xmDefinitions[i], j++);
                if (!schemaNames.add(schemaPair.getKey())) {
                    throw new RuntimeException("XSD schema with name " + schemaPair.getKey() + " already exists!");
                }
            }
        }

        return xmlSchemaCollection;
    }

    @Override
    public XmlSchemaCollection createSchemas(XDPool xdPool) {
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
        xDefTargetNs.clear();
        Set<String> xDefsWithoutNs = new HashSet<String>();

        for (int i = 0; i < xmDefinitions.length; i++) {
            XDefinition xDef = ((XDefinition) xmDefinitions[i]);
            final String xDefName = xDef.getName();
            Boolean targetNamespaceError = false;

            Pair<String, String> targetNamespace = XD2XsdUtils.getSchemaTargetNamespace(xDef, targetNamespaceError);
            if (targetNamespace.getKey() != null && targetNamespace.getValue() != null) {
                addTargetNamespace(xDefName, targetNamespace.getKey(), targetNamespace.getValue());
            } else {
                xDefsWithoutNs.add(xDefName);
            }

        }

        importSchemaLocations.clear();

        for (Map.Entry<String, Pair<String, String>> entry : xDefTargetNs.entrySet()) {
            final String nsUri = entry.getValue().getValue();
            if (importSchemaLocations.containsKey(nsUri)) {
                System.out.println("Schema import for namespace URI " + nsUri + " already exists!");
            } else {
                importSchemaLocations.put(nsUri, new XmlSchemaImportLocation(nsUri, entry.getKey()));
            }
        }

        for (String xDefName: xDefsWithoutNs) {
            final String nsUri = XD2XsdUtils.createNsUriFromXDefName(xDefName);
            if (importSchemaLocations.containsKey(nsUri)) {
                System.out.println("Schema import for namespace URI " + nsUri + " already exists!");
            } else {
                importSchemaLocations.put(nsUri, new XmlSchemaImportLocation(nsUri, xDefName));
            }
        }
    }

    private XDef2XsdAdapter createAdapter() {
        XDef2XsdAdapter adapter = new XDef2XsdAdapter();
        adapter.setVerbose(verbose);
        return adapter;
    }

    private void initAdapter(final XDef2XsdAdapter adapter) {
        adapter.setSchemaNamespaceLocations(importSchemaLocations);
    }

    private void addTargetNamespace(final String xDefName, final String nsPrefix, final String nsUri) {
        if (xDefTargetNs.containsKey(xDefName)) {
            System.out.println("Target namespace of x-definition \"" + xDefName + "\" is already defined!");
            return;
        }

        xDefTargetNs.put(xDefName, new Pair(nsPrefix, nsUri));
    }

}
