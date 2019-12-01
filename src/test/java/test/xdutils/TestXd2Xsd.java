package test.xdutils;

import buildtools.XDTester;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.constants.Constants;
import org.xdef.*;
import org.xdef.impl.util.conv.xd2schema.xsd.XDPool2XsdAdapter;
import org.xdef.impl.util.conv.xd2schema.xsd.XDef2XsdAdapter;
import org.xdef.impl.util.conv.xd2schema.xsd.definition.XD2XsdFeature;
import org.xdef.impl.util.conv.xd2schema.xsd.util.XD2XsdUtils;
import org.xdef.impl.util.conv.schema.util.XsdLogger;
import org.xdef.proc.XXElement;
import org.xdef.proc.XXNode;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SUtils;
import org.xdef.util.XValidate;

import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.xdef.impl.util.conv.schema.util.XsdLoggerDefs.LOG_INFO;
import static org.xdef.impl.util.conv.schema.util.XsdLoggerDefs.LOG_WARN;

public class TestXd2Xsd extends TesterXdSchema {

    private void init() {
        File dataDir = new File(getDataDir());
        if (!dataDir.exists() || !dataDir.isDirectory()) {
            throw new RuntimeException(
                    "Data directory does not exists or is not a directory!");
        }

        _inputFilesRoot = initFolder(dataDir, "xd2xsd_2");
        _refFilesRoot = initFolder(dataDir, "xd2xsd_2");
        _dataFilesRoot = initFolder(dataDir, "xd2xsd_2");
        _outputFilesRoot = initFolder(dataDir, "xd2xsd_2\\output");

        XsdLogger.setLogLevel(LOG_WARN);
    }

    private File getInputXDefFile(final String fileName) throws FileNotFoundException {
        return getFile(_inputFilesRoot.getAbsolutePath() + "\\" + fileName, fileName, ".xdef");
    }

    private XDPool compileXd(final String fileName) throws FileNotFoundException {
        return compile(getInputXDefFile(fileName), this.getClass());
    }

    private XmlSchemaCollection getRefXsd(final String fileName) throws FileNotFoundException {
        XmlSchemaCollection schemaCollection = new XmlSchemaCollection();
        schemaCollection.setBaseUri(_inputFilesRoot.getAbsolutePath() + "\\" + fileName);
        schemaCollection.read(createRefFileReader(fileName, ".xsd"));

        return schemaCollection;
    }

    private File getOutputSchemaFile(final String fileName) {
        File res = null;
        try {
            res = getFile(_outputFilesRoot.getAbsolutePath(), fileName, ".xsd");
        } catch (FileNotFoundException ex) {
            assertTrue(false, "Output XSD file is not generated, fileName: " + fileName);
        }

        return res;
    }

    private File getRefSchemaFile(final String fileName) throws FileNotFoundException {
        return getFile(_inputFilesRoot.getAbsolutePath() + "\\" + fileName, fileName, ".xsd");
    }

    private XDef2XsdAdapter createXdDefAdapter(Set<XD2XsdFeature> additionalFeatures) {
        final XDef2XsdAdapter adapter = new XDef2XsdAdapter();
        final Set<XD2XsdFeature> features = XD2XsdUtils.defaultFeatures();
        features.add(XD2XsdFeature.XSD_ANNOTATION);
        features.add(XD2XsdFeature.XSD_NAME_COLISSION_DETECTOR);
        if (additionalFeatures != null) {
            features.addAll(additionalFeatures);
        }
        adapter.setFeatures(features);
        return adapter;
    }

    private XDPool2XsdAdapter createXdPoolAdapter(Set<XD2XsdFeature> additionalFeatures) {
        final XDPool2XsdAdapter adapter = new XDPool2XsdAdapter();
        final Set<XD2XsdFeature> features = XD2XsdUtils.defaultFeatures();
        features.add(XD2XsdFeature.XSD_ANNOTATION);
        features.add(XD2XsdFeature.XSD_NAME_COLISSION_DETECTOR);
        if (additionalFeatures != null) {
            features.addAll(additionalFeatures);
        }
        adapter.setFeatures(features);
        return adapter;
    }

    private void writeOutputSchemas(final XmlSchemaCollection outputSchemaCollection, final Set<String> schemaNames) {
        for (String schemaName : schemaNames) {
            XmlSchema[] outputSchemas = outputSchemaCollection.getXmlSchema(schemaName);

            assertEq(1, outputSchemas.length, "Multiple schemas of same system name: " + schemaName);

            if (WRITE_OUTPUT_INTO_FILE == true) {
                try {
                    // Output XSD
                    for (int i = 0; i < outputSchemas.length; i++) {
                        String outFileName = schemaName;
                        if (outputSchemas.length != 1) {
                            outFileName += "_err";
                        }

                        if (outputSchemas.length > 1) {
                            outFileName += "_" + i;
                        }

                        outFileName += ".xsd";

                        BufferedWriter writer = new BufferedWriter(new FileWriter(_outputFilesRoot.getAbsolutePath() + "\\" + outFileName));
                        outputSchemas[i].write(writer);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void validateSchemas(final String fileName,
                                 final XmlSchemaCollection refSchemaCollection,
                                 final XmlSchemaCollection outputSchemaCollection,
                                 final Set<String> schemaNames,
                                 int schemaCount) throws UnsupportedEncodingException {
        XmlSchema[] refSchemasAll = refSchemaCollection.getXmlSchemas();
        XmlSchema[] outputSchemasAll = outputSchemaCollection.getXmlSchemas();

        // TODO: Fix multiple XSD validation - How to properly filter circle loaded XSD schemas
        int realRefSchemas = 0;
        boolean xsdRootImported = false;
        for (XmlSchema refSchema : refSchemasAll) {
            if (refSchema.getSourceURI() != null) {
                realRefSchemas++;
            } else if (Constants.URI_2001_SCHEMA_XSD.equals(refSchema.getLogicalTargetNamespace()) == false && xsdRootImported == false) {
                realRefSchemas++;
                xsdRootImported = true;
            }
        }

        //assertEq(realRefSchemas + 1, schemaCount + 1, "Invalid number of reference schemas, fileName: " + fileName);
        assertEq(outputSchemasAll.length, schemaCount + 1, "Invalid number of output schemas, fileName: " + fileName);
        //assertEq(realRefSchemas + 1, outputSchemasAll.length, "Expected same number of reference and output schemas, fileName: " + fileName);

        if (PRINT_OUTPUT_TO_CONSOLE == true) {
            for (XmlSchema outputSchema : outputSchemasAll) {
                if (Constants.URI_2001_SCHEMA_XSD.equals(outputSchema.getLogicalTargetNamespace()) == false) {
                    outputSchema.write(System.out);
                }
            }
        }

        boolean xsdRootChecked = false;

        for (String schemaName : schemaNames) {
            String refSourceName = ("file:/" + _inputFilesRoot.getAbsolutePath() + "\\" + fileName + "\\" + schemaName + ".xsd").replace('\\', '/');
            XmlSchema[] refSchemas = refSchemaCollection.getXmlSchema(refSourceName);
            if (refSchemas.length == 0 && xsdRootChecked == false) {
                refSchemas = refSchemaCollection.getXmlSchema(null);
                xsdRootChecked = true;
            }
            XmlSchema[] outputSchemas = outputSchemaCollection.getXmlSchema(schemaName);

            assertEq(1, outputSchemas.length, "Multiple schemas of same system name: " + schemaName);
            assertEq(outputSchemas.length, refSchemas.length, "Unexpected number of matched schemas name: " + schemaName);

            boolean mismatch = false;
            if (refSchemas.length > 0 && outputSchemas.length > 0) {
                ByteArrayOutputStream refOutputStream = new ByteArrayOutputStream();
                refSchemas[0].write(refOutputStream);

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                outputSchemas[0].write(outputStream);

                mismatch = !refOutputStream.toString().equals(outputStream.toString());
                assertFalse(mismatch, "Same schema by sourceId, but different content, name: " + schemaName);
            }

            if (WRITE_OUTPUT_INTO_FILE == true) {
                try {
                    // Reference XSD
                    for (int i = 0; i < refSchemas.length; i++) {
                        String outFileName = schemaName;
                        if (mismatch || refSchemas.length != 1) {
                            outFileName += "_err";
                        }

                        if (refSchemas.length > 1) {
                            outFileName += "_" + i;
                        }

                        outFileName += "_ref";
                        outFileName += ".xsd";

                        BufferedWriter writer = new BufferedWriter(new FileWriter(_outputFilesRoot.getAbsolutePath() + "\\" + outFileName));
                        refSchemas[i].write(writer);
                    }

                    // Output XSD
                    for (int i = 0; i < outputSchemas.length; i++) {
                        String outFileName = schemaName;
                        if (mismatch || outputSchemas.length != 1) {
                            outFileName += "_err";
                        }

                        if (outputSchemas.length > 1) {
                            outFileName += "_" + i;
                        }

                        outFileName += ".xsd";

                        BufferedWriter writer = new BufferedWriter(new FileWriter(_outputFilesRoot.getAbsolutePath() + "\\" + outFileName));
                        outputSchemas[i].write(writer);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        for (int i = 0; i < refSchemasAll.length; i++) {
            String sourceUri = refSchemasAll[i].getSourceURI();
            if (sourceUri != null) {
                File tmpFile = new File(sourceUri);
                String refFileName = tmpFile.getName().replaceFirst("[.][^.]+$", "");
                if (refFileName != null && !schemaNames.contains(refFileName)) {
                    try {
                        BufferedWriter refWriter = new BufferedWriter(new FileWriter(_outputFilesRoot.getAbsolutePath() + "\\" + fileName + "_unk_ref_" + i + ".xsd"));
                        refSchemasAll[i].write(refWriter);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void validateXmlAgainstXDef(final String fileName, List<String> validTestingData, List<String> invalidTestingData) throws FileNotFoundException {
        // Validate valid XML file against XSD schema
        if (validTestingData != null) {
            for (String testingFile : validTestingData) {
                File xmlDataFile = getXmlDataFile(fileName, testingFile);
                File xDefFile = getInputXDefFile(fileName);
                ArrayReporter reporter = new ArrayReporter();
                XDDocument xdDocument = XValidate.validate(null, xmlDataFile, (File[])Arrays.asList(xDefFile).toArray(), fileName, reporter);
                assertTrue(xdDocument != null, "XML is not valid against x-definition. Test=" + fileName + ", File=" + testingFile);
                assertFalse(reporter.errors(), "Error occurs on x-definition validation. Test=" + fileName + ", File=" + testingFile);
            }
        }

        // Validate invalid XML file against XSD schema
        if (invalidTestingData != null) {
            for (String testingFile : invalidTestingData) {
                File xmlDataFile = getXmlDataFile(fileName, testingFile);
                File xDefFile = getInputXDefFile(fileName);
                ArrayReporter reporter = new ArrayReporter();
                XValidate.validate(null, xmlDataFile, (File[])Arrays.asList(xDefFile).toArray(), fileName, reporter);
                assertTrue(reporter.errors(), "Error does not occurs on x-definition validation (but it should). Test=" + fileName + ", File=" + testingFile);
            }
        }
    }

    private void validateXmlAgainstXsd(final String fileName, List<String> validTestingData, List<String> invalidTestingData, boolean validateRef, boolean invalidXsd) throws FileNotFoundException {
        File outputXsdFile = getOutputSchemaFile(fileName);
        File refXsdFile = validateRef ? getRefSchemaFile(fileName) : null;

        // Validate valid XML file against XSD schema
        if (validTestingData != null) {
            for (String testingFile : validTestingData) {
                File xmlDataFile = getXmlDataFile(fileName, testingFile);
                if (validateRef == true && VALIDATE_XML_AGAINST_REF_FILE == true) {
                    validateXmlAgainstXsd(fileName, xmlDataFile, refXsdFile, true, "ref");
                }
                if (outputXsdFile != null) {
                    validateXmlAgainstXsd(fileName, xmlDataFile, outputXsdFile, !invalidXsd, "out");
                }
            }
        }

        // Validate invalid XML file against XSD schema
        if (invalidTestingData != null) {
            for (String testingFile : invalidTestingData) {
                File xmlDataFile = getXmlDataFile(fileName, testingFile);
                if (validateRef == true && VALIDATE_XML_AGAINST_REF_FILE == true) {
                    validateXmlAgainstXsd(fileName, xmlDataFile, refXsdFile, false, "ref");
                }
                if (outputXsdFile != null) {
                    validateXmlAgainstXsd(fileName, xmlDataFile, outputXsdFile, false, "out");
                }
            }
        }
    }

    private void validateXmlAgainstXsd(final String fileName, final File xmlFile, final File xsdSchemaFile, boolean expectedResult, String type) {
        XmlValidator validator = new XmlValidator(new StreamSource(xmlFile), new StreamSource(xsdSchemaFile));
        assertEq(expectedResult, validator.validate(_outputFilesRoot.getAbsolutePath(), expectedResult && PRINT_XML_VALIDATION_ERRORS),
                "Xml validation failed, testCase: " + fileName + ", type: " + type + ", fileName: " + xmlFile.getName());
    }

    private void convertXdDef2Xsd(final String fileName, List<String> validTestingData, List<String> invalidTestingData) {
        convertXdDef2Xsd(fileName, validTestingData, invalidTestingData, true, null, false, null);
    }

    private void convertXdDef2XsdNoRef(final String fileName, List<String> validTestingData, List<String> invalidTestingData) {
        convertXdDef2Xsd(fileName, validTestingData, invalidTestingData, false, null, false, null);
    }

    private void convertXdDef2XsdWithFeatures(final String fileName, List<String> validTestingData, List<String> invalidTestingData, Set<XD2XsdFeature> features) {
        convertXdDef2Xsd(fileName, validTestingData, invalidTestingData, false, null, false, features);
    }

    private void convertXdDef2XsdNoSupport(final String fileName, List<String> validTestingData, List<String> invalidTestingData, String exMsg) {
        convertXdDef2Xsd(fileName, validTestingData, invalidTestingData, false, exMsg, false, null);
    }

    private void convertXdDef2XsdInvalidXsd(final String fileName, List<String> validTestingData, List<String> invalidTestingData) {
        convertXdDef2Xsd(fileName, validTestingData, invalidTestingData, false, null, true, null);
    }

    private void convertXdDef2Xsd(final String fileName,
                                  List<String> validTestingData, List<String> invalidTestingData,
                                  boolean validateAgainstRefXsd,
                                  String exMsg, boolean invalidXsd,
                                  Set<XD2XsdFeature> features) {
        ArrayReporter reporter = new ArrayReporter();
        setProperty("xdef.warnings", "true");
        try {
            XDef2XsdAdapter adapter = createXdDefAdapter(features);

            // Convert XD -> XSD Schema
            XDPool inputXD = compileXd(fileName);
            XmlSchemaCollection outputXmlSchemaCollection = adapter.createSchema(inputXD);

            // Compare output XSD schemas to XSD references
            if (validateAgainstRefXsd) {
                validateSchemas(fileName, getRefXsd(fileName), outputXmlSchemaCollection, adapter.getSchemaNames(), 1);
            } else {
                writeOutputSchemas(outputXmlSchemaCollection, adapter.getSchemaNames());
            }

            validateXmlAgainstXDef(fileName, validTestingData, invalidTestingData);

            // Validate XML files against output XSD schemas and reference XSD schemas
            validateXmlAgainstXsd(fileName, validTestingData, invalidTestingData, validateAgainstRefXsd, invalidXsd);

            assertNoErrors(reporter);
        } catch (Exception ex) {
            if (exMsg != null) {
                assertEq(exMsg, ex.getMessage());
            } else {
                fail(ex);
            }
        }
    }

    private void convertXdPool2XsdNoSupport(final String fileName, List<String> validTestingData, List<String> invalidTestingData, String exMsg) {
        convertXdPool2Xsd(fileName, validTestingData, invalidTestingData, false, exMsg, false, null);
    }

    private void convertXdPool2XsdInvalidXsd(final String fileName, List<String> validTestingData, List<String> invalidTestingData) {
        convertXdPool2Xsd(fileName, validTestingData, invalidTestingData, false, null, true, null);
    }

    private void convertXdPool2XsdNoRef(final String fileName, List<String> validTestingData, List<String> invalidTestingData) {
        convertXdPool2Xsd(fileName, validTestingData, invalidTestingData, false, null, false, null);
    }

    private void convertXdPool2XsdWithFeatures(final String fileName, List<String> validTestingData, List<String> invalidTestingData, Set<XD2XsdFeature> features) {
        convertXdPool2Xsd(fileName, validTestingData, invalidTestingData, false, null, false, features);
    }

    private void convertXdPool2Xsd(final String fileName, List<String> validTestingData, List<String> invalidTestingData) {
        convertXdPool2Xsd(fileName, validTestingData, invalidTestingData, true, null, false, null);
    }

    private void convertXdPool2Xsd(final String fileName, List<String> validTestingData,
                                   List<String> invalidTestingData,
                                   boolean validateAgainstRefXsd,
                                   String exMsg, boolean invalidXsd,
                                   Set<XD2XsdFeature> features) {
        ArrayReporter reporter = new ArrayReporter();
        setProperty("xdef.warnings", "true");
        try {
            XDPool2XsdAdapter adapter = createXdPoolAdapter(features);

            // Load x-definition files
            File[] defFiles = SUtils.getFileGroup(_inputFilesRoot.getAbsolutePath() + "\\" + fileName + "\\" + fileName + "*.xdef");
            XDBuilder xb = XDFactory.getXDBuilder(null);
            xb.setExternals(getClass());
            xb.setSource(defFiles);
            XDPool inputXD = xb.compileXD();
            // Convert XD -> XSD Schema
            XmlSchemaCollection outputXmlSchemaCollection = adapter.createSchemas(inputXD);
            int expectedShemaCount = inputXD.getXMDefinitions().length;

            // Compare output XSD schemas to XSD references
            if (validateAgainstRefXsd) {
                validateSchemas(fileName, getRefXsd(fileName), outputXmlSchemaCollection, adapter.getSchemaNames(), expectedShemaCount);
            } else {
                writeOutputSchemas(outputXmlSchemaCollection, adapter.getSchemaNames());
            }

            validateXmlAgainstXDef(fileName, validTestingData, invalidTestingData);

            // Validate XML files against output XSD schemas and reference XSD schemas
            validateXmlAgainstXsd(fileName, validTestingData, invalidTestingData, validateAgainstRefXsd, invalidXsd);

            assertNoErrors(reporter);
        } catch (Exception ex) {
            if (exMsg != null) {
                assertEq(exMsg, ex.getMessage());
            } else {
                fail(ex);
            }
        }

        if (exMsg != null) {
            fail("Test should failed with message: " + exMsg);
        }
    }

    @Override
    public void test() {
        init();

        // ============ XDef ============

        convertXdDef2Xsd("t000", Arrays.asList(new String[] {"t000"}), null);
        convertXdDef2Xsd("t001", Arrays.asList(new String[] {"t001"}), null);
        convertXdDef2Xsd("t002", Arrays.asList(new String[] {"t002"}), null);
        convertXdDef2Xsd("t003", Arrays.asList(new String[] {"t003"}), null);
        convertXdDef2Xsd("t004", Arrays.asList(new String[] {"t004"}), null);
        convertXdDef2Xsd("t005", Arrays.asList(new String[] {"t005"}), null);
        convertXdDef2Xsd("t007", Arrays.asList(new String[] {"t007"}), null);
        convertXdDef2Xsd("t009", Arrays.asList(new String[] {"t009"}), null);
        convertXdDef2Xsd("t010", Arrays.asList(new String[] {"t010"}), null);
        convertXdDef2Xsd("t016", Arrays.asList(new String[] {"t016"}), Arrays.asList(new String[] {"t016e"}));
        convertXdDef2Xsd("t019", Arrays.asList(new String[] {"t019"}), null);
        convertXdDef2Xsd("t020", Arrays.asList(new String[] {"t020"}), null);

        convertXdDef2XsdNoRef ("ATTR_CHLD_to_CHLD", Arrays.asList(new String[] {"ATTR_CHLD_to_CHLD_valid_1"}), null);
        convertXdDef2XsdNoRef ("ATTR_CHLD_to_ATTR", Arrays.asList(new String[] {"ATTR_CHLD_to_ATTR_valid_1"}), null);
        convertXdDef2XsdNoRef ("ATTR_CHLD_to_ATTR_CHLD", Arrays.asList(new String[] {"ATTR_CHLD_to_ATTR_CHLD_valid_1"}), null);
        convertXdDef2XsdNoRef ("ATTR_to_ATTR", Arrays.asList(new String[] {"ATTR_to_ATTR_valid_1", "ATTR_to_ATTR_valid_2"}), Arrays.asList(new String[] {"ATTR_to_ATTR_invalid_1", "ATTR_to_ATTR_invalid_2"}));
        convertXdDef2XsdNoRef ("ATTR_to_CHLD", Arrays.asList(new String[] {"ATTR_to_CHLD_valid_1"}), null);
        convertXdDef2XsdNoRef ("ATTR_to_ATTR_CHLD", Arrays.asList(new String[] {"ATTR_to_ATTR_CHLD_valid_1"}), null);
        convertXdDef2XsdNoRef ("basicTest",
                Arrays.asList(new String[] {"basicTest_valid_1", "basicTest_valid_2", "basicTest_valid_3"}),
                Arrays.asList(new String[] {"basicTest_invalid_1", "basicTest_invalid_2", "basicTest_invalid_3", "basicTest_invalid_4"}));

        convertXdDef2XsdNoRef ("B1_common", Arrays.asList(new String[] {"B1_Common_valid_1", "B1_Common_valid_2"}), null);

        convertXdDef2XsdNoRef ("CHLD_to_ATTR", Arrays.asList(new String[] {"CHLD_to_ATTR_valid_1"}), null);
        convertXdDef2XsdNoRef ("CHLD_to_ATTR_CHLD", Arrays.asList(new String[] {"CHLD_to_ATTR_CHLD_valid_1"}), null);
        convertXdDef2XsdNoRef ("CHLD_to_CHLD", Arrays.asList(new String[] {"CHLD_to_CHLD_valid_1"}), null);

        convertXdDef2XsdNoRef ("dateTimeTest", Arrays.asList(new String[] {"dateTimeTest_valid_1"}), null);
        convertXdDef2XsdNoRef ("declarationTest",
                Arrays.asList(new String[] {"declarationTest_valid_1", "declarationTest_valid_2", "declarationTest_valid_3"}),
                Arrays.asList(new String[] {"declarationTest_invalid_1", "declarationTest_invalid_2", "declarationTest_invalid_3", "declarationTest_invalid_4"}));

        convertXdDef2XsdNoRef ("namespaceTest", Arrays.asList(new String[] {"namespaceTest_valid"}), null);
        convertXdDef2XsdNoRef ("namespaceTest2", Arrays.asList(new String[] {"namespaceTest2_valid_1"}), null);
        convertXdPool2XsdNoRef("namespaceTest3", Arrays.asList(new String[] {"namespaceTest3_valid_1"}), null);
        convertXdPool2XsdNoRef("namespaceTest4", Arrays.asList(new String[] {"namespaceTest4_valid_1"}), null);

        convertXdDef2XsdNoRef ("simpleModelTest",
                Arrays.asList(new String[] {"simpleModelTest_valid_1", "simpleModelTest_valid_2", //"simpleModelTest_valid_3",
                        "simpleModelTest_valid_5", "simpleModelTest_valid_5"}), null);

        convertXdDef2XsdNoRef ("t990", Arrays.asList(new String[] {"t990_1"}), Arrays.asList(new String[] {"t990_1e", "t990_2e", "t990_3e", "t990_4e", "t990_5e"}));
        convertXdDef2XsdNoRef ("D1A", Arrays.asList(new String[] {"D1A"}), null);

        // ============ XDPool ============

        convertXdPool2Xsd("t011", Arrays.asList(new String[] {"t011"}), null);
        convertXdPool2Xsd("t012", Arrays.asList(new String[] {"t012", "t012_1", "t012_2"}), null);
        convertXdPool2Xsd("t013", Arrays.asList(new String[] {"t013"}), null);
        convertXdPool2Xsd("t014", Arrays.asList(new String[] {"t014"}), null);
        convertXdPool2Xsd("t015", Arrays.asList(new String[] {"t015", "t015_1"}), null);
        convertXdPool2Xsd("t018", Arrays.asList(new String[] {"t018"}), null);

        convertXdPool2XsdNoRef ("globalAndLocalTest",
                Arrays.asList(new String[] {"globalAndLocalTest_X", "globalAndLocalTest_Y", "globalAndLocalTest_Z"}),
                Arrays.asList(new String[] {"globalAndLocalTest_X_invalid", "globalAndLocalTest_Y_invalid", "globalAndLocalTest_Z_invalid"})
        );

        convertXdPool2XsdNoRef ("multiXdefTest", Arrays.asList(new String[] {"multiXdefTest_valid_1"}), null);
        convertXdPool2XsdNoRef ("multiXdefTest2", Arrays.asList(new String[] {"multiXdefTest2_valid_1"}), null);
        convertXdPool2XsdNoRef ("multiXdefTest3", Arrays.asList(new String[] {"multiXdefTest3_valid_1"}), null);

        convertXdPool2XsdNoRef ("simpleRefTest", Arrays.asList(new String[] {"simpleRefTest_valid_1"}), null);

        convertXdPool2XsdNoRef ("refTest1", Arrays.asList(new String[] {"refTest1_valid_1"}), null);
        convertXdPool2XsdNoRef ("refTest2", Arrays.asList(new String[] {"refTest2_valid_1"}), null);
        convertXdPool2XsdNoRef ("refTest3", Arrays.asList(new String[] {"refTest3_valid_1"}), null);

        convertXdPool2XsdNoRef ("sisma", Arrays.asList(new String[] {"sisma"}), null);
        convertXdPool2XsdNoRef ("typeTest", Arrays.asList(new String[] {"typeTest_valid_1"}), null);
        convertXdPool2XsdWithFeatures ("typeTest2", Arrays.asList(new String[] {"typeTest2_valid_1"}), null, EnumSet.of(XD2XsdFeature.XSD_DECIMAL_ANY_SEPARATOR));
        convertXdPool2XsdNoRef("Test000_05", Arrays.asList(new String[] {"Test000_05"}), null);

        // ============ List/union advanced ============

        convertXdDef2XsdNoRef ("schemaTypeTest", Arrays.asList(new String[] {"schemaTypeTest_valid_1"}), Arrays.asList(new String[] {"schemaTypeTest_invalid_1"}));
        convertXdDef2XsdNoRef ("schemaTypeTest2", Arrays.asList(new String[] {"schemaTypeTest2_valid_1"}), null);
        convertXdDef2XsdNoRef ("schemaTypeTest3", Arrays.asList(new String[] {"schemaTypeTest3_valid_1"}), null);

        // ============ Groups ============

        convertXdDef2XsdNoRef ("groupChoice1", Arrays.asList(new String[] {"groupChoice1_valid_1", "groupChoice1_valid_2"}), Arrays.asList(new String[] {"groupChoice1_invalid_1", "groupChoice1_invalid_2"}));
        convertXdDef2XsdNoRef ("groupChoice2", Arrays.asList(new String[] {"groupChoice2_valid_1", "groupChoice2_valid_2"}), Arrays.asList(new String[] {"groupChoice2_invalid_1"}));
        convertXdDef2XsdNoRef ("groupChoice3",
                Arrays.asList(new String[] {"groupChoice3_valid_1", "groupChoice3_valid_2", "groupChoice3_valid_3", "groupChoice3_valid_4", "groupChoice3_valid_5"}),
                Arrays.asList(new String[] {"groupChoice3_invalid_1", "groupChoice3_invalid_2", "groupChoice3_invalid_3"})
        );
        convertXdDef2XsdNoRef ("groupChoice4",
                Arrays.asList(new String[] {"groupChoice4_valid_1", "groupChoice4_valid_2", "groupChoice4_valid_3"}),
                Arrays.asList(new String[] {"groupChoice4_invalid_1", "groupChoice4_invalid_2"})
        );

        convertXdDef2XsdNoRef ("groupMixed1", Arrays.asList(new String[] {"groupMixed1_valid_1", "groupMixed1_valid_2"}), Arrays.asList(new String[] {"groupMixed1_invalid_1"}));
        convertXdDef2XsdNoRef ("groupMixed2", Arrays.asList(new String[] {"groupMixed2_valid_1", "groupMixed2_valid_2", "groupMixed2_valid_3", "groupMixed2_valid_4"}), Arrays.asList(new String[] {"groupMixed2_invalid_1"}));
        convertXdDef2XsdNoRef ("groupMixed3",
                Arrays.asList(new String[] {"groupMixed3_valid_1", "groupMixed3_valid_2"}),
                Arrays.asList(new String[] {"groupMixed3_invalid_1", "groupMixed3_invalid_2"}));
        convertXdPool2XsdNoRef ("groupMixed4", Arrays.asList(new String[] {"groupMixed4_valid_1"}), Arrays.asList(new String[] {"groupMixed4_invalid_1", "groupMixed4_invalid_2"}));
        convertXdPool2XsdNoRef ("groupMixed5",
                Arrays.asList(new String[] {"groupMixed5_valid_1", "groupMixed5_valid_2", "groupMixed5_valid_3"}),
                Arrays.asList(new String[] {"groupMixed5_invalid_1"}));
        convertXdPool2XsdNoRef ("groupMixed6",
                Arrays.asList(new String[] {"groupMixed6_valid_1", "groupMixed6_valid_2", "groupMixed6_valid_3", "groupMixed6_valid_4"}),
                Arrays.asList(new String[] {"groupMixed6_invalid_1", "groupMixed6_invalid_2"}));


        convertXdDef2XsdNoRef ("testGroup1", Arrays.asList(new String[] {"testGroup1_valid_1", "testGroup1_valid_2", "testGroup1_valid_3"}), null);
        convertXdDef2XsdNoRef ("testGroup2", Arrays.asList(new String[] {"testGroup2_valid_1"}), null);
        convertXdDef2XsdNoRef ("testGroup3", Arrays.asList(new String[] {"testGroup3_valid_1"}), null);

        // ============ Default/Fixed values ============

        convertXdDef2XsdNoRef ("defaultValue1", Arrays.asList(new String[] {"defaultValue1_valid_1"}), null);
        convertXdDef2XsdNoRef ("defaultValue2", Arrays.asList(new String[] {"defaultValue2_valid_1"}), null);

        // ============ UniqueSets ============

        convertXdDef2XsdNoRef ("keyAndRef1", Arrays.asList(new String[] {"keyAndRef1_valid_1"}), null);
        convertXdDef2XsdNoRef ("keyAndRef2", Arrays.asList(new String[] {"keyAndRef2_valid_1", "keyAndRef2_valid_2"}), null);
        convertXdDef2XsdNoRef ("keyAndRef3", Arrays.asList(new String[] {"keyAndRef3_valid_1"}), null);
        convertXdDef2XsdNoRef ("keyAndRef4", Arrays.asList(new String[] {"keyAndRef4_valid_1"}), null);
        convertXdDef2XsdNoRef ("keyAndRef5", Arrays.asList(new String[] {"keyAndRef5_valid_1"}), null);
        convertXdDef2XsdNoRef ("keyAndRef6", Arrays.asList(new String[] {"keyAndRef6_valid_1"}), null);
        convertXdDef2XsdNoRef ("keyAndRef7", Arrays.asList(new String[] {"keyAndRef7_valid_1"}), null);

    }

    ////////////////////////////////////////////////////////////////////////////////
// External methods for the test Sisma
////////////////////////////////////////////////////////////////////////////////
    public static void initParams(XXElement chkElem) {}
    public static void setErr(XXElement chkElem, XDValue[] params) {}
    public static boolean tab(XXElement chkEl, XDValue[] params) {return true;}
    public static void chkOpt_RC_ifEQ(XXElement chkElem, XDValue[] params) {}
    public static void dateDavka(XXElement chkElem, XDValue[] params) {}
    public static void chk_dec_nonNegative(XXElement chkEl, XDValue[] params) {}
    public static void chk_RC_DatNar_ifEQ(XXElement chkEl, XDValue[] params) {}
    public static void setDefault_ifEx(XXElement chkElem, XDValue[] params) {}
    public static void isEqual(XXNode c, XDValue[] p) {}
    public static void exactlyOneAttr(XXNode c, XDValue[] p){}
    public static void emptySubjHasAddr(XXElement chkElem, XDValue[] params) {}
    public static String getIdOsoba(XXElement chkElem) { return "1"; }
    public static void protocol(XXElement chkElem, String role, long idXxx) {}
    public static void protocol(XXElement chkElem, String role, String ident) {}
    public static void outputIVR(XXElement chkElem, XDValue[] params) {}
    public static String getKodPartnera() { return "1"; }
    public static void chkEQ_PojistitelFuze(XXElement chkEl, XDValue[] params){}
    public static void chk_Poj_NeexElement(XXElement chkEl, XDValue[] params) {}
    public static void chkOpt_IC_ifEQ(XXElement chkElem, XDValue[] params) {}
    public static void hasElement_if(XXElement chkElem, XDValue[] params) {}
    public static void subjekt_OsobaOrFirma(XXElement chkEl, XDValue[] params){}
    public static String getIdSubjekt(XXElement chkElem) { return "1"; }
    public static void notEmptyMisto(XXElement chkElem, XDValue[] params) {}
    public static void equal(XXElement chkElem, XDValue[] params) {}
    public static void chkOpt_CisloTP_ifEQ(XXElement chkEl, XDValue[] params) {}
    public static String getIdVozidlo(XXElement chkElem) { return "1"; }
    public static boolean kvadrant(XXElement chkElem) { return true; }
    public static void chk_TypMinusPlneni_Platba(XXElement chkEl,
                                                 XDValue[] params) {}
    public static boolean fil0(XXElement chkEl, XDValue[] params) {return true;}
////////////////////////////////////////////////////////////////////////////////

    /** Run test
     * @param args ignored
     */
    public static void main(String... args) {
        XDTester.setFulltestMode(true);
        runTest();
    }
}
