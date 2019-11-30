package org.xdef.impl.util.conv.schema2xd.xsd.util;

import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaUse;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.xdef.impl.util.conv.schema.util.XsdLogger;
import org.xdef.impl.util.conv.schema2xd.xsd.definition.Xsd2XdFeature;
import org.xdef.impl.util.conv.xd2schema.xsd.definition.XD2XsdFeature;

import java.util.HashSet;
import java.util.Set;

import static org.xdef.impl.util.conv.schema.util.XsdLoggerDefs.LOG_DEBUG;
import static org.xdef.impl.util.conv.schema2xd.xsd.definition.Xsd2XdDefinitions.XD_NAMESPACE_URI;
import static org.xdef.impl.util.conv.schema2xd.xsd.definition.Xsd2XdFeature.XD_TEXT_OPTIONAL;
import static org.xdef.impl.util.conv.xd2schema.xsd.definition.AlgPhase.TRANSFORMATION;

public class Xsd2XdUtils {

    public static void addAttribute(final Element el, final String attrName, final String attrValue) {
        XsdLogger.printP(LOG_DEBUG, TRANSFORMATION, el, "Add attribute. Name=" + attrName + ", Value=" + attrValue);
        el.setAttribute(attrName, attrValue);
    }

    public static void addAttribute(final Element el, final XmlSchemaAttribute xsdAttr) {
        XsdLogger.printP(LOG_DEBUG, TRANSFORMATION, el, "Add attribute. QName=" + xsdAttr.getQName() + ", SchemaType=" + xsdAttr.getSchemaTypeName());

        final StringBuilder valueBuilder = new StringBuilder();
        if (XmlSchemaUse.OPTIONAL.equals(xsdAttr.getUse())) {
            valueBuilder.append("optional ");
        } else if (XmlSchemaUse.REQUIRED.equals(xsdAttr.getUse())) {
            valueBuilder.append("required ");
        }

        valueBuilder.append(xsdAttr.getSchemaTypeName().getLocalPart() + "()");

        if (xsdAttr.getQName() != null) {
            el.setAttributeNS(xsdAttr.getQName().getNamespaceURI(), xsdAttr.getName(), valueBuilder.toString());
        } else {
            el.setAttribute(xsdAttr.getName(), valueBuilder.toString());
        }
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

    /**
     * Features which should be enabled by default for transformation algorithm
     * @return default algorithm features
     */
    public static Set<Xsd2XdFeature> defaultFeatures() {
        Set<Xsd2XdFeature> features = new HashSet<Xsd2XdFeature>();
        return features;
    }
}
