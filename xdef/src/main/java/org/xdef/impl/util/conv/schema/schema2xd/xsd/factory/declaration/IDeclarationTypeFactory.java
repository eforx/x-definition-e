package org.xdef.impl.util.conv.schema.schema2xd.xsd.factory.declaration;

import org.apache.ws.commons.schema.XmlSchemaFacet;

import java.util.List;

/**
 * Transform XSD restrictions/facets into x-definition declaration type
 */
public interface IDeclarationTypeFactory {

    String FACET_MIN_INCLUSIVE = "MIN_INCLUSIVE";
    String FACET_MIN_EXCLUSIVE = "MIN_EXCLUSIVE";
    String FACET_MAX_INCLUSIVE = "MAX_INCLUSIVE";
    String FACET_MAX_EXCLUSIVE = "MAX_EXCLUSIVE";
    String FACET_PATTERN = "PATTERN";
    String FACET_LENGTH = "LENGTH";
    String FACET_MIN_LENGTH = "MIN_LENGTH";
    String FACET_MAX_LENGTH = "MAX_LENGTH";
    String FACET_TOTAL_DIGITS = "TOTAL_DIGITS";
    String FACET_FRACTIONS_DIGITS = "FRACTIONS_DIGITS";
    String FACET_WHITESPACE = "WHITESPACE";
    String FACET_ENUMERATION = "ENUMERATION";

    enum Type {
        TOP_DECL,           // Used for top level xd:declaration nodes
        TEXT_DECL,          // Used for definition of text value of element
        DATATYPE_DECL       // Used for building only data type from facets (ie. attribute type, list item type)
    }

    /**
     * Set type of x-definition declaration
     * @param type      type of x-definition declaration
     */
    void setType(final Type type);

    /**
     * Set declaration variable name.
     * Use only with mode {@link Type.TOP_DECL}
     * @param typeName      x-definition declaration variable name
     */
    void setName(final String typeName);

    /**
     * Get variable data type
     * @return x-definition variable data type
     */
    String getDataType();

    /**
     * Creates x-definition declaration type restrictions based on given XSD facets
     * @param facets    list of XSD facets
     * @return x-definition restriction
     */
    String build(final List<XmlSchemaFacet> facets);

    /**
     * Creates x-definition declaration type restrictions from given facets string
     * @param facets    list of XSD facets
     * @return x-definition restriction
     */
    String build(final String facets);

}
