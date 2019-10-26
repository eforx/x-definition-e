package org.xdef.impl.util.conv.xd2schemas.xsd.builder.facet.array.param;

import org.xdef.XDNamedValue;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.RangeRegexGenerator;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;

import java.util.List;

import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.DEBUG;
import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.TRANSFORMATION;

public class IntegerRegexBuilder extends AbstractParamRegexBuilder {

    public IntegerRegexBuilder(int logLevel) {
        super(logLevel);
    }

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

        if (XsdLogger.isDebug(logLevel) && pattern != null) {
            XsdLogger.print(DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Pattern created=\"" + pattern + "\"");
        }

        return pattern;
    }
}
