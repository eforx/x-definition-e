package org.xdef.impl.util.conv.schema.xd2schema.xsd.factory;

import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.xdef.impl.XData;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.model.XsdAdapterCtx;
import org.xdef.impl.util.conv.schema.util.SchemaLogger;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.util.XsdNamespaceUtils;
import org.xdef.model.XMNode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.Xd2XsdFeature.XSD_NAME_COLISSION_DETECTOR;
import static org.xdef.impl.util.conv.schema.util.SchemaLoggerDefs.*;
import static org.xdef.impl.util.conv.schema.xd2schema.xsd.util.Xd2XsdLoggerDefs.XSD_NAME_FACTORY;

/**
 * Creates names of specific types of nodes.
 * Stores top level nodes names.
 */
public class XsdNameFactory {

    final private XsdAdapterCtx adapterCtx;

    /**
     * Real used names of XSD nodes. Used for finding real names of created XSD nodes.
     *
     * Key:     systemId
     * Value:   Key:    xdPoisition
     *          Value:  xsd real name
     */
    private final Map<String, Map<String, String>> topLevelNameMap = new HashMap<String, Map<String, String>>();

    /**
     * Base name of XSD nodes. Used for pairing and storing x-definition nodes with same name
     * which are projected on top level XSD node.
     *
     * Key:     systemId
     * Value:   Key:    xsd base name
     *          Value:  list of associated x-nodes
     */
    private final Map<String, Map<String, List<XMNode>>> topLevelBaseNameMap = new HashMap<String, Map<String, List<XMNode>>>();

    public XsdNameFactory(XsdAdapterCtx adapterCtx) {
        this.adapterCtx = adapterCtx;
    }

    /**
     * Finds XSD top level element node name.
     * @param xElem     x-definition element
     * @return  non-null name if x-definition element node with given path has been stored
     *          null otherwise
     */
    public String findTopLevelName(final XElement xElem) {
        if (!adapterCtx.hasEnableFeature(XSD_NAME_COLISSION_DETECTOR)) {
            return null;
        }

        return findTopLevelNameByPath(xElem);
    }

    /**
     * Finds XSD top level simple-type node name
     * @param xData     x-definition node (attribute/text)
     * @param usePath   flag if name should be search by x-definition node path
     * @return  non-null name if x-definition node has been stored
     *          null otherwise
     */
    public String findTopLevelName(final XData xData, boolean usePath) {
        if (!adapterCtx.hasEnableFeature(XSD_NAME_COLISSION_DETECTOR)) {
            return null;
        }

        if (usePath) {
            return findTopLevelNameByPath(xData);
        }

        SchemaLogger.printG(LOG_DEBUG, XSD_NAME_FACTORY, xData, "Finding top level simple-type name ...");

        final String systemId = XsdNamespaceUtils.getSystemIdFromXPos(xData.getXDPosition());
        final Map<String, List<XMNode>> mapBaseName = getOrCreateTopLevelBaseNameMap(systemId);

        if (mapBaseName.containsKey(xData.getRefTypeName())) {
            return xData.getRefTypeName();
        }

        return null;
    }

    /**
     * Finds XSD top level node name based on x-definition path
     * @param xNode x-definition node
     * @return  non-null name if x-definition node with given path has been stored
     *          null otherwise
     */
    private String findTopLevelNameByPath(final XNode xNode) {
        final String nodeType = (xNode instanceof XElement) ? "complex" : "simple";
        SchemaLogger.printG(LOG_DEBUG, XSD_NAME_FACTORY, xNode, "Finding top level " + nodeType + "-type name ...");

        final String systemId = XsdNamespaceUtils.getSystemIdFromXPos(xNode.getXDPosition());
        final Map<String, String> mapName = getOrCreateTopLevelNameMap(systemId);

        String realName = mapName.get(xNode.getXDPosition());
        if (realName != null) {
            return realName;
        }

        return null;
    }

    /**
     * Creates new top level XSD element node name based on {@paramref baseName}
     * and current internal state of name storage.
     *
     * @param xElem         x-definition element node
     * @param baseName      x-definition element node base name
     * @return new name
     */
    public String generateTopLevelName(final XElement xElem, final String baseName) {
        return generateTopLevelName((XNode)xElem, baseName);
    }

    /**
     * Creates new top level XSD simple type node name based on {@paramref baseName}
     * and current internal state of name storage.
     *
     * @param xData         x-definition node
     * @param baseName      x-definition node base name
     * @return new name
     */
    public String generateTopLevelName(final XData xData, final String baseName) {
        return generateTopLevelName((XNode)xData, baseName);
    }

    /**
     * Creates new top level XSD node name based on {@paramref baseName}
     * and current internal state of name storage.
     *
     * @param xNode         x-definition node
     * @param baseName      x-definition node base name
     * @return new name
     */
    private String generateTopLevelName(final XNode xNode, final String baseName) {
        if (!adapterCtx.hasEnableFeature(XSD_NAME_COLISSION_DETECTOR) || baseName == null) {
            return baseName;
        }

        final String nodeType = (xNode instanceof XElement) ? "complex" : "simple";
        SchemaLogger.printG(LOG_DEBUG, XSD_NAME_FACTORY, xNode, "Generating top level " + nodeType + "-type name ...");

        final List<XMNode> nodeList = addNodeWithBaseName(xNode, baseName);
        String realName = baseName;

        if (nodeList.size() > 1) {
            realName += "_" + nodeList.size();
        }

        final String systemId = XsdNamespaceUtils.getSystemIdFromXPos(xNode.getXDPosition());
        final Map<String, String> mapName = getOrCreateTopLevelNameMap(systemId);
        mapName.put(xNode.getXDPosition(), realName);
        SchemaLogger.printG(LOG_INFO, XSD_NAME_FACTORY, xNode, "Add top-level " + nodeType + "-type name. RealName=" + realName + ", SystemId=" + systemId);
        return realName;
    }

    /**
     * Saves XSD simple type node name to internal storage
     * @param xData     x-definition node
     * @param baseName  x-definition node base name
     */
    public void addTopSimpleTypeName(final XData xData, final String baseName) {
        if (!adapterCtx.hasEnableFeature(XSD_NAME_COLISSION_DETECTOR) || baseName == null) {
            return;
        }

        SchemaLogger.printG(LOG_DEBUG, XSD_NAME_FACTORY, xData, "Saving top level simple-type name ...");
        addNodeWithBaseName(xData, baseName);
    }

    /**
     * Get name storage map. If map does not exist, then creates and saves empty one.
     * @param systemId  XSD system identified
     * @return name storage map
     */
    private Map<String, String> getOrCreateTopLevelNameMap(final String systemId) {
        Map<String, String> mapName = topLevelNameMap.get(systemId);
        if (mapName == null) {
            mapName = new HashMap<String, String>();
            topLevelNameMap.put(systemId, mapName);
        }

        return mapName;
    }

    /**
     * Get base name storage map. If map does not exist, then creates and saves empty one.
     * @param systemId  XSD system identified
     * @return base name storage map
     */
    private Map<String, List<XMNode>> getOrCreateTopLevelBaseNameMap(final String systemId) {
        Map<String, List<XMNode>> mapBaseName = topLevelBaseNameMap.get(systemId);
        if (mapBaseName == null) {
            mapBaseName = new HashMap<String, List<XMNode>>();
            topLevelBaseNameMap.put(systemId, mapBaseName);
        }

        return mapBaseName;
    }

    /**
     * Get list of x-definition nodes using same base name. If list does not exist, then creates and saves empty one.
     * @param mapBaseName   map of nodes using base name
     * @param baseName      required base name
     * @return list of x-definition nodes
     */
    private List<XMNode> getOrCreateListNodesInBaseNameMap(final Map<String, List<XMNode>> mapBaseName, final String baseName) {
        List<XMNode> nodeList = mapBaseName.get(baseName);
        if (nodeList == null) {
            nodeList = new LinkedList<XMNode>();
            mapBaseName.put(baseName, nodeList);
        }

        return nodeList;
    }

    /**
     * Saves x-definition node base name.
     * @param xNode         x-definition node
     * @param nodeBaseName  x-definition node base name
     * @return list of x-definition nodes using same {@paramref nodeBaseName}
     */
    private List<XMNode> addNodeWithBaseName(final XNode xNode, final String nodeBaseName) {
        final String nodeType = (xNode instanceof XElement) ? "complex" : "simple";
        final String systemId = XsdNamespaceUtils.getSystemIdFromXPos(xNode.getXDPosition());
        final Map<String, List<XMNode>> mapBaseName = getOrCreateTopLevelBaseNameMap(systemId);
        final List<XMNode> nodeList = getOrCreateListNodesInBaseNameMap(mapBaseName, nodeBaseName);

        nodeList.add(xNode);
        SchemaLogger.printG(LOG_INFO, XSD_NAME_FACTORY, xNode, "Add top-level " + nodeType + "-type base name. Name=" + nodeBaseName + ", SystemId=" + systemId);
        return nodeList;
    }

    /**
     * Creates new name of top level complex type
     * @param name  original complex type name
     * @return new name
     */
    public static String createComplexRefName(final String name) {
        return name;
    }

    /**
     * Creates new name of top level root element's schema type node
     * @param name          original element name
     * @param schemaType    schema type node
     * @return new name
     */
    public static String createRootElemName(final String name, final XmlSchemaType schemaType) {
        return createElementPrefix(schemaType) + "root_" + name;
    }

    /**
     * Creates new name of local simple type
     * @param xData         x-definition node to be converted to XSD simple type node
     * @return new name
     */
    public static String createLocalSimpleTypeName(final XData xData) {
        return xData.isLocalType() ? "refLoc_" + xData.getRefTypeName() : xData.getRefTypeName();
    }

    /**
     * Creates new name of union member reference
     * @param nodeName          node name where union is located
     * @param qNameLocal        local part of QName
     * @return new name
     */
    public static String createUnionRefTypeName(final String nodeName, final String qNameLocal) {
        return nodeName + "_union_" + qNameLocal;
    }

    /**
     * Creates element node prefix based on schema type
     * @param schemaType    input schema type
     * @return  "ct_" if input schema type is complex
     *          "st_" if input schema type is simple
     *          "" otherwise
     */
    private static String createElementPrefix(XmlSchemaType schemaType) {
        if (schemaType != null) {
            if (schemaType instanceof XmlSchemaComplexType) {
                return "ct_";
            } else {
                return "st_";
            }
        }

        return "";
    }

}
