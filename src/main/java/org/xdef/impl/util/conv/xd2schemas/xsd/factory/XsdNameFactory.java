package org.xdef.impl.util.conv.xd2schemas.xsd.factory;

import org.xdef.impl.XData;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.xd2schemas.xsd.model.XsdAdapterCtx;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdNamespaceUtils;
import org.xdef.model.XMNode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XD2XsdFeature.XSD_NAME_COLISSION_DETECTOR;
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XsdLoggerDefs.*;

public class XsdNameFactory {

    final private XsdAdapterCtx adapterCtx;

    /**
     * Key:     systemId
     * Value:   Key:    xdPoisition
     *          Value:  xsd real name
     */
    private final Map<String, Map<String, String>> topLevelNameMap = new HashMap<String, Map<String, String>>();

    /**
     * Key:     systemId
     * Value:   Key:    xsd base name
     *          Value:  list of associated x-nodes
     */
    private final Map<String, Map<String, List<XMNode>>> topLevelBaseNameMap = new HashMap<String, Map<String, List<XMNode>>>();

    public XsdNameFactory(XsdAdapterCtx adapterCtx) {
        this.adapterCtx = adapterCtx;
    }

    public String findTopLevelName(final XElement xElem) {
        if (!adapterCtx.hasEnableFeature(XSD_NAME_COLISSION_DETECTOR)) {
            return null;
        }

        return findTopLevelNameByPath(xElem);
    }

    public String findTopLevelName(final XData xData, boolean usePath) {
        if (!adapterCtx.hasEnableFeature(XSD_NAME_COLISSION_DETECTOR)) {
            return null;
        }

        if (usePath) {
            return findTopLevelNameByPath(xData);
        }

        XsdLogger.printG(LOG_DEBUG, XSD_NAME_FACTORY, xData, "Finding top level simple-type name ...");

        final String systemId = XsdNamespaceUtils.getSystemIdFromXPos(xData.getXDPosition());
        final Map<String, List<XMNode>> mapBaseName = getOrCreateTopLevelBaseNameMap(systemId);

        if (mapBaseName.containsKey(xData.getRefTypeName())) {
            return xData.getRefTypeName();
        }

        return null;
    }

    private String findTopLevelNameByPath(final XNode xNode) {
        final String nodeType = (xNode instanceof XElement) ? "complex" : "simple";
        XsdLogger.printG(LOG_DEBUG, XSD_NAME_FACTORY, xNode, "Finding top level " + nodeType + "-type name ...");

        final String systemId = XsdNamespaceUtils.getSystemIdFromXPos(xNode.getXDPosition());
        final Map<String, String> mapName = getOrCreateTopLevelNameMap(systemId);

        String realName = mapName.get(xNode.getXDPosition());
        if (realName != null) {
            return realName;
        }

        return null;
    }

    public String generateTopLevelName(final XElement xElem, final String baseName) {
        return generateTopLevelName((XNode)xElem, baseName);
    }

    public String generateTopLevelName(final XData xData, final String baseName) {
        return generateTopLevelName((XNode)xData, baseName);
    }

    public String generateTopLevelName(final XNode xNode, final String baseName) {
        if (!adapterCtx.hasEnableFeature(XSD_NAME_COLISSION_DETECTOR) || baseName == null) {
            return baseName;
        }

        final String nodeType = (xNode instanceof XElement) ? "complex" : "simple";
        XsdLogger.printG(LOG_DEBUG, XSD_NAME_FACTORY, xNode, "Generating top level " + nodeType + "-type name ...");

        final String systemId = XsdNamespaceUtils.getSystemIdFromXPos(xNode.getXDPosition());
        final Map<String, List<XMNode>> mapBaseName = getOrCreateTopLevelBaseNameMap(systemId);
        final List<XMNode> nodeList = getOrCreateListNodesInBaseNameMap(mapBaseName, baseName);

        nodeList.add(xNode);
        final Map<String, String> mapName = getOrCreateTopLevelNameMap(systemId);
        String realName = baseName;

        if (nodeList.size() > 1) {
            realName += "_" + nodeList.size();
        }

        mapName.put(xNode.getXDPosition(), realName);
        XsdLogger.printG(LOG_INFO, XSD_NAME_FACTORY, xNode, "Add top-level " + nodeType + "-type name. RealName=" + realName + ", SystemId=" + systemId);
        return realName;
    }

    public void saveTopLevelName(final XData xData, final String name) {
        if (!adapterCtx.hasEnableFeature(XSD_NAME_COLISSION_DETECTOR) || name == null) {
            return;
        }

        XsdLogger.printG(LOG_DEBUG, XSD_NAME_FACTORY, xData, "Saving top level simple-type name ...");

        final String systemId = XsdNamespaceUtils.getSystemIdFromXPos(xData.getXDPosition());
        final Map<String, List<XMNode>> mapBaseName = getOrCreateTopLevelBaseNameMap(systemId);
        final List<XMNode> nodeList = getOrCreateListNodesInBaseNameMap(mapBaseName, name);

        nodeList.add(xData);
        XsdLogger.printG(LOG_INFO, XSD_NAME_FACTORY, xData, "Add top-level simple-type name. Name=" + name + ", SystemId=" + systemId);
    }

    private Map<String, String> getOrCreateTopLevelNameMap(final String systemId) {
        Map<String, String> mapName = topLevelNameMap.get(systemId);
        if (mapName == null) {
            mapName = new HashMap<String, String>();
            topLevelNameMap.put(systemId, mapName);
        }

        return mapName;
    }

    private Map<String, List<XMNode>> getOrCreateTopLevelBaseNameMap(final String systemId) {
        Map<String, List<XMNode>> mapBaseName = topLevelBaseNameMap.get(systemId);
        if (mapBaseName == null) {
            mapBaseName = new HashMap<String, List<XMNode>>();
            topLevelBaseNameMap.put(systemId, mapBaseName);
        }

        return mapBaseName;
    }

    private List<XMNode> getOrCreateListNodesInBaseNameMap(final Map<String, List<XMNode>> mapBaseName, final String baseName) {
        List<XMNode> nodeList = mapBaseName.get(baseName);
        if (nodeList == null) {
            nodeList = new LinkedList<XMNode>();
            mapBaseName.put(baseName, nodeList);
        }

        return nodeList;
    }

    public static String newTopLocalRefName(final String name) {
        return name;
    }
}
