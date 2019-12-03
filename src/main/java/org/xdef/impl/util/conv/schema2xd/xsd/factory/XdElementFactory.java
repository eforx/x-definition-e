package org.xdef.impl.util.conv.schema2xd.xsd.factory;

import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xdef.impl.util.conv.schema.util.XsdLogger;
import org.xdef.impl.util.conv.schema2xd.xsd.model.XdAdapterCtx;
import org.xdef.impl.util.conv.schema2xd.xsd.util.XdNameUtils;
import org.xdef.impl.util.conv.schema2xd.xsd.util.Xsd2XdUtils;
import org.xdef.xml.KXmlUtils;

import javax.xml.namespace.QName;

import static org.xdef.impl.util.conv.schema.util.XsdLoggerDefs.LOG_INFO;
import static org.xdef.impl.util.conv.schema2xd.xsd.definition.Xsd2XdDefinitions.*;
import static org.xdef.impl.util.conv.xd2schema.xsd.definition.AlgPhase.TRANSFORMATION;

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
        XsdLogger.print(LOG_INFO, TRANSFORMATION, xDefName, "Root x-definition node");
        final Document res = KXmlUtils.newDocument(XD_NAMESPACE_URI, XD_ELEM_XDEF, null);
        final Element root = res.getDocumentElement();
        Xsd2XdUtils.addAttribute(root, XD_ATTR_NAME, xDefName);
        Xsd2XdUtils.addAttribute(root, XD_ATTR_ROOT_ELEMT, rootElements);
        return res;
    }

    public Element createXDefinition(final String xDefName, final String rootElements) {
        XsdLogger.print(LOG_INFO, TRANSFORMATION, xDefName, "X-definition node");
        final Element xdDef = doc.createElementNS(XD_NAMESPACE_URI, XD_ELEM_XDEF);
        Xsd2XdUtils.addAttribute(xdDef, XD_ATTR_NAME, xDefName);
        Xsd2XdUtils.addAttribute(xdDef, XD_ATTR_ROOT_ELEMT, rootElements);
        return xdDef;
    }

    public Element createEmptyElement(final XmlSchemaElement xsdElem, final String xDefName) {
        if (xsdElem.isRef()) {
            final QName xsdQName = xsdElem.getRef().getTargetQName();
            if (xsdQName != null) {
                final Element xdElem = doc.createElementNS(xsdQName.getNamespaceURI(),  XdNameUtils.createQualifiedName(xsdQName));
                final String refXDef = adapterCtx.getXDefByNamespace(xsdQName.getNamespaceURI());
                Xsd2XdUtils.addRefInDiffXDefAttribute(xdElem, refXDef, xsdQName);
                return xdElem;
            }
        } else {
            final QName xsdQName = xsdElem.getQName();
            if (xsdQName != null && xsdQName.getNamespaceURI() != null && !XmlSchemaForm.UNQUALIFIED.equals(xsdElem.getForm())) {
                final String qualifiedName = XdNameUtils.createQualifiedName(xsdQName, xDefName, adapterCtx);
                return doc.createElementNS(xsdQName.getNamespaceURI(), qualifiedName);
            }
        }

        return doc.createElement(xsdElem.getName());
    }

    public Element createEmptyElement(final XmlSchemaComplexType xsdComplex) {
        final QName xsdQName = xsdComplex.getQName();
        if (xsdQName.getNamespaceURI() != null) {
            return doc.createElementNS(xsdQName.getNamespaceURI(), xsdComplex.getName());
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
        Xsd2XdUtils.addAttribute(elem, XD_ATTR_NAME, name);
        return elem;
    }

    public Element createEmptyAny() {
        return doc.createElementNS(XD_NAMESPACE_URI, XD_ELEM_ANY);
    }
}
