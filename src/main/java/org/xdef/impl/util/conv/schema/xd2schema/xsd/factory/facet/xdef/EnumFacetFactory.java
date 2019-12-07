package org.xdef.impl.util.conv.schema.xd2schema.xsd.factory.facet.xdef;

import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.xdef.XDNamedValue;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.factory.facet.DefaultFacetFactory;

import java.util.List;

import static org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.Xd2XsdDefinitions.XD_FACET_ARGUMENT;

public class EnumFacetFactory extends DefaultFacetFactory {

    static public final String XD_PARSER_NAME = "enum";

    @Override
    public boolean customFacet(List<XmlSchemaFacet> facets, XDNamedValue param) {
        if (XD_FACET_ARGUMENT.equals(param.getName())) {
            facets.addAll(enumeration(param));
            return true;
        }

        return false;
    }

}
