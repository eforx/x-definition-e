package org.xdef.impl.util.conv.xd2schemas.xsd.util;

import org.apache.ws.commons.schema.*;
import org.xdef.impl.XElement;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.XsdElementFactory;

import javax.xml.namespace.QName;

public class XsdPostProcessor {

    /**
     * Transform root element containing reference to local type + complexType
     */
    public static void elemRootRef(final XmlSchemaElement xsdElem, final XElement xDefEl, final XmlSchema schema, final XsdElementFactory xsdFactory) {
        final QName qName = xsdElem.getRef().getTargetQName();
        xsdElem.getRef().setTargetQName(null);

        // Creating complex content with extension to original reference
        XmlSchemaComplexType complexType = xsdFactory.createEmptyComplexType();
        complexType.setName(xDefEl.getName());

        // Create complex content
        XmlSchemaComplexContent complexContent = new XmlSchemaComplexContent();
        XmlSchemaComplexContentExtension contentExtension = new XmlSchemaComplexContentExtension();
        contentExtension.setBaseTypeName(qName);
        complexContent.setContent(contentExtension);
        complexType.setContentModel(complexContent);

        XD2XsdUtils.addSchemaType(schema, complexType);
    }
}
