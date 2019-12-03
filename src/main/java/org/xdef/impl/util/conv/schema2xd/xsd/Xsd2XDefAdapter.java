package org.xdef.impl.util.conv.schema2xd.xsd;

import javafx.util.Pair;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaGroup;
import org.apache.ws.commons.schema.XmlSchemaType;
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
import org.xdef.impl.util.conv.schema2xd.xsd.util.XdNamespaceUtils;
import org.xdef.impl.util.conv.schema2xd.xsd.util.Xsd2XdUtils;
import org.xdef.model.XMDefinition;
import org.xdef.xml.KXmlUtils;

import javax.xml.namespace.QName;
import java.util.Map;

import static org.xdef.impl.util.conv.schema.util.XsdLoggerDefs.*;
import static org.xdef.impl.util.conv.xd2schema.xsd.definition.AlgPhase.*;

public class Xsd2XDefAdapter extends AbstractXsd2XdAdapter implements Schema2XDefAdapter<XmlSchema> {

    private XdElementFactory elementFactory;

    @Override
    public XMDefinition createCompiledXDefinition(final XmlSchema rootSchema) {
        return null;
    }

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
        Element xdRootElem;

        int schemaCount = 0;
        boolean realXsdSchemaRootFound = false;
        for (int i = 0; i < schemas.length; i++) {
            if (Constants.URI_2001_SCHEMA_XSD.equals(schemas[i].getTargetNamespace())) {
                break;
            }

            if (rootSchema.equals(schemas[i])) {
                realXsdSchemaRootFound = true;
            }

            schemaCount++;
        }

        XmlSchema realRootSchema = null;
        if (!realXsdSchemaRootFound && rootSchema.getParent() != null && rootSchema.getTargetNamespace() != null) {
            realRootSchema = rootSchema.getParent().schemaForNamespace(rootSchema.getTargetNamespace());
            schemaCount++;
        } else if (realXsdSchemaRootFound) {
            realRootSchema = rootSchema;
        }

        if (schemaCount == 0) {
            XsdLogger.print(LOG_ERROR, INITIALIZATION, xDefName, "All input XSD schemas are default!");
            return "";
        }

        if (realRootSchema == null) {
            XsdLogger.print(LOG_ERROR, INITIALIZATION, xDefName, "Real root of XSD schema collection has not been found!");
            return "";
        }

        XsdLogger.print(LOG_DEBUG, INITIALIZATION, xDefName, "Total input schemas: " + schemaCount);

        initializeNamespaces(schemas, xDefName, realRootSchema);

        if (schemaCount > 1) {
            xdRootElem = createXdPool();

            // First transform root xsd
            xdRootElem.appendChild(createXDef(xDefName, realRootSchema, true));

            int j = 1;
            for (int i = 0; i < schemas.length; i++) {
                final XmlSchema schema = schemas[i];
                if (Constants.URI_2001_SCHEMA_XSD.equals(schema.getTargetNamespace())) {
                    break;
                }

                if (realRootSchema.equals(schema)) {
                    continue;
                }

                final String schemaName = xDefName + "_" + j;
                xdRootElem.appendChild(createXDef(schemaName, schema, true));
                j++;
            }
        } else {
            XsdLogger.print(LOG_INFO, PREPROCESSING, xDefName, "Creating x-definition. Name=" + xDefName);
            xdRootElem = createXDef(xDefName, realRootSchema, false);
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

    private void initializeNamespaces(final XmlSchema[] schemas, final String xDefName, final XmlSchema rootSchema) {
        XsdLogger.printG(LOG_INFO, XD_ADAPTER, "====================");
        XsdLogger.printG(LOG_INFO, XD_ADAPTER, "Namespace initialization");
        XsdLogger.printG(LOG_INFO, XD_ADAPTER, "====================");

        initializeNamespaces(xDefName, rootSchema);

        int j = 1;
        for (int i = 0; i < schemas.length; i++) {
            final XmlSchema schema = schemas[i];
            if (Constants.URI_2001_SCHEMA_XSD.equals(schema.getTargetNamespace())) {
                break;
            }

            if (rootSchema.equals(schema)) {
                continue;
            }

            final String schemaName = xDefName + "_" + j;
            initializeNamespaces(schemaName, schema);
            j++;
        }
    }

    private void initializeNamespaces(final String xDefName, final XmlSchema schema) {
        final Pair<String, String> targetNamespace = getTargetNamespace(schema);
        if (targetNamespace != null) {
            adapterCtx.addTargetNamespace(xDefName, targetNamespace);
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
        XsdLogger.printG(LOG_INFO, XD_ADAPTER, "Creating x-definition pool ...");
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
