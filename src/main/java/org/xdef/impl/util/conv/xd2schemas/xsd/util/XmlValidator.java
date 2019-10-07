package org.xdef.impl.util.conv.xd2schemas.xsd.util;

import jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;

public class XmlValidator {

    private Source xmlSource;
    private Source schemaSource;

    public XmlValidator(Source xmlSource, Source schemaSource) {
        this.xmlSource = xmlSource;
        this.schemaSource = schemaSource;
    }

    public Source getXmlSource() {
        return xmlSource;
    }

    public void setXmlSource(Source xmlSource) {
        this.xmlSource = xmlSource;
    }

    public Source getSchemaSource() {
        return schemaSource;
    }

    public void setSchemaSource(Source schemaSource) {
        this.schemaSource = schemaSource;
    }

    public boolean validate(final String baseUri) {
        if (xmlSource == null || schemaSource == null) {
            throw new InternalException("xml == null || schema == null");
        }

        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        try {
            Schema schema = schemaFactory.newSchema(schemaSource);

            Validator validator = schema.newValidator();
            validator.validate(xmlSource);
            return true;
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}
