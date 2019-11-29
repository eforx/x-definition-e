package test.xdutils;

import jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;

/**
 * Simple XML validator used only for testing purposes
 */
public class XmlValidator {

    private Source xmlSource;
    private Source schemaSource;

    public XmlValidator(Source xmlSource, Source schemaSource) {
        this.xmlSource = xmlSource;
        this.schemaSource = schemaSource;
    }

    public boolean validate(final String baseUri, boolean printEx) {
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
            if (printEx) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            if (printEx) {
                e.printStackTrace();
            }
        }

        return false;
    }
}
