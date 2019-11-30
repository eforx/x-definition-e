package org.xdef.impl.util.conv.xd2schema.xsd.factory.facet;

import org.apache.ws.commons.schema.*;
import org.xdef.XDContainer;
import org.xdef.XDNamedValue;
import org.xdef.XDValue;
import org.xdef.impl.util.conv.xd2schema.xsd.util.XsdLogger;

import java.util.ArrayList;
import java.util.List;

import static org.xdef.impl.util.conv.xd2schema.xsd.factory.facet.IXsdFacetFactory.ValueType.DECIMAL_FLOATING;
import static org.xdef.impl.util.conv.xd2schema.xsd.factory.facet.IXsdFacetFactory.ValueType.DECIMAL_INTEGER;
import static org.xdef.impl.util.conv.xd2schema.xsd.definition.AlgPhase.TRANSFORMATION;
import static org.xdef.impl.util.conv.xd2schema.xsd.definition.XsdLoggerDefs.LOG_DEBUG;

/**
 * Used for creation of restrictions
 */
public class DefaultFacetFactory extends AbstractXsdFacetFactory {

    @Override
    public XmlSchemaMinInclusiveFacet minInclusive(final XDNamedValue param) {
        XsdLogger.print(LOG_DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Add facet minInclusive");
        XmlSchemaMinInclusiveFacet facet = new XmlSchemaMinInclusiveFacet();
        setValue(facet, param.getValue());
        return facet;
    }

    @Override
    public XmlSchemaMaxInclusiveFacet maxInclusive(final XDNamedValue param) {
        XsdLogger.print(LOG_DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Add facet maxInclusive");
        XmlSchemaMaxInclusiveFacet facet = new XmlSchemaMaxInclusiveFacet();
        setValue(facet, param.getValue());
        return facet;
    }

    @Override
    public XmlSchemaMinExclusiveFacet minExclusive(final XDNamedValue param) {
        XsdLogger.print(LOG_DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Add facet minExclusive");
        XmlSchemaMinExclusiveFacet facet = new XmlSchemaMinExclusiveFacet();
        setValue(facet, param.getValue());
        return facet;
    }

    @Override
    public XmlSchemaMaxExclusiveFacet maxExclusive(final XDNamedValue param) {
        XsdLogger.print(LOG_DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Add facet maxExclusive");
        XmlSchemaMaxExclusiveFacet facet = new XmlSchemaMaxExclusiveFacet();
        setValue(facet, param.getValue());
        return facet;
    }

    @Override
    public XmlSchemaMaxLengthFacet maxLength(final XDNamedValue param) {
        XsdLogger.print(LOG_DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Add facet maxLength");
        XmlSchemaMaxLengthFacet facet = new XmlSchemaMaxLengthFacet();
        facet.setValue(param.getValue().intValue());
        return facet;
    }

    @Override
    public XmlSchemaMinLengthFacet minLength(final XDNamedValue param) {
        XsdLogger.print(LOG_DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Add facet minLength");
        XmlSchemaMinLengthFacet facet = new XmlSchemaMinLengthFacet();
        facet.setValue(param.getValue().intValue());
        return facet;
    }

    @Override
    public XmlSchemaLengthFacet length(final XDNamedValue param) {
        XsdLogger.print(LOG_DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Add facet length");
        XmlSchemaLengthFacet facet = new XmlSchemaLengthFacet();
        facet.setValue(param.getValue().intValue());
        return facet;
    }

    @Override
    public XmlSchemaWhiteSpaceFacet whitespace(final XDNamedValue param) {
        XsdLogger.print(LOG_DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Add facet whitespace");
        XmlSchemaWhiteSpaceFacet facet = new XmlSchemaWhiteSpaceFacet();
        facet.setValue(param.getValue().stringValue());
        return facet;
    }

    @Override
    public List<XmlSchemaPatternFacet> pattern(final XDNamedValue param) {
        XsdLogger.print(LOG_DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Add facet pattern");

        List<XmlSchemaPatternFacet> facets = new ArrayList<XmlSchemaPatternFacet>();
        String[] patterns = param.getValue().stringValue().split("\n");
        for (String p : patterns) {
            facets.add(super.pattern(p));
        }

        return facets;
    }

    @Override
    public List<XmlSchemaEnumerationFacet> enumeration(XDNamedValue param) {
        XsdLogger.print(LOG_DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Add facet enumeration");

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
        XsdLogger.print(LOG_DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Add facet fractionDigits");
        XmlSchemaFractionDigitsFacet facet = new XmlSchemaFractionDigitsFacet();
        facet.setValue(param.getValue().intValue());
        return facet;
    }

    @Override
    public XmlSchemaTotalDigitsFacet totalDigits(final XDNamedValue param) {
        XsdLogger.print(LOG_DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Add facet totalDigits");
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
