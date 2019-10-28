package org.xdef.impl.util.conv.xd2schemas.xsd.util;

import org.apache.ws.commons.schema.*;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.XsdElementFactory;

import javax.xml.namespace.QName;
import java.util.List;

import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.*;
import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.POSTPROCESSING;

public class XsdPostProcessor {

    /**
     * Transform root element containing reference to local type + complexType
     */
    public static void elementTopLevelRef(final XmlSchemaElement xsdElem, final XElement xDefEl, final XmlSchema schema, final XsdElementFactory xsdFactory) {
        final QName qName = xsdElem.getRef().getTargetQName();
        xsdElem.getRef().setTargetQName(null);
        xsdElem.setName(xDefEl.getName());
        //xsdElem.setSchemaTypeName(new QName("", "test"));

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

    public static void elementComplexContent(final XmlSchema schema, final XElement defEl, final XmlSchemaComplexType complexType, int logLevel) {
        if (XsdLogger.isDebug(logLevel)) {
            XsdLogger.printP(DEBUG, POSTPROCESSING, defEl, "Updating complex content of element");
        }

        // if xs:all contains only unbounded elements, then we can use unbounded xs:choise
        {
            boolean allElementsUnbounded = true;
            boolean anyElementUnbounded = false;

            if (complexType.getParticle() instanceof XmlSchemaAll) {

                for (XNode xNode : defEl._childNodes) {
                    if (xNode.getKind() == XNode.XMELEMENT) {
                        if (!xNode.isMaxUnlimited() && !xNode.isUnbounded()) {
                            allElementsUnbounded = false;
                        } else if (xNode.maxOccurs() > 1) {
                            anyElementUnbounded = true;
                        }
                    }
                }

                if (allElementsUnbounded) {
                    if (XsdLogger.isDebug(logLevel)) {
                        XsdLogger.printP(DEBUG, POSTPROCESSING, defEl, "Complex content contains xs:all with only unbounded elements. Update to unbounded xs:choise.");
                    }

                    XmlSchemaChoice group = new XmlSchemaChoice();
                    group.setMaxOccurs(Long.MAX_VALUE);

                    // Copy elements
                    for (XmlSchemaAllMember member : ((XmlSchemaAll)complexType.getParticle()).getItems()) {
                        group.getItems().add((XmlSchemaChoiceMember) member);
                    }

                    complexType.setParticle(group);
                } else if (anyElementUnbounded) {
                    // TODO: XD->XSD Solve?
                    if (XsdLogger.isError(logLevel)) {
                        XsdLogger.printP(ERROR, POSTPROCESSING, defEl, "xs:all contains element which has maxOccurs higher than 1");
                    }

                }
            }
        }

        // element contains simple content and particle -> XSD does not support restrictions for text if element contains elements
        // We have to use mixed attribute for root element and remove simple content
        {
            if (complexType.getParticle() != null && complexType.getContentModel() != null && complexType.getContentModel() instanceof XmlSchemaSimpleContent) {
                if (XsdLogger.isWarn(logLevel)) {
                    XsdLogger.printP(WARN, POSTPROCESSING, defEl, "!Lossy transformation! Remove simple content from element due to existence of complex content. Use mixed attr.");
                }

                // Copy attributes from simple content
                XmlSchemaContent content = complexType.getContentModel().getContent();
                if (content instanceof XmlSchemaSimpleContentExtension) {
                    List attrs = ((XmlSchemaSimpleContentExtension) content).getAttributes();
                    if (attrs != null && !attrs.isEmpty()) {
                        complexType.getAttributes().addAll(attrs);
                    }
                }

                // TODO: remove by reference handler
                //schema.getItems().remove(complexType.getContentModel());
                complexType.setContentModel(null);
                complexType.setMixed(true);
                complexType.setAnnotation(XsdElementFactory.createAnnotation("Text content has been originally restricted by x-definition"));
            }
        }
    }
}
