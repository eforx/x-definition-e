package org.xdef.impl.util.conv.schema.xd2schema.xsd.factory.facet;

import org.apache.ws.commons.schema.*;
import org.xdef.XDNamedValue;
import org.xdef.impl.util.conv.schema.util.SchemaLogger;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.model.XsdAdapterCtx;

import java.util.ArrayList;
import java.util.List;

import static org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.AlgPhase.TRANSFORMATION;
import static org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.Xd2XsdDefinitions.*;
import static org.xdef.impl.util.conv.schema.util.SchemaLoggerDefs.*;

/**
 * Base class for transformation of facets
 */
public abstract class AbstractXsdFacetFactory implements IXsdFacetFactory {

    /**
     * XSD adapter context
     */
    protected XsdAdapterCtx adapterCtx;

    /**
     * X-definition value type
     */
    protected ValueType valueType;

    @Override
    public void setAdapterCtx(XsdAdapterCtx adapterCtx) {
        this.adapterCtx = adapterCtx;
    }

    @Override
    public List<XmlSchemaFacet> build(final XDNamedValue[] params) {
        SchemaLogger.print(LOG_DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Building facets ...");

        List<XmlSchemaFacet> facets = new ArrayList<XmlSchemaFacet>();
        if (params != null && params.length > 0) {
            for (XDNamedValue param : params) {
                build(facets, param);
            }
        } else {
            SchemaLogger.print(LOG_DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"No basic facets will be built - no input params");
        }

        SchemaLogger.print(LOG_DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Building extra facets ...");
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
        SchemaLogger.print(LOG_DEBUG, TRANSFORMATION, this.getClass().getSimpleName(), "Pattern. Value=" + value);
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

    /**
     * Creates facet from given x-definition parameter
     * @param facets    list of facets, where newly created facet will be inserted
     * @param param     x-definition parameter
     */
    protected void build(final List<XmlSchemaFacet> facets, final XDNamedValue param) {
        SchemaLogger.print(LOG_DEBUG, TRANSFORMATION, this.getClass().getSimpleName(), "Creating Facet. Type=" + param.getName());

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
        } else if (isInternalFacet(param)) {
            // Do nothing
        } else if (!customFacet(facets, param)) {
            SchemaLogger.print(LOG_WARN, TRANSFORMATION, this.getClass().getSimpleName(),"Unsupported restriction parameter. Parameter=" + param.getName());
        }

        if (facet != null) {
            facets.add(facet);
        }
    }

    /**
     * Check if given x-definition parameter is internal and should not be transformed
     * @param param     x-definition parameter
     * @return true, if given parameter is internal
     */
    private boolean isInternalFacet(XDNamedValue param) {
        return XD_INTERNAL_FACET_OUTFORMAT.equals(param.getName());
    }
}
