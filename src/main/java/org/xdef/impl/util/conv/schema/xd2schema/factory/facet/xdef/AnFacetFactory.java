package org.xdef.impl.util.conv.schema.xd2schema.factory.facet.xdef;

import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.xdef.impl.util.conv.schema.xd2schema.factory.facet.DefaultFacetFactory;

import java.util.List;

public class AnFacetFactory extends DefaultFacetFactory {

    static public final String XD_PARSER_NAME = "an";

    @Override
    public void extraFacets(final List<XmlSchemaFacet> facets) {
        facets.add(pattern("[a-zA-Z0-9]*"));
    }

}
