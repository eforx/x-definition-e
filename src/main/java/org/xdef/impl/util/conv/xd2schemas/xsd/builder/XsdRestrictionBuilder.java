package org.xdef.impl.util.conv.xd2schemas.xsd.builder;

import javafx.util.Pair;
import org.apache.ws.commons.schema.*;
import org.xdef.XDNamedValue;
import org.xdef.impl.XData;
import org.xdef.impl.util.conv.xd2schemas.xsd.builder.facet.IXsdFacetBuilder;
import org.xdef.impl.util.conv.xd2schemas.xsd.builder.facet.DefaultFacetBuilder;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XD2XsdUtils;

import javax.xml.namespace.QName;

import java.util.ArrayList;
import java.util.List;

import static org.xdef.impl.util.conv.xd2schemas.xsd.XD2XsdDefinitions.XD_PARSER_NUM;

public class XsdRestrictionBuilder {

    private final XData xData;
    private final String parserName;
    private XDNamedValue[] parameters = null;

    public XsdRestrictionBuilder(XData xData) {
        this.xData = xData;
        this.parserName = xData.getParserName();
    }

    public void setParameters(XDNamedValue[] parameters) {
        this.parameters = parameters;
    }

    public XmlSchemaSimpleTypeRestriction buildRestriction() {
        XmlSchemaSimpleTypeRestriction restriction = null;

        boolean customParser = true;
        Pair<QName, IXsdFacetBuilder> parserInfo = XD2XsdUtils.getCustomFacetBuilder(parserName, parameters);
        if (parserInfo == null) {
            parserInfo = XD2XsdUtils.getDefaultFacetBuilder(parserName);
            customParser = false;
        }

        if (parserInfo != null) {
            restriction = simpleRestriction(parserInfo.getKey(), parserInfo.getValue());
        }

        if (restriction == null) {
            throw new RuntimeException("Unknown reference type parser: " + parserName);
        }

        if (customParser) {
            restriction.setAnnotation(XsdElementBuilder.createAnnotation("Original x-definition parser: " + parserName));
        }

        return restriction;
    }

    public XmlSchemaSimpleTypeRestriction buildDefaultRestriction(final QName qName) {
        return simpleRestriction(qName, new DefaultFacetBuilder());
    }

    private XmlSchemaSimpleTypeRestriction simpleRestriction(final QName qName, final IXsdFacetBuilder facetBuilder) {
        XmlSchemaSimpleTypeRestriction restriction = new XmlSchemaSimpleTypeRestriction();
        restriction.setBaseTypeName(qName);
        restriction.getFacets().addAll(buildFacets(qName, facetBuilder, parameters));
        return restriction;
    }

    private static List<XmlSchemaFacet> buildFacets(final QName qName, final IXsdFacetBuilder facetBuilder, final XDNamedValue[] parameters) {
        if ("double".equals(qName.getLocalPart()) || "float".equals(qName.getLocalPart())) {
            facetBuilder.setValueType(IXsdFacetBuilder.ValueType.DECIMAL_FLOATING);
        } else if ("int".equals(qName.getLocalPart()) || "long".equals(qName.getLocalPart())) {
            facetBuilder.setValueType(IXsdFacetBuilder.ValueType.DECIMAL_INTEGER);
        } else {
            facetBuilder.setValueType(IXsdFacetBuilder.ValueType.STRING);
        }

        return facetBuilder.build(parameters);
    }

    public static List<XmlSchemaFacet> buildFacets(final String parserName, final XDNamedValue[] parameters) {
        List<XmlSchemaFacet> facets = null;

        Pair<QName, IXsdFacetBuilder> parserInfo = XD2XsdUtils.getCustomFacetBuilder(parserName, parameters);
        if (parserInfo == null) {
            parserInfo = XD2XsdUtils.getDefaultFacetBuilder(parserName);
        }

        if (parserInfo != null) {
            facets = buildFacets(parserInfo.getKey(), parserInfo.getValue(), parameters);
        }

        if (facets == null) {
            throw new RuntimeException("Unknown reference type parser: " + parserName);
        }

        return facets;
    }

}
