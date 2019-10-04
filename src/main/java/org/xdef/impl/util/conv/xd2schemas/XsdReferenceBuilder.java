package org.xdef.impl.util.conv.xd2schemas;

import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import org.apache.ws.commons.schema.constants.Constants;
import org.xdef.XDNamedValue;
import org.xdef.impl.XData;

import javax.xml.namespace.QName;

public class XsdReferenceBuilder {

    public static XmlSchemaSimpleTypeRestriction stringReference(final XData xData, final XDNamedValue params[]) {
        return simpleReference(Constants.XSD_STRING, xData, params);
    }

    public static XmlSchemaSimpleTypeRestriction intReference(final XData xData, final XDNamedValue params[]) {
        return simpleReference(Constants.XSD_INT, xData, params);
    }

    public static XmlSchemaSimpleTypeRestriction longReference(final XData xData, final XDNamedValue params[]) {
        return simpleReference(Constants.XSD_LONG, xData, params);
    }

    public static XmlSchemaSimpleTypeRestriction doubleReference(final XData xData, final XDNamedValue params[]) {
        return simpleReference(Constants.XSD_DOUBLE, xData, params, true);
    }

    public static XmlSchemaSimpleTypeRestriction floatReference(final XData xData, final XDNamedValue params[]) {
        return simpleReference(Constants.XSD_FLOAT, xData, params, true);
    }

    private static XmlSchemaSimpleTypeRestriction simpleReference(final QName qName, final XData xData, final XDNamedValue params[]) {
        return simpleReference(qName, xData, params, false);
    }

    private static XmlSchemaSimpleTypeRestriction simpleReference(final QName qName, final XData xData, final XDNamedValue params[], boolean decimal) {
        XmlSchemaSimpleTypeRestriction restriction = new XmlSchemaSimpleTypeRestriction();
        restriction.setBaseTypeName(qName);
        restriction.getFacets().addAll(XsdFacetBuilder.build(params, decimal));
        return restriction;
    }
}
