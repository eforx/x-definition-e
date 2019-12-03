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
import org.xdef.impl.util.conv.schema2xd.xsd.util.XdNamespaceUtils;
import org.xdef.impl.util.conv.schema2xd.xsd.util.Xsd2XdUtils;
import org.xdef.model.XMDefinition;
import org.xdef.xml.KXmlUtils;

import javax.xml.namespace.QName;
import java.util.Map;

import static org.xdef.impl.util.conv.schema.util.XsdLoggerDefs.*;
import static org.xdef.impl.util.conv.xd2schema.xsd.definition.AlgPhase.INITIALIZATION;
import static org.xdef.impl.util.conv.xd2schema.xsd.definition.AlgPhase.TRANSFORMATION;

public class Xsd2XDefAdapter extends AbstractXsd2XdAdapter implements Schema2XDefAdapter<XmlSchemaCollection> {

    @Override
    public XMDefinition createCompiledXDefinition(final XmlSchemaCollection schemaCollection) {
        return null;
    }

    @Override
    public String createXDefinition(final XmlSchemaCollection schemaCollection, final String xDefName) {
        final XmlSchema[] schemas = schemaCollection.getXmlSchemas();
        if (schemas == null || schemas.length <= 1) {
            XsdLogger.print(LOG_ERROR, INITIALIZATION, xDefName, "Input XSD schema collection is empty!");
            return "";
        }

        XsdLogger.print(LOG_INFO, INITIALIZATION, xDefName, "*** Initializing ***");

        adapterCtx = new XdAdapterCtx(features);
        adapterCtx.init();

        final XdElementFactory elementFactory = new XdElementFactory(adapterCtx);
        Element xdRootElem;

        if (schemas.length > 2) {
            final Document doc = elementFactory.createPool();
            elementFactory.setDoc(doc);
            xdRootElem = doc.getDocumentElement();
            for (int i = schemas.length - 2; i >= 0; i--) {
                final XmlSchema schema = schemas[i];
                final String schemaName = i == schemas.length - 2 ? xDefName : (xDefName + "_" + (i + 1));
                initializeNamespaces(schemaName, schema);
            }

            for (int i = schemas.length - 2; i >= 0; i--) {
                final XmlSchema schema = schemas[i];
                final String schemaName = i == schemas.length - 2 ? xDefName : (xDefName + "_" + (i + 1));

                final Xsd2XdTreeAdapter treeAdapter = new Xsd2XdTreeAdapter(schemaName, schema, elementFactory, adapterCtx);
                final String rootElements = treeAdapter.loadXsdRootNames(schema.getElements());

                final Element xdDefRootElem = elementFactory.createXDefinition(schemaName, rootElements);
                xdRootElem.appendChild(xdDefRootElem);
                addNamespaces(xdDefRootElem, schemaName);
                transformXsdTree(treeAdapter, xdDefRootElem, schemaName, schema);
            }
        } else {
            final XmlSchema schema = schemas[0];
            final Xsd2XdTreeAdapter treeAdapter = new Xsd2XdTreeAdapter(xDefName, schema, elementFactory, adapterCtx);
            initializeNamespaces(xDefName, schema);
            final String rootElements = treeAdapter.loadXsdRootNames(schema.getElements());
            final Document doc = elementFactory.createRootXdefinition(xDefName, rootElements);
            elementFactory.setDoc(doc);
            xdRootElem = doc.getDocumentElement();
            addNamespaces(xdRootElem, xDefName);
            transformXsdTree(treeAdapter, xdRootElem, xDefName, schema);
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

}
