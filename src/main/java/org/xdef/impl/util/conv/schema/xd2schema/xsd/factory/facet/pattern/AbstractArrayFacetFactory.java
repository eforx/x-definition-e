package org.xdef.impl.util.conv.schema.xd2schema.xsd.factory.facet.pattern;

import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.apache.ws.commons.schema.constants.Constants;
import org.xdef.XDContainer;
import org.xdef.XDNamedValue;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.impl.util.conv.schema.util.SchemaLogger;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.factory.facet.DefaultFacetFactory;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.factory.facet.pattern.types.EnumerationRegexFactory;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.factory.facet.pattern.types.IntegerRegexFactory;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.util.Xd2XsdParserMapping;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.xdef.XDValueID.XD_CONTAINER;
import static org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.AlgPhase.TRANSFORMATION;
import static org.xdef.impl.util.conv.schema.util.SchemaLoggerDefs.*;

public abstract class AbstractArrayFacetFactory extends DefaultFacetFactory {

    protected Set<Integer> ignoredParams = new HashSet<Integer>();
    protected QName type = null;

    @Override
    public List<XmlSchemaFacet> build(final XDNamedValue[] params) {
        SchemaLogger.print(LOG_INFO, TRANSFORMATION, this.getClass().getSimpleName(),"Building facets ...");

        final List<XmlSchemaFacet> facets = new ArrayList<XmlSchemaFacet>();

        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                if (ignoredParams.contains(i)) {
                    continue;
                }

                build(facets, params[i]);
            }
        }

        createPatternFacets(facets);
        return facets;
    }

    public QName determineBaseType(final XDNamedValue[] parameters) {
        SchemaLogger.print(LOG_DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Determination of QName...");

        String parserName = null;
        boolean allParsersSame = true;
        boolean nonItemRestriction = false;

        for (int i = 0; i < parameters.length; i++) {
            XDValue xVal = parameters[i].getValue();
            if (xVal.getItemId() == XD_CONTAINER) {
                XDValue[] values = ((XDContainer) xVal).getXDItems();
                /*if (values.length > 1) {
                    System.out.println("List/Union - multiple parsers - unsupported!");
                    allParsersSame = false;
                    ignoredParams.add(i);
                }
                else*/ if (values.length == 1) {
                    if (parserName == null || parserName.isEmpty()) {
                        parserName = ((XDParser) values[0]).parserName();
                    } else {
                        allParsersSame = checkParser((XDParser) values[0], i, parserName);
                    }
                }
            }
            else if (xVal instanceof XDParser) {
                if (parserName == null || parserName.isEmpty()) {
                    parserName = ((XDParser) xVal).parserName();
                } else {
                    allParsersSame = checkParser((XDParser) xVal, i, parserName);
                }
            } else {
                nonItemRestriction = true;
                ignoredParams.add(i);
            }
        }

        if ((nonItemRestriction == true && parserName != null && !parserName.isEmpty()) || allParsersSame == false) {
            String ignoredParamsStr = "";
            for (Integer paramIndex : ignoredParams) {
                if (!handleIgnoredParam(parameters[paramIndex])) {
                    ignoredParamsStr += parameters[paramIndex].getName() + ", ";
                }
            }

            if (!ignoredParamsStr.isEmpty()) {
                SchemaLogger.print(LOG_WARN, TRANSFORMATION, this.getClass().getSimpleName(),
                        "List/Union facet - Using of unhandled restrictions found! Following attributes/parsers being ignored: " + ignoredParamsStr);
            }
        }

        type = null;
        if (parserName == null || parserName.isEmpty()) {
            type = Constants.XSD_STRING;
        } else if (allParsersSame) {
            type = Xd2XsdParserMapping.getDefaultParserQName(parserName, adapterCtx);
        }

        if (type == null) {
            type = Constants.XSD_STRING;
        }

        return type;
    }

    protected String parserParamsToRegex(final String parserName, final XDNamedValue[] params) {
        QName parserQName = this instanceof ListRegexFacetFactory ? type : Xd2XsdParserMapping.getDefaultParserQName(parserName, adapterCtx);
        String regex = "";

        if (Constants.XSD_INT.equals(parserQName)) {
            regex = new IntegerRegexFactory().regex(params);
        } else if (Constants.XSD_STRING.equals(parserQName)) {
            regex = new EnumerationRegexFactory().regex(params);
        } else {
            SchemaLogger.print(LOG_WARN, TRANSFORMATION, this.getClass().getSimpleName(),"Parser params to regex - Unsupported list parser! QName=" + type);
        }

        return regex;
    }

    protected boolean handleIgnoredParam(XDNamedValue param) {
        return false;
    }

    protected void createPatternFacets(final List<XmlSchemaFacet> facets) { }

    protected void createPatterns(final String parserName, final XDNamedValue[] params) { }

    protected boolean createPatternFromValue(final XDValue xVal) {
        SchemaLogger.print(LOG_DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Creating pattern from value");

        if (xVal instanceof XDParser) {
            final XDParser parser = ((XDParser) xVal);
            createPatterns(parser.parserName(), parser.getNamedParams().getXDNamedItems());
            return true;
        } else {
            SchemaLogger.print(LOG_WARN, TRANSFORMATION, this.getClass().getSimpleName(),"Unsupported value of simple content type. ValueId=" + xVal.getItemId());
        }

        return false;
    }

    private boolean checkParser(final XDParser parser, final int index, final String parserName) {
        if (!parserName.equals(parser.parserName())) {
            SchemaLogger.print(LOG_DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"List/Union - parsers are not same!");
            ignoredParams.add(index);
            return false;
        }

        return true;
    }

}
