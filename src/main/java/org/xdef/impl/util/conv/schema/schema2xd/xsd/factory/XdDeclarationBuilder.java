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

/**
 * Creates x-definition declaration and declaration content for x-definition declarations
 */
public class XdDeclarationBuilder {

    /**
     * Input schema used for transformation
     */
    private XmlSchema schema;

    /**
     * X-definition declarations factory
     */
    private XdDeclarationFactory xdDeclarationFactory;

    /**
     * XSD schema type node to be transformed
     */
    XmlSchemaSimpleType simpleType;

    /**
     * Parent x-definition node where should be declaration
     */
    Element parentNode;

    /**
     * Type of x-definition declaration
     */
    private IDeclarationTypeFactory.Type type;

    /**
     * Declaration variable name
     */
    private String name;

    /**
     * Declaration qualified name
     */
    private QName baseType;

    XdDeclarationBuilder() {}

    /**
     * Initialize x-definition declaration builder with default values
     * @param schema
     * @param xdDeclarationFactory
     * @return
     */
    XdDeclarationBuilder init(XmlSchema schema, XdDeclarationFactory xdDeclarationFactory) {
        this.schema = schema;
        this.xdDeclarationFactory = xdDeclarationFactory;
        return this;
    }

    /**
     * Sets XSD schema type
     * @param simpleType XSD schema type
     * @return current instance
     */
    public XdDeclarationBuilder setSimpleType(XmlSchemaSimpleType simpleType) {
        this.simpleType = simpleType;
        return this;
    }

    /**
     * Sets Parent x-definition node
     * @param parentNode Parent x-definition node
     * @return current instance
     */
    public XdDeclarationBuilder setParentNode(Element parentNode) {
        this.parentNode = parentNode;
        return this;
    }

    /**
     * Sets x-definition declaration
     * @param type x-definition declaration
     * @return current instance
     */
    public XdDeclarationBuilder setType(IDeclarationTypeFactory.Type type) {
        this.type = type;
        return this;
    }

    /**
     * Sets variable name
     * @param name variable name
     * @return current instance
     */
    public XdDeclarationBuilder setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets declaration qualified name
     * @param baseType Declaration qualified name
     * @return current instance
     */
    public XdDeclarationBuilder setBaseType(QName baseType) {
        this.baseType = baseType;
        return this;
    }

    @Override
    public XdDeclarationBuilder clone() {
        final XdDeclarationBuilder o = xdDeclarationFactory.createBuilder();
        o.simpleType = this.simpleType;
        o.parentNode = this.parentNode;
        o.type = this.type;
        o.name = this.name;
        o.baseType = this.baseType;
        return o;
    }

    /**
     * Creates x-definition declaration/declaration content based on internal state
     * @return x-definition declaration/declaration content
     */
    String build() {
        if (type == null) {
            SchemaLogger.printP(LOG_ERROR, TRANSFORMATION, simpleType, "Declaration type is not set!");
        }

        SchemaLogger.printP(LOG_INFO, TRANSFORMATION, simpleType, "Building declaration. Type=" + type);

        if (IDeclarationTypeFactory.Type.TOP_DECL.equals(type)) {
            return createTopDeclaration();
        }

        return createDeclaration(simpleType.getContent());
    }

    /**
     * Creates x-definition root declaration based on internal state
     * @return x-definition declaration
     */
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

    /**
     * Creates x-definition root declaration based on internal state and input XSD restriction node
     * @param simpleTypeRestriction     XSD restriction node
     * @return x-definition declaration
     */
    private String createTop(final XmlSchemaSimpleTypeRestriction simpleTypeRestriction) {
        SchemaLogger.printP(LOG_INFO, TRANSFORMATION, simpleTypeRestriction, "Building declaration content. Name=" + name + ", Type=" + type);

        final QName baseType = simpleTypeRestriction.getBaseTypeName();
        IDeclarationTypeFactory xdDeclarationTypeFactory = Xsd2XdTypeMapping.findDefaultDataTypeFactory(baseType);

        if (xdDeclarationTypeFactory == null) {
            final XmlSchemaType itemSchemaType = Xsd2XdUtils.findSchemaTypeByQName(schema, baseType);
            if (itemSchemaType instanceof XmlSchemaSimpleType) {
                final XmlSchemaSimpleType schemaSimpleType = (XmlSchemaSimpleType)itemSchemaType;
                if (IDeclarationTypeFactory.Type.TOP_DECL.equals(type)) {
                    xdDeclarationFactory.createDeclaration(clone().setSimpleType(schemaSimpleType));
                } else if (IDeclarationTypeFactory.Type.TEXT_DECL.equals(type)) {
                    return createDeclaration(schemaSimpleType.getContent());
                }
            }

            xdDeclarationTypeFactory = new DefaultTypeFactory(baseType.getLocalPart());
            xdDeclarationTypeFactory.setName(name);
            xdDeclarationTypeFactory.setType(IDeclarationTypeFactory.Type.TOP_DECL);
            return xdDeclarationTypeFactory.build("");
        }

        if (IDeclarationTypeFactory.Type.TOP_DECL.equals(type) && !xdDeclarationFactory.canBeProcessed(name)) {
            SchemaLogger.printP(LOG_DEBUG, TRANSFORMATION, simpleTypeRestriction, "Declaration has been already created. Name=" + name);
            return null;
        }

        xdDeclarationTypeFactory.setType(type);
        xdDeclarationTypeFactory.setName(name);
        return xdDeclarationTypeFactory.build(simpleTypeRestriction.getFacets());
    }

    /**
     * Creates x-definition declaration content based on internal state and input XSD schema simple content node
     * @param simpleTypeContent     XSD schema simple content node
     * @return x-definition declaration content
     */
    private String createDeclaration(final XmlSchemaSimpleTypeContent simpleTypeContent) {
        if (simpleTypeContent instanceof XmlSchemaSimpleTypeRestriction) {
            return createDeclaration((XmlSchemaSimpleTypeRestriction)simpleTypeContent);
        }

        return createSetDeclaration(simpleTypeContent, null);
    }

    /**
     * Creates x-definition declaration of x-definition list/union based on internal state and input XSD union/list node
     * @param simpleTypeContent     XSD union/list node
     * @param extraFacets           additional list of XSD facet nodes, which should be applied
     * @return x-definition declaration
     */
    private String createSetDeclaration(final XmlSchemaSimpleTypeContent simpleTypeContent, final List<XmlSchemaFacet> extraFacets) {
        if (simpleTypeContent instanceof XmlSchemaSimpleTypeList) {
            return create((XmlSchemaSimpleTypeList)simpleTypeContent, extraFacets);
        } else if (simpleTypeContent instanceof XmlSchemaSimpleTypeUnion) {
            return create((XmlSchemaSimpleTypeUnion)simpleTypeContent, extraFacets);
        }

        SchemaLogger.printP(LOG_WARN, TRANSFORMATION, simpleType, "Empty set text declaration has been created!");
        return "";
    }

    /**
     * Creates x-definition declaration/declaration content based on internal state and input XSD restriction node
     * @param simpleTypeRestriction     XSD restriction node
     * @return x-definition declaration/declaration content
     */
    private String createDeclaration(final XmlSchemaSimpleTypeRestriction simpleTypeRestriction) {
        if (baseType == null) {
            baseType = simpleTypeRestriction.getBaseTypeName();
        }

        if (baseType != null) {
            IDeclarationTypeFactory xdDeclarationFactory = Xsd2XdTypeMapping.findDefaultDataTypeFactory(baseType);
            if (xdDeclarationFactory != null) {
                xdDeclarationFactory.setType(type);
                return xdDeclarationFactory.build(simpleTypeRestriction.getFacets());
            }

            xdDeclarationFactory = new DefaultTypeFactory(baseType.getLocalPart());
            xdDeclarationFactory.setType(type);
            return xdDeclarationFactory.build("");
        } else if (simpleTypeRestriction.getBaseType() != null) {
            return createSetDeclaration(simpleTypeRestriction.getBaseType().getContent(), simpleTypeRestriction.getFacets());
        }

        SchemaLogger.printP(LOG_WARN, TRANSFORMATION, simpleTypeRestriction, "Empty restriction declaration has been created!");
        return "";
    }

    /**
     * Creates x-definition declaration/declaration content based on internal state and input XSD union node
     * @param simpleTypeUnion       XSD union node
     * @param extraFacets           additional list of XSD facet nodes, which should be applied
     * @return x-definition declaration/declaration content
     */
    private String create(final XmlSchemaSimpleTypeUnion simpleTypeUnion, final List<XmlSchemaFacet> extraFacets) {
        final QName[] qNames = simpleTypeUnion.getMemberTypesQNames();
        if (qNames != null && qNames.length > 0) {
            if (qNames.length == 1) {
                final IDeclarationTypeFactory xdDeclarationFactory = Xsd2XdTypeMapping.findDefaultDataTypeFactory(qNames[0]);
                if (xdDeclarationFactory == null) {
                    SchemaLogger.printP(LOG_WARN, TRANSFORMATION, simpleTypeUnion, "Unknown XSD union member type! QName=" + qNames[0]);
                    return null;
                }

                final XmlSchemaSimpleTypeContent unionSimpleContent = simpleTypeUnion.getBaseTypes().get(0).getContent();
                if (unionSimpleContent instanceof XmlSchemaSimpleTypeRestriction) {
                    xdDeclarationFactory.setType(type);
                    return xdDeclarationFactory.build(((XmlSchemaSimpleTypeRestriction)unionSimpleContent).getFacets());
                }
            } else {
                final StringBuilder facetStringBuilder = new StringBuilder();
                for (QName qName : qNames) {
                    final XmlSchemaType itemSchemaType = Xsd2XdUtils.findSchemaTypeByQName(schema, qName);
                    if (itemSchemaType instanceof XmlSchemaSimpleType) {
                        final XmlSchemaSimpleType itemSimpleSchemaType = (XmlSchemaSimpleType) itemSchemaType;
                        if (itemSimpleSchemaType.getContent() instanceof XmlSchemaSimpleTypeRestriction) {
                            if (facetStringBuilder.length() > 0) {
                                facetStringBuilder.append(", ");
                            }

                            final XdDeclarationBuilder b = clone().setSimpleType(itemSimpleSchemaType).setType(IDeclarationTypeFactory.Type.DATATYPE_DECL);
                            facetStringBuilder.append(b.build());
                        }
                    }
                }

                final UnionTypeFactory unionTypeFactory = new UnionTypeFactory();
                unionTypeFactory.setType(type);
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
                            xdDeclarationFactory = Xsd2XdTypeMapping.findDefaultDataTypeFactory(((XmlSchemaSimpleTypeRestriction) baseType.getContent()).getBaseTypeName());
                        }
                    }
                }

                if (xdDeclarationFactory == null) {
                    SchemaLogger.printP(LOG_WARN, TRANSFORMATION, simpleTypeUnion, "Unknown XSD union base type!");
                    return null;
                }

                xdDeclarationFactory.setType(type);
                return xdDeclarationFactory.build(facets);
            }
        }

        SchemaLogger.printP(LOG_WARN, TRANSFORMATION, simpleTypeUnion, "Empty union declaration has been created!");
        return "";
    }

    /**
     * Creates x-definition declaration/declaration content based on internal state and input XSD list node
     * @param simpleTypeList        XSD list node
     * @param extraFacets           additional list of XSD facet nodes, which should be applied
     * @return x-definition declaration/declaration content
     */
    private String create(final XmlSchemaSimpleTypeList simpleTypeList, final List<XmlSchemaFacet> extraFacets) {
        SchemaLogger.printP(LOG_INFO, TRANSFORMATION, simpleTypeList, "Creating list declaration content. Name=" + name + ", Type=" + type);

        String facetString = "";
        final QName baseType = simpleTypeList.getItemTypeName();
        if (baseType != null) {
            final XmlSchemaType itemSchemaType = Xsd2XdUtils.findSchemaTypeByQName(schema, baseType);
            if (itemSchemaType instanceof XmlSchemaSimpleType) {
                final XmlSchemaSimpleType itemSimpleSchemaType = (XmlSchemaSimpleType) itemSchemaType;
                if (itemSimpleSchemaType.getContent() instanceof XmlSchemaSimpleTypeRestriction) {
                    final XdDeclarationBuilder b = clone().setSimpleType(itemSimpleSchemaType).setType(IDeclarationTypeFactory.Type.DATATYPE_DECL);
                    facetString = b.build();
                }
            }
        } else if (simpleTypeList.getItemType() != null) {
            if (simpleTypeList.getItemType().getContent() instanceof XmlSchemaSimpleTypeRestriction) {
                final XdDeclarationBuilder b = clone().setSimpleType(simpleTypeList.getItemType()).setType(IDeclarationTypeFactory.Type.DATATYPE_DECL);
                facetString = b.build();
            }
        }

        if (facetString.isEmpty()) {
            SchemaLogger.printP(LOG_DEBUG, TRANSFORMATION, simpleTypeList, "List declaration content empty");
        }

        if (extraFacets != null && !extraFacets.isEmpty()) {
            final DefaultTypeFactory defaultTypeFactory = new DefaultTypeFactory("");
            defaultTypeFactory.setType(IDeclarationTypeFactory.Type.DATATYPE_DECL);
            String extraFacetsString = defaultTypeFactory.build(extraFacets);
            if (!extraFacetsString.isEmpty()) {
                extraFacetsString = extraFacetsString.substring(1).substring(0, extraFacetsString.length() - 2);
                facetString += ", " + extraFacetsString;
            }
        }

        if (IDeclarationTypeFactory.Type.TOP_DECL.equals(type) && !xdDeclarationFactory.canBeProcessed(name)) {
            SchemaLogger.printP(LOG_DEBUG, TRANSFORMATION, simpleTypeList, "Declaration has been already created. Name=" + name);
            return null;
        }

        final ListTypeFactory listTypeFactory = new ListTypeFactory();
        listTypeFactory.setType(type);
        listTypeFactory.setName(name);
        return listTypeFactory.build(facetString);
    }

}
