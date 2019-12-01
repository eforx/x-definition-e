package org.xdef.impl.util.conv.schema2xd.xsd.factory.declaration;

public class EmptyTypeFactory extends AbstractDeclarationTypeFactory {

    public final String xdType;

    public EmptyTypeFactory(String xdType) {
        this.xdType = xdType;
    }

    @Override
    public String getDataType() {
        return xdType;
    }

}
