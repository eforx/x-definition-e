package test.xdutils;

import test.XDTester;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.xdef.XDDocument;
import org.xdef.impl.util.conv.schema.util.SchemaLogger;
import org.xdef.impl.util.conv.schema.schema2xd.xsd.Xsd2XDefAdapter;
import org.xdef.impl.util.conv.schema.schema2xd.xsd.definition.Xsd2XdFeature;
import org.xdef.impl.util.conv.schema.schema2xd.xsd.util.Xsd2XdUtils;
import org.xdef.sys.ArrayReporter;
import org.xdef.util.XValidate;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.testng.reporters.Files.readFile;
import static org.xdef.impl.util.conv.schema.util.SchemaLoggerDefs.LOG_INFO;

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

        SchemaLogger.setLogLevel(LOG_INFO);
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

    private XmlSchema compileXsd(final String fileName) throws FileNotFoundException {
        final XmlSchemaCollection inputXmlSchemaCollection = new XmlSchemaCollection();
        inputXmlSchemaCollection.setBaseUri(_inputFilesRoot.getAbsolutePath() + "\\" + fileName);
        return inputXmlSchemaCollection.read(createInputFileReader(fileName, ".xsd"));
    }

    private void convertXsd2XDef(final String fileName,
                                  List<String> validTestingData, List<String> invalidTestingData,
                                  boolean validateAgainstRef, Set<Xsd2XdFeature> additionalFeatures) {
        try {
            Xsd2XDefAdapter adapter = createXsdAdapter(additionalFeatures);

            // Convert XSD -> XD Schema
            XmlSchema inputXmlSchema = compileXsd(fileName);
            String outputXDefinition = adapter.createXDefinition(inputXmlSchema, fileName);

            // Compare output x-definition to x-definition reference
            if (validateAgainstRef) {
                validateXDefinition(fileName, outputXDefinition);
            } else {
                writeOutputXDefinition(fileName, outputXDefinition);
            }

            // Validate XML files against input XSD schema
            validateXmlAgainstXsd(fileName, validTestingData, invalidTestingData);

            validateXmlAgainstXDef(fileName, validTestingData, invalidTestingData, validateAgainstRef);
        } catch (Exception ex) {
            fail(ex);
        }
    }

    private void convertXsd2XdPool(final String fileName, List<String> validTestingData, List<String> invalidTestingData) {
        convertXsd2XDef(fileName, validTestingData, invalidTestingData, true, null);
    }

    private void convertXsd2XdPoolNoRef(final String fileName, List<String> validTestingData, List<String> invalidTestingData) {
        convertXsd2XDef(fileName, validTestingData, invalidTestingData, false, null);
    }

    private void convertXsd2XdPoolWithFeatures(final String fileName, List<String> validTestingData, List<String> invalidTestingData, Set<Xsd2XdFeature> features) {
        convertXsd2XDef(fileName, validTestingData, invalidTestingData, false, features);
    }

    @Override
    public void test() {
        System.setProperty("javax.xml.transform.TransformerFactory",
                "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");

        init();

        // ==============================
        // ============ XDef ============
        // ==============================

        convertXsd2XDef("t000", Arrays.asList(new String[] {"t000"}), Arrays.asList(new String[] {"t000_1e", "t000_2e", "t000_3e"}));
        convertXsd2XDefNoRef("t001", Arrays.asList(new String[] {"t001"}),  Arrays.asList(new String[] {"t001_1e", "t001_2e", "t001_3e", "t001_4e", "t001e"}));
        convertXsd2XDefNoRef("t002", Arrays.asList(new String[] {"t002"}),  Arrays.asList(new String[] {"t002_1e", "t002_2e"}));
        convertXsd2XDefNoRef("t003", Arrays.asList(new String[] {"t003"}),  Arrays.asList(new String[] {"t003_1e"}));
        convertXsd2XDefNoRef("t004", Arrays.asList(new String[] {"t004"}),  Arrays.asList(new String[] {"t004_1e"}));
        convertXsd2XDefNoRef("t005", Arrays.asList(new String[] {"t005"}), null);
        convertXsd2XDefNoRef("t006", Arrays.asList(new String[] {"t006", "t006_1"}),  Arrays.asList(new String[] {"t006_2e", "t006_3e"}));
        convertXsd2XDefNoRef("t007", Arrays.asList(new String[] {"t007"}),  Arrays.asList(new String[] {"t007_1e"}));
        convertXsd2XDefNoRef("t009", Arrays.asList(new String[] {"t009"}), null);
        convertXsd2XDefNoRef("t010", Arrays.asList(new String[] {"t010"}), null);
        convertXsd2XDefNoRef("t016", Arrays.asList(new String[] {"t016"}),  Arrays.asList(new String[] {"t016e"}));
        convertXsd2XDefNoRef("t019", Arrays.asList(new String[] {"t019"}), null);
        convertXsd2XDefNoRef("t020", Arrays.asList(new String[] {"t020"}), null);
        convertXsd2XDefNoRef("t020_1", Arrays.asList(new String[] {"t020_1"}), null);
        convertXsd2XDefNoRef("t021b", Arrays.asList(new String[] {"t021"}), null);
        convertXsd2XDefNoRef("t990", Arrays.asList(new String[] {"t990", "t990_1"}), Arrays.asList(new String[] {"t990_1e", "t990_2e", "t990_3e", "t990_4e", "t990_5e"}));

        convertXsd2XDefNoRef("test_Inf", Arrays.asList(new String[] {"test_Inf_valid"}), null);
        convertXsd2XDefNoRef("basicTestSchema", Arrays.asList(new String[] {"basicTest_valid_1"}), null);
        convertXsd2XDefNoRef ("basicTest",
                Arrays.asList(new String[] {"basicTest_valid_1", "basicTest_valid_2", "basicTest_valid_3"}),
                Arrays.asList(new String[] {"basicTest_invalid_1", "basicTest_invalid_2", "basicTest_invalid_3", "basicTest_invalid_4"}));
        convertXsd2XDefNoRef ("dateTimeTest", Arrays.asList(new String[] {"dateTimeTest_valid_1"}), null);
        convertXsd2XDefNoRef ("declarationTest",
                Arrays.asList(new String[] {"declarationTest_valid_1", "declarationTest_valid_2", "declarationTest_valid_3"}),
                Arrays.asList(new String[] {"declarationTest_invalid_1", "declarationTest_invalid_2", "declarationTest_invalid_3", "declarationTest_invalid_4"}));
        convertXsd2XDefNoRef ("M1RC", Arrays.asList(new String[] {"M1RC"}), null);
        convertXsd2XDefNoRef ("M1RT", Arrays.asList(new String[] {"M1RT"}), null);

        // ============ Huge data type set ============

        convertXsd2XDefNoRef("test_00015", Arrays.asList(new String[] {"test_00015_data"}), null);
        convertXsd2XDefNoRef("typeTestSchema", Arrays.asList(new String[] {"typeTest_valid_1"}), null);
        convertXsd2XDefNoRef ("simpleModelTest",
                Arrays.asList(new String[] {"simpleModelTest_valid_1", "simpleModelTest_valid_2", //"simpleModelTest_valid_3",
                "simpleModelTest_valid_5", "simpleModelTest_valid_5"}), null);
        convertXsd2XDefNoRef ("B1_common", Arrays.asList(new String[] {"B1_Common_valid_1", "B1_Common_valid_2"}), null);
        convertXsd2XDefNoRef ("D1A", Arrays.asList(new String[] {"D1A"}), null);
        convertXsd2XDefNoRef ("D2A", Arrays.asList(new String[] {"D2A"}), null);
        convertXsd2XDefNoRef ("D3A", Arrays.asList(new String[] {"D3A"}), null);
        convertXsd2XDefNoRef ("D5", Arrays.asList(new String[] {"D5"}), null);
        convertXsd2XDefNoRef ("L1A", Arrays.asList(new String[] {"L1A"}), null);
        convertXsd2XDefNoRef ("M1RN", Arrays.asList(new String[] {"M1RN"}), null);
        convertXsd2XDefNoRef ("M1RS", Arrays.asList(new String[] {"M1RS"}), null);
        convertXsd2XDefNoRef ("M1RV", Arrays.asList(new String[] {"M1RV"}), null);
        convertXsd2XDefNoRef ("P1A", Arrays.asList(new String[] {"P1A"}), null);

        // ============ Multiple types of references ============

        convertXsd2XDefNoRef ("ATTR_CHLD_to_ATTR", Arrays.asList(new String[] {"ATTR_CHLD_to_ATTR_valid_1"}), null);
        convertXsd2XDefNoRef ("ATTR_CHLD_to_ATTR_CHLD", Arrays.asList(new String[] {"ATTR_CHLD_to_ATTR_CHLD_valid_1"}), null);
        convertXsd2XDefNoRef ("ATTR_CHLD_to_CHLD", Arrays.asList(new String[] {"ATTR_CHLD_to_CHLD_valid_1"}), null);
        convertXsd2XDefNoRef ("ATTR_to_ATTR", Arrays.asList(new String[] {"ATTR_to_ATTR_valid_1", "ATTR_to_ATTR_valid_2"}), Arrays.asList(new String[] {"ATTR_to_ATTR_invalid_1", "ATTR_to_ATTR_invalid_2"}));
        convertXsd2XDefNoRef ("ATTR_to_ATTR_CHLD", Arrays.asList(new String[] {"ATTR_to_ATTR_CHLD_valid_1"}), null);
        convertXsd2XDefNoRef ("ATTR_to_CHLD", Arrays.asList(new String[] {"ATTR_to_CHLD_valid_1"}), null);
        convertXsd2XDefNoRef ("CHLD_to_ATTR", Arrays.asList(new String[] {"CHLD_to_ATTR_valid_1"}), null);
        convertXsd2XDefNoRef ("CHLD_to_ATTR_CHLD", Arrays.asList(new String[] {"CHLD_to_ATTR_CHLD_valid_1"}), null);
        convertXsd2XDefNoRef ("CHLD_to_CHLD", Arrays.asList(new String[] {"CHLD_to_CHLD_valid_1"}), null);

        // ============ List/union advanced ============

        convertXsd2XDefNoRef ("schemaTypeTest", Arrays.asList(new String[] {"schemaTypeTest_valid_1"}), Arrays.asList(new String[] {"schemaTypeTest_invalid_1"}));
        convertXsd2XDefNoRef ("schemaTypeTest2", Arrays.asList(new String[] {"schemaTypeTest2_valid_1"}), null);
        convertXsd2XDefNoRef ("schemaTypeTest3", Arrays.asList(new String[] {"schemaTypeTest3_valid_1"}), null);

        // ============ Default/Fixed values ============

        convertXsd2XDefNoRef ("defaultValue1", Arrays.asList(new String[] {"defaultValue1_valid_1"}), null);

        // ============ Groups ============

        convertXsd2XDefNoRef ("testGroup1", Arrays.asList(new String[] {"testGroup1_valid_1", "testGroup1_valid_2"}), null);
        convertXsd2XDefNoRef ("testGroup2", Arrays.asList(new String[] {"testGroup2_valid_1"}), null);
        convertXsd2XDefNoRef ("testGroup3", Arrays.asList(new String[] {"testGroup3_valid_1"}), null);

        // ============ All ============

        convertXsd2XDefNoRef ("groupMixed4", Arrays.asList(new String[] {"groupMixed4_valid_1"}), Arrays.asList(new String[] {"groupMixed4_invalid_1", "groupMixed4_invalid_2"}));
        convertXsd2XDefNoRef ("groupMixed5",
                Arrays.asList(new String[] {"groupMixed5_valid_1", "groupMixed5_valid_2", "groupMixed5_valid_3"}),
                 null);
        convertXsd2XDefNoRef ("groupMixed6",
                Arrays.asList(new String[] {"groupMixed6_valid_1", "groupMixed6_valid_2", "groupMixed6_valid_3", "groupMixed6_valid_4"}),
                Arrays.asList(new String[] {"groupMixed6_invalid_2"}));

        // ============ Mixed content ============

        convertXsd2XdPoolNoRef ("t022", Arrays.asList(new String[] {"t022", "t022_1", "t022_2", "t022_3"}), null);
        convertXsd2XdPoolNoRef ("t023", Arrays.asList(new String[] {"t023", "t023_1", "t023_2", "t023_3", "t023_4", "t023_5", "t023_6"}), null);

        convertXsd2XDefNoRef ("simpleRefTest", Arrays.asList(new String[] {"simpleRefTest_valid_1"}), null);
        convertXsd2XDefNoRef("t021a", Arrays.asList(new String[] {"t021"}), null);

        // ============ Basic namespace ============

        convertXsd2XDefNoRef ("namespaceTest", Arrays.asList(new String[] {"namespaceTest_valid"}), null);

        // ==============================
        // =========== XDPool ===========
        // ==============================

        convertXsd2XdPoolNoRef ("t011", Arrays.asList(new String[] {"t011"}), null);
        convertXsd2XdPoolNoRef ("t012", Arrays.asList(new String[] {"t012"}), null);
        convertXsd2XdPoolNoRef ("t013", Arrays.asList(new String[] {"t013"}), null);
        convertXsd2XdPoolNoRef ("t014", Arrays.asList(new String[] {"t014"}), null);
        convertXsd2XdPoolNoRef ("t015", Arrays.asList(new String[] {"t015"}), null);
        convertXsd2XdPoolNoRef ("t018", Arrays.asList(new String[] {"t018"}), null);

        convertXsd2XdPoolNoRef ("namespaceTest2", Arrays.asList(new String[] {"namespaceTest2_valid_1"}), null);
        convertXsd2XdPoolNoRef ("namespaceTest3", Arrays.asList(new String[] {"namespaceTest3_valid_1"}), null);
        convertXsd2XdPoolNoRef ("namespaceTest4", Arrays.asList(new String[] {"namespaceTest4_valid_1"}), null);

        convertXsd2XdPoolNoRef ("multiXdefTest", Arrays.asList(new String[] {"multiXdefTest_valid_1"}), null);
        convertXsd2XdPoolNoRef ("multiXdefTest2", Arrays.asList(new String[] {"multiXdefTest2_valid_1"}), null);
        convertXsd2XdPoolNoRef ("multiXdefTest3", Arrays.asList(new String[] {"multiXdefTest3_valid_1"}), null);

        convertXsd2XdPoolNoRef ("refTest1", Arrays.asList(new String[] {"refTest1_valid_1"}), null);
        convertXsd2XdPoolNoRef ("refTest2", Arrays.asList(new String[] {"refTest2_valid_1"}), null);
        convertXsd2XdPoolNoRef ("refTest3", Arrays.asList(new String[] {"refTest3_valid_1"}), null);
        convertXsd2XdPoolNoRef ("sisma", Arrays.asList(new String[] {"sisma"}), null);
        convertXsd2XdPoolNoRef ("Sisma_RegistraceSU", Arrays.asList(new String[] {"Sisma_RegistraceSU"}), null);

    }

    /** Run test
     * @param args ignored
     */
    public static void main(String... args) {
        XDTester.setFulltestMode(true);
        runTest();
    }
}
