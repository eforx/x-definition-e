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

import static org.xdef.impl.util.conv.schema.util.XsdLoggerDefs.LOG_INFO;
import static org.xdef.impl.util.conv.schema.util.XsdLoggerDefs.LOG_WARN;
import static org.xdef.impl.util.conv.xd2schema.xsd.definition.AlgPhase.INITIALIZATION;
import static org.xdef.impl.util.conv.xd2schema.xsd.definition.AlgPhase.TRANSFORMATION;

public class Xsd2XDefAdapter extends AbstractXsd2XdAdapter implements Schema2XDefAdapter<XmlSchemaCollection> {

    /**
     * Input schema used for transformation
     */
    private XmlSchema schema = null;

    /**
     * Output x-definition name
     */
    private String xDefName = null;

    @Override
    public XMDefinition createCompiledXDefinition(final XmlSchemaCollection schemaCollection) {
        return null;
    }

    @Override
    public String createXDefinition(final XmlSchemaCollection schemaCollection, final String xDefName) {
        this.xDefName = xDefName;
        final XmlSchema[] schemas = schemaCollection.getXmlSchemas();

        if (schemas.length <= 0) {
            XsdLogger.print(LOG_WARN, INITIALIZATION, this.xDefName, "Input XSD schema is empty!");
            return "";
        }

        adapterCtx = new XdAdapterCtx(features);
        adapterCtx.init();

        Document doc;
        if (schemas.length > 2) {
            final XdElementFactory elementFactory = new XdElementFactory(adapterCtx);
            doc = elementFactory.createPool();
            // TODO: multiple schemas
            return elementFactory.createHeader();
        } else {
            schema = schemas[0];
            final XdElementFactory elementFactory = new XdElementFactory(adapterCtx);
            final Xsd2XdTreeAdapter treeAdapter = new Xsd2XdTreeAdapter(this.xDefName, schema, elementFactory, adapterCtx);
            initializeNamespaces();
            final String rootElements = treeAdapter.loadXsdRootNames(schema.getElements());
            // TODO: x-definition name
            doc = elementFactory.createRootXdefinition(this.xDefName, rootElements);
            elementFactory.setDoc(doc);
            final Element xdRootElem = doc.getDocumentElement();
            addNamespaces(xdRootElem);
            transformXsdTree(treeAdapter, xdRootElem);
            return elementFactory.createHeader() + KXmlUtils.nodeToString(xdRootElem, true);
        }
    }

    private void transformXsdTree(final Xsd2XdTreeAdapter treeAdapter, final Element xdElem) {
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

    private void initializeNamespaces() {
        final Pair<String, String> targetNamespace = getTargetNamespace();
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


    private Pair<String, String> getTargetNamespace() {
        if (schema.getTargetNamespace() == null || schema.getTargetNamespace().isEmpty()) {
            return null;
        }

        final NamespacePrefixList namespaceCtx = schema.getNamespaceContext();
        final String nsPrefix = namespaceCtx.getPrefix(schema.getTargetNamespace());
        return new Pair<String, String>(nsPrefix, schema.getTargetNamespace());
    }

    private void addNamespaces(final Element xdRootElem) {
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
