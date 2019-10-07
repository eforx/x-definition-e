package org.xdef.impl.util.conv.xd2schemas.xsd;

import javafx.util.Pair;
import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.constants.Constants;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;
import org.xdef.XDParser;
import org.xdef.XDPool;
import org.xdef.impl.*;
import org.xdef.impl.util.conv.xd2schemas.XD2SchemaAdapter;
import org.xdef.impl.util.conv.xd2schemas.xsd.builder.XsdBaseBuilder;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.XmlSchemaImportLocation;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XD2XsdUtils;
import org.xdef.model.XMData;
import org.xdef.model.XMDefinition;
import org.xdef.model.XMNode;

import javax.xml.namespace.QName;
import java.io.PrintStream;
import java.util.*;

public class XD2XsdAdapter implements XD2SchemaAdapter<XmlSchema> {

    private boolean printXdTree = false;
    private XDefinition xDefinition = null;
    private String schemaName = null;
    private XmlSchema schema = null;
    private XsdBaseBuilder xsdBuilder = null;

    /**
     * ================ Input parameters ================
     */

    /**
     * Key:     namespace prefix
     * Value:   namespace URI
     */
    private Map<String, String> schemaNamespaces = new HashMap<String, String>();
    /**
     * Key:     schema namespace
     * Value:   schema location
     */
    private Map<String, XmlSchemaImportLocation> importSchemaLocations = new HashMap<String, XmlSchemaImportLocation>();
    private XmlSchemaForm elemSchemaForm = null;
    private XmlSchemaForm attrSchemaForm = null;
    private String targetNamespace = null;

    public void setPrintXdTree(boolean printXdTree) {
        this.printXdTree = printXdTree;
    }

    public final XDefinition getXDefinition() {
        return xDefinition;
    }

    public final String getSchemaName() {
        return schemaName;
    }

    public void setSchemaNamespaces(Map<String, String> schemaNamespaces) {
        this.schemaNamespaces = schemaNamespaces;
    }

    public void addSchemaNamespace(String prefix, String namespaceUri) {
        schemaNamespaces.put(prefix, namespaceUri);
    }

    public void setSchemaNamespaceLocations(Map<String, XmlSchemaImportLocation> schemaNamespaceLocations) {
        this.importSchemaLocations = schemaNamespaceLocations;
    }

    public void addSchemaNamespaceLocation(String namespaceUri, XmlSchemaImportLocation location) {
        importSchemaLocations.put(namespaceUri, location);
    }

    public void setElemSchemaForm(XmlSchemaForm elemSchemaForm) {
        this.elemSchemaForm = elemSchemaForm;
    }

    public void setAttrSchemaForm(XmlSchemaForm attrSchemaForm) {
        this.attrSchemaForm = attrSchemaForm;
    }

    public void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }

    @Override
    public XmlSchema createSchema(final XDPool xdPool) {

        if (xdPool == null) {
            throw new IllegalArgumentException("xdPool = null");
        }

        return createSchema(xdPool.getXMDefinition());
    }

    @Override
    public XmlSchema createSchema(final XDPool xdPool, final String xdefName) {

        if (xdPool == null) {
            throw new IllegalArgumentException("xdPool = null");
        }

        if (xdefName == null) {
            throw new IllegalArgumentException("xdefName = null");
        }

        return createSchema(xdPool.getXMDefinition(xdefName));
    }

    @Override
    public XmlSchema createSchema(XDPool xdPool, int xdefIndex) {
        if (xdPool == null) {
            throw new IllegalArgumentException("xdPool = null");
        }

        if (xdefIndex < 0) {
            throw new IllegalArgumentException("xdefIndex < 0");
        }

        XMDefinition xmDefinitions[] = xdPool.getXMDefinitions();

        if (xdefIndex > xmDefinitions.length) {
            throw new IllegalArgumentException("xdefIndex > xmDefinitions.length");
        }

        return createSchema(xmDefinitions[xdefIndex]);
    }

    @Override
    public XmlSchema createSchema(final XMDefinition xdef) {
        return createSchema(xdef, new XmlSchemaCollection()).getValue();
    }

    public Pair<String, XmlSchema> createSchema(final XMDefinition xdef, final XmlSchemaCollection xmlSchemaCollection) {

        if (xdef == null) {
            throw new IllegalArgumentException("xdef = null");
        }

        this.xDefinition = (XDefinition)xdef;

        // Initialize XSD schema
        initSchema(xmlSchemaCollection);

        // Extract all used references in x-definition
        XD2XsdReferenceAdapter referenceAdapter = new XD2XsdReferenceAdapter(xsdBuilder, schema, importSchemaLocations);
        referenceAdapter.convertReferences(xDefinition);

        // Convert x-definition tree to XSD tree
        Set<XMNode> processed = new HashSet<XMNode>();
        convertTree(xDefinition, System.out, processed, "");

        return new Pair<String, XmlSchema>(schemaName, schema);
    }

    private void initSchema(final XmlSchemaCollection xmlSchemaCollection) {
        schemaName = xDefinition.getName();
        schema = new XmlSchema(targetNamespace, schemaName, xmlSchemaCollection);

        if (elemSchemaForm != null) {
            schema.setElementFormDefault(elemSchemaForm);
        }
        if (attrSchemaForm != null) {
            schema.setAttributeFormDefault(attrSchemaForm);
        }

        xsdBuilder = new XsdBaseBuilder(schema);

        // Namespace initialization
        NamespaceMap namespaceMap = new NamespaceMap();
        namespaceMap.add("xs", Constants.URI_2001_SCHEMA_XSD);

        for (Map.Entry<String, String> entry : schemaNamespaces.entrySet()) {
            namespaceMap.add(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, String> entry : xDefinition._namespaces.entrySet()) {
            if (XD2XsdUtils.isDefaultNamespacePrefix(entry.getKey())) {
                continue;
            }

            if (!namespaceMap.containsKey(entry.getKey())) {
                namespaceMap.add(entry.getKey(), entry.getValue());
            } else {
                System.out.println("XSD schema already contains namespace " + entry.getKey());
            }
        }

        schema.setNamespaceContext(namespaceMap);
        if (targetNamespace != null) {
            schema.setSchemaNamespacePrefix(namespaceMap.getPrefix(targetNamespace));
        }
    }

    private XmlSchemaObject convertTree(
            XMNode xn,
            final PrintStream out,
            final Set<XMNode> processed,
            String outputPrefix) {

        if (outputPrefix != null && !processed.add(xn)) {
            System.out.println(outputPrefix + "Already processed node (reference): " + xn.getName() + " (" + xn.getXDPosition() + ")");
            return null;
        }

        short xdElemKind = xn.getKind();
        switch (xdElemKind) {
            case XNode.XMATTRIBUTE: {
                XMData xd = (XMData) xn;
                if (outputPrefix != null && printXdTree) {
                    out.print(outputPrefix + "|-- XMAttr: ");
                    displayDesriptor((XData)xn, out);
                }
                return xsdBuilder.createAttribute(xd.getName(), xd);
            }
            case XNode.XMTEXT: {
                XData xd = (XData) xn;
                if (outputPrefix != null && printXdTree) {
                    out.print(outputPrefix + "|-- XMText: ");
                    displayDesriptor(xd, out);
                }
                return xsdBuilder.createSimpleContent(xd);
            }
            case XNode.XMELEMENT: {
                XElement defEl = (XElement)xn;
                if (printXdTree) {
                    out.print(outputPrefix + "|-- XMElement: ");
                    displayDesriptor(defEl, out);
                }

                XmlSchemaElement xsdElem = xsdBuilder.createElement(defEl.getName(), defEl);
                XmlSchemaComplexType complexType = xsdBuilder.createComplexType();
                XmlSchemaGroupParticle group = null;
                boolean hasSimpleContent = false;
                boolean isReference = defEl.isReference();

                if (isReference) {
                    if (XD2XsdUtils.isExternalRef(defEl.getName(), defEl.getNSUri(), schema)) {
                        xsdElem.getRef().setTargetQName(new QName(defEl.getNSUri(), defEl.getName()));
                    } else {
                        xsdElem.setName(defEl.getName());
                        // TODO: reference namespace?
                        xsdElem.setSchemaTypeName(new QName("", XD2XsdUtils.getReferenceName(defEl.getReferencePos())));
                        xsdBuilder.resolveElementName(xsdElem);
                    }
                } else {
                    xsdElem.setName(defEl.getName());
                    XMNode[] attrs = defEl.getXDAttrs();

                    for(int i = 0; i < attrs.length; i++) {
                        convertTree(attrs[i], out, processed, outputPrefix + "|   ");
                    }

                    // If element contains only data, we dont have to create complexType
                    if (attrs.length == 0 && defEl._childNodes.length == 1 && defEl._childNodes[0].getKind() == XNode.XMTEXT) {
                        XData xd = (XData)defEl._childNodes[0];
                        // TODO: Should we lookup for simple type reference?
                        XmlSchemaSimpleType simpleType = xsdBuilder.creatSimpleType(xd);
                        if (simpleType != null) {
                            xsdElem.setType(simpleType);
                        } else {
                            // TODO: Has to be instance of XDParser?
                            if (xd.getParseMethod() instanceof XDParser) {
                                final String parserName = xd.getParserName();
                                xsdElem.setSchemaTypeName(XD2XsdUtils.parserNameToQName(parserName));
                            }
                        }
                    } else {
                        // Convert all children nodes
                        for (int i = 0; i < defEl._childNodes.length; i++) {
                            short childrenKind = defEl._childNodes[i].getKind();
                            // Particle nodes (sequence, choice, all)
                            if (childrenKind == XNode.XMSEQUENCE || childrenKind == XNode.XMMIXED || childrenKind == XNode.XMCHOICE) {
                                group = (XmlSchemaGroupParticle) convertTree(defEl._childNodes[i], out, processed, outputPrefix + "|   ");
                                complexType.setParticle(group);
                                outputPrefix += "|   ";
                            } else if (childrenKind == XNode.XMTEXT) { // Simple value node
                                XmlSchemaSimpleContent simpleContent = (XmlSchemaSimpleContent) convertTree(defEl._childNodes[i], out, processed, outputPrefix + "|   ");
                                for (int j = 0; j < attrs.length; j++) {
                                    ((XmlSchemaSimpleContentExtension) simpleContent.getContent()).getAttributes().add((XmlSchemaAttributeOrGroupRef) convertTree(attrs[j], out, processed, null));
                                }

                                complexType.setContentModel(simpleContent);
                                hasSimpleContent = true;
                            } else {
                                XmlSchemaObjectBase xsdChild = convertTree(defEl._childNodes[i], out, processed, outputPrefix + "|   ");
                                if (xsdChild != null) {
                                    if (group instanceof XmlSchemaSequence) {
                                        ((XmlSchemaSequence) group).getItems().add((XmlSchemaSequenceMember) xsdChild);
                                    } else if (group instanceof XmlSchemaChoice) {
                                        ((XmlSchemaChoice) group).getItems().add((XmlSchemaChoiceMember) xsdChild);
                                    } else if (group instanceof XmlSchemaAll) {
                                        ((XmlSchemaAll) group).getItems().add((XmlSchemaAllMember) xsdChild);
                                    }
                                }
                            }
                        }

                        if (hasSimpleContent == false) {
                            for (int i = 0; i < attrs.length; i++) {
                                complexType.getAttributes().add((XmlSchemaAttributeOrGroupRef) convertTree(attrs[i], out, processed, null));
                            }
                        }

                        xsdElem.setType(complexType);
                    }

                    xsdBuilder.resolveElementName(xsdElem);
                }

                return xsdElem;
            }
            case XNode.XMSELECTOR_END:
                if (printXdTree) {
                    out.println(outputPrefix + "|-- End of selector: ");
                }
                return null;
            case XNode.XMSEQUENCE:
            case XNode.XMMIXED:
            case XNode.XMCHOICE:
                if (printXdTree) {
                    displaySelector(xn, out, outputPrefix);
                }
                return xsdBuilder.createGroup(xdElemKind, xn.getOccurence());
            case XNode.XMDEFINITION: {
                XDefinition def = (XDefinition)xn;
                if (printXdTree) {
                    out.print(outputPrefix + "XMDefinition: ");
                    displayDesriptor(def, out);
                }

                List<String> rootElemNames = new ArrayList<String>();
                if (def._rootSelection != null && def._rootSelection.size() > 0) {
                    Iterator<String> e=def._rootSelection.keySet().iterator();
                    rootElemNames.add(e.next());
                    if (printXdTree) {
                        out.println("|-- Root: " + rootElemNames.get(0));
                    }
                    while (e.hasNext()) {
                        rootElemNames.add(e.next());
                        if (printXdTree) {
                            out.println("    | " + rootElemNames.get(rootElemNames.size() - 1));
                        }
                    }
                } else {
                    if (printXdTree) {
                        out.println(outputPrefix + "|-- Root: null");
                    }
                }

                XElement[] elems = def.getXElements();
                for (int i = 0; i < elems.length; i++){
                    if (rootElemNames.contains(elems[i].getName())) {
                        XmlSchemaElement xsdElem = (XmlSchemaElement) convertTree(elems[i], out, processed, outputPrefix + "|   ");
                        xsdBuilder.addElement(xsdElem);
                    } else {
                        XmlSchemaElement xsdElem = (XmlSchemaElement) convertTree(elems[i], out, processed, outputPrefix + "|   ");
                        XmlSchemaComplexType complexType = (XmlSchemaComplexType)xsdElem.getSchemaType();
                        complexType.setName(xsdElem.getName());
                        xsdBuilder.addComplexType(complexType);
                    }
                }
                return null;
            }
            default: {
                out.println(outputPrefix + "UNKNOWN: " + xn.getName() + "; " + xn.getKind());
            }
        }

        return null;
    }

    private static void displaySelector(final XMNode xn, final PrintStream out, final String outputPrefix) {
        XSelector xsel = (XSelector) xn;
        switch (xsel.getKind()) {
            case XNode.XMSEQUENCE:
                out.print(outputPrefix + "|-- Sequence: ");
                break;
            case XNode.XMMIXED:
                out.print(outputPrefix + "|-- Mixed:");
                break;
            case XNode.XMCHOICE:
                out.print(outputPrefix + "|-- Choice:");
                break;
            default:
                return;
        }
        out.print("min=" + xsel.minOccurs());
        out.print(",max=" + xsel.maxOccurs());
        out.print(",beg=" + xsel.getBegIndex());
        out.print(",end=" + xsel.getEndIndex());
        if (xsel.isSelective()) {
            out.print(",selective");
        }
        if (xsel.isIgnorable()) {
            out.print(",ignorable");
        }
        if (xsel.isEmptyDeclared()) {
            out.print(",emptyDeclared");
        }
        if (xsel.isEmptyFlag()) {
            out.print(",empty");
        }
        if (xsel.getMatchCode() >= 0) {
            out.print(",match=" + xsel.getMatchCode());
        }
        if (xsel.getInitCode() >= 0) {
            out.print(",init=" + xsel.getInitCode());
        }
        if (xsel.getComposeCode() >= 0) {
            out.print(",setSourceMethod=" + xsel.getComposeCode());
        }
        if (xsel.getOnAbsenceCode() >= 0) {
            out.print(",absence=" + xsel.getOnAbsenceCode());
        }
        if (xsel.getOnExcessCode() >= 0) {
            out.print(",excess=" + xsel.getOnAbsenceCode());
        }
        if (xsel.getFinallyCode() >= 0) {
            out.print(",finally=" + xsel.getFinallyCode());
        }
        out.println();
    }

    private static String printOption(final String s,
                                      final String name,
                                      final byte value) {
        if (value == 0) {
            return s;
        }
        return (s.length() > 0 ? s + "\n": "") + name + "=" + (char) value;
    }

    private static void displayDesriptor(final XCodeDescriptor sc,
                                         final PrintStream out) {
        out.print(sc.getName() + " " + sc.minOccurs() + ".."
                + (sc.maxOccurs() == Integer.MAX_VALUE ? "*" :
                String.valueOf(sc.maxOccurs())));
        if (sc.getKind() == XNode.XMELEMENT) {
            if (((XElement)sc)._forget != 0) {
                out.print("forget= " + (char) ((XElement)sc)._forget);
            }
        } else if (sc.getKind() == XNode.XMATTRIBUTE ||
                sc.getKind() == XNode.XMTEXT) {
            out.print(" (" + ((XMData) sc).getValueTypeName() + ")");
        }
        if (sc._check >= 0) {
            out.print(",check=" + sc._check);
        }
        if (sc._deflt >= 0) {
            out.print(",default=" + sc._deflt);
        }
        if (sc._compose >= 0) {
            out.print(",compose=" + sc._compose);
        }
        if (sc._finaly >= 0) {
            out.print(",finally=" + sc._finaly);
        }
        if (sc._varinit >= 0) {
            out.print(",varinit=" + sc._varinit);
        }
        if (sc._init >= 0) {
            out.print(",init=" + sc._init);
        }
        if (sc._onAbsence >= 0) {
            out.print(",onAbsence=" + sc._onAbsence);
        }
        if (sc._onStartElement >= 0) {
            out.print(",onStartElement=" + sc._onStartElement);
        }
        if (sc._onExcess >= 0) {
            out.print(",onExcess=" + sc._onExcess);
        }
        if (sc._onTrue >= 0) {
            out.print(",onTrue=" + sc._onTrue);
        }
        if (sc._onFalse >= 0) {
            out.print(",onFalse=" + sc._onFalse);
        }
        if (sc._onIllegalAttr >= 0) {
            out.print(",onIllegalAttr=" + sc._onIllegalAttr);
        }
        if (sc._onIllegalText >= 0) {
            out.print(",onIllegalText=" + sc._onIllegalText);
        }
        if (sc._onIllegalElement >= 0) {
            out.print(",onIllegalElement=" + sc._onIllegalElement);
        }
        if (sc._match >= 0) {
            out.print(",match=" + sc._match);
        }
        out.println();
        String s = printOption("", "attrWhiteSpaces", sc._attrWhiteSpaces);
        s = printOption(s, "attrWhiteSpaces", sc._attrWhiteSpaces);
        s = printOption(s, "ignoreComments", sc._ignoreComments);
        s = printOption(s, "ignoreEmptyAttributes", sc._ignoreEmptyAttributes);
        s = printOption(s, "textWhiteSpaces", sc._textWhiteSpaces);
        s = printOption(s, "moreAttributes", sc._moreAttributes);
        s = printOption(s, "moreElements", sc._moreElements);
        s = printOption(s, "moreText", sc._moreText);
        s = printOption(s, "attrValuesCase", sc._attrValuesCase);
        s = printOption(s, "textValuesCase", sc._textValuesCase);
        s = printOption(s, "trimAttr", sc._trimAttr);
        s = printOption(s, "acceptQualifiedAttr", sc._acceptQualifiedAttr);
        s = printOption(s, "trimText", sc._trimText);
        if (s.length() > 0) {
            out.println(s);
        }
    }

}
