package org.xdef.impl.util.conv.xd2schemas.xsd.definition;

/**
 * Definitions of constant used in x-definition -> XSD transform algorithm
 */
public interface XD2XsdDefinitions {

    String XSD_NAMESPACE_PREFIX_EMPTY = "";
    String XSD_DEFAULT_SCHEMA_NAMESPACE_PREFIX = "xs";

    /**
     * X-definition parsers mapped to default xsd parsers
     */

    String XD_PARSER_CDATA = "CDATA";
    String XD_PARSER_EQ = "eq";
    String XD_PARSER_ISODATE = "ISOdate";
    String XD_PARSER_ISODATETIME = "ISOdateTime";
    String XD_PARSER_ISOYEAR = "ISOyear";
    String XD_PARSER_ISOYEARMONTH = "ISOyearMonth";
    String XD_PARSER_REGEX = "regex";

    /**
     * XSD schema facets
     */
    String XSD_FACET_ENUMERATION = "enumeration";
    String XSD_FACET_FRACTION_DIGITS = "fractionDigits";
    String XSD_FACET_LENGTH = "length";
    String XSD_FACET_MAX_EXCLUSIVE = "maxExclusive";
    String XSD_FACET_MAX_INCLUSIVE = "maxInclusive";
    String XSD_FACET_MAX_LENGTH = "maxLength";
    String XSD_FACET_MIN_EXCLUSIVE = "minExclusive";
    String XSD_FACET_MIN_INCLUSIVE = "minInclusive";
    String XSD_FACET_MIN_LENGTH = "minLength";
    String XSD_FACET_PATTERN = "pattern";
    String XSD_FACET_TOTAL_DIGITS = "totalDigits";
    String XSD_FACET_WHITESPACE = "whiteSpace";

    /**
     * X-definition custom facets
     */
    String XD_FACET_ARGUMENT = "argument";
    String XD_FACET_FORMAT = "format";
    String XD_FACET_SEPARATOR = "separator";
    String XD_INTERNAL_FACET_OUTFORMAT = "outFormat";

    /**
     * X-definition uniqueSet types
     */

    String XD_UNIQUE_ID = "ID";
    String XD_UNIQUE_IDREF = "IDREF";
    String XD_UNIQUE_IDREFS = "IDREFS";
    String XD_UNIQUE_CHKID = "CHKID";
}
