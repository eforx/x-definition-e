package org.xdef.impl.util.conv.xd2schemas.xsd.util;

import org.apache.ws.commons.schema.*;
import org.xdef.impl.XElement;

import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.xdef.model.XMNode.*;

public class XD2XsdUtils {

    static private final Pattern ciPattern = Pattern.compile("[a-zA-Z]");

    public static void addElement(final XmlSchema schema, final XmlSchemaElement element) {
        schema.getItems().add(element);
    }

    public static void addAttr(final XmlSchema schema, final XmlSchemaAttribute attr) {
        schema.getItems().add(attr);
    }

    public static void addSchemaType(final XmlSchema schema, final XmlSchemaType schemaType) {
        schema.getItems().add(schemaType);
    }

    public static void removeItem(final XmlSchema schema, final XmlSchemaObject xmlObj) {
        schema.getItems().remove(xmlObj);
    }

    public static String particleXKindToString(short kind) {
        switch (kind) {
            case XMSEQUENCE: return "sequence";
            case XMMIXED: return "mixed";
            case XMCHOICE: return "choise";
        }

        return null;
    }

    public static String caseSensitiveValue2CIPattern(final String value) {
        final Matcher matcher = ciPattern.matcher(value);
        final StringBuilder stringBuilder = new StringBuilder();
        int lastMatchPos = 0;
        while (matcher.find()) {
            stringBuilder.append(value.substring(lastMatchPos, matcher.start()));
            stringBuilder.append("[" + matcher.group(0).toLowerCase() + matcher.group(0).toUpperCase() + "]");
            lastMatchPos = matcher.end();
        }

        return stringBuilder.toString();
    }

    public static String regexCollectionToSingleRegex(Collection<String> regex) {
        final StringBuilder stringBuilder = new StringBuilder();
        if (!regex.isEmpty()) {
            Iterator<String> itr = regex.iterator();
            stringBuilder.append(itr.next());
            while (itr.hasNext()) {
                stringBuilder.append("|" + itr.next());
            }
        }

        return stringBuilder.toString();
    }

    public static boolean isAnyElement(final XElement xElem) {
        return "$any".equals(xElem.getName());
    }

    public static boolean containsAnyElement(final XElement xElem) {
        return xElem.getName().endsWith("$any");
    }
}
