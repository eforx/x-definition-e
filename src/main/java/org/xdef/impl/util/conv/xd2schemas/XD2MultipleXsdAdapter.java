package org.xdef.impl.util.conv.xd2schemas;

import javafx.util.Pair;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.apache.ws.commons.schema.constants.Constants;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.xdef.XDConstants;
import org.xdef.XDPool;
import org.xdef.impl.XDefinition;
import org.xdef.model.XMDefinition;

import java.util.*;

public class XD2MultipleXsdAdapter implements XD2MultipleSchemasAdapter<XmlSchemaCollection> {

    private boolean printXdTree = false;
    private Set<String> schemaNames = new HashSet<String>();

    /**
     * ================ Input parameters ================
     */

    /**
     * Key:     schema namespace URI
     * Value:   schema location
     */
    private Map<String, XmlSchemaImportLocation> importSchemaLocations = new HashMap<String, XmlSchemaImportLocation>();
    private Map<Object, Map<String, String>> schemaNamespaces = new HashMap<Object, Map<String, String>>();
    private Map<Object, XmlSchemaForm> elemSchemaForm = new HashMap<Object, XmlSchemaForm>();
    private Map<Object, XmlSchemaForm> attrSchemaForm = new HashMap<Object, XmlSchemaForm>();
    private Map<Object, String> targetNamespace = new HashMap<Object, String>();

    public void setPrintXdTree(boolean printXdTree) {
        this.printXdTree = printXdTree;
    }

    public final Set<String> getSchemaNames() {
        return schemaNames;
    }

    public void setSchemaNamespaceLocations(Map<String, XmlSchemaImportLocation> schemaNamespaceLocations) {
        this.importSchemaLocations = schemaNamespaceLocations;
    }

    public void addSchemaNamespaceLocation(String namespaceUri, XmlSchemaImportLocation location) {
        importSchemaLocations.put(namespaceUri, location);
    }

    public void addSchemaNamespace(String xdefName, Map<String, String> schemaNamespaces) {
        addSchemaNamespace((Object)xdefName, schemaNamespaces);
    }

    public void addSchemaNamespace(Integer xdefIndex, Map<String, String> schemaNamespaces) {
        addSchemaNamespace((Object)xdefIndex, schemaNamespaces);
    }

    private void addSchemaNamespace(Object xdefIdentifier, Map<String, String> schemaNamespaces) {
        if (this.schemaNamespaces.containsKey(xdefIdentifier)) {
            this.schemaNamespaces.get(xdefIdentifier).putAll(schemaNamespaces);
        } else {
            this.schemaNamespaces.put(xdefIdentifier, schemaNamespaces);
        }
    }

    public void addSchemaNamespace(String xdefName, String prefix, String namespaceUri) {
        addSchemaNamespace((Object)xdefName, prefix, namespaceUri);
    }

    public void addSchemaNamespace(Integer xdefIndex, String prefix, String namespaceUri) {
        addSchemaNamespace((Object)xdefIndex, prefix, namespaceUri);
    }

    private void addSchemaNamespace(Object xdefIdentifier, String prefix, String namespaceUri) {
        if (schemaNamespaces.containsKey(xdefIdentifier)) {
            schemaNamespaces.get(xdefIdentifier).put(prefix, namespaceUri);
        } else {
            Map<String, String> namespaces = new HashMap<String, String>();
            namespaces.put(prefix, namespaceUri);
            schemaNamespaces.put(xdefIdentifier, namespaces);
        }
    }

    public void setElemSchemaForm(String xdefName, XmlSchemaForm elemSchemaForm) {
        setElemSchemaForm((Object)xdefName, elemSchemaForm);
    }

    public void setElemSchemaForm(Integer xdefIndex, XmlSchemaForm elemSchemaForm) {
        setElemSchemaForm((Object)xdefIndex, elemSchemaForm);
    }

    private void setElemSchemaForm(Object xdefIdentifier, XmlSchemaForm elemSchemaForm) {
        this.elemSchemaForm.put(xdefIdentifier, elemSchemaForm);
    }

    public void setAttrSchemaForm(String xdefName, XmlSchemaForm attrSchemaForm) {
        setAttrSchemaForm((Object)xdefName, attrSchemaForm);
    }

    public void setAttrSchemaForm(Integer xdefIndex, XmlSchemaForm attrSchemaForm) {
        setAttrSchemaForm((Object)xdefIndex, attrSchemaForm);
    }

    private void setAttrSchemaForm(Object xdefIdentifier, XmlSchemaForm attrSchemaForm) {
        this.attrSchemaForm.put(xdefIdentifier, attrSchemaForm);
    }

    public void setTargetNamespace(String xdefName, String targetNamespace) {
        setTargetNamespace((Object)xdefName, targetNamespace);
    }

    public void setTargetNamespace(Integer xdefIndex, String targetNamespace) {
        setTargetNamespace((Object)xdefIndex, targetNamespace);
    }

    private void setTargetNamespace(Object xdefIdentifier, String targetNamespace) {
        this.targetNamespace.put(xdefIdentifier, targetNamespace);
    }

    @Override
    public XmlSchemaCollection createSchemas(XDPool xdPool, String[] xdefNames) {
        if (xdPool == null) {
            throw new IllegalArgumentException("xdPool = null");
        }

        schemaNames.clear();

        XMDefinition xmDefinitions[] = xdPool.getXMDefinitions();
        List<String> xdefNamesFilterList = Arrays.asList(xdefNames);
        XD2XsdAdapter adapter = createAdapter();

        XmlSchemaCollection xmlSchemaCollection = new XmlSchemaCollection();
        initNamespaceContext(xmlSchemaCollection, xmDefinitions);

        int j = 0;
        for (int i = 0; i < xmDefinitions.length; i++) {
            if (xmDefinitions[i].getName().equals(xdefNamesFilterList.contains(i))) {
                Pair<String, XmlSchema> schemaPair = createSchema(xmlSchemaCollection, adapter, xmDefinitions[i], j++);
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

        schemaNames.clear();

        XMDefinition xmDefinitions[] = xdPool.getXMDefinitions();
        List<Integer> xdefIndexesFilterList = Arrays.asList(xdefIndexes);
        XD2XsdAdapter adapter = createAdapter();
        XmlSchemaCollection xmlSchemaCollection = new XmlSchemaCollection();
        initNamespaceContext(xmlSchemaCollection, xmDefinitions);

        int j = 0;
        for (int i = 0; i < xmDefinitions.length; i++) {
            if (xdefIndexesFilterList.contains(i)) {
                Pair<String, XmlSchema> schemaPair = createSchema(xmlSchemaCollection, adapter, xmDefinitions[i], j++);
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

        schemaNames.clear();

        XMDefinition xmDefinitions[] = xdPool.getXMDefinitions();
        XD2XsdAdapter adapter = createAdapter();
        XmlSchemaCollection xmlSchemaCollection = new XmlSchemaCollection();
        initNamespaceContext(xmlSchemaCollection, xmDefinitions);

        for (int i = 0; i < xmDefinitions.length; i++) {
            Pair<String, XmlSchema> schemaPair = createSchema(xmlSchemaCollection, adapter, xmDefinitions[i], i);
            if (!schemaNames.add(schemaPair.getKey())) {
                throw new RuntimeException("XSD schema with name " + schemaPair.getKey() + " already exists!");
            }
        }

        return xmlSchemaCollection;
    }

    private Pair<String, XmlSchema> createSchema(final XmlSchemaCollection xmlSchemaCollection, final XD2XsdAdapter adapter, final XMDefinition xmDefinition, Integer index) {
        initAdapter(adapter, xmDefinition, index);
        return adapter.createSchema(xmDefinition, xmlSchemaCollection);
    }

    private void initNamespaceContext(final XmlSchemaCollection xmlSchemaCollection, final XMDefinition xmDefinitions[]) {
        // Namespace initialization
        NamespaceMap namespaceMap = new NamespaceMap();
        namespaceMap.add("xs", Constants.URI_2001_SCHEMA_XSD);

        Map<String, List<String>> xDeftargetNamespaces = new HashMap<String, List<String>>();

        for (int i = 0; i < xmDefinitions.length; i++) {
            for (Map.Entry<String, String> entry : ((XDefinition) xmDefinitions[i])._namespaces.entrySet()) {
                if (XD2XsdUtils.isDefaultNamespacePrefix(entry.getKey())) {
                    continue;
                }

                // TODO: remap targetNamespace prefixes? create dictionary for mapping?
                if (!namespaceMap.containsKey(entry.getKey())) {
                    namespaceMap.add(entry.getKey(), entry.getValue());
                } else {
                    System.out.println("XSD schema already contains namespace " + entry.getKey());
                }

                // Save target namespaces
                if ("tns".equals(entry.getKey())) {
                    final String xDefName = xmDefinitions[i].getName();
                    List<String> xDefNames = xDeftargetNamespaces.get(entry.getValue());
                    if (xDefNames == null) {
                        xDeftargetNamespaces.put(entry.getValue(), Arrays.asList(xDefName));
                    } else {
                        xDefNames.add(xDefName);
                    }
                }
            }
        }

        xmlSchemaCollection.setNamespaceContext(namespaceMap);

        // Update schema locations based on target namespaces
        for (Map.Entry<String, XmlSchemaImportLocation> entry : importSchemaLocations.entrySet()) {
            List<String> locationList = xDeftargetNamespaces.get(entry.getKey());
            if (locationList != null && locationList.size() == 1 && entry.getValue().getFileName() == null) {
                entry.getValue().setFileName(locationList.get(0));
            }
        }
    }

    private XD2XsdAdapter createAdapter() {
        XD2XsdAdapter adapter = new XD2XsdAdapter();
        adapter.setPrintXdTree(printXdTree);
        return adapter;
    }

    private void initAdapter(final XD2XsdAdapter adapter, final XMDefinition xmDefinition, Integer index) {
        adapter.setSchemaNamespaceLocations(importSchemaLocations);

        if (index != null && schemaNamespaces.containsKey(index)) {
            adapter.setSchemaNamespaces(schemaNamespaces.get(index));
        }

        if (schemaNamespaces.containsKey(xmDefinition.getName())) {
            adapter.setSchemaNamespaces(schemaNamespaces.get(xmDefinition.getName()));
        }

        if (index != null && elemSchemaForm.containsKey(index)) {
            adapter.setElemSchemaForm(elemSchemaForm.get(index));
        }

        if (elemSchemaForm.containsKey(xmDefinition.getName())) {
            adapter.setElemSchemaForm(elemSchemaForm.get(xmDefinition.getName()));
        }

        if (index != null && attrSchemaForm.containsKey(index)) {
            adapter.setAttrSchemaForm(attrSchemaForm.get(index));
        }

        if (attrSchemaForm.containsKey(xmDefinition.getName())) {
            adapter.setAttrSchemaForm(attrSchemaForm.get(xmDefinition.getName()));
        }

        if (index != null && targetNamespace.containsKey(index)) {
            adapter.setTargetNamespace(targetNamespace.get(index));
        }

        if (targetNamespace.containsKey(xmDefinition.getName())) {
            adapter.setTargetNamespace(targetNamespace.get(xmDefinition.getName()));
        }
    }

}
