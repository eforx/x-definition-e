package org.xdef.impl.util.conv.schema2xd.xsd;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xdef.impl.util.conv.schema.util.XsdLogger;
import org.xdef.impl.util.conv.schema2xd.Schema2XDefAdapter;
import org.xdef.impl.util.conv.schema2xd.xsd.adapter.AbstractXsd2XdAdapter;
import org.xdef.impl.util.conv.schema2xd.xsd.adapter.Xsd2XdTreeAdapter;
import org.xdef.impl.util.conv.schema2xd.xsd.definition.Xsd2XdFeature;
import org.xdef.impl.util.conv.schema2xd.xsd.factory.XdElementFactory;
import org.xdef.impl.util.conv.schema2xd.xsd.model.XdAdapterCtx;
import org.xdef.model.XMDefinition;
import org.xdef.xml.KXmlUtils;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

        final XdElementFactory elementFactory = new XdElementFactory(adapterCtx);
        final Xsd2XdTreeAdapter treeAdapter = new Xsd2XdTreeAdapter(this.xDefName, elementFactory, adapterCtx);

        Document doc;
        if (schemas.length > 2) {
            doc = elementFactory.createPool();
            // TODO: multiple schemas
            return elementFactory.createHeader();
        } else {
            schema = schemas[0];
            final String rootElements = treeAdapter.loadXsdRootNames(schema.getElements());
            // TODO: x-definition name
            doc = elementFactory.createRootXdefinition(this.xDefName, rootElements);
            treeAdapter.setDoc(doc);
            elementFactory.setDoc(doc);
            final Element xdRootElem = doc.getDocumentElement();
            transformXSdTree(treeAdapter, xdRootElem);
            return elementFactory.createHeader() + KXmlUtils.nodeToString(xdRootElem, true);
        }
    }

    private void transformXSdTree(final Xsd2XdTreeAdapter treeAdapter, final Element xdElem) {
        XsdLogger.print(LOG_INFO, TRANSFORMATION, xDefName, "*** Transformation of XSD tree ***");

        final Map<QName, XmlSchemaType> schemaTypeMap = schema.getSchemaTypes();
        if (schemaTypeMap != null) {
            for (XmlSchemaType xsdSchemaType : schemaTypeMap.values()) {
                final Node res = treeAdapter.convertTree(xsdSchemaType, true);
                if (res != null) {
                    xdElem.appendChild(res);
                }
            }
        }

        final Map<QName, XmlSchemaElement> elementMap = schema.getElements();

        if (elementMap != null) {
            for (XmlSchemaElement xsdElem : elementMap.values()) {
                final Node res = treeAdapter.convertTree(xsdElem, true);
                if (res != null) {
                    xdElem.appendChild(res);
                }
            }
        }
    }

}
