package org.xdef.impl.util.conv.schema2xd;

import org.xdef.model.XMDefinition;

public interface Schema2XDefAdapter<T> {

    XMDefinition createCompiledXDefinition(final T schema);

    String createXDefinition(final T schema, final String xDefName);
}
