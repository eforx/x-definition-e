package org.xdef.util.schema;

import org.xdef.impl.util.conv.schema.util.SchemaLoggerDefs;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.Xd2XsdFeature;

import java.util.EnumSet;
import java.util.List;

public class XdefAdapterConfig {
    private String inputFileName;
    private String inputRoot;
    private String outputDirectory;
    private String outputFileExt = ".xsd";
    private String outputFilePrefix = "";
    private List<String> testingDataPos;
    private List<String> testingDataNeg;

    private int verbose = SchemaLoggerDefs.LOG_INFO;

    private boolean useDefaultFeatures = true;
    EnumSet<Xd2XsdFeature> features;

    public String getInputFileName() {
        return inputFileName;
    }

    public void setInputFileName(String inputFileName) {
        this.inputFileName = inputFileName;
    }

    public String getInputRoot() {
        return inputRoot;
    }

    public void setInputRoot(String inputRoot) {
        this.inputRoot = inputRoot;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String getOutputFileExt() {
        return outputFileExt;
    }

    public void setOutputFileExt(String outputFileExt) {
        this.outputFileExt = outputFileExt;
    }

    public String getOutputFilePrefix() {
        return outputFilePrefix;
    }

    public void setOutputFilePrefix(String outputFilePrefix) {
        this.outputFilePrefix = outputFilePrefix;
    }

    public List<String> getTestingDataPos() {
        return testingDataPos;
    }

    public void setTestingDataPos(List<String> testingDataPos) {
        this.testingDataPos = testingDataPos;
    }

    public List<String> getTestingDataNeg() {
        return testingDataNeg;
    }

    public void setTestingDataNeg(List<String> testingDataNeg) {
        this.testingDataNeg = testingDataNeg;
    }

    public int getVerbose() {
        return verbose;
    }

    public void setVerbose(int verbose) {
        this.verbose = verbose;
    }

    public boolean useDefaultFeatures() {
        return useDefaultFeatures;
    }

    public void setUseDefaultFeatures(boolean useDefaultFeatures) {
        this.useDefaultFeatures = useDefaultFeatures;
    }

    public EnumSet<Xd2XsdFeature> getFeatures() {
        return features;
    }

    public void setFeatures(EnumSet<Xd2XsdFeature> features) {
        this.features = features;
    }

    public boolean hasTestingData() {
        return hasPositiveTestingData() || hasNegativeTestingData();
    }

    public boolean hasPositiveTestingData() {
        return getTestingDataPos() != null && !getTestingDataPos().isEmpty();
    }

    public boolean hasNegativeTestingData() {
        return getTestingDataNeg() != null && !getTestingDataNeg().isEmpty();
    }

    @Override
    public String toString() {
        return "XdefAdapterConfig{" +
                "inputFileName='" + inputFileName + '\'' +
                ", inputRootModel='" + inputRoot + '\'' +
                ", outputDirectory='" + outputDirectory + '\'' +
                ", outputFileExt='" + outputFileExt + '\'' +
                ", outputFilePrefix='" + outputFilePrefix + '\'' +
                ", testingDataPos=" + testingDataPos +
                ", testingDataNeg=" + testingDataNeg +
                ", verbose=" + verbose +
                ", useDefaultFeatures=" + useDefaultFeatures +
                ", features=" + features +
                '}';
    }
}
