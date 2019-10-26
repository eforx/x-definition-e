package org.xdef.impl.util.conv.xd2schemas.xsd;

import javafx.util.Pair;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaForm;
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
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;
import org.xdef.model.XMDefinition;
import org.xdef.model.XMNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.xdef.impl.util.conv.xd2schemas.xsd.XD2XsdDefinitions.XSD_DEFAULT_SCHEMA_NAMESPACE_PREFIX;
import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.*;

public class XDef2XsdAdapter implements XDef2SchemaAdapter<XmlSchema> {

    private int logLevel = LOG_LEVEL_NONE;

    private XDefinition xDefinition = null;
    private String schemaName = null;
    private XmlSchema schema = null;
    private XsdElementBuilder xsdBuilder = null;

    /**
     * Initialized by {@link XDPool2XsdAdapter}
     * Key:     schema namespace URI
     * Value:   schema location
     */
    private Map<String, XmlSchemaImportLocation> importSchemaLocations = new HashMap<String, XmlSchemaImportLocation>();

    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
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

    @Override
    public XmlSchema createSchema(final XDPool xdPool) {

        if (xdPool == null) {
            throw new IllegalArgumentException("xdPool = null");
        }

        return createSchema(xdPool.getXMDefinition());
    }

    @Override
    public XmlSchema createSchema(final XMDefinition xdef) {
        return createSchema(xdef, new XmlSchemaCollection()).getValue();
    }

    protected Pair<String, XmlSchema> createSchema(final XMDefinition xDef, final XmlSchemaCollection xmlSchemaCollection) {

        if (xDef == null) {
            throw new IllegalArgumentException("xdef = null");
        }

        if (XsdLogger.isInfo(logLevel)) {
            XsdLogger.printC(INFO, CAT_XD_DEF, "====================");
            XsdLogger.printC(INFO, CAT_XD_DEF, "Transforming x-definition. Name=" + xDef.getName());
            XsdLogger.printC(INFO, CAT_XD_DEF, "====================");
        }

        this.xDefinition = (XDefinition)xDef;

        // Initialize XSD schema
        initSchema(xmlSchemaCollection);

        XDTree2XsdAdapter treeAdapter = new XDTree2XsdAdapter(logLevel, schema, xsdBuilder);

        treeAdapter.loadXdefRootNames(xDefinition);

        // Extract all used references in x-definition
        XD2XsdReferenceAdapter referenceAdapter = new XD2XsdReferenceAdapter(logLevel, xsdBuilder, treeAdapter, schema, importSchemaLocations);
        referenceAdapter.createRefsAndImports(xDefinition);

        addXdefNamespaces(referenceAdapter.getSystemIdImports());

        // Convert x-definition tree to XSD tree
        convertXdef(treeAdapter);

        return new Pair<String, XmlSchema>(schemaName, schema);
    }

    private void convertXdef(final XDTree2XsdAdapter treeAdapter) {
        if (XsdLogger.isInfo(logLevel)) {
            XsdLogger.printC(INFO, CAT_XSD_BUILDER, "Transform x-definition tree ...");
        }

        for (XElement elem : xDefinition.getXElements()) {
            if (treeAdapter.getXdRootNames().contains(elem.getName())) {
                XmlSchemaElement xsdElem = (XmlSchemaElement) treeAdapter.convertTree(elem);
                XD2XsdUtils.addElement(schema, xsdElem);
                if (XsdLogger.isInfo(logLevel)) {
                    XsdLogger.printP(INFO, TRANSFORMATION, elem, "Adding root element to schema. Element=" + elem.getName());
                }
            }
        }
    }

    private void initSchema(final XmlSchemaCollection xmlSchemaCollection) {
        if (XsdLogger.isInfo(logLevel)) {
            XsdLogger.printP(INFO, INITIALIZATION, xDefinition, "Initialize XSD schema");
        }

        schemaName = xDefinition.getName();

        Boolean targetNamespaceError = false;

        Pair<String, String> targetNamespace = XD2XsdUtils.getSchemaTargetNamespace(xDefinition, targetNamespaceError);

        if (XsdLogger.isDebug(logLevel)) {
            XsdLogger.printP(DEBUG, INITIALIZATION, xDefinition, "Getting basic information." +
                    "systemName=" + schemaName + ", targetNamespacePrefix=" + targetNamespace.getKey() + ", targetNamespaceUri" + targetNamespace.getValue());
        }

        schema = new XmlSchema(targetNamespace.getValue(), schemaName, xmlSchemaCollection);

        xsdBuilder = new XsdElementBuilder(logLevel, schema);

        if (XsdLogger.isDebug(logLevel)) {
            XsdLogger.printP(DEBUG, INITIALIZATION, xDefinition, "Initializing namespace context ...");
        }

        // Namespace initialization
        NamespaceMap namespaceMap = new NamespaceMap();
        addNamespaceToCtx(namespaceMap, XSD_DEFAULT_SCHEMA_NAMESPACE_PREFIX, Constants.URI_2001_SCHEMA_XSD);

        // Set target namespace
        if (targetNamespace.getKey() != null && targetNamespace.getValue() != null) {
            addNamespaceToCtx(namespaceMap, targetNamespace.getKey(), targetNamespace.getValue());
            schema.setSchemaNamespacePrefix(targetNamespace.getKey());
        }

        for (Map.Entry<String, String> entry : xDefinition._namespaces.entrySet()) {
            final String nsPrefix = entry.getKey();
            final String nsUri = entry.getValue();

            if (XD2XsdUtils.isDefaultNamespacePrefix(nsPrefix) || (targetNamespace.getKey() != null && nsPrefix.equals(targetNamespace.getKey()))) {
                continue;
            }

            if (!namespaceMap.containsKey(nsPrefix)) {
                addNamespaceToCtx(namespaceMap, nsPrefix, nsUri);
            } else {
                if (XsdLogger.isWarn(logLevel)) {
                    XsdLogger.printP(WARN, INITIALIZATION, xDefinition, "Namespace has been already defined! Prefix=" + nsPrefix + ", Uri=" + nsUri);
                }
            }
        }

        // Set attributeFormDefault and elementFormDefault
        {
            XmlSchemaForm elemSchemaForm = getElemDefaultForm(targetNamespace.getKey());
            schema.setElementFormDefault(elemSchemaForm);

            XmlSchemaForm attrSchemaForm = getAttrDefaultForm(targetNamespace.getKey());
            schema.setAttributeFormDefault(attrSchemaForm);

            if (XsdLogger.isDebug(logLevel)) {
                XsdLogger.printP(DEBUG, INITIALIZATION, xDefinition, "Setting element default schema form. Form=" + elemSchemaForm);
                XsdLogger.printP(DEBUG, INITIALIZATION, xDefinition, "Setting attribute default schema form. Form=" + attrSchemaForm);
            }
        }

        schema.setNamespaceContext(namespaceMap);
    }

    private void addXdefNamespaces(final Set<String> xDefs) {
        if (XsdLogger.isInfo(logLevel)) {
            XsdLogger.printP(INFO, PREPROCESSING, xDefinition, "Updating namespace context - add namespaces of other x-definitions");
        }

        NamespaceMap namespaceMap = (NamespaceMap)schema.getNamespaceContext();
        for (String xDefName : xDefs) {
            addNamespaceToCtx(namespaceMap, XD2XsdUtils.createNsPrefixFromXDefName(xDefName), XD2XsdUtils.createNsUriFromXDefName(xDefName));
        }
    }

    private void addNamespaceToCtx(final NamespaceMap namespaceMap, final String nsPrefix, final String nsUri) {
        namespaceMap.add(nsPrefix, nsUri);
        if (XsdLogger.isDebug(logLevel)) {
            XsdLogger.printP(DEBUG, INITIALIZATION, xDefinition, "Add namespace. Prefix=" + nsPrefix + ", Uri=" + nsUri);
        }
    }

    private XmlSchemaForm getElemDefaultForm(final String targetNsPrefix) {
        if (targetNsPrefix != null && targetNsPrefix.trim().isEmpty()) {
            if (XsdLogger.isDebug(logLevel)) {
                XsdLogger.printP(DEBUG, INITIALIZATION, xDefinition, "Target namespace prefix is empty. Element default form will be Qualified");
            }
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
                        if (XsdLogger.isDebug(logLevel)) {
                            XsdLogger.printP(DEBUG, INITIALIZATION, xDefinition, "Some of root element has different namespace prefix. Element default form will be Qualified. ExpectedPrefix=" + targetNsPrefix);
                        }
                        return XmlSchemaForm.QUALIFIED;
                    }
                }
            }
        }

        if (XsdLogger.isDebug(logLevel)) {
            XsdLogger.printP(DEBUG, INITIALIZATION, xDefinition, "All root elements have same namespace prefix. Element default form will be Unqualified");
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
                            if (XsdLogger.isDebug(logLevel)) {
                                XsdLogger.printP(DEBUG, INITIALIZATION, xDefinition, "Some of root attribute has different namespace prefix. Attribute default form will be Qualified. ExpectedPrefix=" + targetNsPrefix);
                            }
                            return XmlSchemaForm.QUALIFIED;
                        }
                    }
                }
            }
        }

        if (XsdLogger.isDebug(logLevel)) {
            XsdLogger.printP(DEBUG, INITIALIZATION, xDefinition, "All root attributes have same namespace prefix. Attribute default form will be Unqualified");
        }
        return XmlSchemaForm.UNQUALIFIED;
    }

}
