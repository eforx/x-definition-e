package org.xdef.impl.util.conv.schema2xd.xsd.factory.declaration;

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
