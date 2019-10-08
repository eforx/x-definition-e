package org.xdef.impl.util.conv.xd2schemas.xsd.builder;

import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import org.xdef.XDNamedValue;
import org.xdef.impl.XData;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XD2XsdUtils;

import javax.xml.namespace.QName;

import java.util.ArrayList;
import java.util.List;

import static org.xdef.impl.util.conv.xd2schemas.xsd.XD2XsdDefinitions.XD_PARSER_NUM;

public class XsdRestrictionBuilder {

    final XData xData;
    final String parserName;
    XDNamedValue parameters[] = null;

    public XsdRestrictionBuilder(XData xData) {
        this.xData = xData;
        this.parserName = xData.getParserName();
    }

    public void setParameters(XDNamedValue[] parameters) {
        this.parameters = parameters;
    }

    public XmlSchemaSimpleTypeRestriction buildRestriction() {
        XmlSchemaSimpleTypeRestriction restriction = null;

        QName qName = XD2XsdUtils.customParserNameToQName(parserName);
        if (qName != null) {
            restriction = buildRestriction(qName);
            buildCustomRestriction(restriction, qName);
        } else {
            qName = XD2XsdUtils.parserNameToQName(parserName);
            if (qName != null) {
                restriction = buildRestriction(qName);
            }
        }

        if (restriction == null) {
            throw new RuntimeException("Unknown reference type parser: " + parserName);
        }

        return restriction;
    }

    public XmlSchemaSimpleTypeRestriction buildRestriction(final QName qName) {
        if ("double".equals(qName.getLocalPart()) || "float".equals(qName.getLocalPart())) {
            return decimalRestriction(qName);
        }

        return nonDecimalRestriction(qName);
    }

    private XmlSchemaSimpleTypeRestriction nonDecimalRestriction(final QName qName) {
        return simpleRestriction(qName, false);
    }

    private XmlSchemaSimpleTypeRestriction decimalRestriction(final QName qName) {
        return simpleRestriction(qName, true);
    }

    private XmlSchemaSimpleTypeRestriction simpleRestriction(final QName qName, boolean decimal) {
        XmlSchemaSimpleTypeRestriction restriction = new XmlSchemaSimpleTypeRestriction();
        restriction.setBaseTypeName(qName);
        restriction.getFacets().addAll(XsdFacetBuilder.build(parameters, decimal));
        return restriction;
    }

    public void buildCustomRestriction(final XmlSchemaSimpleTypeRestriction restriction, final QName qName) {
        final List<XmlSchemaFacet> extraFacets = restriction.getFacets();
        if (XD_PARSER_NUM.equals(parserName)) {
            extraFacets.add(XsdFacetBuilder.pattern("([0-9])*"));
        }
    }
}
