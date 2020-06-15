package org.xdef.impl.util.conv.schema.xd2schema.xsd.adapter;

import org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.Xd2XsdFeature;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.model.XsdAdapterCtx;
import org.xdef.sys.ReportWriter;

import java.util.HashSet;
import java.util.Set;

/**
 * Base class for all adapters transforming x-definition to XSD document
 */
public abstract class AbstractXd2XsdAdapter {

    /**
     * Adapter context
     */
    protected XsdAdapterCtx adapterCtx = null;

    /**
     * Output report writer
     */
    protected ReportWriter reportWriter = null;

    /**
     * Enabled algorithm features
     */
    protected Set<Xd2XsdFeature> features = new HashSet<Xd2XsdFeature>();

    /**
     * External setting of adapter context
     * @param adapterCtx    adapter context
     */
    public void setAdapterCtx(XsdAdapterCtx adapterCtx) {
        this.adapterCtx = adapterCtx;
    }

    /**
     * Get names of all created schemas
     * @return return set of schemas names
     */
    public final Set<String> getSchemaNames() {
        return adapterCtx.getSchemaNames();
    }

    /**
     * Set features which should be enabled by transformation algorithm
     * @param features
     */
    public void setFeatures(Set<Xd2XsdFeature> features) {
        this.features = features;
    }

    /**
     * Add feature which should be enabled by transformation algorithm
     * @param feature
     */
    public void addFeature(Xd2XsdFeature feature) {
        features.add(feature);
    }

    /**
     * Set output writer
     * @param reportWriter
     */
    public void setReportWriter(ReportWriter reportWriter) {
        this.reportWriter = reportWriter;
    }
}
