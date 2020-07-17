package org.xdef.util.schema;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class XdefToXsdOptionDefs {

    public final static Options cli() {
        Options options = new Options();

        Option input = new Option("i", XdefToXsdOptions.INPUT_DIR, true, "input directory path containing x-definition file(s)");
        input.setRequired(true);
        options.addOption(input);

        Option outputDir = new Option("o", XdefToXsdOptions.OUTPUT_DIR, true, "output directory path");
        outputDir.setRequired(true);
        options.addOption(outputDir);

        Option xDefRootModel = new Option("r", XdefToXsdOptions.INPUT_ROOT, true, "name of root x-definition (root schema of output XSD collection will be named same way)");
        xDefRootModel.setRequired(true);
        options.addOption(xDefRootModel);

        Option schemaPrefix = new Option("sp", XdefToXsdOptions.OUTPUT_FILE_PREFIX, true, "prefix of schema output file");
        schemaPrefix.setRequired(false);
        options.addOption(schemaPrefix);

        Option schemaFileExt = new Option("se", XdefToXsdOptions.OUTPUT_EXT, true, "extension of schema output file");
        schemaFileExt.setRequired(false);
        options.addOption(schemaFileExt);

        Option validatePos = new Option("tp", XdefToXsdOptions.VALIDATE_POSITIVE, true, "testing data paths (expected positive result)");
        validatePos.setRequired(false);
        validatePos.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(validatePos);

        Option validateNeg = new Option("tn", XdefToXsdOptions.VALIDATE_NEGATIVE, true, "testing data paths (expected negative result)");
        validateNeg.setRequired(false);
        validateNeg.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(validateNeg);

        Option verbose = new Option("v", XdefToXsdOptions.VERBOSE, true, "verbose mode (values: [0,5]; default: 3; no-verbose: 0)");
        verbose.setRequired(false);
        options.addOption(verbose);

        Option defaultFeatures = new Option("nf", XdefToXsdOptions.NO_DEFAULT_FEATURES, false, "do not use default transformation features");
        defaultFeatures.setRequired(false);
        options.addOption(defaultFeatures);

        Option features = new Option("f", XdefToXsdOptions.FEATURES, true, "transformation features\n" +
                XdefToXsdOptions.F_XSD_ANNOTATION + " - XSD_ANNOTATION,\n" +
                XdefToXsdOptions.F_XSD_DECIMAL_ANY_SEPARATOR + " - XSD_DECIMAL_ANY_SEPARATOR,\n" +
                XdefToXsdOptions.F_XSD_ALL_UNBOUNDED + " - XSD_ALL_UNBOUNDED,\n" +
                XdefToXsdOptions.F_XSD_NAME_COLISSION_DETECTOR + " - XSD_NAME_COLISSION_DETECTOR,\n" +
                XdefToXsdOptions.F_POSTPROCESSING + " - POSTPROCESSING,\n" +
                XdefToXsdOptions.F_POSTPROCESSING_EXTRA_SCHEMAS + " - POSTPROCESSING_EXTRA_SCHEMAS,\n" +
                XdefToXsdOptions.F_POSTPROCESSING_REFS + " - POSTPROCESSING_REFS,\n" +
                XdefToXsdOptions.F_POSTPROCESSING_QNAMES + " - POSTPROCESSING_QNAMES,\n" +
                XdefToXsdOptions.F_POSTPROCESSING_ALL_TO_CHOICE + " - POSTPROCESSING_ALL_TO_CHOICE,\n" +
                XdefToXsdOptions.F_POSTPROCESSING_MIXED + " - POSTPROCESSING_MIXED,\n" +
                XdefToXsdOptions.F_POSTPROCESSING_UNIQUE + " - POSTPROCESSING_KEYS_AND_REFS");
        features.setRequired(false);
        features.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(features);

        return options;
    }

    public final static Options features() {
        Options options = new Options();

        addFeatureOption(XdefToXsdOptions.F_XSD_ANNOTATION, "annotation", options);
        addFeatureOption(XdefToXsdOptions.F_XSD_DECIMAL_ANY_SEPARATOR, "choiceUnbounded", options);
        addFeatureOption(XdefToXsdOptions.F_XSD_ALL_UNBOUNDED, "nameCollision", options);
        addFeatureOption(XdefToXsdOptions.F_XSD_NAME_COLISSION_DETECTOR, "nameCollision", options);

        addFeatureOption(XdefToXsdOptions.F_POSTPROCESSING, "postprocessing", options);
        addFeatureOption(XdefToXsdOptions.F_POSTPROCESSING_EXTRA_SCHEMAS, "postprocessingExtraSchema", options);
        addFeatureOption(XdefToXsdOptions.F_POSTPROCESSING_REFS, "postprocessingReference", options);
        addFeatureOption(XdefToXsdOptions.F_POSTPROCESSING_QNAMES, "postprocessingQNames",  options);
        addFeatureOption(XdefToXsdOptions.F_POSTPROCESSING_ALL_TO_CHOICE, "postprocessingAll2Choice", options);
        addFeatureOption(XdefToXsdOptions.F_POSTPROCESSING_MIXED, "postprocessingMixed", options);
        addFeatureOption(XdefToXsdOptions.F_POSTPROCESSING_UNIQUE, "postprocessingUnique", options);

        return options;
    }

    private static void addFeatureOption(final String opt, final String longOpt, final Options options) {
        Option pp = new Option(opt, longOpt, false, "");
        pp.setRequired(false);
        options.addOption(pp);
    }
}
