package org.xdef.impl.util.conv.schema.xd2schema.xsd.model.xsd;

import org.apache.ws.commons.schema.XmlSchemaAll;
import org.apache.ws.commons.schema.XmlSchemaAllMember;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;

import java.util.List;

public class CXmlSchemaAll extends CXmlSchemaGroupParticle<XmlSchemaAll, XmlSchemaAllMember> {

    public CXmlSchemaAll(XmlSchemaAll xsdGroupElem) {
        super(xsdGroupElem);
    }

    @Override
    public List<XmlSchemaAllMember> getItems() {
        return xsdNode.getItems();
    }

    @Override
    public void addItem(XmlSchemaObjectBase item) {
        xsdNode.getItems().add((XmlSchemaAllMember) item);
    }

    @Override
    public void addItems(final List<XmlSchemaObjectBase> items) {
        for (XmlSchemaObjectBase item : items) {
            xsdNode.getItems().add((XmlSchemaAllMember) item);
        }
    }
}
