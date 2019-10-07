package test.xdutils;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.apache.ws.commons.schema.constants.Constants;
import org.xdef.XDPool;
import org.xdef.XDValue;
import org.xdef.impl.util.conv.xd2schemas.xsd.XD2MultipleXsdAdapter;
import org.xdef.impl.util.conv.xd2schemas.xsd.XD2XsdAdapter;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.XmlSchemaImportLocation;
import org.xdef.impl.util.conv.xsd.XmlValidator;
import org.xdef.proc.XXElement;
import org.xdef.sys.ArrayReporter;
import test.utils.XDTester;

import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.*;

public class TestXd2Xsd extends XDTester {

    static private boolean PRINT_SCHEMA_TO_OUTPUT = false;
    static private boolean WRITE_SCHEMAS_INTO_FILE = true;
    static private boolean VALIDATE_XML_AGAINST_REF_XSD = true;

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

    private XD2XsdAdapter createXsdAdapter(XmlSchemaForm elemSchemaForm, XmlSchemaForm attrSchemaForm, String targetNamespace) {
        XD2XsdAdapter adapter = new XD2XsdAdapter();
        //adapter.setPrintXdTree(true);
        adapter.setElemSchemaForm(elemSchemaForm);
        adapter.setAttrSchemaForm(attrSchemaForm);
        adapter.setTargetNamespace(targetNamespace);

        return adapter;
    }

    private XD2MultipleXsdAdapter createXsdAdapterMultiple(XmlSchemaForm[] elemSchemaForms,
                                                           XmlSchemaForm[] attrSchemaForms,
                                                           String[] targetNamespaces,
                                                           XmlSchemaImportLocation[] schemaImportLocations) {
        XD2MultipleXsdAdapter adapter = new XD2MultipleXsdAdapter();
        //adapter.setPrintXdTree(true);

        Map<String, XmlSchemaImportLocation> schemaNamespaceLocations = new HashMap<String, XmlSchemaImportLocation>();
        for (int i = 0; i < schemaImportLocations.length; i++) {
            schemaNamespaceLocations.put(schemaImportLocations[i].getNamespaceUri(), schemaImportLocations[i]);
        }
        adapter.setSchemaNamespaceLocations(schemaNamespaceLocations);

        for (int i = 0; i < elemSchemaForms.length; i++) {
            adapter.setElemSchemaForm(i, elemSchemaForms[i]);
        }

        for (int i = 0; i < attrSchemaForms.length; i++) {
            adapter.setAttrSchemaForm(i, attrSchemaForms[i]);
        }

        for (int i = 0; i < targetNamespaces.length; i++) {
            adapter.setTargetNamespace(i, targetNamespaces[i]);
        }

        return adapter;
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

    private void validateXml(final String fileName, List<String> validTestingData, List<String> invalidTestingData) throws FileNotFoundException {
        File refXsdFile = getRefSchemaFile(fileName);
        File outputXsdFile = getOutputSchemaFile(fileName);

        // Validate valid XML file against XSD schema
        if (validTestingData != null) {
            for (String testingFile : validTestingData) {
                File xmlDataFile = getXmlDataFile(fileName, testingFile);
                if (VALIDATE_XML_AGAINST_REF_XSD == true) {
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
                if (VALIDATE_XML_AGAINST_REF_XSD == true) {
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
        assertEq(expectedResult, validator.validate(_outputFilesRoot.getAbsolutePath()),
                "Xml validation failed, testCase: " + fileName + ", type: " + type + ", fileName: " + xmlFile.getName());
    }

    private void convertXd2Xsd(final String fileName,
                               List<String> validTestingData, List<String> invalidTestingData,
                               XmlSchemaForm elemSchemaForm, XmlSchemaForm attrSchemaForm) {
        convertXd2Xsd(fileName, validTestingData, invalidTestingData, elemSchemaForm, attrSchemaForm, null);
    }

    private void convertXd2Xsd(final String fileName,
                               List<String> validTestingData, List<String> invalidTestingData,
                               XmlSchemaForm elemSchemaForm, XmlSchemaForm attrSchemaForm, String targetNamespace) {
        ArrayReporter reporter = new ArrayReporter();
        setProperty("xdef.warnings", "true");
        try {
            XD2XsdAdapter adapter = createXsdAdapter(elemSchemaForm, attrSchemaForm, targetNamespace);

            // Convert XD -> XSD Schema
            XDPool inputXD = compileXd(fileName);
            XmlSchemaCollection outputXmlSchemaCollection = adapter.createSchema(inputXD).getParent();

            // Compare output XSD schemas to XSD references
            validateSchemas(fileName, getRefSchemas(fileName), outputXmlSchemaCollection, new HashSet<String>(Arrays.asList(adapter.getSchemaName())), 1);

            // Validate XML files against output XSD schemas and reference XSD schemas
            validateXml(fileName, validTestingData, invalidTestingData);

            assertNoErrors(reporter);
        } catch (Exception ex) {fail(ex);}
    }

    private void convertXd2Xsd_Multiple(final String fileName,
                                        List<String> validTestingData, List<String> invalidTestingData,
                                        XmlSchemaForm[] elemSchemaForms, XmlSchemaForm[] attrSchemaForms, String[] targetNamespaces,
                                        XmlSchemaImportLocation[] schemaImportLocations, int schemaCount) {
        ArrayReporter reporter = new ArrayReporter();
        setProperty("xdef.warnings", "true");
        try {
            XD2MultipleXsdAdapter adapter = createXsdAdapterMultiple(elemSchemaForms, attrSchemaForms, targetNamespaces, schemaImportLocations);

            // Convert XD -> XSD Schema
            XDPool inputXD = compileXd(fileName);
            XmlSchemaCollection outputXmlSchemaCollection = adapter.createSchemas(inputXD);

            // Compare output XSD schemas to XSD references
            validateSchemas(fileName, getRefSchemas(fileName), outputXmlSchemaCollection, adapter.getSchemaNames(), schemaCount);

            // Validate XML files against output XSD schemas and reference XSD schemas
            validateXml(fileName, validTestingData, invalidTestingData);

            assertNoErrors(reporter);
        } catch (Exception ex) {fail(ex);}
    }

    @Override
    public void test() {
        init();



        convertXd2Xsd("t000", Arrays.asList(new String[] {"t000"}), Arrays.asList(new String[] {"t000_invalid_blank_char"}), XmlSchemaForm.UNQUALIFIED, XmlSchemaForm.UNQUALIFIED);
        convertXd2Xsd("t001", Arrays.asList(new String[] {"t001"}), null, XmlSchemaForm.UNQUALIFIED, XmlSchemaForm.UNQUALIFIED);
        convertXd2Xsd("t002", Arrays.asList(new String[] {"t002"}), null, XmlSchemaForm.UNQUALIFIED, XmlSchemaForm.UNQUALIFIED);
        convertXd2Xsd("t003", Arrays.asList(new String[] {"t003"}), null, XmlSchemaForm.UNQUALIFIED, XmlSchemaForm.UNQUALIFIED);
        convertXd2Xsd("t004", Arrays.asList(new String[] {"t004"}), null, XmlSchemaForm.UNQUALIFIED, XmlSchemaForm.UNQUALIFIED);
        convertXd2Xsd("t005", Arrays.asList(new String[] {"t005"}), null, XmlSchemaForm.UNQUALIFIED, XmlSchemaForm.UNQUALIFIED);
        convertXd2Xsd("t007", Arrays.asList(new String[] {"t007"}), null, XmlSchemaForm.QUALIFIED, XmlSchemaForm.UNQUALIFIED, "http://www.w3schools.com");
        convertXd2Xsd("t009", Arrays.asList(new String[] {"t009"}), null, XmlSchemaForm.QUALIFIED, XmlSchemaForm.UNQUALIFIED, "http://www.w3schools.com");
        convertXd2Xsd("t010", Arrays.asList(new String[] {"t010"}), null, XmlSchemaForm.QUALIFIED, XmlSchemaForm.QUALIFIED, "http://www.w3schools.com");
        convertXd2Xsd("t016", Arrays.asList(new String[] {"t016"}), Arrays.asList(new String[] {"t016e"}), XmlSchemaForm.UNQUALIFIED, XmlSchemaForm.UNQUALIFIED);

        // ============ References ============

        convertXd2Xsd_Multiple("t011", Arrays.asList(new String[] {"t011"}), null,
                new XmlSchemaForm[] {XmlSchemaForm.QUALIFIED, XmlSchemaForm.QUALIFIED}, new XmlSchemaForm[] {XmlSchemaForm.UNQUALIFIED, XmlSchemaForm.UNQUALIFIED},
                new String[] {"http://www.w3ctest.com", "http://www.w3schools.com"},
                new XmlSchemaImportLocation[] {
                        new XmlSchemaImportLocation("http://www.w3ctest.com")
                },
                2);

        convertXd2Xsd_Multiple("t012", Arrays.asList(new String[] {"t012", "t012_1", "t012_2"}), null,
                new XmlSchemaForm[] {XmlSchemaForm.QUALIFIED, XmlSchemaForm.UNQUALIFIED}, new XmlSchemaForm[] {XmlSchemaForm.UNQUALIFIED, XmlSchemaForm.UNQUALIFIED},
                new String[] {"http://a", null},
                new XmlSchemaImportLocation[] {
                        new XmlSchemaImportLocation("http://a")
                },
                2);

        convertXd2Xsd_Multiple("t013", Arrays.asList(new String[] {"t013"}), null,
                new XmlSchemaForm[] {XmlSchemaForm.QUALIFIED, XmlSchemaForm.QUALIFIED}, new XmlSchemaForm[] {XmlSchemaForm.UNQUALIFIED, XmlSchemaForm.UNQUALIFIED},
                new String[] {"http://b", "http://a"},
                new XmlSchemaImportLocation[] {
                        new XmlSchemaImportLocation("http://a")
                },
                2);

        convertXd2Xsd_Multiple("t014", Arrays.asList(new String[] {"t014"}), null,
                new XmlSchemaForm[] {XmlSchemaForm.QUALIFIED, XmlSchemaForm.QUALIFIED, XmlSchemaForm.QUALIFIED, XmlSchemaForm.QUALIFIED},
                new XmlSchemaForm[] {XmlSchemaForm.UNQUALIFIED, XmlSchemaForm.UNQUALIFIED, XmlSchemaForm.UNQUALIFIED, XmlSchemaForm.UNQUALIFIED},
                new String[] {"http://d", "http://c", "http://b", "http://a"},
                new XmlSchemaImportLocation[] {
                        new XmlSchemaImportLocation("http://a"),
                        new XmlSchemaImportLocation("http://c"),
                        new XmlSchemaImportLocation("http://d")
                },
                4);

        convertXd2Xsd_Multiple("t015", Arrays.asList(new String[] {"t015", "t015_1"}), null,
                new XmlSchemaForm[] {XmlSchemaForm.QUALIFIED, XmlSchemaForm.QUALIFIED, XmlSchemaForm.QUALIFIED, XmlSchemaForm.QUALIFIED},
                new XmlSchemaForm[] {XmlSchemaForm.QUALIFIED, XmlSchemaForm.QUALIFIED, XmlSchemaForm.QUALIFIED, XmlSchemaForm.QUALIFIED},
                new String[] {"http://a", "http://d", "http://b", "http://c"},
                new XmlSchemaImportLocation[] {
                        new XmlSchemaImportLocation("http://a"),
                        new XmlSchemaImportLocation("http://b"),
                        new XmlSchemaImportLocation("http://c"),
                        new XmlSchemaImportLocation("http://d")
                },
                4);
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
