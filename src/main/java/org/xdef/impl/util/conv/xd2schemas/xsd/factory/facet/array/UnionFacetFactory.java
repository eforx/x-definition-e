package org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.array;

import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.xdef.XDContainer;
import org.xdef.XDNamedValue;
import org.xdef.XDValue;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;

import java.util.ArrayList;
import java.util.List;

import static org.xdef.XDValueID.XD_CONTAINER;
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.AlgPhase.TRANSFORMATION;
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XsdLoggerDefs.LOG_DEBUG;

public class UnionFacetFactory extends AbstractArrayFacetFactory {

    private List<String> facetPatterns = new ArrayList<String>();

    @Override
    public boolean customFacet(List<XmlSchemaFacet> facets, XDNamedValue param) {
        XDValue xVal = param.getValue();
        boolean res = false;
        if (xVal.getItemId() == XD_CONTAINER) {
            XDValue[] values = ((XDContainer) xVal).getXDItems();
            for (XDValue v : values) {
                boolean resTmp = createPatternFromValue(v);
                if (res == false) {
                    res = resTmp;
                }
            }
        } else {
            res = createPatternFromValue(xVal);
        }

        return res;
    }

    @Override
    protected void createPatterns(final String parserName, final XDNamedValue[] params) {
        XsdLogger.print(LOG_DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Creating patterns ...");
        facetPatterns.add(parserParamsToRegex(parserName, params));
    }

    @Override
    protected List<XmlSchemaFacet> createPatternFacets() {
        XsdLogger.print(LOG_DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Creating pattern facets ...");

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
