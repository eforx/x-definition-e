package org.xdef.impl.util.conv.schema.schema2xd.xsd.factory.declaration;

public class IntegerTypeFactory extends AbstractDeclarationTypeFactory {

    public final String xdType;

    public IntegerTypeFactory(String xdType) {
        this.xdType = xdType;
    }

    @Override
    public String getDataType() {
        return xdType;
    }

    @Override
    protected void buildFacets(final StringBuilder sb) {
        if (hasFacet(FACET_MIN_INCLUSIVE) && hasFacet(FACET_MAX_INCLUSIVE)) {
            if ("short".equals(xdType)) {
                if ((!getFacet(FACET_MIN_INCLUSIVE).equals(Short.MIN_VALUE) || !getFacet(FACET_MAX_INCLUSIVE).equals(Short.MAX_VALUE))) {
                    buildMinMaxFacet(sb);
                } else {
                    removeMinMaxFacet();
                }
            } else if ("int".equals(xdType)) {
                if ((!getFacet(FACET_MIN_INCLUSIVE).equals(Integer.MIN_VALUE) || !getFacet(FACET_MAX_INCLUSIVE).equals(Integer.MAX_VALUE))) {
                    buildMinMaxFacet(sb);
                } else {
                    removeMinMaxFacet();
                }
            } else if ("long".equals(xdType)) {
                if ((!getFacet(FACET_MIN_INCLUSIVE).equals(Long.MIN_VALUE) || !getFacet(FACET_MAX_INCLUSIVE).equals(Long.MAX_VALUE))) {
                    buildMinMaxFacet(sb);
                } else {
                    removeMinMaxFacet();
                }
            } else {
                buildMinMaxFacet(sb);
            }
        }
    }

    private void buildMinMaxFacet(final StringBuilder sb) {
        facetBuilder(sb, useFacet(FACET_MIN_INCLUSIVE) + ", " + useFacet(FACET_MAX_INCLUSIVE));
    }

    private void removeMinMaxFacet() {
        useFacet(FACET_MIN_INCLUSIVE);
        useFacet(FACET_MAX_INCLUSIVE);
    }
}
