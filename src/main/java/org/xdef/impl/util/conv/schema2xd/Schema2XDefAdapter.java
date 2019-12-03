package org.xdef.impl.util.conv.schema2xd;

import org.xdef.model.XMDefinition;

public interface Schema2XDefAdapter<T> {

    XMDefinition createCompiledXDefinition(final T rootSchema);

    String createXDefinition(final T rootSchema, final String xDefName);
}
