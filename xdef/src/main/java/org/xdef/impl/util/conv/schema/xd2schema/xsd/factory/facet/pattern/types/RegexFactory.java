package org.xdef.impl.util.conv.schema.xd2schema.xsd.factory.facet.pattern.types;

import org.xdef.XDNamedValue;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.model.XsdAdapterCtx;

public interface RegexFactory {

    String regex(final XDNamedValue[] params);

    void setAdapterCtx(XsdAdapterCtx adapterCtx);

}
