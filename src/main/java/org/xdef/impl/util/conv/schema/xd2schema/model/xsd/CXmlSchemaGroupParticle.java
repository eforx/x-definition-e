package org.xdef.impl.util.conv.schema.xd2schema.model.xsd;

import org.apache.ws.commons.schema.XmlSchemaGroupParticle;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;

public abstract class CXmlSchemaGroupParticle<T extends XmlSchemaGroupParticle, M extends XmlSchemaObjectBase> extends XmlSchemaGroupParticle implements IXmlSchemaGroupParticle<M> {

    protected final T xsdNode;

    public CXmlSchemaGroupParticle(T xsdNode) {
        this.xsdNode = xsdNode;
    }

    public final T xsd() {
        return xsdNode;
    }

}
