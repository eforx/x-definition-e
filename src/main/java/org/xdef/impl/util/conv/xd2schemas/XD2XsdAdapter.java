package org.xdef.impl.util.conv.xd2schemas;

import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.constants.Constants;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;
import org.xdef.XDPool;
import org.xdef.impl.*;
import org.xdef.model.XMData;
import org.xdef.model.XMDefinition;
import org.xdef.model.XMNode;

import java.io.PrintStream;
import java.util.*;

public class XD2XsdAdapter implements XD2SchemaAdapter<XmlSchema>  {

    private Map<String, String> schemaNamespaces = new HashMap<String, String>();
    private XmlSchema schema = null;
    private XsdBuilder xsdBuilder = null;

    public XD2XsdAdapter(final String xsdPrefix) {
        if (xsdPrefix != null) {
            schemaNamespaces.put(xsdPrefix, Constants.URI_2001_SCHEMA_XSD);
        }
    }

    public XD2XsdAdapter() {
        this("xs");
    }

    public final Map<String, String> getSchemaNamespaces() {
        return schemaNamespaces;
    }

    public void addSchemaNamespace(String prefix, String namespaceUri) {
        schemaNamespaces.put(prefix, namespaceUri);
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
    public XmlSchema createSchema(final XMDefinition xdef) {

        if (xdef == null) {
            throw new IllegalArgumentException("xdef = null");
        }

        if (schemaNamespaces == null) {
            throw new IllegalArgumentException("schemaNamespaces = null");
        }

        schema = new XmlSchema("", new XmlSchemaCollection());
        xsdBuilder = new XsdBuilder(schema);

        // Namespace initialization
        NamespaceMap namespaceMap = new NamespaceMap();
        for (Map.Entry<String, String> entry : schemaNamespaces.entrySet()) {
            namespaceMap.add(entry.getKey(), entry.getValue());
        }
        schema.setNamespaceContext(namespaceMap);

        // Extract all used references
        XD2XsdReferenceAdapter referenceAdapter = new XD2XsdReferenceAdapter(schema);
        referenceAdapter.convertReferences(xdef);

        // Convert x-definition tree to xsd tree
        Set<XMNode> processed = new HashSet<XMNode>();
        convertTree(xdef, System.out, processed, "");
        return schema;
    }

    private List<XmlSchemaAttributeOrGroupRef> convertAttrs(final XMNode[] xdAttrs) {
        List<XmlSchemaAttributeOrGroupRef> xsdAttrList = new ArrayList<XmlSchemaAttributeOrGroupRef>();
        for (XMNode x : xdAttrs) {
            xsdAttrList.add(xsdBuilder.createAttribute(x.getName(), (XMData)x));
        }

        return xsdAttrList;
    }

    private XmlSchemaObject convertTree(
            XMNode xn,
            final PrintStream out,
            final Set<XMNode> processed,
            String outputPrefix) {

        if (outputPrefix != null && !processed.add(xn)) {
            System.out.println(outputPrefix + " * ref " + xn.getXDPosition());
            return null;
        }

        short xdElemKind = xn.getKind();
        switch (xdElemKind) {
            case XNode.XMATTRIBUTE: {
                XMData xd = (XMData) xn;
                if (outputPrefix != null) {
                    out.print(outputPrefix + "|-- XMAttr: ");
                    displayDesriptor((XData)xn, out);
                }
                return xsdBuilder.createAttribute(xd.getName(), xd);
            }
            case XNode.XMTEXT: {
                XData xd = (XData) xn;
                if (outputPrefix != null) {
                    out.print(outputPrefix + "|-- XMText: ");
                    displayDesriptor(xd, out);
                }
                return xsdBuilder.createSimpleContent(xd);
            }
            case XNode.XMELEMENT: {
                XElement defEl = (XElement)xn;
                out.print(outputPrefix + "|-- XMElement: ");
                displayDesriptor(defEl, out);

                XmlSchemaElement xsdElem = xsdBuilder.createElement(defEl.getName());
                XmlSchemaComplexType complexType = xsdBuilder.createComplexType();
                XmlSchemaGroupParticle group = null;
                boolean attrInsideContent = false;

                XMNode[] attrs = defEl.getXDAttrs();

                for(int i = 0; i < attrs.length; i++) {
                    convertTree(attrs[i], out, processed, outputPrefix + "|   ");
                }

                for (int i = 0; i < defEl._childNodes.length; i++) {
                    short childrenKind = defEl._childNodes[i].getKind();
                    if (childrenKind == XNode.XMSEQUENCE || childrenKind == XNode.XMMIXED || childrenKind == XNode.XMCHOICE) {
                        group = (XmlSchemaGroupParticle)convertTree(defEl._childNodes[i], out, processed, outputPrefix + "|   ");
                        complexType.setParticle(group);
                        outputPrefix += "|   ";
                    } else if (childrenKind == XNode.XMTEXT) {
                        XmlSchemaSimpleContent simpleContent = (XmlSchemaSimpleContent)convertTree(defEl._childNodes[i], out, processed, outputPrefix + "|   ");
                        for (int j = 0; j < attrs.length; j++) {
                            ((XmlSchemaSimpleContentExtension)simpleContent.getContent()).getAttributes().add((XmlSchemaAttributeOrGroupRef)convertTree(attrs[j], out, processed, null));
                        }
                        complexType.setContentModel(simpleContent);
                        attrInsideContent = true;
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

                if (attrInsideContent == false) {
                    for (int i = 0; i < attrs.length; i++) {
                        complexType.getAttributes().add((XmlSchemaAttributeOrGroupRef) convertTree(attrs[i], out, processed, null));
                    }
                }

                xsdElem.setType(complexType);
                return xsdElem;
            }
            case XNode.XMSELECTOR_END:
                out.println(outputPrefix + "|-- End of selector: ");
                return null;
            case XNode.XMSEQUENCE:
            case XNode.XMMIXED:
            case XNode.XMCHOICE:
                displaySelector(xn, out, outputPrefix);
                return xsdBuilder.createGroup(xdElemKind, xn.getOccurence());
            case XNode.XMDEFINITION: {
                XDefinition def = (XDefinition)xn;
                out.print(outputPrefix + "XMDefinition: ");
                displayDesriptor(def, out);

                if (def._rootSelection !=null && def._rootSelection.size() > 0) {
                    Iterator<String> e=def._rootSelection.keySet().iterator();
                    String name = e.next();
                    out.println("|-- Root: " + name);

                    while (e.hasNext()) {
                        out.println("    | " + e.next());
                    }
                } else {
                    out.println(outputPrefix + "|-- Root: null");
                }

                XElement[] elems = def.getXElements();
                for (int i = 0; i < elems.length; i++){
                    XmlSchemaElement xsdElem = (XmlSchemaElement)convertTree(elems[i], out, processed, outputPrefix + "|   ");
                    xsdBuilder.addElement(xsdElem);
                }
                //out.println(outputPrefix + "=== End XMDefinition: " + def.getName() + "\n");
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
