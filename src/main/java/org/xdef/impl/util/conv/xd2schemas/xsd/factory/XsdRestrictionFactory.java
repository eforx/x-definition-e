package org.xdef.impl.util.conv.xd2schemas.xsd.factory;

import javafx.util.Pair;
import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import org.apache.ws.commons.schema.constants.Constants;
import org.xdef.XDNamedValue;
import org.xdef.impl.XData;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.DefaultFacetFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.IXsdFacetFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XD2XsdParserMapping;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;

import javax.xml.namespace.QName;
import java.util.List;

import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.AlgPhase.TRANSFORMATION;
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XsdLoggerDefs.*;

public class XsdRestrictionFactory {

    private final XData xData;
    private final String parserName;
    private XDNamedValue[] parameters = null;

    public XsdRestrictionFactory(XData xData) {
        this.xData = xData;
        this.parserName = xData.getParserName();
    }

    public void setParameters(XDNamedValue[] parameters) {
        this.parameters = parameters;
    }

    public XmlSchemaSimpleTypeRestriction createRestriction() {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xData, "Creating restrictions of simple content ...");

        boolean customParser = true;
        boolean unknownParser = false;

        Pair<QName, IXsdFacetFactory> parserInfo = XD2XsdParserMapping.getCustomFacetFactory(parserName, parameters);
        if (parserInfo == null) {
            parserInfo = XD2XsdParserMapping.getDefaultFacetFactory(parserName);
            if (parserInfo != null) {
                customParser = false;
            }
        }

        if (parserInfo == null) {
            XsdLogger.printP(LOG_WARN, TRANSFORMATION, xData, "Unsupported restriction parser! Parser=" + parserName);
            parserInfo = new Pair(Constants.XSD_STRING, new DefaultFacetFactory());
            unknownParser = true;
        }

        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xData, "Following factory will be used. Factory=" + parserInfo.getValue().getClass().getSimpleName() + ", Parser=" + parserName);

        final XmlSchemaSimpleTypeRestriction restriction = simpleTypeRestriction(parserInfo.getKey(), parserInfo.getValue());
        if (customParser || unknownParser) {
            restriction.setAnnotation(XsdElementFactory.createAnnotation("Original x-definition parser: " + parserName));
        }

        return restriction;
    }

    public XmlSchemaSimpleTypeRestriction buildDefaultRestriction(final QName qName) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xData, "Creating restrictions of simple content (default facet factory will be used) ...");
        return simpleTypeRestriction(qName, new DefaultFacetFactory());
    }

    private XmlSchemaSimpleTypeRestriction simpleTypeRestriction(final QName qName, final IXsdFacetFactory facetBuilder) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xData, "Creating simple type restriction. Type=" + qName);
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

        return facetBuilder.build(parameters);
    }

    /*
    public static List<XmlSchemaFacet> buildFacets(final String parserName, final XDNamedValue[] parameters) {
        List<XmlSchemaFacet> facets = null;

        Pair<QName, IXsdFacetFactory> parserInfo = XD2XsdUtils.getCustomFacetFactory(parserName, parameters);
        if (parserInfo == null) {
            parserInfo = XD2XsdUtils.getDefaultFacetFactory(parserName);
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
