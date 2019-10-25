package org.xdef.impl.util.conv.xd2schemas.xsd.util;

import javafx.util.Pair;
import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.constants.Constants;
import org.xdef.XDConstants;
import org.xdef.XDNamedValue;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.impl.XData;
import org.xdef.impl.XDefinition;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.xd2schemas.xsd.builder.facet.*;
import org.xdef.impl.util.conv.xd2schemas.xsd.builder.facet.array.ListFacetBuilder;
import org.xdef.impl.util.conv.xd2schemas.xsd.builder.facet.array.UnionFacetBuilder;

import javax.xml.namespace.QName;

import java.util.Iterator;
import java.util.Map;

import static org.xdef.impl.util.conv.xd2schemas.xsd.XD2XsdDefinitions.*;

public class XD2XsdUtils {

    /**
     * Convert xd parser name to xsd QName
     * @param parserName
     * @return
     */
    public static QName getDefaultQName(final String parserName) {
        if ("CDATA".equals(parserName)) {
            return Constants.XSD_STRING;
        } else if ("string".equals(parserName)) {
            return Constants.XSD_STRING;
        } else if ("int".equals(parserName)) {
            return Constants.XSD_INT;
        } else if ("long".equals(parserName)) {
            return Constants.XSD_LONG;
        } else if (XD_PARSER_DEC.equals(parserName)) {
            return Constants.XSD_DECIMAL;
        } else if ("double".equals(parserName)) {
            return Constants.XSD_DOUBLE;
        } else if ("float".equals(parserName)) {
            return Constants.XSD_FLOAT;
        } else if ("enum".equals(parserName)) {
            return Constants.XSD_STRING;
        } else if ("gMonth".equals(parserName)) {
            return Constants.XSD_MONTH;
        } else if ("gMonthDay".equals(parserName)) {
            return Constants.XSD_MONTHDAY;
        } else if ("gYear".equals(parserName) || "ISOyear".equals(parserName)) {
            return Constants.XSD_YEAR;
        } else if ("gDay".equals(parserName)) {
            return Constants.XSD_DAY;
        } else if ("time".equals(parserName)) {
            return Constants.XSD_TIME;
        } else if ("dateTime".equals(parserName) || "ISOdateTime".equals(parserName)) {
            return Constants.XSD_DATETIME;
        } else if ("ISOyearMonth".equals(parserName)) {
            return Constants.XSD_YEARMONTH;
        } else if ("ISOdate".equals(parserName) || "date".equals(parserName)) {
            return Constants.XSD_DATE;
        }

        return null;
    }

    public static Pair<QName, IXsdFacetBuilder> getDefaultFacetBuilder(final String parserName) {
        QName qName = getDefaultQName(parserName);
        if (qName != null) {
            return new Pair(qName, new DefaultFacetBuilder());
        }

        return null;
    }

    /**
     * Some xd types requires specific way how to create simpleType and restrictions
     * @param parserName x-definition parser name
     * @return  QName - qualified XML name
     *          Boolean - use also default facet facets builder
     */
    public static Pair<QName, IXsdFacetBuilder> getCustomFacetBuilder(final String parserName, final XDNamedValue[] parameters) {
        if (XD_PARSER_AN.equals(parserName)) {
            return new Pair(Constants.XSD_STRING, new AnFacetBuilder());
        } else if (XD_PARSER_NUM.equals(parserName)) {
            return new Pair(Constants.XSD_STRING, new NumFacetBuilder());
        } else if (XD_PARSER_XDATETIME.equals(parserName)) {
            return new Pair(Constants.XSD_STRING, new DateTimeFacetBuilder());
        } else if (XD_PARSER_LIST.equals(parserName)) {
            ListFacetBuilder facetBuilder = new ListFacetBuilder();
            return new Pair(facetBuilder.determineBaseType(parameters), facetBuilder);
        } else if (XD_PARSER_UNION.equals(parserName)) {
            UnionFacetBuilder facetBuilder = new UnionFacetBuilder();
            return new Pair(facetBuilder.determineBaseType(parameters), facetBuilder);
        }

        return null;
    }

    // If name contains ":" or reference has different namespace, then element contains external reference
    public static boolean isRefInDifferentNamespace(final String nodeName, final String namespaceUri, final XmlSchema schema) {
        return hasNamespace(nodeName) && (namespaceUri != null && !namespaceUri.equals(schema.getTargetNamespace()));
    }

    public static boolean isRefInDifferentSystem(final String nodeRefName, final String xdPos) {
        final String nodeSystemId = getReferenceSystemId(xdPos);
        final String refSystemId = getReferenceSystemId(nodeRefName);
        return !hasNamespace(xdPos) && !hasNamespace(refSystemId) && !nodeSystemId.equals(refSystemId);
    }

    public static String getReferenceName(final String reference) {
        int xdefNamespaceSeparatorPos = reference.indexOf(':');
        if (xdefNamespaceSeparatorPos != -1) {
            return reference.substring(xdefNamespaceSeparatorPos + 1);
        }

        int xdefSystemSeparatorPos = reference.indexOf('#');
        if (xdefSystemSeparatorPos != -1) {
            return reference.substring(xdefSystemSeparatorPos + 1);
        }

        return reference;
    }

    private static boolean hasNamespace(final String name) {
        return name.indexOf(':') != -1;
    }

    public static String getReferenceSystemId(final String reference) {
        int xdefSystemSeparatorPos = reference.indexOf('#');
        if (xdefSystemSeparatorPos != -1) {
            return reference.substring(0, xdefSystemSeparatorPos);
        }

        return null;
    }

    public static String getNamespacePrefix(final String name) {
        int nsPos = name.indexOf(':');
        if (nsPos != -1) {
            return name.substring(0, nsPos);
        }

        return null;
    }

    public static String getNamespaceOrRefPrefix(final String name) {
        String res = getReferenceSystemId(name);
        if (res == null) {
            res = getNamespacePrefix(name);
        }

        return res;
    }

    public static boolean isDefaultNamespacePrefix(final String prefix) {
        return Constants.XML_NS_PREFIX.equals(prefix)
                || Constants.XMLNS_ATTRIBUTE.equals(prefix)
                || XDConstants.XDEF_NS_PREFIX.equals(prefix);
    }

    public static void resolveElementName(final XmlSchema schema, final XmlSchemaElement elem) {
        final String name = elem.getName();
        final String newName = resolveName(schema, name);

        if (!name.equals(newName)) {
            elem.setName(newName);
        } else if (XmlSchemaForm.QUALIFIED.equals(schema.getElementFormDefault()) && isUnqualifiedName(schema, name)) {
            elem.setForm(XmlSchemaForm.UNQUALIFIED);
        }
    }

    /**
     * Returns true if name is using schema target namespace
     * @param schema
     * @param name
     * @return
     */
    public static boolean usingTargetNamespace(final XmlSchema schema, final String name) {
        return schema.getSchemaNamespacePrefix() != null && name.startsWith(schema.getSchemaNamespacePrefix() + ':');
    }

    public static Pair<String, String> getSchemaTargetNamespace(final XDefinition xDef, Boolean targetNamespaceError) {
        String targetNamespacePrefix = null;
        String targetNamespaceUri = null;

        // Get target namespace prefix based on root elements
        if (xDef._rootSelection != null && xDef._rootSelection.size() > 0) {
            Iterator<String> e = xDef._rootSelection.keySet().iterator();
            while (e.hasNext()) {
                String tmpNs = XD2XsdUtils.getNamespacePrefix(e.next());
                if (targetNamespacePrefix == null) {
                    targetNamespacePrefix = tmpNs;
                } else if (tmpNs != null && !targetNamespacePrefix.equals(tmpNs)) {
                    System.out.println("[" + xDef.getName() + "] Expected namespace: " + targetNamespacePrefix + ", given: " + tmpNs);
                    targetNamespaceError = true;
                }
            }
        }

        if (targetNamespaceError == true) {
            return new Pair<String, String>(targetNamespacePrefix, targetNamespaceUri);
        }

        // Find target namespace URI based on x-definition namespaces
        if (targetNamespacePrefix != null) {
            for (Map.Entry<String, String> entry : xDef._namespaces.entrySet()) {
                if (targetNamespacePrefix.equals(entry.getKey())) {
                    targetNamespaceUri = entry.getValue();
                    break;
                }
            }
        }

        if (targetNamespacePrefix != null && targetNamespaceUri == null) {
            System.out.println("[" + xDef.getName() + "] Target namespace URI has been not found for prefix: " + targetNamespacePrefix);
            targetNamespaceError = true;
        }

        if (targetNamespaceError == true) {
            return new Pair<String, String>(targetNamespacePrefix, targetNamespaceUri);
        }

        // Try to find default namespace
        if (targetNamespacePrefix == null && targetNamespaceUri == null) {
            for (Map.Entry<String, String> entry : xDef._namespaces.entrySet()) {
                if ("".equals(entry.getKey())) {
                    targetNamespacePrefix = entry.getKey();
                    targetNamespaceUri = entry.getValue();
                    break;
                }
            }
        }

        // Create namespace from x-definition name
        /*if (targetNamespacePrefix == null && targetNamespaceUri == null) {
            targetNamespacePrefix = XD2XsdUtils.createNsPrefixFromXDefName(xDef.getName());
            targetNamespaceUri = XD2XsdUtils.createNsUriFromXDefName(xDef.getName());
        }*/

        return new Pair<String, String>(targetNamespacePrefix, targetNamespaceUri);
    }

    /**
     * Returns name without target namespace
     * @param schema
     * @param name
     * @return
     */
    public static String resolveName(final XmlSchema schema, final String name) {
        // Element's name contains target namespace prefix, we can remove this prefix
        if (usingTargetNamespace(schema, name)) {
            return name.substring(schema.getSchemaNamespacePrefix().length() + 1);
        }

        return name;
    }

    public static boolean isUnqualifiedName(final XmlSchema schema, final String name) {
        // Element's name without namespace prefix, while xml is using target namespace
        return name.indexOf(':') == -1 && schema.getSchemaNamespacePrefix() != null && !XSD_NAMESPACE_PREFIX_EMPTY.equals(schema.getSchemaNamespacePrefix());
    }

    public static void addElement(final XmlSchema schema, final XmlSchemaElement element) {
        schema.getItems().add(element);
    }

    public static void addRefType(final XmlSchema schema, final XmlSchemaType schemaType) {
        schema.getItems().add(schemaType);
    }

    public static QName getDefaultSimpleParserQName(final XData xData) {
        final XDValue parseMethod = xData.getParseMethod();
        final String parserName = xData.getParserName();

        QName defaultQName = XD2XsdUtils.getDefaultQName(parserName);

        // TODO: Has to be instance of XDParser?
        if (defaultQName != null && parseMethod instanceof XDParser) {
            XDParser parser = ((XDParser)parseMethod);
            XDNamedValue parameters[] = parser.getNamedParams().getXDNamedItems();
            if (parameters.length == 0 && XD2XsdUtils.getCustomFacetBuilder(parserName, parameters) == null) {
                return defaultQName;
            }
        }

        return null;
    }

    public static String createNameFromParser(final XData xData) {
        final XDValue parseMethod = xData.getParseMethod();
        final String parserName = xData.getParserName();

        String name;
        QName defaultQName = XD2XsdUtils.getDefaultQName(parserName);
        if (defaultQName != null) {
            name = defaultQName.getLocalPart();
        } else {
            name = parserName;
        }

        if (!"string".equals(name) && !"CDATA".equals(name) && !"int".equals(name) && !"long".equals(name)) {
            // TODO: Add to debug print
            XsdLogger.print(xData, "Unsupported parser type for creating of reference name. ParserName=" + parserName);
            return null;
        }

        if (parseMethod instanceof XDParser) {
            XDParser parser = ((XDParser)parseMethod);
            for (XDNamedValue p : parser.getNamedParams().getXDNamedItems()) {
                if ("maxLength".equals(p.getName())) {
                    name += "_maxl" + p.getValue().intValue();
                } else if ("minLength".equals(p.getName())) {
                    name += "_minl" + p.getValue().intValue();
                } else if ("whiteSpace".equals(p.getName())) {
                    name += "_w";
                } else if ("pattern".equals(p.getName()) || "format".equals(p.getName())) {
                    name += "_p";
                } else if ("minInclusive".equals(p.getName())) {
                    name += "_minI" + p.getValue().intValue();
                } else if ("minExclusive".equals(p.getName())) {
                    name += "_minE" + p.getValue().intValue();
                } else if ("maxInclusive".equals(p.getName())) {
                    name += "_maxI" + p.getValue().intValue();
                } else if ("maxExclusive".equals(p.getName())) {
                    name += "_maxE" + p.getValue().intValue();
                } else if ("argument".equals(p.getName()) || "enumeration".equals(p.getName())) {
                    name += "_e";
                } else if ("length".equals(p.getName())) {
                    name += "_l" + p.getValue().intValue();
                } else if ("fractionDigits".equals(p.getName())) {
                    name += "_fd";
                } else if ("totalDigits".equals(p.getName())) {
                    name += "_td";
                }
            }
        }

        return name;
    }

    public static String createNsPrefixFromXDefName(final String name) {
        return "ns_xDef_" + name;
    }

    public static String createNsUriFromXDefName(final String name) {
        return name;
    }


}
