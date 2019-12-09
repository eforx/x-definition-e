package org.xdef.impl.util.conv.schema.schema2xd.xsd.factory.declaration;

/**
 * Declaration for transforming any type of value by default implementation
 */
public class DefaultTypeFactory extends AbstractDeclarationTypeFactory {

    public final String xdType;

    public DefaultTypeFactory(String xdType) {
        this.xdType = xdType;
    }

    @Override
    public String getDataType() {
        return xdType;
    }

}
