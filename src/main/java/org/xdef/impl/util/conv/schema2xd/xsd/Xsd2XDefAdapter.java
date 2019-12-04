package org.xdef.impl.util.conv.schema2xd.xsd;

import javafx.util.Pair;
import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.constants.Constants;
import org.apache.ws.commons.schema.utils.NamespacePrefixList;
import org.apache.ws.commons.schema.utils.NodeNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xdef.impl.util.conv.schema.util.XsdLogger;
import org.xdef.impl.util.conv.schema2xd.Schema2XDefAdapter;
import org.xdef.impl.util.conv.schema2xd.xsd.adapter.AbstractXsd2XdAdapter;
import org.xdef.impl.util.conv.schema2xd.xsd.adapter.Xsd2XdTreeAdapter;
import org.xdef.impl.util.conv.schema2xd.xsd.factory.XdElementFactory;
import org.xdef.impl.util.conv.schema2xd.xsd.model.XdAdapterCtx;
import org.xdef.impl.util.conv.schema2xd.xsd.util.XdNameUtils;
import org.xdef.impl.util.conv.schema2xd.xsd.util.XdNamespaceUtils;
import org.xdef.impl.util.conv.schema2xd.xsd.util.Xsd2XdUtils;
import org.xdef.xml.KXmlUtils;

import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.xdef.impl.util.conv.schema.util.XsdLoggerDefs.*;
import static org.xdef.impl.util.conv.xd2schema.xsd.definition.AlgPhase.*;

public class Xsd2XDefAdapter extends AbstractXsd2XdAdapter implements Schema2XDefAdapter<XmlSchema> {

    private XdElementFactory elementFactory;

    @Override
    public String createXDefinition(final XmlSchema rootSchema, final String xDefName) {
        if (rootSchema == null) {
            XsdLogger.print(LOG_ERROR, INITIALIZATION, xDefName, "Input XSD schema is not set!");
            return "";
        }

        XmlSchema[] schemas;
        if (rootSchema.getParent() != null) {
            schemas = rootSchema.getParent().getXmlSchemas();
        } else {
            schemas = new XmlSchema[1];
            schemas[0] = rootSchema;
        }

        if (schemas == null || schemas.length < 1) {
            XsdLogger.print(LOG_ERROR, INITIALIZATION, xDefName, "Input XSD schema collection is empty!");
            return "";
        }

        adapterCtx = new XdAdapterCtx(features);
        elementFactory = new XdElementFactory(adapterCtx);

        adapterCtx.init();

        final ArrayList<XmlSchema> schemasToBeProcessed = initializeSchemas(schemas, rootSchema, xDefName);

        if (schemasToBeProcessed.isEmpty()) {
            XsdLogger.print(LOG_ERROR, INITIALIZATION, xDefName, "No XSD schema to be processed found!");
            return "";
        }

        initializeNamespaces(schemasToBeProcessed);

        Element xdRootElem;

        if (schemasToBeProcessed.size() > 1) {
            xdRootElem = createXdPool();

            // First transform root XSD schema
            xdRootElem.appendChild(createXDef(xDefName, rootSchema, true));

            for (XmlSchema schema : schemasToBeProcessed) {
                if (rootSchema.equals(schema)) {
                    continue;
                }

                final String schemaName = adapterCtx.getXmlSchemaName(schema);
                xdRootElem.appendChild(createXDef(schemaName, schema, true));
            }
        } else {
            xdRootElem = createXDef(xDefName, rootSchema, false);
        }

        return elementFactory.createHeader() + KXmlUtils.nodeToString(xdRootElem, true);
    }

    private void transformXsdTree(final Xsd2XdTreeAdapter treeAdapter, final Element xdElem, final String xDefName, final XmlSchema schema) {
        XsdLogger.print(LOG_INFO, TRANSFORMATION, xDefName, "*** Transformation of XSD tree ***");

        final Map<QName, XmlSchemaType> schemaTypeMap = schema.getSchemaTypes();
        if (schemaTypeMap != null && !schemaTypeMap.isEmpty()) {
            for (XmlSchemaType xsdSchemaType : schemaTypeMap.values()) {
                treeAdapter.convertTree(xsdSchemaType, xdElem);
            }
        }

        final Map<QName, XmlSchemaGroup> groupMap = schema.getGroups();
        if (groupMap != null && !groupMap.isEmpty()) {
            for (XmlSchemaGroup xsdGroup : groupMap.values()) {
                treeAdapter.convertTree(xsdGroup, xdElem);
            }
        }

        final Map<QName, XmlSchemaElement> elementMap = schema.getElements();

        if (elementMap != null && !elementMap.isEmpty()) {
            for (XmlSchemaElement xsdElem : elementMap.values()) {
                treeAdapter.convertTree(xsdElem, xdElem);
            }
        }
    }

    private Map<XmlSchema, String> initializeSchemaNames(final XmlSchema[] schemas, final XmlSchema rootSchema, final String xDefName) {
        final Map<XmlSchema, String> schemaNames = new HashMap<XmlSchema, String>();

        initializeSchemaName(rootSchema, xDefName, schemaNames);

        for (int i = 0; i < schemas.length; i++) {
            final XmlSchema schema = schemas[i];
            for (XmlSchemaObject xmlNode : schema.getItems()) {
                if (xmlNode instanceof XmlSchemaExternal) {
                    initializeSchemaName(((XmlSchemaExternal) xmlNode).getSchema(), ((XmlSchemaExternal) xmlNode).getSchemaLocation(), schemaNames);
                }
            }
        }

        return schemaNames;
    }

    private void initializeSchemaName(final XmlSchema schema, final String schemaLocation, final Map<XmlSchema, String> schemaNames) {
        final String refSchemaSavedName = schemaNames.get(schema);
        final String refSchemaFileName = XdNameUtils.getSchemaName(schemaLocation);
        if (refSchemaSavedName == null) {
            schemaNames.put(schema, refSchemaFileName);
            XsdLogger.print(LOG_INFO, PREPROCESSING, XD_ADAPTER, "Add schema name. Name=" + refSchemaFileName);
        } else if (!refSchemaFileName.equals(refSchemaSavedName)) {
            XsdLogger.print(LOG_WARN, PREPROCESSING, XD_ADAPTER, "Schema already exists, but with different name! Original=" + refSchemaSavedName + ", Current=" + refSchemaFileName);
        } else {
            XsdLogger.print(LOG_DEBUG, PREPROCESSING, XD_ADAPTER, "Schema already exists. Name=" + refSchemaFileName);
        }
    }

    private ArrayList<XmlSchema> initializeSchemas(final XmlSchema[] schemas, final XmlSchema rootSchema, final String xDefName) {
        XsdLogger.printG(LOG_INFO, XD_ADAPTER, "====================");
        XsdLogger.printG(LOG_INFO, XD_ADAPTER, "Schemas pre-processing");
        XsdLogger.printG(LOG_INFO, XD_ADAPTER, "====================");

        final Map<XmlSchema, String> schemaNames = initializeSchemaNames(schemas, rootSchema, xDefName);
        final Map<String, Pair<String, XmlSchema>> xmlSchemaContent = new HashMap<String, Pair<String, XmlSchema>>();
        final ArrayList<XmlSchema> schemasToBeProcessed = new ArrayList<XmlSchema>();

        for (int i = 0; i < schemas.length; i++) {
            final XmlSchema schema = schemas[i];

            if (Constants.URI_2001_SCHEMA_XSD.equals(schema.getTargetNamespace())) {
                continue;
            }

            final ByteArrayOutputStream byteOS = new ByteArrayOutputStream();
            final String schemaName = schemaNames.get(schema);

            try {
                schema.write(byteOS);
                final String xsdStr = new String(byteOS.toByteArray());

                final Pair<String, XmlSchema> contentInfo = xmlSchemaContent.get(xsdStr);
                if (contentInfo == null) {
                    xmlSchemaContent.put(xsdStr, new Pair<String, XmlSchema>(schemaName, schema));
                    schemasToBeProcessed.add(schema);
                    XsdLogger.print(LOG_DEBUG, PREPROCESSING, XD_ADAPTER, "Add schema to be processed. Name=" + schemaName);
                } else {
                    if (rootSchema.equals(schema)) {
                        schemasToBeProcessed.remove(contentInfo.getValue());
                        XsdLogger.print(LOG_DEBUG, PREPROCESSING, XD_ADAPTER, "Remove schema from processing. Name=" + contentInfo.getKey());
                        xmlSchemaContent.put(xsdStr, new Pair<String, XmlSchema>(schemaName, schema));
                        schemasToBeProcessed.add(schema);
                        XsdLogger.print(LOG_DEBUG, PREPROCESSING, XD_ADAPTER, "Add schema to be processed. Name=" + schemaName);
                    } else {
                        xmlSchemaContent.put(xsdStr, contentInfo);
                        XsdLogger.print(LOG_DEBUG, PREPROCESSING, XD_ADAPTER, "Schema is already defined. Name=" + schemaName + ", OriginalName=" + contentInfo.getKey());
                    }
                }

                adapterCtx.addXmlSchemaName(schema, schemaName);
            } catch (UnsupportedEncodingException e) {
                XsdLogger.print(LOG_ERROR, PREPROCESSING, XD_ADAPTER, "Unsuccessful loading of XSD schema. Name=" + schemaName);
            }
        }

        XsdLogger.print(LOG_INFO, PREPROCESSING, XD_ADAPTER, "Schemas to be processed." +
                " Count=" + schemasToBeProcessed.size() + ", InputCount=" + (rootSchema.getParent() != null ? schemas.length - 1 : schemas.length));

        return schemasToBeProcessed;
    }

    private void initializeNamespaces(ArrayList<XmlSchema> schemaToBeProcessed) {
        XsdLogger.printG(LOG_INFO, XD_ADAPTER, "====================");
        XsdLogger.printG(LOG_INFO, XD_ADAPTER, "Namespace initialization");
        XsdLogger.printG(LOG_INFO, XD_ADAPTER, "====================");

        for (XmlSchema schema : schemaToBeProcessed) {
            final String xDefName = adapterCtx.getXmlSchemaName(schema);
            final Pair<String, String> targetNamespace = getTargetNamespace(schema);
            if (targetNamespace != null) {
                adapterCtx.addTargetNamespace(xDefName, targetNamespace);
            } else {
                adapterCtx.addTargetNamespace(xDefName, new Pair<String, String>("", ""));
            }

            final NodeNamespaceContext namespaceCtx = (NodeNamespaceContext)schema.getNamespaceContext();
            for (String prefix : namespaceCtx.getDeclaredPrefixes()) {
                if (XdNamespaceUtils.isDefaultNamespacePrefix(prefix)) {
                    continue;
                }

                final String uri = namespaceCtx.getNamespaceURI(prefix);
                adapterCtx.addNamespace(xDefName, prefix, uri);
            }
        }
    }

    private Pair<String, String> getTargetNamespace(final XmlSchema schema) {
        if (schema.getTargetNamespace() == null || schema.getTargetNamespace().isEmpty()) {
            return null;
        }

        final NamespacePrefixList namespaceCtx = schema.getNamespaceContext();
        final String nsPrefix = namespaceCtx.getPrefix(schema.getTargetNamespace());
        return new Pair<String, String>(nsPrefix, schema.getTargetNamespace());
    }

    private void addNamespaces(final Element xdRootElem, final String xDefName) {
        Map<String, String> namespaces = adapterCtx.getNamespaces(xDefName);
        if (namespaces != null && !namespaces.isEmpty()) {
            for (Map.Entry<String, String> namespace : namespaces.entrySet()) {
                if (!namespace.getValue().isEmpty()) {
                    Xsd2XdUtils.addAttribute(xdRootElem, Constants.XMLNS_ATTRIBUTE + ":" + namespace.getValue(), namespace.getKey());
                } else {
                    Xsd2XdUtils.addAttribute(xdRootElem, Constants.XMLNS_ATTRIBUTE, namespace.getKey());
                }
            }
        }
    }

    private Element createXdPool() {
        XsdLogger.print(LOG_INFO, TRANSFORMATION, XD_ADAPTER, "Creating x-definition pool ...");
        final Document doc = elementFactory.createPool();
        elementFactory.setDoc(doc);
        return doc.getDocumentElement();
    }

    private Element createXDefRoot(final String schemaName, final String rootElements) {
        final Document doc = elementFactory.createRootXdefinition(schemaName, rootElements);
        elementFactory.setDoc(doc);
        return doc.getDocumentElement();
    }

    private Element createXDef(final String schemaName, final XmlSchema schema, final boolean pool) {
        XsdLogger.printG(LOG_INFO, XD_ADAPTER, "====================");
        XsdLogger.printG(LOG_INFO, XD_ADAPTER, "Creating x-definition. Name=" + schemaName);
        XsdLogger.printG(LOG_INFO, XD_ADAPTER, "====================");

        final Xsd2XdTreeAdapter treeAdapter = new Xsd2XdTreeAdapter(schemaName, schema, elementFactory, adapterCtx);
        final String rootElements = treeAdapter.loadXsdRootNames(schema.getElements());
        final Element xdDefRootElem = pool ? elementFactory.createXDefinition(schemaName, rootElements) : createXDefRoot(schemaName, rootElements);

        addNamespaces(xdDefRootElem, schemaName);
        transformXsdTree(treeAdapter, xdDefRootElem, schemaName, schema);
        return xdDefRootElem;
    }

}
