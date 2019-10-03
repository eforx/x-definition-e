package org.xdef.impl.util.conv.xd2schemas;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;

public class XsdBuilder {

    public static void addElement(final XmlSchema schema, final XmlSchemaElement element) {
        schema.getItems().add(element);
    }

    public static XmlSchemaElement createElement(final XmlSchema schema, final String name) {
        XmlSchemaElement elem = new XmlSchemaElement(schema, false);
        elem.setName(name);
        /*hostElement.setMinOccurs(1);
        hostElement.setMaxOccurs(1);*/

        return elem;
    }

    public static XmlSchemaComplexType createComplexType(final XmlSchema schema) {
        return new XmlSchemaComplexType(schema, false);
    }
}
