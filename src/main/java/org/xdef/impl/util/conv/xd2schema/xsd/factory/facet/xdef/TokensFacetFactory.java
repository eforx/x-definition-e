package org.xdef.impl.util.conv.xd2schema.xsd.factory.facet.xdef;

import org.apache.ws.commons.schema.XmlSchemaEnumerationFacet;
import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.xdef.XDNamedValue;
import org.xdef.impl.util.conv.xd2schema.xsd.factory.facet.DefaultFacetFactory;
import org.xdef.impl.util.conv.schema.util.XsdLogger;

import java.util.List;

import static org.xdef.impl.util.conv.xd2schema.xsd.definition.AlgPhase.TRANSFORMATION;
import static org.xdef.impl.util.conv.xd2schema.xsd.definition.XD2XsdDefinitions.XD_FACET_ARGUMENT;
import static org.xdef.impl.util.conv.schema.util.XsdLoggerDefs.LOG_DEBUG;

public class TokensFacetFactory extends DefaultFacetFactory {

    static public final String XD_PARSER_NAME = "tokens";

    @Override
    public boolean customFacet(List<XmlSchemaFacet> facets, XDNamedValue param) {
        if (XD_FACET_ARGUMENT.equals(param.getName())) {
            enumeration(param.getValue().toString().split("\\s*\\|\\s*"), facets);
            return true;
        }

        return false;
    }

    protected void enumeration(final String[] values, final List<XmlSchemaFacet> facets) {
        XsdLogger.print(LOG_DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Add facet enumeration");
        for (String value : values) {
            XmlSchemaEnumerationFacet facet = new XmlSchemaEnumerationFacet();
            // Remove all new lines and leading whitespaces on new line
            String strValue = value.replaceAll("\\n *", " ");
            facet.setValue(strValue);
            facets.add(facet);
        }
        return;
    }
}
