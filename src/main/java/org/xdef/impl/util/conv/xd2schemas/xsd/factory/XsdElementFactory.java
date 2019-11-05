package org.xdef.impl.util.conv.xd2schemas.xsd.factory;

import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.constants.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.impl.XData;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.*;
import org.xdef.model.XMOccurrence;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.security.InvalidParameterException;
import java.util.List;

import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XD2XsdDefinitions.XSD_NAMESPACE_PREFIX_EMPTY;
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XsdLoggerDefs.*;

public class XsdElementFactory {

    private static boolean createAnnotation = false;

    private final XmlSchema schema;

    public XsdElementFactory(XmlSchema schema) {
        this.schema = schema;
    }

    public static void setCreateAnnotation(boolean createAnnotation) {
        XsdElementFactory.createAnnotation = createAnnotation;
    }

    /**
     * Creates xsd element
     * Example: <element>
     */
    public XmlSchemaElement createEmptyElement(final XElement xElement, boolean topLevel) {
        XsdLogger.printG(LOG_TRACE, XSD_ELEM_FACTORY, "Empty element. Top=" + topLevel);
        XmlSchemaElement elem = new XmlSchemaElement(schema, topLevel);

        if (topLevel == false) {
            elem.setMinOccurs(xElement.getOccurence().minOccurs());
            elem.setMaxOccurs((xElement.isUnbounded() || xElement.isMaxUnlimited()) ? Long.MAX_VALUE : xElement.getOccurence().maxOccurs());
        }

        if (xElement._nillable == 'T') {
            elem.setNillable(true);
        }

        return elem;
    }

    /**
     * Creates attribute
     */
    public XmlSchemaAttribute createEmptyAttribute(final XData xData, boolean topLevel) {
        XsdLogger.printG(LOG_TRACE, XSD_ELEM_FACTORY, "Attribute element. Top=" + topLevel);
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
     * Creates complexType element
     * Output: <complexType>
     */
    public XmlSchemaComplexType createEmptyComplexType(boolean topLevel) {
        XsdLogger.printG(LOG_TRACE, XSD_ELEM_FACTORY, "Empty complex-type. Top=" + topLevel);
        return new XmlSchemaComplexType(schema, topLevel);
    }

    /**
     * Creates simpleType element
     * Output: <simpleType>
     */
    public XmlSchemaSimpleType createEmptySimpleType(boolean topLevel) {
        XsdLogger.printG(LOG_TRACE, XSD_ELEM_FACTORY, "Empty simple-type. Top=" + topLevel);
        return new XmlSchemaSimpleType(schema, topLevel);
    }

    public void creatSimpleTypeTop(final XData xData, final String name) {
        XsdLogger.printG(LOG_TRACE, XSD_ELEM_FACTORY, xData, "Simple-type top. Name=" + name);
        final XmlSchemaSimpleType itemType = createEmptySimpleType(true);
        itemType.setName(name);
        itemType.setContent(createSimpleTypeContent(xData, name));
    }

    public XmlSchemaSimpleType creatSimpleType(final XData xData, final String nodeName) {
        XsdLogger.printG(LOG_TRACE, XSD_ELEM_FACTORY, xData, "Simple-type no-top");
        final XmlSchemaSimpleType itemType = createEmptySimpleType(false);
        itemType.setName(XsdNameUtils.newLocalScopeRefTypeName(xData));
        itemType.setContent(createSimpleTypeContent(xData, nodeName));
        return itemType;
    }

    public XmlSchemaSimpleContent createSimpleContentWithExtension(final XData xData) {
        XsdLogger.printG(LOG_TRACE, XSD_ELEM_FACTORY, xData, "Simple-content with extension");

        QName qName;
        if (xData.getRefTypeName() != null) {
            final String refTypeName = XsdNameUtils.newLocalScopeRefTypeName(xData);
            final String nsPrefix = XsdNamespaceUtils.getReferenceNamespacePrefix(refTypeName);
            final String nsUri = schema.getNamespaceContext().getNamespaceURI(nsPrefix);
            qName = new QName(nsUri, refTypeName);
            XsdLogger.printG(LOG_DEBUG, XSD_ELEM_FACTORY, xData, "Simple-content using reference. nsUri=" + nsUri + ", localName=" + refTypeName);
        } else {
            qName = XD2XsdParserMapping.getDefaultSimpleParserQName(xData);
        }

        if (qName == null) {
            final String refParserName = XsdNameUtils.createRefNameFromParser(xData);
            if (refParserName != null) {
                qName = new QName(XSD_NAMESPACE_PREFIX_EMPTY, refParserName);
                XsdLogger.printG(LOG_DEBUG, XSD_ELEM_FACTORY, xData, "Simple-content using parser. Parser=" + refParserName);
            }
        } else {
            XsdLogger.printG(LOG_DEBUG, XSD_ELEM_FACTORY, xData, "Simple-content using simple parser. Parser=" + qName.getLocalPart());
        }

        if (qName != null) {
            final XmlSchemaSimpleContentExtension contentExtension = createSimpleContentExtension(qName);
            final XmlSchemaSimpleContent content = createSimpleContent(contentExtension);
            XsdLogger.printG(LOG_INFO, XSD_ELEM_FACTORY, xData, "Simple-content extending type. QName=" + qName);
            return content;
        }

        return null;
    }

    /**
     * Creates element based on groupType
     * Possible outputs: xs:sequence, xs:choice, xs:all
     * @param groupType
     * @return
     */
    public XmlSchemaGroupParticle createGroupParticle(short groupType, final XMOccurrence occurrence) {
        XsdLogger.printG(LOG_TRACE, XSD_ELEM_FACTORY, "Particle=" + XD2XsdUtils.particleXKindToString(groupType));

        XmlSchemaGroupParticle particle;
        switch (groupType) {
            case XNode.XMSEQUENCE: {
                particle = new XmlSchemaSequence();
                break;
            }
            case XNode.XMMIXED: {
                particle = new XmlSchemaAll();
                break;
            }
            case XNode.XMCHOICE: {
                particle = new XmlSchemaChoice();
                break;
            }
            default: {
                XsdLogger.printG(LOG_ERROR, XSD_ELEM_FACTORY, "Unknown group particle!. Particle=" + XD2XsdUtils.particleXKindToString(groupType));
                throw new InvalidParameterException("Unknown groupType");
            }
        }

        particle.setMinOccurs(occurrence.minOccurs());
        particle.setMaxOccurs((occurrence.isUnbounded() || occurrence.isMaxUnlimited()) ? Long.MAX_VALUE : occurrence.maxOccurs());

        return particle;
    }

    public XmlSchemaComplexType createComplexTypeWithComplexExtension(final String name, final QName qName) {
        final XmlSchemaComplexType complexType = createEmptyComplexType(true);
        final XmlSchemaComplexContent complexContent = createComplexContentWithComplexExtension(qName);
        complexType.setContentModel(complexContent);
        complexType.setName(name);
        return complexType;
    }

    public XmlSchemaComplexContent createComplexContentWithComplexExtension(final QName qName) {
        final XmlSchemaComplexContentExtension complexContentExtension = createComplexContentExtension(qName);
        final XmlSchemaComplexContent complexContent = createComplexContent(complexContentExtension);
        return complexContent;
    }

    public XmlSchemaComplexType createComplextTypeWithSimpleExtension(final String name, final QName qName, boolean topLevel) {
        final XmlSchemaComplexType complexType = createEmptyComplexType(topLevel);
        final XmlSchemaSimpleContentExtension simpleContentExtension = createSimpleContentExtension(qName);
        final XmlSchemaSimpleContent simpleContent = createSimpleContent(simpleContentExtension);
        complexType.setContentModel(simpleContent);
        complexType.setName(name);
        return complexType;
    }

    public XmlSchemaComplexContent createComplexContent(final XmlSchemaContent content) {
        XsdLogger.printG(LOG_TRACE, XSD_ELEM_FACTORY, "Complex content. ContentType=" + content.getClass().getSimpleName());
        final XmlSchemaComplexContent complexContent = new XmlSchemaComplexContent();
        complexContent.setContent(content);
        return complexContent;
    }

    public XmlSchemaSimpleContent createSimpleContent(final XmlSchemaContent content) {
        XsdLogger.printG(LOG_TRACE, XSD_ELEM_FACTORY, "Simple content. ContentType=" + content.getClass().getSimpleName());
        final XmlSchemaSimpleContent simpleContent = new XmlSchemaSimpleContent();
        simpleContent.setContent(content);
        return simpleContent;
    }

    public XmlSchemaComplexContentExtension createComplexContentExtension(final QName baseType) {
        XsdLogger.printG(LOG_TRACE, XSD_ELEM_FACTORY, "Complex content extension. BaseType=" + baseType);
        final XmlSchemaComplexContentExtension contentExtension = new XmlSchemaComplexContentExtension();
        contentExtension.setBaseTypeName(baseType);
        return contentExtension;
    }

    public XmlSchemaSimpleContentExtension createSimpleContentExtension(final QName baseType) {
        XsdLogger.printG(LOG_TRACE, XSD_ELEM_FACTORY, "Simple content extension. BaseType=" + baseType);
        final XmlSchemaSimpleContentExtension contentExtension = new XmlSchemaSimpleContentExtension();
        contentExtension.setBaseTypeName(baseType);
        return contentExtension;
    }

    private XmlSchemaSimpleTypeContent createSimpleTypeContent(final XData xData, final String nodeName) {
        XsdLogger.printG(LOG_TRACE, XSD_ELEM_FACTORY, xData, "Simple-type content");

        final XDValue parseMethod = xData.getParseMethod();
        final XsdSimpleContentFactory simpleContentFactory = new XsdSimpleContentFactory(this, xData);

        if (parseMethod instanceof XDParser) {
            XDParser parser = ((XDParser)parseMethod);
            simpleContentFactory.setParameters(parser.getNamedParams().getXDNamedItems());
            return simpleContentFactory.createSimpleContent(nodeName);
        }

        return simpleContentFactory.createDefaultRestriction(Constants.XSD_STRING);
    }

    public void createSchemaImport(final XmlSchema schema, final String nsUri, final String location) {
        final XmlSchemaImport schemaImport = new XmlSchemaImport(schema);
        schemaImport.setNamespace(nsUri);
        schemaImport.setSchemaLocation(location);
    }

    public void createSchemaInclude(final XmlSchema schema, final String location) {
        final XmlSchemaInclude schemaImport = new XmlSchemaInclude(schema);
        schemaImport.setSchemaLocation(location);
    }

    public static XmlSchemaAnnotation createAnnotation(final String annotationValue) {
        if (createAnnotation) {
            final XmlSchemaAnnotation annotation = new XmlSchemaAnnotation();
            annotation.getItems().add(createAnnotationItem(annotationValue));
            return annotation;
        }

        return null;
    }

    public static XmlSchemaAnnotation createAnnotation(final List<String> annotationValues) {
        if (createAnnotation) {
            final XmlSchemaAnnotation annotation = new XmlSchemaAnnotation();
            for (String value : annotationValues) {
                annotation.getItems().add(createAnnotationItem(value));
            }
            return annotation;
        }

        return null;
    }

    private static XmlSchemaDocumentation createAnnotationItem(final String annotation) {
        if (annotation == null || annotation.isEmpty()) {
            return null;
        }

        XmlSchemaDocumentation annotationItem = new XmlSchemaDocumentation();

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("documentation");
            doc.appendChild(rootElement);
            rootElement.appendChild(doc.createTextNode(annotation));
            annotationItem.setMarkup(rootElement.getChildNodes());
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        return annotationItem;
    }
}
