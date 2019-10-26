package org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet;

import org.apache.ws.commons.schema.*;
import org.xdef.XDNamedValue;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;

import java.util.ArrayList;
import java.util.List;

import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.*;

public abstract class AbstractXsdFacetFactory implements IXsdFacetFactory {

    protected int logLevel;
    protected ValueType valueType;

    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    @Override
    public List<XmlSchemaFacet> build(final XDNamedValue[] params) {
        if (XsdLogger.isDebug(logLevel)) {
            XsdLogger.print(DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Building facets...");
        }

        List<XmlSchemaFacet> facets = new ArrayList<XmlSchemaFacet>();
        if (params != null) {
            for (XDNamedValue param : params) {
                build(facets, param);
            }
        }

        if (XsdLogger.isDebug(logLevel)) {
            XsdLogger.print(DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Adding extra facets...");
        }

        extraFacets(facets, params);

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
    public void extraFacets(final List<XmlSchemaFacet> facets, final XDNamedValue[] params) {
    }

    @Override
    public void setValueType(final ValueType valueType) {
        this.valueType = valueType;
    }

    protected void build(final List<XmlSchemaFacet> facets, final XDNamedValue param) {
        if ("maxLength".equals(param.getName())) {
            facets.add(maxLength(param));
        } else if ("minLength".equals(param.getName())) {
            facets.add(minLength(param));
        } else if ("whiteSpace".equals(param.getName())) {
            facets.add(whitespace(param));
        } else if ("pattern".equals(param.getName()) || "format".equals(param.getName())) {
            facets.addAll(pattern(param));
        } else if ("minInclusive".equals(param.getName())) {
            facets.add(minInclusive(param));
        } else if ("minExclusive".equals(param.getName())) {
            facets.add(minExclusive(param));
        } else if ("maxInclusive".equals(param.getName())) {
            facets.add(maxInclusive(param));
        } else if ("maxExclusive".equals(param.getName())) {
            facets.add(maxExclusive(param));
        } else if ("argument".equals(param.getName()) || "enumeration".equals(param.getName())) {
            facets.addAll(enumeration(param));
        } else if ("length".equals(param.getName())) {
            facets.add(length(param));
        } else if ("fractionDigits".equals(param.getName())) {
            facets.add(fractionDigits(param));
        } else if ("totalDigits".equals(param.getName())) {
            facets.add(totalDigits(param));
        } else if (!customFacet(facets, param)) {
            System.out.println("Unknown reference type parameter: " + param.getName());
        }
    }
}
