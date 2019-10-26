package org.xdef.impl.util.conv.xd2schemas.xsd.factory.facet.array.regex;

import org.xdef.XDContainer;
import org.xdef.XDNamedValue;
import org.xdef.XDValue;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;

import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.DEBUG;
import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.TRANSFORMATION;

public class EnumerationRegexFactory extends AbstractParamRegexFactory {

    public EnumerationRegexFactory(int logLevel) {
        super(logLevel);
    }

    public String regex(final XDNamedValue[] params) {

        String pattern = "";

        for (XDNamedValue param : params) {
            if ("enumeration".equals(param.getName())) {
                if (param.getValue().getItemId() == XDValue.XD_CONTAINER) {
                    for (XDValue value : ((XDContainer) param.getValue()).getXDItems()) {
                        // Remove all new lines and leading whitespaces on new line
                        String strValue = value.stringValue().replaceAll("\\n *", " ");
                        if (!pattern.isEmpty()) {
                            pattern += "|" + strValue;
                        } else {
                            pattern = strValue;
                        }
                    }
                }
            }
        }

        if (XsdLogger.isDebug(logLevel) && pattern != null) {
            XsdLogger.print(DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Pattern created=\"" + pattern + "\"");
        }

        return pattern;
    }
}
