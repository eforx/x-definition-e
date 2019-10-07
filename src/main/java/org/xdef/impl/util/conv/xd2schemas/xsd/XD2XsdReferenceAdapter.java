package org.xdef.impl.util.conv.xd2schemas.xsd;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaImport;
import org.xdef.impl.XData;
import org.xdef.impl.XDefinition;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.xd2schemas.xsd.builder.XsdBaseBuilder;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.XmlSchemaImportLocation;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XD2XsdUtils;
import org.xdef.model.XMNode;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class XD2XsdReferenceAdapter {

    private final XsdBaseBuilder xsdBaseBuilder;
    private final XDTree2XsdAdapter xdTree2XsdAdapter;
    private final XmlSchema schema;
    private final Map<String, XmlSchemaImportLocation> importSchemaLocations;

    private Set<String> simpleTypeReferences;
    private Set<String> namespaceImports;

    public XD2XsdReferenceAdapter(XsdBaseBuilder xsdBaseBuilder, XDTree2XsdAdapter xdTree2XsdAdapter, XmlSchema schema, Map<String, XmlSchemaImportLocation> importSchemaLocations) {
        this.xsdBaseBuilder = xsdBaseBuilder;
        this.xdTree2XsdAdapter = xdTree2XsdAdapter;
        this.schema = schema;
        this.importSchemaLocations = importSchemaLocations;
    }

    /**
     * Creates following nodes:
     *      simpleType      - attribute type
     *      complexType     - element type
     *      import          - used namespaces in reference of attributes and elements
     * @param xDef
     * @param out
     */
    public void createRefsAndImports(XDefinition xDef, final PrintStream out) {
        simpleTypeReferences = new HashSet<String>();
        namespaceImports = new HashSet<String>();
        extractRefsFromAttrs(xDef, out);
    }

    private void extractRefsFromAttrs(XDefinition xDef, final PrintStream out) {
        Set<XMNode> processed = new HashSet<XMNode>();
        XElement[] elems = xDef.getXElements();

        // Extract all simple types and imports
        for (int i = 0; i < elems.length; i++) {
            extractRefsFromAttrs(elems[i], processed);
        }

        // Extract all complex types
        for (int i = 0; i < elems.length; i++) {
            if (!xdTree2XsdAdapter.getXdRootNames().contains(elems[i].getName())) {
                extractComplexTypeRefs(elems[i], out);
            }
        }
    }

    private void extractComplexTypeRefs(final XMNode xmNode, final PrintStream out) {
        XmlSchemaElement xsdElem = (XmlSchemaElement) xdTree2XsdAdapter.convertTree(xmNode, out, "|   ");
        XmlSchemaComplexType complexType = (XmlSchemaComplexType)xsdElem.getSchemaType();
        complexType.setName(xsdElem.getName());
        xsdBaseBuilder.addComplexType(complexType);
    }

    private void extractRefsFromAttrs(XMNode xn, final Set<XMNode> processed) {

        if (!processed.add(xn)) {
            //System.out.println("Already processed node (reference): " + xn.getName() + " (" + xn.getXDPosition() + ")");
            return;
        }

        short xdElemKind = xn.getKind();
        switch (xdElemKind) {
            case XNode.XMELEMENT: {
                XElement defEl = (XElement)xn;
                XMNode[] attrs = defEl.getXDAttrs();

                for (int i = 0; i < attrs.length; i++) {
                    addSimpleTypeReference((XData)attrs[i]);
                }

                if (defEl.isReference() && XD2XsdUtils.isExternalRef(defEl.getName(), defEl.getNSUri(), schema)) {
                    addSchemaImportFromElem(defEl.getNSUri(), defEl.getReferencePos());
                }

                for (int i = 0; i < defEl._childNodes.length; i++) {
                    extractRefsFromAttrs(defEl._childNodes[i], processed);
                }

                return;
            }
            case XNode.XMDEFINITION: {
                XDefinition def = (XDefinition)xn;
                XElement[] elems = def.getXElements();
                for (int i = 0; i < elems.length; i++){
                    extractRefsFromAttrs(elems[i], processed);
                }
                return;
            }
        }
    }

    private void addSimpleTypeReference(final XData xData) {
        final String refTypeName = xData.getRefTypeName();

        // Simple type node
        if (refTypeName != null && simpleTypeReferences.add(refTypeName)) {
            xsdBaseBuilder.creatSimpleTypeRef(xData);
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
