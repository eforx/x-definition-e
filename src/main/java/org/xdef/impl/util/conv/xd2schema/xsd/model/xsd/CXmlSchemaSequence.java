package org.xdef.impl.util.conv.xd2schema.xsd.model.xsd;

import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSequenceMember;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;

import java.util.List;

public class CXmlSchemaSequence extends CXmlSchemaGroupParticle<XmlSchemaSequence, XmlSchemaSequenceMember> {

    public CXmlSchemaSequence(XmlSchemaSequence xsdGroupElem) {
        super(xsdGroupElem);
    }

    @Override
    public final List<XmlSchemaSequenceMember> getItems() {
        return xsdNode.getItems();
    }

    @Override
    public void addItem(XmlSchemaObjectBase item) {
        xsdNode.getItems().add((XmlSchemaSequenceMember) item);
    }

    @Override
    public void addItems(List<XmlSchemaObjectBase> items) {
        for (XmlSchemaObjectBase member : items) {
            xsdNode.getItems().add((XmlSchemaSequenceMember) member);
        }
    }
}
