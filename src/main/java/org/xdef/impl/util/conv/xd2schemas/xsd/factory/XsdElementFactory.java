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
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XD2XsdUtils;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdNameUtils;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdNamespaceUtils;
import org.xdef.model.XMOccurrence;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.security.InvalidParameterException;
import java.util.List;

import static org.xdef.impl.util.conv.xd2schemas.xsd.XD2XsdDefinitions.XSD_NAMESPACE_PREFIX_EMPTY;
import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.*;

public class XsdElementFactory {

    private final int logLevel;
    private final XmlSchema schema;

    public XsdElementFactory(int logLevel, XmlSchema schema) {
        this.logLevel = logLevel;
        this.schema = schema;
    }

    /**
     * Create named xsd element
     * Example: <element name="elem_name">
     */
    public XmlSchemaElement createEmptyElement(final XElement xElement) {
        if (XsdLogger.isTrace(logLevel)) {
            XsdLogger.printC(TRACE, XSD_ELEM_FACTORY, "Empty element");
        }
        XmlSchemaElement elem = new XmlSchemaElement(schema, false);
        elem.setMinOccurs(xElement.getOccurence().minOccurs());
        elem.setMaxOccurs((xElement.isUnbounded() || xElement.isMaxUnlimited()) ? Long.MAX_VALUE : xElement.getOccurence().maxOccurs());
        return elem;
    }

    /**
     * Create complexType element
     * Output: <complexType>
     */
    public XmlSchemaComplexType createEmptyComplexType() {
        if (XsdLogger.isTrace(logLevel)) {
            XsdLogger.printC(TRACE, XSD_ELEM_FACTORY, "Empty complex-type");
        }
        return new XmlSchemaComplexType(schema, false);
    }

    /**
     * Create simpleType element
     * Output: <simpleType>
     */
    public XmlSchemaSimpleType createEmptySimpleType(boolean topLevel) {
        if (XsdLogger.isTrace(logLevel)) {
            XsdLogger.printC(TRACE, XSD_ELEM_FACTORY, "Empty simple-type");
        }
        return new XmlSchemaSimpleType(schema, topLevel);
    }

    public XmlSchemaSimpleType creatSimpleTypeTop(final XData xData, final String name) {
        if (XsdLogger.isTrace(logLevel)) {
            XsdLogger.printC(TRACE, XSD_ELEM_FACTORY, xData, "Reference simple-type");
        }

        XmlSchemaSimpleType itemType = createEmptySimpleType(true);
        itemType.setName(name);
        itemType.setContent(createSimpleTypeRestriction(xData));
        return itemType;
    }

    public XmlSchemaSimpleType creatSimpleType(final XData xData) {
        if (XsdLogger.isTrace(logLevel)) {
            XsdLogger.printC(TRACE, XSD_ELEM_FACTORY, xData, "Simple-type");
        }

        XmlSchemaSimpleType itemType = createEmptySimpleType(false);
        itemType.setName(xData.getRefTypeName());
        itemType.setContent(createSimpleTypeRestriction(xData));
        return itemType;
    }

    public XmlSchemaSimpleContent createSimpleContent(final XData xd) {
        if (XsdLogger.isTrace(logLevel)) {
            XsdLogger.printC(TRACE, XSD_ELEM_FACTORY, xd, "Simple-content");
        }

        XmlSchemaSimpleContent content = new XmlSchemaSimpleContent();

        QName qName;
        if (xd.getRefTypeName() != null) {
            final String nsPrefix = XsdNamespaceUtils.getReferenceNamespacePrefix(xd.getRefTypeName());
            final String nsUri = schema.getNamespaceContext().getNamespaceURI(nsPrefix);
            qName = new QName(nsUri, xd.getRefTypeName());
        } else {
            qName = XD2XsdUtils.getDefaultSimpleParserQName(xd);
        }

        if (qName == null) {
            final String refParserName = XD2XsdUtils.createNameFromParser(xd);
            if (refParserName != null) {
                qName = new QName(XSD_NAMESPACE_PREFIX_EMPTY, refParserName);
            }
        }

        if (qName != null) {
            XmlSchemaSimpleContentExtension contentExtension = new XmlSchemaSimpleContentExtension();
            contentExtension.setBaseTypeName(qName);
            content.setContent(contentExtension);
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
        if (XsdLogger.isTrace(logLevel)) {
            XsdLogger.printC(TRACE, XSD_ELEM_FACTORY, "Particle=" + XD2XsdUtils.particleXKindToString(groupType));
        }

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
                throw new InvalidParameterException("Unknown groupType");
            }
        }

        particle.setMinOccurs(occurrence.minOccurs());
        particle.setMaxOccurs((occurrence.isUnbounded() || occurrence.isMaxUnlimited()) ? Long.MAX_VALUE : occurrence.maxOccurs());

        return particle;
    }

    public void createSchemaImport(final XmlSchema schema, final String nsUri, final String location) {
        XmlSchemaImport schemaImport = new XmlSchemaImport(schema);
        schemaImport.setNamespace(nsUri);
        schemaImport.setSchemaLocation(location);
    }

    public void createSchemaInclude(final XmlSchema schema, final String location) {
        XmlSchemaInclude schemaImport = new XmlSchemaInclude(schema);
        schemaImport.setSchemaLocation(location);
    }

    public static XmlSchemaAnnotation createAnnotation(final String annotationValue) {
        XmlSchemaAnnotation annotation = new XmlSchemaAnnotation();
        annotation.getItems().add(createAnnotationItem(annotationValue));
        return annotation;
    }

    public static XmlSchemaAnnotation createAnnotation(final List<String> annotationValues) {
        XmlSchemaAnnotation annotation = new XmlSchemaAnnotation();
        for (String value : annotationValues) {
            annotation.getItems().add(createAnnotationItem(value));
        }
        return annotation;
    }

    private XmlSchemaSimpleTypeRestriction createSimpleTypeRestriction(final XData xData) {
        if (XsdLogger.isTrace(logLevel)) {
            XsdLogger.printC(TRACE, XSD_ELEM_FACTORY, xData, "Simple-type restrictions");
        }

        XDValue parseMethod = xData.getParseMethod();
        XsdRestrictionFactory restrictionBuilder = new XsdRestrictionFactory(xData, logLevel);

        if (parseMethod instanceof XDParser) {
            XDParser parser = ((XDParser)parseMethod);
            restrictionBuilder.setParameters(parser.getNamedParams().getXDNamedItems());
            return restrictionBuilder.createRestriction();
        }

        return restrictionBuilder.buildDefaultRestriction(Constants.XSD_STRING);
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
