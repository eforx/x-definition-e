package org.xdef.impl.util.conv.xd2schemas.xsd.util;

import javafx.util.Pair;
import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;
import org.xdef.impl.XElement;
import org.xdef.impl.util.conv.xd2schemas.xsd.definition.XD2XsdFeature;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.XsdAdapterCtx;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
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

    public static Pair<String, String> xPathSplitByAttr(final String xdPos) {
        final int paramPos = xdPos.lastIndexOf("/@");
        if (paramPos != -1) {
            return new Pair(xdPos.substring(0, paramPos), xdPos.substring(paramPos + 1));
        }

        return null;
    }

    public static String relativeXPath(final String xPath, final String xPathNode) {
        final int pos = xPath.indexOf(xPathNode);
        if (pos != -1) {
            return xPath.substring(pos + xPathNode.length() + 1);
        }

        return xPath;
    }

    public static boolean isAnyElement(final XElement xElem) {
        return "$any".equals(xElem.getName());
    }

    public static boolean containsAnyElement(final XElement xElem) {
        return xElem.getName().endsWith("$any");
    }

    public static Pair<Long, Long> calculateGroupAllMembersOccurrence(final XmlSchemaAll groupParticleAll, final XsdAdapterCtx adapterCtx) {
        final XmlSchemaObjectBase[] members = new XmlSchemaObjectBase[groupParticleAll.getItems().size()];
        groupParticleAll.getItems().toArray(members);
        return calculateGroupParticleMembersOccurrence(members, adapterCtx);
    }

    public static Pair<Long, Long> calculateGroupChoiceMembersOccurrence(final XmlSchemaChoice groupParticleChoice, final XsdAdapterCtx adapterCtx) {
        final XmlSchemaObjectBase[] members = new XmlSchemaObjectBase[groupParticleChoice.getItems().size()];
        groupParticleChoice.getItems().toArray(members);
        return calculateGroupParticleMembersOccurrence(members, adapterCtx);
    }

    public static Pair<Long, Long> calculateGroupParticleMembersOccurrence(final XmlSchemaObjectBase[] members, final XsdAdapterCtx adapterCtx) {
        long elementMaxOccursSum = 0;
        long elementMinOccursSum = 0;

        for (XmlSchemaObjectBase member : members) {
            if (member instanceof XmlSchemaParticle) {
                final XmlSchemaParticle memberParticle = (XmlSchemaParticle) member;
                if (memberParticle.getMaxOccurs() < Long.MAX_VALUE) {
                    elementMaxOccursSum += memberParticle.getMaxOccurs();
                } else {
                    elementMaxOccursSum = Long.MAX_VALUE;
                    break;
                }

                if (memberParticle.getMinOccurs() < Long.MAX_VALUE) {
                    elementMinOccursSum += memberParticle.getMinOccurs();
                } else {
                    elementMinOccursSum = Long.MAX_VALUE;
                    break;
                }
            }
        }

        if (adapterCtx.hasEnableFeature(XD2XsdFeature.XSD_ALL_UNBOUNDED)) {
            elementMaxOccursSum = Long.MAX_VALUE;
        }

        return new Pair<Long, Long>(elementMinOccursSum, elementMaxOccursSum);
    }

    public static Set<XD2XsdFeature> defaultFeatures() {
        Set<XD2XsdFeature> features = new HashSet<XD2XsdFeature>();
        features.addAll(XD2XsdFeature.DEFAULT_POSTPROCESSING_FEATURES);
        return features;
    }
}
