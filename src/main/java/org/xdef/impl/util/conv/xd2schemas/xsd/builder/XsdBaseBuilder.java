package org.xdef.impl.util.conv.xd2schemas.xsd.builder;

import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.constants.Constants;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;
import org.xdef.XDNamedValue;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.impl.XData;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XD2XsdUtils;
import org.xdef.model.XMData;
import org.xdef.model.XMNode;
import org.xdef.model.XMOccurrence;

import javax.xml.namespace.QName;
import java.security.InvalidParameterException;

public class XsdBaseBuilder {

    private final XmlSchema schema;

    public XsdBaseBuilder(XmlSchema schema) {
        this.schema = schema;
    }

    /**
     * Create named xsd element
     * Example: <element name="elem_name">
     */
    public XmlSchemaElement createEmptyElement(final String name, final XElement xElement) {
        XmlSchemaElement elem = new XmlSchemaElement(schema, false);
        elem.setMinOccurs(xElement.getOccurence().minOccurs());
        elem.setMaxOccurs((xElement.isUnbounded() || xElement.isMaxUnlimited()) ? Long.MAX_VALUE : xElement.getOccurence().maxOccurs());
        return elem;
    }

    /**
     * Create complexType element
     * Output: <complexType>
     */
    public XmlSchemaComplexType createEmptyComplexType() {
        return new XmlSchemaComplexType(schema, false);
    }

    public XmlSchemaSimpleType creatSimpleTypeRef(final XData xData) {
        XmlSchemaSimpleType itemType = new XmlSchemaSimpleType(schema, true);
        itemType.setName(xData.getRefTypeName());

        XmlSchemaSimpleTypeRestriction restriction = null;
        final String parserName = xData.getParserName();
        XDValue parseMethod = xData.getParseMethod();
        if (parseMethod instanceof XDParser) {
            XDParser parser = ((XDParser)parseMethod);
            XDNamedValue parameters[] = parser.getNamedParams().getXDNamedItems();
            QName qName = XD2XsdUtils.parserNameToQName(parserName);
            if (qName != null) {
                restriction = XsdRestrictionBuilder.buildRestriction(qName, xData, parameters);
            }
        } else {
            restriction = XsdRestrictionBuilder.buildRestriction(Constants.XSD_STRING, xData, null);
        }

        if (restriction == null) {
            throw new RuntimeException("Unknown reference type parser: " + parserName);
        }

        itemType.setContent(restriction);
        return itemType;
    }

    public XmlSchemaSimpleType creatSimpleType(final XData xData) {
        XmlSchemaSimpleTypeRestriction restriction = null;
        final String parserName = xData.getParserName();
        XDValue parseMethod = xData.getParseMethod();
        if (parseMethod instanceof XDParser) {
            XDParser parser = ((XDParser)parseMethod);
            XDNamedValue parameters[] = parser.getNamedParams().getXDNamedItems();
            if (parameters.length == 0) {
                return null;
            }
            QName qName = XD2XsdUtils.parserNameToQName(parserName);
            if (qName != null) {
                restriction = XsdRestrictionBuilder.buildRestriction(qName, xData, parameters);
            }
        } else {
            restriction = XsdRestrictionBuilder.buildRestriction(Constants.XSD_STRING, xData, null);
        }

        if (restriction == null) {
            throw new RuntimeException("Unknown reference type parser: " + parserName);
        }

        XmlSchemaSimpleType itemType = new XmlSchemaSimpleType(schema, false);
        itemType.setName(xData.getRefTypeName());
        itemType.setContent(restriction);
        return itemType;
    }

    public XmlSchemaAttribute createAttribute(final String name, final XMData xmData) {
        XmlSchemaAttribute attr = new XmlSchemaAttribute(schema, false);
        final String importNamespace = xmData.getNSUri();
        final String nodeName = xmData.getName();
        if (importNamespace != null && XD2XsdUtils.isExternalRef(nodeName, importNamespace, schema)) {
            attr.getRef().setTargetQName(new QName(importNamespace, nodeName));
        } else {
            attr.setName(name);

            // TODO: Handling of reference namespaces?
            if (xmData.getRefTypeName() != null) {
                attr.setSchemaTypeName(new QName("", xmData.getRefTypeName()));
            } else {
                XmlSchemaSimpleType simpleType = creatSimpleType((XData)xmData);
                if (simpleType != null) {
                    attr.setSchemaType(simpleType);
                } else {
                    attr.setSchemaTypeName(XD2XsdUtils.parserNameToQName(xmData.getValueTypeName()));
                }
            }

            String newName = XD2XsdUtils.getResolvedName(schema, name);
            if (!name.equals(newName)) {
                attr.setName(newName);
            } else if (XmlSchemaForm.QUALIFIED.equals(schema.getAttributeFormDefault()) && XD2XsdUtils.isUnqualifiedName(schema, name)) {
                attr.setForm(XmlSchemaForm.UNQUALIFIED);
            }
        }

        if (xmData.isOptional() || xmData.getOccurence().isOptional()) {
            attr.setUse(XmlSchemaUse.OPTIONAL);
        } else if (xmData.isRequired() || xmData.getOccurence().isRequired()) {
            attr.setUse(XmlSchemaUse.REQUIRED);
        }

        return attr;
    }

    public XmlSchemaSimpleContent createSimpleContent(final XMData xmData) {
        XmlSchemaSimpleContent content = new XmlSchemaSimpleContent();
        XmlSchemaSimpleContentExtension contentExtension = new XmlSchemaSimpleContentExtension();

        final String parserName = xmData.getParserName();
        XDValue parseMethod = xmData.getParseMethod();
        // TODO: Has to be instance of XDParser?
        if (parseMethod instanceof XDParser) {
            contentExtension.setBaseTypeName(XD2XsdUtils.parserNameToQName(parserName));
        }

        content.setContent(contentExtension);
        return content;
    }

    /**
     *
     * @param groupType
     * @return
     */
    public XmlSchemaGroupParticle createGroup(short groupType, final XMOccurrence occurrence) {
        XmlSchemaGroupParticle particle = null;
        switch (groupType) {
            case XNode.XMSEQUENCE: {
                particle = new XmlSchemaSequence();
                break;
            }
            case XNode.XMMIXED: {
                particle = new XmlSchemaAll();
                break;
            }
            case XNode.XMCHOICE: {
                particle = new XmlSchemaChoice();
                break;
            }
            default: {
                throw new InvalidParameterException("Unknown groupType");
            }
        }

        particle.setMinOccurs(occurrence.minOccurs());
        particle.setMaxOccurs((occurrence.isUnbounded() || occurrence.isMaxUnlimited()) ? Long.MAX_VALUE : occurrence.maxOccurs());

        return particle;
    }
}
