package org.xdef.impl.util.conv.xd2schemas.xsd.util;

import org.xdef.impl.XNode;
import org.xdef.model.XMDefinition;

import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.*;
import static org.xdef.model.XMNode.XMDEFINITION;
import static org.xdef.model.XMNode.XMTEXT;

public class XsdLogger {

    public static boolean isError(int logLevel) {
        return logLevel >= LOG_LEVEL_ERROR;
    }

    public static boolean isWarn(int logLevel) {
        return logLevel >= LOG_LEVEL_WARN;
    }

    public static boolean isInfo(int logLevel) {
        return logLevel >= LOG_LEVEL_INFO;
    }

    public static boolean isDebug(int logLevel) {
        return logLevel >= LOG_LEVEL_DEBUG;
    }

    public static boolean isTrace(int logLevel) {
        return logLevel >= LOG_LEVEL_TRACE;
    }

    public static void print(final String level, final String phase, final String category, final String msg) {
        System.out.println("[" + level + "][" + category + "] " + phase + ": " + msg);
    }

    public static void printP(final String level, final String phase, final XNode node, final String msg) {
        if (node.getKind() == XMDEFINITION) {
            System.out.println("[" + level + "][" + node.getXMDefinition().getName() + "] " + phase + ": " + msg);
        } else {
            System.out.println("[" + level + "][" + node.getXMDefinition().getName() + ":" + getXNodeName(node) + "] " + phase + ": " + msg);
        }
    }

    public static void printP(final String level, final String phase, final String msg) {
        System.out.println("[" + level + "] " + phase + ": " + msg);
    }

    public static void printC(final String level, final String category, final String msg) {
        System.out.println("[" + level + "][" + category + "] " + msg);
    }

    public static void printC(final String level, final String category, final XNode node, final String msg) {
        if (node.getKind() == XMDEFINITION) {
            System.out.println("[" + level + "][" + category + "][" + node.getXMDefinition().getName() + "] " + msg);
        } else {
            System.out.println("[" + level + "][" + category + "][" + node.getXMDefinition().getName() + ":" + getXNodeName(node) + "] " + msg);
        }
    }


    private static String getXNodeName(final XNode node) {
        String nodeName = node.getName();
        if (node.getKind() == XMTEXT) {
            int pos = -1;
            try {
                pos = node.getXDPosition().substring(0, node.getXDPosition().length() - nodeName.length() - 1).lastIndexOf('/');
            } catch (Exception ex) {

            } finally {
                if (pos != -1) {
                    nodeName = node.getXDPosition().substring(pos + 1, node.getXDPosition().lastIndexOf('/'));
                } else {
                    pos = node.getXDPosition().lastIndexOf('/');
                    if (pos != -1) {
                        nodeName = node.getXDPosition().substring(0, pos);
                    }
                }

                pos = nodeName.indexOf('#');
                if (pos != -1) {
                    nodeName = nodeName.substring(pos + 1);
                }
            }
        }

        return nodeName;
    }

}
