package test.xdutils;

import buildtools.XDTester;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public abstract class TesterXdSchema extends XDTester {

    static protected boolean PRINT_OUTPUT_TO_CONSOLE = false;
    static protected boolean WRITE_OUTPUT_INTO_FILE = true;
    static protected boolean VALIDATE_XML_AGAINST_REF_FILE = true;
    static protected boolean PRINT_XML_VALIDATION_ERRORS = true;

    protected File _inputFilesRoot;
    protected File _refFilesRoot;
    protected File _dataFilesRoot;
    protected File _outputFilesRoot;

    protected File initFolder(final File dataDir, final String folderPath) {
        File folder = new File(dataDir.getAbsolutePath(), folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            throw new RuntimeException("Directory " + folderPath + " does not exists!");
        }

        return folder;
    }

    protected File getFile(final String path, final String fileName, final String fileExt) throws FileNotFoundException {
        File xdFile = new File(path, fileName + fileExt);
        if (xdFile == null || !xdFile.exists() || !xdFile.isFile()) {
            throw new FileNotFoundException("Path: " + path + "\\" + fileName + fileExt);
        }

        return xdFile;
    }

    protected FileReader createFileReader(final String filePath, final String fileName, final String fileExt) throws FileNotFoundException {
        return new FileReader(filePath + "\\" + fileName + fileExt);
    }

    protected FileReader createInputFileReader(final String fileName, final String fileExt) throws FileNotFoundException {
        return createFileReader(_inputFilesRoot.getAbsolutePath() + "\\" + fileName, fileName, fileExt);
    }

    protected FileReader createRefFileReader(final String fileName, final String fileExt) throws FileNotFoundException {
        return createFileReader(_refFilesRoot.getAbsolutePath() + "\\" + fileName, fileName, fileExt);
    }

    protected FileReader createOutputFileReader(final String fileName, final String fileExt) throws FileNotFoundException {
        return createFileReader(_outputFilesRoot.getAbsolutePath(), fileName, fileExt);
    }

    protected File getXmlDataFile(final String testCase, final String fileName) throws FileNotFoundException {
        return getFile(_dataFilesRoot.getAbsolutePath() + "\\" + testCase + "\\data", fileName, ".xml");
    }
}
