package org.xdef.impl.util.conv.schema.xd2schema.xsd.factory;

import javafx.util.Pair;
import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.constants.Constants;
import org.xdef.XDContainer;
import org.xdef.XDNamedValue;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.impl.XData;
import org.xdef.impl.util.conv.schema.util.SchemaLogger;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.factory.facet.DefaultFacetFactory;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.factory.facet.IXsdFacetFactory;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.factory.facet.xdef.ListFacetFactory;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.factory.facet.xdef.UnionFacetFactory;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.model.XsdAdapterCtx;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.util.Xd2XsdParserMapping;
import org.xdef.msg.XSD;
import org.xdef.sys.ReportWriter;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.xdef.XDValueID.XD_CONTAINER;
import static org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.AlgPhase.TRANSFORMATION;
import static org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.Xd2XsdDefinitions.XSD_NAMESPACE_PREFIX_EMPTY;
import static org.xdef.impl.util.conv.schema.util.SchemaLoggerDefs.*;

/**
 * Creates multiple types of XSD simple content node
 */
public class XsdSimpleContentFactory {

    private final XsdNodeFactory xsdFactory;
    private final XsdAdapterCtx adapterCtx;
    /**
     * Source x-definition node
     */
    private final XData xData;
    /**
     * X-definition node parser name
     */
    private final String parserName;
    /**
     * X-definition node parser parameters
     */
    private XDNamedValue[] parameters = null;

    /**
     * @param xsdFactory    XSD element factory
     * @param adapterCtx    XSD adapter context
     * @param xData         source x-definition node
     */
    public XsdSimpleContentFactory(XsdNodeFactory xsdFactory, XsdAdapterCtx adapterCtx, XData xData) {
        this.xsdFactory = xsdFactory;
        this.adapterCtx = adapterCtx;
        this.xData = xData;
        this.parserName = xData.getParserName();
    }

    public void setParameters(XDNamedValue[] parameters) {
        this.parameters = parameters;
    }

    /**
     * Create XSD simple content node
     * @param nodeName  node name (required for <xs:union/> node)
     * @param isAttr    flag if x-definition node is attribute
     * @return based on x-definition node parser
     *          <xs:restriction base="...">...</xs:restriction>
     *          <xs:list itemType="...">...</xs:list>
     *          <xs:union memberTypes="...">...</xs:union>
     */
    public XmlSchemaSimpleTypeContent createSimpleContent(final String nodeName, boolean isAttr) {
        boolean customParser = true;
        boolean unknownParser = false;

        Pair<QName, IXsdFacetFactory> parserInfo = Xd2XsdParserMapping.findCustomFacetFactory(parserName, parameters, adapterCtx);
        if (parserInfo == null) {
            parserInfo = Xd2XsdParserMapping.findDefaultFacetFactory(parserName, adapterCtx);
            if (parserInfo != null) {
                customParser = false;
            }
        }

        if (parserInfo == null) {
            adapterCtx.getReportWriter().warning(XSD.XSD026, parserName);
            SchemaLogger.printP(LOG_WARN, TRANSFORMATION, xData, "Unsupported simple content parser! Parser=" + parserName);
            parserInfo = new Pair(Constants.XSD_STRING, new DefaultFacetFactory());
            unknownParser = true;
        }

        SchemaLogger.printP(LOG_INFO, TRANSFORMATION, xData, "Following factory will be used. Factory=" + parserInfo.getValue().getClass().getSimpleName() + ", Parser=" + parserName);

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
            res.setAnnotation(XsdNodeFactory.createAnnotation(annotations, adapterCtx));
        }

        return res;
    }

    /**
     * Creates XSD simple type string restriction node without any facet
     * @return <xs:restriction base="xs:string"/>
     */
    public XmlSchemaSimpleTypeRestriction createDefaultRestriction() {
        SchemaLogger.printP(LOG_INFO, TRANSFORMATION, xData, "Creating restrictions of simple content (default facet factory will be used) ...");
        return simpleTypeRestriction(Constants.XSD_STRING, new DefaultFacetFactory(), null);
    }

    /**
     * Creates XSD simple type restriction node with facet based on input parameters
     * @param qName             XSD restriction base
     * @param facetBuilder      XSD restriction facet builder
     * @param parameters        source x-definition parameters for facets building
     * @return <xs:restriction base="{@paramref qName}">...</xs:restriction>
     */
    private XmlSchemaSimpleTypeRestriction simpleTypeRestriction(final QName qName, final IXsdFacetFactory facetBuilder, final XDNamedValue[] parameters) {
        SchemaLogger.printP(LOG_INFO, TRANSFORMATION, xData, "Creating simple type restriction. Type=" + qName);
        facetBuilder.setAdapterCtx(adapterCtx);
        final XmlSchemaSimpleTypeRestriction restriction = new XmlSchemaSimpleTypeRestriction();
        if (qName != null) {
            restriction.setBaseTypeName(qName);
        }
        restriction.getFacets().addAll(buildFacets(qName, facetBuilder, parameters));
        return restriction;
    }

    /**
     * Creates XSD simple type list node and restriction node with facets
     * @param qName             XSD list item type and XSD restriction base
     * @param facetBuilder      XSD restriction facet builder
     * @return  <xs:list itemType="{@paramref qName}">...</xs:list> if restriction has no facets
     *          <xs:restriction>...</xs:restriction> otherwise
     */
    private XmlSchemaSimpleTypeContent simpleTypeList(final QName qName, final IXsdFacetFactory facetBuilder) {
        SchemaLogger.printP(LOG_INFO, TRANSFORMATION, xData, "Creating simple type list. Type=" + qName);
        final XmlSchemaSimpleTypeList list = simpleTypeList(qName);
        final XmlSchemaSimpleTypeRestriction restriction = simpleTypeRestriction(qName, facetBuilder, parameters);
        return wrapUpSimpleTypeContent(restriction, list);
    }

    /**
     * Creates XSD simple type list node
     * @param qName     XSD list item type
     * @return <xs:list itemType="{@paramref qName}">...</xs:list>
     */
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
            adapterCtx.getReportWriter().warning(XSD.XSD027);
            SchemaLogger.printP(LOG_WARN, TRANSFORMATION, xData, "List restrictions have not been found!");
        } else {
            simpleType.setContent(restriction);
        }

        list.setItemType(simpleType);
        return list;
    }

    /**
     * Creates XSD simple type union node and restriction node with facets optionally
     * @param facetBuilder      XSD restriction facet builder
     * @param nodeName          source x-definition node name
     * @return  <xs:union memberTypes="...">...</xs:union> if restriction has no facets
     *          <xs:restriction><xs:union memberTypes="...">...</xs:union>...</xs:restriction> otherwise
     */
    private XmlSchemaSimpleTypeContent simpleTypeUnion(final IXsdFacetFactory facetBuilder, final String nodeName) {
        SchemaLogger.printP(LOG_INFO, TRANSFORMATION, xData, "Creating simple type union." );
        final XmlSchemaSimpleTypeUnion union = simpleTypeUnion(nodeName);
        final XmlSchemaSimpleTypeRestriction restriction = simpleTypeRestriction(null, facetBuilder, parameters);
        return wrapUpSimpleTypeContent(restriction, union);
    }

    /**
     * Creates XSD simple type union node
     * @param nodeName      source x-definition node name
     * @return <xs:union memberTypes="...">...</xs:union>
     */
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

    /**
     * Creates top level simple type node which contains union member type reference
     * @param xParser   x-definition parser
     * @param refNames  used reference names
     * @param nodeName  source x-definition node name
     */
    private void simpleTypeUnionTopReference(final XDParser xParser, final Set<String> refNames, final String nodeName) {
        boolean unknownParser = false;
        Pair<QName, IXsdFacetFactory> parserInfo = Xd2XsdParserMapping.findDefaultFacetFactory(xParser.parserName(), adapterCtx);
        if (parserInfo == null) {
            adapterCtx.getReportWriter().warning(XSD.XSD026, xParser.parserName());
            SchemaLogger.printP(LOG_WARN, TRANSFORMATION, xData, "Unsupported simple content parser! Parser=" + xParser.parserName());
            parserInfo = new Pair(Constants.XSD_STRING, new DefaultFacetFactory());
            unknownParser = true;
        }

        final XmlSchemaSimpleTypeRestriction restriction = simpleTypeRestriction(parserInfo.getKey(), parserInfo.getValue(), xParser.getNamedParams().getXDNamedItems());
        if (unknownParser) {
            restriction.setAnnotation(XsdNodeFactory.createAnnotation("Original x-definition parser: " + xParser.parserName(), adapterCtx));
        }

        String refName = XsdNameFactory.createUnionRefTypeName(nodeName, parserInfo.getKey().getLocalPart());
        refName = adapterCtx.getNameFactory().generateTopLevelName(xData, refName);

        if (!refNames.add(refName)) {
            adapterCtx.getReportWriter().warning(XSD.XSD028, refName);
            SchemaLogger.printP(LOG_ERROR, TRANSFORMATION, xData, "Union reference name already exists! RefName=" + refName);
        } else {
            final XmlSchemaSimpleType simpleType = xsdFactory.createEmptySimpleType(true);
            simpleType.setName(refName);
            simpleType.setContent(restriction);
        }
    }

    /**
     * Wrap xs:list or xs:union node to xs:simpleType if input XSD restriction node has any facet
     * @param restriction   XSD restriction node
     * @param content       XSD list or union node
     * @return  {@paramref content} if {@paramref restriction} has no facet
     *          {@paramref restriction} containing <xs:simpleType>{@paramref content}</xs:simpleType>
     */
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

    /**
     * Creates XSD facet nodes based on input {@paramref parameters} by {@paramref facetBuilder}
     * @param qName             XSD restriction base
     * @param facetBuilder      XSD restriction facet builder
     * @param parameters        source x-definition parameters for facets building
     * @return
     */
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

}
