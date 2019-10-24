package org.xdef.impl.util.conv.xd2schemas.xsd.builder.facet.array;

import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.apache.ws.commons.schema.constants.Constants;
import org.xdef.XDContainer;
import org.xdef.XDNamedValue;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.impl.util.conv.xd2schemas.xsd.builder.facet.DefaultFacetBuilder;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XD2XsdUtils;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.xdef.XDValueID.XD_CONTAINER;

public abstract class AbstractArrayFacetBuilder extends DefaultFacetBuilder {

    protected Set<Integer> ignoredParams = new HashSet<Integer>();

    @Override
    public List<XmlSchemaFacet> build(final XDNamedValue[] params) {
        List<XmlSchemaFacet> facets = new ArrayList<XmlSchemaFacet>();

        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                if (ignoredParams.contains(i)) {
                    continue;
                }

                build(facets, params[i]);
            }
        }

        facets.addAll(createFacet());
        return facets;
    }

    public QName determineBaseType(final XDNamedValue[] parameters) {
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
                    XDParser parser = ((XDParser) values[0]);
                    if (parserName == null) {
                        parserName = parser.parserName();
                    } else if (!parserName.equals(parser.parserName())) {
                        System.out.println("List/Union - parsers are not same!");
                        allParsersSame = false;
                        ignoredParams.add(i);
                    }
                }
            }
            else if (xVal instanceof XDParser) {
                XDParser parser = ((XDParser) xVal);
                if (parserName == null) {
                    parserName = parser.parserName();
                } else if (!parserName.equals(parser.parserName())) {
                    System.out.println("List/Union - parsers are not same!");
                    allParsersSame = false;
                    ignoredParams.add(i);
                }
            } else {
                nonItemRestriction = true;
                ignoredParams.add(i);
            }
        }

        if ((nonItemRestriction == true && parserName != null) || allParsersSame == false) {
            String ignoredParamsStr = "";
            for (Integer paramIndex : ignoredParams) {
                if (!handleIgnoredParam(parameters[paramIndex])) {
                    ignoredParamsStr += parameters[paramIndex].getName() + ", ";
                }
            }

            if (!ignoredParamsStr.isEmpty()) {
                System.out.println("List/Union facet - Using of unhandled restrictions found! Following attributes/parsers being ignored: " + ignoredParamsStr);
            }
        }

        return Constants.XSD_STRING;
    }

    protected String transformToRegex(final String parserName, final XDNamedValue[] params) {
        QName parserQName = XD2XsdUtils.getDefaultQName(parserName);

        String regex = null;

        if (Constants.XSD_INT.equals(parserQName)) {
            regex = IntegerRegexBuilder.regex(params);
        } else if (Constants.XSD_STRING.equals(parserQName)) {
            regex = EnumerationRegexBuilder.regex(params);
        } else {
            System.out.println("Unsupported list parser: " + parserName);
        }

        return regex;
    }

    protected boolean handleIgnoredParam(XDNamedValue param) {
        return false;
    }

    protected List<XmlSchemaFacet> createFacet() {
        return null;
    }

}
