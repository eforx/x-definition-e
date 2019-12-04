package org.xdef.impl.util.conv.schema.xd2schema.factory.facet.xdef;

import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.xdef.XDNamedValue;
import org.xdef.impl.util.conv.schema.xd2schema.factory.facet.AbstractXsdFacetFactory;
import org.xdef.impl.util.conv.schema.xd2schema.util.Xd2XsdUtils;

import java.util.List;

import static org.xdef.impl.util.conv.schema.xd2schema.definition.Xd2XsdDefinitions.XD_FACET_ARGUMENT;

public class ContainsFacetFactory extends AbstractXsdFacetFactory {

    static public final String XD_PARSER_NAME = "contains";
    static public final String XD_PARSER_CI_NAME = "containsi";

    private final boolean isCaseSensitive;

    public ContainsFacetFactory(boolean isCaseSensitive) {
        this.isCaseSensitive = isCaseSensitive;
    }

    @Override
    public boolean customFacet(List<XmlSchemaFacet> facets, XDNamedValue param) {
        if (XD_FACET_ARGUMENT.equals(param.getName())) {
            final String pattern = isCaseSensitive ? param.getValue().stringValue() : Xd2XsdUtils.regex2CaseInsensitive(param.getValue().stringValue());
            facets.add(pattern(".*" + pattern + ".*"));
            return true;
        }

        return false;
    }

}
