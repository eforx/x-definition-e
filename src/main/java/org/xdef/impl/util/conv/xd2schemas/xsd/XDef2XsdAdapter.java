package org.xdef.impl.util.conv.xd2schemas.xsd;

import javafx.util.Pair;
import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.constants.Constants;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.xdef.XDPool;
import org.xdef.impl.XDefinition;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.xd2schemas.XDef2SchemaAdapter;
import org.xdef.impl.util.conv.xd2schemas.xsd.builder.XsdElementBuilder;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.XmlSchemaImportLocation;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XD2XsdUtils;
import org.xdef.model.XMDefinition;
import org.xdef.model.XMNode;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.xdef.impl.util.conv.xd2schemas.xsd.XD2XsdDefinitions.XSD_DEFAULT_SCHEMA_NAMESPACE_PREFIX;

public class XDef2XsdAdapter implements XDef2SchemaAdapter<XmlSchema> {

    private boolean verbose = false;
    private XDefinition xDefinition = null;
    private String schemaName = null;
    private XmlSchema schema = null;
    private XsdElementBuilder xsdBuilder = null;

    /**
     * ================ Input parameters ================
     */

    /**
     * Key:     schema namespace URI
     * Value:   schema location
     */
    private Map<String, XmlSchemaImportLocation> importSchemaLocations = new HashMap<String, XmlSchemaImportLocation>();

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public final XDefinition getXDefinition() {
        return xDefinition;
    }

    public final String getSchemaName() {
        return schemaName;
    }


    public void setSchemaNamespaceLocations(Map<String, XmlSchemaImportLocation> schemaNamespaceLocations) {
        this.importSchemaLocations = schemaNamespaceLocations;
    }

    public void addSchemaNamespaceLocation(String namespaceUri, XmlSchemaImportLocation location) {
        importSchemaLocations.put(namespaceUri, location);
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

    protected Pair<String, XmlSchema> createSchema(final XMDefinition xDef, final XmlSchemaCollection xmlSchemaCollection) {

        if (xDef == null) {
            throw new IllegalArgumentException("xdef = null");
        }

        this.xDefinition = (XDefinition)xDef;

        // Initialize XSD schema
        initSchema(xmlSchemaCollection);

        XDTree2XsdAdapter treeAdapter = new XDTree2XsdAdapter(verbose, schema, xsdBuilder);

        treeAdapter.loadXdefRootNames(xDefinition);

        // Extract all used references in x-definition
        XD2XsdReferenceAdapter referenceAdapter = new XD2XsdReferenceAdapter(xsdBuilder, treeAdapter, schema, importSchemaLocations);
        referenceAdapter.createRefsAndImports(xDefinition, System.out);

        addXdefNamespaces(referenceAdapter.getSystemIdImports());

        // Convert x-definition tree to XSD tree
        convertXdef(xDefinition, System.out, treeAdapter);

        return new Pair<String, XmlSchema>(schemaName, schema);
    }

    private void convertXdef(
            final XDefinition xDef,
            final PrintStream out,
            final XDTree2XsdAdapter treeAdapter) {

        if (verbose) {
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

        Boolean targetNamespaceError = false;

        Pair<String, String> targetNamespace = XD2XsdUtils.getSchemaTargetNamespace(xDefinition, targetNamespaceError);

        schema = new XmlSchema(targetNamespace.getValue(), schemaName, xmlSchemaCollection);

        xsdBuilder = new XsdElementBuilder(schema);

        // Namespace initialization
        NamespaceMap namespaceMap = new NamespaceMap();
        namespaceMap.add(XSD_DEFAULT_SCHEMA_NAMESPACE_PREFIX, Constants.URI_2001_SCHEMA_XSD);

        for (Map.Entry<String, String> entry : xDefinition._namespaces.entrySet()) {
            final String nsPrefix = entry.getKey();
            final String nsUri = entry.getValue();

            if (XD2XsdUtils.isDefaultNamespacePrefix(nsPrefix) || (targetNamespace.getKey() != null && nsPrefix.equals(targetNamespace.getKey()))) {
                continue;
            }

            if (!namespaceMap.containsKey(nsPrefix)) {
                namespaceMap.add(nsPrefix, nsUri);
            } else {
                System.out.println("[" + xDefinition.getName() + "] XSD schema already has defined namespace prefix " + nsPrefix);
            }
        }

        // Set attributeFormDefault and elementFormDefault
        {
            XmlSchemaForm elemSchemaForm = getElemDefaultForm(targetNamespace.getKey());
            schema.setElementFormDefault(elemSchemaForm);

            XmlSchemaForm attrQualified = getAttrDefaultForm(targetNamespace.getKey());
            schema.setAttributeFormDefault(attrQualified);
        }

        // Set target namespace
        if (targetNamespace.getKey() != null && targetNamespace.getValue() != null) {
            namespaceMap.add(targetNamespace.getKey(), targetNamespace.getValue());
            schema.setSchemaNamespacePrefix(targetNamespace.getKey());
        }

        schema.setNamespaceContext(namespaceMap);
    }

    private void addXdefNamespaces(final Set<String> xDefs) {
        NamespaceMap namespaceMap = (NamespaceMap)schema.getNamespaceContext();
        for (String xDefName : xDefs) {
            namespaceMap.add(XD2XsdUtils.createNsPrefixFromXDefName(xDefName), XD2XsdUtils.createNsUriFromXDefName(xDefName));
        }
    }

    private XmlSchemaForm getElemDefaultForm(final String targetNsPrefix) {
        if (targetNsPrefix != null && targetNsPrefix.trim().isEmpty()) {
            return XmlSchemaForm.QUALIFIED;
        }

        if (xDefinition._rootSelection != null && xDefinition._rootSelection.size() > 0) {
            for (XNode xn : xDefinition._rootSelection.values()) {
                if (xn.getKind() == XNode.XMELEMENT) {
                    XElement defEl = (XElement)xn;
                    String tmpNs = XD2XsdUtils.getNamespacePrefix(defEl.getName());
                    if (tmpNs == null && defEl.getReferencePos() != null) {
                        tmpNs = XD2XsdUtils.getReferenceSystemId(defEl.getReferencePos());
                    }
                    if (tmpNs != null && tmpNs.equals(targetNsPrefix)) {
                        return XmlSchemaForm.QUALIFIED;
                    }
                }
            }
        }

        return XmlSchemaForm.UNQUALIFIED;
    }

    private XmlSchemaForm getAttrDefaultForm(final String targetNsPrefix) {
        if (xDefinition._rootSelection != null && xDefinition._rootSelection.size() > 0) {
            for (XNode xn : xDefinition._rootSelection.values()) {
                if (xn.getKind() == XNode.XMELEMENT) {
                    XElement defEl = (XElement)xn;
                    for (XMNode attr : defEl.getXDAttrs()) {
                        String tmpNs = XD2XsdUtils.getNamespacePrefix(attr.getName());
                        if (tmpNs != null && tmpNs.equals(targetNsPrefix)) {
                            return XmlSchemaForm.QUALIFIED;
                        }
                    }
                }
            }
        }

        return XmlSchemaForm.UNQUALIFIED;
    }

}
