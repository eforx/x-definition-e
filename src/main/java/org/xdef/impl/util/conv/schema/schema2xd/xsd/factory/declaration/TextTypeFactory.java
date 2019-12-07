package org.xdef.impl.util.conv.schema.schema2xd.xsd.factory.declaration;

public class TextTypeFactory extends AbstractDeclarationTypeFactory {

    public final String xdType;

    public TextTypeFactory(String xdType) {
        this.xdType = xdType;
    }

    @Override
    public String getDataType() {
        return xdType;
    }

    @Override
    protected void buildFacets(final StringBuilder sb) {
        if (hasFacet(FACET_LENGTH)) {
            facetBuilder(sb, useFacet(FACET_LENGTH));
        } else if (hasFacet(FACET_MIN_LENGTH) && hasFacet(FACET_MAX_LENGTH)) {
            facetBuilder(sb, useFacet(FACET_MIN_LENGTH) + ", " + useFacet(FACET_MAX_LENGTH));
        }
    }

}
