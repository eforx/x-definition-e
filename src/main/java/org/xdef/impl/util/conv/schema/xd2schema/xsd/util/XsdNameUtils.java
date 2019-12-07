package org.xdef.impl.util.conv.schema.xd2schema.xsd.util;

import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.constants.Constants;
import org.xdef.XDNamedValue;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.impl.XData;
import org.xdef.impl.XElement;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.model.UniqueConstraint;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.model.XsdAdapterCtx;

import javax.xml.namespace.QName;

import static org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.Xd2XsdDefinitions.*;

public class XsdNameUtils {

    /**
     * Parse x-definition reference node name from given x-definition reference position
     * @param refPos    x-definition reference position
     * @return x-definition reference node name
     */
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

    /**
     * Get x-definition node position without x-definition name
     * @param nodePos   x-definition node position
     * @return  position without x-definition name
     */
    public static String getXNodePath(final String nodePos) {
        int xdefSystemSeparatorPos = nodePos.indexOf('#');
        if (xdefSystemSeparatorPos != -1) {
            return nodePos.substring(xdefSystemSeparatorPos + 1);
        }

        return nodePos;
    }

    /**
     * Parse x-definition node name without target namespace (if using it)
     * @param schema    XSD schema
     * @param name      x-definition node name
     * @return  x-definition node name
     */
    public static String resolveName(final XmlSchema schema, final String name) {
        // Element's name contains target namespace prefix, we can remove this prefix
        if (XsdNamespaceUtils.usingTargetNamespace(schema, name)) {
            return name.substring(schema.getSchemaNamespacePrefix().length() + 1);
        }

        return name;
    }

    /**
     * Resolve XSD attribute node name and schema form
     * @param schema    XSD schema
     * @param attr      XSD attribute node
     * @param xName     x-definition node name
     */
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

    /**
     * Resolve XSD element node name and schema form
     * @param schema        XSD schema
     * @param xElem         x-definition element node
     * @param elem          XSD element node
     * @param adapterCtx    XSD adapter context
     */
    public static void resolveElementQName(final XmlSchema schema, final XElement xElem, final XmlSchemaElement elem, final XsdAdapterCtx adapterCtx) {
        if (elem.isRef()) {
            return;
        }

        if (elem.isTopLevel()) {
            String name = adapterCtx.getNameFactory().findTopLevelName(xElem);
            if (name == null) {
                name = getNodeNameWithoutPrefix(elem.getName());
                name = adapterCtx.getNameFactory().generateTopLevelName(xElem, name);
            }

            elem.setName(name);
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

    /**
     * Resolve XSD attribute node type
     * @param schema        XSD schema
     * @param xsdAttr       XSD attribute node
     */
    public static void resolveAttributeSchemaTypeQName(final XmlSchema schema, final XmlSchemaAttribute xsdAttr) {
        if (XmlSchemaForm.QUALIFIED.equals(schema.getAttributeFormDefault())) {
            final QName schemaTypeName = xsdAttr.getSchemaTypeName();
            if (schemaTypeName != null && !Constants.URI_2001_SCHEMA_XSD.equals(schemaTypeName.getNamespaceURI())) {
                xsdAttr.setSchemaTypeName(new QName(schema.getTargetNamespace(), schemaTypeName.getLocalPart()));
            }
        }
    }

    /**
     * Resolve XSD element node type
     * @param schema        XSD schema
     * @param xsdElem       XSD attribute node
     */
    public static void resolveElementSchemaTypeQName(final XmlSchema schema, final XmlSchemaElement xsdElem) {
        if (XmlSchemaForm.QUALIFIED.equals(schema.getElementFormDefault())) {
            final QName schemaTypeName = xsdElem.getSchemaTypeName();
            if (schemaTypeName != null && !Constants.URI_2001_SCHEMA_XSD.equals(schemaTypeName.getNamespaceURI())) {
                xsdElem.setSchemaTypeName(new QName(schema.getTargetNamespace(), schemaTypeName.getLocalPart()));
            }
        }
    }

    /**
     * Check if x-definition node name is not using namespace prefix while XSD schema is using target namespace prefix
     * @param schema    XSD schema
     * @param name      x-definition node name
     * @return true if x-definition node name is not using namespace prefix while XSD schema yes
     */
    public static boolean isUnqualifiedName(final XmlSchema schema, final String name) {
        return !XsdNamespaceUtils.containsNsPrefix(name) && schema.getSchemaNamespacePrefix() != null && !XSD_NAMESPACE_PREFIX_EMPTY.equals(schema.getSchemaNamespacePrefix());
    }

    /**
     * Parse x-definition node name without prefix
     * @param nodeName  x-definition node name
     * @return  x-definition node name
     */
    public static String getNodeNameWithoutPrefix(final String nodeName) {
        int nsPos = nodeName.indexOf(':');
        if (nsPos != -1) {
            return nodeName.substring(nsPos + 1);
        }

        return nodeName;
    }

    /**
     * Parse x-definition element node name without x-definition type
     * @param xElem     x-definition element node
     * @return  x-definition element node name
     */
    public static String getName(final XElement xElem) {
        int typeSepPos = xElem.getName().indexOf('$');
        if (typeSepPos <= 0) {
            return xElem.getName();
        }

        return xElem.getName().substring(0, typeSepPos);
    }

    /**
     * Parse x-definition unique set variable name
     * @param varTypeName   x-definition unique set variable type name
     * @return  x-definition unique set variable name
     */
    public static String getUniqueSetVarName(final String varTypeName) {
        final int pos = varTypeName.lastIndexOf('.');
        if (pos != -1) {
            final String res = varTypeName.substring(pos + 1);
            if (XD_UNIQUE_ID.equals(res) || XD_UNIQUE_IDREF.equals(res) || XD_UNIQUE_IDREFS.equals(res) || XD_UNIQUE_CHKID.equals(res)) {
                return getUniqueSetVarName(varTypeName.substring(0, pos));
            } else {
                return res;
            }
        }

        return "";
    }

    /**
     * Parse x-definition unique set variable type
     * @param varTypeName   x-definition unique set variable type name
     * @return  x-definition unique set variable type
     */
    public static UniqueConstraint.Type getUniqueSetVarType(final String varTypeName) {
        UniqueConstraint.Type ucType = UniqueConstraint.Type.UNK;
        if (varTypeName.endsWith(XD_UNIQUE_CHKID)) {
            ucType = UniqueConstraint.Type.CHKID;
        } else if (varTypeName.endsWith(XD_UNIQUE_ID)) {
            ucType = UniqueConstraint.Type.ID;
        }else if (varTypeName.endsWith(XD_UNIQUE_CHKIDS)) {
            ucType = UniqueConstraint.Type.CHKIDS;
        } else if (varTypeName.endsWith(XD_UNIQUE_IDREF)) {
            ucType = UniqueConstraint.Type.IDREF;
        } else if (varTypeName.endsWith(XD_UNIQUE_IDREFS)) {
            ucType = UniqueConstraint.Type.IDREFS;
        }

        return ucType;
    }

    /**
     * Parse x-definition unique set name
     * @param varTypeName   x-definition unique set variable type
     * @return  x-definition unique set name
     */
    public static String getUniqueSetName(final String varTypeName) {
        final int pos = varTypeName.indexOf('.');
        if (pos != -1) {
            return varTypeName.substring(0, pos);
        }

        return varTypeName;
    }

    /**
     * Creates reference name from given x-definition node
     * @param xData         x-definition node
     * @param adapterCtx    XSD adapter context
     * @return  reference name
     */
    public static String createRefNameFromParser(final XData xData, final XsdAdapterCtx adapterCtx) {
        final XDValue parseMethod = xData.getParseMethod();
        final String parserName = xData.getParserName();

        String name;
        QName defaultQName = Xd2XsdParserMapping.getDefaultParserQName(parserName, adapterCtx);
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
