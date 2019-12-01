package org.xdef.impl.util.conv.schema2xd.xsd.util;

import org.apache.ws.commons.schema.*;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.xdef.impl.util.conv.schema.util.XsdLogger;
import org.xdef.impl.util.conv.schema2xd.xsd.definition.Xsd2XdFeature;
import org.xdef.impl.util.conv.schema2xd.xsd.factory.declaration.IDeclarationTypeFactory;
import org.xdef.impl.util.conv.schema2xd.xsd.model.XdAdapterCtx;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.xdef.impl.util.conv.schema.util.XsdLoggerDefs.LOG_DEBUG;
import static org.xdef.impl.util.conv.schema2xd.xsd.definition.Xsd2XdDefinitions.XD_ATTR_SCRIPT;
import static org.xdef.impl.util.conv.schema2xd.xsd.definition.Xsd2XdDefinitions.XD_NAMESPACE_URI;
import static org.xdef.impl.util.conv.xd2schema.xsd.definition.AlgPhase.TRANSFORMATION;

public class Xsd2XdUtils {

    public static void addAttribute(final Element el, final String attrName, final String attrValue) {
        XsdLogger.printP(LOG_DEBUG, TRANSFORMATION, el, "Add attribute. Name=" + attrName + ", Value=" + attrValue);
        el.setAttribute(attrName, attrValue);
    }

    public static void addAttribute(final Element el, final XmlSchemaAttribute xsdAttr, final String attrValue, final String xDefName, final XdAdapterCtx xdAdapterCtx) {
        XsdLogger.printP(LOG_DEBUG, TRANSFORMATION, el, "Add attribute. QName=" + xsdAttr.getQName());

        final QName xsdQName = xsdAttr.getQName();
        if (xsdQName.getNamespaceURI() != null && !XmlSchemaForm.UNQUALIFIED.equals(xsdAttr.getForm())) {
            final String qualifiedName = XdNameUtils.createQualifiedName(xsdQName, xDefName, xdAdapterCtx);
            el.setAttributeNS(xsdQName.getNamespaceURI(), qualifiedName, attrValue);
        } else {
            el.setAttribute(xsdAttr.getName(), attrValue);
        }
    }

    public static void addRefAttribute(final Element el, final QName qName) {
        Xsd2XdUtils.addXdefAttribute(el, XD_ATTR_SCRIPT, "ref " + qName.getLocalPart());
    }

    public static void addXdefAttribute(final Element el, final String qName, final String value) {
        XsdLogger.printP(LOG_DEBUG, TRANSFORMATION, el, "Add x-definition attribute. QName=" + qName + ", Value=" + value);
        final String localName = XdNameUtils.getLocalName(qName);
        final Attr attr = el.getAttributeNodeNS(XD_NAMESPACE_URI, localName);
        if (attr != null) {
            el.setAttributeNS(XD_NAMESPACE_URI, qName, attr.getValue() + "; " + value);
        } else {
            el.setAttributeNS(XD_NAMESPACE_URI, qName, value);
        }
    }

    public static XmlSchemaType getSchemaTypeByQName(final XmlSchema schema, final QName qName) {
        final Map<QName, XmlSchemaType> schemaTypeMap = schema.getSchemaTypes();
        if (schemaTypeMap != null) {
            return schemaTypeMap.get(qName);
        }

        return null;
    }

    /**
     * Features which should be enabled by default for transformation algorithm
     * @return default algorithm features
     */
    public static Set<Xsd2XdFeature> defaultFeatures() {
        Set<Xsd2XdFeature> features = new HashSet<Xsd2XdFeature>();
        return features;
    }
}
