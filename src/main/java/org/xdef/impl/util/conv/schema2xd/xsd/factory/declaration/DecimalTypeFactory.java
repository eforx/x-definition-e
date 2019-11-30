package org.xdef.impl.util.conv.schema2xd.xsd.factory.declaration;

public class DecimalTypeFactory extends AbstractDeclarationTypeFactory {

    public final String xdType;

    public DecimalTypeFactory(String xdType) {
        this.xdType = xdType;
    }

    @Override
    public String getDataType() {
        return xdType;
    }

    @Override
    protected void buildFacets(final StringBuilder sb) {
        if (hasFacet(MIN_INCLUSIVE) && hasFacet(MAX_INCLUSIVE)) {
            facetBuilder(sb, useFacet(MIN_INCLUSIVE) + ", " + useFacet(MAX_INCLUSIVE));
        }
    }


}
