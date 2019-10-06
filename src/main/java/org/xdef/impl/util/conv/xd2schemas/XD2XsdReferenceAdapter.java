package org.xdef.impl.util.conv.xd2schemas;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaImport;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import org.apache.ws.commons.schema.constants.Constants;
import org.xdef.XDNamedValue;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.impl.XData;
import org.xdef.impl.XDefinition;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.model.XMNode;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class XD2XsdReferenceAdapter {

    private final XmlSchema schema;
    private final Map<String, XmlSchemaImportLocation> importSchemaLocations;

    private Set<String> simpleTypeReferences;
    private Set<String> namespaceImports;

    public XD2XsdReferenceAdapter(final XmlSchema schema, final Map<String, XmlSchemaImportLocation> importSchemaLocations ) {
        this.schema = schema;
        this.importSchemaLocations = importSchemaLocations;
    }

    public void convertReferences(XMNode xn) {
        simpleTypeReferences = new HashSet<String>();
        namespaceImports = new HashSet<String>();
        extractRefsFromAttrs(xn);
    }

    private void extractRefsFromAttrs(XMNode xn) {
        short xdElemKind = xn.getKind();
        switch (xdElemKind) {
            case XNode.XMELEMENT: {
                XElement defEl = (XElement)xn;
                XMNode[] attrs = defEl.getXDAttrs();

                for (int i = 0; i < attrs.length; i++) {
                    addAttrTypeReference((XData)attrs[i]);
                }

                if (defEl.isReference() && XD2XsdUtils.isExternalRef(defEl.getName(), defEl.getNSUri(), schema)) {
                    addSchemaImportFromElem(defEl.getNSUri(), defEl.getReferencePos());
                }

                for (int i = 0; i < defEl._childNodes.length; i++) {
                    extractRefsFromAttrs(defEl._childNodes[i]);
                }

                return;
            }
            case XNode.XMDEFINITION: {
                XDefinition def = (XDefinition)xn;
                XElement[] elems = def.getXElements();
                for (int i = 0; i < elems.length; i++){
                    extractRefsFromAttrs(elems[i]);
                }
                return;
            }
        }
    }

    private void addAttrTypeReference(final XData xData) {
        final String refTypeName = xData.getRefTypeName();

        // Simple type node
        if (refTypeName != null && simpleTypeReferences.add(refTypeName)) {
            XmlSchemaSimpleType itemType = new XmlSchemaSimpleType(schema, true);
            itemType.setName(xData.getRefTypeName());

            XmlSchemaSimpleTypeRestriction restriction = null;
            final String parserName = xData.getParserName();
            XDValue parseMethod = xData.getParseMethod();
            if (parseMethod instanceof XDParser) {
                XDParser parser = ((XDParser)parseMethod);
                XDNamedValue parameters[] = parser.getNamedParams().getXDNamedItems();
                QName qName = XD2XsdUtils.parserNameToQName(parserName);
                if (qName != null) {
                    restriction = XsdRestrictionBuilder.buildRestriction(qName, xData, parameters);
                }
            } else {
                restriction = XsdRestrictionBuilder.buildRestriction(Constants.XSD_STRING, xData, null);
            }

            if (restriction == null) {
                throw new RuntimeException("Unknown reference type parser: " + parserName);
            }

            itemType.setContent(restriction);
            return;
        }

        final String importNamespace = xData.getNSUri();
        if (importNamespace != null && XD2XsdUtils.isExternalRef(xData.getName(), importNamespace, schema)) {
            addSchemaImportFromAttr(importNamespace);
        }
    }

    private void addSchemaImportFromElem(final String importNamespace, final String referencePos) {
        if (importNamespace == null || !namespaceImports.add(importNamespace)) {
            return;
        }

        XmlSchemaImport schemaImport = new XmlSchemaImport(schema);
        schemaImport.setNamespace(importNamespace);
        if (importSchemaLocations != null && importSchemaLocations.containsKey(importNamespace)) {
            schemaImport.setSchemaLocation(importSchemaLocations.get(importNamespace).buildLocalition(XD2XsdUtils.getReferenceSystemId(referencePos)));
        }
    }

    private void addSchemaImportFromAttr(final String importNamespace) {
        if (importNamespace == null || !namespaceImports.add(importNamespace)) {
            return;
        }

        XmlSchemaImport schemaImport = new XmlSchemaImport(schema);
        schemaImport.setNamespace(importNamespace);
        // TODO: Schema location?
        if (importSchemaLocations != null && importSchemaLocations.containsKey(importNamespace)) {
            schemaImport.setSchemaLocation(importSchemaLocations.get(importNamespace).buildLocalition(null));
        }
    }

}
