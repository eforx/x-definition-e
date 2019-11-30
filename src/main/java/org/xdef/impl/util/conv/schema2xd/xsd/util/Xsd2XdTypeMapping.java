package org.xdef.impl.util.conv.schema2xd.xsd.util;

import org.apache.ws.commons.schema.constants.Constants;
import org.xdef.impl.util.conv.schema2xd.xsd.factory.declaration.*;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

public class Xsd2XdTypeMapping {

    /**
     * Transformation map of XSD data types to x-definition data type
     */
    private static final Map<QName, IDeclarationTypeFactory> defaultQNameMap = new HashMap<QName, IDeclarationTypeFactory>();

    static {
        defaultQNameMap.put(Constants.XSD_STRING, new StringTypeFactory());
        defaultQNameMap.put(Constants.XSD_FLOAT, new DecimalTypeFactory("float"));
        defaultQNameMap.put(Constants.XSD_DOUBLE, new DecimalTypeFactory("double"));
        defaultQNameMap.put(Constants.XSD_SHORT, new DecimalTypeFactory("short"));
        defaultQNameMap.put(Constants.XSD_INT, new DecimalTypeFactory("int"));
        defaultQNameMap.put(Constants.XSD_INTEGER, new DecimalTypeFactory("int"));
        defaultQNameMap.put(Constants.XSD_LONG, new DecimalTypeFactory("long"));
    }

    public static IDeclarationTypeFactory getDefaultDataTypeFactory(final QName xsdType) {
        return defaultQNameMap.get(xsdType);
    }
}
