package org.xdef.impl.util.conv.xd2schemas.xsd.adapter;

import org.xdef.impl.util.conv.xd2schemas.xsd.definition.XD2XsdFeature;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.XsdAdapterCtx;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractXd2XsdAdapter {

    /**
     * Adapter context
     */
    protected XsdAdapterCtx adapterCtx = null;

    /**
     * Enabled algorithm features
     */
    protected Set<XD2XsdFeature> features = new HashSet<XD2XsdFeature>();

    /**
     * External setting of adapter context
     * @param adapterCtx    adapter context
     */
    public void setAdapterCtx(XsdAdapterCtx adapterCtx) {
        this.adapterCtx = adapterCtx;
    }

    /**
     * Get names of all created schemas
     * @return  return set of schemas names
     */
    public final Set<String> getSchemaNames() {
        return adapterCtx.getSchemaNames();
    }

    public void setFeatures(Set<XD2XsdFeature> features) {
        this.features = features;
    }

    public void addFeature(XD2XsdFeature feature) {
        features.add(feature);
    }
}
