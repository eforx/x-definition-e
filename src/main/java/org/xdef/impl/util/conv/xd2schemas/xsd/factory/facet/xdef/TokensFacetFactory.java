package org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.xdef;

import org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.DefaultFacetFactory;

// TODO
public class TokensFacetFactory extends DefaultFacetFactory {

    static public final String XD_PARSER_NAME = "tokens";
    static public final String XD_PARSER_CI_NAME = "tokensi";

    private final boolean isCaseSensitive;

    public TokensFacetFactory(boolean isCaseSensitive) {
        this.isCaseSensitive = isCaseSensitive;
    }
}
