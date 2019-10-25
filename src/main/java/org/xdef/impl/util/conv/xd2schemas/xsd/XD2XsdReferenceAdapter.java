package org.xdef.impl.util.conv.xd2schemas.xsd;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaImport;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.xdef.impl.XData;
import org.xdef.impl.XDefinition;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.xd2schemas.xsd.builder.XsdElementBuilder;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.XmlSchemaImportLocation;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XD2XsdUtils;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;
import org.xdef.model.XMNode;

import javax.xml.namespace.QName;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class XD2XsdReferenceAdapter {

    private final XsdElementBuilder xsdBaseBuilder;
    private final XDTree2XsdAdapter xdTree2XsdAdapter;
    private final XmlSchema schema;
    private final Map<String, XmlSchemaImportLocation> importSchemaLocations;

    private Set<String> simpleTypeReferences;
    private Set<String> namespaceImports;
    private Set<String> systemIdImports;

    protected XD2XsdReferenceAdapter(XsdElementBuilder xsdBaseBuilder,
                                     XDTree2XsdAdapter xdTree2XsdAdapter,
                                     XmlSchema schema,
                                     Map<String, XmlSchemaImportLocation> importSchemaLocations) {
        this.xsdBaseBuilder = xsdBaseBuilder;
        this.xdTree2XsdAdapter = xdTree2XsdAdapter;
        this.schema = schema;
        this.importSchemaLocations = importSchemaLocations;
    }

    public final Set<String> getSystemIdImports() {
        return systemIdImports;
    }

    /**
     * Creates following nodes:
     *      simpleType      - attribute type
     *      complexType     - element type
     *      import          - used namespaces in reference of attributes and elements
     * @param xDef
     * @param out
     */
    protected void createRefsAndImports(XDefinition xDef, final PrintStream out) {
        simpleTypeReferences = new HashSet<String>();
        namespaceImports = new HashSet<String>();
        systemIdImports = new HashSet<String>();
        extractRefsAndImports(xDef, out);
    }

    private void extractRefsAndImports(XDefinition xDef, final PrintStream out) {
        XsdLogger.print(xDef, "Pre-processing: Creating references based on x-definition");

        final Set<XMNode> processed = new HashSet<XMNode>();
        final XElement[] elems = xDef.getXElements();

        // Extract all simple types and imports
        XsdLogger.print(xDef, "Pre-processing: Extracting simple references and imports ...");
        for (int i = 0; i < elems.length; i++) {
            extractSimpleRefsAndImports(elems[i], processed);
        }

        // Extract all complex types
        XsdLogger.print(xDef, "Pre-processing: Extracting complex references ...");
        for (int i = 0; i < elems.length; i++) {
            if (!xdTree2XsdAdapter.getXdRootNames().contains(elems[i].getName())) {
                extractElementRefs(elems[i], out);
            }
        }
    }

    private void extractElementRefs(final XMNode xmNode, final PrintStream out) {
        XmlSchemaElement xsdElem = (XmlSchemaElement) xdTree2XsdAdapter.convertTree(xmNode, out, "|   ");
        XmlSchemaType elementType = xsdElem.getSchemaType();
        if (elementType == null) {
            XD2XsdUtils.addElement(schema, xsdElem);
        } else if (elementType instanceof XmlSchemaType) {
            elementType.setName(xsdElem.getName());
            XD2XsdUtils.addRefType(schema, elementType);
        }
    }

    private void extractSimpleRefsAndImports(XMNode xn, final Set<XMNode> processed) {

        if (!processed.add(xn)) {
            //System.out.println("Already processed node (reference): " + xn.getName() + " (" + xn.getXDPosition() + ")");
            return;
        }

        switch (xn.getKind()) {
            case XNode.XMELEMENT: {
                XElement defEl = (XElement)xn;
                XMNode[] attrs = defEl.getXDAttrs();

                for (int i = 0; i < attrs.length; i++) {
                    addSimpleTypeReference((XData)attrs[i], true);
                }

                if (defEl.isReference()) {
                    if (XD2XsdUtils.isRefInDifferentNamespace(defEl.getName(), defEl.getNSUri(), schema)) {
                        addSchemaImportFromElem(defEl.getNSUri(), defEl.getReferencePos());
                    } else if (XD2XsdUtils.isRefInDifferentSystem(defEl.getReferencePos(), defEl.getXDPosition())) {
                        addSchemaImport(defEl.getReferencePos());
                    }
                }

                int childrenCount = defEl._childNodes.length;
                for (int i = 0; i < defEl._childNodes.length; i++) {
                    if (defEl._childNodes[i].getKind() == XNode.XMTEXT && childrenCount > 1) {
                        addSimpleTypeReference((XData) defEl._childNodes[i], false);
                    } else {
                        extractSimpleRefsAndImports(defEl._childNodes[i], processed);
                    }
                }

                return;
            }
            case XNode.XMDEFINITION: {
                XDefinition def = (XDefinition)xn;
                XElement[] elems = def.getXElements();
                for (int i = 0; i < elems.length; i++){
                    extractSimpleRefsAndImports(elems[i], processed);
                }
                return;
            }
        }
    }

    private void addSimpleTypeReference(final XData xData, boolean isAttrRef) {
        String refTypeName = xData.getRefTypeName();

        // Simple type node
        if (refTypeName != null && simpleTypeReferences.add(refTypeName)) {
            xsdBaseBuilder.creatSimpleTypeTop(xData, refTypeName);
            return;
        }

        if (isAttrRef == false && refTypeName == null && XD2XsdUtils.getDefaultSimpleParserQName(xData) == null && xData.getValueTypeName() != null) {
            refTypeName = XD2XsdUtils.createNameFromParser(xData);
            if (refTypeName != null && simpleTypeReferences.add(refTypeName)) {
                xsdBaseBuilder.creatSimpleTypeTop(xData, refTypeName);
                XsdLogger.print(xData, "Creating simple type reference from parser. ReferenceName=" + refTypeName);
                return;
            }
        }

        final String importNamespace = xData.getNSUri();
        if (importNamespace != null && XD2XsdUtils.isRefInDifferentNamespace(xData.getName(), importNamespace, schema)) {
            addSchemaImportFromAttr(importNamespace);
        }
    }

    private void addSchemaImport(final String referencePos) {
        final String refSystemId = XD2XsdUtils.getReferenceSystemId(referencePos);

        if (refSystemId == null || !systemIdImports.add(refSystemId)) {
            return;
        }

        XmlSchemaImport schemaImport = new XmlSchemaImport(schema);
        // TODO: Search for target namespace?
        schemaImport.setNamespace(refSystemId);
        if (importSchemaLocations != null && importSchemaLocations.containsKey(refSystemId)) {
            schemaImport.setSchemaLocation(importSchemaLocations.get(refSystemId).buildLocalition(refSystemId));
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
