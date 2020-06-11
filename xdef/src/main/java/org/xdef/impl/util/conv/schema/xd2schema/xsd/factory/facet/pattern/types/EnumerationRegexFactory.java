package org.xdef.impl.util.conv.schema.xd2schema.xsd.factory.facet.pattern.types;

import org.xdef.XDContainer;
import org.xdef.XDNamedValue;
import org.xdef.XDValue;
import org.xdef.impl.util.conv.schema.util.SchemaLogger;

import static org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.AlgPhase.TRANSFORMATION;
import static org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.Xd2XsdDefinitions.XSD_FACET_ENUMERATION;
import static org.xdef.impl.util.conv.schema.util.SchemaLoggerDefs.LOG_DEBUG;

public class EnumerationRegexFactory implements RegexFactory {

    @Override
    public String regex(final XDNamedValue[] params) {
        String pattern = "";

        for (XDNamedValue param : params) {
            if (XSD_FACET_ENUMERATION.equals(param.getName())) {
                if (param.getValue().getItemId() == XDValue.XD_CONTAINER) {
                    pattern = containerValuesToPattern((XDContainer) param.getValue());
                }
            }
        }

        SchemaLogger.print(LOG_DEBUG, TRANSFORMATION, this.getClass().getSimpleName(),"Pattern created=\"" + pattern + "\"");
        return pattern;
    }

    public static String containerValuesToPattern(XDContainer xdContainer) {
        final StringBuilder sb = new StringBuilder();
        for (XDValue value : xdContainer.getXDItems()) {
            // Remove all new lines and leading whitespaces on new line
            String strValue = value.stringValue().replaceAll("\\n *", " ");
            if (sb.length() == 0) {
                sb.append(strValue);
            } else {
                sb.append("|" + strValue);
            }
        }

        return sb.toString();
    }

    public static String containerValuesToPattern(String[] values) {
        final StringBuilder sb = new StringBuilder();
        for (String value : values) {
            // Remove all new lines and leading whitespaces on new line
            String strValue = value.replaceAll("\\n *", " ");
            if (sb.length() == 0) {
                sb.append(strValue);
            } else {
                sb.append("|" + strValue);
            }
        }

        return sb.toString();
    }
}
