package org.xdef.impl.util.conv.xd2schemas.xsd.util;

import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.constants.Constants;
import org.xdef.XDNamedValue;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.impl.XData;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.SchemaNode;

import javax.xml.namespace.QName;

import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XD2XsdDefinitions.*;

public class XsdNameUtils {

    public static String getReferenceName(final String refPos) {
        int xdefNamespaceSeparatorPos = refPos.indexOf(':');
        if (xdefNamespaceSeparatorPos != -1) {
            return refPos.substring(xdefNamespaceSeparatorPos + 1);
        }

        int xdefSystemSeparatorPos = refPos.indexOf('#');
        if (xdefSystemSeparatorPos != -1) {
            return refPos.substring(xdefSystemSeparatorPos + 1);
        }

        return refPos;
    }

    public static String getReferenceNodePath(final String refPos) {
        int xdefSystemSeparatorPos = refPos.indexOf('#');
        if (xdefSystemSeparatorPos != -1) {
            return refPos.substring(xdefSystemSeparatorPos + 1);
        }

        return refPos;
    }

    /**
     * Returns name without target namespace
     * @param schema
     * @param name
     * @return
     */
    public static String resolveName(final XmlSchema schema, final String name) {
        // Element's name contains target namespace prefix, we can remove this prefix
        if (XsdNamespaceUtils.usingTargetNamespace(schema, name)) {
            return name.substring(schema.getSchemaNamespacePrefix().length() + 1);
        }

        return name;
    }

    public static void resolveAttributeQName(final XmlSchema schema, final XmlSchemaAttribute attr, final String xName) {
        if (attr.isRef()) {
            return;
        }

        if (attr.isTopLevel()) {
            attr.setName(getNodeNameWithoutPrefix(xName));
            return;
        }

        String newName = resolveName(schema, xName);
        if (!xName.equals(newName)) {
            attr.setName(newName);
        } else if (XmlSchemaForm.QUALIFIED.equals(schema.getAttributeFormDefault()) && isUnqualifiedName(schema, xName)) {
            attr.setForm(XmlSchemaForm.UNQUALIFIED);
        }
    }

    public static void resolveElementQName(final XmlSchema schema, final XmlSchemaElement elem) {
        if (elem.isRef()) {
            return;
        }

        if (elem.isTopLevel()) {
            elem.setName(getNodeNameWithoutPrefix(elem.getName()));
            return;
        }

        final String name = elem.getName();
        final String newName = resolveName(schema, name);

        if (!name.equals(newName)) {
            elem.setName(newName);
        } else if (XmlSchemaForm.QUALIFIED.equals(schema.getElementFormDefault()) && isUnqualifiedName(schema, name)) {
            elem.setForm(XmlSchemaForm.UNQUALIFIED);
        }
    }

    public static void resolveAttributeSchemaTypeQName(final XmlSchema schema, final SchemaNode schemaNode) {
        if (XmlSchemaForm.QUALIFIED.equals(schema.getAttributeFormDefault())) {
            final QName schemaTypeName = schemaNode.toXsdAttr().getSchemaTypeName();
            if (schemaTypeName != null && !Constants.URI_2001_SCHEMA_XSD.equals(schemaTypeName.getNamespaceURI())) {
                schemaNode.toXsdAttr().setSchemaTypeName(new QName(schema.getTargetNamespace(), schemaTypeName.getLocalPart()));
            }
        }
    }

    public static void resolveElementSchemaTypeQName(final XmlSchema schema, final SchemaNode schemaNode) {
        if (XmlSchemaForm.QUALIFIED.equals(schema.getElementFormDefault())) {
            final QName schemaTypeName = schemaNode.toXsdElem().getSchemaTypeName();
            if (schemaTypeName != null && !Constants.URI_2001_SCHEMA_XSD.equals(schemaTypeName.getNamespaceURI())) {
                schemaNode.toXsdElem().setSchemaTypeName(new QName(schema.getTargetNamespace(), schemaTypeName.getLocalPart()));
            }
        }
    }

    public static boolean isUnqualifiedName(final XmlSchema schema, final String name) {
        // Element's name without namespace prefix, while xml is using target namespace
        return !XsdNamespaceUtils.containsNsPrefix(name) && schema.getSchemaNamespacePrefix() != null && !XSD_NAMESPACE_PREFIX_EMPTY.equals(schema.getSchemaNamespacePrefix());
    }

    public static String getNodeNameWithoutPrefix(final String nodeName) {
        int nsPos = nodeName.indexOf(':');
        if (nsPos != -1) {
            return nodeName.substring(nsPos + 1);
        }

        return nodeName;
    }

    public static String newTopLocalRefName(final String name) {
        return name;
    }

    public static String newRootElemName(final String name, final XmlSchemaType schemaType) {
        return newElemenPrefix(schemaType) + "root_" + name;
    }

    public static String newLocalScopeRefTypeName(final XData xData) {
        return xData.isLocalType() ? "refLoc_" + xData.getRefTypeName() : xData.getRefTypeName();
    }

    public static String newUnionRefTypeName(final String nodeName, final String localPartName) {
        return nodeName + "_union_" + localPartName;
    }

    private static String newElemenPrefix(XmlSchemaType schemaType) {
        if (schemaType != null) {
            return newElemenPrefix(schemaType instanceof XmlSchemaComplexType);
        }

        return "";
    }

    private static String newElemenPrefix(boolean isComplexType) {
        if (isComplexType) {
            return "ct_";
        } else {
            return "st_";
        }
    }

    public static String createRefNameFromParser(final XData xData) {
        final XDValue parseMethod = xData.getParseMethod();
        final String parserName = xData.getParserName();

        String name;
        QName defaultQName = XD2XsdParserMapping.getDefaultParserQName(parserName);
        if (defaultQName != null) {
            name = defaultQName.getLocalPart();
        } else {
            name = parserName;
        }

        if (!Constants.XSD_STRING.getLocalPart().equals(name)
                && !XD_PARSER_CDATA.equals(name)
                && !Constants.XSD_INT.getLocalPart().equals(name)
                && !Constants.XSD_LONG.getLocalPart().equals(name)) {
            return null;
        }

        final StringBuilder sb = new StringBuilder(name);

        if (parseMethod instanceof XDParser) {
            XDParser parser = ((XDParser)parseMethod);
            for (XDNamedValue p : parser.getNamedParams().getXDNamedItems()) {
                if (XSD_FACET_MAX_LENGTH.equals(p.getName())) {
                    sb.append("_maxl" + p.getValue().intValue());
                } else if (XSD_FACET_MIN_LENGTH.equals(p.getName())) {
                    sb.append("_minl" + p.getValue().intValue());
                } else if (XSD_FACET_WHITESPACE.equals(p.getName())) {
                    sb.append("_w");
                } else if (XSD_FACET_PATTERN.equals(p.getName()) || XD_FACET_FORMAT.equals(p.getName())) {
                    sb.append("_p");
                } else if (XSD_FACET_MIN_INCLUSIVE.equals(p.getName())) {
                    sb.append("_minI" + p.getValue().intValue());
                } else if (XSD_FACET_MIN_EXCLUSIVE.equals(p.getName())) {
                    sb.append("_minE" + p.getValue().intValue());
                } else if (XSD_FACET_MAX_INCLUSIVE.equals(p.getName())) {
                    sb.append("_maxI" + p.getValue().intValue());
                } else if (XSD_FACET_MAX_EXCLUSIVE.equals(p.getName())) {
                    sb.append("_maxE" + p.getValue().intValue());
                } else if (XD_FACET_ARGUMENT.equals(p.getName()) || XSD_FACET_ENUMERATION.equals(p.getName())) {
                    sb.append("_e");
                } else if (XSD_FACET_LENGTH.equals(p.getName())) {
                    sb.append("_l" + p.getValue().intValue());
                } else if (XSD_FACET_FRACTION_DIGITS.equals(p.getName())) {
                    sb.append("_fd");
                } else if (XSD_FACET_TOTAL_DIGITS.equals(p.getName())) {
                    sb.append("_td");
                }
            }
        }

        return name;
    }
}
