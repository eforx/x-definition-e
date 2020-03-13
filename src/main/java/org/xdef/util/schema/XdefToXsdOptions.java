package org.xdef.util.schema;

public interface XdefToXsdOptions {
    String INPUT = "input";
    String INPUT_ROOT = "root";

    String OUTPUT_DIR = "outputDir";
    String OUTPUT_FILE_PREFIX = "schemaPrefix";
    String OUTPUT_EXT = "schemaExt";

    String VALIDATE_POSITIVE = "validatePos";
    String VALIDATE_NEGATIVE = "validateNeg";

    String VERBOSE = "verbose";

    String NO_DEFAULT_FEATURES = "noFeatures";
    String FEATURES = "features";

    /**
     * Features
     */
    String F_XSD_ANNOTATION = "a";
    String F_XSD_DECIMAL_ANY_SEPARATOR = "ds";
    String F_XSD_ALL_UNBOUNDED = "cu";
    String F_XSD_NAME_COLISSION_DETECTOR = "nc";

    String F_POSTPROCESSING = "p";
    String F_POSTPROCESSING_EXTRA_SCHEMAS = "pe";
    String F_POSTPROCESSING_REFS = "pr";
    String F_POSTPROCESSING_QNAMES = "pq";
    String F_POSTPROCESSING_ALL_TO_CHOICE = "pa";
    String F_POSTPROCESSING_MIXED = "pm";
    String F_POSTPROCESSING_UNIQUE = "pu";

}
