package org.xdef.impl.util.conv.xd2schema.xsd.factory.facet.xdef;

import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.apache.ws.commons.schema.XmlSchemaLengthFacet;
import org.xdef.XDNamedValue;
import org.xdef.impl.util.conv.xd2schema.xsd.factory.facet.DefaultFacetFactory;

import java.util.List;

public class MD5FacetFactory extends DefaultFacetFactory {

    static public final String XD_PARSER_NAME = "MD5";

    @Override
    public void extraFacets(final List<XmlSchemaFacet> facets) {
        facets.add(pattern("[a-fA-F0-9]{32}"));
    }

    @Override
    public XmlSchemaLengthFacet length(XDNamedValue param) {
        return null;
    }
}
