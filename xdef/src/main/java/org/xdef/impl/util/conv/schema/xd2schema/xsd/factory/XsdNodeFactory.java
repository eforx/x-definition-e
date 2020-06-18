package org.xdef.impl.util.conv.schema.xd2schema.xsd.factory;

import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.constants.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xdef.XDNamedValue;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.impl.XData;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.schema.util.SchemaLogger;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.Xd2XsdFeature;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.model.UniqueConstraint;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.model.XsdAdapterCtx;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.model.xsd.CXmlSchemaAll;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.model.xsd.CXmlSchemaChoice;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.model.xsd.CXmlSchemaGroupParticle;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.model.xsd.CXmlSchemaSequence;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.util.Xd2XsdParserMapping;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.util.Xd2XsdUtils;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.util.XsdNameUtils;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.util.XsdNamespaceUtils;
import org.xdef.model.XMNode;
import org.xdef.model.XMOccurrence;
import org.xdef.msg.XSD;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SRuntimeException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.security.InvalidParameterException;
import java.util.List;

import static org.xdef.impl.util.conv.schema.util.SchemaLoggerDefs.*;
import static org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.AlgPhase.TRANSFORMATION;
import static org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.Xd2XsdDefinitions.XSD_FACET_MIN_LENGTH;
import static org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.Xd2XsdDefinitions.XSD_NAMESPACE_PREFIX_EMPTY;
import static org.xdef.impl.util.conv.schema.xd2schema.xsd.util.Xd2XsdLoggerDefs.XSD_ELEM_FACTORY;
import static org.xdef.model.XMNode.XMATTRIBUTE;

/**
 * Basic factory for creating XSD nodes
 */
public class XsdNodeFactory {

    /**
     * Output XSD document
     */
    private final XmlSchema schema;

    /**
     * XSD adapter context
     */
    private final XsdAdapterCtx adapterCtx;

    public XsdNodeFactory(XmlSchema schema, XsdAdapterCtx adapterCtx) {
        this.schema = schema;
        this.adapterCtx = adapterCtx;
    }

    /**
     * Creates empty XSD element node with occurrence
     * @param xElem         x-definition element node
     * @param topLevel      flag if x-definition node is placed on top level
     * @return <xs:element/>
     */
    public XmlSchemaElement createEmptyElement(final XElement xElem, boolean topLevel) {
        SchemaLogger.printG(LOG_TRACE, XSD_ELEM_FACTORY, "Empty element. Top=" + topLevel);
        XmlSchemaElement elem = new XmlSchemaElement(schema, topLevel);

        if (topLevel == false) {
            elem.setMinOccurs(xElem.getOccurence().minOccurs());
            elem.setMaxOccurs((xElem.isUnbounded() || xElem.isMaxUnlimited()) ? Long.MAX_VALUE : xElem.getOccurence().maxOccurs());
        }

        if (xElem._nillable == 'T') {
            elem.setNillable(true);
        }

        return elem;
    }

    /**
     * Creates empty XSD attribute node with use
     * @param xData         x-definition attribute node
     * @param topLevel      flag if x-definition node is placed on top level
     * @return <xs:attribute/>
     */
    public XmlSchemaAttribute createEmptyAttribute(final XData xData, boolean topLevel) {
        SchemaLogger.printG(LOG_TRACE, XSD_ELEM_FACTORY, "Attribute element. Top=" + topLevel);
        XmlSchemaAttribute attr = new XmlSchemaAttribute(schema, topLevel);

        if (topLevel == false) {
            if (xData.isOptional() || xData.getOccurence().isOptional()) {
                attr.setUse(XmlSchemaUse.OPTIONAL);
            } else if (xData.isRequired() || xData.getOccurence().isRequired()) {
                attr.setUse(XmlSchemaUse.REQUIRED);
            }
        }

        return attr;
    }

    /**
     * Creates empty XSD complex-type node
     * @param topLevel          flag if x-definition node is placed on top level
     * @return <xs:complexType/>
     */
    public XmlSchemaComplexType createEmptyComplexType(boolean topLevel) {
        SchemaLogger.printG(LOG_TRACE, XSD_ELEM_FACTORY, "Empty complex-type. Top=" + topLevel);
        return new XmlSchemaComplexType(schema, topLevel);
    }

    /**
     * Creates empty XSD simple-type node
     * @param topLevel          flag if x-definition node is placed on top level
     * @return <xs:simpleType/>
     */
    public XmlSchemaSimpleType createEmptySimpleType(boolean topLevel) {
        SchemaLogger.printG(LOG_TRACE, XSD_ELEM_FACTORY, "Empty simple-type. Top=" + topLevel);
        return new XmlSchemaSimpleType(schema, topLevel);
    }

    /**
     * Creates XSD any node with occurrence
     * @param xElem             x-definition element node
     * @return <xs:any/>
     */
    public XmlSchemaAny createAny(final XElement xElem) {
        SchemaLogger.printG(LOG_TRACE, XSD_ELEM_FACTORY, "Any");
        final XmlSchemaAny any = new XmlSchemaAny();
        any.setMinOccurs(xElem.getOccurence().minOccurs());
        any.setMaxOccurs((xElem.isUnbounded() || xElem.isMaxUnlimited()) ? Long.MAX_VALUE : xElem.getOccurence().maxOccurs());
        return any;
    }

    /**
     * Creates XSD simple-type node on top level of XSD document
     * @param xData             x-definition attribute/text node
     * @param refTypeName       reference type name
     */
    public void createSimpleTypeTop(final XData xData, final String refTypeName) {
        SchemaLogger.printG(LOG_TRACE, XSD_ELEM_FACTORY, xData, "Simple-type top. Name=" + refTypeName);
        final XmlSchemaSimpleType itemType = createEmptySimpleType(true);
        itemType.setName(refTypeName);
        itemType.setContent(createSimpleTypeContent(xData, refTypeName));
    }

    /**
     * Creates XSD simple-type node
     * @param xData             x-definition attribute/text node
     * @param nodeName          simple-type name
     * @return <xs:simpleType>...</xs:simpleType>
     */
    public XmlSchemaSimpleType createSimpleType(final XData xData, final String nodeName) {
        SchemaLogger.printG(LOG_TRACE, XSD_ELEM_FACTORY, xData, "Simple-type. NodeName=" + nodeName);
        final XmlSchemaSimpleType itemType = createEmptySimpleType(false);
        itemType.setContent(createSimpleTypeContent(xData, nodeName));
        itemType.setName(XsdNameFactory.createLocalSimpleTypeName(xData));
        return itemType;
    }

    /**
     * Creates XSD simple content node
     * @param xDataText         x-definition text node
     * @return  if reference and parser are unknown, then null
     *          else <xs:simpleContent><xs:extension base="...">...</xs:extension></xs:simpleContent>
     */
    public XmlSchemaSimpleContent createTextBasedSimpleContent(final XData xDataText) {
        SchemaLogger.printG(LOG_TRACE, XSD_ELEM_FACTORY, xDataText, "Simple-content with extension");

        QName qName = null;

        final UniqueConstraint uniqueConstraint = adapterCtx.findUniqueConst(xDataText);
        if (uniqueConstraint != null) {
            SchemaLogger.printP(LOG_INFO, TRANSFORMATION, xDataText, "Simple-content is using unique set. UniqueSet=" + uniqueConstraint.getName());
            final UniqueConstraint.Type type = XsdNameUtils.getUniqueSetVarType(xDataText.getValueTypeName());
            if (UniqueConstraint.isStringConstraint(type)) {
                qName = Constants.XSD_STRING;
            }
        }

        boolean extension = true;

        if (qName == null) {
            if (xDataText.getRefTypeName() != null) {
                final String refTypeName = XsdNameFactory.createLocalSimpleTypeName(xDataText);
                final String nsPrefix = XsdNamespaceUtils.getReferenceNamespacePrefix(refTypeName);
                final String nsUri = schema.getNamespaceContext().getNamespaceURI(nsPrefix);
                qName = new QName(nsUri, refTypeName);
                SchemaLogger.printG(LOG_DEBUG, XSD_ELEM_FACTORY, xDataText, "Simple-content using reference. nsUri=" + nsUri + ", localName=" + refTypeName);
            } else {
                qName = Xd2XsdParserMapping.getDefaultParserQName(xDataText, adapterCtx, false);
                final XDValue parseMethod = xDataText.getParseMethod();
                if (parseMethod instanceof XDParser) {
                    // Restriction facets
                    final XDNamedValue[] items = ((XDParser) parseMethod).getNamedParams().getXDNamedItems();
                    // Check if restriction contains only min-length equals 1 and string is optional => then use xs:extension instead of xs:restriction
                    if (items.length == 0 ||
                            (items.length == 1 && Constants.XSD_STRING.equals(qName) &&
                            XSD_FACET_MIN_LENGTH.equals(items[0].getName()) && items[0].getValue().intValue() == 1 && xDataText.isOptional())) {
                        extension = true;
                    } else {
                        extension = false;
                    }
                }
            }

            if (qName == null) {
                final String refParserName = XsdNameUtils.createRefNameFromParser(xDataText, adapterCtx);
                if (refParserName != null) {
                    qName = new QName(XSD_NAMESPACE_PREFIX_EMPTY, refParserName);
                    SchemaLogger.printG(LOG_DEBUG, XSD_ELEM_FACTORY, xDataText, "Simple-content using parser. Parser=" + refParserName);
                }
            } else {
                SchemaLogger.printG(LOG_DEBUG, XSD_ELEM_FACTORY, xDataText, "Simple-content using simple parser. Parser=" + qName.getLocalPart());
            }
        }

        if (qName != null) {
            XmlSchemaContent schemaContent;
            if (extension) {
                schemaContent = createEmptySimpleContentExtension(qName);
                SchemaLogger.printG(LOG_INFO, XSD_ELEM_FACTORY, xDataText, "Simple-content extension type. QName=" + qName);
            } else {
                schemaContent = createEmptySimpleContentRestriction(qName);
                SchemaLogger.printG(LOG_INFO, XSD_ELEM_FACTORY, xDataText, "Simple-content restriction type. QName=" + qName);

                // Copy facets
                final XmlSchemaSimpleTypeContent simpleTypeContent = createSimpleTypeContent(xDataText, null);
                if (simpleTypeContent instanceof XmlSchemaSimpleTypeRestriction) {
                    ((XmlSchemaSimpleContentRestriction)schemaContent).getFacets().addAll(((XmlSchemaSimpleTypeRestriction)simpleTypeContent).getFacets());
                }
            }

            final XmlSchemaSimpleContent content = createSimpleContent(schemaContent);
            if (uniqueConstraint != null) {
                schemaContent.setAnnotation(XsdNodeFactory.createAnnotation("Original part of uniqueSet: " + uniqueConstraint.getPath() + " (" + xDataText.getValueTypeName() + ")", adapterCtx));
            }
            return content;
        }

        return null;
    }

    /**
     * Creates XSD group particle node with occurrence
     * @param xNode             x-definition group node
     * @return based on {@paramref xNode}
     *          <xs:sequence/>
     *          <xs:choice/>
     *          <xs:all/>
     */
    public CXmlSchemaGroupParticle createGroupParticle(final XMNode xNode) {
        final short groupType = xNode.getKind();
        final XMOccurrence occurrence = xNode.getOccurence();

        SchemaLogger.printG(LOG_TRACE, XSD_ELEM_FACTORY, "Particle=" + Xd2XsdUtils.particleXKindToString(groupType));

        CXmlSchemaGroupParticle particle;
        switch (groupType) {
            case XNode.XMSEQUENCE: {
                particle = new CXmlSchemaSequence(new XmlSchemaSequence());
                break;
            }
            case XNode.XMMIXED: {
                particle = new CXmlSchemaAll(new XmlSchemaAll());
                break;
            }
            case XNode.XMCHOICE: {
                particle = new CXmlSchemaChoice(new XmlSchemaChoice());
                break;
            }
            default: {
                SchemaLogger.printG(LOG_ERROR, XSD_ELEM_FACTORY, "Unknown group particle!. Particle=" + Xd2XsdUtils.particleXKindToString(groupType));
                throw new SRuntimeException(XSD.XSD044, Xd2XsdUtils.particleXKindToString(groupType));
            }
        }

        particle.xsd().setMinOccurs(occurrence.minOccurs());
        particle.xsd().setMaxOccurs((occurrence.isUnbounded() || occurrence.isMaxUnlimited()) ? Long.MAX_VALUE : occurrence.maxOccurs());

        return particle;
    }

    /**
     * Creates empty XSD group node
     * @param name          group name
     * @return <xs:group name="@{paramref name}"/>
     */
    public XmlSchemaGroup createEmptyGroup(final String name) {
        SchemaLogger.printG(LOG_TRACE, XSD_ELEM_FACTORY, "Group. Name=" + name);
        final XmlSchemaGroup group = new XmlSchemaGroup(schema);
        group.setName(name);
        return group;
    }

    /**
     * Creates XSD group reference node
     * @param qName         reference QName
     * @return <xs:group ref="@{paramref qName}"/>
     */
    public XmlSchemaGroupRef createGroupRef(final QName qName) {
        SchemaLogger.printG(LOG_TRACE, XSD_ELEM_FACTORY, "Group reference. QName=" + qName);
        final XmlSchemaGroupRef groupRef = new XmlSchemaGroupRef();
        groupRef.setRefName(qName);
        return groupRef;
    }

    /**
     * Creates XSD complex type node with complex extension on top level of XSD document
     * @param complexTypeName       complex type name
     * @param extQName              complex extension QName
     * @return  <xs:complexType name="@{paramref complexTypeName}">
     *              <xs:complexContent>
     *                      <xs:extension base="@{paramref extQName}"></xs:extension>
     *              </xs:complexContent>
     *          </xs:complexType>
     */
    public XmlSchemaComplexType createComplexTypeWithComplexExtensionTop(final String complexTypeName, final QName extQName) {
        final XmlSchemaComplexType complexType = createEmptyComplexType(true);
        final XmlSchemaComplexContent complexContent = createComplexContentWithComplexExtension(extQName);
        complexType.setContentModel(complexContent);
        complexType.setName(complexTypeName);
        return complexType;
    }

    /**
     * Creates XSD complex content node with extension
     * @param qName             complex extension QName
     * @return  <xs:complexContent>
     *              <xs:extension base="@{paramref qName}"></xs:extension>
     *          </xs:complexContent>
     */
    private XmlSchemaComplexContent createComplexContentWithComplexExtension(final QName qName) {
        final XmlSchemaComplexContentExtension complexContentExtension = createEmptyComplexContentExtension(qName);
        final XmlSchemaComplexContent complexContent = createComplexContent(complexContentExtension);
        return complexContent;
    }

    /**
     * Creates XSD complex type node with simple content on top level of XSD document
     * @param complexTypeName       complex type name
     * @param schemaContent         simple content
     * @return  <xs:complexType name="@{paramref simpleTypeName}">
     *              <xs:simpleContent>
     *                      <xs:extension base="@{paramref extQName}"></xs:extension>
     *              </xs:simpleContent>
     *          </xs:complexType>
     */
    public XmlSchemaComplexType createComplexTypeWithSimpleContentTop(final String complexTypeName, final XmlSchemaContent schemaContent) {
        final XmlSchemaComplexType complexType = createEmptyComplexType(true);
        final XmlSchemaSimpleContent simpleContent = createSimpleContent(schemaContent);
        complexType.setContentModel(simpleContent);
        complexType.setName(complexTypeName);
        return complexType;
    }

    /**
     * Creates XSD complex type node with simple extension on top level of XSD document
     * @param complexTypeName       complex type name
     * @param extQName              simple extension QName
     * @return  <xs:complexType name="@{paramref simpleTypeName}">
     *              <xs:simpleContent>
     *                      <xs:extension base="@{paramref extQName}"></xs:extension>
     *              </xs:simpleContent>
     *          </xs:complexType>
     */
    public XmlSchemaComplexType createComplexTypeWithSimpleExtensionTop(final String complexTypeName, final QName extQName) {
        final XmlSchemaComplexType complexType = createEmptyComplexType(true);
        final XmlSchemaSimpleContentExtension simpleContentExtension = createEmptySimpleContentExtension(extQName);
        final XmlSchemaSimpleContent simpleContent = createSimpleContent(simpleContentExtension);
        complexType.setContentModel(simpleContent);
        complexType.setName(complexTypeName);
        return complexType;
    }

    /**
     * Creates XSD complex content node
     * @param content       schema content
     * @return  <xs:complexContent>@{paramref content}</xs:complexContent>
     */
    public XmlSchemaComplexContent createComplexContent(final XmlSchemaContent content) {
        SchemaLogger.printG(LOG_TRACE, XSD_ELEM_FACTORY, "Complex content. ContentType=" + content.getClass().getSimpleName());
        final XmlSchemaComplexContent complexContent = new XmlSchemaComplexContent();
        complexContent.setContent(content);
        return complexContent;
    }

    /**
     * Creates XSD simple content node
     * @param content       schema content
     * @return  <xs:simpleContent>@{paramref content}</xs:simpleContent>
     */
    public XmlSchemaSimpleContent createSimpleContent(final XmlSchemaContent content) {
        SchemaLogger.printG(LOG_TRACE, XSD_ELEM_FACTORY, "Simple content. ContentType=" + content.getClass().getSimpleName());
        final XmlSchemaSimpleContent simpleContent = new XmlSchemaSimpleContent();
        simpleContent.setContent(content);
        return simpleContent;
    }

    /**
     * Creates empty XSD complex content extension node
     * @param baseType      context extension base
     * @return <xs:extension base="@{paramref baseType}"/>
     */
    public XmlSchemaComplexContentExtension createEmptyComplexContentExtension(final QName baseType) {
        SchemaLogger.printG(LOG_TRACE, XSD_ELEM_FACTORY, "Complex content extension. BaseType=" + baseType);
        final XmlSchemaComplexContentExtension contentExtension = new XmlSchemaComplexContentExtension();
        contentExtension.setBaseTypeName(baseType);
        return contentExtension;
    }

    /**
     * Creates empty XSD simple content extension node
     * @param baseType      simple extension base
     * @return <xs:extension base="@{paramref baseType}"/>
     */
    public XmlSchemaSimpleContentExtension createEmptySimpleContentExtension(final QName baseType) {
        SchemaLogger.printG(LOG_TRACE, XSD_ELEM_FACTORY, "Simple content extension. BaseType=" + baseType);
        final XmlSchemaSimpleContentExtension contentExtension = new XmlSchemaSimpleContentExtension();
        contentExtension.setBaseTypeName(baseType);
        return contentExtension;
    }

    /**
     * Creates empty XSD simple content restriction node
     * @param baseType      simple restriction base
     * @return <xs:restriction base="@{paramref baseType}"/>
     */
    public XmlSchemaSimpleContentRestriction createEmptySimpleContentRestriction(final QName baseType) {
        SchemaLogger.printG(LOG_TRACE, XSD_ELEM_FACTORY, "Simple content restriction. BaseType=" + baseType);
        final XmlSchemaSimpleContentRestriction contentExtension = new XmlSchemaSimpleContentRestriction();
        contentExtension.setBaseTypeName(baseType);
        return contentExtension;
    }

    /**
     * Creates XSD simple content node
     * @param xData             x-definition attribute/text node
     * @param nodeName          node name (required for <xs:union/> node)
     * @return  if x-definition node has known parser, then based on parser
     *              <xs:restriction base="...">...</xs:restriction>
     *              <xs:list itemType="...">...</xs:list>
     *              <xs:union memberTypes="...">...</xs:union>
     *          else <xs:restriction base="xs:string"/>
     */
    private XmlSchemaSimpleTypeContent createSimpleTypeContent(final XData xData, final String nodeName) {
        SchemaLogger.printG(LOG_TRACE, XSD_ELEM_FACTORY, xData, "Simple-type content");

        final XDValue parseMethod = xData.getParseMethod();
        final XsdSimpleContentFactory simpleContentFactory = new XsdSimpleContentFactory(this, adapterCtx, xData);

        if (parseMethod instanceof XDParser) {
            XDParser parser = ((XDParser)parseMethod);
            simpleContentFactory.setParameters(parser.getNamedParams().getXDNamedItems());
            return simpleContentFactory.createSimpleContent(nodeName, xData.getKind() == XMATTRIBUTE);
        }

        return simpleContentFactory.createDefaultRestriction();
    }

    /**
     * Creates XSD document import node
     * @param schema        output XSD document
     * @param nsUri         import namespace URI
     * @param location      import schema location
     */
    public void createSchemaImport(final XmlSchema schema, final String nsUri, final String location) {
        final XmlSchemaImport schemaImport = new XmlSchemaImport(schema);
        schemaImport.setNamespace(nsUri);
        schemaImport.setSchemaLocation(location);
    }

    /**
     * Creates XSD document include node
     * @param schema        output XSD document
     * @param location      include schema location
     */
    public void createSchemaInclude(final XmlSchema schema, final String location) {
        final XmlSchemaInclude schemaImport = new XmlSchemaInclude(schema);
        schemaImport.setSchemaLocation(location);
    }

    /**
     * Creates XSD annotation node with single documentation node
     * @param annotationValue   annotation value
     * @param adapterCtx        XSD adapter context
     * @return <xs:annotation><xs:documentation>@{paramref annotationValue}</xs:documentation></xs:annotation>
     */
    public static XmlSchemaAnnotation createAnnotation(final String annotationValue, final XsdAdapterCtx adapterCtx) {
        if (adapterCtx.hasEnableFeature(Xd2XsdFeature.XSD_ANNOTATION)) {
            final XmlSchemaAnnotation annotation = new XmlSchemaAnnotation();
            annotation.getItems().add(createAnnotationItem(annotationValue, adapterCtx));
            return annotation;
        }

        return null;
    }

    /**
     * Creates XSD annotation node with multiple documentation nodes
     * @param annotationValues  list of annotation values
     * @param adapterCtx        XSD adapter context
     * @return  <xs:annotation>
     *              <xs:documentation>@{paramref annotationValue[0]}</xs:documentation>
     *              <xs:documentation>@{paramref annotationValue[1]}</xs:documentation>
     *              ...
     *              <xs:documentation>@{paramref annotationValue[n-1]}</xs:documentation>
     *          </xs:annotation>
     */
    public static XmlSchemaAnnotation createAnnotation(final List<String> annotationValues, final XsdAdapterCtx adapterCtx) {
        if (adapterCtx.hasEnableFeature(Xd2XsdFeature.XSD_ANNOTATION)) {
            final XmlSchemaAnnotation annotation = new XmlSchemaAnnotation();
            for (String value : annotationValues) {
                annotation.getItems().add(createAnnotationItem(value, adapterCtx));
            }
            return annotation;
        }

        return null;
    }

    /**
     * Creates XSD documentation node
     * @param docValue      documentation value
     * @return <xs:documentation>@{paramref docValue}</xs:documentation>
     */
    private static XmlSchemaDocumentation createAnnotationItem(final String docValue, final XsdAdapterCtx adapterCtx) {
        if (docValue == null || docValue.isEmpty()) {
            return null;
        }

        final XmlSchemaDocumentation annotationItem = new XmlSchemaDocumentation();

        try {
            final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            final Document doc = docBuilder.newDocument();
            final Element rootElement = doc.createElement("documentation");
            doc.appendChild(rootElement);
            rootElement.appendChild(doc.createTextNode(docValue));
            annotationItem.setMarkup(rootElement.getChildNodes());
        } catch (ParserConfigurationException e) {
            adapterCtx.getReportWriter().warning(XSD.XSD035, e.getMessage());
            SchemaLogger.printP(LOG_WARN, TRANSFORMATION, "Error occurs while creating XSD documentation node: " + e.getMessage());
        }

        return annotationItem;
    }
}
