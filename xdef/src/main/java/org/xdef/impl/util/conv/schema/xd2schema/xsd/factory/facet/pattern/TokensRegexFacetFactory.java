package org.xdef.impl.util.conv.schema.xd2schema.xsd.factory.facet.pattern;

import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.xdef.XDNamedValue;
import org.xdef.impl.util.conv.schema.util.SchemaLogger;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.factory.facet.pattern.types.EnumerationRegexFactory;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.util.Xd2XsdUtils;

import java.util.List;

import static org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.AlgPhase.TRANSFORMATION;
import static org.xdef.impl.util.conv.schema.util.SchemaLoggerDefs.LOG_DEBUG;

public class TokensRegexFacetFactory extends AbstractArrayFacetFactory {

    static public final String XD_PARSER_CI_NAME = "tokensi";

    private String regex = null;

    @Override
    public boolean customFacet(List<XmlSchemaFacet> facets, XDNamedValue param) {
        regex = EnumerationRegexFactory.containerValuesToPattern(param.getValue().toString().split("\\s*\\|\\s*"));
        regex = Xd2XsdUtils.regex2CaseInsensitive(regex);
        return true;
    }

    @Override
    protected void createPatternFacets(final List<XmlSchemaFacet> facets) {
        SchemaLogger.print(LOG_DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Creating pattern facets ...");
        facets.add(super.pattern(regex));
    }

}
