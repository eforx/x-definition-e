package org.xdef.impl.util.conv.schema2xd.xsd.factory.declaration;

import org.apache.ws.commons.schema.XmlSchemaFacet;

import java.util.List;

public interface IDeclarationTypeFactory {

    void setName(final String typeName);
    String getDataType();

    String build(final List<XmlSchemaFacet> facets);
}
