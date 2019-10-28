package test.xdutils;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.constants.Constants;
import org.xdef.XDPool;
import org.xdef.XDValue;
import org.xdef.impl.util.conv.xd2schemas.xsd.XDPool2XsdAdapter;
import org.xdef.impl.util.conv.xd2schemas.xsd.XDef2XsdAdapter;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XmlValidator;
import org.xdef.proc.XXElement;
import org.xdef.sys.ArrayReporter;
import test.utils.XDTester;

import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.*;

public class TestXd2Xsd extends XDTester {

    static private boolean PRINT_SCHEMA_TO_OUTPUT = false;
    static private boolean WRITE_SCHEMAS_INTO_FILE = true;
    static private boolean VALIDATE_XML_AGAINST_REF_XSD = true;
    static private boolean VALIDATE_XML_PRINT_ERRORS = true;

    private File _inputFilesRoot;
    private File _refFilesRoot;
    private File _dataFilesRoot;
    private File _outputFilesRoot;

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
    }

    private File initFolder(final File dataDir, final String folderPath) {
        File folder = new File(dataDir.getAbsolutePath(), folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            throw new RuntimeException("Directory " + folderPath + " does not exists!");
        }

        return folder;
    }

    private File getFile(final String path, final String fileName, final String fileExt) throws FileNotFoundException {
        File xdFile = new File(path, fileName + fileExt);
        if (xdFile == null || !xdFile.exists() || !xdFile.isFile()) {
            throw new FileNotFoundException("Path: " + path + "\\" + fileName + fileExt);
        }

        return xdFile;
    }

    private XDPool compileXd(final String fileName) throws FileNotFoundException {
        return compile(getFile(_inputFilesRoot.getAbsolutePath() + "\\" + fileName, fileName, ".xdef"), this.getClass());
    }

    private FileReader createFileReader(final String filePath, final String fileName, final String fileExt) throws FileNotFoundException {
        return new FileReader(filePath + "\\" + fileName + fileExt);
    }

    private FileReader createRefFileReader(final String fileName, final String fileExt) throws FileNotFoundException {
        return createFileReader(_refFilesRoot.getAbsolutePath() + "\\" + fileName, fileName, fileExt);
    }

    private FileReader createOutputFileReader(final String fileName, final String fileExt) throws FileNotFoundException {
        return createFileReader(_outputFilesRoot.getAbsolutePath(), fileName, fileExt);
    }

    private File getXmlDataFile(final String testCase, final String fileName) throws FileNotFoundException {
        return getFile(_dataFilesRoot.getAbsolutePath() + "\\" + testCase + "\\data", fileName, ".xml");
    }

    private XmlSchemaCollection getRefSchemas(final String fileName) throws FileNotFoundException {
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

    private XDef2XsdAdapter createXdDefAdapter() {
        XDef2XsdAdapter adapter = new XDef2XsdAdapter();
        adapter.setLogLevel(LOG_LEVEL_WARN);
        return adapter;
    }

    private XDPool2XsdAdapter createXdPoolAdapter() {
        XDPool2XsdAdapter adapter = new XDPool2XsdAdapter();
        adapter.setLogLevel(LOG_LEVEL_WARN);
        return adapter;
    }

    private void writeOutputSchemas(final XmlSchemaCollection outputSchemaCollection, final Set<String> schemaNames) {
        for (String schemaName : schemaNames) {
            XmlSchema[] outputSchemas = outputSchemaCollection.getXmlSchema(schemaName);

            assertEq(1, outputSchemas.length, "Multiple schemas of same system name: " + schemaName);

            if (WRITE_SCHEMAS_INTO_FILE == true) {
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

        if (PRINT_SCHEMA_TO_OUTPUT == true) {
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

            if (WRITE_SCHEMAS_INTO_FILE == true) {
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

    private void validateXml(final String fileName, List<String> validTestingData, List<String> invalidTestingData, boolean validateRef) throws FileNotFoundException {
        File refXsdFile = null;
        if (validateRef) {
            refXsdFile = getRefSchemaFile(fileName);
        }
        File outputXsdFile = getOutputSchemaFile(fileName);

        // Validate valid XML file against XSD schema
        if (validTestingData != null) {
            for (String testingFile : validTestingData) {
                File xmlDataFile = getXmlDataFile(fileName, testingFile);
                if (validateRef == true && VALIDATE_XML_AGAINST_REF_XSD == true) {
                    validateXml(fileName, xmlDataFile, refXsdFile, true, "ref");
                }
                if (outputXsdFile != null) {
                    validateXml(fileName, xmlDataFile, outputXsdFile, true, "out");
                }
            }
        }

        // Validate invalid XML file against XSD schema
        if (invalidTestingData != null) {
            for (String testingFile : invalidTestingData) {
                File xmlDataFile = getXmlDataFile(fileName, testingFile);
                if (validateRef == true && VALIDATE_XML_AGAINST_REF_XSD == true) {
                    validateXml(fileName, xmlDataFile, refXsdFile, false, "ref");
                }
                if (outputXsdFile != null) {
                    validateXml(fileName, xmlDataFile, outputXsdFile, false, "out");
                }
            }
        }
    }

    private void validateXml(final String fileName, final File xmlFile, final File xsdSchemaFile, boolean expectedResult, String type) {
        XmlValidator validator = new XmlValidator(new StreamSource(xmlFile), new StreamSource(xsdSchemaFile));
        assertEq(expectedResult, validator.validate(_outputFilesRoot.getAbsolutePath(), expectedResult && VALIDATE_XML_PRINT_ERRORS),
                "Xml validation failed, testCase: " + fileName + ", type: " + type + ", fileName: " + xmlFile.getName());
    }

    private void convertXdDef2Xsd(final String fileName, List<String> validTestingData, List<String> invalidTestingData) {
        convertXdDef2Xsd(fileName, validTestingData, invalidTestingData, true);
    }

    private void convertXdDef2XsdNoRef(final String fileName, List<String> validTestingData, List<String> invalidTestingData) {
        convertXdDef2Xsd(fileName, validTestingData, invalidTestingData, false);
    }

    private void convertXdDef2Xsd(final String fileName,
                                  List<String> validTestingData, List<String> invalidTestingData,
                                  boolean validateAgainstRefXsd) {
        ArrayReporter reporter = new ArrayReporter();
        setProperty("xdef.warnings", "true");
        try {
            XDef2XsdAdapter adapter = createXdDefAdapter();

            // Convert XD -> XSD Schema
            XDPool inputXD = compileXd(fileName);
            XmlSchemaCollection outputXmlSchemaCollection = adapter.createSchema(inputXD);

            // Compare output XSD schemas to XSD references
            if (validateAgainstRefXsd) {
                validateSchemas(fileName, getRefSchemas(fileName), outputXmlSchemaCollection, adapter.getSchemaNames(), 1);
            } else {
                writeOutputSchemas(outputXmlSchemaCollection, adapter.getSchemaNames());
            }

            // Validate XML files against output XSD schemas and reference XSD schemas
            validateXml(fileName, validTestingData, invalidTestingData, validateAgainstRefXsd);

            assertNoErrors(reporter);
        } catch (Exception ex) {fail(ex);}
    }

    private void convertXdPool2XsdNoRef(final String fileName, List<String> validTestingData, List<String> invalidTestingData) {
        convertXdPool2Xsd(fileName, validTestingData, invalidTestingData, null, false);
    }

    private void convertXdPool2XsdNoRef(final String fileName, List<String> validTestingData, List<String> invalidTestingData, int schemaCount) {
        convertXdPool2Xsd(fileName, validTestingData, invalidTestingData, schemaCount, false);
    }

    private void convertXdPool2Xsd(final String fileName, List<String> validTestingData, List<String> invalidTestingData) {
        convertXdPool2Xsd(fileName, validTestingData, invalidTestingData, null, true);
    }

    private void convertXdPool2Xsd(final String fileName, List<String> validTestingData, List<String> invalidTestingData, int schemaCount) {
        convertXdPool2Xsd(fileName, validTestingData, invalidTestingData, schemaCount, true);
    }

    private void convertXdPool2Xsd(final String fileName, List<String> validTestingData, List<String> invalidTestingData, Integer schemaCount, boolean validateAgainstRefXsd) {
        ArrayReporter reporter = new ArrayReporter();
        setProperty("xdef.warnings", "true");
        try {
            XDPool2XsdAdapter adapter = createXdPoolAdapter();

            // Convert XD -> XSD Schema
            XDPool inputXD = compileXd(fileName);
            XmlSchemaCollection outputXmlSchemaCollection = adapter.createSchemas(inputXD);
            int expectedShemaCount = schemaCount == null ? inputXD.getXMDefinitions().length : schemaCount;

            // Compare output XSD schemas to XSD references
            if (validateAgainstRefXsd) {
                validateSchemas(fileName, getRefSchemas(fileName), outputXmlSchemaCollection, adapter.getSchemaNames(), expectedShemaCount);
            } else {
                writeOutputSchemas(outputXmlSchemaCollection, adapter.getSchemaNames());
            }

            // Validate XML files against output XSD schemas and reference XSD schemas
            validateXml(fileName, validTestingData, invalidTestingData, validateAgainstRefXsd);

            assertNoErrors(reporter);
        } catch (Exception ex) {fail(ex);}
    }

    @Override
    public void test() {
        init();

        // ============ XDef ============


        convertXdDef2Xsd("t000", Arrays.asList(new String[] {"t000"}), Arrays.asList(new String[] {"t000_invalid_blank_char"}));
        convertXdDef2Xsd("t001", Arrays.asList(new String[] {"t001"}), null);
        convertXdDef2Xsd("t002", Arrays.asList(new String[] {"t002"}), null);
        convertXdDef2Xsd("t003", Arrays.asList(new String[] {"t003"}), null);
        convertXdDef2Xsd("t004", Arrays.asList(new String[] {"t004"}), null);
        convertXdDef2Xsd("t005", Arrays.asList(new String[] {"t005"}), null);
        convertXdDef2Xsd("t007", Arrays.asList(new String[] {"t007"}), null);
        convertXdDef2Xsd("t009", Arrays.asList(new String[] {"t009"}), null);
        convertXdDef2Xsd("t010", Arrays.asList(new String[] {"t010"}), null);
        convertXdDef2Xsd("t016", Arrays.asList(new String[] {"t016"}), Arrays.asList(new String[] {"t016e"}));

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

        // TODO: xdatetime with pattern
        //convertXdDef2XsdNoRef ("dateTimeTest", Arrays.asList(new String[] {"dateTimeTest_valid_1"}), null);
        // TODO: xdatetime with pattern
//        convertXdDef2XsdNoRef ("declarationTest",
//                Arrays.asList(new String[] {"declarationTest_valid_1", "declarationTest_valid_2", "declarationTest_valid_3"}),
//                Arrays.asList(new String[] {"declarationTest_invalid_1", "declarationTest_invalid_2", "declarationTest_invalid_3", "declarationTest_invalid_4"}));

        // TODO: fixed value
        //convertXdDef2XsdNoRef ("namespaceTest", Arrays.asList(new String[] {"namespaceTest_valid"}), null);

        convertXdDef2XsdNoRef ("namespaceTest2", Arrays.asList(new String[] {"namespaceTest2_valid_1"}), null);
        convertXdPool2XsdNoRef("namespaceTest3", Arrays.asList(new String[] {"namespaceTest3_valid_1"}), null);
        convertXdPool2XsdNoRef("namespaceTest4", Arrays.asList(new String[] {"namespaceTest4_valid_1"}), null);

        convertXdDef2XsdNoRef ("schemaTypeTest", Arrays.asList(new String[] {"schemaTypeTest_valid_1"}), null);
        convertXdDef2XsdNoRef ("simpleModelTest",
                Arrays.asList(new String[] {"simpleModelTest_valid_1", "simpleModelTest_valid_2", "simpleModelTest_valid_3", "simpleModelTest_valid_5", "simpleModelTest_valid_5"}), null);

        // ============ XDPool ============

        convertXdPool2Xsd("t011", Arrays.asList(new String[] {"t011"}), null);
        convertXdPool2Xsd("t012", Arrays.asList(new String[] {"t012", "t012_1", "t012_2"}), null);
        convertXdPool2Xsd("t013", Arrays.asList(new String[] {"t013"}), null);
        convertXdPool2Xsd("t014", Arrays.asList(new String[] {"t014"}), null);

        // TODO: unknown attr2
//        convertXdPool2Xsd("t015", Arrays.asList(new String[] {"t015", "t015_1"}), null);

        // TODO: Local and global declaration scope
        // TODO: root element as reference
        //      Top level reference
//        convertXdPool2XsdNoRef ("globalAndLocalTest",
//                Arrays.asList(new String[] {"globalAndLocalTest_X", "globalAndLocalTest_Y", "globalAndLocalTest_Z"}),
//                Arrays.asList(new String[] {"globalAndLocalTest_X_invalid", "globalAndLocalTest_Y_invalid", "globalAndLocalTest_Z_invalid"})
//        );

        // TODO: Invalid usage of namespace?
//        convertXdPool2XsdNoRef ("multiXdefTest", Arrays.asList(new String[] {"multiXdefTest_valid_1"}), null);
        
        convertXdPool2XsdNoRef ("simpleRefTest", Arrays.asList(new String[] {"simpleRefTest_valid_1"}), null);

        convertXdPool2XsdNoRef ("refTest1", Arrays.asList(new String[] {"refTest1_valid_1"}), null);
        // TODO: Reference from complex type to no namespace root element
        //convertXdPool2XsdNoRef ("refTest2", Arrays.asList(new String[] {"refTest2_valid_1"}), null);
        convertXdPool2XsdNoRef ("refTest3", Arrays.asList(new String[] {"refTest3_valid_1"}), null);
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
