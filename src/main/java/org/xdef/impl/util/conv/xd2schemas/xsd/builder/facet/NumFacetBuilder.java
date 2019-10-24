package org.xdef.impl.util.conv.xd2schemas.xsd.builder.facet;

import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.xdef.XDNamedValue;

import java.util.List;

public class NumFacetBuilder extends DefaultFacetBuilder {

    @Override
    public void extraFacets(final List<XmlSchemaFacet> facets, final XDNamedValue[] params) {
        facets.add(pattern("([0-9])*"));
    }
}
