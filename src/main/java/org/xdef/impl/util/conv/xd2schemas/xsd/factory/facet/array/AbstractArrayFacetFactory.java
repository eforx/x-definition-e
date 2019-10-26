package org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.array;

import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.apache.ws.commons.schema.constants.Constants;
import org.xdef.XDContainer;
import org.xdef.XDNamedValue;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.DefaultFacetFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.array.regex.EnumerationRegexFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.array.regex.IntegerRegexFactory;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XD2XsdUtils;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.xdef.XDValueID.XD_CONTAINER;
import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.*;

public abstract class AbstractArrayFacetFactory extends DefaultFacetFactory {

    protected Set<Integer> ignoredParams = new HashSet<Integer>();

    @Override
    public List<XmlSchemaFacet> build(final XDNamedValue[] params) {
        if (XsdLogger.isInfo(logLevel)) {
            XsdLogger.print(INFO, TRANSFORMATION, this.getClass().getSimpleName(),"Building facets...");
        }

        List<XmlSchemaFacet> facets = new ArrayList<XmlSchemaFacet>();

        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                if (ignoredParams.contains(i)) {
                    continue;
                }

                build(facets, params[i]);
            }
        }

        facets.addAll(createPatternFacets());
        return facets;
    }

    public QName determineBaseType(final XDNamedValue[] parameters) {
        if (XsdLogger.isDebug(logLevel)) {
            XsdLogger.print(DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Determination of QName...");
        }

        String parserName = "";
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
                    if (parserName.isEmpty()) {
                        parserName = ((XDParser) values[0]).parserName();
                    } else {
                        allParsersSame = checkParser((XDParser) values[0], i, parserName);
                    }
                }
            }
            else if (xVal instanceof XDParser) {
                if (parserName.isEmpty()) {
                    parserName = ((XDParser) xVal).parserName();
                } else {
                    allParsersSame = checkParser((XDParser) xVal, i, parserName);
                }
            } else {
                nonItemRestriction = true;
                ignoredParams.add(i);
            }
        }

        if ((nonItemRestriction == true && !parserName.isEmpty()) || allParsersSame == false) {
            String ignoredParamsStr = "";
            for (Integer paramIndex : ignoredParams) {
                if (!handleIgnoredParam(parameters[paramIndex])) {
                    ignoredParamsStr += parameters[paramIndex].getName() + ", ";
                }
            }

            if (!ignoredParamsStr.isEmpty()) {
                if (XsdLogger.isWarn(logLevel)) {
                    XsdLogger.print(WARN, TRANSFORMATION, this.getClass().getSimpleName(),
                            "List/Union facet - Using of unhandled restrictions found! Following attributes/parsers being ignored: " + ignoredParamsStr);
                }
            }
        }

        return Constants.XSD_STRING;
    }

    protected String parserParamsToRegex(final String parserName, final XDNamedValue[] params) {
        QName parserQName = XD2XsdUtils.getDefaultQName(parserName);

        String regex = null;

        if (Constants.XSD_INT.equals(parserQName)) {
            regex = new IntegerRegexFactory(logLevel).regex(params);
        } else if (Constants.XSD_STRING.equals(parserQName)) {
            regex = new EnumerationRegexFactory(logLevel).regex(params);
        } else {
            if (XsdLogger.isWarn(logLevel)) {
                XsdLogger.print(WARN, TRANSFORMATION, this.getClass().getSimpleName(),"Parser params to regex - Unsupported list parser! Parser=" + parserName);
            }
        }

        return regex;
    }

    protected boolean handleIgnoredParam(XDNamedValue param) {
        return false;
    }

    protected List<XmlSchemaFacet> createPatternFacets() {
        return null;
    }

    protected void createPatterns(final String parserName, final XDNamedValue[] params) { }

    protected boolean createPatternFromValue(final XDValue xVal) {
        if (XsdLogger.isDebug(logLevel)) {
            XsdLogger.print(DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Creating pattern from value");
        }

        if (xVal instanceof XDParser) {
            XDParser parser = ((XDParser) xVal);
            createPatterns(parser.parserName(),  parser.getNamedParams().getXDNamedItems());
            return true;
        } else {
            if (XsdLogger.isWarn(logLevel)) {
                XsdLogger.print(WARN, TRANSFORMATION, this.getClass().getSimpleName(),"Unsupported type of value. ValueId=" + xVal.getItemId());
            }
        }

        return false;
    }

    private boolean checkParser(final XDParser parser, final int index, final String parserName) {
        if (!parserName.equals(parser.parserName())) {
            if (XsdLogger.isDebug(logLevel)) {
                XsdLogger.print(DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"List/Union - parsers are not same!");
            }
            ignoredParams.add(index);
            return false;
        }

        return true;
    }

}
