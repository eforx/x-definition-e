package org.xdef.impl.util.conv.schema.xd2schema.xsd.model;

import org.xdef.msg.XSD;
import org.xdef.sys.SRuntimeException;

import java.util.Arrays;

/**
 * Definition of XSD document import.
 *
 * Source data model for creating XSD xs:import node.
 */
public class XsdSchemaImportLocation {

    /**
     * XSD document namespace URI
     */
    private String namespaceUri;

    /**
     * XSD document path
     */
    private String path;

    /**
     * XSD document file name
     */
    private String fileName;

    /**
     * XSD document file extension
     */
    private String fileExt = ".xsd";

    public XsdSchemaImportLocation(String namespaceUri, String fileName) {
        this.namespaceUri = namespaceUri;
        this.fileName = fileName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    /**
     * Creates XSD import path based on internal variable state
     * @param schemaName    XSD document name which will be used if fileName is not set
     * @return XSD document import path
     */
    public String buildLocation(final String schemaName) {
        String res = "";
        if (path != null && !path.trim().isEmpty()) {
            res += path + "\\";
        }

        if (fileName != null) {
            res += fileName;
        } else if (schemaName != null) {
            res += schemaName;
        } else {
            throw new SRuntimeException(XSD.XSD008, schemaName, namespaceUri);
        }

        res += fileExt;

        return res;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        XsdSchemaImportLocation that = (XsdSchemaImportLocation) o;
        return equals(namespaceUri, that.namespaceUri) &&
                equals(path, that.path) &&
                equals(fileName, that.fileName) &&
                equals(fileExt, that.fileExt);
    }

    @Override
    public int hashCode() {
        return hash(namespaceUri, path, fileName, fileExt);
    }

    private static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    private static int hash(Object... values) {
        return Arrays.hashCode(values);
    }
}
