package org.xdef.impl.util.conv.xd2schemas;

import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.xdef.XDPool;
import org.xdef.impl.*;
import org.xdef.model.XMData;
import org.xdef.model.XMDefinition;
import org.xdef.model.XMElement;
import org.xdef.model.XMNode;

import java.io.PrintStream;
import java.util.*;

public class XD2XsdAdapter implements XD2SchemaAdapter<XmlSchema> {

    private Map<String, String> schemaNamespaces = new HashMap<String, String>();
    private XmlSchema schema = null;

    public XD2XsdAdapter(final String xsdPrefix) {
        if (xsdPrefix != null) {
            schemaNamespaces.put(xsdPrefix, "http://www.w3.org/2001/XMLSchema");
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

        // Namespace initialization
        NamespaceMap namespaceMap = new NamespaceMap();
        for (Map.Entry<String, String> entry : schemaNamespaces.entrySet()) {
            namespaceMap.add(entry.getKey(), entry.getValue());
        }
        //schemaNamespaces.forEach((k, v) -> namespaceMap.add(k, v));
        schema.setNamespaceContext(namespaceMap);

        convert(xdef);
        return schema;
    }

    private void convert(XMDefinition xdef) {
        Set<XMNode> processed = new HashSet<XMNode>();
        XmlSchemaElement rootElem = null;
        convertRec(xdef, System.out, processed, "", rootElem);
    }

    private void convertRec(
            XMNode xn,
            final PrintStream out,
            final Set<XMNode> processed,
            String outputPrefix,
            XmlSchemaItemWithRef xsdElem
            ) {
        if (!processed.add(xn)) {
            System.out.println(outputPrefix + " * ref " + xn.getXDPosition());
            return;
        }

        switch (xn.getKind()) {
            case XNode.XMATTRIBUTE:
            case XNode.XMTEXT: {
                XData xd = (XData) xn;
                out.print(outputPrefix + "|-- XMAttr: ");
                displayDesriptor(xd, out);
                return;
            }
            case XNode.XMELEMENT: {
                XElement defEl = (XElement)xn;
                out.print(outputPrefix + "|-- XMElement: ");
                displayDesriptor(defEl, out);
                XNode[] attrs = defEl.getXDAttrs();

                for(int i = 0; i < attrs.length; i++) {
                    convertRec(attrs[i], out, processed, outputPrefix + "|   ", xsdElem);
                }

                XmlSchemaComplexType complexType = XsdBuilder.createComplexType(schema);
                ((XmlSchemaElement)xsdElem).setType(complexType);

                for (int i = 0; i < defEl._childNodes.length; i++) {
                    convertRec(defEl._childNodes[i], out, processed, outputPrefix + "|   ", xsdElem);
                    if (defEl._childNodes[i].getKind() == XNode.XMSEQUENCE ||
                            defEl._childNodes[i].getKind() == XNode.XMMIXED ||
                            defEl._childNodes[i].getKind() == XNode.XMCHOICE) {
                        outputPrefix += "|   ";
                    }
                }
                //out.println(outputPrefix + "|-- End XMElement: " + xn.getName());
                return;
            }
            case XNode.XMSELECTOR_END:
                out.println(outputPrefix + "|-- End of selector: ");
                return;
            case XNode.XMSEQUENCE:
            case XNode.XMMIXED:
            case XNode.XMCHOICE:
                displaySelector(xn, out, outputPrefix);
                return;
            case XNode.XMDEFINITION: {
                XDefinition def = (XDefinition)xn;
                out.print(outputPrefix + "XMDefinition: ");
                displayDesriptor(def, out);

               xsdElem = null;
                if (def._rootSelection !=null && def._rootSelection.size() > 0) {
                    Iterator<String> e=def._rootSelection.keySet().iterator();
                    String name = e.next();
                    out.println("|-- Root: " + name);
                    xsdElem = XsdBuilder.createElement(schema, name);
                    XsdBuilder.addElement(schema, (XmlSchemaElement)xsdElem);

                    while (e.hasNext()) {
                        out.println("    | " + e.next());
                    }
                } else {
                    out.println(outputPrefix + "|-- Root: null");
                }

                // TODO: What should we do if x-definition has no root element
                if (xsdElem == null) {
                    throw new RuntimeException("x-definition has no root element");
                }

                XElement[] elems = def.getXElements();
                for (int i = 0; i < elems.length; i++){
                    convertRec(elems[i], out, processed, outputPrefix + "|   ", xsdElem);
                }
                //out.println(outputPrefix + "=== End XMDefinition: " + def.getName() + "\n");
                return;
            }
            default: {
                out.println(outputPrefix + "UNKNOWN: " + xn.getName() + "; " + xn.getKind());
            }
        }
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
