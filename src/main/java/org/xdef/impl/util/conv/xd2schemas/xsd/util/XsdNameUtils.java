package org.xdef.impl.util.conv.xd2schemas.xsd.util;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaForm;

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
        String newName = resolveName(schema, xName);
        if (!xName.equals(newName)) {
            attr.setName(newName);
        } else if (XmlSchemaForm.QUALIFIED.equals(schema.getAttributeFormDefault()) && isUnqualifiedName(schema, xName)) {
            attr.setForm(XmlSchemaForm.UNQUALIFIED);
        }
    }


    public static void resolveElementQName(final XmlSchema schema, final XmlSchemaElement elem) {
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
}
