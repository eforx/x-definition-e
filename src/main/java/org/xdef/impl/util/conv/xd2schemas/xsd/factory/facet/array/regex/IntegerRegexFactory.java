package org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.array.regex;

import org.xdef.XDNamedValue;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.RangeRegexGenerator;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;

import java.util.List;

import static org.xdef.impl.util.conv.xd2schemas.xsd.util.AlgPhase.TRANSFORMATION;
import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.LOG_DEBUG;

public class IntegerRegexFactory extends AbstractParamRegexFactory {

    public String regex(final XDNamedValue[] params) {
        Integer rangeMin = null;
        Integer rangeMax = null;

        for (XDNamedValue param : params) {
            if ("minInclusive".equals(param.getName())) {
                rangeMin = param.getValue().intValue();
            } else if ("minExclusive".equals(param.getName())) {
                rangeMin = param.getValue().intValue() + 1;
            } else if ("maxInclusive".equals(param.getName())) {
                rangeMax = param.getValue().intValue();
            } else if ("maxExclusive".equals(param.getName())) {
                rangeMax = param.getValue().intValue() - 1;
            }
        }

        if (rangeMin == null) {
            rangeMin = 0;
        }

        if (rangeMax == null) {
            rangeMax = Integer.MAX_VALUE;
        }

        // Build regular expression for list of integers
        RangeRegexGenerator g = new RangeRegexGenerator();
        List<String> regexs = g.getRegex(rangeMin, rangeMax);
        regexs.contains(0);
        String pattern = null;
        if (regexs.isEmpty() == false) {
            pattern = regexs.get(0);
            for (int i = 1; i < regexs.size(); i++) {
                pattern += "|" + regexs.get(i);
            }
        }

        XsdLogger.print(LOG_DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Pattern created=\"" + pattern + "\"");
        return pattern;
    }
}
