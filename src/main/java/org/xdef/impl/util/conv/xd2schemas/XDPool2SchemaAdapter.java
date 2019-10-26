package org.xdef.impl.util.conv.xd2schemas;

import org.xdef.XDPool;

public interface XDPool2SchemaAdapter<T> {

    T createSchemas(final XDPool xdPool);

}
