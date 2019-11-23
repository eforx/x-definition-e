package org.xdef.impl.util.conv.xd2schemas.xsd.factory;

import javafx.util.Pair;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.apache.ws.commons.schema.constants.Constants;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.xdef.impl.XDefinition;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.XsdAdapterCtx;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdNamespaceUtils;
import org.xdef.model.XMNode;

import java.util.Map;

import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.AlgPhase.INITIALIZATION;
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XD2XsdDefinitions.XSD_DEFAULT_SCHEMA_NAMESPACE_PREFIX;
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XsdLoggerDefs.*;

/**
 * Creates and initialize XSD schema
 */
public class XsdSchemaFactory {

    private final XsdAdapterCtx adapterCtx;

    public XsdSchemaFactory(XsdAdapterCtx adapterCtx) {
        this.adapterCtx = adapterCtx;
    }

    /**
     * Creates and initialize XSD schema
     * @param xDef              source x-definition
     * @param targetNamespace   target namespace (prefix, URI) of input x-definition
     * @return empty initialized XSD schema
     */
    public XmlSchema createXsdSchema(final XDefinition xDef, Pair<String, String> targetNamespace) {
        XsdLogger.printP(LOG_INFO, INITIALIZATION, xDef, "Initialize xsd schema.");

        if (targetNamespace != null) {
            XsdLogger.printP(LOG_INFO, INITIALIZATION, xDef, "Creating XSD schema. " +
                    "systemName=" + xDef.getName() + ", targetNamespacePrefix=" + targetNamespace.getKey() + ", targetNamespaceUri=" + targetNamespace.getValue());
        } else {
            XsdLogger.printP(LOG_INFO, INITIALIZATION, xDef, "Creating XSD schema. SystemName=" + xDef.getName());
            targetNamespace = new Pair<String, String>(null, null);
        }

        adapterCtx.addSchemaName(xDef.getName());

        final XmlSchema xmlSchema = new XmlSchema(targetNamespace.getValue(), xDef.getName(), adapterCtx.getXmlSchemaCollection());
        initSchemaNamespace(xmlSchema, xDef, targetNamespace);
        initSchemaFormDefault(xmlSchema, xDef, targetNamespace);
        return xmlSchema;
    }

    /**
     * Initialize XSD schema namespace context
     * @param xmlSchema         XSD schema to be initialized
     * @param xDef              source x-definition
     * @param targetNamespace   XSD schema target namespace (prefix, URI)
     */
    private void initSchemaNamespace(final XmlSchema xmlSchema, final XDefinition xDef, final Pair<String, String> targetNamespace) {
        XsdLogger.printP(LOG_DEBUG, INITIALIZATION, xDef, "Initializing namespace context ...");

        final String targetNsPrefix = targetNamespace.getKey();
        final String targetNsUri = targetNamespace.getValue();

        if (targetNamespace.getKey() != null && targetNamespace.getValue() != null) {
            xmlSchema.setSchemaNamespacePrefix(targetNamespace.getKey());
        }

        NamespaceMap namespaceCtx = new NamespaceMap();
        // Default XSD namespace with prefix xs
        namespaceCtx.add(XSD_DEFAULT_SCHEMA_NAMESPACE_PREFIX, Constants.URI_2001_SCHEMA_XSD);

        if (targetNsPrefix != null && targetNsUri != null) {
            XsdNamespaceUtils.addNamespaceToCtx(namespaceCtx, xDef.getName(), targetNsPrefix, targetNsUri, INITIALIZATION);
        }

        for (Map.Entry<String, String> entry : xDef._namespaces.entrySet()) {
            final String nsPrefix = entry.getKey();
            final String nsUri = entry.getValue();

            if (XsdNamespaceUtils.isDefaultNamespacePrefix(nsPrefix) || (targetNsPrefix != null && nsPrefix.equals(targetNsPrefix))) {
                continue;
            }

            if (!namespaceCtx.containsKey(nsPrefix)) {
                XsdNamespaceUtils.addNamespaceToCtx(namespaceCtx, xDef.getName(), nsPrefix, nsUri, INITIALIZATION);
            } else {
                XsdLogger.printP(LOG_WARN, INITIALIZATION, xDef, "Namespace has been already defined! Prefix=" + nsPrefix + ", Uri=" + nsUri);
            }
        }

        xmlSchema.setNamespaceContext(namespaceCtx);
    }

    /**
     * Sets attributeFormDefault and elementFormDefault to XSD schema
     * @param xmlSchema         XSD schema to be initialized
     * @param xDef              source x-definition
     * @param targetNamespace   XSD schema target namespace (prefix, URI)
     */
    private void initSchemaFormDefault(final XmlSchema xmlSchema, final XDefinition xDef, final Pair<String, String> targetNamespace) {
        final XmlSchemaForm elemSchemaForm = getElemDefaultForm(xDef, targetNamespace.getKey());
        xmlSchema.setElementFormDefault(elemSchemaForm);

        final XmlSchemaForm attrSchemaForm = getAttrDefaultForm(xDef, targetNamespace.getKey());
        xmlSchema.setAttributeFormDefault(attrSchemaForm);

        XsdLogger.printP(LOG_DEBUG, INITIALIZATION, xDef, "Setting element default schema form. Form=" + elemSchemaForm);
        XsdLogger.printP(LOG_DEBUG, INITIALIZATION, xDef, "Setting attribute default schema form. Form=" + attrSchemaForm);
    }

    /**
     * Determines default element schema form for XSD schema
     * @param xDef              input x-definition
     * @param targetNsPrefix    x-definition target namespace prefix
     * @return schema form
     */
    private XmlSchemaForm getElemDefaultForm(final XDefinition xDef, final String targetNsPrefix) {
        if (targetNsPrefix != null && targetNsPrefix.trim().isEmpty()) {
            XsdLogger.printP(LOG_DEBUG, INITIALIZATION, xDef, "Target namespace prefix is empty. Element default form will be Qualified");
            return XmlSchemaForm.QUALIFIED;
        }

        if (xDef._rootSelection != null && xDef._rootSelection.size() > 0) {
            for (XNode xn : xDef._rootSelection.values()) {
                if (xn.getKind() == XNode.XMELEMENT) {
                    XElement defEl = (XElement)xn;
                    String tmpNs = XsdNamespaceUtils.getNamespacePrefix(defEl.getName());
                    if (tmpNs == null && defEl.getReferencePos() != null) {
                        tmpNs = XsdNamespaceUtils.getSystemIdFromXPos(defEl.getReferencePos());
                    }
                    if (tmpNs != null && tmpNs.equals(targetNsPrefix)) {
                        XsdLogger.printP(LOG_DEBUG, INITIALIZATION, xDef, "Some of root element has different namespace prefix. Element default form will be Qualified. ExpectedPrefix=" + targetNsPrefix);
                        return XmlSchemaForm.QUALIFIED;
                    }
                }
            }
        }

        XsdLogger.printP(LOG_DEBUG, INITIALIZATION, xDef, "All root elements have same namespace prefix. Element default form will be Unqualified");
        return XmlSchemaForm.UNQUALIFIED;
    }

    /**
     * Determines default attribute schema form for XSD schema
     * @param xDef              input x-definition
     * @param targetNsPrefix    x-definition target namespace prefix
     * @return schema form
     */
    private XmlSchemaForm getAttrDefaultForm(final XDefinition xDef, final String targetNsPrefix) {
        if (xDef._rootSelection != null && xDef._rootSelection.size() > 0) {
            for (XNode xn : xDef._rootSelection.values()) {
                if (xn.getKind() == XNode.XMELEMENT) {
                    XElement defEl = (XElement)xn;
                    for (XMNode attr : defEl.getXDAttrs()) {
                        String tmpNs = XsdNamespaceUtils.getNamespacePrefix(attr.getName());
                        if (tmpNs != null && tmpNs.equals(targetNsPrefix)) {
                            XsdLogger.printP(LOG_DEBUG, INITIALIZATION, xDef, "Some of root attribute has different namespace prefix. Attribute default form will be Qualified. ExpectedPrefix=" + targetNsPrefix);
                            return XmlSchemaForm.QUALIFIED;
                        }
                    }
                }
            }
        }

        XsdLogger.printP(LOG_DEBUG, INITIALIZATION, xDef, "All root attributes have same namespace prefix. Attribute default form will be Unqualified");
        return XmlSchemaForm.UNQUALIFIED;
    }
}
