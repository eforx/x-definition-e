package org.xdef.impl.util.conv.xd2schemas.xsd.util;

import javafx.util.Pair;
import org.apache.ws.commons.schema.constants.Constants;
import org.xdef.XDNamedValue;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.impl.XData;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.DefaultFacetFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.IXsdFacetFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.array.ListFacetFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.xdef.TokensFacetFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.array.UnionFacetFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.xdef.*;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XD2XsdDefinitions.*;

public class XD2XsdParserMapping {

    private static final Map<String, QName> defaultQNameMap = new HashMap<String, QName>();
    private static final Map<String, Pair<QName, IXsdFacetFactory>> customFacetMap = new HashMap<String, Pair<QName, IXsdFacetFactory>>();

    static {
        // Default parsers - custom x-definition names
        defaultQNameMap.put(XD_PARSER_CDATA, Constants.XSD_STRING);
        defaultQNameMap.put(XD_PARSER_ISODATE, Constants.XSD_DATE);
        defaultQNameMap.put(XD_PARSER_ISODATETIME, Constants.XSD_DATETIME);
        defaultQNameMap.put(XD_PARSER_ISOYEARMONTH, Constants.XSD_YEARMONTH);
        defaultQNameMap.put(XD_PARSER_DEC, Constants.XSD_DECIMAL);
        defaultQNameMap.put(XD_PARSER_ISOYEAR, Constants.XSD_YEAR);
        defaultQNameMap.put(XD_PARSER_REGEX, Constants.XSD_STRING);

        // Default parsers
        defaultQNameMap.put(Constants.XSD_BASE64.getLocalPart(), Constants.XSD_BASE64);
        defaultQNameMap.put(Constants.XSD_BOOLEAN.getLocalPart(), Constants.XSD_BOOLEAN);
        defaultQNameMap.put(Constants.XSD_DATE.getLocalPart(), Constants.XSD_DATE);
        defaultQNameMap.put(Constants.XSD_DATETIME.getLocalPart(), Constants.XSD_DATETIME);
        defaultQNameMap.put(Constants.XSD_DAY.getLocalPart(), Constants.XSD_DAY);
        defaultQNameMap.put(Constants.XSD_DOUBLE.getLocalPart(), Constants.XSD_DOUBLE);
        defaultQNameMap.put(Constants.XSD_DURATION.getLocalPart(), Constants.XSD_DURATION);
        defaultQNameMap.put(Constants.XSD_ENTITY.getLocalPart(), Constants.XSD_ENTITY);
        defaultQNameMap.put(Constants.XSD_ENTITIES.getLocalPart(), Constants.XSD_ENTITIES);
        defaultQNameMap.put(Constants.XSD_FLOAT.getLocalPart(), Constants.XSD_FLOAT);
        defaultQNameMap.put(Constants.XSD_HEXBIN.getLocalPart(), Constants.XSD_HEXBIN);
        defaultQNameMap.put(Constants.XSD_ID.getLocalPart(), Constants.XSD_ID);
        defaultQNameMap.put(Constants.XSD_IDREF.getLocalPart(), Constants.XSD_IDREF);
        defaultQNameMap.put(Constants.XSD_IDREFS.getLocalPart(), Constants.XSD_IDREFS);
        defaultQNameMap.put(Constants.XSD_INT.getLocalPart(), Constants.XSD_INT);
        defaultQNameMap.put(Constants.XSD_LANGUAGE.getLocalPart(), Constants.XSD_LANGUAGE);
        defaultQNameMap.put(Constants.XSD_LONG.getLocalPart(), Constants.XSD_LONG);
        defaultQNameMap.put(Constants.XSD_MONTH.getLocalPart(), Constants.XSD_MONTH);
        defaultQNameMap.put(Constants.XSD_MONTHDAY.getLocalPart(), Constants.XSD_MONTHDAY);
        defaultQNameMap.put(Constants.XSD_NCNAME.getLocalPart(), Constants.XSD_NCNAME);
        defaultQNameMap.put(Constants.XSD_NMTOKEN.getLocalPart(), Constants.XSD_NMTOKEN);
        defaultQNameMap.put(Constants.XSD_NMTOKENS.getLocalPart(), Constants.XSD_NMTOKENS);
        defaultQNameMap.put(Constants.XSD_NORMALIZEDSTRING.getLocalPart(), Constants.XSD_NORMALIZEDSTRING);
        defaultQNameMap.put(Constants.XSD_QNAME.getLocalPart(), Constants.XSD_QNAME);
        defaultQNameMap.put(Constants.XSD_STRING.getLocalPart(), Constants.XSD_STRING);
        defaultQNameMap.put(Constants.XSD_TIME.getLocalPart(), Constants.XSD_TIME);
        defaultQNameMap.put(Constants.XSD_TOKEN.getLocalPart(), Constants.XSD_TOKEN);
        defaultQNameMap.put(Constants.XSD_YEAR.getLocalPart(), Constants.XSD_YEAR);

        // Custom static facets
        customFacetMap.put(AnFacetFactory.XD_PARSER_NAME, new Pair(Constants.XSD_STRING, new AnFacetFactory()));
        customFacetMap.put(ContainerFacetFactory.XD_PARSER_NAME_LANGUAGES, new Pair(Constants.XSD_STRING, new ContainerFacetFactory("[a-zA-Z0-9]{2,3}")));
        customFacetMap.put(ContainerFacetFactory.XD_PARSER_NAME_NC_NAMELIST, new Pair(Constants.XSD_STRING, new ContainerFacetFactory("[a-zA-Z0-9]+")));
        customFacetMap.put(ContainsFacetFactory.XD_PARSER_NAME, new Pair(Constants.XSD_STRING, new ContainsFacetFactory(true)));
        customFacetMap.put(ContainsFacetFactory.XD_PARSER_CI_NAME, new Pair(Constants.XSD_STRING, new ContainsFacetFactory(false)));
        customFacetMap.put(EnumFacetFactory.XD_PARSER_NAME, new Pair(Constants.XSD_STRING, new EnumFacetFactory()));
        customFacetMap.put(DateTimeFormatFacetFactory.XD_PARSER_XDATETIME_NAME, new Pair(Constants.XSD_STRING, new DateTimeFormatFacetFactory()));
        customFacetMap.put(DateTimeFormatFacetFactory.XD_PARSER_DATETIME_NAME, new Pair(Constants.XSD_STRING, new DateTimeFormatFacetFactory()));
        customFacetMap.put(DateTimeFormatFacetFactory.XD_PARSER_EMAILDATE_NAME, new Pair(Constants.XSD_STRING, new DateTimeFormatFacetFactory()));
        customFacetMap.put(EndsFacetFactory.XD_PARSER_NAME, new Pair(Constants.XSD_STRING, new EndsFacetFactory(true)));
        customFacetMap.put(EndsFacetFactory.XD_PARSER_CI_NAME, new Pair(Constants.XSD_STRING, new EndsFacetFactory(false)));
        customFacetMap.put(EqFacetFactory.XD_PARSER_NAME, new Pair(Constants.XSD_STRING, new EqFacetFactory(true)));
        customFacetMap.put(EqFacetFactory.XD_PARSER_CI_NAME, new Pair(Constants.XSD_STRING, new EqFacetFactory(false)));
        customFacetMap.put(NumFacetFactory.XD_PARSER_NAME, new Pair(Constants.XSD_STRING, new NumFacetFactory()));
        customFacetMap.put(MD5FacetFactory.XD_PARSER_NAME, new Pair(Constants.XSD_STRING, new MD5FacetFactory()));
        customFacetMap.put(RegexFacetFactory.XD_PARSER_NAME, new Pair(Constants.XSD_STRING, new RegexFacetFactory()));
        customFacetMap.put(StartsFacetFactory.XD_PARSER_NAME, new Pair(Constants.XSD_STRING, new StartsFacetFactory(true)));
        customFacetMap.put(StartsFacetFactory.XD_PARSER_CI_NAME, new Pair(Constants.XSD_STRING, new StartsFacetFactory(false)));
        customFacetMap.put(TokensFacetFactory.XD_PARSER_NAME, new Pair(Constants.XSD_STRING, new TokensFacetFactory(true)));
        customFacetMap.put(TokensFacetFactory.XD_PARSER_CI_NAME, new Pair(Constants.XSD_STRING, new TokensFacetFactory(false)));

    }

    /**
     * Convert xd parser name to xsd QName
     * @param parserName
     * @return
     */
    public static QName getDefaultParserQName(final String parserName) {
        return defaultQNameMap.get(parserName);
    }

    /**
     * Some xd types requires specific way how to create simpleType and restrictions
     * @param parserName x-definition parser name
     * @return  QName - qualified XML name
     *          Boolean - use also default facet facets factory
     */
    public static Pair<QName, IXsdFacetFactory> getCustomFacetFactory(final String parserName, final XDNamedValue[] parameters) {
        Pair<QName, IXsdFacetFactory> res = customFacetMap.get(parserName);
        // Custom dynamic facets
        if (res == null) {
            if (ListFacetFactory.XD_PARSER_NAME.equals(parserName) || ListFacetFactory.XD_PARSER_CI_NAME.equals(parserName)) {
                ListFacetFactory facetBuilder = new ListFacetFactory(ListFacetFactory.XD_PARSER_NAME.equals(parserName));
                res = new Pair(facetBuilder.determineBaseType(parameters), facetBuilder);
            } else if (UnionFacetFactory.XD_PARSER_NAME.equals(parserName)) {
                UnionFacetFactory facetBuilder = new UnionFacetFactory();
                res = new Pair(facetBuilder.determineBaseType(parameters), facetBuilder);
            }
        }

        return res;
    }

    public static Pair<QName, IXsdFacetFactory> getDefaultFacetFactory(final String parserName) {
        QName qName = getDefaultParserQName(parserName);
        if (qName != null) {
            return new Pair(qName, new DefaultFacetFactory());
        }

        return null;
    }

    public static QName getDefaultSimpleParserQName(final XData xData) {
        final XDValue parseMethod = xData.getParseMethod();
        final String parserName = xData.getParserName();

        QName defaultQName = getDefaultParserQName(parserName);

        if (defaultQName != null && parseMethod instanceof XDParser) {
            XDParser parser = ((XDParser)parseMethod);
            XDNamedValue parameters[] = parser.getNamedParams().getXDNamedItems();
            if (parameters.length == 0 && getCustomFacetFactory(parserName, parameters) == null) {
                return defaultQName;
            }
        }

        return null;
    }
}
