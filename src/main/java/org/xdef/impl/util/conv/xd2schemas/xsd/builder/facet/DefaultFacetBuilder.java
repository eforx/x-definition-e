package org.xdef.impl.util.conv.xd2schemas.xsd.builder.facet;

import org.apache.ws.commons.schema.*;
import org.xdef.XDContainer;
import org.xdef.XDNamedValue;
import org.xdef.XDValue;

import java.util.ArrayList;
import java.util.List;

import static org.xdef.impl.util.conv.xd2schemas.xsd.builder.facet.IXsdFacetBuilder.ValueType.DECIMAL_FLOATING;
import static org.xdef.impl.util.conv.xd2schemas.xsd.builder.facet.IXsdFacetBuilder.ValueType.DECIMAL_INTEGER;

/**
 * Used for creation of restrictions
 */
public class DefaultFacetBuilder extends AbstractXsdFacetBuilder {

    @Override
    public XmlSchemaMinInclusiveFacet minInclusive(final XDNamedValue param) {
        XmlSchemaMinInclusiveFacet facet = new XmlSchemaMinInclusiveFacet();
        setValue(facet, param.getValue());
        return facet;
    }

    @Override
    public XmlSchemaMaxInclusiveFacet maxInclusive(final XDNamedValue param) {
        XmlSchemaMaxInclusiveFacet facet = new XmlSchemaMaxInclusiveFacet();
        setValue(facet, param.getValue());
        return facet;
    }

    @Override
    public XmlSchemaMinExclusiveFacet minExclusive(final XDNamedValue param) {
        XmlSchemaMinExclusiveFacet facet = new XmlSchemaMinExclusiveFacet();
        setValue(facet, param.getValue());
        return facet;
    }

    @Override
    public XmlSchemaMaxExclusiveFacet maxExclusive(final XDNamedValue param) {
        XmlSchemaMaxExclusiveFacet facet = new XmlSchemaMaxExclusiveFacet();
        setValue(facet, param.getValue());
        return facet;
    }

    @Override
    public XmlSchemaMaxLengthFacet maxLength(final XDNamedValue param) {
        XmlSchemaMaxLengthFacet facet = new XmlSchemaMaxLengthFacet();
        facet.setValue(param.getValue().intValue());
        return facet;
    }

    @Override
    public XmlSchemaMinLengthFacet minLength(final XDNamedValue param) {
        XmlSchemaMinLengthFacet facet = new XmlSchemaMinLengthFacet();
        facet.setValue(param.getValue().intValue());
        return facet;
    }

    @Override
    public XmlSchemaLengthFacet length(final XDNamedValue param) {
        XmlSchemaLengthFacet facet = new XmlSchemaLengthFacet();
        facet.setValue(param.getValue().intValue());
        return facet;
    }

    @Override
    public XmlSchemaWhiteSpaceFacet whitespace(final XDNamedValue param) {
        XmlSchemaWhiteSpaceFacet facet = new XmlSchemaWhiteSpaceFacet();
        facet.setValue(param.getValue().stringValue());
        return facet;
    }

    @Override
    public List<XmlSchemaPatternFacet> pattern(final XDNamedValue param) {
        List<XmlSchemaPatternFacet> facets = new ArrayList<XmlSchemaPatternFacet>();
        String[] patterns = param.getValue().stringValue().split("\n");
        for (String p : patterns) {
            facets.add(super.pattern(p));
        }

        return facets;
    }

    @Override
    public List<XmlSchemaEnumerationFacet> enumeration(XDNamedValue param) {
        List<XmlSchemaEnumerationFacet> facets = new ArrayList<XmlSchemaEnumerationFacet>();
        if (param.getValue().getItemId() == XDValue.XD_CONTAINER) {
            for (XDValue value : ((XDContainer) param.getValue()).getXDItems()) {
                XmlSchemaEnumerationFacet facet = new XmlSchemaEnumerationFacet();
                // Remove all new lines and leading whitespaces on new line
                String strValue = value.stringValue().replaceAll("\\n *", " ");
                facet.setValue(strValue);
                facets.add(facet);
            }
        }
        return facets;
    }

    @Override
    public XmlSchemaFractionDigitsFacet fractionDigits(final XDNamedValue param) {
        XmlSchemaFractionDigitsFacet facet = new XmlSchemaFractionDigitsFacet();
        facet.setValue(param.getValue().intValue());
        return facet;
    }

    @Override
    public XmlSchemaTotalDigitsFacet totalDigits(final XDNamedValue param) {
        XmlSchemaTotalDigitsFacet facet = new XmlSchemaTotalDigitsFacet();
        facet.setValue(param.getValue().intValue());
        return facet;
    }

    protected void setValue(final XmlSchemaFacet facet, XDValue xdValue) {
        if (DECIMAL_INTEGER.equals(valueType)) {
            facet.setValue(xdValue.intValue());
        } else if (DECIMAL_FLOATING.equals(valueType)) {
            facet.setValue(xdValue.doubleValue());
        } else {
            facet.setValue(xdValue.stringValue());
        }
    }

}
