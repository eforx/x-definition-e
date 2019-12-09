package org.xdef.impl.util.conv.schema.schema2xd.xsd;

/**
 * Transforms XSD/JSON schema to x-definition or x-definiton pool
 * @param <T> type of input schema
 */
public interface Schema2XDefAdapter<T> {

    /**
     * Transforms given XSD/JSON schema to x-definition or x-definiton pool XML
     * @param schema        input schema to be transformed
     * @param xDefName      output x-definition name
     * @return x-definition or x-definiton pool
     */
    String createXDefinition(final T schema, final String xDefName);

}
