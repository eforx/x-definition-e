package org.xdef.impl.util.conv.schema.schema2xd.xsd.factory.declaration;

import org.apache.ws.commons.schema.*;
import org.xdef.impl.util.conv.schema.util.SchemaLogger;

import java.util.*;

import static org.xdef.impl.util.conv.schema.util.SchemaLoggerDefs.*;
import static org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.AlgPhase.TRANSFORMATION;

/**
 * Base class for transformation of facets
 */
public abstract class AbstractDeclarationTypeFactory implements IDeclarationTypeFactory {

    /**
     * X-definition output declaration type
     */
    private Type type;

    /**
     * X-definition variable declaration name.
     * Used only with type={@link Type.TOP_DECL}
     */
    protected String typeName = null;

    /**
     * Map of facets with single value, which should be transformed.
     * Content is filled by {@link #parseFacets}
     */
    private final Map<String, Object> facetSingleValues = new HashMap<String, Object>();

    /**
     * Map of facets with possible multiple values, which should be transformed.
     * Content is filled by {@link #parseFacets}
     */
    private final Map<String, List<Object>> facetMultipleValues = new HashMap<String, List<Object>>();

    /**
     * Flag of x-definition declaration builder, if first XSD facet has been converted
     */
    private boolean builderFirstFacet;

    /**
     * Set of marked facet names, which should be removed or have been already transformed
     */
    private Set<String> facetsToRemove = null;

    @Override
    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public void setName(final String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String build(final List<XmlSchemaFacet> facets) {
        parseFacets(facets);

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

    /**
     * Creates x-definition declaration type restrictions based on given type and facet string
     * @param type      X-definition output declaration type
     * @param facets    X-definition facet string
     * @return x-definition declaration type
     */
    private String build(final String type, final String facets) {
        StringBuilder sb = new StringBuilder();

        if (Type.TOP_DECL.equals(this.type)) {
            SchemaLogger.print(LOG_INFO, TRANSFORMATION, typeName, "Building top declaration. Type=" + type);
            sb.append("type " + typeName + " " + type);
        } else if (Type.TEXT_DECL.equals(this.type)) {
            SchemaLogger.print(LOG_INFO, TRANSFORMATION, null, "Building text declaration. Type=" + type);
            sb.append("required " + type);
        } else if (Type.DATATYPE_DECL.equals(this.type)) {
            SchemaLogger.print(LOG_INFO, TRANSFORMATION, null, "Building data type declaration. Type=" + type);
            sb.append(type);
        }

        boolean useItemSyntax = facets != null && !facets.isEmpty() && (this instanceof ListTypeFactory || this instanceof UnionTypeFactory);
        boolean itemArraySyntax = this instanceof UnionTypeFactory;

        sb.append("(");
        if (useItemSyntax) {
            sb.append("%item=");
            if (itemArraySyntax) {
                sb.append("[");
            }
        }
        sb.append(facets);
        if (useItemSyntax && itemArraySyntax) {
            sb.append("]");
        }
        sb.append(")");
        if (!Type.DATATYPE_DECL.equals(this.type)) {
            sb.append(";");
        }

        reset();

        return sb.toString();
    }

    /**
     * Marks facet as resolved
     * @param facetToRemove     facet to be removed
     * @return current instance
     */
    public AbstractDeclarationTypeFactory removeFacet(final String facetToRemove) {
        if (this.facetsToRemove == null) {
            this.facetsToRemove = new HashSet<String>();
        }
        this.facetsToRemove.add(facetToRemove);
        return this;
    }

    /**
     * Marks multiple facets as resolved
     * @param facetsToRemove    facets to be removed
     * @return current instance
     */
    public AbstractDeclarationTypeFactory removeFacets(final Set<String> facetsToRemove) {
        if (this.facetsToRemove == null) {
            this.facetsToRemove = new HashSet<String>();
        }
        this.facetsToRemove.addAll(facetsToRemove);
        return this;
    }

    /**
     * Parse given XSD facet nodes into internal state
     * @param facets    XSD facet nodes to be transformed
     */
    private void parseFacets(final List<XmlSchemaFacet> facets) {
        reset();

        if (facets != null && !facets.isEmpty()) {
            for (XmlSchemaFacet facet : facets) {
                if (facet instanceof XmlSchemaFractionDigitsFacet) {
                    facetSingleValues.put(FACET_FRACTIONS_DIGITS, (facet).getValue());
                    SchemaLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add fraction digits. Value=" + facet.getValue());
                } else if (facet instanceof XmlSchemaLengthFacet) {
                    facetSingleValues.put(FACET_LENGTH, facet.getValue());
                    SchemaLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add length. Value=" + facet.getValue());
                } else if (facet instanceof XmlSchemaMaxExclusiveFacet) {
                    facetSingleValues.put(FACET_MAX_EXCLUSIVE, (facet).getValue());
                    SchemaLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add max exclusive. Value=" + facet.getValue());
                } else if (facet instanceof XmlSchemaMaxInclusiveFacet) {
                    facetSingleValues.put(FACET_MAX_INCLUSIVE, (facet).getValue());
                    SchemaLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add max inclusive. Value=" + facet.getValue());
                } else if (facet instanceof XmlSchemaMaxLengthFacet) {
                    facetSingleValues.put(FACET_MAX_LENGTH, (facet).getValue());
                    SchemaLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add max length. Value=" + facet.getValue());
                } else if (facet instanceof XmlSchemaMinLengthFacet) {
                    facetSingleValues.put(FACET_MIN_LENGTH, (facet).getValue());
                    SchemaLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add min length. Value=" + facet.getValue());
                } else if (facet instanceof XmlSchemaMinExclusiveFacet) {
                    facetSingleValues.put(FACET_MIN_EXCLUSIVE, (facet).getValue());
                    SchemaLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add min exclusive. Value=" + facet.getValue());
                } else if (facet instanceof XmlSchemaMinInclusiveFacet) {
                    facetSingleValues.put(FACET_MIN_INCLUSIVE, (facet).getValue());
                    SchemaLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add min inclusive. Value=" + facet.getValue());
                } else if (facet instanceof XmlSchemaPatternFacet) {
                    final List<Object> patterns = getOrCreateValueList(FACET_PATTERN);
                    patterns.add((facet).getValue());
                    SchemaLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add pattern. Value=" + facet.getValue());
                } else if (facet instanceof XmlSchemaTotalDigitsFacet) {
                    facetSingleValues.put(FACET_TOTAL_DIGITS, (facet).getValue());
                    SchemaLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add total digits. Value=" + facet.getValue());
                } else if (facet instanceof XmlSchemaWhiteSpaceFacet) {
                    facetSingleValues.put(FACET_WHITESPACE, (facet).getValue());
                    SchemaLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add whitespace. Value=" + facet.getValue());
                } else if (facet instanceof XmlSchemaEnumerationFacet) {
                    final List<Object> enumeration = getOrCreateValueList(FACET_ENUMERATION);
                    enumeration.add((facet).getValue());
                    SchemaLogger.print(LOG_DEBUG, TRANSFORMATION, typeName, "Declaration - Add enumeration. Value=" + facet.getValue());
                } else {
                    SchemaLogger.print(LOG_WARN, TRANSFORMATION, typeName, "Declaration - Unsupported XSD facet! Clazz=" + facet.getClass().getSimpleName());
                }
            }
        }
    }

    /**
     * Check if facet of given name is stored internally
     * @param facetName     facet to be found
     * @return true, if facet is stored internally
     */
    protected boolean hasFacet(final String facetName) {
        return facetSingleValues.containsKey(facetName);
    }

    /**
     * Remove facet of given name from internal storage and return value of facet
     * @param facetName     facet to be removed
     * @return value of removed facet, null if facet is not stored
     */
    protected Object useFacet(final String facetName) {
        return facetSingleValues.remove(facetName);
    }

    /**
     * Get value of internally stored facet
     * @param facetName     facet's value to be returned
     * @return value of facet, null if facet is not stored
     */
    protected Object getFacet(final String facetName) {
        return facetSingleValues.get(facetName);
    }

    /**
     * Check if facet using multiple values of given name is stored internally
     * @param facetName     facet to be found
     * @return true, if facet is stored internally
     */
    protected boolean hasMultipleFacet(final String facetName) {
        return facetMultipleValues.containsKey(facetName);
    }

    /**
     * Remove facet using multiple values of given name from internal storage and return value of facet
     * @param facetName     facet to be removed
     * @return value of removed facet, null if facet is not stored
     */
    protected List<Object> useMultipleFacet(final String facetName) {
        return facetMultipleValues.remove(facetName);
    }

    /**
     * Custom implementation of converting XSD facet into x-definition declaration.
     * Called before {@link #defaultBuildFacets}
     * @param sb string builder where should be custom string appended
     */
    protected void buildFacets(final StringBuilder sb) {
    }

    /**
     * X-definition declaration builder helper for concatenating facets values
     * @param sb        facet string builder
     * @param value     facet value to be appended
     */
    protected void facetBuilder(final StringBuilder sb, final Object value) {
        if (!builderFirstFacet) {
            sb.append(", " + value);
        } else {
            sb.append(value);
            builderFirstFacet = false;
        }
    }

    /**
     * Default implementation of transforming XSD facets to x-definition declaration
     * @param sb
     */
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
        if (hasMultipleFacet(FACET_PATTERN)) {
            final List<Object> patterns = useMultipleFacet(FACET_PATTERN);
            if (patterns != null && !patterns.isEmpty()) {
                facetBuilder(sb, "%pattern=['");

                if (patterns.size() == 1) {
                    sb.append(patterns.get(0).toString().replace("\\", "\\\\"));
                } else {
                    Iterator<Object> patternItr = patterns.iterator();
                    sb.append('(');
                    sb.append(patternItr.next().toString().replace("\\", "\\\\"));
                    sb.append(')');
                    while (patternItr.hasNext()) {
                        sb.append("|(");
                        sb.append(patternItr.next().toString().replace("\\", "\\\\"));
                        sb.append(')');
                    }
                }

                sb.append("']");
            }
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

    /**
     * Get or create new list for facet using multiple values
     * @param facet     facet name using multiple values
     * @return list of facet's values
     */
    protected List<Object> getOrCreateValueList(final String facet) {
        List<Object> list = facetMultipleValues.get(facet);
        if (list == null) {
            list = new LinkedList<Object>();
            facetMultipleValues.put(facet, list);
        }

        return list;
    }

    /**
     * Resets internal state of factory
     */
    private void reset() {
        facetSingleValues.clear();
        facetMultipleValues.clear();
        builderFirstFacet = true;
    }
}
