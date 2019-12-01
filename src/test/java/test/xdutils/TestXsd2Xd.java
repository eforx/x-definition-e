package test.xdutils;

import buildtools.XDTester;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.xdef.XDDocument;
import org.xdef.impl.util.conv.schema2xd.xsd.Xsd2XDefAdapter;
import org.xdef.impl.util.conv.schema.util.XsdLogger;
import org.xdef.impl.util.conv.schema2xd.xsd.definition.Xsd2XdFeature;
import org.xdef.impl.util.conv.schema2xd.xsd.util.Xsd2XdUtils;
import org.xdef.impl.util.conv.xd2schema.xsd.definition.XD2XsdFeature;
import org.xdef.impl.util.conv.xd2schema.xsd.util.XD2XsdUtils;
import org.xdef.sys.ArrayReporter;
import org.xdef.util.XValidate;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.testng.reporters.Files.readFile;
import static org.xdef.impl.util.conv.schema.util.XsdLoggerDefs.LOG_DEBUG;
import static org.xdef.impl.util.conv.schema.util.XsdLoggerDefs.LOG_INFO;
import static org.xdef.impl.util.conv.schema2xd.xsd.definition.Xsd2XdFeature.XD_TEXT_OPTIONAL;

public class TestXsd2Xd extends TesterXdSchema {

    private void init() {
        File dataDir = new File(getDataDir());
        if (!dataDir.exists() || !dataDir.isDirectory()) {
            throw new RuntimeException(
                    "Data directory does not exists or is not a directory!");
        }

        _inputFilesRoot = initFolder(dataDir, "xsd2xd_2");
        _refFilesRoot = initFolder(dataDir, "xsd2xd_2");
        _dataFilesRoot = initFolder(dataDir, "xsd2xd_2");
        _outputFilesRoot = initFolder(dataDir, "xsd2xd_2\\output");

        XsdLogger.setLogLevel(LOG_DEBUG);
    }

    private File getInputSchemaFile(final String fileName) {
        File res = null;
        try {
            res = getFile(_inputFilesRoot.getAbsolutePath() + "\\" + fileName, fileName, ".xsd");
        } catch (FileNotFoundException ex) {
            assertTrue(false, "Input XSD file not found, fileName: " + fileName);
        }

        return res;
    }

    private File getOutputXDefFile(final String fileName) {
        File res = null;
        try {
            res = getFile(_outputFilesRoot.getAbsolutePath(), fileName, ".xdef");
        } catch (FileNotFoundException ex) {
            assertTrue(false, "Output x-definition file is not generated, fileName: " + fileName);
        }

        return res;
    }

    private File getRefXDefFile(final String fileName) throws FileNotFoundException {
        return getFile(_inputFilesRoot.getAbsolutePath() + "\\" + fileName, fileName, ".xdef");
    }

    private Xsd2XDefAdapter createXsdAdapter(Set<Xsd2XdFeature> additionalFeatures) {
        final Xsd2XDefAdapter adapter = new Xsd2XDefAdapter();
        final Set<Xsd2XdFeature> features = Xsd2XdUtils.defaultFeatures();
        if (additionalFeatures != null) {
            features.addAll(additionalFeatures);
        }
        adapter.setFeatures(features);
        return adapter;
    }

//    private XDPool2XsdAdapter createXdPoolAdapter(Set<XD2XsdFeature> additionalFeatures) {
//        final XDPool2XsdAdapter adapter = new XDPool2XsdAdapter();
//        final Set<XD2XsdFeature> features = XD2XsdUtils.defaultFeatures();
//        features.add(XD2XsdFeature.XSD_ANNOTATION);
//        features.add(XD2XsdFeature.XSD_NAME_COLISSION_DETECTOR);
//        if (additionalFeatures != null) {
//            features.addAll(additionalFeatures);
//        }
//        adapter.setFeatures(features);
//        return adapter;
//    }

    private void writeOutputXDefinition(final String fileName, final String outputXDefinition) {
        if (WRITE_OUTPUT_INTO_FILE == true) {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(_outputFilesRoot.getAbsolutePath() + "\\" + fileName + ".xdef"));
                writer.write(outputXDefinition);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void validateXDefinition(final String fileName, final String outputXDefinition) throws IOException {
        if (PRINT_OUTPUT_TO_CONSOLE == true) {
            System.out.println(outputXDefinition);
        }

        File refXDefFile = getRefXDefFile(fileName);
        String refXDefFileContent = readFile(refXDefFile);

        Diff diff = DiffBuilder.compare(refXDefFileContent)
                .withTest(outputXDefinition)
                .ignoreComments()
                .ignoreWhitespace()
                .checkForSimilar()
                .build();

        boolean mismatch = diff.hasDifferences();
        assertFalse(mismatch, "Output x-definition is different to reference x-definition");

        // Reference x-definition
        {
            String outFileName = fileName;
            if (mismatch) {
                outFileName += "_err";
            }

            outFileName += "_ref.xdef";
            BufferedWriter writer = new BufferedWriter(new FileWriter(_outputFilesRoot.getAbsolutePath() + "\\" + outFileName));
            writer.write(refXDefFileContent);
            writer.close();
        }

        // Output x-definition
        {
            String outFileName = fileName;
            if (mismatch) {
                outFileName += "_err";
            }

            outFileName += ".xdef";
            BufferedWriter writer = new BufferedWriter(new FileWriter(_outputFilesRoot.getAbsolutePath() + "\\" + outFileName));
            writer.write(outputXDefinition);
            writer.close();
        }
    }

    private void validateXmlAgainstXDef(final String fileName, List<String> validTestingData, List<String> invalidTestingData, boolean validateRef) throws FileNotFoundException {
        // Validate valid XML file against x-definition
        File xDefFile = getOutputXDefFile(fileName);
        File refXDefFile = validateRef ? getRefXDefFile(fileName) : null;

        if (validTestingData != null) {
            for (String testingFile : validTestingData) {
                File xmlDataFile = getXmlDataFile(fileName, testingFile);
                if (validateRef == true && VALIDATE_XML_AGAINST_REF_FILE == true) {
                    ArrayReporter reporter = new ArrayReporter();
                    XDDocument xdDocument = XValidate.validate(null, xmlDataFile, (File[])Arrays.asList(refXDefFile).toArray(), fileName, reporter);
                    assertTrue(xdDocument != null, "XML is not valid against x-definition. Test=" + fileName + ", File=" + testingFile);
                    assertFalse(reporter.errors(), "Error occurs on x-definition validation. Test=" + fileName + ", File=" + testingFile);
                }

                ArrayReporter reporter = new ArrayReporter();
                XDDocument xdDocument = XValidate.validate(null, xmlDataFile, (File[])Arrays.asList(xDefFile).toArray(), fileName, reporter);
                assertTrue(xdDocument != null, "XML is not valid against x-definition. Test=" + fileName + ", File=" + testingFile);
                assertFalse(reporter.errors(), "Error occurs on x-definition validation. Test=" + fileName + ", File=" + testingFile);
            }
        }

        // Validate invalid XML file against x-definition
        if (invalidTestingData != null) {
            for (String testingFile : invalidTestingData) {
                File xmlDataFile = getXmlDataFile(fileName, testingFile);
                if (validateRef == true && VALIDATE_XML_AGAINST_REF_FILE == true) {
                    ArrayReporter reporter = new ArrayReporter();
                    XValidate.validate(null, xmlDataFile, (File[])Arrays.asList(refXDefFile).toArray(), fileName, reporter);
                    assertTrue(reporter.errors(), "Error does not occurs on x-definition validation (but it should). Test=" + fileName + ", File=" + testingFile);
                }

                ArrayReporter reporter = new ArrayReporter();
                XValidate.validate(null, xmlDataFile, (File[])Arrays.asList(xDefFile).toArray(), fileName, reporter);
                assertTrue(reporter.errors(), "Error does not occurs on x-definition validation (but it should). Test=" + fileName + ", File=" + testingFile);
            }
        }
    }

    private void validateXmlAgainstXsd(final String fileName, List<String> validTestingData, List<String> invalidTestingData) throws FileNotFoundException {
        File inputXsdFile = getInputSchemaFile(fileName);

        // Validate valid XML file against XSD schema
        if (validTestingData != null) {
            for (String testingFile : validTestingData) {
                File xmlDataFile = getXmlDataFile(fileName, testingFile);
                if (inputXsdFile != null) {
                    validateXmlAgainstXsd(fileName, xmlDataFile, inputXsdFile, true, "out");
                }
            }
        }

        // Validate invalid XML file against XSD schema
        if (invalidTestingData != null) {
            for (String testingFile : invalidTestingData) {
                File xmlDataFile = getXmlDataFile(fileName, testingFile);
                if (inputXsdFile != null) {
                    validateXmlAgainstXsd(fileName, xmlDataFile, inputXsdFile, false, "out");
                }
            }
        }
    }

    private void validateXmlAgainstXsd(final String fileName, final File xmlFile, final File xsdSchemaFile, boolean expectedResult, String type) {
        XmlValidator validator = new XmlValidator(new StreamSource(xmlFile), new StreamSource(xsdSchemaFile));
        assertEq(expectedResult, validator.validate(_outputFilesRoot.getAbsolutePath(), expectedResult && PRINT_XML_VALIDATION_ERRORS),
                "Xml validation failed, testCase: " + fileName + ", type: " + type + ", fileName: " + xmlFile.getName());
    }

    private void convertXsd2XDef(final String fileName, List<String> validTestingData, List<String> invalidTestingData) {
        convertXsd2XDef(fileName, validTestingData, invalidTestingData, true, null);
    }

    private void convertXsd2XDefNoRef(final String fileName, List<String> validTestingData, List<String> invalidTestingData) {
        convertXsd2XDef(fileName, validTestingData, invalidTestingData, false, null);
    }

    private void convertXsd2XDefWithFeatures(final String fileName, List<String> validTestingData, List<String> invalidTestingData, Set<Xsd2XdFeature> features) {
        convertXsd2XDef(fileName, validTestingData, invalidTestingData, false, features);
    }

    private XmlSchemaCollection compileXsd(final String fileName) throws FileNotFoundException {
        final XmlSchemaCollection inputXmlSchemaCollection = new XmlSchemaCollection();
        inputXmlSchemaCollection.read(createInputFileReader(fileName, ".xsd"));
        return inputXmlSchemaCollection;
    }

    private void convertXsd2XDef(final String fileName,
                                  List<String> validTestingData, List<String> invalidTestingData,
                                  boolean validateAgainstRef, Set<Xsd2XdFeature> additionalFeatures) {
        ArrayReporter reporter = new ArrayReporter();
        setProperty("xdef.warnings", "true");
        try {
            Xsd2XDefAdapter adapter = createXsdAdapter(additionalFeatures);

            // Convert XSD -> XD Schema
            XmlSchemaCollection inputXmlSchemaCollection = compileXsd(fileName);
            String outputXDefinition = adapter.createXDefinition(inputXmlSchemaCollection, fileName);

            // Compare output x-definition to x-definition reference
            if (validateAgainstRef) {
                validateXDefinition(fileName, outputXDefinition);
            } else {
                writeOutputXDefinition(fileName, outputXDefinition);
            }

            // Validate XML files against input XSD schema
            validateXmlAgainstXsd(fileName, validTestingData, invalidTestingData);

            validateXmlAgainstXDef(fileName, validTestingData, invalidTestingData, validateAgainstRef);

            assertNoErrors(reporter);
        } catch (Exception ex) {
            fail(ex);
        }
    }

//    private void convertXdPool2Xsd(final String fileName, List<String> validTestingData, List<String> invalidTestingData) {
//        convertXdPool2Xsd(fileName, validTestingData, invalidTestingData, true, null, false, null);
//    }
//
//    private void convertXdPool2XsdNoRef(final String fileName, List<String> validTestingData, List<String> invalidTestingData) {
//        convertXdPool2Xsd(fileName, validTestingData, invalidTestingData, false, null, false, null);
//    }
//
//    private void convertXdPool2Xsd(final String fileName, List<String> validTestingData,
//                                   List<String> invalidTestingData,
//                                   boolean validateAgainstRefXsd,
//                                   String exMsg, boolean invalidXsd,
//                                   Set<XD2XsdFeature> features) {
//        ArrayReporter reporter = new ArrayReporter();
//        setProperty("xdef.warnings", "true");
//        try {
//            XDPool2XsdAdapter adapter = createXdPoolAdapter(features);
//
//            // Load x-definition files
//            File[] defFiles = SUtils.getFileGroup(_inputFilesRoot.getAbsolutePath() + "\\" + fileName + "\\" + fileName + "*.xdef");
//            XDBuilder xb = XDFactory.getXDBuilder(null);
//            xb.setExternals(getClass());
//            xb.setSource(defFiles);
//            XDPool inputXD = xb.compileXD();
//            // Convert XD -> XSD Schema
//            XmlSchemaCollection outputXmlSchemaCollection = adapter.createSchemas(inputXD);
//            int expectedShemaCount = inputXD.getXMDefinitions().length;
//
//            // Compare output XSD schemas to XSD references
//            if (validateAgainstRefXsd) {
//                validateSchemas(fileName, getRefSchemas(fileName), outputXmlSchemaCollection, adapter.getSchemaNames(), expectedShemaCount);
//            } else {
//                writeOutputSchemas(outputXmlSchemaCollection, adapter.getSchemaNames());
//            }
//
//            validateXmlAgainstXDef(fileName, validTestingData, invalidTestingData);
//
//            // Validate XML files against output XSD schemas and reference XSD schemas
//            validateXmlAgainstXsd(fileName, validTestingData, invalidTestingData, validateAgainstRefXsd, invalidXsd);
//
//            assertNoErrors(reporter);
//        } catch (Exception ex) {
//            if (exMsg != null) {
//                assertEq(exMsg, ex.getMessage());
//            } else {
//                fail(ex);
//            }
//        }
//
//        if (exMsg != null) {
//            fail("Test should failed with message: " + exMsg);
//        }
//    }

    @Override
    public void test() {
        System.setProperty("javax.xml.transform.TransformerFactory",
                "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");

        init();

        // ============ XDef ============

        convertXsd2XDef("t000", Arrays.asList(new String[] {"t000"}), Arrays.asList(new String[] {"t000_1e", "t000_2e", "t000_3e"}));
        convertXsd2XDefNoRef("t001", Arrays.asList(new String[] {"t001"}),  Arrays.asList(new String[] {"t001_1e", "t001_2e", "t001_3e", "t001_4e", "t001e"}));
        convertXsd2XDefNoRef("t002", Arrays.asList(new String[] {"t002"}),  Arrays.asList(new String[] {"t002_1e", "t002_2e"}));
        convertXsd2XDefNoRef("t003", Arrays.asList(new String[] {"t003"}),  Arrays.asList(new String[] {"t003_1e"}));
        convertXsd2XDefNoRef("t004", Arrays.asList(new String[] {"t004"}),  Arrays.asList(new String[] {"t004_1e"}));
        convertXsd2XDefWithFeatures("t005", Arrays.asList(new String[] {"t005"}), null, EnumSet.of(XD_TEXT_OPTIONAL));
        convertXsd2XDefNoRef("t006", Arrays.asList(new String[] {"t006", "t006_1"}),  Arrays.asList(new String[] {"t006_2e", "t006_3e"}));
        convertXsd2XDefNoRef("t007", Arrays.asList(new String[] {"t007"}),  Arrays.asList(new String[] {"t007_1e"}));
        convertXsd2XDefNoRef("t009", Arrays.asList(new String[] {"t009"}), null);
        convertXsd2XDefNoRef("t010", Arrays.asList(new String[] {"t010"}), null);
        convertXsd2XDefNoRef("t019", Arrays.asList(new String[] {"t019"}), null);
        convertXsd2XDefNoRef("t020", Arrays.asList(new String[] {"t020"}), null);
        convertXsd2XDefNoRef("t020_1", Arrays.asList(new String[] {"t020_1"}), null);

    }

    /** Run test
     * @param args ignored
     */
    public static void main(String... args) {
        XDTester.setFulltestMode(true);
        runTest();
    }
}
