package org.xdef.impl.util.conv.schema.schema2xd.factory;

import org.apache.ws.commons.schema.*;
import org.w3c.dom.Element;
import org.xdef.impl.util.conv.schema.util.SchemaLogger;
import org.xdef.impl.util.conv.schema.schema2xd.factory.declaration.*;
import org.xdef.impl.util.conv.schema.schema2xd.util.Xsd2XdTypeMapping;
import org.xdef.impl.util.conv.schema.schema2xd.util.Xsd2XdUtils;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.xdef.impl.util.conv.schema.util.SchemaLoggerDefs.*;
import static org.xdef.impl.util.conv.schema.xd2schema.definition.AlgPhase.TRANSFORMATION;

public class XdDeclarationFactory {

    /**
     * Input schema used for transformation
     */
    private final XmlSchema schema;

    /**
     * X-definition XML element factory
     */
    final private XdElementFactory xdFactory;

    final Set<String> processedTopDeclarations = new HashSet<String>();

    public XdDeclarationFactory(XmlSchema schema, XdElementFactory xdFactory) {
        this.schema = schema;
        this.xdFactory = xdFactory;
    }

    public void createTopDeclaration(final XmlSchemaSimpleType simpleType, final Element parentNode) {
        SchemaLogger.printP(LOG_INFO, TRANSFORMATION, simpleType, "Creating declaration ...");
        final Element xdDeclaration = xdFactory.createEmptyDeclaration();
        xdDeclaration.setTextContent(_createTopDeclaration(simpleType, parentNode));
        parentNode.appendChild(xdDeclaration);
    }

    private String _createTopDeclaration(final XmlSchemaSimpleType simpleType, final Element parentNode) {
        SchemaLogger.printP(LOG_INFO, TRANSFORMATION, simpleType, "Creating top declaration content ...");
        final String name = simpleType.getName();
        if (simpleType.getContent() instanceof XmlSchemaSimpleTypeRestriction) {
            return _createTopDeclaration((XmlSchemaSimpleTypeRestriction) simpleType.getContent(), name, parentNode);
        } else if (simpleType.getContent() instanceof XmlSchemaSimpleTypeList) {
            return _createTopDeclaration((XmlSchemaSimpleTypeList) simpleType.getContent(), name);
        }

        // TODO: union?
        SchemaLogger.printP(LOG_WARN, TRANSFORMATION, simpleType, "Empty top declaration has been created!");
        return "";
    }

    private String _createTopDeclaration(final XmlSchemaSimpleTypeRestriction simpleTypeRestriction, final String name, final Element parentNode) {
        return create(simpleTypeRestriction, name, IDeclarationTypeFactory.Mode.TOP_DECL, parentNode);
    }

    private String _createTopDeclaration(final XmlSchemaSimpleTypeList simpleTypeList, final String name) {
        return _create(simpleTypeList, name, IDeclarationTypeFactory.Mode.TOP_DECL, null);
    }

    public String createTextDeclaration(final XmlSchemaSimpleTypeContent simpleTypeContent, final QName baseType) {
        if (simpleTypeContent instanceof XmlSchemaSimpleTypeRestriction) {
            return _createTextDeclaration((XmlSchemaSimpleTypeRestriction)simpleTypeContent, baseType);
        } else if (simpleTypeContent instanceof XmlSchemaSimpleTypeList) {
            return _createTextDeclaration((XmlSchemaSimpleTypeList)simpleTypeContent, null);
        } else if (simpleTypeContent instanceof XmlSchemaSimpleTypeUnion) {
            return _createTextDeclaration((XmlSchemaSimpleTypeUnion)simpleTypeContent, null);
        }

        return "";
    }

    private String _createTextDeclaration(final XmlSchemaSimpleTypeContent simpleTypeContent, final List<XmlSchemaFacet> extraFacets) {
        if (simpleTypeContent instanceof XmlSchemaSimpleTypeList) {
            return _createTextDeclaration((XmlSchemaSimpleTypeList)simpleTypeContent, extraFacets);
        } else if (simpleTypeContent instanceof XmlSchemaSimpleTypeUnion) {
            return _createTextDeclaration((XmlSchemaSimpleTypeUnion)simpleTypeContent, extraFacets);
        }

        return "";
    }

    private String _createTextDeclaration(final XmlSchemaSimpleTypeRestriction simpleTypeRestriction, QName baseType) {
        if (baseType == null) {
            baseType = simpleTypeRestriction.getBaseTypeName();
        }

        if (baseType != null) {
            IDeclarationTypeFactory xdDeclarationFactory = Xsd2XdTypeMapping.getDefaultDataTypeFactory(baseType);
            if (xdDeclarationFactory != null) {
                xdDeclarationFactory.setMode(IDeclarationTypeFactory.Mode.TEXT_DECL);
                return xdDeclarationFactory.build(simpleTypeRestriction.getFacets());
            }

            xdDeclarationFactory = new DefaultTypeFactory(baseType.getLocalPart());
            xdDeclarationFactory.setMode(IDeclarationTypeFactory.Mode.TEXT_DECL);
            return xdDeclarationFactory.build("");
        } else if (simpleTypeRestriction.getBaseType() != null) {
            return _createTextDeclaration(simpleTypeRestriction.getBaseType().getContent(), simpleTypeRestriction.getFacets());
        }

        SchemaLogger.printP(LOG_WARN, TRANSFORMATION, simpleTypeRestriction, "Empty restriction declaration has been created!");
        return "";
    }

    private String _createTextDeclaration(final XmlSchemaSimpleTypeList simpleTypeList, final List<XmlSchemaFacet> extraFacets) {
        return _create(simpleTypeList, null, IDeclarationTypeFactory.Mode.TEXT_DECL, extraFacets);
    }

    private String _createTextDeclaration(final XmlSchemaSimpleTypeUnion simpleTypeUnion, final List<XmlSchemaFacet> extraFacets) {
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
                    xdDeclarationFactory.setMode(IDeclarationTypeFactory.Mode.TEXT_DECL);
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
                            facetStringBuilder.append(create((XmlSchemaSimpleTypeRestriction) itemSimpleSchemaType.getContent(), null, IDeclarationTypeFactory.Mode.DATATYPE_DECL, null));
                        }
                    }
                }

                final UnionTypeFactory unionTypeFactory = new UnionTypeFactory();
                unionTypeFactory.setMode(IDeclarationTypeFactory.Mode.TEXT_DECL);
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

                xdDeclarationFactory.setMode(IDeclarationTypeFactory.Mode.TEXT_DECL);
                return xdDeclarationFactory.build(facets);
            }
        }

        SchemaLogger.printP(LOG_WARN, TRANSFORMATION, simpleTypeUnion, "Empty union declaration has been created!");
        return "";
    }

    public String createSimpleTextDeclaration(final QName baseType) {
        final EmptyTypeFactory emptyTypeFactory = new EmptyTypeFactory(baseType.getLocalPart());
        emptyTypeFactory.setMode(IDeclarationTypeFactory.Mode.TEXT_DECL);
        return emptyTypeFactory.build("");
    }

    public String create(final XmlSchemaSimpleTypeRestriction simpleTypeRestriction, final String name, final IDeclarationTypeFactory.Mode mode, final Element parentNode) {
        SchemaLogger.printP(LOG_INFO, TRANSFORMATION, simpleTypeRestriction, "Creating declaration content. Name=" + name + ", Mode=" + mode);

        final QName baseType = simpleTypeRestriction.getBaseTypeName();
        IDeclarationTypeFactory xdDeclarationFactory = Xsd2XdTypeMapping.getDefaultDataTypeFactory(baseType);

        if (xdDeclarationFactory == null) {
            final XmlSchemaType itemSchemaType = Xsd2XdUtils.getSchemaTypeByQName(schema, baseType);
            if (itemSchemaType instanceof XmlSchemaSimpleType) {
                final XmlSchemaSimpleType schemaSimpleType = (XmlSchemaSimpleType)itemSchemaType;
                if (IDeclarationTypeFactory.Mode.TEXT_DECL.equals(mode)) {
                    return createTextDeclaration(schemaSimpleType.getContent(), null);
                } else if (IDeclarationTypeFactory.Mode.TOP_DECL.equals(mode)) {
                    final String res = _createTopDeclaration(schemaSimpleType, parentNode);
                    if (parentNode != null && res != null) {
                        final Element xdDeclaration = xdFactory.createEmptyDeclaration();
                        xdDeclaration.setTextContent(res);
                        parentNode.appendChild(xdDeclaration);
                    }
                }
            }

            xdDeclarationFactory = new DefaultTypeFactory(name);
            xdDeclarationFactory.setName(name);
            xdDeclarationFactory.setMode(IDeclarationTypeFactory.Mode.TOP_DECL);
            return xdDeclarationFactory.build(baseType.getLocalPart(), "");
        }

        if (IDeclarationTypeFactory.Mode.TOP_DECL.equals(mode) && !processedTopDeclarations.add(name)) {
            SchemaLogger.printP(LOG_DEBUG, TRANSFORMATION, simpleTypeRestriction, "Declaration has been already created. Name=" + name);
            return null;
        }

        xdDeclarationFactory.setMode(mode);
        xdDeclarationFactory.setName(name);
        return xdDeclarationFactory.build(simpleTypeRestriction.getFacets());
    }

    private String _create(final XmlSchemaSimpleTypeList simpleTypeList, final String name, final IDeclarationTypeFactory.Mode mode, final List<XmlSchemaFacet> extraFacets) {
        SchemaLogger.printP(LOG_INFO, TRANSFORMATION, simpleTypeList, "Creating list declaration content. Name=" + name + ", Mode=" + mode);

        String facetString = "";
        final QName baseType = simpleTypeList.getItemTypeName();
        if (baseType != null) {
            final XmlSchemaType itemSchemaType = Xsd2XdUtils.getSchemaTypeByQName(schema, baseType);
            if (itemSchemaType instanceof XmlSchemaSimpleType) {
                final XmlSchemaSimpleType itemSimpleSchemaType = (XmlSchemaSimpleType) itemSchemaType;
                if (itemSimpleSchemaType.getContent() instanceof XmlSchemaSimpleTypeRestriction) {
                    facetString = create((XmlSchemaSimpleTypeRestriction) itemSimpleSchemaType.getContent(), null, IDeclarationTypeFactory.Mode.DATATYPE_DECL, null);
                }
            }
        } else if (simpleTypeList.getItemType() != null) {
            if (simpleTypeList.getItemType().getContent() instanceof XmlSchemaSimpleTypeRestriction) {
                facetString = create((XmlSchemaSimpleTypeRestriction)simpleTypeList.getItemType().getContent(), null, IDeclarationTypeFactory.Mode.DATATYPE_DECL, null);
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

        if (IDeclarationTypeFactory.Mode.TOP_DECL.equals(mode) && !processedTopDeclarations.add(name)) {
            SchemaLogger.printP(LOG_DEBUG, TRANSFORMATION, simpleTypeList, "Declaration has been already created. Name=" + name);
            return null;
        }

        final ListTypeFactory listTypeFactory = new ListTypeFactory();
        listTypeFactory.setMode(mode);
        listTypeFactory.setName(name);
        return listTypeFactory.build(facetString);
    }

}
