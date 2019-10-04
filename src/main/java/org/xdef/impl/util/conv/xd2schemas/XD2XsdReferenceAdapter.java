package org.xdef.impl.util.conv.xd2schemas;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import org.xdef.XDNamedValue;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.impl.XData;
import org.xdef.impl.XDefinition;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.model.XMNode;

import java.util.HashSet;
import java.util.Set;

public class XD2XsdReferenceAdapter {

    private final XmlSchema schema;
    private Set<String> references;

    public XD2XsdReferenceAdapter(XmlSchema schema) {
        this.schema = schema;
    }

    protected void convertReferences(XMNode xn) {
        references = new HashSet<String>();
        Set<XMNode> processed = new HashSet<XMNode>();
        convertTree(xn, processed);
    }

    private void convertTree(XMNode xn, final Set<XMNode> processed) {

        if (!processed.add(xn)) {
            return;
        }

        short xdElemKind = xn.getKind();
        switch (xdElemKind) {
            case XNode.XMELEMENT: {
                XElement defEl = (XElement)xn;
                XMNode[] attrs = defEl.getXDAttrs();

                for(int i = 0; i < attrs.length; i++) {
                    addReference((XData)attrs[i]);
                }

                for (int i = 0; i < defEl._childNodes.length; i++) {
                    convertTree(defEl._childNodes[i], processed);
                }

                return;
            }
            case XNode.XMDEFINITION: {
                XDefinition def = (XDefinition)xn;
                XElement[] elems = def.getXElements();
                for (int i = 0; i < elems.length; i++){
                    convertTree(elems[i], processed);
                }
                return;
            }
        }
    }

    private void addReference(final XData xData) {
        final String refTypeName = xData.getRefTypeName();

        if (references.contains(refTypeName)) {
            return;
        }

        references.add(refTypeName);

        XmlSchemaSimpleType itemType = new XmlSchemaSimpleType(schema, true);
        itemType.setName(xData.getRefTypeName());

        XmlSchemaSimpleTypeRestriction restriction = null;
        final String parserName = xData.getParserName();
        XDValue parseMethod = xData.getParseMethod();
        if (parseMethod instanceof XDParser) {
            XDParser parser = ((XDParser)parseMethod);
            XDNamedValue parameters[] = parser.getNamedParams().getXDNamedItems();
            if ("CDATA".equals(parserName)) {
                restriction = XsdReferenceBuilder.stringReference(xData, parameters);
            } else if ("string".equals(parserName)) {
                restriction = XsdReferenceBuilder.stringReference(xData, parameters);
            } else if ("int".equals(parserName)) {
                restriction = XsdReferenceBuilder.intReference(xData, parameters);
            } else if ("long".equals(parserName)) {
                restriction = XsdReferenceBuilder.longReference(xData, parameters);
            } else if ("double".equals(parserName)) {
                restriction = XsdReferenceBuilder.doubleReference(xData, parameters);
            } else if ("float".equals(parserName)) {
                restriction = XsdReferenceBuilder.floatReference(xData, parameters);
            } else {
                System.out.println("Unknown reference type parser: "+ parserName);
            }
        } else {
            restriction = XsdReferenceBuilder.stringReference(xData, null);
        }

        if (restriction == null) {
            throw new RuntimeException("Unknown reference type parser: " + parserName);
        }

        itemType.setContent(restriction);
    }

}
