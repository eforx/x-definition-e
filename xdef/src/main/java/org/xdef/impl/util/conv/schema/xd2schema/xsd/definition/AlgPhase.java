package org.xdef.impl.util.conv.schema.xd2schema.xsd.definition;

/**
 * Enum of algorithm phases describing individual phase
 */
public enum AlgPhase {
    INITIALIZATION("Initialization"),       // Used for preparation process of transformation x-defintion to xsd
    PREPROCESSING("Pre-processing"),        // Used for extracting data from x-definition tree
    TRANSFORMATION("Transformation"),       // Used for tree transformation of x-definition elements
    POSTPROCESSING("Post-processing");      // Used for post transformation based on gathered nodes and information

    private String val;

    AlgPhase(String val) {
        this.val = val;
    }

    public String getVal() {
        return val;
    }
}
