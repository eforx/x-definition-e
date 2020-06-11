package org.xdef.impl.util.conv.schema.schema2xd.xsd.factory.declaration;

/**
 * Declaration for transforming union value
 */
public class UnionTypeFactory extends AbstractDeclarationTypeFactory {

    public static final String XD_TYPE = "union";

    @Override
    public String getDataType() {
        return XD_TYPE;
    }

}
