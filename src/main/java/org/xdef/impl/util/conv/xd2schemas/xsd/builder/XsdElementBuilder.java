package org.xdef.impl.util.conv.xd2schemas.xsd.builder;

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
import org.xdef.model.XMData;
import org.xdef.model.XMOccurrence;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.security.InvalidParameterException;
import java.util.List;

import static org.xdef.impl.util.conv.xd2schemas.xsd.XD2XsdDefinitions.XD_PARSER_EQ;
import static org.xdef.impl.util.conv.xd2schemas.xsd.XD2XsdDefinitions.XSD_NAMESPACE_PREFIX_EMPTY;

public class XsdElementBuilder {

    private final XmlSchema schema;

    public XsdElementBuilder(XmlSchema schema) {
        this.schema = schema;
    }

    /**
     * Create named xsd element
     * Example: <element name="elem_name">
     */
    public XmlSchemaElement createEmptyElement(final XElement xElement) {
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
        return new XmlSchemaComplexType(schema, false);
    }

    /**
     * Create simpleType element
     * Output: <simpleType>
     */
    public XmlSchemaSimpleType createEmptySimpleType(boolean topLevel) {
        return new XmlSchemaSimpleType(schema, topLevel);
    }

    private XmlSchemaSimpleTypeRestriction createSimpleTypeRestriction(final XData xData) {
        XDValue parseMethod = xData.getParseMethod();
        XsdRestrictionBuilder restrictionBuilder = new XsdRestrictionBuilder(xData);

        if (parseMethod instanceof XDParser) {
            XDParser parser = ((XDParser)parseMethod);
            restrictionBuilder.setParameters(parser.getNamedParams().getXDNamedItems());
            return restrictionBuilder.buildRestriction();
        }

        return restrictionBuilder.buildDefaultRestriction(Constants.XSD_STRING);
    }

    public XmlSchemaSimpleType creatSimpleTypeTop(final XData xData, final String name) {
        XmlSchemaSimpleType itemType = createEmptySimpleType(true);
        itemType.setName(name);
        itemType.setContent(createSimpleTypeRestriction(xData));
        return itemType;
    }

    public XmlSchemaSimpleType creatSimpleType(final XData xData) {
        XmlSchemaSimpleType itemType = createEmptySimpleType(false);
        itemType.setName(xData.getRefTypeName());
        itemType.setContent(createSimpleTypeRestriction(xData));
        return itemType;
    }

    public XmlSchemaAttribute createAttribute(final String name, final XMData xmData) {
        XmlSchemaAttribute attr = new XmlSchemaAttribute(schema, false);
        final String importNamespace = xmData.getNSUri();
        final String nodeName = xmData.getName();
        if (importNamespace != null && XD2XsdUtils.isRefInDifferentNamespace(nodeName, importNamespace, schema)) {
            attr.getRef().setTargetQName(new QName(importNamespace, nodeName));
        } else {
            attr.setName(name);

            // TODO: Handling of reference namespaces?
            if (xmData.getRefTypeName() != null) {
                attr.setSchemaTypeName(new QName(XSD_NAMESPACE_PREFIX_EMPTY, xmData.getRefTypeName()));
            } else if (XD2XsdUtils.getDefaultSimpleParserQName((XData)xmData) != null) {
                attr.setSchemaTypeName(XD2XsdUtils.getDefaultQName(xmData.getValueTypeName()));
            } else if (XD_PARSER_EQ.equals(xmData.getParserName())) {
                // TODO: Where to get fixed value
                //attr.setFixedValue("1.0");
                // TODO: Possible to use non-default xsd types?
                attr.setSchemaTypeName(XD2XsdUtils.getDefaultQName(xmData.getValueTypeName()));
            } else {
                attr.setSchemaType(creatSimpleType((XData)xmData));
            }

            String newName = XD2XsdUtils.resolveName(schema, name);
            if (!name.equals(newName)) {
                attr.setName(newName);
            } else if (XmlSchemaForm.QUALIFIED.equals(schema.getAttributeFormDefault()) && XD2XsdUtils.isUnqualifiedName(schema, name)) {
                attr.setForm(XmlSchemaForm.UNQUALIFIED);
            }
        }

        if (xmData.isOptional() || xmData.getOccurence().isOptional()) {
            attr.setUse(XmlSchemaUse.OPTIONAL);
        } else if (xmData.isRequired() || xmData.getOccurence().isRequired()) {
            attr.setUse(XmlSchemaUse.REQUIRED);
        }

        return attr;
    }

    public XmlSchemaSimpleContent createSimpleContent(final XData xd) {
        XmlSchemaSimpleContent content = new XmlSchemaSimpleContent();

        QName qName;
        // TODO: Handling of reference namespaces?
        if (xd.getRefTypeName() != null) {
            qName = new QName(XSD_NAMESPACE_PREFIX_EMPTY, xd.getRefTypeName());
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
    public static XmlSchemaGroupParticle createGroupParticle(short groupType, final XMOccurrence occurrence) {
        XmlSchemaGroupParticle particle = null;
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
