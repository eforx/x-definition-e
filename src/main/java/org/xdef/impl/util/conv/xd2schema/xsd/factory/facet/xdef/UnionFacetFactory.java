package org.xdef.impl.util.conv.xd2schema.xsd.factory.facet.xdef;

import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.xdef.XDNamedValue;
import org.xdef.XDParser;
import org.xdef.impl.util.conv.xd2schema.xsd.factory.facet.DefaultFacetFactory;

import java.util.List;

public class UnionFacetFactory extends DefaultFacetFactory {
    static public final String XD_PARSER_NAME = "union";

    @Override
    public boolean customFacet(List<XmlSchemaFacet> facets, XDNamedValue param) {
        if (param.getValue() instanceof XDParser) {
            return true;
        }

        return false;
    }

}
