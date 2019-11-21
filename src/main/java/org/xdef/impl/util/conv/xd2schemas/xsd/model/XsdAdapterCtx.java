package org.xdef.impl.util.conv.xd2schemas.xsd.model;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.utils.XmlSchemaNamed;
import org.xdef.impl.XData;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.xd2schemas.xsd.definition.AlgPhase;
import org.xdef.impl.util.conv.xd2schemas.xsd.definition.XD2XsdFeature;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XD2XsdParserMapping;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdNameUtils;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdNamespaceUtils;

import javax.xml.namespace.QName;
import java.util.*;

import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.AlgPhase.*;
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.XsdLoggerDefs.*;

/**
 * Basic XSD context for transformation x-definition to XSD schema
 */
public class XsdAdapterCtx {

    /**
     * Names of created xsd schemas
     */
    private Set<String> schemaNames = null;

    /**
     * Schemas location based on x-definition
     * Key:     namespace URI
     * Value:   location
     */
    private Map<String, XsdSchemaImportLocation> schemaLocationsCtx = null;

    /**
     * Schemas locations which are created in post-processing
     * Key:     schema namespace URI
     * Value:   schema location
     */
    private Map<String, XsdSchemaImportLocation> extraSchemaLocationsCtx = null;

    /**
     * Collection of created XSD schemas
     */
    private XmlSchemaCollection xmlSchemaCollection = null;

    /**
     * Element/attributes nodes per schema
     * Key:     schema name
     * Value:   node path, schema node
     */
    private Map<String, Map<String, SchemaNode>> nodes = null;

    /**
     * Nodes which will be created in post-procession
     * Key:     namespace URI
     * Value:   node name, x-definition node
     */
    private Map<String, Map<String, XNode>> nodesToBePostProcessed;

    /**
     * Names of nodes which can be root of x-definitions
     * Key:     x-definition name
     * Value:   set of node names
     */
    private Map<String, Set<String>> rootNodeNames = null;

    /**
     * Nodes which will be created in post-procession
     * Key:     schema name
     * Value:   xpath to uniqueSet, unique info
     */
    private Map<String, Map<String, List<UniqueConstraints>>> uniqueRestrictions;
    
    /**
     * Enabled algorithm features
     */
    final private Set<XD2XsdFeature> features;

    public XsdAdapterCtx(Set<XD2XsdFeature> features) {
        this.features = features;
    }

    public void init() {
        schemaNames = new HashSet<String>();
        schemaLocationsCtx = new HashMap<String, XsdSchemaImportLocation>();
        extraSchemaLocationsCtx = new HashMap<String, XsdSchemaImportLocation>();
        xmlSchemaCollection = new XmlSchemaCollection();
        nodes = new HashMap<String, Map<String, SchemaNode>>();
        nodesToBePostProcessed = new HashMap<String, Map<String, XNode>>();
        rootNodeNames = new HashMap<String, Set<String>>();
        uniqueRestrictions = new HashMap<String, Map<String, List<UniqueConstraints>>>();
    }

    public final Set<String> getSchemaNames() {
        return schemaNames;
    }

    public final Map<String, XsdSchemaImportLocation> getSchemaLocationsCtx() {
        return schemaLocationsCtx;
    }

    public final Map<String, XsdSchemaImportLocation> getExtraSchemaLocationsCtx() {
        return extraSchemaLocationsCtx;
    }

    public final XmlSchemaCollection getXmlSchemaCollection() {
        return xmlSchemaCollection;
    }

    public final Map<String, Map<String, SchemaNode>> getNodes() {
        return nodes;
    }

    public Map<String, Map<String, XNode>> getNodesToBePostProcessed() {
        return nodesToBePostProcessed;
    }

    /**
     * Add schema name to name set
     * @param name  x-definition name
     */
    public void addSchemaName(final String name) throws RuntimeException {
        if (!schemaNames.add(name)) {
            XsdLogger.printG(LOG_ERROR, XSD_ADAPTER_CTX, "Schema with this name has been already processed! Name=" + name);
            throw new RuntimeException("X-definition name duplication");
        }
    }

    /**
     *
     * @param nsUri
     * @param importLocation
     */
    public void addSchemaLocation(final String nsUri, final XsdSchemaImportLocation importLocation) {
        if (schemaLocationsCtx.containsKey(nsUri)) {
            XsdLogger.printG(LOG_WARN, XSD_ADAPTER_CTX, "Schema location already exists for namespace URI. NamespaceURI=" + nsUri);
            return;
        }

        XsdLogger.printP(LOG_INFO, PREPROCESSING, "Add schema location. NamespaceURI=" + nsUri + ", Path=" + importLocation.buildLocalition(null));
        schemaLocationsCtx.put(nsUri, importLocation);
    }

    /**
     * Returns true if schema with given namespace URI exists
     * @param nsUri
     * @return
     */
    public boolean existsSchemaLocation(final String nsUri) {
        return schemaLocationsCtx.containsKey(nsUri);
    }

    public XsdSchemaImportLocation getPostProcessingNsImport(final String nsUri) {
        return extraSchemaLocationsCtx.get(nsUri);
    }

    /**
     * Returns true if schema with given namespace URI should be created in post-processing
     * @param nsUri
     * @return
     */
    public boolean isPostProcessingNamespace(final String nsUri) {
        return extraSchemaLocationsCtx.containsKey(nsUri);
    }

    public void addNodeToPostProcessing(final String nsUri, final XNode xNode) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xNode, "Add node to post-processing.");

        final String nodeName = xNode.getName();
        Map<String, XNode> ppNsNodes = nodesToBePostProcessed.get(nsUri);

        if (ppNsNodes == null) {
            ppNsNodes = new HashMap<String, XNode>();
            ppNsNodes.put(nodeName, xNode);
            nodesToBePostProcessed.put(nsUri, ppNsNodes);
        } else {
            if (ppNsNodes.containsKey(nodeName)) {
                XsdLogger.printP(LOG_DEBUG, TRANSFORMATION, xNode, "Node is already marked for post-processing");
            } else {
                ppNsNodes.put(nodeName, xNode);
            }
        }
    }

    public XmlSchema getSchema(final String refSystemId, boolean shouldExists, final AlgPhase phase) {
        XmlSchema[] schemas = xmlSchemaCollection.getXmlSchema(refSystemId);
        if (schemas == null || schemas.length == 0) {
            if (shouldExists == true) {
                XsdLogger.printP(LOG_WARN, phase, "Schema with required name not found! Name=" + refSystemId);
                throw new RuntimeException("Referenced schema does not exist! Name=" + refSystemId);
            }

            return null;
        }

        if (schemas.length > 1) {
            XsdLogger.printP(LOG_WARN, phase, "Multiple schemas with required name have been found! Name=" + refSystemId);
        }

        return schemas[0];
    }

    public XmlSchema getSchemaByNamespace(final String nsUri, boolean shouldExists, final AlgPhase phase) {
        String schemaName = getSchemaNameByNamespace(nsUri, shouldExists, phase);
        XmlSchema schema = null;
        if (schemaName != null) {
            schema = getSchema(schemaName, false, phase);
        }

        if (schema == null && shouldExists) {
            XsdLogger.printP(LOG_WARN, phase, "Schema with required name not found! Namespace=" + nsUri);
            throw new RuntimeException("Referenced schema does not exist! Namespace=" + nsUri);
        }

        return schema;
    }

    public String getSchemaNameByNamespace(final String nsUri, boolean shouldExists, final AlgPhase phase) {
        XsdSchemaImportLocation schemaLocation = schemaLocationsCtx.get(nsUri);
        String schemaName = null;
        if (schemaLocation != null) {
            schemaName = schemaLocation.getFileName();
        } else {
            schemaLocation = extraSchemaLocationsCtx.get(nsUri);
            if (schemaLocation != null) {
                schemaName = schemaLocation.getFileName();
            }
        }

        if (schemaName == null && shouldExists) {
            XsdLogger.printP(LOG_WARN, phase, "Schema with required name not found! Namespace=" + nsUri);
            throw new RuntimeException("Referenced schema does not exist! Namespace=" + nsUri);
        }

        return schemaName;
    }


    public SchemaNode addOrUpdateNode(SchemaNode node) {
        final String xPos = node.getXdNode().getXDPosition();
        final String systemId = XsdNamespaceUtils.getReferenceSystemId(xPos);
        final String nodePath = XsdNameUtils.getXNodePath(xPos);
        return addOrUpdateNode(systemId, nodePath, node);
    }

    public SchemaNode addOrUpdateNodeInDiffNs(SchemaNode node, final String systemId) {
        final String xPos = node.getXdNode().getXDPosition();
        final String nodePath = XsdNameUtils.getPostProcessingReferenceNodePath(xPos);
        return addOrUpdateNode(systemId, nodePath, node);
    }

    public SchemaNode addOrUpdateNode(final String systemId, String nodePath, SchemaNode node) {
        Map<String, SchemaNode> xsdSystemRefs = getSchemaNodes(systemId);

        final SchemaNode refOrig = xsdSystemRefs.get(nodePath);
        if (refOrig != null && refOrig.getXsdNode() != null) {
            XsdLogger.printG(LOG_DEBUG, XSD_REFERENCE, "Node with this name is already defined. System=" + systemId + ", Path=" + nodePath + ", Node=" + node.getXdPosition());
            return node;
        }

        String msg = node.hasReference() ? " (with reference)" : "";
        if (refOrig != null) {
            refOrig.copy(node);
            msg = "Updating node" + msg + ". System=" + systemId + ", Path=" + nodePath + ", Node=" + node.getXdPosition();
            if (node.getXsdNode() != null) {
                msg += ", Xsd=" + node.getXsdNode().getClass().getSimpleName();
            }
            XsdLogger.printG(LOG_INFO, XSD_REFERENCE, msg);
            return refOrig;
        } else {
            xsdSystemRefs.put(nodePath, node);
            msg = "Creating node" + msg + ". System=" + systemId + ", Path=" + nodePath + ", Node=" + node.getXdPosition();
            if (node.getXsdNode() != null) {
                msg += ", Xsd=" + node.getXsdNode().getClass().getSimpleName();
            }
            XsdLogger.printG(LOG_INFO, XSD_REFERENCE, msg);
            return node;
        }
    }

    public void updateNode(final XNode xNode, final XmlSchemaNamed newXsdNode) {
        final String xPos = xNode.getXDPosition();
        final String systemId = XsdNamespaceUtils.getReferenceSystemId(xPos);
        final String nodePath = XsdNameUtils.getXNodePath(xPos);
        updateNode(systemId, nodePath, newXsdNode);
    }

    public void updateNode(final String systemId, String nodePath, final XmlSchemaNamed newXsdNode) {
        XsdLogger.printG(LOG_INFO, XSD_REFERENCE, "Updating xsd content of node. System=" + systemId + ", Path=" + nodePath + ", NewXsd=" + newXsdNode.getClass().getSimpleName());

        final Map<String, SchemaNode> xsdSystemRefs = getSchemaNodes(systemId);
        final SchemaNode refOrig = xsdSystemRefs.get(nodePath);

        if (refOrig == null) {
            XsdLogger.printG(LOG_WARN, XSD_REFERENCE, "Node does not exist in system! System=" + systemId + ", Path=" + nodePath);
            return;
        }

        refOrig.setXsdNode(newXsdNode);
    }

    public Map<String, SchemaNode> getSchemaNodes(final String systemId) {
        Map<String, SchemaNode> xsdSystemRefs = nodes.get(systemId);
        if (xsdSystemRefs == null) {
            xsdSystemRefs = new HashMap<String, SchemaNode>();
            nodes.put(systemId, xsdSystemRefs);
        }

        return xsdSystemRefs;
    }

    public SchemaNode getSchemaNode(final String systemId, final String nodePath) {
        final Map<String, SchemaNode> xsdSystemRefs = nodes.get(systemId);
        if (xsdSystemRefs == null) {
            return null;
        }

        return xsdSystemRefs.get(nodePath);
    }

    public void removeNode(final XNode xNode) {
        final String xPos = xNode.getXDPosition();
        final String systemId = XsdNamespaceUtils.getReferenceSystemId(xPos);
        final String nodePath = XsdNameUtils.getXNodePath(xPos);
        removeNode(systemId, nodePath);
    }

    public void removeNode(final String systemId, String nodePath) {
        XsdLogger.printG(LOG_INFO, XSD_REFERENCE, "Removing xsd node. System=" + systemId + ", Path=" + nodePath);

        final Map<String, SchemaNode> xsdSystemRefs = getSchemaNodes(systemId);
        final SchemaNode refOrig = xsdSystemRefs.remove(nodePath);
        if (refOrig != null) {
            XsdLogger.printG(LOG_DEBUG, XSD_REFERENCE, "Node has been removed! System=" + systemId + ", Path=" + nodePath + ", NodeName: " + refOrig.getXdName());
        }
    }

    public Set<String> getSchemaRootNodeNames(final String schemaName) {
        return rootNodeNames.get(schemaName);
    }

    public void addRootNodeName(final String schemaName, final String nodeName) {
        Set<String> schemaRootNodeNames = getSchemaRootNodeNames(schemaName);
        if (schemaRootNodeNames == null) {
            schemaRootNodeNames = new HashSet<String>();
            rootNodeNames.put(schemaName, schemaRootNodeNames);
        }

        schemaRootNodeNames.add(nodeName);
    }

    public boolean hasEnableFeature(final XD2XsdFeature feature) {
        return features.contains(feature);
    }

    public UniqueConstraints addUniqueInfo(final String name, final String systemId, String path) {
        final Map<String, List<UniqueConstraints>> uniqueInfoMap = getOrCreateSchemUniqueInfo(systemId);
        if (!path.isEmpty()) {
            path = "/" + path;
        }
        UniqueConstraints uniqueInfo = getUniqueConstraints(uniqueInfoMap, name, path);

        if (uniqueInfo == null) {
            XsdLogger.printP(LOG_INFO, PREPROCESSING, "Creating unique set. Name=" + name + ", Path=" + path + ", System=" + systemId);
            List<UniqueConstraints> uniqueInfoList = uniqueInfoMap.get(path);
            if (uniqueInfoList == null) {
                uniqueInfoList = new LinkedList<UniqueConstraints>();
                uniqueInfoMap.put(path, uniqueInfoList);
            }

            uniqueInfo = new UniqueConstraints(name);
            uniqueInfoList.add(uniqueInfo);
        } else {
            XsdLogger.printP(LOG_DEBUG, PREPROCESSING, "Creating unique set - already exists. Name=" + name + ", Path=" + path + ", System=" + systemId);
        }

        return uniqueInfo;
    }

    public void addVarToUniqueInfo(final XData xData, final UniqueConstraints uniqueConstraints) {
        final String varName = XsdNameUtils.getUniqueSetVarName(xData.getValueTypeName());
        final String nodePath = XsdNameUtils.getXNodePath(xData.getXDPosition());
        final String parserName = xData.getParserName();
        final QName qName = XD2XsdParserMapping.getDefaultParserQName(parserName, this);

        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xData, "Add variable to unique set. UniqueName=" + uniqueConstraints.getName() + ", VarName=" + varName + ", QName=" + qName);

        if (qName != null) {
            uniqueConstraints.addVar(varName, qName);
        }

        // TODO: Attributes using ID(), IDREF(), ... don't contain ID, IDREF in valueTypeName
//        if (XD_UNIQUE_ID.equals(varName)) {
//            uniqueConstraints.addKey(nodePath);
//        } else if (XD_UNIQUE_IDREF.equals(varName)) {
//            uniqueConstraints.addRef(nodePath);
//        } else if (XD_UNIQUE_IDREFS.equals(varName)) {
//            uniqueConstraints.addRef(nodePath);
//        }
    }

    private UniqueConstraints getUniqueConstraints(final Map<String, List<UniqueConstraints>> uniqueInfoMap, final String name, final String path) {
        if (!uniqueInfoMap.isEmpty()) {
            final List<UniqueConstraints> uniqueInfoList = uniqueInfoMap.get(path);
            if (uniqueInfoList != null && !uniqueInfoList.isEmpty()) {
                for (UniqueConstraints u : uniqueInfoList) {
                    if (u.getName().equals(name)) {
                        return u;
                    }
                }
            }
        }

        return null;
    }

    public UniqueConstraints findUniqueInfo(final XData xData) {
        XsdLogger.printP(LOG_DEBUG, TRANSFORMATION, xData, "Finding unique set. Name=" + xData.getValueTypeName());

        final String systemId = XsdNamespaceUtils.getReferenceSystemId(xData.getXDPosition());
        final String uniqueInfoName = XsdNameUtils.getUniqueSetName(xData.getValueTypeName());

        final Map<String, List<UniqueConstraints>> uniqueInfoMap = getOrCreateSchemUniqueInfo(systemId);
        UniqueConstraints uniqueInfo = null;
        String uniquestSetPath = "/" + XsdNameUtils.getXNodePath(xData.getXDPosition());
        int slashPos;

        if (!uniqueInfoMap.isEmpty()) {
            while (uniqueInfo == null && !"".equals(uniquestSetPath)) {
                slashPos = uniquestSetPath.lastIndexOf('/');
                if (slashPos == -1) {
                    uniquestSetPath = "";
                } else {
                    uniquestSetPath = uniquestSetPath.substring(0, slashPos);
                }
                uniqueInfo = getUniqueConstraints(uniqueInfoMap, uniqueInfoName, uniquestSetPath);
            }
        }

        return uniqueInfo;
    }

    public final Map<String, List<UniqueConstraints>> getSchemUniqueInfo(final String systemId) {
        return uniqueRestrictions.get(systemId);
    }

    public Map<String, List<UniqueConstraints>> getOrCreateSchemUniqueInfo(final String systemId) {
        Map<String, List<UniqueConstraints>> uniqueInfo = uniqueRestrictions.get(systemId);
        if (uniqueInfo == null) {
            uniqueInfo = new HashMap<String, List<UniqueConstraints>>();
            uniqueRestrictions.put(systemId, uniqueInfo);
        }

        return uniqueInfo;
    }


}
