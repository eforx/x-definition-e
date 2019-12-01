package org.xdef.impl.util.conv.schema2xd.xsd.factory.declaration;

import org.apache.ws.commons.schema.XmlSchemaFacet;

import java.util.List;

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

    enum Mode {
        NAMED_DECL,         // Used for top level xd:declaration nodes
        TEXT_DECL,          // Used for definition of text value of element
        DATATYPE_DECL       // Used for building only data type from facets (ie. attribute type, list item type)
    }

    void setMode(final Mode mode);
    void setName(final String typeName);

    String getDataType();

    String build(final List<XmlSchemaFacet> facets);
    String build(final String facets);
    String build(final String type, final String facets);
}
