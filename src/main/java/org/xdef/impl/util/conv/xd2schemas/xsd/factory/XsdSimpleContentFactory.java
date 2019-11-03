package org.xdef.impl.util.conv.xd2schemas.xsd.factory;

import javafx.util.Pair;
import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.constants.Constants;
import org.xdef.XDNamedValue;
import org.xdef.XDParser;
import org.xdef.impl.XData;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.DefaultFacetFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.IXsdFacetFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.pattern.ListRegexFacetFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.xdef.ListFacetFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XD2XsdParserMapping;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;

import javax.xml.namespace.QName;
import java.util.List;

import static org.xdef.XDValueID.XD_CONTAINER;
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.AlgPhase.TRANSFORMATION;
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XsdLoggerDefs.*;

public class XsdSimpleContentFactory {

    private final XsdElementFactory xsdFactory;
    private final XData xData;
    private final String parserName;
    private XDNamedValue[] parameters = null;

    public XsdSimpleContentFactory(XsdElementFactory xsdFactory, XData xData) {
        this.xsdFactory = xsdFactory;
        this.xData = xData;
        this.parserName = xData.getParserName();
    }

    public void setParameters(XDNamedValue[] parameters) {
        this.parameters = parameters;
    }

    public XmlSchemaSimpleTypeContent createSimpleContent() {
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
            XsdLogger.printP(LOG_WARN, TRANSFORMATION, xData, "Unsupported simple content parser! Parser=" + parserName);
            parserInfo = new Pair(Constants.XSD_STRING, new DefaultFacetFactory());
            unknownParser = true;
        }

        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xData, "Following factory will be used. Factory=" + parserInfo.getValue().getClass().getSimpleName() + ", Parser=" + parserName);

        XmlSchemaSimpleTypeContent res;
        if (parserInfo.getValue() instanceof ListFacetFactory) {
            res = simpleTypeList(parserInfo.getKey(), parserInfo.getValue());
        } else {
            res = simpleTypeRestriction(parserInfo.getKey(), parserInfo.getValue(), parameters);
            if (customParser || unknownParser) {
                res.setAnnotation(XsdElementFactory.createAnnotation("Original x-definition parser: " + parserName));
            }
        }

        return res;
    }

    public XmlSchemaSimpleTypeRestriction createDefaultRestriction(final QName qName) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xData, "Creating restrictions of simple content (default facet factory will be used) ...");
        return simpleTypeRestriction(qName, new DefaultFacetFactory(), parameters);
    }

    private XmlSchemaSimpleTypeRestriction simpleTypeRestriction(final QName qName, final IXsdFacetFactory facetBuilder, final XDNamedValue[] parameters) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xData, "Creating simple type restriction. Type=" + qName);
        XmlSchemaSimpleTypeRestriction restriction = new XmlSchemaSimpleTypeRestriction();
        restriction.setBaseTypeName(qName);
        restriction.getFacets().addAll(buildFacets(qName, facetBuilder, parameters));
        return restriction;
    }

    private XmlSchemaSimpleTypeContent simpleTypeList(final QName qName, final IXsdFacetFactory facetBuilder) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xData, "Creating simple type list. Type=" + qName);
        final XmlSchemaSimpleTypeList list = simpleTypeList(qName);
        final XmlSchemaSimpleTypeRestriction restriction = simpleTypeRestriction(qName, facetBuilder, parameters);

        // If exists some other restrictions for list, then wrap up list inside
        if (!restriction.getFacets().isEmpty()) {
            restriction.setBaseTypeName(null);
            final XmlSchemaSimpleType simpleTypeRestriction = xsdFactory.createEmptySimpleType(false);
            simpleTypeRestriction.setContent(list);
            restriction.setBaseType(simpleTypeRestriction);
            return restriction;
        }

        return list;
    }

    private XmlSchemaSimpleTypeList simpleTypeList(final QName qName) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xData, "Creating simple type list. Type=" + qName);
        final XmlSchemaSimpleTypeList list = new XmlSchemaSimpleTypeList();
        final XmlSchemaSimpleType simpleType = xsdFactory.createEmptySimpleType(false);
        XmlSchemaSimpleTypeRestriction restriction = null;

        for (XDNamedValue namedValue : parameters) {
            if (namedValue.getValue() instanceof XDParser) {
                restriction = simpleTypeRestriction(qName, new DefaultFacetFactory(), ((XDParser) namedValue.getValue()).getNamedParams().getXDNamedItems());
                break;
            }
        }

        if (restriction == null) {
            XsdLogger.printP(LOG_WARN, TRANSFORMATION, xData, "List restrictions have not been found!");
        } else {
            simpleType.setContent(restriction);
        }

        list.setItemType(simpleType);
        return list;
    }

    private List<XmlSchemaFacet> buildFacets(final QName qName, final IXsdFacetFactory facetBuilder, final XDNamedValue[] parameters) {
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
