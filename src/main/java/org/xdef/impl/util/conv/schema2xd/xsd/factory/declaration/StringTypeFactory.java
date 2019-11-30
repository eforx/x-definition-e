package org.xdef.impl.util.conv.schema2xd.xsd.factory.declaration;

public class StringTypeFactory extends AbstractDeclarationTypeFactory {

    public static final String XD_TYPE = "string";

    @Override
    public String getDataType() {
        return XD_TYPE;
    }

    @Override
    protected void buildFacets(final StringBuilder sb) {
        if (hasFacet(LENGTH)) {
            facetBuilder(sb, useFacet(LENGTH));
        } else if (hasFacet(MIN_LENGTH) && hasFacet(MAX_LENGTH)) {
            facetBuilder(sb, useFacet(MIN_LENGTH) + ", " + useFacet(MAX_LENGTH));
        }
    }

}
