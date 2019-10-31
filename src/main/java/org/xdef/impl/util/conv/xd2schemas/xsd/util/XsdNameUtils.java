package org.xdef.impl.util.conv.xd2schemas.xsd.util;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.xdef.XDNamedValue;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.impl.XData;

import javax.xml.namespace.QName;

import static org.xdef.impl.util.conv.xd2schemas.xsd.XD2XsdDefinitions.XSD_NAMESPACE_PREFIX_EMPTY;

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
            attr.setName(XsdNamespaceUtils.getNoneNameWithoutPrefix(xName));
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
            elem.setName(XsdNamespaceUtils.getNoneNameWithoutPrefix(elem.getName()));
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

    public static boolean isUnqualifiedName(final XmlSchema schema, final String name) {
        // Element's name without namespace prefix, while xml is using target namespace
        return name.indexOf(':') == -1 && schema.getSchemaNamespacePrefix() != null && !XSD_NAMESPACE_PREFIX_EMPTY.equals(schema.getSchemaNamespacePrefix());
    }

    public static String createRefNameFromParser(final XData xData) {
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
}
