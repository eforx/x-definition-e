package org.xdef.impl.util.conv.schema2xd.xsd.util;

import org.apache.ws.commons.schema.constants.Constants;
import org.xdef.impl.util.conv.schema2xd.xsd.factory.declaration.*;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

import static org.xdef.impl.util.conv.schema2xd.xsd.factory.declaration.IDeclarationTypeFactory.FACET_PATTERN;

public class Xsd2XdTypeMapping {

    /**
     * Transformation map of XSD data types to x-definition data type
     */
    private static final Map<QName, IDeclarationTypeFactory> defaultQNameMap = new HashMap<QName, IDeclarationTypeFactory>();

    static {

        defaultQNameMap.put(Constants.XSD_ANYURI, new TextTypeFactory("anyURI"));
        defaultQNameMap.put(Constants.XSD_BASE64, new TextTypeFactory("base64Binary"));
        defaultQNameMap.put(Constants.XSD_BOOLEAN, new DefaultTypeFactory("boolean"));
        defaultQNameMap.put(Constants.XSD_ENTITIES, new DefaultTypeFactory("ENTITIES"));
        defaultQNameMap.put(Constants.XSD_ENTITY, new DefaultTypeFactory("ENTITY"));
        defaultQNameMap.put(Constants.XSD_HEXBIN, new TextTypeFactory("hexBinary"));
        defaultQNameMap.put(Constants.XSD_ID, new DefaultTypeFactory("ID"));
        defaultQNameMap.put(Constants.XSD_IDREF, new DefaultTypeFactory("IDREF"));
        defaultQNameMap.put(Constants.XSD_DAY, new DefaultTypeFactory("gDay"));
        defaultQNameMap.put(Constants.XSD_MONTH, new DefaultTypeFactory("gMonth"));
        defaultQNameMap.put(Constants.XSD_MONTHDAY, new DefaultTypeFactory("gMonthDay"));
        defaultQNameMap.put(Constants.XSD_NCNAME, new DefaultTypeFactory("NCName").removeFacet(FACET_PATTERN));
        defaultQNameMap.put(Constants.XSD_NMTOKEN, new DefaultTypeFactory("NMTOKEN").removeFacet(FACET_PATTERN));
        defaultQNameMap.put(Constants.XSD_YEAR, new DefaultTypeFactory("gYear"));
        defaultQNameMap.put(Constants.XSD_YEARMONTH, new DefaultTypeFactory("gYearMonth"));
        defaultQNameMap.put(Constants.XSD_NORMALIZEDSTRING, new DefaultTypeFactory("normalizedString"));
        defaultQNameMap.put(Constants.XSD_QNAME, new DefaultTypeFactory("QName"));

        defaultQNameMap.put(Constants.XSD_DATE, new DefaultTypeFactory("ISOdate"));
        defaultQNameMap.put(Constants.XSD_DATETIME, new DefaultTypeFactory("ISOdateTime"));
        defaultQNameMap.put(Constants.XSD_TIME, new DefaultTypeFactory("ISOtime"));
        defaultQNameMap.put(Constants.XSD_LANGUAGE, new DefaultTypeFactory("ISOlanguage").removeFacet(FACET_PATTERN));
        defaultQNameMap.put(Constants.XSD_DURATION, new DefaultTypeFactory("ISOduration"));

        defaultQNameMap.put(Constants.XSD_STRING, new TextTypeFactory("string"));
        defaultQNameMap.put(Constants.XSD_FLOAT, new DecimalTypeFactory("float"));
        defaultQNameMap.put(Constants.XSD_DOUBLE, new DecimalTypeFactory("double"));
        defaultQNameMap.put(Constants.XSD_DECIMAL, new DecimalTypeFactory("dec"));
        defaultQNameMap.put(Constants.XSD_SHORT, new IntegerTypeFactory("short"));
        defaultQNameMap.put(Constants.XSD_INT, new IntegerTypeFactory("int"));
        defaultQNameMap.put(Constants.XSD_INTEGER, new IntegerTypeFactory("int"));
        defaultQNameMap.put(Constants.XSD_LONG, new IntegerTypeFactory("long"));


    }

    public static IDeclarationTypeFactory getDefaultDataTypeFactory(final QName xsdType) {
        return defaultQNameMap.get(xsdType);
    }
}
