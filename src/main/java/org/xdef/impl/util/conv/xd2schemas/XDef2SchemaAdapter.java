package org.xdef.impl.util.conv.xd2schemas;

import org.xdef.XDPool;
import org.xdef.model.XMDefinition;

public interface XDef2SchemaAdapter<T> {

    T createSchema(final XDPool xdPool);
    T createSchema(final XMDefinition xmDefinition);

}
