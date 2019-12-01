package org.xdef.impl.util.conv.schema2xd.xsd.factory.declaration;

import org.apache.ws.commons.schema.*;
import org.xdef.impl.util.conv.schema.util.XsdLogger;

import java.util.*;

import static org.xdef.impl.util.conv.schema.util.XsdLoggerDefs.*;
import static org.xdef.impl.util.conv.xd2schema.xsd.definition.AlgPhase.TRANSFORMATION;

public abstract class AbstractDeclarationTypeFactory implements IDeclarationTypeFactory {

    // TODO: default/fixed value?

    private Mode mode;
    protected String typeName = null;
    protected List<XmlSchemaFacet> facets = null;

    private final Map<String, Object> facetSingleValues = new HashMap<String, Object>();
    private final Map<String, List<Object>> facetMultipleValues = new HashMap<String, List<Object>>();
    private boolean firstFacet;
    protected Set<String> facetsToRemove = null;

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

        if (facetsToRemove != null) {
            for (String facet : facetsToRemove) {
                facetSingleValues.remove(facet);
                facetMultipleValues.remove(facet);
            }
        }

        final String type = hasMultipleFacet(FACET_ENUMERATION) ? "enum" : getDataType();

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

    public AbstractDeclarationTypeFactory removeFacet(final String facetToRemove) {
        if (this.facetsToRemove == null) {
            this.facetsToRemove = new HashSet<String>();
        }
        this.facetsToRemove.add(facetToRemove);
        return this;
    }

    public AbstractDeclarationTypeFactory removeFacets(final Set<String> facetsToRemove) {
        if (this.facetsToRemove == null) {
            this.facetsToRemove = new HashSet<String>();
        }
        this.facetsToRemove.addAll(facetsToRemove);
        return this;
    }

    protected void parseFacets() {
        reset();

        if (facets != null) {
            for (XmlSchemaFacet facet : facets) {
                if (facet instanceof XmlSchemaFractionDigitsFacet) {
                    facetSingleValues.put(FACET_FRACTIONS_DIGITS, (facet).getValue());
                    XsdLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add fraction digits. Value=" + facet.getValue());
                } else if (facet instanceof XmlSchemaLengthFacet) {
                    facetSingleValues.put(FACET_LENGTH, facet.getValue());
                    XsdLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add length. Value=" + facet.getValue());
                } else if (facet instanceof XmlSchemaMaxExclusiveFacet) {
                    facetSingleValues.put(FACET_MAX_EXCLUSIVE, (facet).getValue());
                    XsdLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add max exclusive. Value=" + facet.getValue());
                } else if (facet instanceof XmlSchemaMaxInclusiveFacet) {
                    facetSingleValues.put(FACET_MAX_INCLUSIVE, (facet).getValue());
                    XsdLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add max inclusive. Value=" + facet.getValue());
                } else if (facet instanceof XmlSchemaMaxLengthFacet) {
                    facetSingleValues.put(FACET_MAX_LENGTH, (facet).getValue());
                    XsdLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add max length. Value=" + facet.getValue());
                } else if (facet instanceof XmlSchemaMinLengthFacet) {
                    facetSingleValues.put(FACET_MIN_LENGTH, (facet).getValue());
                    XsdLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add min length. Value=" + facet.getValue());
                } else if (facet instanceof XmlSchemaMinExclusiveFacet) {
                    facetSingleValues.put(FACET_MIN_EXCLUSIVE, (facet).getValue());
                    XsdLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add min exclusive. Value=" + facet.getValue());
                } else if (facet instanceof XmlSchemaMinInclusiveFacet) {
                    facetSingleValues.put(FACET_MIN_INCLUSIVE, (facet).getValue());
                    XsdLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add min inclusive. Value=" + facet.getValue());
                } else if (facet instanceof XmlSchemaPatternFacet) {
                    facetSingleValues.put(FACET_PATTERN, (facet).getValue());
                    XsdLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add pattern. Value=" + facet.getValue());
                } else if (facet instanceof XmlSchemaTotalDigitsFacet) {
                    facetSingleValues.put(FACET_TOTAL_DIGITS, (facet).getValue());
                    XsdLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add total digits. Value=" + facet.getValue());
                } else if (facet instanceof XmlSchemaWhiteSpaceFacet) {
                    facetSingleValues.put(FACET_WHITESPACE, (facet).getValue());
                    XsdLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add whitespace. Value=" + facet.getValue());
                } else if (facet instanceof XmlSchemaEnumerationFacet) {
                    List<Object> enumeration = facetMultipleValues.get(FACET_ENUMERATION);
                    if (enumeration == null) {
                        enumeration = new LinkedList<Object>();
                        facetMultipleValues.put(FACET_ENUMERATION, enumeration);
                    }

                    enumeration.add((facet).getValue());
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

    protected Object useFacet(final String facetName) {
        return facetSingleValues.remove(facetName);
    }

    protected Object getFacet(final String facetName) {
        return facetSingleValues.get(facetName);
    }

    protected boolean hasMultipleFacet(final String facetName) {
        return facetMultipleValues.containsKey(facetName);
    }

    protected List<Object> useMultipleFacet(final String facetName) {
        return facetMultipleValues.remove(facetName);
    }

    protected void buildFacets(final StringBuilder sb) {
    }

    protected void facetBuilder(final StringBuilder sb, final Object value) {
        if (!firstFacet) {
            sb.append("," + value);
        } else {
            sb.append(value);
            firstFacet = false;
        }
    }

    protected void defaultBuildFacets(final StringBuilder sb) {
        if (hasFacet(FACET_FRACTIONS_DIGITS)) {
            facetBuilder(sb, "%fractionDigits='" + useFacet(FACET_FRACTIONS_DIGITS) + "'");
        }
        if (hasFacet(FACET_LENGTH)) {
            facetBuilder(sb, "%length='" + useFacet(FACET_LENGTH) + "'");
        }
        if (hasFacet(FACET_MAX_EXCLUSIVE)) {
            facetBuilder(sb, "%maxExclusive='" + useFacet(FACET_MAX_EXCLUSIVE) + "'");
        }
        if (hasFacet(FACET_MAX_INCLUSIVE)) {
            facetBuilder(sb, "%maxInclusive='" + useFacet(FACET_MAX_INCLUSIVE) + "'");
        }
        if (hasFacet(FACET_MAX_LENGTH)) {
            facetBuilder(sb, "%maxLength='" + useFacet(FACET_MAX_LENGTH) + "'");
        }
        if (hasFacet(FACET_MIN_LENGTH)) {
            facetBuilder(sb, "%minLength='" + useFacet(FACET_MIN_LENGTH) + "'");
        }
        if (hasFacet(FACET_MIN_EXCLUSIVE)) {
            facetBuilder(sb, "%minExclusive='" + useFacet(FACET_MIN_EXCLUSIVE) + "'");
        }
        if (hasFacet(FACET_MIN_INCLUSIVE)) {
            facetBuilder(sb, "%minInclusive='" + useFacet(FACET_MIN_INCLUSIVE) + "'");
        }
        if (hasFacet(FACET_PATTERN)) {
            facetBuilder(sb, "%pattern=['" + useFacet(FACET_PATTERN).toString().replace("\\", "\\\\") + "']");
        }
        if (hasFacet(FACET_TOTAL_DIGITS)) {
            facetBuilder(sb, "%totalDigits='" + useFacet(FACET_TOTAL_DIGITS) + "'");
        }
        if (hasFacet(FACET_WHITESPACE)) {
            facetBuilder(sb, "%whiteSpace='" + useFacet(FACET_WHITESPACE) + "'");
        }
        if (hasMultipleFacet(FACET_ENUMERATION)) {
            final List<Object> enumeration = useMultipleFacet(FACET_ENUMERATION);
            if (enumeration != null && !enumeration.isEmpty()) {
                Iterator<Object> enumItr = enumeration.iterator();
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
