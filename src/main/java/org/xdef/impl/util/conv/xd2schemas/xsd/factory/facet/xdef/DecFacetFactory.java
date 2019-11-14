package org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.xdef;

import org.apache.ws.commons.schema.*;
import org.xdef.XDNamedValue;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.XsdElementFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.AbstractXsdFacetFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.DateTimeFormatAdapter;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XD2XsdUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DecFacetFactory extends AbstractXsdFacetFactory {

    public static final String XD_PARSER_NAME = "dec";

    private Long totalDigits = null;
    private Long fractionsDigits = null;

    @Override
    public XmlSchemaFractionDigitsFacet fractionDigits(XDNamedValue param) {
        fractionsDigits = Long.valueOf(param.getValue().intValue());
        return null;
    }

    @Override
    public XmlSchemaTotalDigitsFacet totalDigits(XDNamedValue param) {
        totalDigits = Long.valueOf(param.getValue().intValue());
        return null;
    }

    @Override
    public void extraFacets(final List<XmlSchemaFacet> facets) {
        addPattern(facets);
        if (totalDigits != null) {
            addLength(facets);
        }
    }

    private void addPattern(final List facets) {
        final StringBuilder sb = new StringBuilder();
        sb.append("(-)?[0-9]+");
        if (fractionsDigits != null) {
            sb.append("([.,][0-9]{1," + fractionsDigits + "})?");
        } else {
            sb.append("([.,][0-9]*)?");
        }
        final String pattern = sb.toString();
        final XmlSchemaPatternFacet facet = super.pattern(pattern);
        facets.add(facet);
    }

    private void addLength(final List facets) {
        final XmlSchemaMaxLengthFacet facet = new XmlSchemaMaxLengthFacet();
        facet.setValue(totalDigits + 1);
        facets.add(facet);
    }
}
