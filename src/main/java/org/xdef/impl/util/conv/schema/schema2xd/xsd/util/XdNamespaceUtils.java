package org.xdef.impl.util.conv.schema.schema2xd.xsd.util;

import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.constants.Constants;
import org.xdef.impl.util.conv.schema.schema2xd.xsd.model.XdAdapterCtx;

import javax.xml.namespace.QName;
import java.util.Set;

import static org.xdef.impl.util.conv.schema.schema2xd.xsd.definition.Xsd2XdDefinitions.XSD_DEFAULT_NAMESPACE_PREFIX;

public class XdNamespaceUtils {

    /**
     * Checks if given namespace prefix is default for x-definition
     * @param prefix    namespace prefix
     * @return  return true if if given namespace prefix is default
     */
    public static boolean isDefaultNamespacePrefix(final String prefix) {
        return Constants.XML_NS_PREFIX.equals(prefix)
                || Constants.XMLNS_ATTRIBUTE.equals(prefix)
                || XSD_DEFAULT_NAMESPACE_PREFIX.equals(prefix);
    }

    public static String getReferenceSchemaName(final XmlSchemaCollection schemaCollection, final QName refQName, final XdAdapterCtx xdAdapterCtx, boolean simple) {
        String schemaName = null;

        final Set<String> refXDefs = xdAdapterCtx.getXDefByNamespace(refQName.getNamespaceURI());
        if (refXDefs == null) {
            return schemaName;
        }

        if (refXDefs.size() == 1) {
            schemaName = refXDefs.iterator().next();
        } else {
            if (simple == false) {
                if (schemaName == null) {
                    final XmlSchemaType refSchemaType = schemaCollection.getTypeByQName(refQName);
                    if (refSchemaType != null) {
                        schemaName = xdAdapterCtx.getXmlSchemaName(refSchemaType.getParent());
                    }
                }

                if (schemaName == null) {
                    final XmlSchemaGroup refGroup = schemaCollection.getGroupByQName(refQName);
                    if (refGroup != null) {
                        schemaName = xdAdapterCtx.getXmlSchemaName(refGroup.getParent());
                    }
                }

                if (schemaName == null) {
                    final XmlSchemaElement refElem = schemaCollection.getElementByQName(refQName);
                    if (refElem != null) {
                        schemaName = xdAdapterCtx.getXmlSchemaName(refElem.getParent());
                    }
                }
            } else {
                if (schemaName == null) {
                    final XmlSchemaAttribute refAttr = schemaCollection.getAttributeByQName(refQName);
                    if (refAttr != null) {
                        schemaName = xdAdapterCtx.getXmlSchemaName(refAttr.getParent());
                    }
                }
            }
        }

        return schemaName;
    }

    public static XmlSchemaComplexType getReferenceComplexType(final XmlSchemaCollection schemaCollection, final QName refQName) {
        final XmlSchemaType refSchemaType = schemaCollection.getTypeByQName(refQName);
        if (refSchemaType instanceof XmlSchemaComplexType) {
            return (XmlSchemaComplexType)refSchemaType;
        }

        return null;
    }

}
