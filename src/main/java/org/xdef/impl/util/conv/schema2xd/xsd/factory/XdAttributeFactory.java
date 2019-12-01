package org.xdef.impl.util.conv.schema2xd.xsd.factory;

import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.w3c.dom.Element;
import org.xdef.impl.util.conv.schema2xd.xsd.model.XdAdapterCtx;
import org.xdef.impl.util.conv.schema2xd.xsd.util.Xsd2XdUtils;

import static org.xdef.impl.util.conv.schema2xd.xsd.definition.Xsd2XdDefinitions.XD_ATTR_SCRIPT;
import static org.xdef.impl.util.conv.schema2xd.xsd.definition.Xsd2XdFeature.XD_EXPLICIT_OCCURRENCE;

public class XdAttributeFactory {

    final private XdAdapterCtx adapterCtx;

    public XdAttributeFactory(XdAdapterCtx adapterCtx) {
        this.adapterCtx = adapterCtx;
    }

    public void addOccurrence(final Element xdParticle, final XmlSchemaParticle xsdParicle) {
        if (xsdParicle.getMaxOccurs() == 1 && xsdParicle.getMinOccurs() == 1) {
            if (adapterCtx.hasEnableFeature(XD_EXPLICIT_OCCURRENCE)) {
                Xsd2XdUtils.addXdefAttribute(xdParticle, XD_ATTR_SCRIPT, "occurs 1");
            }
            return;
        }

        if (xsdParicle.getMaxOccurs() == Long.MAX_VALUE) {
            if (xsdParicle.getMinOccurs() == 0) {
                Xsd2XdUtils.addXdefAttribute(xdParticle, XD_ATTR_SCRIPT, "occurs *");
                return;
            }

            if (xsdParicle.getMinOccurs() == 1) {
                Xsd2XdUtils.addXdefAttribute(xdParticle, XD_ATTR_SCRIPT, "occurs +");
                return;
            }

            Xsd2XdUtils.addXdefAttribute(xdParticle, XD_ATTR_SCRIPT, "occurs " + xsdParicle.getMinOccurs() + "..*");
            return;
        }

        if (xsdParicle.getMinOccurs() == 0 && xsdParicle.getMaxOccurs() == 1) {
            Xsd2XdUtils.addXdefAttribute(xdParticle, XD_ATTR_SCRIPT, "occurs ?");
            return;
        }

        Xsd2XdUtils.addXdefAttribute(xdParticle, XD_ATTR_SCRIPT, "occurs " + xsdParicle.getMinOccurs() + ".." + xsdParicle.getMaxOccurs());
    }

    public void addNillable(final Element xdParticle, final XmlSchemaElement xsdElem) {
        if (xsdElem.isNillable()) {
            Xsd2XdUtils.addXdefAttribute(xdParticle, XD_ATTR_SCRIPT, "options nillable");
        }
    }
}
