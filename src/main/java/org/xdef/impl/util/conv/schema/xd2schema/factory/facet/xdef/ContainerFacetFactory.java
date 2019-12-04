package org.xdef.impl.util.conv.schema.xd2schema.factory.facet.xdef;

import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.xdef.XDNamedValue;
import org.xdef.impl.util.conv.schema.xd2schema.factory.facet.DefaultFacetFactory;
import org.xdef.impl.util.conv.schema.xd2schema.util.Xd2XsdUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.xdef.impl.util.conv.schema.xd2schema.definition.Xd2XsdDefinitions.XD_FACET_SEPARATOR;

public class ContainerFacetFactory extends DefaultFacetFactory {
    static public final String XD_PARSER_NAME_LANGUAGES = "ISOlanguages";
    static public final String XD_PARSER_NAME_NC_NAMELIST = "NCNameList";

    private final String regex;
    private Set<String> separators = new HashSet<String>();

    public ContainerFacetFactory(String regex) {
        this.regex = regex;
    }

    @Override
    public boolean customFacet(List<XmlSchemaFacet> facets, XDNamedValue param) {
        if (XD_FACET_SEPARATOR.equals(param.getName())) {
            separators.add(param.getValue().stringValue());
            return true;
        }

        return false;
    }

    @Override
    public void extraFacets(final List<XmlSchemaFacet> facets) {
        if (separators.isEmpty()) {
            separators.add(" ");
        }

        final String separatorPatten = Xd2XsdUtils.regexCollectionToSingle(separators);
        final String pattern = regex + "[" + separatorPatten + "]" + "(" + regex + ")*";
        facets.add(pattern(pattern));
    }
}
