package org.xdef.impl.util.conv.xd2schemas.xsd.builder.facet.xdef;

import org.apache.ws.commons.schema.XmlSchemaPatternFacet;
import org.xdef.XDNamedValue;
import org.xdef.impl.util.conv.xd2schemas.xsd.builder.XsdElementBuilder;
import org.xdef.impl.util.conv.xd2schemas.xsd.builder.facet.AbstractXsdFacetBuilder;

import java.util.ArrayList;
import java.util.List;

public class DateTimeFacetBuilder extends AbstractXsdFacetBuilder {

    @Override
    public List<XmlSchemaPatternFacet> pattern(final XDNamedValue param) {
        List<XmlSchemaPatternFacet> facets = new ArrayList<XmlSchemaPatternFacet>();
        String[] patterns = param.getValue().stringValue().split("\n");
        for (String p : patterns) {
            XmlSchemaPatternFacet facet = super.pattern(p);
            facet.setAnnotation(XsdElementBuilder.createAnnotation("Original pattern value: '" + p + "'"));
            //String pattern = param.getValue().stringValue().replaceAll("\\[", "(").replaceAll("]", ")?");

            facets.add(super.pattern(p));
        }

        return facets;
    }
}
