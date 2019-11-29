package org.xdef.impl.util.conv.xd2schemas;

import org.xdef.XDPool;

public interface XDPool2SchemaAdapter<T> {

    /**
     * Transforms given x-definition pool to XSD/JSON schema
     * @param xdPool    source x-definition pool
     * @return  XSD/JSON schema
     */
    T createSchemas(final XDPool xdPool);

}
