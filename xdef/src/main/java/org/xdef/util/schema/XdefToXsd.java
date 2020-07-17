package org.xdef.util.schema;

import org.apache.commons.cli.*;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.xdef.impl.util.conv.schema.util.SchemaLoggerDefs;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.Xd2XsdFeature;

import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class XdefToXsd {

    private static EnumSet<Xd2XsdFeature> parseFeatures(String[] features) {
        Set<Xd2XsdFeature> featureSet = new HashSet<Xd2XsdFeature>();

        if (features == null || features.length == 0) {
            return null;
        }

        CommandLineParser featureParser = new DefaultParser();
        CommandLine cmd = null;

        for (int i = 0; i < features.length; i++) {
            if (features[i].length() < 3) {
                features[i] = "-" + features[i];
            } else {
                features[i] = "--" + features[i];
            }
        }

        try {
            cmd = featureParser.parse(XdefToXsdOptionDefs.features(), features, true);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }

        if (cmd != null) {
            if (cmd.hasOption(XdefToXsdOptions.F_XSD_ANNOTATION)) { featureSet.add(Xd2XsdFeature.XSD_ANNOTATION); }
            if (cmd.hasOption(XdefToXsdOptions.F_XSD_DECIMAL_ANY_SEPARATOR)) { featureSet.add(Xd2XsdFeature.XSD_DECIMAL_ANY_SEPARATOR); }
            if (cmd.hasOption(XdefToXsdOptions.F_XSD_ALL_UNBOUNDED)) { featureSet.add(Xd2XsdFeature.XSD_ALL_UNBOUNDED); }
            if (cmd.hasOption(XdefToXsdOptions.F_XSD_NAME_COLISSION_DETECTOR)) { featureSet.add(Xd2XsdFeature.XSD_NAME_COLISSION_DETECTOR); }

            if (cmd.hasOption(XdefToXsdOptions.F_POSTPROCESSING)) { featureSet.add(Xd2XsdFeature.POSTPROCESSING); }
            if (cmd.hasOption(XdefToXsdOptions.F_POSTPROCESSING_EXTRA_SCHEMAS)) { featureSet.add(Xd2XsdFeature.POSTPROCESSING_EXTRA_SCHEMAS); }
            if (cmd.hasOption(XdefToXsdOptions.F_POSTPROCESSING_REFS)) { featureSet.add(Xd2XsdFeature.POSTPROCESSING_REFS); }
            if (cmd.hasOption(XdefToXsdOptions.F_POSTPROCESSING_QNAMES)) { featureSet.add(Xd2XsdFeature.POSTPROCESSING_QNAMES); }
            if (cmd.hasOption(XdefToXsdOptions.F_POSTPROCESSING_ALL_TO_CHOICE)) { featureSet.add(Xd2XsdFeature.POSTPROCESSING_ALL_TO_CHOICE); }
            if (cmd.hasOption(XdefToXsdOptions.F_POSTPROCESSING_MIXED)) { featureSet.add(Xd2XsdFeature.POSTPROCESSING_MIXED); }
            if (cmd.hasOption(XdefToXsdOptions.F_POSTPROCESSING_UNIQUE)) { featureSet.add(Xd2XsdFeature.POSTPROCESSING_UNIQUE); }
        }

        if (featureSet.isEmpty()) {
            return null;
        }

        return EnumSet.copyOf(featureSet);
    }

    private static int getVerboseLevel(final String verbose) {
        int res = SchemaLoggerDefs.LOG_INFO;
        try {
            Integer verboseLevel = Integer.valueOf(verbose);
            if (verboseLevel < SchemaLoggerDefs.LOG_NONE || verboseLevel > SchemaLoggerDefs.LOG_TRACE) {
                System.out.println("Unknown verbose level, use default: " + SchemaLoggerDefs.LOG_INFO + " (info)");
            } else {
                res = verboseLevel;
            }
        } catch (NumberFormatException e) {
            // Do nothing
        }

        return res;
    }

    private static void checkPaths(final CommandLine cmd) {
        File f = new File(cmd.getOptionValue(XdefToXsdOptions.INPUT_DIR));
        if (!f.exists() || !f.isDirectory()) {
            throw new RuntimeException("Input directory \"" + cmd.getOptionValue(XdefToXsdOptions.INPUT_DIR) + "\" does not exist!");
        }

        f = new File(cmd.getOptionValue(XdefToXsdOptions.OUTPUT_DIR));
        if (!f.exists() || !f.isDirectory()) {
            throw new RuntimeException("Output directory \"" + cmd.getOptionValue(XdefToXsdOptions.OUTPUT_DIR) + "\" does not exist!");
        }

        if (cmd.hasOption(XdefToXsdOptions.VALIDATE_POSITIVE)) {
            for (String s : cmd.getOptionValues(XdefToXsdOptions.VALIDATE_POSITIVE)) {
                f = new File(s);
                if (!f.exists() || !f.isFile()) {
                    throw new RuntimeException("Input testing (positive) data file \"" + s + "\" does not exist!");
                }
            }
        }

        if (cmd.hasOption(XdefToXsdOptions.VALIDATE_NEGATIVE)) {
            for (String s : cmd.getOptionValues(XdefToXsdOptions.VALIDATE_NEGATIVE)) {
                f = new File(s);
                if (!f.exists() || !f.isFile()) {
                    throw new RuntimeException("Input testing (negative) data file \"" + s + "\" does not exist!");
                }
            }
        }
    }

    private static XdefAdapterConfig createConfig(final CommandLine cmd) {
        XdefAdapterConfig config = new XdefAdapterConfig();

        config.setInputDirectory(cmd.getOptionValue(XdefToXsdOptions.INPUT_DIR));
        config.setOutputDirectory(cmd.getOptionValue(XdefToXsdOptions.OUTPUT_DIR));
        config.setInputRoot(cmd.getOptionValue(XdefToXsdOptions.INPUT_ROOT));
        if (cmd.hasOption(XdefToXsdOptions.OUTPUT_FILE_PREFIX)) {
            config.setOutputFilePrefix(cmd.getOptionValue(XdefToXsdOptions.OUTPUT_FILE_PREFIX));
        }

        if (cmd.hasOption(XdefToXsdOptions.OUTPUT_EXT)) {
            config.setOutputFileExt(cmd.getOptionValue(XdefToXsdOptions.OUTPUT_EXT));
        }

        if (cmd.hasOption(XdefToXsdOptions.NO_DEFAULT_FEATURES)) {
            config.setUseDefaultFeatures(false);
        }

        if (cmd.hasOption(XdefToXsdOptions.FEATURES)) {
            config.setFeatures(parseFeatures(cmd.getOptionValues(XdefToXsdOptions.FEATURES)));
        }

        if (cmd.hasOption(XdefToXsdOptions.VALIDATE_POSITIVE)) {
            config.setTestingDataPos(Arrays.asList(cmd.getOptionValues(XdefToXsdOptions.VALIDATE_POSITIVE)));
        }

        if (cmd.hasOption(XdefToXsdOptions.VALIDATE_NEGATIVE)) {
            config.setTestingDataNeg(Arrays.asList(cmd.getOptionValues(XdefToXsdOptions.VALIDATE_NEGATIVE)));
        }

        if (cmd.hasOption(XdefToXsdOptions.VERBOSE)) {
            config.setVerbose(getVerboseLevel(cmd.getOptionValue(XdefToXsdOptions.VERBOSE)));
        }

        if (config.getVerbose() >= SchemaLoggerDefs.LOG_INFO) {
            System.out.println("Input configuration");
            System.out.println(config);
        }
        return config;
    }

    public final static XmlSchemaCollection transform(final XdefAdapterConfig config) {
        final XdefAdapter adapter = new XdefAdapter(config);
        return adapter.transform();
    }

    public final static void main(String... args) {

        final Options options = XdefToXsdOptionDefs.cli();
        final CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            new HelpFormatter().printHelp("X-definition to XSD converter", options);

            System.exit(1);
            return;
        }

        checkPaths(cmd);
        final XdefAdapterConfig config = createConfig(cmd);
        transform(config);
    }
}
