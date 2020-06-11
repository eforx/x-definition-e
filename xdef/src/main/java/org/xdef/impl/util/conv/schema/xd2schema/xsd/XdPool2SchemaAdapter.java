package org.xdef.impl.util.conv.schema.xd2schema.xsd;

import org.xdef.XDPool;

/**
 * Transforms x-definition pool to XSD/JSON schema
 * @param <T> type of returned schema
 */
public interface XdPool2SchemaAdapter<T> {

    /**
     * Transforms given x-definition pool to XSD/JSON schema
     * @param xdPool    source x-definition pool
     * @return  XSD/JSON schema
     */
    T createSchemas(final XDPool xdPool);

}
