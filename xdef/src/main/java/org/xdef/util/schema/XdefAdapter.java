package org.xdef.util.schema;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.xdef.XDBuilder;
import org.xdef.XDConstants;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.impl.util.conv.schema.util.SchemaLogger;
import org.xdef.impl.util.conv.schema.util.SchemaLoggerDefs;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.XdPool2XsdAdapter;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.Xd2XsdFeature;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.util.Xd2XsdUtils;
import org.xdef.sys.SUtils;

import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class XdefAdapter {

    private final XdefAdapterConfig config;

    public XdefAdapter(XdefAdapterConfig config) {
        this.config = config;
    }

    private XdPool2XsdAdapter createXdPoolAdapter() {
        final XdPool2XsdAdapter adapter = new XdPool2XsdAdapter();
        final Set<Xd2XsdFeature> features = config.useDefaultFeatures() ? Xd2XsdUtils.defaultFeatures() : new HashSet<Xd2XsdFeature>();
        if (config.getFeatures() != null) {
            features.addAll(config.getFeatures());
        }
        adapter.setFeatures(features);
        if (config.getVerbose() >= SchemaLoggerDefs.LOG_INFO) {
            System.out.println("Enabled features: " + features);
        }
        return adapter;
    }

    private String writeOutputSchemas(final XmlSchemaCollection outputSchemaCollection, final Set<String> schemaNames) {
        final File outputDir = new File(config.getOutputDirectory());
        String outputRootSchemaPath = null;

        for (String schemaName : schemaNames) {
            final XmlSchema[] outputSchemas = outputSchemaCollection.getXmlSchema(schemaName);
            try {
                // Output XSD
                for (int i = 0; i < outputSchemas.length; i++) {
                    String outFileName = schemaName;

                    if (config.getOutputFilePrefix() != null && !config.getOutputFilePrefix().isEmpty()) {
                        outFileName = config.getOutputFilePrefix() + outFileName;
                    }

                    if (outputSchemas.length > 1) {
                        outFileName += "_" + i;
                    }

                    if (config.getOutputFileExt() != null && !config.getOutputFileExt().isEmpty()) {
                        outFileName += config.getOutputFileExt();
                    }

                    outFileName = outputDir.getAbsolutePath() + "\\" + outFileName;
                    if (schemaName.equals(config.getInputRoot())) {
                        outputRootSchemaPath = outFileName;
                    }
                    BufferedWriter writer = new BufferedWriter(new FileWriter(outFileName));
                    outputSchemas[i].write(writer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return outputRootSchemaPath;
    }

    private void validateXmlData(final String outputRootSchemaPath) {
        final StreamSource outputRootSchemaFile = new StreamSource(new File(outputRootSchemaPath));

        if (config.hasPositiveTestingData()) {
            for (String testingFile : config.getTestingDataPos()) {
                validateXmlAgainstXsd(new File(testingFile), outputRootSchemaFile, true);
            }
        }

        if (config.hasNegativeTestingData()) {
            for (String testingFile : config.getTestingDataNeg()) {
                validateXmlAgainstXsd(new File(testingFile), outputRootSchemaFile, false);
            }
        }
    }

    private void validateXmlAgainstXsd(final File xmlDataFile, final StreamSource outputRootSchema, boolean expectedResult) {
        XmlValidator validator = new XmlValidator(new StreamSource(xmlDataFile), outputRootSchema);
        if (expectedResult != validator.validate(config.getVerbose() > SchemaLoggerDefs.LOG_NONE)) {
            System.out.println("Xml validation " + (expectedResult ? "positive" : "negative") + " failed, fileName: " + xmlDataFile);
        }
    }

    public XmlSchemaCollection transform() {
        SchemaLogger.setLogLevel(config.getVerbose());

        try {
            final XdPool2XsdAdapter adapter = createXdPoolAdapter();

            // Load x-definition files
            final File[] defFiles = SUtils.getFileGroup(config.getInputFileName());
            final Properties props = new Properties();
            props.setProperty(XDConstants.XDPROPERTY_IGNORE_UNDEF_EXT, XDConstants.XDPROPERTYVALUE_IGNORE_UNDEF_EXT_TRUE);
            final XDBuilder xb = XDFactory.getXDBuilder(props);
            xb.setSource(defFiles);
            final XDPool inputXD = xb.compileXD();

            // Convert XD -> XSD Schema
            final XmlSchemaCollection outputXmlSchemaCollection = adapter.createSchemas(inputXD);
            final String outputRootSchemaPath = writeOutputSchemas(outputXmlSchemaCollection, adapter.getSchemaNames());

            if (config.hasTestingData()) {
                validateXmlData(outputRootSchemaPath);
            }

            return outputXmlSchemaCollection;
        } catch (Exception ex) {
            System.out.println(ex);
        }

        return null;
    }
}
