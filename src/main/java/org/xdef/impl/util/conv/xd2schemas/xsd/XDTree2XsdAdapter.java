package org.xdef.impl.util.conv.xd2schemas.xsd;

import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;
import org.xdef.XDParser;
import org.xdef.impl.*;
import org.xdef.impl.util.conv.xd2schemas.xsd.builder.XsdBaseBuilder;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XD2XsdUtils;
import org.xdef.model.XMData;
import org.xdef.model.XMNode;

import javax.xml.namespace.QName;
import java.io.PrintStream;
import java.util.*;

import static org.xdef.impl.util.conv.xd2schemas.xsd.XD2XsdDefinitions.XSD_NAMESPACE_PREFIX_EMPTY;

class XDTree2XsdAdapter {

    final private boolean printXdTree;
    final private XmlSchema schema;
    final private XsdBaseBuilder xsdBuilder;

    private Set<XMNode> xdProcessedNodes = null;
    private List<String> xdRootNames = null;

    protected XDTree2XsdAdapter(boolean printXdTree, XmlSchema schema, XsdBaseBuilder xsdBuilder) {
        this.printXdTree = printXdTree;
        this.schema = schema;
        this.xsdBuilder = xsdBuilder;
    }

    protected List<String> getXdRootNames() {
        return xdRootNames;
    }

    protected void loadXdefRootNames(final XDefinition def) {
        xdRootNames = new ArrayList<String>();
        if (def._rootSelection != null && def._rootSelection.size() > 0) {
            Iterator<String> e = def._rootSelection.keySet().iterator();
            while (e.hasNext()) {
                xdRootNames.add(e.next());
            }
        }
    }

    protected XmlSchemaObject convertTree(XMNode xn, final PrintStream out, String outputPrefix) {
        xdProcessedNodes = new HashSet<XMNode>();
        return convertTreeInt(xn, out, outputPrefix);
    }

    private XmlSchemaObject convertTreeInt(
            XMNode xn,
            final PrintStream out,
            String outputPrefix) {

        if (!xdProcessedNodes.add(xn)) {
            //System.out.println(outputPrefix + "Already processed node: " + xn.getName() + " (" + xn.getXDPosition() + ")");
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
                if (printXdTree) {
                    XElement defEl = (XElement)xn;
                    out.print(outputPrefix + "|-- XMElement: ");
                    displayDesriptor(defEl, out);
                }

                return createElement(xn, out, outputPrefix);
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
                out.println(outputPrefix + "XDefinition node should not be converted: " + xn.getName());
                return null;
            }
            default: {
                out.println(outputPrefix + "UNKNOWN: " + xn.getName() + "; " + xn.getKind());
            }
        }

        return null;
    }

    private XmlSchemaObject createElement(XMNode xn,
                                          final PrintStream out,
                                          String outputPrefix) {
        XElement defEl = (XElement)xn;
        XmlSchemaElement xsdElem = xsdBuilder.createEmptyElement(defEl.getName(), defEl);

        if (defEl.isReference()) {
            if (XD2XsdUtils.isExternalRef(defEl.getName(), defEl.getNSUri(), schema)) {
                xsdElem.getRef().setTargetQName(new QName(defEl.getNSUri(), defEl.getName()));
            } else {
                xsdElem.setName(defEl.getName());
                // TODO: reference namespace?
                xsdElem.setSchemaTypeName(new QName(XSD_NAMESPACE_PREFIX_EMPTY, XD2XsdUtils.getReferenceName(defEl.getReferencePos())));
                XD2XsdUtils.resolveElementName(schema, xsdElem);
            }
        } else {
            xsdElem.setName(defEl.getName());

            // If element contains only data, we dont have to create complexType
            if (defEl.getXDAttrs().length == 0 && defEl._childNodes.length == 1 && defEl._childNodes[0].getKind() == XNode.XMTEXT) {
                addSimpleContentToElem(xsdElem, (XData)defEl._childNodes[0]);
            } else {
                addComplexContentToElem(xsdElem, defEl, out, outputPrefix);
            }

            XD2XsdUtils.resolveElementName(schema, xsdElem);
        }

        return xsdElem;
    }

    private void addSimpleContentToElem(final XmlSchemaElement xsdElem, final XData xd) {
        // TODO: Should we lookup for simple type reference, which is equal to current simple type?
        if (XD2XsdUtils.hasSimpleParser(xd)) {
            // TODO: Has to be instance of XDParser?
            if (xd.getParseMethod() instanceof XDParser) {
                final String parserName = xd.getParserName();
                xsdElem.setSchemaTypeName(XD2XsdUtils.parserNameToQName(parserName));
            }
        } else {
            xsdElem.setType(xsdBuilder.creatSimpleType(xd, false));
        }
    }

    private void addComplexContentToElem(final XmlSchemaElement xsdElem, final XElement defEl, final PrintStream out, String outputPrefix) {
        XmlSchemaComplexType complexType = xsdBuilder.createEmptyComplexType();
        XmlSchemaGroupParticle group = null;
        boolean hasSimpleContent = false;
        XMNode[] attrs = defEl.getXDAttrs();

        // Convert all children nodes
        for (int i = 0; i < defEl._childNodes.length; i++) {
            XNode xnChild = defEl._childNodes[i];
            short childrenKind = xnChild.getKind();
            // Particle nodes (sequence, choice, all)
            if (childrenKind == XNode.XMSEQUENCE || childrenKind == XNode.XMMIXED || childrenKind == XNode.XMCHOICE) {
                if (complexType.getParticle() != null) {
                    // TODO: XD->XSD Solve
                    out.println("Multiple particle group inside element!");
                }
                group = (XmlSchemaGroupParticle) convertTreeInt(xnChild, out, outputPrefix + "|   ");
                complexType.setParticle(group);
                outputPrefix += "|   ";
            } else if (childrenKind == XNode.XMTEXT) { // Simple value node
                XmlSchemaSimpleContent simpleContent = (XmlSchemaSimpleContent) convertTreeInt(xnChild, out, outputPrefix + "|   ");
                for (int j = 0; j < attrs.length; j++) {
                    ((XmlSchemaSimpleContentExtension) simpleContent.getContent()).getAttributes().add((XmlSchemaAttributeOrGroupRef) convertTreeInt(attrs[j], out, outputPrefix + "|   "));
                }

                complexType.setContentModel(simpleContent);
                hasSimpleContent = true;
            } else {
                XmlSchemaObjectBase xsdChild = convertTreeInt(xnChild, out, outputPrefix + "|   ");
                if (xsdChild != null) {
                    // x-definition has no required group
                    if (group == null) {
                        group = new XmlSchemaSequence();
                        complexType.setParticle(group);
                    }

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

        updateParticleGroup(defEl, complexType);

        if (hasSimpleContent == false) {
            for (int i = 0; i < attrs.length; i++) {
                complexType.getAttributes().add((XmlSchemaAttributeOrGroupRef) convertTreeInt(attrs[i], out, outputPrefix + "|   "));
            }
        }

        xsdElem.setType(complexType);
    }

    private void updateParticleGroup(final XElement defEl, final XmlSchemaComplexType complexType) {

        // if xs:all contains only unbounded elements, then we can use unbounded xs:choise
        {
            boolean allElementsUnbounded = true;
            boolean anyElementUnbounded = false;

            if (complexType.getParticle() instanceof XmlSchemaAll) {

                for (XNode xNode : defEl._childNodes) {
                    if (xNode.getKind() == XNode.XMELEMENT) {
                        if (!xNode.isMaxUnlimited() && !xNode.isUnbounded()) {
                            allElementsUnbounded = false;
                        } else if (xNode.maxOccurs() > 1) {
                            anyElementUnbounded = true;
                        }
                    }
                }

                if (allElementsUnbounded) {
                    XmlSchemaChoice group = new XmlSchemaChoice();
                    group.setMaxOccurs(Long.MAX_VALUE);

                    // Copy elements
                    for (XmlSchemaAllMember member : ((XmlSchemaAll)complexType.getParticle()).getItems()) {
                        group.getItems().add((XmlSchemaChoiceMember) member);
                    }

                    complexType.setParticle(group);
                } else if (anyElementUnbounded) {
                    // TODO: XD->XSD Solve
                    System.out.println("xs:all contains some element which has maxOccurs higher than 1");
                }
            }
        }
    }

    protected static void displaySelector(final XMNode xn, final PrintStream out, final String outputPrefix) {
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

    protected static String printOption(final String s,
                                      final String name,
                                      final byte value) {
        if (value == 0) {
            return s;
        }
        return (s.length() > 0 ? s + "\n": "") + name + "=" + (char) value;
    }

    protected static void displayDesriptor(final XCodeDescriptor sc,
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
