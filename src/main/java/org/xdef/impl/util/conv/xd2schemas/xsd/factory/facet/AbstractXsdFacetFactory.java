package org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet;

import org.apache.ws.commons.schema.*;
import org.xdef.XDNamedValue;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;

import java.util.ArrayList;
import java.util.List;

import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.AlgPhase.TRANSFORMATION;
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XD2XsdDefinitions.*;
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XsdLoggerDefs.*;

public abstract class AbstractXsdFacetFactory implements IXsdFacetFactory {

    protected ValueType valueType;

    @Override
    public List<XmlSchemaFacet> build(final XDNamedValue[] params) {
        XsdLogger.print(LOG_DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Building facets ...");

        List<XmlSchemaFacet> facets = new ArrayList<XmlSchemaFacet>();
        if (params != null && params.length > 0) {
            for (XDNamedValue param : params) {
                build(facets, param);
            }
        } else {
            XsdLogger.print(LOG_DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"No basic facets will be built - no input params");
        }

        XsdLogger.print(LOG_DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Building extra facets ...");
        extraFacets(facets);
        return facets;
    }

    @Override
    public XmlSchemaMinExclusiveFacet minExclusive(XDNamedValue param) {
        throw new UnsupportedOperationException("minExclusive");
    }

    @Override
    public XmlSchemaMinInclusiveFacet minInclusive(XDNamedValue param) {
        throw new UnsupportedOperationException("minInclusive");
    }

    @Override
    public XmlSchemaMaxExclusiveFacet maxExclusive(XDNamedValue param) {
        throw new UnsupportedOperationException("maxExclusive");
    }

    @Override
    public XmlSchemaMaxInclusiveFacet maxInclusive(XDNamedValue param) {
        throw new UnsupportedOperationException("maxInclusive");
    }

    @Override
    public XmlSchemaMinLengthFacet minLength(XDNamedValue param) {
        throw new UnsupportedOperationException("minLength");
    }

    @Override
    public XmlSchemaMaxLengthFacet maxLength(XDNamedValue param) {
        throw new UnsupportedOperationException("maxLength");
    }

    @Override
    public XmlSchemaLengthFacet length(XDNamedValue param) {
        throw new UnsupportedOperationException("length");
    }

    @Override
    public List<XmlSchemaPatternFacet> pattern(XDNamedValue param) {
        throw new UnsupportedOperationException("pattern");
    }

    @Override
    public XmlSchemaPatternFacet pattern(String value) {
        XsdLogger.print(LOG_DEBUG, TRANSFORMATION, this.getClass().getSimpleName(), "Pattern. Value=" + value);
        XmlSchemaPatternFacet facet = new XmlSchemaPatternFacet();
        facet.setValue(value);
        return facet;
    }

    @Override
    public List<XmlSchemaEnumerationFacet> enumeration(XDNamedValue param) {
        throw new UnsupportedOperationException("enumeration");
    }

    @Override
    public XmlSchemaFractionDigitsFacet fractionDigits(XDNamedValue param) {
        throw new UnsupportedOperationException("fractionDigits");
    }

    @Override
    public XmlSchemaTotalDigitsFacet totalDigits(XDNamedValue param) {
        throw new UnsupportedOperationException("totalDigits");
    }

    @Override
    public XmlSchemaWhiteSpaceFacet whitespace(XDNamedValue param) {
        throw new UnsupportedOperationException("whitespace");
    }

    @Override
    public boolean customFacet(List<XmlSchemaFacet> facets, XDNamedValue param) {
        return false;
    }

    @Override
    public void extraFacets(final List<XmlSchemaFacet> facets) {
    }

    @Override
    public void setValueType(final ValueType valueType) {
        this.valueType = valueType;
    }

    protected void build(final List<XmlSchemaFacet> facets, final XDNamedValue param) {
        XsdLogger.print(LOG_DEBUG, TRANSFORMATION, this.getClass().getSimpleName(), "Creating Facet. Type=" + param.getName());

        XmlSchemaFacet facet = null;

        if (XSD_FACET_ENUMERATION.equals(param.getName())) {
            facets.addAll(enumeration(param));
        } else if (XSD_FACET_MAX_EXCLUSIVE.equals(param.getName())) {
            facet = maxExclusive(param);
        } else if (XSD_FACET_MAX_INCLUSIVE.equals(param.getName())) {
            facet = maxInclusive(param);
        } else if (XSD_FACET_MIN_EXCLUSIVE.equals(param.getName())) {
            facet = minExclusive(param);
        } else if (XSD_FACET_MIN_INCLUSIVE.equals(param.getName())) {
            facet = minInclusive(param);
        } else if (XSD_FACET_LENGTH.equals(param.getName())) {
            facet = length(param);
        } else if (XSD_FACET_MAX_LENGTH.equals(param.getName())) {
            facet = maxLength(param);
        } else if (XSD_FACET_MIN_LENGTH.equals(param.getName())) {
            facet = minLength(param);
        } else if (XSD_FACET_FRACTION_DIGITS.equals(param.getName())) {
            facet = fractionDigits(param);
        } else if (XSD_FACET_PATTERN.equals(param.getName()) || XD_FACET_FORMAT.equals(param.getName())) {
            facets.addAll(pattern(param));
        } else if (XSD_FACET_TOTAL_DIGITS.equals(param.getName())) {
            facet = totalDigits(param);
        } else if (XSD_FACET_WHITESPACE.equals(param.getName())) {
            facet = whitespace(param);
        } else if (!customFacet(facets, param)) {
            XsdLogger.print(LOG_WARN, TRANSFORMATION, this.getClass().getSimpleName(),"Unsupported restriction parameter. Parameter=" + param.getName());
        }

        if (facet != null) {
            facets.add(facet);
        }
    }
}
