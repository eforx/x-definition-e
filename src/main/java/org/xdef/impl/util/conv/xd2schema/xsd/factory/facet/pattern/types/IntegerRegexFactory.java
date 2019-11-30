package org.xdef.impl.util.conv.xd2schema.xsd.factory.facet.pattern.types;

import org.xdef.XDNamedValue;
import org.xdef.impl.util.conv.xd2schema.xsd.util.RangeRegexGenerator;
import org.xdef.impl.util.conv.xd2schema.xsd.util.XD2XsdUtils;
import org.xdef.impl.util.conv.schema.util.XsdLogger;

import java.util.List;

import static org.xdef.impl.util.conv.xd2schema.xsd.definition.AlgPhase.TRANSFORMATION;
import static org.xdef.impl.util.conv.xd2schema.xsd.definition.XD2XsdDefinitions.*;
import static org.xdef.impl.util.conv.schema.util.XsdLoggerDefs.LOG_DEBUG;
import static org.xdef.impl.util.conv.schema.util.XsdLoggerDefs.LOG_ERROR;

public class IntegerRegexFactory implements RegexFactory {

    @Override
    public String regex(final XDNamedValue[] params) {
        Integer rangeMin = null;
        Integer rangeMax = null;

        for (XDNamedValue param : params) {
            if (XSD_FACET_MAX_EXCLUSIVE.equals(param.getName())) {
                rangeMax = param.getValue().intValue() + 1;
            } else if (XSD_FACET_MAX_INCLUSIVE.equals(param.getName())) {
                rangeMax = param.getValue().intValue();
            } else if (XSD_FACET_MIN_EXCLUSIVE.equals(param.getName())) {
                rangeMin = param.getValue().intValue() - 1;
            } else if (XSD_FACET_MIN_INCLUSIVE.equals(param.getName())) {
                rangeMin = param.getValue().intValue();
            }
        }

        if (rangeMin == null) {
            rangeMin = 0;
        }

        if (rangeMax == null) {
            rangeMax = 1000;
        }

        String pattern = "";

        try {
            // Build regular expression for list of integers
            final RangeRegexGenerator rangeRegexGenerator = new RangeRegexGenerator();
            final List<String> regex = rangeRegexGenerator.getRegex(rangeMin, rangeMax);
            pattern = XD2XsdUtils.regexCollectionToSingle(regex);
        } catch (NumberFormatException ex) {
            XsdLogger.print(LOG_ERROR, TRANSFORMATION, this.getClass().getSimpleName(),"Exception occurs while converting range to regex. Error=" + ex.getMessage());
        }

        XsdLogger.print(LOG_DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Pattern created=\"" + pattern + "\"");
        return pattern;
    }
}
