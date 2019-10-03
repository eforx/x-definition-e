package test.xdutils;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.xdef.XDPool;
import org.xdef.XDValue;
import org.xdef.impl.util.conv.xd2schemas.XD2XsdAdapter;
import org.xdef.impl.util.conv.xsd.XmlValidator;
import org.xdef.proc.XXElement;
import org.xdef.sys.ArrayReporter;
import test.utils.XDTester;

import javax.xml.transform.stream.StreamSource;
import java.io.*;

public class TestXd2Xsd extends XDTester {

    private File _inputFilesRoot;
    private File _refFilesRoot;
    private File _dataFilesRoot;

    private void init() {
        File dataDir = new File(getDataDir());
        if (!dataDir.exists() || !dataDir.isDirectory()) {
            throw new RuntimeException(
                    "Data directory does not exists or is not a directory!");
        }

        _inputFilesRoot = initFolder(dataDir, "xd2xsd2/input");
        _refFilesRoot = initFolder(dataDir, "xd2xsd2/ref");
        _dataFilesRoot = initFolder(dataDir, "xd2xsd2/data");
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
            throw new FileNotFoundException();
        }

        return xdFile;
    }

    private XDPool compileXd(final String fileName) throws FileNotFoundException {
        return compile(getFile(_inputFilesRoot.getAbsolutePath(), fileName, ".xdef"), this.getClass());
    }

    private FileReader createFileReader(final String filePath, final String fileName, final String fileExt) throws FileNotFoundException {
        return new FileReader(filePath + "\\" + fileName + fileExt);
    }

    private FileReader createRefFileReader(final String fileName, final String fileExt) throws FileNotFoundException {
        return createFileReader(_refFilesRoot.getAbsolutePath(), fileName, fileExt);
    }

    private File getXmlDataFile(final String fileName) throws FileNotFoundException {
        return getFile(_dataFilesRoot.getAbsolutePath(), fileName, ".xml");
    }

    private XmlSchema getRefSchema(final String fileName) throws FileNotFoundException {
        XmlSchemaCollection schemaCollection = new XmlSchemaCollection();
        schemaCollection.read(createRefFileReader(fileName, ".xsd"));
        XmlSchema[] schemas = schemaCollection.getXmlSchemas();

        if (schemas.length != 2) {
            throw new RuntimeException("Invalid number of reference schemas, expected: 2, actual: " + schemas.length);
        }

        return schemas[0];
    }

    private ByteArrayOutputStream compareSchemas(XmlSchema ref, XmlSchema output) throws UnsupportedEncodingException {
        ByteArrayOutputStream outputStreamRef = new ByteArrayOutputStream();
        ref.write(outputStreamRef);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        output.write(outputStream);

        assertEq(outputStream.toString(), outputStreamRef.toString());

        return outputStream;
    }

    private void validateXml(final File xmlFile, final ByteArrayOutputStream xsdStream) throws FileNotFoundException {
        XmlValidator validator = new XmlValidator(new StreamSource(xmlFile), new StreamSource(new ByteArrayInputStream(xsdStream.toByteArray())));
        assertTrue(validator.validate());
    }

    private void convertXd2Xsd(final String fileName) {
        ArrayReporter reporter = new ArrayReporter();
        setProperty("xdef.warnings", "true");
        try {
            XD2XsdAdapter adapter = new XD2XsdAdapter();
            // Convert XD -> XSD Schema
            XDPool inputXD = compileXd(fileName);
            XmlSchema outputSchema = adapter.createSchema(inputXD, fileName);
            outputSchema.write(System.out);

            // Compare XSD schemas
            ByteArrayOutputStream outputSchemaStream = compareSchemas(getRefSchema(fileName), outputSchema);

            // Validate XML file against XSD schema
            validateXml(getXmlDataFile(fileName), outputSchemaStream);

            assertNoErrors(reporter);
        } catch (Exception ex) {fail(ex);}
    }

    @Override
    public void test() {
        init();

        convertXd2Xsd("t000");
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
