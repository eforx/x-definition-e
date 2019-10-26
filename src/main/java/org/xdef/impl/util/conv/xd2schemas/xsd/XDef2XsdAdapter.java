package org.xdef.impl.util.conv.xd2schemas.xsd;

import javafx.util.Pair;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.xdef.XDPool;
import org.xdef.impl.XDefinition;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.xd2schemas.XDef2SchemaAdapter;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.XsdElementFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.XmlSchemaImportLocation;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XD2XsdUtils;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdNamespaceUtils;
import org.xdef.model.XMDefinition;
import org.xdef.model.XMNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.*;

public class XDef2XsdAdapter implements XDef2SchemaAdapter<XmlSchemaCollection> {

    private int logLevel = LOG_LEVEL_NONE;

    private XDefinition xDefinition = null;
    private XsdElementFactory xsdBuilder = null;
    private Set<String> schemaNames = null;
    private XmlSchema schema = null;
    private XDPool2XsdAdapter poolAdapter = null;

    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * Get names of all created schemas
     * @return
     */
    public final Set<String> getSchemaNames() {
        return schemaNames;
    }

    public void setPoolAdapter(XDPool2XsdAdapter poolAdapter) {
        this.poolAdapter = poolAdapter;
    }

    @Override
    public XmlSchemaCollection createSchema(final XDPool xdPool) {
        if (xdPool == null) {
            throw new IllegalArgumentException("xdPool = null");
        }
        return createSchema(xdPool.getXMDefinition());
    }

    @Override
    public XmlSchemaCollection createSchema(final XMDefinition xdef) {
        XmlSchemaCollection xmlSchemaCollection = new XmlSchemaCollection();
        createSchema(xdef, xmlSchemaCollection);
        return xmlSchemaCollection;
    }

    protected void createSchema(final XMDefinition xDef, final XmlSchemaCollection xmlSchemaCollection) {
        if (xDef == null) {
            throw new IllegalArgumentException("xdef = null");
        }

        if (XsdLogger.isInfo(logLevel)) {
            XsdLogger.printC(INFO, XSD_XDEF_ADAPTER, "====================");
            XsdLogger.printC(INFO, XSD_XDEF_ADAPTER, "Transforming x-definition. Name=" + xDef.getName());
            XsdLogger.printC(INFO, XSD_XDEF_ADAPTER, "====================");
        }

        schemaNames = new HashSet<String>();
        this.xDefinition = (XDefinition)xDef;

        Map<String, XmlSchemaImportLocation> schemaLocations = poolAdapter != null ? poolAdapter.getSchemaLocations() : new HashMap<String, XmlSchemaImportLocation>();
        Map<String, XmlSchemaImportLocation> extraSchemaLocations = poolAdapter != null ? poolAdapter.getExtraSchemaLocations() : new HashMap<String, XmlSchemaImportLocation>();
        NamespaceMap namespaceCtx = poolAdapter != null ? poolAdapter.getNamespaceCtx().get(xDef.getName()) : XsdNamespaceUtils.createCtx();

        // Initialize XSD schema
        initSchema(xmlSchemaCollection, namespaceCtx);

        XDTree2XsdAdapter treeAdapter = new XDTree2XsdAdapter(logLevel, schema, xsdBuilder, null, extraSchemaLocations);
        treeAdapter.loadXdefRootNames(xDefinition);

        // Extract all used references in x-definition
        XD2XsdReferenceAdapter referenceAdapter = new XD2XsdReferenceAdapter(logLevel, xsdBuilder, treeAdapter, schema, schemaLocations, extraSchemaLocations, false);
        referenceAdapter.createRefsAndImports(xDefinition);

        addXdefNamespaces(referenceAdapter.getSystemIdImports());

        // Convert x-definition tree to XSD tree
        convertXdef(treeAdapter);

        // TODO: Merge extra nodes for multiple x-definitions
        if (!treeAdapter.getExtraNodes().isEmpty() && !extraSchemaLocations.isEmpty()) {
            XD2XsdExtraSchemaAdapter.createExtraSchemas(xDef, schema.getSchemaNamespacePrefix(), namespaceCtx,
                    treeAdapter.getExtraNodes(), schemaLocations, extraSchemaLocations, xmlSchemaCollection, schemaNames, logLevel);
        }
    }

    private void convertXdef(final XDTree2XsdAdapter treeAdapter) {
        if (XsdLogger.isInfo(logLevel)) {
            XsdLogger.printC(INFO, XSD_XDEF_ADAPTER, "Transform x-definition tree ...");
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

    private void initSchema(final XmlSchemaCollection xmlSchemaCollection, NamespaceMap namespaceCtx) {
        if (XsdLogger.isInfo(logLevel)) {
            XsdLogger.printP(INFO, INITIALIZATION, xDefinition, "Initialize XSD schema");
        }

        if (!schemaNames.add(xDefinition.getName())) {
            if (XsdLogger.isError(logLevel)) {
                XsdLogger.printP(ERROR, INITIALIZATION, xDefinition, "X-definition with this name has been already processed! Name=" + xDefinition.getName());
            }

            throw new IllegalArgumentException("X-definition name duplication");
        }

        // Target namespace
        Boolean targetNamespaceError = false;
        Pair<String, String> targetNamespace = XD2XsdUtils.getSchemaTargetNamespace(xDefinition, targetNamespaceError);

        if (XsdLogger.isDebug(logLevel)) {
            XsdLogger.printP(DEBUG, INITIALIZATION, xDefinition, "Getting basic information." +
                    "systemName=" + schemaNames + ", targetNamespacePrefix=" + targetNamespace.getKey() + ", targetNamespaceUri" + targetNamespace.getValue());
        }

        schema = new XmlSchema(targetNamespace.getValue(), xDefinition.getName(), xmlSchemaCollection);

        if (targetNamespace.getKey() != null && targetNamespace.getValue() != null) {
            schema.setSchemaNamespacePrefix(targetNamespace.getKey());
        }

        xsdBuilder = new XsdElementFactory(logLevel, schema);

        if (XsdLogger.isDebug(logLevel)) {
            XsdLogger.printP(DEBUG, INITIALIZATION, xDefinition, "Initializing namespace context ...");
        }

        // Namespace context
        if (namespaceCtx == null) {
            if (XsdLogger.isWarn(logLevel)) {
                XsdLogger.printP(WARN, INITIALIZATION, xDefinition, "Namespace context should be already initializated!");
            }

            namespaceCtx = XsdNamespaceUtils.createCtx();
        }

        if (namespaceCtx.getDeclaredPrefixes().length == 1) {
            XsdNamespaceUtils.initCtx(namespaceCtx, xDefinition, targetNamespace.getKey(), targetNamespace.getValue(), INITIALIZATION, logLevel);
        }
        schema.setNamespaceContext(namespaceCtx);

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
    }

    /**
     * Add x-definition names as namespace to namespace context
     * @param xDefs
     */
    private void addXdefNamespaces(final Set<String> xDefs) {
        if (XsdLogger.isInfo(logLevel)) {
            XsdLogger.printP(INFO, PREPROCESSING, xDefinition, "Updating namespace context - add namespaces of other x-definitions");
        }

        NamespaceMap namespaceMap = (NamespaceMap)schema.getNamespaceContext();
        for (String xDefName : xDefs) {
            XsdNamespaceUtils.addNamespaceToCtx(namespaceMap, xDefinition.getName(),
                    XD2XsdUtils.createNsPrefixFromXDefName(xDefName), XD2XsdUtils.createNsUriFromXDefName(xDefName),
                    PREPROCESSING, logLevel);
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
