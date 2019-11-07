package org.xdef.impl.util.conv.xd2schemas.xsd.model.xsd;

import org.apache.ws.commons.schema.XmlSchemaChoice;
import org.apache.ws.commons.schema.XmlSchemaChoiceMember;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;

import java.util.List;

public class CXmlSchemaChoice extends CXmlSchemaGroupParticle<XmlSchemaChoice, XmlSchemaChoiceMember> {

    /**
     * Used to be true if the instance has x-definition source mixed element
     */
    private boolean isSourceMixed = false;

    public CXmlSchemaChoice(XmlSchemaChoice xsdGroupElem) {
        super(xsdGroupElem);
    }

    @Override
    public final List<XmlSchemaChoiceMember> getItems() {
        return xsdNode.getItems();
    }

    @Override
    public void addItem(XmlSchemaObjectBase item) {
        xsdNode.getItems().add((XmlSchemaChoiceMember) item);
    }

    @Override
    public void addItems(List<XmlSchemaObjectBase> items) {
        for (XmlSchemaObjectBase member : items) {
            xsdNode.getItems().add((XmlSchemaChoiceMember) member);
        }
    }

    public boolean isSourceMixed() {
        return isSourceMixed;
    }

    public void setSourceMixed() {
        isSourceMixed = true;
    }

    public void updateOccurence() {
        xsdNode.setMaxOccurs(xsdNode.getItems().size());
        xsdNode.setMinOccurs(xsdNode.getMaxOccurs());
    }
}
