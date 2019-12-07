package org.xdef.impl.util.conv.schema.xd2schema.xsd;

import javafx.util.Pair;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.xdef.XDPool;
import org.xdef.impl.XDefinition;
import org.xdef.impl.XElement;
import org.xdef.impl.util.conv.schema.util.SchemaLogger;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.adapter.AbstractXd2XsdAdapter;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.adapter.Xd2XsdPostProcessingAdapter;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.adapter.Xd2XsdReferenceAdapter;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.adapter.Xd2XsdTreeAdapter;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.factory.XsdNodeFactory;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.factory.XsdSchemaFactory;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.model.XsdAdapterCtx;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.util.XsdNamespaceUtils;
import org.xdef.model.XMDefinition;

import java.util.Set;

import static org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.AlgPhase.INITIALIZATION;
import static org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.AlgPhase.TRANSFORMATION;
import static org.xdef.impl.util.conv.schema.util.SchemaLoggerDefs.LOG_INFO;
import static org.xdef.impl.util.conv.schema.util.SchemaLoggerDefs.XSD_XDEF_ADAPTER;

/**
 * Transformation of given x-definition or x-definition pool to collection of XSD documents
 */
public class XDef2XsdAdapter extends AbstractXd2XsdAdapter implements XDef2SchemaAdapter<XmlSchemaCollection> {

    /**
     * Input x-definition used for transformation
     */
    private XDefinition xDefinition = null;

    /**
     * Output XSD document
     */
    private XmlSchema schema = null;

    @Override
    public XmlSchemaCollection createSchema(final XDPool xdPool) {
        if (xdPool == null) {
            throw new IllegalArgumentException("xdPool = null");
        }
        return createSchema(xdPool.getXMDefinition());
    }

    @Override
    public XmlSchemaCollection createSchema(final XMDefinition xDef) {
        if (xDef == null) {
            throw new IllegalArgumentException("xdef = null");
        }

        SchemaLogger.printG(LOG_INFO, XSD_XDEF_ADAPTER, "====================");
        SchemaLogger.printG(LOG_INFO, XSD_XDEF_ADAPTER, "Transforming x-definition. Name=" + xDef.getName());
        SchemaLogger.printG(LOG_INFO, XSD_XDEF_ADAPTER, "====================");

        boolean poolPostProcessing = true;

        this.xDefinition = (XDefinition)xDef;
        if (adapterCtx == null) {
            adapterCtx = new XsdAdapterCtx(features);
            adapterCtx.init();
            schema = createXsdSchema();
            poolPostProcessing = false;
        } else {
            schema = adapterCtx.findSchema(xDef.getName(), false, INITIALIZATION);
        }

        final XsdNodeFactory xsdFactory = new XsdNodeFactory(schema, adapterCtx);
        final Xd2XsdTreeAdapter treeAdapter = new Xd2XsdTreeAdapter(schema, xDef.getName(), xsdFactory, adapterCtx);
        final Xd2XsdReferenceAdapter referenceAdapter = new Xd2XsdReferenceAdapter(schema, xDef.getName(), xsdFactory, treeAdapter, adapterCtx);

        treeAdapter.loadXdefRootNames(xDefinition);
        treeAdapter.loadXdefRootUniqueSets(xDefinition);
        referenceAdapter.createRefsAndImports(xDefinition);
        transformXdef(treeAdapter);

        if (!poolPostProcessing) {
            final Xd2XsdPostProcessingAdapter postProcessingAdapter = new Xd2XsdPostProcessingAdapter();
            postProcessingAdapter.setAdapterCtx(adapterCtx);
            postProcessingAdapter.process(xDefinition);
        }

        return adapterCtx.getXmlSchemaCollection();
    }

    /**
     * Transform x-definition tree to XSD document via treeAdapter
     * @param treeAdapter   transformation algorithm
     */
    private void transformXdef(final Xd2XsdTreeAdapter treeAdapter) {
        SchemaLogger.printP(LOG_INFO, TRANSFORMATION, xDefinition, "*** Transformation of x-definition tree ***");

        final Set<String> rootNodeNames = adapterCtx.getSchemaRootNodeNames(xDefinition.getName());

        if (rootNodeNames != null) {
            for (XElement elem : xDefinition.getXElements()) {
                if (rootNodeNames.contains(elem.getName())) {
                    treeAdapter.convertTree(elem);
                    SchemaLogger.printP(LOG_INFO, TRANSFORMATION, elem, "Adding root element to schema. Element=" + elem.getName());
                }
            }
        }
    }

    /**
     * Creates and initialize XSD document
     */
    private XmlSchema createXsdSchema() {
        Pair<String, String> targetNamespace = XsdNamespaceUtils.getSchemaTargetNamespace(xDefinition);

        SchemaLogger.printP(LOG_INFO, INITIALIZATION, xDefinition, "Creating XSD document. " +
                "systemName=" + xDefinition.getName() + ", targetNamespacePrefix=" + targetNamespace.getKey() + ", targetNamespaceUri=" + targetNamespace.getValue());

        XsdSchemaFactory schemaFactory = new XsdSchemaFactory(adapterCtx);
        return schemaFactory.createXsdSchema(xDefinition, targetNamespace);
    }

}
