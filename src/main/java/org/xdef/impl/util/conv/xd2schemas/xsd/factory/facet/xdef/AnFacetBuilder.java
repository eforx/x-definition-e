package org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.xdef;

import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.xdef.XDNamedValue;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.DefaultFacetBuilder;

import java.util.List;

public class AnFacetBuilder extends DefaultFacetBuilder {

    @Override
    public void extraFacets(final List<XmlSchemaFacet> facets, final XDNamedValue[] params) {
        facets.add(pattern("[a-zA-Z0-9]*"));
    }

}
