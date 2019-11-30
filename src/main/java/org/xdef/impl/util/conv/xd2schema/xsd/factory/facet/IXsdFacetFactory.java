package org.xdef.impl.util.conv.xd2schema.xsd.factory.facet;

import org.apache.ws.commons.schema.*;
import org.xdef.XDNamedValue;
import org.xdef.impl.util.conv.xd2schema.xsd.model.XsdAdapterCtx;

import java.util.List;

/**
 * Converts x-defnition XDNamedValue parameters into XSD facet facets
 */
public interface IXsdFacetFactory {

    enum ValueType {
        DECIMAL_INTEGER,
        DECIMAL_FLOATING,
        STRING
    }

    void setAdapterCtx(XsdAdapterCtx adapterCtx);

    /**
     * Creates facets from parameters
     * @param params
     * @return
     */
    List<XmlSchemaFacet> build(final XDNamedValue[] params);

    /**
     * Creates xs:minExclusive facet
     * @param param
     * @return
     */
    XmlSchemaMinExclusiveFacet minExclusive(final XDNamedValue param);

    /**
     * Creates xs:minInclusive facet
     * @param param
     * @return
     */
    XmlSchemaMinInclusiveFacet minInclusive(final XDNamedValue param);

    /**
     * Creates xs:maxExclusive facet
     * @param param
     * @return
     */
    XmlSchemaMaxExclusiveFacet maxExclusive(final XDNamedValue param);

    /**
     * Creates xs:maxInclusive facet
     * @param param
     * @return
     */
    XmlSchemaMaxInclusiveFacet maxInclusive(final XDNamedValue param);

    /**
     * Creates xs:minLength facet
     * @param param
     * @return
     */
    XmlSchemaMinLengthFacet minLength(final XDNamedValue param);

    /**
     * Creates xs:maxLength facet
     * @param param
     * @return
     */
    XmlSchemaMaxLengthFacet maxLength(final XDNamedValue param);

    /**
     * Creates xs:length facet
     * @param param
     * @return
     */
    XmlSchemaLengthFacet length(final XDNamedValue param);

    /**
     * Creates xs:pattern facet
     * @param param
     * @return
     */
    List<XmlSchemaPatternFacet> pattern(final XDNamedValue param);

    /**
     * Creates xs:pattern facet
     * @param value
     * @return
     */
    XmlSchemaPatternFacet pattern(final String value);

    /**
     * Creates xs:enumeration facet
     * @param param
     */
    List<XmlSchemaEnumerationFacet> enumeration(final XDNamedValue param);

    /**
     * Creates xs:fractionDigits facet
     * @param param
     * @return
     */
    XmlSchemaFractionDigitsFacet fractionDigits(final XDNamedValue param);

    /**
     * Creates xs:totalDigits facet
     * @param param
     * @return
     */
    XmlSchemaTotalDigitsFacet totalDigits(final XDNamedValue param);

    /**
     * Creates xs:whiteSpace facet
     * @param param
     * @return
     */
    XmlSchemaWhiteSpaceFacet whitespace(final XDNamedValue param);

    /**
     * Handling of custom facet
     * @param param
     * @return
     */
    boolean customFacet(final List<XmlSchemaFacet> facets, final XDNamedValue param);

    /**
     * Creates extra facets
     * @param facets
     */
    void extraFacets(final List<XmlSchemaFacet> facets);

    void setValueType(final ValueType valueType);
}
