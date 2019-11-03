package org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.xdef;

import org.apache.ws.commons.schema.XmlSchemaPatternFacet;
import org.xdef.XDNamedValue;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.XsdElementFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.AbstractXsdFacetFactory;

import java.util.ArrayList;
import java.util.List;

/*
    XDParser y = (XDParser) p;
        if ("xdatetime".equals(y.parserName())) {
        XDContainer c = y.getNamedParams();
        for (XDNamedValue item: c.getXDNamedItems()) {
        if ("format".equals(item.getName())) {
        return '"' + item.getValue().toString() + '"';
        }
        }
        return "\"yyyy-MM-ddTHH:mm:ss[.S][Z]\"";
        } else if ("dateTime".equals(y.parserName())) {
        return "\"y-MM-ddTHH:mm:ss[.S][Z]\"";
        } else if ("date".equals(y.parserName())) {
        return "\"y-MM-dd[Z]\"";
        } else if ("gDay".equals(y.parserName())) {
        return "\"---dd[Z]\"";
        } else if ("gMonth".equals(y.parserName())) {
        return "\"--MM[Z]\"";
        } else if ("gMonthDay".equals(y.parserName())) {
        return "\"--MM-dd[Z]\"";
        } else if ("\"gYear".equals(y.parserName())) {
        return "\"y[Z]\"";
        } else if ("ISOyear".equals(y.parserName())) {
        return "\"y[Z]\"";
        } else if ("gYearMonth".equals(y.parserName())) {
        return "\"y-MM[Z]\"";
        } else if ("ISOyearMonth".equals(y.parserName())) {
        return "\"y-MM[Z]\"";
        } else if ("dateYMDhms".equals(y.parserName())) {
        return "\"yyyyMMddHHmmss\"";
        } else if ("ISOdate".equals(y.parserName())) {
        return "\"y-MM-dd[Z]\"";
        } else if ("time".equals(y.parserName())) {
        return "\"HH:mm:ss[.S][Z]\"";
        } else if ("emailDate".equals(y.parserName())) {
        return "\"EEE, d MMM y HH:mm:ss[ ZZZZZ][ (z)]\"";
        }
        */

public class DateTimeFormatFacetFactory extends AbstractXsdFacetFactory {

    public static final String XD_PARSER_XDATETIME_NAME = "xdatetime";
    public static final String XD_PARSER_DATETIME_NAME = "dateYMDhms";
    public static final String XD_PARSER_EMAILDATE_NAME = "emailDate";

    @Override
    public List<XmlSchemaPatternFacet> pattern(final XDNamedValue param) {
        List<XmlSchemaPatternFacet> facets = new ArrayList<XmlSchemaPatternFacet>();
        String[] patterns = param.getValue().stringValue().split("\n");
        for (String p : patterns) {
            XmlSchemaPatternFacet facet = super.pattern(p);
            facet.setAnnotation(XsdElementFactory.createAnnotation("Original pattern value: '" + p + "'"));
            //String pattern = param.getValue().stringValue().replaceAll("\\[", "(").replaceAll("]", ")?");

            //facets.add(super.pattern(p));
        }

        return facets;
    }
}
