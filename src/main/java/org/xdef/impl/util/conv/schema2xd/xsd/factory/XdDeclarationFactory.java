package org.xdef.impl.util.conv.schema2xd.xsd.factory;

import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.constants.Constants;
import org.w3c.dom.Element;
import org.xdef.impl.util.conv.schema.util.XsdLogger;
import org.xdef.impl.util.conv.schema2xd.xsd.factory.declaration.EmptyTypeFactory;
import org.xdef.impl.util.conv.schema2xd.xsd.factory.declaration.IDeclarationTypeFactory;
import org.xdef.impl.util.conv.schema2xd.xsd.factory.declaration.ListTypeFactory;
import org.xdef.impl.util.conv.schema2xd.xsd.model.XdAdapterCtx;
import org.xdef.impl.util.conv.schema2xd.xsd.util.Xsd2XdTypeMapping;
import org.xdef.impl.util.conv.schema2xd.xsd.util.Xsd2XdUtils;

import javax.xml.namespace.QName;

import java.util.LinkedList;
import java.util.List;

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
            XsdLogger.printP(LOG_WARN, TRANSFORMATION, simpleTypeRestriction, "Unknown XSD data type! QName=" + baseType);
            return null;
        }

        xdDeclarationFactory.setMode(mode);
        xdDeclarationFactory.setName(name);
        return xdDeclarationFactory.build(simpleTypeRestriction.getFacets());
    }

    public String createTextDeclaration(final XmlSchemaSimpleTypeRestriction simpleTypeRestriction, QName baseType) {
        if (baseType == null) {
            baseType = simpleTypeRestriction.getBaseTypeName();
        }

        final IDeclarationTypeFactory xdDeclarationFactory = Xsd2XdTypeMapping.getDefaultDataTypeFactory(baseType);
        if (xdDeclarationFactory == null) {
            XsdLogger.printP(LOG_WARN, TRANSFORMATION, simpleTypeRestriction, "Unknown XSD data type! QName=" + baseType);
            return null;
        }

        xdDeclarationFactory.setMode(IDeclarationTypeFactory.Mode.TEXT_DECL);
        return xdDeclarationFactory.build(simpleTypeRestriction.getFacets());
    }

    public String createTextDeclaration(final XmlSchemaSimpleTypeUnion simpleTypeUnion) {
        final QName[] qNames = simpleTypeUnion.getMemberTypesQNames();
        if (qNames != null && qNames.length > 0) {
            if (qNames.length == 1) {
                final IDeclarationTypeFactory xdDeclarationFactory = Xsd2XdTypeMapping.getDefaultDataTypeFactory(qNames[0]);
                if (xdDeclarationFactory == null) {
                    XsdLogger.printP(LOG_WARN, TRANSFORMATION, simpleTypeUnion, "Unknown XSD union member type! QName=" + qNames[0]);
                    return null;
                }

                final XmlSchemaSimpleTypeContent unionSimpleContent = simpleTypeUnion.getBaseTypes().get(0).getContent();
                if (unionSimpleContent instanceof XmlSchemaSimpleTypeRestriction) {
                    xdDeclarationFactory.setMode(IDeclarationTypeFactory.Mode.TEXT_DECL);
                    return xdDeclarationFactory.build(((XmlSchemaSimpleTypeRestriction)unionSimpleContent).getFacets());
                }
            } else {
                // TODO: multiple union member types
            }
        } else {
            final List<XmlSchemaSimpleType> baseTypes = simpleTypeUnion.getBaseTypes();
            if (baseTypes != null && !baseTypes.isEmpty()) {
                IDeclarationTypeFactory xdDeclarationFactory = null;
                final List<XmlSchemaFacet> facets = new LinkedList<XmlSchemaFacet>();

                for (XmlSchemaSimpleType baseType : baseTypes) {
                    if (baseType.getContent() instanceof XmlSchemaSimpleTypeRestriction) {
                        facets.addAll(((XmlSchemaSimpleTypeRestriction)baseType.getContent()).getFacets());
                        if (xdDeclarationFactory == null) {
                            xdDeclarationFactory = Xsd2XdTypeMapping.getDefaultDataTypeFactory(((XmlSchemaSimpleTypeRestriction) baseType.getContent()).getBaseTypeName());
                        }
                    }
                }

                if (xdDeclarationFactory == null) {
                    XsdLogger.printP(LOG_WARN, TRANSFORMATION, simpleTypeUnion, "Unknown XSD union base type!");
                    return null;
                }

                xdDeclarationFactory.setMode(IDeclarationTypeFactory.Mode.TEXT_DECL);
                return xdDeclarationFactory.build(facets);
            }
        }

        XsdLogger.printP(LOG_WARN, TRANSFORMATION, simpleTypeUnion, "Empty union declaration has been created!");
        return "";
    }

    public String createSimpleTextDeclaration(final QName baseType) {
        final EmptyTypeFactory emptyTypeFactory = new EmptyTypeFactory(baseType.getLocalPart());
        emptyTypeFactory.setMode(IDeclarationTypeFactory.Mode.TEXT_DECL);
        return emptyTypeFactory.build("");
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
