package org.xdef.impl.util.conv.xd2schemas.xsd.model;

import java.util.Arrays;

public class XmlSchemaImportLocation {
    private String namespaceUri;
    private String path;
    /**
     * If fileName is not set, then building of XML namespace location will use name of referenced x-definition
     */
    private String fileName;
    private String fileExt = ".xsd";

    public XmlSchemaImportLocation(String namespaceUri) {
        this.namespaceUri = namespaceUri;
    }

    public XmlSchemaImportLocation(String namespaceUri, String path) {
        this.namespaceUri = namespaceUri;
        this.path = path;
    }

    public XmlSchemaImportLocation(String namespaceUri, String path, String fileName) {
        this.namespaceUri = namespaceUri;
        this.path = path;
        this.fileName = fileName;
    }

    public String getNamespaceUri() {
        return namespaceUri;
    }

    public void setNamespaceUri(String namespaceUri) {
        this.namespaceUri = namespaceUri;
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

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileExt() {
        return fileExt;
    }

    public void setFileExt(String fileExt) {
        this.fileExt = fileExt;
    }

    public String buildLocalition(final String schemaName) {
        String res = "";
        if (path != null && !path.trim().isEmpty()) {
            res += path + "\\";
        }

        if (fileName != null) {
            res += fileName;
        } else if (schemaName != null) {
            res += schemaName;
        } else {
            throw new RuntimeException("Unknown reference file! schemaName: " + schemaName + ", namespaceUri: " + namespaceUri);
        }

        res += fileExt;

        return res;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        XmlSchemaImportLocation that = (XmlSchemaImportLocation) o;
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
