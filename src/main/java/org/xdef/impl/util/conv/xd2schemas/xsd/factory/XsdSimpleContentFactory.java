package org.xdef.impl.util.conv.xd2schemas.xsd.factory;

import javafx.util.Pair;
import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.constants.Constants;
import org.xdef.XDContainer;
import org.xdef.XDNamedValue;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.impl.XData;
import org.xdef.impl.util.conv.xd2schemas.xsd.definition.AlgPhase;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.DefaultFacetFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.IXsdFacetFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.xdef.ListFacetFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.xdef.UnionFacetFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.XsdAdapterCtx;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XD2XsdParserMapping;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdNameUtils;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.xdef.XDValueID.XD_CONTAINER;
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.AlgPhase.TRANSFORMATION;
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XD2XsdDefinitions.XSD_NAMESPACE_PREFIX_EMPTY;
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XsdLoggerDefs.LOG_INFO;
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XsdLoggerDefs.LOG_WARN;

public class XsdSimpleContentFactory {

    private final XsdElementFactory xsdFactory;
    private final XsdAdapterCtx adapterCtx;
    private final XData xData;
    private final String parserName;
    private XDNamedValue[] parameters = null;

    public XsdSimpleContentFactory(XsdElementFactory xsdFactory, XsdAdapterCtx adapterCtx, XData xData) {
        this.xsdFactory = xsdFactory;
        this.adapterCtx = adapterCtx;
        this.xData = xData;
        this.parserName = xData.getParserName();
    }

    public void setParameters(XDNamedValue[] parameters) {
        this.parameters = parameters;
    }

    public XmlSchemaSimpleTypeContent createSimpleContent(final String nodeName, boolean isAttr) {

        boolean customParser = true;
        boolean unknownParser = false;

        Pair<QName, IXsdFacetFactory> parserInfo = XD2XsdParserMapping.getCustomFacetFactory(parserName, parameters, adapterCtx);
        if (parserInfo == null) {
            parserInfo = XD2XsdParserMapping.getDefaultFacetFactory(parserName, adapterCtx);
            if (parserInfo != null) {
                customParser = false;
            }
        }

        if (parserInfo == null) {
            XsdLogger.printP(LOG_WARN, TRANSFORMATION, xData, "Unsupported simple content parser! Parser=" + parserName);
            parserInfo = new Pair(Constants.XSD_STRING, new DefaultFacetFactory());
            unknownParser = true;
        }

        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xData, "Following factory will be used. Factory=" + parserInfo.getValue().getClass().getSimpleName() + ", Parser=" + parserName);

        List<String> annotations = new LinkedList<String>();

        XmlSchemaSimpleTypeContent res;
        if (parserInfo.getValue() instanceof ListFacetFactory) {
            res = simpleTypeList(parserInfo.getKey(), parserInfo.getValue());
        } else if (parserInfo.getValue() instanceof UnionFacetFactory) {
            res = simpleTypeUnion(parserInfo.getValue(), nodeName);
        } else {
            res = simpleTypeRestriction(parserInfo.getKey(), parserInfo.getValue(), parameters);
            if (customParser || unknownParser) {
                annotations.add("Original x-definition parser: " + parserName);
            }
        }

        if (!isAttr && xData.getDefaultValue() != null) {
            annotations.add("Original x-definition default value: " + xData.getDefaultValue());
        }

        if (!annotations.isEmpty()) {
            res.setAnnotation(XsdElementFactory.createAnnotation(annotations, adapterCtx));
        }

        return res;
    }

    public XmlSchemaSimpleTypeRestriction createDefaultRestriction(final QName qName) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xData, "Creating restrictions of simple content (default facet factory will be used) ...");
        return simpleTypeRestriction(qName, new DefaultFacetFactory(), parameters);
    }

    private XmlSchemaSimpleTypeRestriction simpleTypeRestriction(final QName qName, final IXsdFacetFactory facetBuilder, final XDNamedValue[] parameters) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xData, "Creating simple type restriction. Type=" + qName);
        facetBuilder.setAdapterCtx(adapterCtx);
        final XmlSchemaSimpleTypeRestriction restriction = new XmlSchemaSimpleTypeRestriction();
        if (qName != null) {
            restriction.setBaseTypeName(qName);
        }
        restriction.getFacets().addAll(buildFacets(qName, facetBuilder, parameters));
        return restriction;
    }

    private XmlSchemaSimpleTypeContent simpleTypeList(final QName qName, final IXsdFacetFactory facetBuilder) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xData, "Creating simple type list. Type=" + qName);
        final XmlSchemaSimpleTypeList list = simpleTypeList(qName);
        final XmlSchemaSimpleTypeRestriction restriction = simpleTypeRestriction(qName, facetBuilder, parameters);
        return wrapUpSimpleTypeContent(restriction, list);
    }

    private XmlSchemaSimpleTypeList simpleTypeList(final QName qName) {
        final XmlSchemaSimpleTypeList list = new XmlSchemaSimpleTypeList();
        final XmlSchemaSimpleType simpleType = xsdFactory.createEmptySimpleType(false);
        XmlSchemaSimpleTypeRestriction restriction = null;

        for (XDNamedValue namedValue : parameters) {
            if (namedValue.getValue() instanceof XDParser) {
                restriction = simpleTypeRestriction(qName, new DefaultFacetFactory(), ((XDParser) namedValue.getValue()).getNamedParams().getXDNamedItems());
                break;
            }
        }

        if (restriction == null) {
            XsdLogger.printP(LOG_WARN, TRANSFORMATION, xData, "List restrictions have not been found!");
        } else {
            simpleType.setContent(restriction);
        }

        list.setItemType(simpleType);
        return list;
    }

    private XmlSchemaSimpleTypeContent simpleTypeUnion(final IXsdFacetFactory facetBuilder, final String nodeName) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xData, "Creating simple type union." );
        final XmlSchemaSimpleTypeUnion union = simpleTypeUnion(nodeName);
        final XmlSchemaSimpleTypeRestriction restriction = simpleTypeRestriction(null, facetBuilder, parameters);
        return wrapUpSimpleTypeContent(restriction, union);
    }

    private XmlSchemaSimpleTypeUnion simpleTypeUnion(final String nodeName) {
        final XmlSchemaSimpleTypeUnion union = new XmlSchemaSimpleTypeUnion();

        Set<String> refNames = new HashSet<String>();
        for (XDNamedValue namedValue : parameters) {
            if (namedValue.getValue() instanceof XDParser) {
                simpleTypeUnionTopReference((XDParser)namedValue.getValue(), refNames, nodeName);
            } else if (namedValue.getValue().getItemId() == XD_CONTAINER) {
                XDValue[] values = ((XDContainer) namedValue.getValue()).getXDItems();
                for (XDValue v : values) {
                    if (v instanceof XDParser) {
                        simpleTypeUnionTopReference((XDParser)v, refNames, nodeName);
                    } else {
                        // TODO: Wrap-up?
                    }
                }
            } else {
                // TODO: Wrap-up?
            }
        }

        if (!refNames.isEmpty()) {
            List<QName> refQNames = new LinkedList<QName>();
            for (String refName : refNames) {
                refQNames.add(new QName(XSD_NAMESPACE_PREFIX_EMPTY, refName));
            }

            union.setMemberTypesQNames(refQNames.toArray(new QName[refQNames.size()]));
        }

        return union;
    }

    private void simpleTypeUnionTopReference(final XDParser value, final Set<String> refNames, final String nodeName) {
        boolean unknownParser = false;
        Pair<QName, IXsdFacetFactory> parserInfo = XD2XsdParserMapping.getDefaultFacetFactory(value.parserName(), adapterCtx);
        if (parserInfo == null) {
            XsdLogger.printP(LOG_WARN, TRANSFORMATION, xData, "Unsupported simple content parser! Parser=" + value.parserName());
            parserInfo = new Pair(Constants.XSD_STRING, new DefaultFacetFactory());
            unknownParser = true;
        }

        final XmlSchemaSimpleTypeRestriction restriction = simpleTypeRestriction(parserInfo.getKey(), parserInfo.getValue(), value.getNamedParams().getXDNamedItems());
        if (unknownParser) {
            restriction.setAnnotation(XsdElementFactory.createAnnotation("Original x-definition parser: " + value.parserName(), adapterCtx));
        }

        final String refName = XsdNameUtils.newUnionRefTypeName(nodeName, parserInfo.getKey().getLocalPart());
        if (!refNames.add(refName)) {
            XsdLogger.printP(LOG_WARN, TRANSFORMATION, xData, "Union reference name already exists! RefName=" + refName);
        } else {
            final XmlSchemaSimpleType simpleType = xsdFactory.createEmptySimpleType(true);
            simpleType.setName(refName);
            simpleType.setContent(restriction);
        }
    }

    private XmlSchemaSimpleTypeContent wrapUpSimpleTypeContent(final XmlSchemaSimpleTypeRestriction restriction, final XmlSchemaSimpleTypeContent content) {
        // If exists some other restrictions for list, then wrap up list inside
        if (!restriction.getFacets().isEmpty()) {
            restriction.setBaseTypeName(null);
            final XmlSchemaSimpleType simpleType = xsdFactory.createEmptySimpleType(false);
            simpleType.setContent(content);
            restriction.setBaseType(simpleType);
            return restriction;
        }

        return content;
    }

    private List<XmlSchemaFacet> buildFacets(final QName qName, final IXsdFacetFactory facetBuilder, final XDNamedValue[] parameters) {
        if (qName != null && ("double".equals(qName.getLocalPart()) || "float".equals(qName.getLocalPart()))) {
            facetBuilder.setValueType(IXsdFacetFactory.ValueType.DECIMAL_FLOATING);
        } else if (qName != null && ("int".equals(qName.getLocalPart()) || "long".equals(qName.getLocalPart()))) {
            facetBuilder.setValueType(IXsdFacetFactory.ValueType.DECIMAL_INTEGER);
        } else {
            facetBuilder.setValueType(IXsdFacetFactory.ValueType.STRING);
        }

        return facetBuilder.build(parameters);
    }

    /*
    public static List<XmlSchemaFacet> buildFacets(final String parserName, final XDNamedValue[] parameters) {
        List<XmlSchemaFacet> facets = null;

        Pair<QName, IXsdFacetFactory> parserInfo = XD2XsdUtils.getCustomFacetFactory(parserName, parameters);
        if (parserInfo == null) {
            parserInfo = XD2XsdUtils.getDefaultFacetFactory(parserName);
        }

        if (parserInfo != null) {
            facets = buildFacets(parserInfo.getKey(), parserInfo.getValue(), parameters);
        }

        if (facets == null) {
            throw new RuntimeException("Unknown reference type parser: " + parserName);
        }

        return facets;
    }*/

}
