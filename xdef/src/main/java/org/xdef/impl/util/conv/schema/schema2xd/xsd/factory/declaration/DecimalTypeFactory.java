package org.xdef.impl.util.conv.schema.schema2xd.xsd.factory.declaration;

/**
 * Declaration for transforming floating point values
 */
public class DecimalTypeFactory extends AbstractDeclarationTypeFactory {

    public final String xdType;

    public DecimalTypeFactory(String xdType) {
        this.xdType = xdType;
    }

    @Override
    public String getDataType() {
        return xdType;
    }

}
