package org.xdef.impl.util.conv.schema2xd.xsd.factory;

import org.apache.ws.commons.schema.*;
import org.w3c.dom.Element;
import org.xdef.impl.util.conv.schema.util.XsdLogger;
import org.xdef.impl.util.conv.schema2xd.xsd.factory.declaration.IDeclarationTypeFactory;
import org.xdef.impl.util.conv.schema2xd.xsd.factory.declaration.ListTypeFactory;
import org.xdef.impl.util.conv.schema2xd.xsd.model.XdAdapterCtx;
import org.xdef.impl.util.conv.schema2xd.xsd.util.Xsd2XdTypeMapping;
import org.xdef.impl.util.conv.schema2xd.xsd.util.Xsd2XdUtils;

import javax.xml.namespace.QName;

import static org.xdef.impl.util.conv.schema.util.XsdLoggerDefs.*;
import static org.xdef.impl.util.conv.xd2schema.xsd.definition.AlgPhase.TRANSFORMATION;

public class XdDeclarationFactory {

    /**
     * Input schema used for transformation
     */
    private final XmlSchema schema;

    /**
     * X-definition XML element factory
     */
    final private XdElementFactory xdFactory;

    final private XdAdapterCtx adapterCtx;

    public XdDeclarationFactory(XmlSchema schema, XdElementFactory xdFactory, XdAdapterCtx adapterCtx) {
        this.schema = schema;
        this.xdFactory = xdFactory;
        this.adapterCtx = adapterCtx;
    }

    public Element createTopDeclaration(final XmlSchemaSimpleType simpleType) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, simpleType, "Creating declaration ...");
        final Element xdDeclaration = xdFactory.createEmptyDeclaration();
        final String name = simpleType.getName();
        if (simpleType.getContent() instanceof XmlSchemaSimpleTypeRestriction) {
            xdDeclaration.setTextContent(createTopDeclaration((XmlSchemaSimpleTypeRestriction) simpleType.getContent(), name));
        } else if (simpleType.getContent() instanceof XmlSchemaSimpleTypeList) {
            xdDeclaration.setTextContent(createTopDeclaration((XmlSchemaSimpleTypeList) simpleType.getContent(), name));
        }

        // TODO: union
        return xdDeclaration;
    }

    public String createTopDeclaration(final XmlSchemaSimpleTypeRestriction simpleTypeRestriction, final String name) {
        return create(simpleTypeRestriction, name, IDeclarationTypeFactory.Mode.NAMED_DECL);
    }

    public String create(final XmlSchemaSimpleTypeRestriction simpleTypeRestriction, final String name, final IDeclarationTypeFactory.Mode mode) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, simpleTypeRestriction, "Creating declaration content. Name=" + name + ", Mode=" + mode);

        final QName baseType = simpleTypeRestriction.getBaseTypeName();
        final IDeclarationTypeFactory xdDeclarationFactory = Xsd2XdTypeMapping.getDefaultDataTypeFactory(baseType);
        if (xdDeclarationFactory == null) {
            XsdLogger.printP(LOG_ERROR, TRANSFORMATION, simpleTypeRestriction, "Unknown XSD data type! QName=" + baseType);
            return null;
        }

        xdDeclarationFactory.setMode(mode);
        xdDeclarationFactory.setName(name);
        return xdDeclarationFactory.build(simpleTypeRestriction.getFacets());
    }

    public String createTextDeclaration(final XmlSchemaSimpleTypeRestriction simpleTypeRestriction, final QName baseType) {
        final IDeclarationTypeFactory xdDeclarationFactory = Xsd2XdTypeMapping.getDefaultDataTypeFactory(baseType);
        if (xdDeclarationFactory == null) {
            XsdLogger.printP(LOG_ERROR, TRANSFORMATION, simpleTypeRestriction, "Unknown XSD data type! QName=" + baseType);
            return null;
        }

        xdDeclarationFactory.setMode(IDeclarationTypeFactory.Mode.TEXT_DECL);
        return xdDeclarationFactory.build(simpleTypeRestriction.getFacets());
    }

    private String createTopDeclaration(final XmlSchemaSimpleTypeList simpleTypeList, final String name) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, simpleTypeList, "Creating list declaration content ...");

        String facetString = "";
        final QName baseType = simpleTypeList.getItemTypeName();
        if (baseType != null) {
            final XmlSchemaType itemSchemaType = Xsd2XdUtils.getSchemaTypeByQName(schema, baseType);
            if (itemSchemaType instanceof XmlSchemaSimpleType) {
                final XmlSchemaSimpleType itemSimpleSchemaType = (XmlSchemaSimpleType) itemSchemaType;
                if (itemSimpleSchemaType.getContent() instanceof XmlSchemaSimpleTypeRestriction) {
                    facetString = create((XmlSchemaSimpleTypeRestriction) itemSimpleSchemaType.getContent(), null, IDeclarationTypeFactory.Mode.DATATYPE_DECL);
                }
            }
        } else if (simpleTypeList.getItemType() != null) {
            if (simpleTypeList.getItemType().getContent() instanceof XmlSchemaSimpleTypeRestriction) {
                facetString = create((XmlSchemaSimpleTypeRestriction)simpleTypeList.getItemType().getContent(), null, IDeclarationTypeFactory.Mode.DATATYPE_DECL);
            }
        }

        if (facetString.isEmpty()) {
            XsdLogger.printP(LOG_WARN, TRANSFORMATION, simpleTypeList, "Unsuccessfully created list declaration content!");
        }

        ListTypeFactory listTypeFactory = new ListTypeFactory();
        listTypeFactory.setMode(IDeclarationTypeFactory.Mode.NAMED_DECL);
        listTypeFactory.setName(name);
        return listTypeFactory.build(facetString);
    }
}
