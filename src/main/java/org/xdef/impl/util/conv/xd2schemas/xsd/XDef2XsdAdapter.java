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
import org.xdef.impl.util.conv.xd2schemas.xsd.model.XsdAdapterCtx;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XD2XsdUtils;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdNamespaceUtils;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdPostProcessor;
import org.xdef.model.XMDefinition;
import org.xdef.model.XMNode;

import java.util.HashSet;
import java.util.Set;

import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.*;

public class XDef2XsdAdapter extends AbstractXd2XsdAdapter implements XDef2SchemaAdapter<XmlSchemaCollection> {

    /**
     * Input x-definition used for transformation
     */
    private XDefinition xDefinition = null;

    /**
     * Output xsd schema
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

        if (XsdLogger.isInfo(logLevel)) {
            XsdLogger.printC(INFO, XSD_XDEF_ADAPTER, "====================");
            XsdLogger.printC(INFO, XSD_XDEF_ADAPTER, "Transforming x-definition. Name=" + xDef.getName());
            XsdLogger.printC(INFO, XSD_XDEF_ADAPTER, "====================");
        }

        this.xDefinition = (XDefinition)xDef;
        if (adapterCtx == null) {
            adapterCtx = new XsdAdapterCtx(logLevel);
            adapterCtx.init();
        }

        NamespaceMap namespaceCtx = adapterCtx.getNamespaceCtx().get(xDef.getName());
        if (namespaceCtx == null) {
            namespaceCtx = XsdNamespaceUtils.createCtx();
        }

        createXsdSchema(namespaceCtx);

        XsdElementFactory xsdFactory = new XsdElementFactory(logLevel, schema);

        XDTree2XsdAdapter treeAdapter = new XDTree2XsdAdapter(logLevel, schema, xsdFactory);
        treeAdapter.initPostprocessing(null, adapterCtx.getExtraSchemaLocationsCtx());
        treeAdapter.loadXdefRootNames(xDefinition);

        XD2XsdReferenceAdapter referenceAdapter = new XD2XsdReferenceAdapter(logLevel, schema, xsdFactory, treeAdapter, adapterCtx.getSchemaLocationsCtx());
        referenceAdapter.initPostprocessing(adapterCtx.getExtraSchemaLocationsCtx(), false);
        // Extract all used references in x-definition
        referenceAdapter.createRefsAndImports(xDefinition);

        transformXdef(treeAdapter, xsdFactory);

        // Node post-processing
        if (!treeAdapter.getPostprocessedNodes().isEmpty() && !adapterCtx.getExtraSchemaLocationsCtx().isEmpty()) {
            XD2XsdPPAdapterWrapper postProcessingAdapter = new XD2XsdPPAdapterWrapper(logLevel, xDefinition);
            postProcessingAdapter.setAdapterCtx(adapterCtx);
            postProcessingAdapter.setSourceNamespaceCtx(namespaceCtx, schema.getSchemaNamespacePrefix());
            postProcessingAdapter.processNodes(treeAdapter.getPostprocessedNodes());
        }

        return adapterCtx.getXmlSchemaCollection();
    }

    /**
     * Transform x-definition tree to xsd schema via treeAdapter
     * @param treeAdapter   transformation algorithm
     */
    private void transformXdef(final XDTree2XsdAdapter treeAdapter, final XsdElementFactory xsdFactory) {
        if (XsdLogger.isInfo(logLevel)) {
            XsdLogger.printC(INFO, XSD_XDEF_ADAPTER, "Transform x-definition tree ...");
        }

        for (XElement elem : xDefinition.getXElements()) {
            if (treeAdapter.getXdRootNames().contains(elem.getName())) {
                XmlSchemaElement xsdElem = (XmlSchemaElement) treeAdapter.convertTree(elem);
                if (xsdElem.getRef().getTargetQName() == null) {
                    XD2XsdUtils.addElement(schema, xsdElem);
                    if (XsdLogger.isInfo(logLevel)) {
                        XsdLogger.printP(INFO, TRANSFORMATION, elem, "Adding root element to schema. Element=" + elem.getName());
                    }
                } else {
                    XsdPostProcessor.elemRootRef(xsdElem, elem, schema, xsdFactory);
                }
            }
        }
    }

    /**
     * Creates and initialize XSD schema
     */
    private void createXsdSchema(NamespaceMap namespaceCtx) {
        if (XsdLogger.isInfo(logLevel)) {
            XsdLogger.printP(INFO, INITIALIZATION, xDefinition, "Initialize XSD schema");
        }

        adapterCtx.addSchemaName(xDefinition.getName());

        // Target namespace
        Boolean targetNamespaceError = false;
        Pair<String, String> targetNamespace = XsdNamespaceUtils.getSchemaTargetNamespace(xDefinition, targetNamespaceError, logLevel);

        if (XsdLogger.isInfo(logLevel)) {
            XsdLogger.printP(INFO, INITIALIZATION, xDefinition, "Creating XSD schema. " +
                    "systemName=" + xDefinition.getName() + ", targetNamespacePrefix=" + targetNamespace.getKey() + ", targetNamespaceUri=" + targetNamespace.getValue());
        }

        schema = new XmlSchema(targetNamespace.getValue(), xDefinition.getName(), adapterCtx.getXmlSchemaCollection());

        initSchemaNamespace(targetNamespace, namespaceCtx);
        initSchemaFormDefault(targetNamespace);
    }

    /**
     * Initialize xsd schema namespace
     * @param targetNamespace   target namespace
     * @param namespaceCtx      xsd schema namespace context
     */
    private void initSchemaNamespace(final Pair<String, String> targetNamespace, NamespaceMap namespaceCtx) {
        if (XsdLogger.isDebug(logLevel)) {
            XsdLogger.printP(DEBUG, INITIALIZATION, xDefinition, "Initializing namespace context ...");
        }

        if (targetNamespace.getKey() != null && targetNamespace.getValue() != null) {
            schema.setSchemaNamespacePrefix(targetNamespace.getKey());
        }

        // Context contains only default namespace -> initialize it!
        if (namespaceCtx.getDeclaredPrefixes().length == 1) {
            XsdNamespaceUtils.initCtx(namespaceCtx, xDefinition, targetNamespace.getKey(), targetNamespace.getValue(), INITIALIZATION, logLevel);
        }

        schema.setNamespaceContext(namespaceCtx);
    }

    /**
     * Sets attributeFormDefault and elementFormDefault
     * @param targetNamespace   xsd schema target namespace
     */
    private void initSchemaFormDefault(final Pair<String, String> targetNamespace) {
        XmlSchemaForm elemSchemaForm = getElemDefaultForm(targetNamespace.getKey());
        schema.setElementFormDefault(elemSchemaForm);

        XmlSchemaForm attrSchemaForm = getAttrDefaultForm(targetNamespace.getKey());
        schema.setAttributeFormDefault(attrSchemaForm);

        if (XsdLogger.isDebug(logLevel)) {
            XsdLogger.printP(DEBUG, INITIALIZATION, xDefinition, "Setting element default schema form. Form=" + elemSchemaForm);
            XsdLogger.printP(DEBUG, INITIALIZATION, xDefinition, "Setting attribute default schema form. Form=" + attrSchemaForm);
        }
    }

    /**
     * Add x-definition names as namespace to namespace context
     * @param xDefs
     */
    private void addDefaultXdefinitionNamespaces(final Set<String> xDefs) {
        if (XsdLogger.isInfo(logLevel)) {
            XsdLogger.printP(INFO, PREPROCESSING, xDefinition, "Updating namespace context - add namespaces of other x-definitions");
        }

        NamespaceMap namespaceMap = (NamespaceMap)schema.getNamespaceContext();
        for (String xDefName : xDefs) {
            XsdNamespaceUtils.addNamespaceToCtx(namespaceMap, xDefinition.getName(),
                    XsdNamespaceUtils.createNsPrefixFromXDefName(xDefName), XsdNamespaceUtils.createNsUriFromXDefName(xDefName),
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
                    String tmpNs = XsdNamespaceUtils.getNamespacePrefix(defEl.getName());
                    if (tmpNs == null && defEl.getReferencePos() != null) {
                        tmpNs = XsdNamespaceUtils.getReferenceSystemId(defEl.getReferencePos());
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
                        String tmpNs = XsdNamespaceUtils.getNamespacePrefix(attr.getName());
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
