package org.xdef.impl.util.conv.xd2schemas;

import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import org.xdef.XDNamedValue;
import org.xdef.impl.XData;

import javax.xml.namespace.QName;

public class XsdRestrictionBuilder {

    public static XmlSchemaSimpleTypeRestriction buildRestriction(final QName qName, final XData xData, final XDNamedValue params[]) {
        if ("double".equals(qName.getLocalPart()) || "float".equals(qName.getLocalPart())) {
            return XsdRestrictionBuilder.decimalRestriction(qName, xData, params);
        }

        return XsdRestrictionBuilder.nonDecimalRestriction(qName, xData, params);
    }

    private static XmlSchemaSimpleTypeRestriction nonDecimalRestriction(final QName qName, final XData xData, final XDNamedValue params[]) {
        return simpleRestriction(qName, xData, params, false);
    }

    private static XmlSchemaSimpleTypeRestriction decimalRestriction(final QName qName, final XData xData, final XDNamedValue params[]) {
        return simpleRestriction(qName, xData, params, true);
    }

    private static XmlSchemaSimpleTypeRestriction simpleRestriction(final QName qName, final XData xData, final XDNamedValue params[], boolean decimal) {
        XmlSchemaSimpleTypeRestriction restriction = new XmlSchemaSimpleTypeRestriction();
        restriction.setBaseTypeName(qName);
        restriction.getFacets().addAll(XsdFacetBuilder.build(params, decimal));
        return restriction;
    }
}
