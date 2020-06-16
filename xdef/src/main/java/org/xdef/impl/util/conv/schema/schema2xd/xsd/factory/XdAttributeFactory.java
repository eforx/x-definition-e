package org.xdef.impl.util.conv.schema.schema2xd.xsd.factory;

import org.apache.ws.commons.schema.*;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.xdef.impl.util.conv.schema.schema2xd.xsd.factory.declaration.IDeclarationTypeFactory;
import org.xdef.impl.util.conv.schema.schema2xd.xsd.model.XdAdapterCtx;
import org.xdef.impl.util.conv.schema.schema2xd.xsd.util.XdNameUtils;
import org.xdef.impl.util.conv.schema.util.SchemaLogger;
import org.xdef.msg.XSD;

import javax.xml.namespace.QName;

import static org.xdef.impl.util.conv.schema.schema2xd.xsd.definition.Xsd2XdDefinitions.*;
import static org.xdef.impl.util.conv.schema.schema2xd.xsd.definition.Xsd2XdFeature.XD_EXPLICIT_OCCURRENCE;
import static org.xdef.impl.util.conv.schema.schema2xd.xsd.definition.Xsd2XdFeature.XD_MIXED_REQUIRED;
import static org.xdef.impl.util.conv.schema.util.SchemaLoggerDefs.LOG_DEBUG;
import static org.xdef.impl.util.conv.schema.util.SchemaLoggerDefs.LOG_WARN;
import static org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.AlgPhase.TRANSFORMATION;

/**
 * Creates x-definition node's attributes
 */
public class XdAttributeFactory {

    /**
     * X-definition adapter context
     */
    final private XdAdapterCtx adapterCtx;

    /**
     * X-definition declaration node factory
     */
    final private XdDeclarationFactory xdDeclarationFactory;

    public XdAttributeFactory(XdAdapterCtx adapterCtx, XdDeclarationFactory xdDeclarationFactory) {
        this.adapterCtx = adapterCtx;
        this.xdDeclarationFactory = xdDeclarationFactory;
    }

    /**
     * Add attribute to given x-definition node
     * @param el            x-definition node
     * @param attrName      attribute name
     * @param attrValue     attribute value
     */
    public static void addAttr(final Element el, final String attrName, final String attrValue) {
        SchemaLogger.printP(LOG_DEBUG, TRANSFORMATION, el, "Add attribute. Name=" + attrName + ", Value=" + attrValue);
        el.setAttribute(attrName, attrValue);
    }

    /**
     * Add attribute based on input XSD attribute to given x-definition node
     * @param el                x-definition node
     * @param xsdAttr           XSD attribute node
     * @param xDefName          XSD document name
     */
    public void addAttr(final Element el, final XmlSchemaAttribute xsdAttr, final String xDefName) {
        SchemaLogger.printP(LOG_DEBUG, TRANSFORMATION, el, "Add attribute. QName=" + xsdAttr.getQName());

        final String attribute = createAttribute(xsdAttr);

        if (xsdAttr.isRef()) {
            final QName xsdQName = xsdAttr.getRef().getTargetQName();
            if (xsdQName != null) {
                final String qualifiedName = XdNameUtils.createQualifiedName(xsdQName, xDefName, adapterCtx);
                el.setAttributeNS(xsdQName.getNamespaceURI(), qualifiedName, attribute);
            } else {
                adapterCtx.getReportWriter().warning(XSD.XSD213);
                SchemaLogger.printP(LOG_WARN, TRANSFORMATION, xsdAttr, "Unknown attribute reference QName!");
            }
        } else {
            final QName xsdQName = xsdAttr.getQName();
            if (xsdQName != null && xsdQName.getNamespaceURI() != null && !XmlSchemaForm.UNQUALIFIED.equals(xsdAttr.getForm())) {
                final String qualifiedName = XdNameUtils.createQualifiedName(xsdQName, xDefName, adapterCtx);
                el.setAttributeNS(xsdQName.getNamespaceURI(), qualifiedName, attribute);
            } else {
                el.setAttribute(xsdAttr.getName(), attribute);
            }
        }
    }

    /**
     * Add reference attribute into given x-definition element node
     * @param el        x-definition element node
     * @param qName     reference qualified name
     */
    public static void addAttrRef(final Element el, final QName qName) {
        addAttrXDef(el, XD_ATTR_SCRIPT, "ref " + qName.getLocalPart());
    }

    /**
     * Add reference (in different x-definition) attribute into given x-definition element node
     * @param el        x-definition element node
     * @param xDefName  x-definition name
     * @param qName     reference qualified name
     */
    public static void addAttrRefInDiffXDef(final Element el, final String xDefName, final QName qName) {
        addAttrXDef(el, XD_ATTR_SCRIPT, "ref " + xDefName + '#' + XdNameUtils.createQualifiedName(qName));
    }

    /**
     * Add or append value to currently existing x-definition attribute (using namespace {@value Xsd2XdDefinitions.XD_NAMESPACE_URI})
     * to given x-definition element node
     * @param el        x-definition element node
     * @param qName     reference qualified name
     * @param value     attribute value
     */
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

    /**
     * Add x-definition occurrence attribute into given x-definition element node
     * @param xdNode        x-definition node
     * @param xsdNode       XSD document node containing occurrence info
     */
    public void addOccurrence(final Element xdNode, final XmlSchemaParticle xsdNode) {
        if (xsdNode.getMaxOccurs() == 1 && xsdNode.getMinOccurs() == 1) {
            if (adapterCtx.hasEnableFeature(XD_EXPLICIT_OCCURRENCE)) {
                addAttrXDef(xdNode, XD_ATTR_SCRIPT, "occurs 1");
            }
            return;
        }

        if (xsdNode.getMaxOccurs() == Long.MAX_VALUE) {
            if (xsdNode.getMinOccurs() == 0) {
                addAttrXDef(xdNode, XD_ATTR_SCRIPT, "occurs *");
                return;
            }

            if (xsdNode.getMinOccurs() == 1) {
                addAttrXDef(xdNode, XD_ATTR_SCRIPT, "occurs +");
                return;
            }

            addAttrXDef(xdNode, XD_ATTR_SCRIPT, "occurs " + xsdNode.getMinOccurs() + "..*");
            return;
        }

        if (xsdNode.getMinOccurs() == 0 && xsdNode.getMaxOccurs() == 1) {
            addAttrXDef(xdNode, XD_ATTR_SCRIPT, "occurs ?");
            return;
        }

        if (xsdNode.getMinOccurs() == xsdNode.getMaxOccurs()) {
            addAttrXDef(xdNode, XD_ATTR_SCRIPT, "occurs " + xsdNode.getMinOccurs());
            return;
        }

        addAttrXDef(xdNode, XD_ATTR_SCRIPT, "occurs " + xsdNode.getMinOccurs() + ".." + xsdNode.getMaxOccurs());
    }

    /**
     * Add x-definition text attribute into given x-definition element node
     * @param el        x-definition element node
     */
    public void addAttrText(final Element el) {
        addAttrXDef(el, XD_ATTR_TEXT, (!adapterCtx.hasEnableFeature(XD_MIXED_REQUIRED) ? "? " : "") + "string()");
    }

    /**
     * Add x-definition nillable attribute into given x-definition element node
     * @param el        x-definition element node
     * @param xsdElem   XSD element node
     */
    public void addAttrNillable(final Element el, final XmlSchemaElement xsdElem) {
        if (xsdElem.isNillable()) {
            addAttrXDef(el, XD_ATTR_SCRIPT, "options nillable");
        }
    }

    /**
     * Creates x-definition attribute based on given XSD attribute node
     * @param xsdAttr   XSD attribute node
     * @return x-definition attribute
     */
    private String createAttribute(final XmlSchemaAttribute xsdAttr) {
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
                final XdDeclarationBuilder b = xdDeclarationFactory.createBuilder()
                        .setSimpleType(xsdAttr.getSchemaType())
                        .setType(IDeclarationTypeFactory.Type.DATATYPE_DECL);

                valueBuilder.append(xdDeclarationFactory.createDeclarationContent(b));
            }
        }

        if (xsdAttr.getDefaultValue() != null && !xsdAttr.getDefaultValue().isEmpty()) {
            valueBuilder.append("; default \"" + xsdAttr.getDefaultValue() + "\"");
        }

        if (xsdAttr.getFixedValue() != null && !xsdAttr.getFixedValue().isEmpty()) {
            valueBuilder.append("; fixed \"" + xsdAttr.getFixedValue() + "\"");
        }

        return valueBuilder.toString();
    }
}
