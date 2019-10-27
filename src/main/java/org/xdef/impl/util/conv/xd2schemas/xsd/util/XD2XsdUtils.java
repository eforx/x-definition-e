package org.xdef.impl.util.conv.xd2schemas.xsd.util;

import javafx.util.Pair;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.apache.ws.commons.schema.constants.Constants;
import org.xdef.XDNamedValue;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.impl.XData;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.DefaultFacetFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.IXsdFacetFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.array.ListFacetFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.array.UnionFacetFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.xdef.AnFacetFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.xdef.DateTimeFacetFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.xdef.NumFacetFactory;

import javax.xml.namespace.QName;

import static org.xdef.impl.util.conv.xd2schemas.xsd.XD2XsdDefinitions.*;
import static org.xdef.model.XMNode.*;

public class XD2XsdUtils {

    /**
     * Convert xd parser name to xsd QName
     * @param parserName
     * @return
     */
    public static QName getDefaultQName(final String parserName) {
        if ("CDATA".equals(parserName)) {
            return Constants.XSD_STRING;
        } else if ("string".equals(parserName)) {
            return Constants.XSD_STRING;
        } else if ("int".equals(parserName)) {
            return Constants.XSD_INT;
        } else if ("long".equals(parserName)) {
            return Constants.XSD_LONG;
        } else if (XD_PARSER_DEC.equals(parserName)) {
            return Constants.XSD_DECIMAL;
        } else if ("double".equals(parserName)) {
            return Constants.XSD_DOUBLE;
        } else if ("float".equals(parserName)) {
            return Constants.XSD_FLOAT;
        } else if ("enum".equals(parserName)) {
            return Constants.XSD_STRING;
        } else if ("gMonth".equals(parserName)) {
            return Constants.XSD_MONTH;
        } else if ("gMonthDay".equals(parserName)) {
            return Constants.XSD_MONTHDAY;
        } else if ("gYear".equals(parserName) || "ISOyear".equals(parserName)) {
            return Constants.XSD_YEAR;
        } else if ("gDay".equals(parserName)) {
            return Constants.XSD_DAY;
        } else if ("time".equals(parserName)) {
            return Constants.XSD_TIME;
        } else if ("dateTime".equals(parserName) || "ISOdateTime".equals(parserName)) {
            return Constants.XSD_DATETIME;
        } else if ("ISOyearMonth".equals(parserName)) {
            return Constants.XSD_YEARMONTH;
        } else if ("ISOdate".equals(parserName) || "date".equals(parserName)) {
            return Constants.XSD_DATE;
        }

        return null;
    }

    public static Pair<QName, IXsdFacetFactory> getDefaultFacetBuilder(final String parserName) {
        QName qName = getDefaultQName(parserName);
        if (qName != null) {
            return new Pair(qName, new DefaultFacetFactory());
        }

        return null;
    }

    /**
     * Some xd types requires specific way how to create simpleType and restrictions
     * @param parserName x-definition parser name
     * @return  QName - qualified XML name
     *          Boolean - use also default facet facets factory
     */
    public static Pair<QName, IXsdFacetFactory> getCustomFacetBuilder(final String parserName, final XDNamedValue[] parameters) {
        if (XD_PARSER_AN.equals(parserName)) {
            return new Pair(Constants.XSD_STRING, new AnFacetFactory());
        } else if (XD_PARSER_NUM.equals(parserName)) {
            return new Pair(Constants.XSD_STRING, new NumFacetFactory());
        } else if (XD_PARSER_XDATETIME.equals(parserName)) {
            return new Pair(Constants.XSD_STRING, new DateTimeFacetFactory());
        } else if (XD_PARSER_LIST.equals(parserName)) {
            ListFacetFactory facetBuilder = new ListFacetFactory();
            return new Pair(facetBuilder.determineBaseType(parameters), facetBuilder);
        } else if (XD_PARSER_UNION.equals(parserName)) {
            UnionFacetFactory facetBuilder = new UnionFacetFactory();
            return new Pair(facetBuilder.determineBaseType(parameters), facetBuilder);
        }

        return null;
    }

    public static void addElement(final XmlSchema schema, final XmlSchemaElement element) {
        schema.getItems().add(element);
    }

    public static void addAttr(final XmlSchema schema, final XmlSchemaAttribute attr) {
        schema.getItems().add(attr);
    }

    public static void addSchemaType(final XmlSchema schema, final XmlSchemaType schemaType) {
        schema.getItems().add(schemaType);
    }

    public static QName getDefaultSimpleParserQName(final XData xData) {
        final XDValue parseMethod = xData.getParseMethod();
        final String parserName = xData.getParserName();

        QName defaultQName = XD2XsdUtils.getDefaultQName(parserName);

        if (defaultQName != null && parseMethod instanceof XDParser) {
            XDParser parser = ((XDParser)parseMethod);
            XDNamedValue parameters[] = parser.getNamedParams().getXDNamedItems();
            if (parameters.length == 0 && XD2XsdUtils.getCustomFacetBuilder(parserName, parameters) == null) {
                return defaultQName;
            }
        }

        return null;
    }

    public static String createNameFromParser(final XData xData) {
        final XDValue parseMethod = xData.getParseMethod();
        final String parserName = xData.getParserName();

        String name;
        QName defaultQName = XD2XsdUtils.getDefaultQName(parserName);
        if (defaultQName != null) {
            name = defaultQName.getLocalPart();
        } else {
            name = parserName;
        }

        if (!"string".equals(name) && !"CDATA".equals(name) && !"int".equals(name) && !"long".equals(name)) {
            return null;
        }

        if (parseMethod instanceof XDParser) {
            XDParser parser = ((XDParser)parseMethod);
            for (XDNamedValue p : parser.getNamedParams().getXDNamedItems()) {
                if ("maxLength".equals(p.getName())) {
                    name += "_maxl" + p.getValue().intValue();
                } else if ("minLength".equals(p.getName())) {
                    name += "_minl" + p.getValue().intValue();
                } else if ("whiteSpace".equals(p.getName())) {
                    name += "_w";
                } else if ("pattern".equals(p.getName()) || "format".equals(p.getName())) {
                    name += "_p";
                } else if ("minInclusive".equals(p.getName())) {
                    name += "_minI" + p.getValue().intValue();
                } else if ("minExclusive".equals(p.getName())) {
                    name += "_minE" + p.getValue().intValue();
                } else if ("maxInclusive".equals(p.getName())) {
                    name += "_maxI" + p.getValue().intValue();
                } else if ("maxExclusive".equals(p.getName())) {
                    name += "_maxE" + p.getValue().intValue();
                } else if ("argument".equals(p.getName()) || "enumeration".equals(p.getName())) {
                    name += "_e";
                } else if ("length".equals(p.getName())) {
                    name += "_l" + p.getValue().intValue();
                } else if ("fractionDigits".equals(p.getName())) {
                    name += "_fd";
                } else if ("totalDigits".equals(p.getName())) {
                    name += "_td";
                }
            }
        }

        return name;
    }

    public static String createNsPrefixFromXDefName(final String name) {
        return "prefxDef_" + name;
    }

    public static String createNsUriFromXDefName(final String name) {
        return name;
    }

    public static String createExternalSchemaNameFromNsPrefix(final String nsPrefix) {
        return "external_" + nsPrefix;
    }

    public static String getNsPrefixFromExternalSchemaName(final String nsPrefix) {
        int pos = nsPrefix.lastIndexOf('_');
        if (pos != -1) {
            return nsPrefix.substring(pos + 1);
        }

        return nsPrefix;
    }

    public static String particleXKindToString(short kind) {
        switch (kind) {
            case XMSEQUENCE: return "sequence";
            case XMMIXED: return "mixed";
            case XMCHOICE: return "choise";
        }

        return null;
    }

}
