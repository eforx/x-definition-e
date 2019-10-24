package org.xdef.impl.util.conv.xd2schemas.xsd.builder.facet.array;

import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.xdef.XDContainer;
import org.xdef.XDNamedValue;
import org.xdef.XDParser;
import org.xdef.XDValue;

import java.util.ArrayList;
import java.util.List;

import static org.xdef.XDValueID.XD_CONTAINER;

public class UnionFacetBuilder extends AbstractArrayFacetBuilder {

    private List<String> facetPatterns = new ArrayList<String>();

    @Override
    public boolean customFacet(List<XmlSchemaFacet> facets, XDNamedValue param) {
        XDValue xVal = param.getValue();
        if (xVal.getItemId() == XD_CONTAINER) {
            XDValue[] values = ((XDContainer) xVal).getXDItems();
            for (XDValue v : values) {
                if (v instanceof XDParser) {
                    XDParser parser = ((XDParser) v);
                    createPatterns(parser.parserName(), parser.getNamedParams().getXDNamedItems());
                } else {
                    System.out.println("Union - Unsupported value: " + v.getItemId());
                }
            }
            return true;
        } else if (xVal instanceof XDParser) {
            XDParser parser = ((XDParser) xVal);
            createPatterns(parser.parserName(), parser.getNamedParams().getXDNamedItems());
            return true;
        }


        return false;
    }

    private void createPatterns(final String parserName, final XDNamedValue[] params) {
        facetPatterns.add(transformToRegex(parserName, params));
    }

    @Override
    protected List<XmlSchemaFacet> createFacet() {
        List<XmlSchemaFacet> facets = new ArrayList<XmlSchemaFacet>();

        if (!facetPatterns.isEmpty()) {
            // Enumeration with list
            if (false) {
                String pattern = "";

                for (String p : facetPatterns) {
                    if (p != null) {
                        if (pattern.isEmpty()) {
                            pattern = "(((" + p + ")\\s)*)";
                        } else {
                            pattern += "|(((" + p + ")\\s)*)";
                        }
                    }
                }

                String patternFinal = "";

                for (String p : facetPatterns) {
                    if (p != null) {
                        if (patternFinal.isEmpty()) {
                            patternFinal = "((" + p + "){0,1})";
                        } else {
                            patternFinal += "|((" + p + "){0,1})";
                        }
                    }
                }

                if (!pattern.isEmpty()) {
                    pattern = "(" + pattern + ")*" + "(" + patternFinal + ")";
                    facets.add(super.pattern(pattern));
                }
            } else {
                String pattern = "";
                for (String p : facetPatterns) {
                    if (p != null) {
                        if (pattern.isEmpty()) {
                            pattern = "(" + p + ")";
                        } else {
                            pattern += "|(" + p + ")";
                        }
                    }
                }

                if (!pattern.isEmpty()) {
                    pattern = "(" + pattern + ")";
                    facets.add(super.pattern(pattern));
                }
            }


        }

        return facets;
    }

}
