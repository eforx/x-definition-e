package org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.xdef;

import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.xdef.XDNamedValue;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.AbstractXsdFacetFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XD2XsdUtils;

import java.util.List;

import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XD2XsdDefinitions.XD_FACET_ARGUMENT;

public class StartsFacetFactory extends AbstractXsdFacetFactory {

    static public final String XD_PARSER_NAME = "starts";
    static public final String XD_PARSER_CI_NAME = "startsi";

    private final boolean isCaseSensitive;

    public StartsFacetFactory(boolean isCaseSensitive) {
        this.isCaseSensitive = isCaseSensitive;
    }

    @Override
    public boolean customFacet(List<XmlSchemaFacet> facets, XDNamedValue param) {
        if (XD_FACET_ARGUMENT.equals(param.getName())) {
            final String pattern = isCaseSensitive ? param.getValue().stringValue() : XD2XsdUtils.caseSensitiveValue2CIPattern(param.getValue().stringValue());
            facets.add(pattern(pattern + ".*"));
            return true;
        }

        return false;
    }

}
