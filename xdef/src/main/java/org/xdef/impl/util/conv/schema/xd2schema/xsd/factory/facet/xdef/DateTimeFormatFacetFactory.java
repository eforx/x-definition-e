package org.xdef.impl.util.conv.schema.xd2schema.xsd.factory.facet.xdef;

import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.apache.ws.commons.schema.XmlSchemaPatternFacet;
import org.xdef.XDNamedValue;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.factory.XsdNodeFactory;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.factory.facet.AbstractXsdFacetFactory;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.util.DateTimeFormatAdapter;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.util.Xd2XsdUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DateTimeFormatFacetFactory extends AbstractXsdFacetFactory {

    public static final String XD_PARSER_XDATETIME_NAME = "xdatetime";
    public static final String XD_PARSER_DATETIME_NAME = "dateYMDhms";
    public static final String XD_PARSER_EMAILDATE_NAME = "emailDate";

    private final String pattern;

    public DateTimeFormatFacetFactory() {
        this.pattern = null;
    }

    public DateTimeFormatFacetFactory(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public List<XmlSchemaPatternFacet> pattern(final XDNamedValue param) {
        List<XmlSchemaPatternFacet> facets = new ArrayList<XmlSchemaPatternFacet>();
        final String[] values = param.getValue().stringValue().split("\n");
        for (String v : values) {
            addPattern(facets, v);
        }

        return facets;
    }

    @Override
    public void extraFacets(final List<XmlSchemaFacet> facets) {
        if (pattern != null) {
            addPattern(facets, pattern);
        }
    }

    private void addPattern(final List facets, final String value) {
        final Set<String> patterns = DateTimeFormatAdapter.getRegexes(value);
        final String pattern = Xd2XsdUtils.regexCollectionToSingle(patterns);
        XmlSchemaPatternFacet facet = super.pattern(pattern);
        facet.setAnnotation(XsdNodeFactory.createAnnotation("Original pattern value: '" + value + "'", adapterCtx));
        facets.add(facet);
    }
}
