package org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.array.regex;

import org.xdef.XDNamedValue;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.RangeRegexGenerator;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;

import java.util.List;

import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.AlgPhase.TRANSFORMATION;
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XD2XsdDefinitions.*;
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XsdLoggerDefs.LOG_DEBUG;
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XsdLoggerDefs.LOG_ERROR;

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

        final StringBuilder sb = new StringBuilder();

        try {
            // Build regular expression for list of integers
            RangeRegexGenerator rangeRegexGenerator = new RangeRegexGenerator();
            List<String> regexes = rangeRegexGenerator.getRegex(rangeMin, rangeMax);
            if (regexes.isEmpty() == false) {
                sb.append(regexes.get(0));
                for (int i = 1; i < regexes.size(); i++) {
                    sb.append("|" + regexes.get(i));
                }
            }
        } catch (NumberFormatException ex) {
            XsdLogger.print(LOG_ERROR, TRANSFORMATION, this.getClass().getSimpleName(),"Exception occurs while converting range to regex. Error=" + ex.getMessage());
        }

        final String pattern = sb.toString();
        XsdLogger.print(LOG_DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Pattern created=\"" + pattern + "\"");
        return pattern;
    }
}
