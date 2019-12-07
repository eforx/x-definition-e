package org.xdef.impl.util.conv.schema.xd2schema.xsd;

import org.xdef.XDPool;

public interface XdPool2SchemaAdapter<T> {

    /**
     * Transforms given x-definition pool to XSD/JSON schema
     * @param xdPool    source x-definition pool
     * @return  XSD/JSON schema
     */
    T createSchemas(final XDPool xdPool);

}
