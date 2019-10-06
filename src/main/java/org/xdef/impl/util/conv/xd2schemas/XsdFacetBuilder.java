package org.xdef.impl.util.conv.xd2schemas;

import org.apache.ws.commons.schema.*;
import org.xdef.XDContainer;
import org.xdef.XDNamedValue;
import org.xdef.XDValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for detailed description of restriction elements
 */
public class XsdFacetBuilder {

    private final XDNamedValue params[];

    public XsdFacetBuilder(final XDNamedValue[] params) {
        this.params = params;
    }

    public static List<XmlSchemaFacet> build(final XDNamedValue[] params) {
        return build(params, false);
    }

    public static List<XmlSchemaFacet> build(final XDNamedValue[] params, boolean decimal) {
        List<XmlSchemaFacet> facets = new ArrayList<XmlSchemaFacet>();
        if (params != null) {
            for (XDNamedValue param : params) {
                if ("maxLength".equals(param.getName())) {
                    facets.add(maxLength(param));
                } else if ("minLength".equals(param.getName())) {
                    facets.add(minLength(param));
                } else if ("whiteSpace".equals(param.getName())) {
                    facets.add(whitespace(param));
                } else if ("pattern".equals(param.getName())) {
                    facets.add(pattern(param));
                } else if ("minInclusive".equals(param.getName())) {
                    facets.add(minInclusive(param, decimal));
                } else if ("minExclusive".equals(param.getName())) {
                    facets.add(minExclusive(param, decimal));
                } else if ("maxInclusive".equals(param.getName())) {
                    facets.add(maxInclusive(param, decimal));
                } else if ("maxExclusive".equals(param.getName())) {
                    facets.add(maxExclusive(param, decimal));
                } else if ("argument".equals(param.getName()) || "enumeration".equals(param.getName())) {
                    enumeration(facets, param);
                } else if ("length".equals(param.getName())) {
                    facets.add(length(param));
                } else if ("fractionDigits".equals(param.getName())) {
                    facets.add(fractionDigits(param));
                } else if ("totalDigits".equals(param.getName())) {
                    facets.add(totalDigits(param));
                } else {
                    System.out.println("Unknown reference type parameter: " + param.getName());
                }
            }
        }

        return facets;
    }

    public static XmlSchemaMinInclusiveFacet minInclusive(final XDNamedValue param, boolean decimal) {
        XmlSchemaMinInclusiveFacet facet = new XmlSchemaMinInclusiveFacet();
        setDecimalValue(facet, decimal, param.getValue());
        return facet;
    }

    public static XmlSchemaMaxInclusiveFacet maxInclusive(final XDNamedValue param, boolean decimal) {
        XmlSchemaMaxInclusiveFacet facet = new XmlSchemaMaxInclusiveFacet();
        setDecimalValue(facet, decimal, param.getValue());
        return facet;
    }

    public static XmlSchemaMinExclusiveFacet minExclusive(final XDNamedValue param, boolean decimal) {
        XmlSchemaMinExclusiveFacet facet = new XmlSchemaMinExclusiveFacet();
        setDecimalValue(facet, decimal, param.getValue());
        return facet;
    }

    public static XmlSchemaMaxExclusiveFacet maxExclusive(final XDNamedValue param, boolean decimal) {
        XmlSchemaMaxExclusiveFacet facet = new XmlSchemaMaxExclusiveFacet();
        setDecimalValue(facet, decimal, param.getValue());
        return facet;
    }

    public static XmlSchemaMaxLengthFacet maxLength(final XDNamedValue param) {
        XmlSchemaMaxLengthFacet facet = new XmlSchemaMaxLengthFacet();
        facet.setValue(param.getValue().intValue());
        return facet;
    }

    public static XmlSchemaMinLengthFacet minLength(final XDNamedValue param) {
        XmlSchemaMinLengthFacet facet = new XmlSchemaMinLengthFacet();
        facet.setValue(param.getValue().intValue());
        return facet;
    }

    public static XmlSchemaLengthFacet length(final XDNamedValue param) {
        XmlSchemaLengthFacet facet = new XmlSchemaLengthFacet();
        facet.setValue(param.getValue().intValue());
        return facet;
    }

    public static XmlSchemaWhiteSpaceFacet whitespace(final XDNamedValue param) {
        XmlSchemaWhiteSpaceFacet facet = new XmlSchemaWhiteSpaceFacet();
        facet.setValue(param.getValue().stringValue());
        return facet;
    }

    public static XmlSchemaPatternFacet pattern(final XDNamedValue param) {
        XmlSchemaPatternFacet facet = new XmlSchemaPatternFacet();
        facet.setValue(param.getValue().stringValue());
        return facet;
    }

    public static void enumeration(final List<XmlSchemaFacet> facets, final XDNamedValue param) {
        if (param.getValue().getItemId() == XDValue.XD_CONTAINER) {
            for (XDValue value : ((XDContainer) param.getValue()).getXDItems()) {
                XmlSchemaEnumerationFacet facet = new XmlSchemaEnumerationFacet();
                // Remove all new lines and leading whitespaces on new line
                String strValue = value.stringValue().replaceAll("\\n *", " ");
                facet.setValue(strValue);
                facets.add(facet);
            }
        }
    }

    public static XmlSchemaFractionDigitsFacet fractionDigits(final XDNamedValue param) {
        XmlSchemaFractionDigitsFacet facet = new XmlSchemaFractionDigitsFacet();
        facet.setValue(param.getValue().intValue());
        return facet;
    }

    public static XmlSchemaTotalDigitsFacet totalDigits(final XDNamedValue param) {
        XmlSchemaTotalDigitsFacet facet = new XmlSchemaTotalDigitsFacet();
        facet.setValue(param.getValue().intValue());
        return facet;
    }

    private static void setDecimalValue(final XmlSchemaFacet facet, boolean decimal, XDValue xdValue) {
        if (decimal == false) {
            facet.setValue(xdValue.intValue());
        } else {
            facet.setValue(xdValue.doubleValue());
        }
    }
}
