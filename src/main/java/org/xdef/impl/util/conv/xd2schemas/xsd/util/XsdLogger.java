package org.xdef.impl.util.conv.xd2schemas.xsd.util;

import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.xd2schemas.xsd.definition.AlgPhase;

import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XsdLoggerDefs.*;
import static org.xdef.model.XMNode.XMDEFINITION;
import static org.xdef.model.XMNode.XMTEXT;

public class XsdLogger {

    private static int LOG_LEVEL = LOG_WARN;

    public static void setLogLevel(int logLevel) {
        LOG_LEVEL = logLevel;
    }

    public static void print(int level, final AlgPhase phase, final String group, final String msg) {
        print(level, phase, group, null, msg);
    }

    public static void printP(int level, final AlgPhase phase, final XNode node, final String msg) {
        print(level, phase, null, node, msg);
    }

    public static void printP(int level, final AlgPhase phase, final String msg) {
        print(level, phase, null, null, msg);
    }

    public static void printG(int level, final String group, final String msg) {
        print(level, null, group, null, msg);
    }

    public static void printG(int level, final String group, final XNode node, final String msg) {
        print(level, null, group, node, msg);
    }

    private static void print(int level, final AlgPhase phase, final String group, final XNode node, final String msg) {
        if (level > LOG_LEVEL) {
            return;
        }

        String log = "[" + levelToString(level) + "]";
        if (group != null) {
            log += "[" + group + "]";
        }
        if (node != null) {
            if (node.getXMDefinition() == null) {
                log += "[" + getXNodeName(node) +  "]";
            } else if (node.getKind() == XMDEFINITION) {
                log += "[" + node.getXMDefinition().getName() + "]";
            } else {
                log += "[" + node.getXMDefinition().getName() + " - " + getXNodeName(node) +  "]";
            }
        }
        if (phase != null) {
            log += " " + phase.getVal() + ":";
        }
        log += " " + msg;
        System.out.println(log);
    }

    private static String levelToString(int level) {
        switch (level) {
            case LOG_ERROR:     return "ERROR";
            case LOG_WARN:      return "WARN";
            case LOG_INFO:      return "INFO";
            case LOG_DEBUG:     return "DEBUG";
            case LOG_TRACE:     return "TRACE";
        }

        return "";
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
