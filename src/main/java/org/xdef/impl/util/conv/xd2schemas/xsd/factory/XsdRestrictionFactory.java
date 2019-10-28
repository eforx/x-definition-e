package org.xdef.impl.util.conv.xd2schemas.xsd.factory;

import javafx.util.Pair;
import org.apache.ws.commons.schema.*;
import org.xdef.XDNamedValue;
import org.xdef.impl.XData;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.AbstractXsdFacetFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.IXsdFacetFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.DefaultFacetFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XD2XsdUtils;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;

import javax.xml.namespace.QName;

import java.util.List;

import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.*;

public class XsdRestrictionFactory {

    private final int logLevel;
    private final XData xData;
    private final String parserName;
    private XDNamedValue[] parameters = null;

    public XsdRestrictionFactory(XData xData, int logLevel) {
        this.xData = xData;
        this.logLevel = logLevel;
        this.parserName = xData.getParserName();
    }

    public void setParameters(XDNamedValue[] parameters) {
        this.parameters = parameters;
    }

    public XmlSchemaSimpleTypeRestriction createRestriction() {
        if (XsdLogger.isInfo(logLevel)) {
            XsdLogger.printP(INFO, TRANSFORMATION, xData, "Creating restrictions of simple content ...");
        }

        XmlSchemaSimpleTypeRestriction restriction = null;

        boolean customParser = true;
        Pair<QName, IXsdFacetFactory> parserInfo = XD2XsdUtils.getCustomFacetBuilder(parserName, parameters);
        if (parserInfo == null) {
            parserInfo = XD2XsdUtils.getDefaultFacetBuilder(parserName);
            if (parserInfo != null) {
                customParser = false;
                if (XsdLogger.isDebug(logLevel)) {
                    XsdLogger.printP(DEBUG, TRANSFORMATION, xData, "Default facet factory will be used");
                }
            }
        } else {
            if (XsdLogger.isDebug(logLevel)) {
                XsdLogger.printP(DEBUG, TRANSFORMATION, xData, "Custom facet factory will be used");
            }
        }

        if (parserInfo != null) {
            restriction = simpleTypeRestriction(parserInfo.getKey(), parserInfo.getValue());
        }

        if (restriction == null) {
            throw new RuntimeException("Unknown reference type parser: " + parserName);
        }

        if (customParser) {
            restriction.setAnnotation(XsdElementFactory.createAnnotation("Original x-definition parser: " + parserName));
        }

        return restriction;
    }

    public XmlSchemaSimpleTypeRestriction buildDefaultRestriction(final QName qName) {
        if (XsdLogger.isInfo(logLevel)) {
            XsdLogger.printP(INFO, TRANSFORMATION, xData, "Creating restrictions of simple content (default facet factory will be used) ...");
        }

        return simpleTypeRestriction(qName, new DefaultFacetFactory());
    }

    private XmlSchemaSimpleTypeRestriction simpleTypeRestriction(final QName qName, final IXsdFacetFactory facetBuilder) {
        if (XsdLogger.isInfo(logLevel)) {
            XsdLogger.printP(INFO, TRANSFORMATION, xData, "Creating simple type restriction. Type=" + qName);
        }

        XmlSchemaSimpleTypeRestriction restriction = new XmlSchemaSimpleTypeRestriction();
        restriction.setBaseTypeName(qName);
        restriction.getFacets().addAll(buildFacets(qName, facetBuilder));
        return restriction;
    }

    private List<XmlSchemaFacet> buildFacets(final QName qName, final IXsdFacetFactory facetBuilder) {
        if ("double".equals(qName.getLocalPart()) || "float".equals(qName.getLocalPart())) {
            facetBuilder.setValueType(IXsdFacetFactory.ValueType.DECIMAL_FLOATING);
        } else if ("int".equals(qName.getLocalPart()) || "long".equals(qName.getLocalPart())) {
            facetBuilder.setValueType(IXsdFacetFactory.ValueType.DECIMAL_INTEGER);
        } else {
            facetBuilder.setValueType(IXsdFacetFactory.ValueType.STRING);
        }

        ((AbstractXsdFacetFactory)facetBuilder).setLogLevel(logLevel);
        return facetBuilder.build(parameters);
    }

    /*
    public static List<XmlSchemaFacet> buildFacets(final String parserName, final XDNamedValue[] parameters) {
        List<XmlSchemaFacet> facets = null;

        Pair<QName, IXsdFacetFactory> parserInfo = XD2XsdUtils.getCustomFacetBuilder(parserName, parameters);
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
    }*/

}
