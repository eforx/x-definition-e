package org.xdef.impl.util.conv.xd2schemas.xsd.factory;

import javafx.util.Pair;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.xdef.impl.XDefinition;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.XsdAdapterCtx;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdNamespaceUtils;
import org.xdef.model.XMNode;

import static org.xdef.impl.util.conv.xd2schemas.xsd.util.AlgPhase.INITIALIZATION;
import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.LOG_DEBUG;
import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.LOG_INFO;

public class XsdSchemaFactory {

    private final XsdAdapterCtx adapterCtx;

    public XsdSchemaFactory(XsdAdapterCtx adapterCtx) {
        this.adapterCtx = adapterCtx;
    }

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

        XmlSchema xmlSchema = new XmlSchema(targetNamespace.getValue(), xDef.getName(), adapterCtx.getXmlSchemaCollection());

        initSchemaNamespace(xmlSchema, xDef, targetNamespace);
        initSchemaFormDefault(xmlSchema, xDef, targetNamespace);
        return xmlSchema;
    }

    /**
     * Initialize xsd schema namespace
     */
    private void initSchemaNamespace(final XmlSchema xmlSchema, final XDefinition xDef, final Pair<String, String> targetNamespace) {
        XsdLogger.printP(LOG_DEBUG, INITIALIZATION, xDef, "Initializing namespace context ...");

        if (targetNamespace.getKey() != null && targetNamespace.getValue() != null) {
            xmlSchema.setSchemaNamespacePrefix(targetNamespace.getKey());
        }

        NamespaceMap namespaceCtx = XsdNamespaceUtils.createCtx();
        XsdNamespaceUtils.initCtx(namespaceCtx, xDef, targetNamespace.getKey(), targetNamespace.getValue());

        xmlSchema.setNamespaceContext(namespaceCtx);
    }

    /**
     * Sets attributeFormDefault and elementFormDefault
     */
    private void initSchemaFormDefault(final XmlSchema xmlSchema, final XDefinition xDef, final Pair<String, String> targetNamespace) {
        XmlSchemaForm elemSchemaForm = getElemDefaultForm(xDef, targetNamespace.getKey());
        xmlSchema.setElementFormDefault(elemSchemaForm);

        XmlSchemaForm attrSchemaForm = getAttrDefaultForm(xDef, targetNamespace.getKey());
        xmlSchema.setAttributeFormDefault(attrSchemaForm);

        XsdLogger.printP(LOG_DEBUG, INITIALIZATION, xDef, "Setting element default schema form. Form=" + elemSchemaForm);
        XsdLogger.printP(LOG_DEBUG, INITIALIZATION, xDef, "Setting attribute default schema form. Form=" + attrSchemaForm);
    }

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
                        tmpNs = XsdNamespaceUtils.getReferenceSystemId(defEl.getReferencePos());
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
