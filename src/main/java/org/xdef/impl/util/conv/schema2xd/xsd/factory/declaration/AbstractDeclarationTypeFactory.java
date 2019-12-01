package org.xdef.impl.util.conv.schema2xd.xsd.factory.declaration;

import org.apache.ws.commons.schema.*;
import org.xdef.impl.util.conv.schema.util.XsdLogger;

import java.util.*;

import static org.xdef.impl.util.conv.schema.util.XsdLoggerDefs.*;
import static org.xdef.impl.util.conv.xd2schema.xsd.definition.AlgPhase.TRANSFORMATION;

public abstract class AbstractDeclarationTypeFactory implements IDeclarationTypeFactory {

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
    protected static final String ENUMERATION = "ENUMERATION";

    private Mode mode;
    protected String typeName = null;
    protected List<XmlSchemaFacet> facets = null;

    final Map<String, String> facetSingleValues = new HashMap<String, String>();
    final Map<String, List<String>> facetMultipleValues = new HashMap<String, List<String>>();
    protected boolean firstFacet;

    @Override
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    @Override
    public void setName(final String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String build(final List<XmlSchemaFacet> facets) {
        this.facets = facets;
        parseFacets();

        final String type = hasMultipleFacet(ENUMERATION) ? "enum" : getDataType();

        StringBuilder facetStringBuilder = new StringBuilder();
        buildFacets(facetStringBuilder);
        defaultBuildFacets(facetStringBuilder);
        return build(type, facetStringBuilder.toString());
    }

    @Override
    public String build(String facets) {
        return build(getDataType(), facets);
    }

    @Override
    public String build(final String type, final String facets) {
        StringBuilder sb = new StringBuilder();

        if (Mode.NAMED_DECL.equals(mode)) {
            XsdLogger.print(LOG_INFO, TRANSFORMATION, typeName, "Building top declaration. Type=" + type);
            sb.append("type " + typeName + " " + type);
        } else if (Mode.TEXT_DECL.equals(mode)) {
            XsdLogger.print(LOG_INFO, TRANSFORMATION, null, "Building text declaration. Type=" + type);
            sb.append("required " + type);
        } else if (Mode.DATATYPE_DECL.equals(mode)) {
            XsdLogger.print(LOG_INFO, TRANSFORMATION, null, "Building data type declaration. Type=" + type);
            sb.append(type);
        }

        sb.append("(");
        sb.append(facets);
        sb.append(")");
        if (!Mode.DATATYPE_DECL.equals(mode)) {
            sb.append(";");
        }

        reset();

        return sb.toString();
    }

    protected void parseFacets() {
        reset();

        if (facets != null) {
            for (XmlSchemaFacet facet : facets) {
                if (facet instanceof XmlSchemaFractionDigitsFacet) {
                    facetSingleValues.put(FRACTIONS_DIGITS, (String)(facet).getValue());
                    XsdLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add fraction digits. Value=" + facet.getValue());
                } else if (facet instanceof XmlSchemaLengthFacet) {
                    facetSingleValues.put(LENGTH, (String)facet.getValue());
                    XsdLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add length. Value=" + facet.getValue());
                } else if (facet instanceof XmlSchemaMaxExclusiveFacet) {
                    facetSingleValues.put(MAX_EXCLUSIVE, (String)(facet).getValue());
                    XsdLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add max exclusive. Value=" + facet.getValue());
                } else if (facet instanceof XmlSchemaMaxInclusiveFacet) {
                    facetSingleValues.put(MAX_INCLUSIVE, (String)(facet).getValue());
                    XsdLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add max inclusive. Value=" + facet.getValue());
                } else if (facet instanceof XmlSchemaMaxLengthFacet) {
                    facetSingleValues.put(MAX_LENGTH, (String)(facet).getValue());
                    XsdLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add max length. Value=" + facet.getValue());
                } else if (facet instanceof XmlSchemaMinLengthFacet) {
                    facetSingleValues.put(MIN_LENGTH, (String)(facet).getValue());
                    XsdLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add min length. Value=" + facet.getValue());
                } else if (facet instanceof XmlSchemaMinExclusiveFacet) {
                    facetSingleValues.put(MIN_EXCLUSIVE, (String)(facet).getValue());
                    XsdLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add min exclusive. Value=" + facet.getValue());
                } else if (facet instanceof XmlSchemaMinInclusiveFacet) {
                    facetSingleValues.put(MIN_INCLUSIVE, (String)(facet).getValue());
                    XsdLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add min inclusive. Value=" + facet.getValue());
                } else if (facet instanceof XmlSchemaPatternFacet) {
                    facetSingleValues.put(PATTERN, (String)(facet).getValue());
                    XsdLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add pattern. Value=" + facet.getValue());
                } else if (facet instanceof XmlSchemaTotalDigitsFacet) {
                    facetSingleValues.put(TOTAL_DIGITS, (String)(facet).getValue());
                    XsdLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add total digits. Value=" + facet.getValue());
                } else if (facet instanceof XmlSchemaWhiteSpaceFacet) {
                    facetSingleValues.put(WHITESPACE, (String)(facet).getValue());
                    XsdLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add whitespace. Value=" + facet.getValue());
                } else if (facet instanceof XmlSchemaEnumerationFacet) {
                    List<String> enumeration = facetMultipleValues.get(ENUMERATION);
                    if (enumeration == null) {
                        enumeration = new LinkedList<String>();
                        facetMultipleValues.put(ENUMERATION, enumeration);
                    }

                    enumeration.add((String)(facet).getValue());
                    XsdLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add enumeration. Value=" + facet.getValue());
                } else {
                    XsdLogger.print(LOG_WARN, TRANSFORMATION, typeName, "Declaration - Unsupported XSD facet! Clazz=" + facet.getClass().getSimpleName());
                }
            }
        }
    }

    protected boolean hasFacet(final String facetName) {
        return facetSingleValues.containsKey(facetName);
    }

    protected String useFacet(final String facetName) {
        return facetSingleValues.remove(facetName);
    }

    protected boolean hasMultipleFacet(final String facetName) {
        return facetMultipleValues.containsKey(facetName);
    }

    protected List<String> useMultipleFacet(final String facetName) {
        return facetMultipleValues.remove(facetName);
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
        if (hasMultipleFacet(ENUMERATION)) {
            final List<String> enumeration = useMultipleFacet(ENUMERATION);
            if (enumeration != null && !enumeration.isEmpty()) {
                Iterator<String> enumItr = enumeration.iterator();
                sb.append("\"" + enumItr.next() + "\"");
                while (enumItr.hasNext()) {
                    sb.append(", \"" + enumItr.next() + "\"");
                }
            }
        }
    }

    private void reset() {
        facetSingleValues.clear();
        facetMultipleValues.clear();
        firstFacet = true;
    }
}
