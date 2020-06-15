package org.xdef.impl.util.conv.schema.xd2schema.xsd.factory.facet.pattern.types;

import org.xdef.impl.util.conv.schema.xd2schema.xsd.model.XsdAdapterCtx;

public abstract class AbstractRegexFactory implements RegexFactory {

    /**
     * XSD adapter context
     */
    protected XsdAdapterCtx adapterCtx;

    @Override
    public void setAdapterCtx(XsdAdapterCtx adapterCtx) {
        this.adapterCtx = adapterCtx;
    }
}
