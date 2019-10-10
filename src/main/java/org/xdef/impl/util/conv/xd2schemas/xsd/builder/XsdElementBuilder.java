package org.xdef.impl.util.conv.xd2schemas.xsd.builder;

import com.sun.org.apache.xerces.internal.dom.TextImpl;
import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.constants.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
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

    public XmlSchemaSimpleType creatSimpleType(final XData xData, boolean topLevel) {

        XDValue parseMethod = xData.getParseMethod();
        XmlSchemaSimpleTypeRestriction restriction;
        XsdRestrictionBuilder restrictionBuilder = new XsdRestrictionBuilder(xData);

        if (parseMethod instanceof XDParser) {
            XDParser parser = ((XDParser)parseMethod);
            restrictionBuilder.setParameters(parser.getNamedParams().getXDNamedItems());
            restriction = restrictionBuilder.buildRestriction();
        } else {
            restriction = restrictionBuilder.buildDefaultRestriction(Constants.XSD_STRING);
        }

        XmlSchemaSimpleType itemType = new XmlSchemaSimpleType(schema, topLevel);
        itemType.setName(xData.getRefTypeName());
        itemType.setContent(restriction);
        return itemType;
    }

    public XmlSchemaAttribute createAttribute(final String name, final XMData xmData) {
        XmlSchemaAttribute attr = new XmlSchemaAttribute(schema, false);
        final String importNamespace = xmData.getNSUri();
        final String nodeName = xmData.getName();
        if (importNamespace != null && XD2XsdUtils.isExternalRef(nodeName, importNamespace, schema)) {
            attr.getRef().setTargetQName(new QName(importNamespace, nodeName));
        } else {
            attr.setName(name);

            // TODO: Handling of reference namespaces?
            if (xmData.getRefTypeName() != null) {
                attr.setSchemaTypeName(new QName(XSD_NAMESPACE_PREFIX_EMPTY, xmData.getRefTypeName()));
            } else if (XD2XsdUtils.hasDefaultSimpleParser((XData)xmData)) {
                attr.setSchemaTypeName(XD2XsdUtils.getDefaultQName(xmData.getValueTypeName()));
            } else {
                attr.setSchemaType(creatSimpleType((XData)xmData, false));
            }

            String newName = XD2XsdUtils.getResolvedName(schema, name);
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

    public static XmlSchemaSimpleContent createSimpleContent(final XMData xmData) {
        XmlSchemaSimpleContent content = new XmlSchemaSimpleContent();
        XmlSchemaSimpleContentExtension contentExtension = new XmlSchemaSimpleContentExtension();

        final String parserName = xmData.getParserName();
        XDValue parseMethod = xmData.getParseMethod();

        if (!XD2XsdUtils.hasDefaultSimpleParser((XData)xmData)) {
            System.out.println("Element requires advanced parser");
        }

        // TODO: Has to be instance of XDParser?
        if (parseMethod instanceof XDParser) {
            contentExtension.setBaseTypeName(XD2XsdUtils.getDefaultQName(parserName));
        }

        content.setContent(contentExtension);
        return content;
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
