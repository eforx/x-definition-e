package org.xdef.impl.util.conv.schema.xd2schema.xsd.util;

import javafx.util.Pair;
import org.apache.ws.commons.schema.constants.Constants;
import org.xdef.XDNamedValue;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.impl.XData;
import org.xdef.impl.util.conv.schema.util.SchemaLogger;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.Xd2XsdFeature;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.factory.facet.DefaultFacetFactory;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.factory.facet.IXsdFacetFactory;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.factory.facet.pattern.ListRegexFacetFactory;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.factory.facet.pattern.TokensRegexFacetFactory;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.factory.facet.xdef.TokensFacetFactory;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.factory.facet.xdef.*;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.model.XsdAdapterCtx;
import org.xdef.msg.XSD;
import org.xdef.sys.SRuntimeException;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

import static org.xdef.XDValueID.XD_CONTAINER;
import static org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.AlgPhase.TRANSFORMATION;
import static org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.Xd2XsdDefinitions.*;
import static org.xdef.impl.util.conv.schema.util.SchemaLoggerDefs.*;

/**
 * Definition of transformation x-definition data types to XSD data types
 */
public class Xd2XsdParserMapping {

    /**
     * Transformation map of x-definition data types to XSD QNames using XSD default facet factory
     */
    private static final Map<String, QName> defaultQNameMap = new HashMap<String, QName>();

    /**
     * Transformation map of x-definition parsers to XSD QNames and custom implementation of XSD facet factory
     * Some x-definition types requires specific way how to create simpleType and restrictions
     */
    private static final Map<String, Pair<QName, IXsdFacetFactory>> customFacetMap = new HashMap<String, Pair<QName, IXsdFacetFactory>>();

    static {
        // Default parsers - custom x-definition names
        defaultQNameMap.put(XD_PARSER_CDATA, Constants.XSD_STRING);
        defaultQNameMap.put(XD_PARSER_ISODATE, Constants.XSD_DATE);
        defaultQNameMap.put(XD_PARSER_ISODATETIME, Constants.XSD_DATETIME);
        defaultQNameMap.put(XD_PARSER_ISOYEARMONTH, Constants.XSD_YEARMONTH);
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
        customFacetMap.put(DateTimeFormatFacetFactory.XD_PARSER_DATETIME_NAME, new Pair(Constants.XSD_STRING, new DateTimeFormatFacetFactory("yyyyMMddHHmmss")));
        customFacetMap.put(DateTimeFormatFacetFactory.XD_PARSER_EMAILDATE_NAME, new Pair(Constants.XSD_STRING, new DateTimeFormatFacetFactory("EEE, d MMM y HH:mm:ss[ ZZZZZ][ (z)]")));
        customFacetMap.put(EndsFacetFactory.XD_PARSER_NAME, new Pair(Constants.XSD_STRING, new EndsFacetFactory(true)));
        customFacetMap.put(EndsFacetFactory.XD_PARSER_CI_NAME, new Pair(Constants.XSD_STRING, new EndsFacetFactory(false)));
        customFacetMap.put(EqFacetFactory.XD_PARSER_NAME, new Pair(Constants.XSD_STRING, new EqFacetFactory(true)));
        customFacetMap.put(EqFacetFactory.XD_PARSER_CI_NAME, new Pair(Constants.XSD_STRING, new EqFacetFactory(false)));
        customFacetMap.put(MD5FacetFactory.XD_PARSER_NAME, new Pair(Constants.XSD_STRING, new MD5FacetFactory()));
        customFacetMap.put(NumFacetFactory.XD_PARSER_NAME, new Pair(Constants.XSD_STRING, new NumFacetFactory()));
        customFacetMap.put(RegexFacetFactory.XD_PARSER_NAME, new Pair(Constants.XSD_STRING, new RegexFacetFactory()));
        customFacetMap.put(StartsFacetFactory.XD_PARSER_NAME, new Pair(Constants.XSD_STRING, new StartsFacetFactory(true)));
        customFacetMap.put(StartsFacetFactory.XD_PARSER_CI_NAME, new Pair(Constants.XSD_STRING, new StartsFacetFactory(false)));
        customFacetMap.put(TokensFacetFactory.XD_PARSER_NAME, new Pair(Constants.XSD_STRING, new TokensFacetFactory()));
        customFacetMap.put(TokensRegexFacetFactory.XD_PARSER_CI_NAME, new Pair(Constants.XSD_STRING, new TokensRegexFacetFactory()));

    }

    /**
     * Converts x-definition parser name to XSD qualified name
     * @param parserName    x-definition parser name
     * @return  XSD QName if mapping exists, otherwise null
     */
    public static QName findDefaultParserQName(final String parserName, final XsdAdapterCtx adapterCtx) {
        QName qName = defaultQNameMap.get(parserName);
        if (qName == null) {
            if (DecFacetFactory.XD_PARSER_NAME.equals(parserName) && !adapterCtx.hasEnableFeature(Xd2XsdFeature.XSD_DECIMAL_ANY_SEPARATOR)) {
                return Constants.XSD_DECIMAL;
            }
        }

        return qName;
    }

    /**
     * Finds XSD qualified name and XSD facet factory for specific x-definition parser
     * @param parserName    x-definition parser name
     * @return  XSD data type with XSD facets factory if transformation exists
     *          otherwise null
     */
    public static Pair<QName, IXsdFacetFactory> findCustomFacetFactory(final String parserName, final XDNamedValue[] parameters, final XsdAdapterCtx adapterCtx) {
        Pair<QName, IXsdFacetFactory> res = customFacetMap.get(parserName);
        // Custom dynamic facet factories
        if (res == null) {
            if (DecFacetFactory.XD_PARSER_NAME.equals(parserName) && adapterCtx.hasEnableFeature(Xd2XsdFeature.XSD_DECIMAL_ANY_SEPARATOR)) {
                res = new Pair(Constants.XSD_STRING, new DecFacetFactory());
            } else if (ListFacetFactory.XD_PARSER_NAME.equals(parserName)) {
                final QName qName = determineListBaseType(parameters, adapterCtx);
                res = new Pair(qName, new ListFacetFactory());
            } else if (UnionFacetFactory.XD_PARSER_NAME.equals(parserName)) {
                res = new Pair(null, new UnionFacetFactory());
            } else if (/*ListRegexFacetFactory.XD_PARSER_NAME.equals(parserName) || */ListRegexFacetFactory.XD_PARSER_CI_NAME.equals(parserName)) {
                ListRegexFacetFactory facetBuilder = new ListRegexFacetFactory(ListRegexFacetFactory.XD_PARSER_NAME.equals(parserName));
                res = new Pair(facetBuilder.determineBaseType(parameters), facetBuilder);
            }/* else if (UnionRegexFacetFactory.XD_PARSER_NAME.equals(parserName)) {
                UnionRegexFacetFactory facetBuilder = new UnionRegexFacetFactory();
                res = new Pair(facetBuilder.determineBaseType(parameters), facetBuilder);
            }*/
        }

        return res;
    }

    /**
     * Converts given x-definition parser name to XSD qualified name
     * @param parserName    x-definition parser name
     * @param adapterCtx    XSD adapter context
     * @return  XSD QName with default facet factory if conversion of x-definition parser name to XSD QName exists
     *          otherwise null
     */
    public static Pair<QName, IXsdFacetFactory> findDefaultFacetFactory(final String parserName, final XsdAdapterCtx adapterCtx) {
        final QName qName = findDefaultParserQName(parserName, adapterCtx);
        if (qName != null) {
            return new Pair(qName, new DefaultFacetFactory());
        }

        return null;
    }

    /**
     * Get QName for x-definition parser which can be transformed by default XSD facet factory
     * @param xData         x-definition node
     * @param adapterCtx    XSD adapter context
     * @param hasNoFacets   check if parser contains any facet
     * @return XSD QName if transformation without facets exists, otherwise null
     */
    public static QName getDefaultParserQName(final XData xData, final XsdAdapterCtx adapterCtx, boolean hasNoFacets) {
        final XDValue parseMethod = xData.getParseMethod();
        final String parserName = xData.getParserName();
        final QName defaultQName = findDefaultParserQName(parserName, adapterCtx);

        if (defaultQName != null) {
            if (parseMethod instanceof XDParser) {
                final XDParser parser = ((XDParser) parseMethod);
                final XDNamedValue parameters[] = parser.getNamedParams().getXDNamedItems();
                if (findCustomFacetFactory(parserName, parameters, adapterCtx) == null) {
                    if ((hasNoFacets && parameters.length == 0) || !hasNoFacets)
                        return defaultQName;
                }
            } else {
                return defaultQName;
            }
        }

        return null;
    }

    /**
     * Determine XSD list's qualified names by its values (given by x-definition {@paramref parameters})
     * @param parameters    x-definition parser parameters
     * @param adapterCtx    XSD adapter context
     * @return  if all parameters are using same known parser, then its QName
     *          if parameters are using different known parser, then string QName
     *          if parameters are using unknown parser, then exception will be raised
     */
    private static QName determineListBaseType(final XDNamedValue[] parameters, final XsdAdapterCtx adapterCtx) {
        SchemaLogger.printP(LOG_DEBUG, TRANSFORMATION, "Determination of list QName ...");

        String parserName = null;
        boolean allParsersSame = true;

        for (int i = 0; i < parameters.length; i++) {
            XDValue xVal = parameters[i].getValue();
            if (xVal.getItemId() == XD_CONTAINER) {
                allParsersSame = false;
            } else if (xVal instanceof XDParser) {
                if (parserName == null) {
                    parserName = ((XDParser) xVal).parserName();
                } else {
                    if (allParsersSame == true && !parserName.equals(((XDParser) xVal).parserName())) {
                        SchemaLogger.printP(LOG_DEBUG, TRANSFORMATION, "List/Union - parsers are not same!");
                        allParsersSame = false;
                    }
                }
            }
        }

        if (parserName == null || allParsersSame == false) {
            SchemaLogger.printP(LOG_ERROR, TRANSFORMATION, "Expected parser type or multiple parsers used!");
            throw new SRuntimeException(XSD.XSD006);
        }

        QName res = findDefaultParserQName(parserName, adapterCtx);
        if (res == null) {
            adapterCtx.getReportWriter().warning(XSD.XSD026, parserName);
            SchemaLogger.printP(LOG_WARN, TRANSFORMATION, "Unsupported simple content parser! Parser=" + parserName);
            res = Constants.XSD_STRING;
        }

        return res;
    }

}
