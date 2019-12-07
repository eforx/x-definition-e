package org.xdef.impl.util.conv.schema.schema2xd.xsd.factory;

import org.apache.ws.commons.schema.*;
import org.w3c.dom.Element;
import org.xdef.impl.util.conv.schema.schema2xd.xsd.factory.declaration.*;
import org.xdef.impl.util.conv.schema.schema2xd.xsd.util.Xsd2XdTypeMapping;
import org.xdef.impl.util.conv.schema.schema2xd.xsd.util.Xsd2XdUtils;
import org.xdef.impl.util.conv.schema.util.SchemaLogger;

import javax.xml.namespace.QName;
import java.util.LinkedList;
import java.util.List;

import static org.xdef.impl.util.conv.schema.util.SchemaLoggerDefs.*;
import static org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.AlgPhase.TRANSFORMATION;

public class XdDeclarationBuilder {

    /**
     * Input schema used for transformation
     */
    private XmlSchema schema;

    /**
     * X-definition XML element factory
     */
    private XdNodeFactory xdFactory;

    private XdDeclarationFactory xdDeclarationFactory;

    XmlSchemaSimpleType simpleType;

    Element parentNode;

    private IDeclarationTypeFactory.Mode mode;

    private String name;

    private QName baseType;

    XdDeclarationBuilder() {}

    XdDeclarationBuilder init(XmlSchema schema, XdNodeFactory xdFactory, XdDeclarationFactory xdDeclarationFactory) {
        this.schema = schema;
        this.xdFactory = xdFactory;
        this.xdDeclarationFactory = xdDeclarationFactory;
        return this;
    }

    public XdDeclarationBuilder setSimpleType(XmlSchemaSimpleType simpleType) {
        this.simpleType = simpleType;
        return this;
    }

    public XdDeclarationBuilder setParentNode(Element parentNode) {
        this.parentNode = parentNode;
        return this;
    }

    public XdDeclarationBuilder setMode(IDeclarationTypeFactory.Mode mode) {
        this.mode = mode;
        return this;
    }

    public XdDeclarationBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public XdDeclarationBuilder setBaseType(QName baseType) {
        this.baseType = baseType;
        return this;
    }

    @Override
    public XdDeclarationBuilder clone() {
        final XdDeclarationBuilder o = xdDeclarationFactory.createBuilder();
        o.simpleType = this.simpleType;
        o.parentNode = this.parentNode;
        o.mode = this.mode;
        o.name = this.name;
        o.baseType = this.baseType;
        return o;
    }

    String build() {
        if (mode == null) {
            SchemaLogger.printP(LOG_ERROR, TRANSFORMATION, simpleType, "Declaration mode is not set!");
        }

        SchemaLogger.printP(LOG_INFO, TRANSFORMATION, simpleType, "Building declaration. Mode=" + mode);

        if (IDeclarationTypeFactory.Mode.TOP_DECL.equals(mode)) {
            return createTopDeclaration();
        }

        return createDeclaration(simpleType.getContent());
    }

    private String createTopDeclaration() {
        SchemaLogger.printP(LOG_INFO, TRANSFORMATION, simpleType, "Building top declaration content ...");
        name = simpleType.getName();
        if (simpleType.getContent() instanceof XmlSchemaSimpleTypeRestriction) {
            return createTop((XmlSchemaSimpleTypeRestriction) simpleType.getContent());
        } else if (simpleType.getContent() instanceof XmlSchemaSimpleTypeList) {
            return create((XmlSchemaSimpleTypeList) simpleType.getContent(), null);
        }

        // TODO: union?
        SchemaLogger.printP(LOG_WARN, TRANSFORMATION, simpleType, "Empty top declaration has been created!");
        return "";
    }

    private String createTop(final XmlSchemaSimpleTypeRestriction simpleTypeRestriction) {
        SchemaLogger.printP(LOG_INFO, TRANSFORMATION, simpleTypeRestriction, "Building declaration content. Name=" + name + ", Mode=" + mode);

        final QName baseType = simpleTypeRestriction.getBaseTypeName();
        IDeclarationTypeFactory xdDeclarationTypeFactory = Xsd2XdTypeMapping.getDefaultDataTypeFactory(baseType);

        if (xdDeclarationTypeFactory == null) {
            final XmlSchemaType itemSchemaType = Xsd2XdUtils.getSchemaTypeByQName(schema, baseType);
            if (itemSchemaType instanceof XmlSchemaSimpleType) {
                final XmlSchemaSimpleType schemaSimpleType = (XmlSchemaSimpleType)itemSchemaType;
                if (IDeclarationTypeFactory.Mode.TOP_DECL.equals(mode)) {
                    xdDeclarationFactory.createDeclaration(clone().setSimpleType(schemaSimpleType));
                } else if (IDeclarationTypeFactory.Mode.TEXT_DECL.equals(mode)) {
                    return createDeclaration(schemaSimpleType.getContent());
                }
            }

            xdDeclarationTypeFactory = new DefaultTypeFactory(baseType.getLocalPart());
            xdDeclarationTypeFactory.setName(name);
            xdDeclarationTypeFactory.setMode(IDeclarationTypeFactory.Mode.TOP_DECL);
            return xdDeclarationTypeFactory.build("");
        }

        if (IDeclarationTypeFactory.Mode.TOP_DECL.equals(mode) && !xdDeclarationFactory.canBeProcessed(name)) {
            SchemaLogger.printP(LOG_DEBUG, TRANSFORMATION, simpleTypeRestriction, "Declaration has been already created. Name=" + name);
            return null;
        }

        xdDeclarationTypeFactory.setMode(mode);
        xdDeclarationTypeFactory.setName(name);
        return xdDeclarationTypeFactory.build(simpleTypeRestriction.getFacets());
    }

    private String createDeclaration(final XmlSchemaSimpleTypeContent simpleTypeContent) {
        if (simpleTypeContent instanceof XmlSchemaSimpleTypeRestriction) {
            return createDeclaration((XmlSchemaSimpleTypeRestriction)simpleTypeContent);
        }

        return createSetDeclaration(simpleTypeContent, null);
    }

    private String createSetDeclaration(final XmlSchemaSimpleTypeContent simpleTypeContent, final List<XmlSchemaFacet> extraFacets) {
        if (simpleTypeContent instanceof XmlSchemaSimpleTypeList) {
            return create((XmlSchemaSimpleTypeList)simpleTypeContent, extraFacets);
        } else if (simpleTypeContent instanceof XmlSchemaSimpleTypeUnion) {
            return create((XmlSchemaSimpleTypeUnion)simpleTypeContent, extraFacets);
        }

        SchemaLogger.printP(LOG_WARN, TRANSFORMATION, simpleType, "Empty set text declaration has been created!");
        return "";
    }

    private String createDeclaration(final XmlSchemaSimpleTypeRestriction simpleTypeRestriction) {
        if (baseType == null) {
            baseType = simpleTypeRestriction.getBaseTypeName();
        }

        if (baseType != null) {
            IDeclarationTypeFactory xdDeclarationFactory = Xsd2XdTypeMapping.getDefaultDataTypeFactory(baseType);
            if (xdDeclarationFactory != null) {
                xdDeclarationFactory.setMode(mode);
                return xdDeclarationFactory.build(simpleTypeRestriction.getFacets());
            }

            xdDeclarationFactory = new DefaultTypeFactory(baseType.getLocalPart());
            xdDeclarationFactory.setMode(mode);
            return xdDeclarationFactory.build("");
        } else if (simpleTypeRestriction.getBaseType() != null) {
            return createSetDeclaration(simpleTypeRestriction.getBaseType().getContent(), simpleTypeRestriction.getFacets());
        }

        SchemaLogger.printP(LOG_WARN, TRANSFORMATION, simpleTypeRestriction, "Empty restriction declaration has been created!");
        return "";
    }

    private String create(final XmlSchemaSimpleTypeUnion simpleTypeUnion, final List<XmlSchemaFacet> extraFacets) {
        final QName[] qNames = simpleTypeUnion.getMemberTypesQNames();
        if (qNames != null && qNames.length > 0) {
            if (qNames.length == 1) {
                final IDeclarationTypeFactory xdDeclarationFactory = Xsd2XdTypeMapping.getDefaultDataTypeFactory(qNames[0]);
                if (xdDeclarationFactory == null) {
                    SchemaLogger.printP(LOG_WARN, TRANSFORMATION, simpleTypeUnion, "Unknown XSD union member type! QName=" + qNames[0]);
                    return null;
                }

                final XmlSchemaSimpleTypeContent unionSimpleContent = simpleTypeUnion.getBaseTypes().get(0).getContent();
                if (unionSimpleContent instanceof XmlSchemaSimpleTypeRestriction) {
                    xdDeclarationFactory.setMode(mode);
                    return xdDeclarationFactory.build(((XmlSchemaSimpleTypeRestriction)unionSimpleContent).getFacets());
                }
            } else {
                final StringBuilder facetStringBuilder = new StringBuilder();
                for (QName qName : qNames) {
                    final XmlSchemaType itemSchemaType = Xsd2XdUtils.getSchemaTypeByQName(schema, qName);
                    if (itemSchemaType instanceof XmlSchemaSimpleType) {
                        final XmlSchemaSimpleType itemSimpleSchemaType = (XmlSchemaSimpleType) itemSchemaType;
                        if (itemSimpleSchemaType.getContent() instanceof XmlSchemaSimpleTypeRestriction) {
                            if (facetStringBuilder.length() > 0) {
                                facetStringBuilder.append(", ");
                            }

                            final XdDeclarationBuilder b = clone().setSimpleType(itemSimpleSchemaType).setMode(IDeclarationTypeFactory.Mode.DATATYPE_DECL);
                            facetStringBuilder.append(b.build());
                        }
                    }
                }

                final UnionTypeFactory unionTypeFactory = new UnionTypeFactory();
                unionTypeFactory.setMode(mode);
                return unionTypeFactory.build(facetStringBuilder.toString());
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
                    SchemaLogger.printP(LOG_WARN, TRANSFORMATION, simpleTypeUnion, "Unknown XSD union base type!");
                    return null;
                }

                xdDeclarationFactory.setMode(mode);
                return xdDeclarationFactory.build(facets);
            }
        }

        SchemaLogger.printP(LOG_WARN, TRANSFORMATION, simpleTypeUnion, "Empty union declaration has been created!");
        return "";
    }

    private String create(final XmlSchemaSimpleTypeList simpleTypeList, final List<XmlSchemaFacet> extraFacets) {
        SchemaLogger.printP(LOG_INFO, TRANSFORMATION, simpleTypeList, "Creating list declaration content. Name=" + name + ", Mode=" + mode);

        String facetString = "";
        final QName baseType = simpleTypeList.getItemTypeName();
        if (baseType != null) {
            final XmlSchemaType itemSchemaType = Xsd2XdUtils.getSchemaTypeByQName(schema, baseType);
            if (itemSchemaType instanceof XmlSchemaSimpleType) {
                final XmlSchemaSimpleType itemSimpleSchemaType = (XmlSchemaSimpleType) itemSchemaType;
                if (itemSimpleSchemaType.getContent() instanceof XmlSchemaSimpleTypeRestriction) {
                    final XdDeclarationBuilder b = clone().setSimpleType(itemSimpleSchemaType).setMode(IDeclarationTypeFactory.Mode.DATATYPE_DECL);
                    facetString = b.build();
                }
            }
        } else if (simpleTypeList.getItemType() != null) {
            if (simpleTypeList.getItemType().getContent() instanceof XmlSchemaSimpleTypeRestriction) {
                final XdDeclarationBuilder b = clone().setSimpleType(simpleTypeList.getItemType()).setMode(IDeclarationTypeFactory.Mode.DATATYPE_DECL);
                facetString = b.build();
            }
        }

        if (facetString.isEmpty()) {
            SchemaLogger.printP(LOG_DEBUG, TRANSFORMATION, simpleTypeList, "List declaration content empty");
        }

        String extraFacetsString = "";
        if (extraFacets != null && !extraFacets.isEmpty()) {
            final DefaultTypeFactory defaultTypeFactory = new DefaultTypeFactory("");
            defaultTypeFactory.setMode(IDeclarationTypeFactory.Mode.DATATYPE_DECL);
            extraFacetsString = defaultTypeFactory.build(extraFacets);
            if (!extraFacetsString.isEmpty()) {
                extraFacetsString = extraFacetsString.substring(1).substring(0, extraFacetsString.length() - 2);
                facetString += ", " + extraFacetsString;
            }
        }

        if (IDeclarationTypeFactory.Mode.TOP_DECL.equals(mode) && !xdDeclarationFactory.canBeProcessed(name)) {
            SchemaLogger.printP(LOG_DEBUG, TRANSFORMATION, simpleTypeList, "Declaration has been already created. Name=" + name);
            return null;
        }

        final ListTypeFactory listTypeFactory = new ListTypeFactory();
        listTypeFactory.setMode(mode);
        listTypeFactory.setName(name);
        return listTypeFactory.build(facetString);
    }

}
