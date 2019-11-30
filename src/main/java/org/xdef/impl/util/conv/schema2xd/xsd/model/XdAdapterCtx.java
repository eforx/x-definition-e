package org.xdef.impl.util.conv.schema2xd.xsd.model;

import org.xdef.impl.util.conv.schema2xd.xsd.definition.Xsd2XdFeature;
import org.xdef.impl.util.conv.xd2schema.xsd.definition.XD2XsdFeature;

import java.util.Set;

/**
 * Basic XSD context for transformation XSD schema to x-definition
 */
public class XdAdapterCtx {

    /**
     * Enabled algorithm features
     */
    final private Set<Xsd2XdFeature> features;

    public XdAdapterCtx(Set<Xsd2XdFeature> features) {
        this.features = features;
    }

    /**
     * Check if algorithm feature is enabled
     * @param feature       algorithm feature
     * @return  true if algorithm feature is enabled
     */
    public boolean hasEnableFeature(final Xsd2XdFeature feature) {
        return features.contains(feature);
    }
}
