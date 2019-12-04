package org.xdef.impl.util.conv.schema.schema2xd.factory.declaration;

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
