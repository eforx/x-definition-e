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

    public Element create(final XmlSchemaType schemaType) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, schemaType, "Creating declaration ...");

        final Element xdDeclaration = xdFactory.createEmptyDeclaration();

        if (schemaType instanceof XmlSchemaSimpleType) {
            final String declarationContent = create((XmlSchemaSimpleType)schemaType);
            xdDeclaration.setTextContent(declarationContent);
        }

        return xdDeclaration;
    }

    private String create(final XmlSchemaSimpleType simpleType) {
        final String name = simpleType.getName();
        if (simpleType.getContent() instanceof XmlSchemaSimpleTypeRestriction) {
            final XmlSchemaSimpleTypeRestriction xsdContentRestriction = (XmlSchemaSimpleTypeRestriction) simpleType.getContent();
            final QName baseType = xsdContentRestriction.getBaseTypeName();
            final IDeclarationTypeFactory xdDeclarationFactory = Xsd2XdTypeMapping.getDefaultDataTypeFactory(baseType);
            if (xdDeclarationFactory == null) {
                XsdLogger.printP(LOG_ERROR, TRANSFORMATION, simpleType, "Unknown XSD data type! QName=" + baseType);
                return "";
            }
            xdDeclarationFactory.setName(name);
            return xdDeclarationFactory.build(xsdContentRestriction.getFacets());
        }

        // TODO: list, union
        return "";
    }

}
