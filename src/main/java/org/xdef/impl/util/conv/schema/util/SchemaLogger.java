package org.xdef.impl.util.conv.schema.util;

import org.apache.ws.commons.schema.utils.XmlSchemaNamed;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;
import org.w3c.dom.Node;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.schema.xd2schema.definition.AlgPhase;

import static org.xdef.impl.util.conv.schema.util.SchemaLoggerDefs.*;
import static org.xdef.model.XMNode.XMDEFINITION;
import static org.xdef.model.XMNode.XMTEXT;

/**
 * Simple XSD logging helper
 */
public class SchemaLogger {

    private static int LOG_LEVEL = LOG_WARN;

    public static void setLogLevel(int logLevel) {
        LOG_LEVEL = logLevel;
    }

    public static void print(int level, final AlgPhase phase, final String group, final String msg) {
        print(level, phase, group, "", msg);
    }

    public static void printP(int level, final AlgPhase phase, final XNode node, final String msg) {
        print(level, phase, null, node, msg);
    }

    public static void printP(int level, final AlgPhase phase, final XmlSchemaObjectBase node, final String msg) {
        print(level, phase, null, node, msg);
    }

    public static void printP(int level, final AlgPhase phase, final Node node, final String msg) {
        print(level, phase, null, node, msg);
    }

    public static void printP(int level, final AlgPhase phase, final String msg) {
        print(level, phase, null, "", msg);
    }

    public static void printG(int level, final String group, final String msg) {
        print(level, null, group, "", msg);
    }

    public static void printG(int level, final String group, final XNode node, final String msg) {
        print(level, null, group, node, msg);
    }

    public static void printG(int level, final String group, final XmlSchemaObjectBase node, final String msg) {
        print(level, null, group, node, msg);
    }

    public static void printG(int level, final String group, final Node node, final String msg) {
        print(level, null, group, node, msg);
    }

    public static void print(int level, final AlgPhase phase, final String group, final XNode node, final String msg) {
        if (level > LOG_LEVEL) {
            return;
        }

        final StringBuilder sb = new StringBuilder();
        if (node != null) {
            if (node.getXMDefinition() == null) {
                sb.append("[" + getXNodeName(node) +  "]");
            } else if (node.getKind() == XMDEFINITION) {
                sb.append("[" + node.getXMDefinition().getName() + "]");
            } else {
                sb.append("[" + node.getXMDefinition().getName() + " - " + getXNodeName(node) +  "]");
            }
        }

        print(level, phase, group, sb.toString(), msg);
    }

    public static void print(int level, final AlgPhase phase, final String group, final XmlSchemaObjectBase node, final String msg) {
        if (level > LOG_LEVEL) {
            return;
        }

        final StringBuilder sb = new StringBuilder();
        if (node != null) {
            if (node instanceof XmlSchemaNamed) {
                sb.append("[" + ((XmlSchemaNamed)node).getName() +  "]");
            }
        }

        print(level, phase, group, sb.toString(), msg);
    }

    public static void print(int level, final AlgPhase phase, final String group, final Node node, final String msg) {
        if (level > LOG_LEVEL) {
            return;
        }

        final StringBuilder sb = new StringBuilder();
        if (node != null) {
            if (node.getNodeName() != null) {
                sb.append("[" + node.getNodeName() +  "]");
            }
        }

        print(level, phase, group, sb.toString(), msg);
    }

    public static void print(int level, final AlgPhase phase, final String group, final String nodeInfo, final String msg) {
        if (level > LOG_LEVEL) {
            return;
        }

        final StringBuilder sb = new StringBuilder("[" + levelToString(level) + "]");
        if (group != null) {
            sb.append("[" + group + "]");
        }
        if (nodeInfo != null) {
            sb.append(nodeInfo);
        }
        if (phase != null) {
            sb.append(" " + phase.getVal() + ":");
        }
        sb.append(" " + msg);
        System.out.println(sb.toString());
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
