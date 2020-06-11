package org.xdef.impl.util.conv.schema.xd2schema.xsd;

import org.xdef.XDPool;
import org.xdef.model.XMDefinition;

/**
 * Transforms single x-definition to XSD/JSON schema
 * @param <T> type of returned schema
 */
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
