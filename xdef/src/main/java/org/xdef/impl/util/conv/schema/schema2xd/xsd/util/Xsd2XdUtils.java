package org.xdef.impl.util.conv.schema.schema2xd.xsd.util;

import org.apache.ws.commons.schema.*;
import org.xdef.impl.util.conv.schema.schema2xd.xsd.definition.Xsd2XdFeature;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.xdef.impl.util.conv.schema.schema2xd.xsd.definition.Xsd2XdFeature.XD_TEXT_REQUIRED;

/**
 * Basic utils used in transformation XSD -> x-definition
 */
public class Xsd2XdUtils {

    /**
     * Finds XSD complex/simple schema type node in given schema by qualified name
     * @param schema    XSD document
     * @param qName     XSD qualified name to be searched
     * @return XSD complex/simple schema type node if exists in given schema, otherwise null
     */
    public static XmlSchemaType findSchemaTypeByQName(final XmlSchema schema, final QName qName) {
        final Map<QName, XmlSchemaType> schemaTypeMap = schema.getSchemaTypes();
        if (schemaTypeMap != null) {
            return schemaTypeMap.get(qName);
        }

        return null;
    }

    /**
     * Finds XSD group node in given schema by qualified name
     * @param schema    XSD document
     * @param qName     XSD qualified name to be searched
     * @return XSD group node if exists in given schema, otherwise null
     */
    public static XmlSchemaGroup findGroupByQName(final XmlSchema schema, final QName qName) {
        final Map<QName, XmlSchemaGroup> schemaTypeMap = schema.getGroups();
        if (schemaTypeMap != null) {
            return schemaTypeMap.get(qName);
        }

        return null;
    }

    /**
     * Features which should be enabled by default for transformation algorithm
     * @return default algorithm features
     */
    public static Set<Xsd2XdFeature> defaultFeatures() {
        Set<Xsd2XdFeature> features = new HashSet<Xsd2XdFeature>();
        features.add(XD_TEXT_REQUIRED);
        return features;
    }
}
