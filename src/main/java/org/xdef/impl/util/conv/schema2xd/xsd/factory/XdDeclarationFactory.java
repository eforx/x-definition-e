package org.xdef.impl.util.conv.schema2xd.xsd.factory;

import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.w3c.dom.Element;
import org.xdef.impl.util.conv.schema.util.XsdLogger;
import org.xdef.impl.util.conv.schema2xd.xsd.factory.declaration.IDeclarationTypeFactory;
import org.xdef.impl.util.conv.schema2xd.xsd.model.XdAdapterCtx;
import org.xdef.impl.util.conv.schema2xd.xsd.util.Xsd2XdTypeMapping;

import javax.xml.namespace.QName;

import static org.xdef.impl.util.conv.schema.util.XsdLoggerDefs.LOG_ERROR;
import static org.xdef.impl.util.conv.schema.util.XsdLoggerDefs.LOG_INFO;
import static org.xdef.impl.util.conv.xd2schema.xsd.definition.AlgPhase.TRANSFORMATION;

public class XdDeclarationFactory {

    /**
     * X-definition XML element factory
     */
    final private XdElementFactory xdFactory;

    final private XdAdapterCtx adapterCtx;

    public XdDeclarationFactory(XdElementFactory xdFactory, XdAdapterCtx adapterCtx) {
        this.xdFactory = xdFactory;
        this.adapterCtx = adapterCtx;
    }

    public Element create(final XmlSchemaSimpleType simpleType) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, simpleType, "Creating declaration ...");
        final Element xdDeclaration = xdFactory.createEmptyDeclaration();
        final String name = simpleType.getName();
        if (simpleType.getContent() instanceof XmlSchemaSimpleTypeRestriction) {
            xdDeclaration.setTextContent(create((XmlSchemaSimpleTypeRestriction) simpleType.getContent(), name));
        }

        // TODO: list, union
        return xdDeclaration;
    }

    public String create(final XmlSchemaSimpleTypeRestriction simpleTypeRestriction, final String name) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, simpleTypeRestriction, "Creating declaration content ...");

        final QName baseType = simpleTypeRestriction.getBaseTypeName();
        final IDeclarationTypeFactory xdDeclarationFactory = Xsd2XdTypeMapping.getDefaultDataTypeFactory(baseType);
        if (xdDeclarationFactory == null) {
            XsdLogger.printP(LOG_ERROR, TRANSFORMATION, simpleTypeRestriction, "Unknown XSD data type! QName=" + baseType);
            return null;
        }

        xdDeclarationFactory.setName(name);
        return xdDeclarationFactory.build(simpleTypeRestriction.getFacets());
    }

}
