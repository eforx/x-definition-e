package org.xdef.impl.util.conv.schema2xd.xsd.factory.declaration;

import org.apache.ws.commons.schema.*;
import org.xdef.impl.util.conv.schema.util.XsdLogger;
import org.xdef.impl.util.conv.xd2schema.xsd.definition.AlgPhase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.xdef.impl.util.conv.schema.util.XsdLoggerDefs.LOG_DEBUG;
import static org.xdef.impl.util.conv.schema.util.XsdLoggerDefs.LOG_INFO;
import static org.xdef.impl.util.conv.xd2schema.xsd.definition.AlgPhase.TRANSFORMATION;

public abstract class AbstractDeclarationTypeFactory implements IDeclarationTypeFactory {

    // TODO: enumeration?
    // TODO: default/fixed?
    protected static final String MIN_INCLUSIVE = "MIN_INCLUSIVE";
    protected static final String MIN_EXCLUSIVE = "MIN_EXCLUSIVE";
    protected static final String MAX_INCLUSIVE = "MAX_INCLUSIVE";
    protected static final String MAX_EXCLUSIVE = "MAX_EXCLUSIVE";
    protected static final String PATTERN = "PATTERN";
    protected static final String LENGTH = "LENGTH";
    protected static final String MIN_LENGTH = "MIN_LENGTH";
    protected static final String MAX_LENGTH = "MAX_LENGTH";
    protected static final String TOTAL_DIGITS = "TOTAL_DIGITS";
    protected static final String FRACTIONS_DIGITS = "FRACTIONS_DIGITS";
    protected static final String WHITESPACE = "WHITESPACE";

    protected String typeName = null;
    protected List<XmlSchemaFacet> facets = null;

    Map<String, String> facetValues = new HashMap<String, String>();
    protected boolean firstFacet;

    @Override
    public void setName(final String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String build(final List<XmlSchemaFacet> facets) {
        XsdLogger.print(LOG_INFO, TRANSFORMATION, typeName, "Building Declaration. Type=" + getDataType());

        this.facets = facets;
        parseFacets();

        final StringBuilder sb = new StringBuilder("type " + typeName + " " + getDataType());
        sb.append("(");
        buildFacets(sb);
        defaultBuildFacets(sb);
        sb.append(");");
        return sb.toString();
    }

    protected void parseFacets() {
        facetValues.clear();
        firstFacet = true;

        if (facets != null) {
            for (XmlSchemaFacet facet : facets) {
                if (facet instanceof XmlSchemaFractionDigitsFacet) {
                    facetValues.put(FRACTIONS_DIGITS, (String)(facet).getValue());
                    XsdLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add fraction digits. Value=" + (facet).getValue());
                } else if (facet instanceof XmlSchemaLengthFacet) {
                    facetValues.put(LENGTH, (String)(facet).getValue());
                    XsdLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add length. Value=" + (facet).getValue());
                } else if (facet instanceof XmlSchemaMaxExclusiveFacet) {
                    facetValues.put(MAX_EXCLUSIVE, (String)(facet).getValue());
                    XsdLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add max exclusive. Value=" + (facet).getValue());
                } else if (facet instanceof XmlSchemaMaxInclusiveFacet) {
                    facetValues.put(MAX_INCLUSIVE, (String)(facet).getValue());
                    XsdLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add max inclusive. Value=" + (facet).getValue());
                } else if (facet instanceof XmlSchemaMaxLengthFacet) {
                    facetValues.put(MAX_LENGTH, (String)(facet).getValue());
                    XsdLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add max length. Value=" + (facet).getValue());
                } else if (facet instanceof XmlSchemaMinLengthFacet) {
                    facetValues.put(MIN_LENGTH, (String)(facet).getValue());
                    XsdLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add min length. Value=" + (facet).getValue());
                } else if (facet instanceof XmlSchemaMinExclusiveFacet) {
                    facetValues.put(MIN_EXCLUSIVE, (String)(facet).getValue());
                    XsdLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add min exclusive. Value=" + (facet).getValue());
                } else if (facet instanceof XmlSchemaMinInclusiveFacet) {
                    facetValues.put(MIN_INCLUSIVE, (String)(facet).getValue());
                    XsdLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add min inclusive. Value=" + (facet).getValue());
                } else if (facet instanceof XmlSchemaPatternFacet) {
                    facetValues.put(PATTERN, (String)(facet).getValue());
                    XsdLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add pattern. Value=" + (facet).getValue());
                } else if (facet instanceof XmlSchemaTotalDigitsFacet) {
                    facetValues.put(TOTAL_DIGITS, (String)(facet).getValue());
                    XsdLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add total digits. Value=" + (facet).getValue());
                } else if (facet instanceof XmlSchemaWhiteSpaceFacet) {
                    facetValues.put(WHITESPACE, (String)(facet).getValue());
                    XsdLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add whitespace. Value=" + (facet).getValue());
                }
            }
        }
    }

    protected boolean hasFacet(final String facetName) {
        return facetValues.containsKey(facetName);
    }

    protected String useFacet(final String facetName) {
        return facetValues.remove(facetName);
    }

    protected void buildFacets(final StringBuilder sb) {
    }

    protected void facetBuilder(final StringBuilder sb, final String value) {
        if (!firstFacet) {
            sb.append("," + value);
        } else {
            sb.append(value);
            firstFacet = false;
        }
    }

    protected void defaultBuildFacets(final StringBuilder sb) {
        if (hasFacet(FRACTIONS_DIGITS)) {
            facetBuilder(sb, "%fractionDigits='" + useFacet(FRACTIONS_DIGITS) + "'");
        }
        if (hasFacet(LENGTH)) {
            facetBuilder(sb, "%length='" + useFacet(LENGTH) + "'");
        }
        if (hasFacet(MAX_EXCLUSIVE)) {
            facetBuilder(sb, "%maxExclusive='" + useFacet(MAX_EXCLUSIVE) + "'");
        }
        if (hasFacet(MAX_INCLUSIVE)) {
            facetBuilder(sb, "%maxInclusive='" + useFacet(MAX_INCLUSIVE) + "'");
        }
        if (hasFacet(MAX_LENGTH)) {
            facetBuilder(sb, "%maxLength='" + useFacet(MAX_LENGTH) + "'");
        }
        if (hasFacet(MIN_LENGTH)) {
            facetBuilder(sb, "%minLength='" + useFacet(MIN_LENGTH) + "'");
        }
        if (hasFacet(MIN_EXCLUSIVE)) {
            facetBuilder(sb, "%minExclusive='" + useFacet(MIN_EXCLUSIVE) + "'");
        }
        if (hasFacet(MIN_INCLUSIVE)) {
            facetBuilder(sb, "%minInclusive='" + useFacet(MIN_INCLUSIVE) + "'");
        }
        if (hasFacet(PATTERN)) {
            facetBuilder(sb, "%pattern=['" + useFacet(PATTERN).replace("\\", "\\\\") + "']");
        }
        if (hasFacet(TOTAL_DIGITS)) {
            facetBuilder(sb, "%totalDigits='" + useFacet(TOTAL_DIGITS) + "'");
        }
        if (hasFacet(WHITESPACE)) {
            facetBuilder(sb, "%whiteSpace='" + useFacet(WHITESPACE) + "'");
        }
    }
}
