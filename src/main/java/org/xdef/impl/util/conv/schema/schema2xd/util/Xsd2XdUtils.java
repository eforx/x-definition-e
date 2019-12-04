package org.xdef.impl.util.conv.schema.schema2xd.util;

import org.apache.ws.commons.schema.*;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.xdef.impl.util.conv.schema.util.SchemaLogger;
import org.xdef.impl.util.conv.schema.schema2xd.definition.Xsd2XdFeature;
import org.xdef.impl.util.conv.schema.schema2xd.model.XdAdapterCtx;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.xdef.impl.util.conv.schema.util.SchemaLoggerDefs.LOG_DEBUG;
import static org.xdef.impl.util.conv.schema.util.SchemaLoggerDefs.LOG_WARN;
import static org.xdef.impl.util.conv.schema.schema2xd.definition.Xsd2XdDefinitions.XD_ATTR_SCRIPT;
import static org.xdef.impl.util.conv.schema.schema2xd.definition.Xsd2XdDefinitions.XD_NAMESPACE_URI;
import static org.xdef.impl.util.conv.schema.schema2xd.definition.Xsd2XdFeature.XD_TEXT_REQUIRED;
import static org.xdef.impl.util.conv.schema.xd2schema.definition.AlgPhase.TRANSFORMATION;

public class Xsd2XdUtils {

    public static XmlSchemaType getSchemaTypeByQName(final XmlSchema schema, final QName qName) {
        final Map<QName, XmlSchemaType> schemaTypeMap = schema.getSchemaTypes();
        if (schemaTypeMap != null) {
            return schemaTypeMap.get(qName);
        }

        return null;
    }

    public static XmlSchemaGroup getGroupByQName(final XmlSchema schema, final QName qName) {
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
