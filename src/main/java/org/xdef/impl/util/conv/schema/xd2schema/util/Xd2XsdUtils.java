package org.xdef.impl.util.conv.schema.xd2schema.util;

import javafx.util.Pair;
import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;
import org.xdef.impl.XElement;
import org.xdef.impl.util.conv.schema.xd2schema.definition.Xd2XsdFeature;
import org.xdef.impl.util.conv.schema.xd2schema.model.XsdAdapterCtx;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.xdef.model.XMNode.*;

public class Xd2XsdUtils {

    static private final Pattern ciPattern = Pattern.compile("[a-zA-Z]");

    /**
     * Add XSD schema type node to top level of given XSD schema
     * @param schema        XSD schema
     * @param schemaType    XSD schema type node
     */
    public static void addSchemaTypeNode2TopLevel(final XmlSchema schema, final XmlSchemaType schemaType) {
        schema.getItems().add(schemaType);
    }

    /**
     * Removes node from given XSD schema
     * @param schema        XSD schema
     * @param xmlObj        XSD node
     */
    public static void removeNode(final XmlSchema schema, final XmlSchemaObject xmlObj) {
        schema.getItems().remove(xmlObj);
    }

    /**
     * Convert x-definition particle kind to string
     * @param kind  x-definition particle kind
     * @return name of x-definition particle kind
     */
    public static String particleXKindToString(short kind) {
        switch (kind) {
            case XMSEQUENCE: return "sequence";
            case XMMIXED: return "mixed";
            case XMCHOICE: return "choise";
        }

        return null;
    }

    /**
     * Transform given regular expression to case insensitive regular expression
     * @param regex     sensitive case regular expression
     * @return case insensitive regular expression
     */
    public static String regex2CaseInsensitive(final String regex) {
        final Matcher matcher = ciPattern.matcher(regex);
        final StringBuilder stringBuilder = new StringBuilder();
        int lastMatchPos = 0;
        while (matcher.find()) {
            stringBuilder.append(regex.substring(lastMatchPos, matcher.start()));
            stringBuilder.append("[" + matcher.group(0).toLowerCase() + matcher.group(0).toUpperCase() + "]");
            lastMatchPos = matcher.end();
        }

        return stringBuilder.toString();
    }

    /**
     * Transform given collection of regular expressions to single regular expression (joined by '|')
     * @param regex     collection of regular expressions
     * @return single regular expression
     */
    public static String regexCollectionToSingle(Collection<String> regex) {
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

//    public static Pair<String, String> xPathSplitByAttr(final String xdPos) {
//        final int paramPos = xdPos.lastIndexOf("/@");
//        if (paramPos != -1) {
//            return new Pair(xdPos.substring(0, paramPos), xdPos.substring(paramPos + 1));
//        }
//
//        return null;
//    }
//
//    public static String relativeXPath(final String xPath, final String xPathNode) {
//        final int pos = xPath.indexOf(xPathNode);
//        if (pos != -1) {
//            return xPath.substring(pos + xPathNode.length() + 1);
//        }
//
//        return xPath;
//    }

    /**
     * Checks if given x-definition element is any type
     * @param xElem     x-definition element node
     * @return  true if x-definition element node is any
     */
    public static boolean isAnyElement(final XElement xElem) {
        return "$any".equals(xElem.getName());
    }

    /**
     * Checks if given x-definition element contains any node
     * @param xElem     x-definition element node
     * @return  true if x-definition element node contains any node
     */
    public static boolean containsAnyElement(final XElement xElem) {
        return xElem.getName().endsWith("$any");
    }

    /**
     * Calculates total occurrence of member nodes inside XSD all node
     * @param groupParticleAll  XSD all node
     * @param adapterCtx        XSD adapter context
     * @return total occurrence of nodes
     */
    public static Pair<Long, Long> calculateGroupAllMembersOccurrence(final XmlSchemaAll groupParticleAll, final XsdAdapterCtx adapterCtx) {
        final XmlSchemaObjectBase[] members = new XmlSchemaObjectBase[groupParticleAll.getItems().size()];
        groupParticleAll.getItems().toArray(members);
        return calculateGroupParticleMembersOccurrence(members, adapterCtx);
    }

    /**
     * Calculates total occurrence of member nodes inside XSD choice node
     * @param groupParticleChoice   XSD choice node
     * @param adapterCtx            XSD adapter context
     * @return total occurrence of nodes
     */
    public static Pair<Long, Long> calculateGroupChoiceMembersOccurrence(final XmlSchemaChoice groupParticleChoice, final XsdAdapterCtx adapterCtx) {
        final XmlSchemaObjectBase[] members = new XmlSchemaObjectBase[groupParticleChoice.getItems().size()];
        groupParticleChoice.getItems().toArray(members);
        return calculateGroupParticleMembersOccurrence(members, adapterCtx);
    }

    private static Pair<Long, Long> calculateGroupParticleMembersOccurrence(final XmlSchemaObjectBase[] members, final XsdAdapterCtx adapterCtx) {
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

        if (adapterCtx.hasEnableFeature(Xd2XsdFeature.XSD_ALL_UNBOUNDED)) {
            elementMaxOccursSum = Long.MAX_VALUE;
        }

        return new Pair<Long, Long>(elementMinOccursSum, elementMaxOccursSum);
    }

    /**
     * Features which should be enabled by default for transformation algorithm
     * @return default algorithm features
     */
    public static Set<Xd2XsdFeature> defaultFeatures() {
        Set<Xd2XsdFeature> features = new HashSet<Xd2XsdFeature>();
        features.addAll(Xd2XsdFeature.DEFAULT_POSTPROCESSING_FEATURES);
        return features;
    }
}
