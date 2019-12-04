package org.xdef.impl.util.conv.schema.schema2xd.factory;

import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xdef.impl.util.conv.schema.util.SchemaLogger;
import org.xdef.impl.util.conv.schema.schema2xd.model.XdAdapterCtx;
import org.xdef.impl.util.conv.schema.schema2xd.util.XdNameUtils;
import org.xdef.impl.util.conv.schema.schema2xd.util.XdNamespaceUtils;
import org.xdef.xml.KXmlUtils;

import javax.xml.namespace.QName;

import static org.xdef.impl.util.conv.schema.util.SchemaLoggerDefs.LOG_INFO;
import static org.xdef.impl.util.conv.schema.util.SchemaLoggerDefs.LOG_WARN;
import static org.xdef.impl.util.conv.schema.schema2xd.definition.Xsd2XdDefinitions.*;
import static org.xdef.impl.util.conv.schema.xd2schema.definition.AlgPhase.TRANSFORMATION;

public class XdElementFactory {

    private final XdAdapterCtx adapterCtx;

    /**
     * Output x-definition document
     */
    private Document doc;

    public XdElementFactory(XdAdapterCtx adapterCtx) {
        this.adapterCtx = adapterCtx;
    }

    public void setDoc(Document doc) {
        this.doc = doc;
    }

    public String createHeader() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n";
    }

    public Document createPool() {
        return KXmlUtils.newDocument(XD_NAMESPACE_URI, XD_ELEM_POOL, null);
    }

    public Document createRootXdefinition(final String xDefName, final String rootElements) {
        SchemaLogger.print(LOG_INFO, TRANSFORMATION, xDefName, "Root x-definition node");
        final Document res = KXmlUtils.newDocument(XD_NAMESPACE_URI, XD_ELEM_XDEF, null);
        final Element root = res.getDocumentElement();
        XdAttributeFactory.addAttr(root, XD_ATTR_NAME, xDefName);
        if (rootElements != null && !rootElements.isEmpty()) {
            XdAttributeFactory.addAttr(root, XD_ATTR_ROOT_ELEMT, rootElements);
        }
        return res;
    }

    public Element createXDefinition(final String xDefName, final String rootElements) {
        SchemaLogger.print(LOG_INFO, TRANSFORMATION, xDefName, "X-definition node");
        final Element xdDef = doc.createElementNS(XD_NAMESPACE_URI, XD_ELEM_XDEF);
        XdAttributeFactory.addAttr(xdDef, XD_ATTR_NAME, xDefName);
        if (rootElements != null && !rootElements.isEmpty()) {
            XdAttributeFactory.addAttr(xdDef, XD_ATTR_ROOT_ELEMT, rootElements);
        }
        return xdDef;
    }

    public Element createElement(final XmlSchemaElement xsdElem, final String xDefName) {
        if (xsdElem.isRef()) {
            final QName xsdQName = xsdElem.getRef().getTargetQName();
            if (xsdQName != null) {
                final Element xdElem = doc.createElementNS(xsdQName.getNamespaceURI(), XdNameUtils.createQualifiedName(xsdQName));
                final String refXDef = XdNamespaceUtils.getReferenceSchemaName(xsdElem.getParent().getParent(), xsdQName, adapterCtx, false);
                XdAttributeFactory.addAttrRefInDiffXDef(xdElem, refXDef, xsdQName);
                return xdElem;
            } else {
                SchemaLogger.printP(LOG_WARN, TRANSFORMATION, xsdElem, "Unknown element reference QName!");
            }
        } else {
            final QName xsdQName = xsdElem.getQName();
            if (xsdQName == null) {
                return doc.createElement(xsdElem.getName());
            } else if (xsdQName.getNamespaceURI() != null && !XmlSchemaForm.UNQUALIFIED.equals(xsdElem.getForm())) {
                final String qualifiedName = XdNameUtils.createQualifiedName(xsdQName, xDefName, adapterCtx);
                return doc.createElementNS(xsdQName.getNamespaceURI(), qualifiedName);
            }
        }

        return doc.createElement(xsdElem.getName());
    }

    public Element createEmptyElement(final XmlSchemaComplexType xsdComplex, final String xDefName) {
        final QName xsdQName = xsdComplex.getQName();
        if (xsdQName.getNamespaceURI() != null) {
            final String qualifiedName = XdNameUtils.createQualifiedName(xsdQName, xDefName, adapterCtx);
            return doc.createElementNS(xsdQName.getNamespaceURI(), qualifiedName);
        } else {
            return doc.createElement(xsdQName.getLocalPart());
        }
    }

    public Element createEmptyDeclaration() {
        return doc.createElementNS(XD_NAMESPACE_URI, XD_ELEM_DECLARATION);
    }

    public Element createEmptySequence() {
        return doc.createElementNS(XD_NAMESPACE_URI, XD_ELEM_SEQUENCE);
    }

    public Element createEmptyChoice() {
        return doc.createElementNS(XD_NAMESPACE_URI, XD_ELEM_CHOICE);
    }

    public Element createEmptyMixed() {
        return doc.createElementNS(XD_NAMESPACE_URI, XD_ELEM_MIXED);
    }

    public Element createEmptyNamedMixed(final String name) {
        final Element elem = doc.createElementNS(XD_NAMESPACE_URI, XD_ELEM_MIXED);
        XdAttributeFactory.addAttr(elem, XD_ATTR_NAME, name);
        return elem;
    }

    public Element createEmptyAny() {
        return doc.createElementNS(XD_NAMESPACE_URI, XD_ELEM_ANY);
    }

    public Element createTextRef() {
        return doc.createElement(XD_ELEM_TEXT_REF);
    }
}
