package org.xdef.impl.util.conv.schema2xd.xsd.factory.declaration;

import org.apache.ws.commons.schema.XmlSchemaFacet;

import java.util.List;

public interface IDeclarationTypeFactory {

    enum Mode {
        NAMED_DECL,         // Used for top level xd:declaration nodes
        TEXT_DECL,          // Used for definition of text value elements
        DATATYPE_DECL  // Used for building only data type from facets
    }

    void setMode(final Mode mode);
    void setName(final String typeName);

    String getDataType();

    String build(final List<XmlSchemaFacet> facets);
    String build(final String facets);
    String build(final String type, final String facets);
}
