package org.xdef.impl.util.conv.schema.schema2xd.xsd;

public interface Schema2XDefAdapter<T> {

    String createXDefinition(final T rootSchema, final String xDefName);
}
