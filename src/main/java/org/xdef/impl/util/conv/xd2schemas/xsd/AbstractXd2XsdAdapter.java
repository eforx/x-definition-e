package org.xdef.impl.util.conv.xd2schemas.xsd;

import org.xdef.impl.util.conv.xd2schemas.xsd.model.XsdAdapterCtx;

import java.util.Set;

import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.LOG_LEVEL_INFO;

public abstract class AbstractXd2XsdAdapter {

    /**
     * Logging level
     */
    protected int logLevel = LOG_LEVEL_INFO;

    /**
     * Adapter context
     */
    protected XsdAdapterCtx adapterCtx = null;

    /**
     * External setting of adapter context
     * @param adapterCtx    adapter context
     */
    protected void setAdapterCtx(XsdAdapterCtx adapterCtx) {
        this.adapterCtx = adapterCtx;
    }

    /**
     * Set logging level
     * @param logLevel      log level
     */
    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * Get names of all created schemas
     * @return  return set of schemas names
     */
    public final Set<String> getSchemaNames() {
        return adapterCtx.getSchemaNames();
    }

}
