package org.xdef.impl.util.conv.xd2schemas.xsd.builder.facet.array;

import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.xdef.XDNamedValue;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;

import java.util.ArrayList;
import java.util.List;

import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.*;

public class ListFacetBuilder extends AbstractArrayFacetBuilder {

    private Integer minItems = null;
    private Integer maxItems = null;
    private String regex = null;

    @Override
    public boolean customFacet(List<XmlSchemaFacet> facets, XDNamedValue param) {
        return createPatternFromValue(param.getValue());
    }

    @Override
    protected void createPatterns(final String parserName, final XDNamedValue[] params) {
        if (XsdLogger.isDebug(logLevel)) {
            XsdLogger.print(DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Creating patterns ...");
        }

        regex = parserParamsToRegex(parserName, params);
    }

    @Override
    protected List<XmlSchemaFacet> createPatternFacets() {
        if (XsdLogger.isDebug(logLevel)) {
            XsdLogger.print(DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Creating pattern facets ...");
        }

        List<XmlSchemaFacet> facets = new ArrayList<XmlSchemaFacet>();

        if (regex != null && !regex.isEmpty()) {
            String pattern = "((" + regex + ")\\s)";
            if (minItems != null || maxItems != null) {
                if (minItems != null) {
                    if (--minItems < 0) {
                        minItems = 0;
                    }
                }

                if (maxItems != null) {
                    if (--maxItems < 0) {
                        maxItems = 0;
                    }
                }

                if (minItems != null && minItems.equals(maxItems)) {
                    pattern += "{" + minItems + "}";
                } else {
                    pattern += "{" + (minItems == null ? 0 : minItems) + ", " + (maxItems == null ? "" : maxItems) + "}";
                }
            } else {
                pattern += "*";
            }

            pattern += "(" + regex + "){0,1}";

            facets.add(super.pattern(pattern));
        }

        return facets;
    }

    @Override
    protected boolean handleIgnoredParam(XDNamedValue param) {
        if ("length".equals(param.getName())) {
            minItems = param.getValue().intValue();
            maxItems = param.getValue().intValue();
            return true;
        } else if ("maxLength".equals(param.getName())) {
            maxItems = param.getValue().intValue();
            return true;
        } else if ("minLength".equals(param.getName())) {
            minItems = param.getValue().intValue();
            return true;
        }

        return false;
    }

}
