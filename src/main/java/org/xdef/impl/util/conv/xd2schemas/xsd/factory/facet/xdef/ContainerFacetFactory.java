package org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.xdef;

import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.xdef.XDNamedValue;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.DefaultFacetFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XD2XsdDefinitions.XD_FACET_SEPARATOR;

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
    public void extraFacets(final List<XmlSchemaFacet> facets, final XDNamedValue[] params) {
        if (separators.isEmpty()) {
            separators.add(" ");
        }

        Iterator<String> separatorItr = separators.iterator();
        final StringBuilder sb = new StringBuilder();
        sb.append(separatorItr.next());
        while (separatorItr.hasNext()) {
            sb.append("|" + separatorItr.next());
        }

        final String pattern = regex + "[" + sb.toString() + "]" + "(" + regex + ")*";
        facets.add(pattern(pattern));
    }
}
