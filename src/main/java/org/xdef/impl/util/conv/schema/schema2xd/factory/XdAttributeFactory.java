package org.xdef.impl.util.conv.schema.schema2xd.factory;

import org.apache.ws.commons.schema.*;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.xdef.impl.util.conv.schema.schema2xd.factory.declaration.IDeclarationTypeFactory;
import org.xdef.impl.util.conv.schema.schema2xd.model.XdAdapterCtx;
import org.xdef.impl.util.conv.schema.schema2xd.util.XdNameUtils;
import org.xdef.impl.util.conv.schema.util.SchemaLogger;

import javax.xml.namespace.QName;

import static org.xdef.impl.util.conv.schema.schema2xd.definition.Xsd2XdDefinitions.XD_ATTR_SCRIPT;
import static org.xdef.impl.util.conv.schema.schema2xd.definition.Xsd2XdDefinitions.XD_NAMESPACE_URI;
import static org.xdef.impl.util.conv.schema.schema2xd.definition.Xsd2XdFeature.XD_EXPLICIT_OCCURRENCE;
import static org.xdef.impl.util.conv.schema.util.SchemaLoggerDefs.LOG_DEBUG;
import static org.xdef.impl.util.conv.schema.util.SchemaLoggerDefs.LOG_WARN;
import static org.xdef.impl.util.conv.schema.xd2schema.definition.AlgPhase.TRANSFORMATION;

public class XdAttributeFactory {

    final private XdAdapterCtx adapterCtx;

    final private XdDeclarationFactory xdDeclarationFactory;

    public XdAttributeFactory(XdAdapterCtx adapterCtx, XdDeclarationFactory xdDeclarationFactory) {
        this.adapterCtx = adapterCtx;
        this.xdDeclarationFactory = xdDeclarationFactory;
    }

    public static void addAttr(final Element el, final String attrName, final String attrValue) {
        SchemaLogger.printP(LOG_DEBUG, TRANSFORMATION, el, "Add attribute. Name=" + attrName + ", Value=" + attrValue);
        el.setAttribute(attrName, attrValue);
    }

    public static void addAttr(final Element el, final XmlSchemaAttribute xsdAttr, final String attrValue, final String xDefName, final XdAdapterCtx xdAdapterCtx) {
        SchemaLogger.printP(LOG_DEBUG, TRANSFORMATION, el, "Add attribute. QName=" + xsdAttr.getQName());

        if (xsdAttr.isRef()) {
            final QName xsdQName = xsdAttr.getRef().getTargetQName();
            if (xsdQName != null) {
                final String qualifiedName = XdNameUtils.createQualifiedName(xsdQName, xDefName, xdAdapterCtx);
                el.setAttributeNS(xsdQName.getNamespaceURI(), qualifiedName, attrValue);
            } else {
                SchemaLogger.printP(LOG_WARN, TRANSFORMATION, xsdAttr, "Unknown attribute reference QName!");
            }
        } else {
            final QName xsdQName = xsdAttr.getQName();
            if (xsdQName != null && xsdQName.getNamespaceURI() != null && !XmlSchemaForm.UNQUALIFIED.equals(xsdAttr.getForm())) {
                final String qualifiedName = XdNameUtils.createQualifiedName(xsdQName, xDefName, xdAdapterCtx);
                el.setAttributeNS(xsdQName.getNamespaceURI(), qualifiedName, attrValue);
            } else {
                el.setAttribute(xsdAttr.getName(), attrValue);
            }
        }
    }

    public static void addAttrRef(final Element el, final QName qName) {
        addAttrXDef(el, XD_ATTR_SCRIPT, "ref " + qName.getLocalPart());
    }

    public static void addAttrRefInDiffXDef(final Element el, final String xDefName, final QName qName) {
        addAttrXDef(el, XD_ATTR_SCRIPT, "ref " + xDefName + '#' + XdNameUtils.createQualifiedName(qName));
    }

    private static void addAttrXDef(final Element el, final String qName, final String value) {
        SchemaLogger.printP(LOG_DEBUG, TRANSFORMATION, el, "Add x-definition attribute. QName=" + qName + ", Value=" + value);
        final String localName = XdNameUtils.getLocalName(qName);
        final Attr attr = el.getAttributeNodeNS(XD_NAMESPACE_URI, localName);
        if (attr != null) {
            el.setAttributeNS(XD_NAMESPACE_URI, qName, attr.getValue() + "; " + value);
        } else {
            el.setAttributeNS(XD_NAMESPACE_URI, qName, value);
        }
    }

    public void addOccurrence(final Element xdParticle, final XmlSchemaParticle xsdParicle) {
        if (xsdParicle.getMaxOccurs() == 1 && xsdParicle.getMinOccurs() == 1) {
            if (adapterCtx.hasEnableFeature(XD_EXPLICIT_OCCURRENCE)) {
                addAttrXDef(xdParticle, XD_ATTR_SCRIPT, "occurs 1");
            }
            return;
        }

        if (xsdParicle.getMaxOccurs() == Long.MAX_VALUE) {
            if (xsdParicle.getMinOccurs() == 0) {
                addAttrXDef(xdParticle, XD_ATTR_SCRIPT, "occurs *");
                return;
            }

            if (xsdParicle.getMinOccurs() == 1) {
                addAttrXDef(xdParticle, XD_ATTR_SCRIPT, "occurs +");
                return;
            }

            addAttrXDef(xdParticle, XD_ATTR_SCRIPT, "occurs " + xsdParicle.getMinOccurs() + "..*");
            return;
        }

        if (xsdParicle.getMinOccurs() == 0 && xsdParicle.getMaxOccurs() == 1) {
            addAttrXDef(xdParticle, XD_ATTR_SCRIPT, "occurs ?");
            return;
        }

        if (xsdParicle.getMinOccurs() == xsdParicle.getMaxOccurs()) {
            addAttrXDef(xdParticle, XD_ATTR_SCRIPT, "occurs " + xsdParicle.getMinOccurs());
            return;
        }

        addAttrXDef(xdParticle, XD_ATTR_SCRIPT, "occurs " + xsdParicle.getMinOccurs() + ".." + xsdParicle.getMaxOccurs());
    }

    public void addAttrNillable(final Element xdParticle, final XmlSchemaElement xsdElem) {
        if (xsdElem.isNillable()) {
            addAttrXDef(xdParticle, XD_ATTR_SCRIPT, "options nillable");
        }
    }

    public String createAttribute(final XmlSchemaAttribute xsdAttr) {
        SchemaLogger.printP(LOG_DEBUG, TRANSFORMATION, xsdAttr, "Creating attribute.");

        final StringBuilder valueBuilder = new StringBuilder();
        if (XmlSchemaUse.REQUIRED.equals(xsdAttr.getUse())) {
            valueBuilder.append("required ");
        } else {
            valueBuilder.append("optional ");
        }

        if (xsdAttr.getSchemaTypeName() != null) {
            valueBuilder.append(xsdAttr.getSchemaTypeName().getLocalPart() + "()");
        } else if (xsdAttr.getSchemaType() != null) {
            if (xsdAttr.getSchemaType().getContent() instanceof XmlSchemaSimpleTypeRestriction) {
                valueBuilder.append(xdDeclarationFactory.create((XmlSchemaSimpleTypeRestriction)xsdAttr.getSchemaType().getContent(), null, IDeclarationTypeFactory.Mode.DATATYPE_DECL));
            }
        }

        if (xsdAttr.getDefaultValue() != null && !xsdAttr.getDefaultValue().isEmpty()) {
            valueBuilder.append("; default \"" + xsdAttr.getDefaultValue() + "\"");
        }

        return valueBuilder.toString();
    }
}
