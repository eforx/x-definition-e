package org.xdef.impl.util.conv.xd2schemas.xsd.util;

import org.xdef.impl.XNode;

import static org.xdef.model.XMNode.XMTEXT;

public class XsdLogger {

    public static void print(final XNode node, final String msg) {
        String nodeName = node.getName();
        if (node.getKind() == XMTEXT) {
            int pos = -1;
            try {
                pos = node.getXDPosition().substring(0, node.getXDPosition().length() - nodeName.length() - 1).lastIndexOf('/');
            } catch (Exception ex) {

            } finally {
                if (pos != -1) {
                    nodeName = node.getXDPosition().substring(pos + 1, node.getXDPosition().lastIndexOf('/'));
                }
            }
        }
        System.out.println("[" + node.getXMDefinition().getName() + " - " + nodeName + "] " + msg);
    }

}
