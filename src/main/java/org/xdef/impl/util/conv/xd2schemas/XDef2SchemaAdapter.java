package org.xdef.impl.util.conv.xd2schemas;

import org.xdef.XDPool;
import org.xdef.model.XMDefinition;

public interface XDef2SchemaAdapter<T> {

    /**
     * Transforms given x-definition pool to XSD/JSON schema
     * @param xdPool    source x-definition pool
     * @return  XSD/JSON schema
     */
    T createSchema(final XDPool xdPool);

    /**
     * Transforms given x-definition to XSD/JSON schema
     * @param xDef  source x-definition
     * @return  XSD/JSON schema
     */
    T createSchema(final XMDefinition xDef);

}
