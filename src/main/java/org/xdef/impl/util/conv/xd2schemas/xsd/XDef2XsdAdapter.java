package org.xdef.impl.util.conv.xd2schemas.xsd;

import javafx.util.Pair;
import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.constants.Constants;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.xdef.XDPool;
import org.xdef.impl.*;
import org.xdef.impl.util.conv.xd2schemas.XDef2SchemaAdapter;
import org.xdef.impl.util.conv.xd2schemas.xsd.builder.XsdElementBuilder;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.XmlSchemaImportLocation;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XD2XsdUtils;
import org.xdef.model.XMDefinition;

import java.io.PrintStream;
import java.util.*;

import static org.xdef.impl.util.conv.xd2schemas.xsd.XD2XsdDefinitions.XD_DEFAULT_TARGET_NAMESPACE_PREFIX;
import static org.xdef.impl.util.conv.xd2schemas.xsd.XD2XsdDefinitions.XSD_DEFAULT_SCHEMA_NAMESPACE_PREFIX;

public class XDef2XsdAdapter implements XDef2SchemaAdapter<XmlSchema> {

    private boolean printXdTree = false;
    private XDefinition xDefinition = null;
    private String schemaName = null;
    private XmlSchema schema = null;
    private XsdElementBuilder xsdBuilder = null;

    /**
     * ================ Input parameters ================
     */

    /**
     * Key:     namespace prefix
     * Value:   namespace URI
     */
    private Map<String, String> schemaNamespaces = new HashMap<String, String>();
    /**
     * Key:     schema namespace
     * Value:   schema location
     */
    private Map<String, XmlSchemaImportLocation> importSchemaLocations = new HashMap<String, XmlSchemaImportLocation>();
    private XmlSchemaForm elemSchemaForm = null;
    private XmlSchemaForm attrSchemaForm = null;
    private String targetNamespace = null;

    public void setPrintXdTree(boolean printXdTree) {
        this.printXdTree = printXdTree;
    }

    public final XDefinition getXDefinition() {
        return xDefinition;
    }

    public final String getSchemaName() {
        return schemaName;
    }

    public void setSchemaNamespaces(Map<String, String> schemaNamespaces) {
        this.schemaNamespaces = schemaNamespaces;
    }

    public void addSchemaNamespace(String prefix, String namespaceUri) {
        schemaNamespaces.put(prefix, namespaceUri);
    }

    public void setSchemaNamespaceLocations(Map<String, XmlSchemaImportLocation> schemaNamespaceLocations) {
        this.importSchemaLocations = schemaNamespaceLocations;
    }

    public void addSchemaNamespaceLocation(String namespaceUri, XmlSchemaImportLocation location) {
        importSchemaLocations.put(namespaceUri, location);
    }

    public void setElemSchemaForm(XmlSchemaForm elemSchemaForm) {
        this.elemSchemaForm = elemSchemaForm;
    }

    public void setAttrSchemaForm(XmlSchemaForm attrSchemaForm) {
        this.attrSchemaForm = attrSchemaForm;
    }

    public void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }

    @Override
    public XmlSchema createSchema(final XDPool xdPool) {

        if (xdPool == null) {
            throw new IllegalArgumentException("xdPool = null");
        }

        return createSchema(xdPool.getXMDefinition());
    }

    @Override
    public XmlSchema createSchema(final XDPool xdPool, final String xdefName) {

        if (xdPool == null) {
            throw new IllegalArgumentException("xdPool = null");
        }

        if (xdefName == null) {
            throw new IllegalArgumentException("xdefName = null");
        }

        return createSchema(xdPool.getXMDefinition(xdefName));
    }

    @Override
    public XmlSchema createSchema(XDPool xdPool, int xdefIndex) {
        if (xdPool == null) {
            throw new IllegalArgumentException("xdPool = null");
        }

        if (xdefIndex < 0) {
            throw new IllegalArgumentException("xdefIndex < 0");
        }

        XMDefinition xmDefinitions[] = xdPool.getXMDefinitions();

        if (xdefIndex > xmDefinitions.length) {
            throw new IllegalArgumentException("xdefIndex > xmDefinitions.length");
        }

        return createSchema(xmDefinitions[xdefIndex]);
    }

    @Override
    public XmlSchema createSchema(final XMDefinition xdef) {
        return createSchema(xdef, new XmlSchemaCollection()).getValue();
    }

    protected Pair<String, XmlSchema> createSchema(final XMDefinition xdef, final XmlSchemaCollection xmlSchemaCollection) {

        if (xdef == null) {
            throw new IllegalArgumentException("xdef = null");
        }

        this.xDefinition = (XDefinition)xdef;

        // Initialize XSD schema
        initSchema(xmlSchemaCollection);

        XDTree2XsdAdapter treeAdapter = new XDTree2XsdAdapter(printXdTree, schema, xsdBuilder);

        treeAdapter.loadXdefRootNames(xDefinition);

        // Extract all used references in x-definition
        XD2XsdReferenceAdapter referenceAdapter = new XD2XsdReferenceAdapter(xsdBuilder, treeAdapter, schema, importSchemaLocations);
        referenceAdapter.createRefsAndImports(xDefinition, System.out);

        // Convert x-definition tree to XSD tree
        convertXdef(xDefinition, System.out, treeAdapter);

        return new Pair<String, XmlSchema>(schemaName, schema);
    }

    private void convertXdef(
            final XDefinition xDef,
            final PrintStream out,
            final XDTree2XsdAdapter treeAdapter) {

        if (printXdTree) {
            out.print("XMDefinition: ");
            XDTree2XsdAdapter.displayDesriptor(xDef, out);

            int i = 0;
            for (; i < treeAdapter.getXdRootNames().size(); i++) {
                out.println("|-- Root: " + treeAdapter.getXdRootNames().get(i));
                if (i > 0) {
                    out.println("    | " + treeAdapter.getXdRootNames().get(i));
                }
            }

            if (i == 0) {
                out.println("|-- Root: null");
            }
        }

        XElement[] elems = xDef.getXElements();
        for (int i = 0; i < elems.length; i++){
            if (treeAdapter.getXdRootNames().contains(elems[i].getName())) {
                XmlSchemaElement xsdElem = (XmlSchemaElement) treeAdapter.convertTree(elems[i], out, "|   ");
                XD2XsdUtils.addElement(schema, xsdElem);
            }
        }
    }

    private void initSchema(final XmlSchemaCollection xmlSchemaCollection) {
        schemaName = xDefinition.getName();

        // Try to select target namespace from x-definition
        if (targetNamespace == null) {
            for (Map.Entry<String, String> entry : xDefinition._namespaces.entrySet()) {
                if (XD_DEFAULT_TARGET_NAMESPACE_PREFIX.equals(entry.getKey())) {
                    targetNamespace = entry.getValue();
                    break;
                }
            }
        }

        schema = new XmlSchema(targetNamespace, schemaName, xmlSchemaCollection);

        if (elemSchemaForm != null && !XmlSchemaForm.NONE.equals(elemSchemaForm)) {
            schema.setElementFormDefault(elemSchemaForm);
        }
        if (attrSchemaForm != null && !XmlSchemaForm.NONE.equals(attrSchemaForm)) {
            schema.setAttributeFormDefault(attrSchemaForm);
        }

        xsdBuilder = new XsdElementBuilder(schema);

        // Namespace initialization
        NamespaceMap namespaceMap = new NamespaceMap();
        namespaceMap.add(XSD_DEFAULT_SCHEMA_NAMESPACE_PREFIX, Constants.URI_2001_SCHEMA_XSD);

        for (Map.Entry<String, String> entry : schemaNamespaces.entrySet()) {
            namespaceMap.add(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, String> entry : xDefinition._namespaces.entrySet()) {
            if (XD2XsdUtils.isDefaultNamespacePrefix(entry.getKey())) {
                continue;
            }

            if (!namespaceMap.containsKey(entry.getKey())) {
                namespaceMap.add(entry.getKey(), entry.getValue());
            } else {
                System.out.println("XDef - XSD schema already contains namespace " + entry.getKey());
            }
        }

        schema.setNamespaceContext(namespaceMap);
        if (targetNamespace != null) {
            schema.setSchemaNamespacePrefix(namespaceMap.getPrefix(targetNamespace));
        }
    }

}
