package org.xdef.impl.util.conv.xd2schemas.xsd.builder.facet;

import org.apache.ws.commons.schema.XmlSchemaPatternFacet;
import org.xdef.XDNamedValue;
import org.xdef.impl.util.conv.xd2schemas.xsd.builder.XsdElementBuilder;

public class DateTimeFacetBuilder extends AbstractXsdFacetBuilder {

    @Override
    public XmlSchemaPatternFacet pattern(final XDNamedValue param) {
        final String value = param.getValue().stringValue();
        XmlSchemaPatternFacet facet = super.pattern(value);
        facet.setAnnotation(XsdElementBuilder.createAnnotation("Original pattern value: '" + value + "'"));
        //String pattern = param.getValue().stringValue().replaceAll("\\[", "(").replaceAll("]", ")?");
        return facet;
    }
}
