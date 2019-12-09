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

    /**
     * Finds reference schema name from given parameters.
     *
     * If multiple schemas using target namespace equals to reference namespace URI have been found, then we try to find proper schema name by searching
     * node by reference qualified name in particular schema.
     *
     * @param schemaCollection      XSD document collection
     * @param refQName              reference qualified name
     * @param xdAdapterCtx          x-definition adapter context
     * @param simple                flag, if only reference should be XSD simple type or XSD attribute node
     * @return reference schema name if found, otherwise null
     */
    public static String findReferenceSchemaName(final XmlSchemaCollection schemaCollection, final QName refQName, final XdAdapterCtx xdAdapterCtx, boolean simple) {
        String schemaName = null;

        final Set<String> refXDefs = xdAdapterCtx.findXDefByNamespace(refQName.getNamespaceURI());
        if (refXDefs == null) {
            return schemaName;
        }

        if (refXDefs.size() == 1) {
            schemaName = refXDefs.iterator().next();
        } else {
            if (simple == false) {
                if (schemaName == null) {
                    final XmlSchemaType refSchemaType = schemaCollection.getTypeByQName(refQName);
                    if (refSchemaType != null && refSchemaType instanceof XmlSchemaComplexType) {
                        schemaName = xdAdapterCtx.findXmlSchemaName(refSchemaType.getParent());
                    }
                }

                if (schemaName == null) {
                    final XmlSchemaGroup refGroup = schemaCollection.getGroupByQName(refQName);
                    if (refGroup != null) {
                        schemaName = xdAdapterCtx.findXmlSchemaName(refGroup.getParent());
                    }
                }

                if (schemaName == null) {
                    final XmlSchemaElement refElem = schemaCollection.getElementByQName(refQName);
                    if (refElem != null) {
                        schemaName = xdAdapterCtx.findXmlSchemaName(refElem.getParent());
                    }
                }
            } else {
                if (schemaName == null) {
                    final XmlSchemaType refSchemaType = schemaCollection.getTypeByQName(refQName);
                    if (refSchemaType != null && refSchemaType instanceof XmlSchemaSimpleType) {
                        schemaName = xdAdapterCtx.findXmlSchemaName(refSchemaType.getParent());
                    }

                    final XmlSchemaAttribute refAttr = schemaCollection.getAttributeByQName(refQName);
                    if (refAttr != null) {
                        schemaName = xdAdapterCtx.findXmlSchemaName(refAttr.getParent());
                    }
                }
            }
        }

        return schemaName;
    }

}
