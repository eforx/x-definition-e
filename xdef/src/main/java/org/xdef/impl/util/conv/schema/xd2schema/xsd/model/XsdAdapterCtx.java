package org.xdef.impl.util.conv.schema.xd2schema.xsd.model;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.utils.XmlSchemaNamed;
import org.xdef.impl.XData;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.schema.util.SchemaLogger;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.AlgPhase;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.Xd2XsdFeature;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.factory.XsdNameFactory;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.util.XsdNameUtils;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.util.XsdNamespaceUtils;

import java.util.*;

import static org.xdef.impl.util.conv.schema.util.SchemaLoggerDefs.*;
import static org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.AlgPhase.PREPROCESSING;
import static org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.AlgPhase.TRANSFORMATION;
import static org.xdef.impl.util.conv.schema.xd2schema.xsd.util.Xd2XsdLoggerDefs.XSD_ADAPTER_CTX;
import static org.xdef.impl.util.conv.schema.xd2schema.xsd.util.Xd2XsdLoggerDefs.XSD_REFERENCE;

/**
 * Basic XSD context for transformation x-definition to XSD document
 */
public class XsdAdapterCtx {

    /**
     * Names of created XSD documents
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
     * Collection of created XSD documents
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
    private Map<String, Map<String, List<UniqueConstraint>>> uniqueRestrictions;

    private XsdNameFactory nameFactory;

    /**
     * Enabled algorithm features
     */
    final private Set<Xd2XsdFeature> features;

    public XsdAdapterCtx(Set<Xd2XsdFeature> features) {
        this.features = features;
    }

    /**
     * Initializes XSD adapter context
     */
    public void init() {
        schemaNames = new HashSet<String>();
        schemaLocationsCtx = new HashMap<String, XsdSchemaImportLocation>();
        extraSchemaLocationsCtx = new HashMap<String, XsdSchemaImportLocation>();
        xmlSchemaCollection = new XmlSchemaCollection();
        nodes = new HashMap<String, Map<String, SchemaNode>>();
        nodesToBePostProcessed = new HashMap<String, Map<String, XNode>>();
        rootNodeNames = new HashMap<String, Set<String>>();
        uniqueRestrictions = new HashMap<String, Map<String, List<UniqueConstraint>>>();
        nameFactory = new XsdNameFactory(this);
    }

    public Set<String> getSchemaNames() {
        return schemaNames;
    }

    public Map<String, XsdSchemaImportLocation> getExtraSchemaLocationsCtx() {
        return extraSchemaLocationsCtx;
    }

    public XmlSchemaCollection getXmlSchemaCollection() {
        return xmlSchemaCollection;
    }

    public Map<String, Map<String, SchemaNode>> getNodes() {
        return nodes;
    }

    public Map<String, Map<String, XNode>> getNodesToBePostProcessed() {
        return nodesToBePostProcessed;
    }

    public XsdNameFactory getNameFactory() {
        return nameFactory;
    }

    /**
     * Add XSD document name to name set
     * @param name  x-definition name
     */
    public void addSchemaName(final String name) throws RuntimeException {
        if (!schemaNames.add(name)) {
            SchemaLogger.printG(LOG_ERROR, XSD_ADAPTER_CTX, "Schema with this name has been already processed! Name=" + name);
            throw new RuntimeException("X-definition name duplication");
        }
    }

    /**
     * Add XSD document location into map
     * @param nsUri             XSD document namespace URI
     * @param importLocation    XSD document location definition
     */
    public void addSchemaLocation(final String nsUri, final XsdSchemaImportLocation importLocation) {
        if (schemaLocationsCtx.containsKey(nsUri)) {
            SchemaLogger.printG(LOG_WARN, XSD_ADAPTER_CTX, "Schema location already exists for namespace URI. NamespaceURI=" + nsUri);
            return;
        }

        SchemaLogger.printP(LOG_INFO, PREPROCESSING, "Add schema location. NamespaceURI=" + nsUri + ", Path=" + importLocation.buildLocation(null));
        schemaLocationsCtx.put(nsUri, importLocation);
    }

    /**
     * Check if schema with given namespace URI exists
     * @param nsUri     XSD document namespace URI
     * @return true if schema exists
     */
    public boolean existsSchemaLocation(final String nsUri) {
        return schemaLocationsCtx.containsKey(nsUri);
    }

    /**
     * Finds XSD document location if exists by given namespace URI
     * @param nsUri     XSD document namespace URI
     * @return XSD document location if exists, otherwise null
     */
    public XsdSchemaImportLocation findSchemaImport(final String nsUri) {
        return schemaLocationsCtx.get(nsUri);
    }

    /**
     * Finds XSD document location if exists by given namespace URI
     * @param nsUri     XSD document namespace URI
     * @return XSD document location if exists, otherwise null
     */
    public XsdSchemaImportLocation findPostProcessingSchemaImport(final String nsUri) {
        return extraSchemaLocationsCtx.get(nsUri);
    }

    /**
     * Add XSD document into extra map. Internally creates document location.
     * @param nsPrefix          XSD document namespace prefix
     * @param nsUri             XSD document namespace URI
     */
    public void addExtraSchemaLocation(final String nsPrefix, final String nsUri) {
        if (extraSchemaLocationsCtx.containsKey(nsUri)) {
            SchemaLogger.printG(LOG_DEBUG, XSD_ADAPTER_CTX, "Extra schema location already exists for namespace URI. NamespaceURI=" + nsUri);
            return;
        }

        final String schemaName = XsdNamespaceUtils.createExtraSchemaNameFromNsPrefix(nsPrefix);
        SchemaLogger.printP(LOG_INFO, PREPROCESSING, "Add external schema to post-process queue. NamespaceURI=" + nsUri + ", SchemaName=" + schemaName);
        extraSchemaLocationsCtx.put(nsUri, new XsdSchemaImportLocation(nsUri, schemaName));
    }

    /**
     * Add XSD document into extra map. Internally creates document location.
     * @param nsUri             XSD document namespace URI
     * @param importLocation    XSD document location definition
     */
    public void addExtraSchemaLocation(final String nsUri, final XsdSchemaImportLocation importLocation) {
        if (extraSchemaLocationsCtx.containsKey(nsUri)) {
            SchemaLogger.printG(LOG_DEBUG, XSD_ADAPTER_CTX, "Extra schema location already exists for namespace URI. NamespaceURI=" + nsUri);
            return;
        }

        SchemaLogger.printP(LOG_INFO, PREPROCESSING, "Add external schema to post-process queue. NamespaceURI=" + nsUri);
        extraSchemaLocationsCtx.put(nsUri, importLocation);
    }

    /**
     * Check if XSD document with given namespace URI should be created in post-processing
     * @param nsUri     XSD document namespace URI
     * @return true if XSD document should be created in post-processing
     */
    public boolean isPostProcessingNamespace(final String nsUri) {
        return extraSchemaLocationsCtx.containsKey(nsUri);
    }

    /**
     * Mark x-definition node to be converted in post-processing phase
     * @param nsUri     XSD document namespace URI
     * @param xNode     X-definition node
     */
    public void addNodeToPostProcessing(final String nsUri, final XNode xNode) {
        SchemaLogger.printP(LOG_INFO, TRANSFORMATION, xNode, "Add node to post-processing.");

        final String nodeName = xNode.getName();
        Map<String, XNode> ppNsNodes = nodesToBePostProcessed.get(nsUri);

        if (ppNsNodes == null) {
            ppNsNodes = new HashMap<String, XNode>();
            nodesToBePostProcessed.put(nsUri, ppNsNodes);
        }

        if (ppNsNodes.containsKey(nodeName)) {
            SchemaLogger.printP(LOG_DEBUG, TRANSFORMATION, xNode, "Node is already marked for post-processing");
        } else {
            ppNsNodes.put(nodeName, xNode);
        }
    }

    /**
     * Finds XSD document by given system identifier.
     *
     * Throws exception if {@paramref shouldExists} value is true and XSD document does not exist
     * @param systemId      XSD document system identifier
     * @param shouldExists  flag, it non-existing schema should throw exception
     * @param phase         phase of transforming algorithm (just for logging purposes)
     * @return  XSD document if exists
     *          null if XSD document does not exist and {@paramref shouldExists} value is false
     */
    public XmlSchema findSchema(final String systemId, boolean shouldExists, final AlgPhase phase) {
        XmlSchema[] schemas = xmlSchemaCollection.getXmlSchema(systemId);
        if (schemas == null || schemas.length == 0) {
            if (shouldExists == true) {
                SchemaLogger.printP(LOG_WARN, phase, "Schema with required name not found! Name=" + systemId);
                throw new RuntimeException("Referenced schema does not exist! Name=" + systemId);
            }

            return null;
        }

        if (schemas.length > 1) {
            SchemaLogger.printP(LOG_WARN, phase, "Multiple schemas with required name have been found! Name=" + systemId);
        }

        return schemas[0];
    }

    /**
     * Finds XSD document by given namespace URI
     * @param nsUri         XSD document namespace URI
     * @param shouldExists  flag, it non-existing schema should throw exception
     * @param phase         phase of transforming algorithm (just for logging purposes)
     * @return  XSD document if exists
     *          null if XSD document does not exist and {@paramref shouldExists} value is false
     */
    public XmlSchema findSchemaByNamespace(final String nsUri, boolean shouldExists, final AlgPhase phase) {
        final String schemaName = findSchemaNameByNamespace(nsUri, shouldExists, phase);
        XmlSchema schema = null;
        if (schemaName != null) {
            schema = findSchema(schemaName, false, phase);
        }

        if (schema == null && shouldExists) {
            SchemaLogger.printP(LOG_WARN, phase, "Schema with required name not found! Namespace=" + nsUri);
            throw new RuntimeException("Referenced schema does not exist! Namespace=" + nsUri);
        }

        return schema;
    }

    /**
     * Finds XSD document name by given namespace URI
     * @param nsUri         XSD document namespace URI
     * @param shouldExists  flag, it non-existing schema should throw exception
     * @param phase         phase of transforming algorithm (just for logging purposes)
     * @return  XSD document name if XSD document exists
     *          null if XSD document does not exist and {@paramref shouldExists} value is false
     */
    public String findSchemaNameByNamespace(final String nsUri, boolean shouldExists, final AlgPhase phase) {
        XsdSchemaImportLocation schemaLocation = schemaLocationsCtx.get(nsUri);
        String schemaName = null;
        if (schemaLocation == null) {
            schemaLocation = extraSchemaLocationsCtx.get(nsUri);
        }

        if (schemaLocation != null) {
            schemaName = schemaLocation.getFileName();
        }

        if (schemaName == null && shouldExists) {
            SchemaLogger.printP(LOG_WARN, phase, "Schema with required name not found! Namespace=" + nsUri);
            throw new RuntimeException("Referenced schema does not exist! Namespace=" + nsUri);
        }

        return schemaName;
    }

    /**
     * Add new or update already existing schema node into XSD document defined by namespace of input {@paramref node}
     * @param node  schema node to be added
     * @return  schema node defined by {@paramref node} if no node exists with same node path
     *          otherwise already existing node with same node path merged with {@paramref node}
     */
    public SchemaNode addOrUpdateNode(final SchemaNode node) {
        final String xPos = node.getXdNode().getXDPosition();
        final String systemId = XsdNamespaceUtils.getSystemIdFromXPos(xPos);
        final String nodePath = XsdNameUtils.getXNodePath(xPos);
        return addOrUpdateNode(systemId, nodePath, node);
    }

    /**
     * Add new or update already existing schema node into XSD document defined by {@paramref systemId}
     * @param node      schema node to be added
     * @param systemId  XSD document identifier
     * @return  schema node defined by {@paramref node} if no node exists with same node path
     *          otherwise already existing node with same node path merged with {@paramref node}
     */
    public SchemaNode addOrUpdateNodeInDiffNs(final SchemaNode node, final String systemId) {
        final String xPos = node.getXdNode().getXDPosition();
        final String nodePath = SchemaNode.getPostProcessingReferenceNodePath(xPos);
        return addOrUpdateNode(systemId, nodePath, node);
    }

    /**
     * Add new or update already existing schema node into XSD document defined by {@paramref systemId} and {@paramref nodePath}
     * @param systemId  XSD document identifier
     * @param nodePath  x-definition node path
     * @param node      schema node to be added
     * @return  schema node defined by {@paramref node} if no node exists with same node path
     *          otherwise already existing node with same node path merged with {@paramref node}
     */
    public SchemaNode addOrUpdateNode(final String systemId, final String nodePath, final SchemaNode node) {
        Map<String, SchemaNode> xsdSystemRefs = findSchemaNodes(systemId);

        final SchemaNode refOrig = xsdSystemRefs.get(nodePath);
        if (refOrig != null && refOrig.getXsdNode() != null) {
            SchemaLogger.printG(LOG_DEBUG, XSD_REFERENCE, "Node with this name is already defined. System=" + systemId + ", Path=" + nodePath + ", Node=" + node.getXdPosition());
            return node;
        }

        String msg = node.hasReference() ? " (with reference)" : "";
        if (refOrig != null) {
            refOrig.copyNodes(node);
            msg = "Updating node" + msg + ". System=" + systemId + ", Path=" + nodePath + ", Node=" + node.getXdPosition();
            if (node.getXsdNode() != null) {
                msg += ", Xsd=" + node.getXsdNode().getClass().getSimpleName();
            }
            SchemaLogger.printG(LOG_INFO, XSD_REFERENCE, msg);
            return refOrig;
        } else {
            xsdSystemRefs.put(nodePath, node);
            msg = "Creating node" + msg + ". System=" + systemId + ", Path=" + nodePath + ", Node=" + node.getXdPosition();
            if (node.getXsdNode() != null) {
                msg += ", Xsd=" + node.getXsdNode().getClass().getSimpleName();
            }
            SchemaLogger.printG(LOG_INFO, XSD_REFERENCE, msg);
            return node;
        }
    }

    /**
     * Updates XSD node of schema node defined by x-definition node {@paramref xNode}
     * @param xNode         x-definition node of schema node
     * @param newXsdNode    new XSD document node
     */
    public void updateNode(final XNode xNode, final XmlSchemaNamed newXsdNode) {
        final String xPos = xNode.getXDPosition();
        final String systemId = XsdNamespaceUtils.getSystemIdFromXPos(xPos);
        final String nodePath = XsdNameUtils.getXNodePath(xPos);
        updateNode(systemId, nodePath, newXsdNode);
    }

    private void updateNode(final String systemId, String nodePath, final XmlSchemaNamed newXsdNode) {
        SchemaLogger.printG(LOG_INFO, XSD_REFERENCE, "Updating xsd content of node. System=" + systemId + ", Path=" + nodePath + ", NewXsd=" + newXsdNode.getClass().getSimpleName());

        final Map<String, SchemaNode> xsdSystemRefs = findSchemaNodes(systemId);
        final SchemaNode refOrig = xsdSystemRefs.get(nodePath);

        if (refOrig == null) {
            SchemaLogger.printG(LOG_WARN, XSD_REFERENCE, "Node does not exist in system! System=" + systemId + ", Path=" + nodePath);
            return;
        }

        refOrig.setXsdNode(newXsdNode);
    }

    /**
     * Finds all created schema nodes in XSD document
     * @param systemId  XSD document identifier
     * @return  map of schema nodes
     */
    public Map<String, SchemaNode> findSchemaNodes(final String systemId) {
        Map<String, SchemaNode> xsdSystemRefs = nodes.get(systemId);
        if (xsdSystemRefs == null) {
            xsdSystemRefs = new HashMap<String, SchemaNode>();
            nodes.put(systemId, xsdSystemRefs);
        }

        return xsdSystemRefs;
    }

    /**
     * Finds schema node defined by {@paramref systemId} and {@paramref nodePath}
     * @param systemId  XSD document identifier
     * @param nodePath  x-definition path
     * @return  schema node if exists, otherwise null
     */
    public SchemaNode findSchemaNode(final String systemId, final String nodePath) {
        final Map<String, SchemaNode> xsdSystemRefs = nodes.get(systemId);
        if (xsdSystemRefs == null) {
            return null;
        }

        return xsdSystemRefs.get(nodePath);
    }

    /**
     * Deletes created schema node defined by x-definition node {@paramref xNode}
     * @param xNode     x-definition node
     */
    public void removeNode(final XNode xNode) {
        final String xPos = xNode.getXDPosition();
        final String systemId = XsdNamespaceUtils.getSystemIdFromXPos(xPos);
        final String nodePath = XsdNameUtils.getXNodePath(xPos);
        removeNode(systemId, nodePath);
    }

    /**
     * Deletes created schema node defined by XSD document identifier and node path
     * @param systemId      XSD document identifier
     * @param nodePath      x-definition node path
     */
    private void removeNode(final String systemId, final String nodePath) {
        SchemaLogger.printG(LOG_INFO, XSD_REFERENCE, "Removing xsd node. System=" + systemId + ", Path=" + nodePath);

        final Map<String, SchemaNode> xsdSystemRefs = findSchemaNodes(systemId);
        final SchemaNode refOrig = xsdSystemRefs.remove(nodePath);
        if (refOrig != null) {
            SchemaLogger.printG(LOG_DEBUG, XSD_REFERENCE, "Node has been removed! System=" + systemId + ", Path=" + nodePath + ", NodeName: " + refOrig.getXdName());
        }
    }

    /**
     * Finds XSD document root node's names by XSD document identifier
     * @param systemId      XSD document identifier
     * @return XSD document root node's names if exist, otherwise null
     */
    public Set<String> findSchemaRootNodeNames(final String systemId) {
        return rootNodeNames.get(systemId);
    }

    /**
     * Add XSD document root node name
     * @param systemId      XSD document identifier
     * @param nodeName      x-definition name
     */
    public void addRootNodeName(final String systemId, final String nodeName) {
        Set<String> schemaRootNodeNames = findSchemaRootNodeNames(systemId);
        if (schemaRootNodeNames == null) {
            schemaRootNodeNames = new HashSet<String>();
            rootNodeNames.put(systemId, schemaRootNodeNames);
        }

        schemaRootNodeNames.add(nodeName);
    }

    /**
     * Check if algorithm feature is enabled
     * @param feature       algorithm feature
     * @return  true if algorithm feature is enabled
     */
    public boolean hasEnableFeature(final Xd2XsdFeature feature) {
        return features.contains(feature);
    }

    /**
     * Creates and saves unique constraint based on input parameters if does not already exist
     * @param name      unique constraint name
     * @param systemId  XSD document identifier (can be nullable)
     * @param path      unique constraint path
     * @return Created or found unique constraint
     */
    public UniqueConstraint addOrGetUniqueConst(final String name, String systemId, String path) {
        if (systemId == null) {
            systemId = "";
        }

        final Map<String, List<UniqueConstraint>> uniqueInfoMap = getOrCreateSchemaUniqueInfo(systemId);
        if (!path.isEmpty()) {
            path = "/" + path;
        }

        UniqueConstraint uniqueConstraint = findUniqueConstraint(uniqueInfoMap, name, path);

        if (uniqueConstraint == null) {
            SchemaLogger.printP(LOG_INFO, PREPROCESSING, "Creating unique set. Name=" + name + ", Path=" + path + ", System=" + systemId);
            List<UniqueConstraint> uniqueInfoList = uniqueInfoMap.get(path);
            if (uniqueInfoList == null) {
                uniqueInfoList = new LinkedList<UniqueConstraint>();
                uniqueInfoMap.put(path, uniqueInfoList);
            }

            uniqueConstraint = new UniqueConstraint(name, systemId);
            uniqueInfoList.add(uniqueConstraint);
        } else {
            SchemaLogger.printP(LOG_DEBUG, PREPROCESSING, "Creating unique set - already exists. Name=" + name + ", Path=" + path + ", System=" + systemId);
        }

        return uniqueConstraint;
    }

    /**
     * Finds unique constraint
     * @param uniqueConstraintMap   unique constraint map
     * @param name                  unique constraint name
     * @param path                  unique constraint path
     * @return unique constraint if exists inside given map, otherwise null
     */
    private static UniqueConstraint findUniqueConstraint(final Map<String, List<UniqueConstraint>> uniqueConstraintMap, final String name, final String path) {
        if (!uniqueConstraintMap.isEmpty()) {
            final List<UniqueConstraint> uniqueInfoList = uniqueConstraintMap.get(path);
            if (uniqueInfoList != null && !uniqueInfoList.isEmpty()) {
                for (UniqueConstraint u : uniqueInfoList) {
                    if (u.getName().equals(name)) {
                        return u;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Finds unique constraint by x-definition node
     * @param xData x-definition node
     * @return  unique constraint if exists, otherwise null
     */
    public UniqueConstraint findUniqueConst(final XData xData) {
        SchemaLogger.printP(LOG_DEBUG, TRANSFORMATION, xData, "Finding unique set. Name=" + xData.getValueTypeName());

        // TODO: Finding of unique set not using variable name, ie. uniqueSet u int();
        final String systemId = XsdNamespaceUtils.getSystemIdFromXPos(xData.getXDPosition());
        final String uniqueInfoName = XsdNameUtils.getUniqueSetName(xData.getValueTypeName());
        final String uniquestSetPath = "/" + XsdNameUtils.getXNodePath(xData.getXDPosition());
        UniqueConstraint uniqueInfo = findUniqueConst(uniqueInfoName, systemId, uniquestSetPath);
        if (uniqueInfo == null) {
            uniqueInfo = findUniqueConst(uniqueInfoName, "", uniquestSetPath);
        }
        return uniqueInfo;
    }

    /**
     * Finds unique constrain in tree of unique constraints.
     * Iterates through tree, starting at {@paramref uniquestSetPath} path and going to root of XML tree
     * @param uniqueInfoName        unique constraint name
     * @param systemId              XSD document identifier
     * @param uniquestSetPath       unique constraint path
     * @return unique constraint if exists inside given map, otherwise null
     */
    private UniqueConstraint findUniqueConst(final String uniqueInfoName, final String systemId, String uniquestSetPath) {
        SchemaLogger.printP(LOG_DEBUG, TRANSFORMATION, "Finding unique set. UniqueName=" + uniqueInfoName + ", SystemId=" + systemId);

        UniqueConstraint uniqueInfo = null;
        int slashPos;
        final Map<String, List<UniqueConstraint>> uniqueInfoMap = getOrCreateSchemaUniqueInfo(systemId);

        if (!uniqueInfoMap.isEmpty()) {
            while (uniqueInfo == null && !"".equals(uniquestSetPath)) {
                slashPos = uniquestSetPath.lastIndexOf('/');
                if (slashPos == -1) {
                    uniquestSetPath = "";
                } else {
                    uniquestSetPath = uniquestSetPath.substring(0, slashPos);
                }
                uniqueInfo = findUniqueConstraint(uniqueInfoMap, uniqueInfoName, uniquestSetPath);
            }
        }

        return uniqueInfo;
    }

    /**
     * Get all created unique constraints created in XSD document
     * @param systemId  XSD document identifier
     * @return  unique constraints
     */
    public Map<String, List<UniqueConstraint>> getSchemaUniqueConstraints(final String systemId) {
        return uniqueRestrictions.get(systemId);
    }

    /**
     * Create or get unique constraints map in givet XSD document
     * @param systemId  XSD document identifier
     * @return  unique constraints map
     */
    private Map<String, List<UniqueConstraint>> getOrCreateSchemaUniqueInfo(final String systemId) {
        Map<String, List<UniqueConstraint>> uniqueInfo = uniqueRestrictions.get(systemId);
        if (uniqueInfo == null) {
            uniqueInfo = new HashMap<String, List<UniqueConstraint>>();
            uniqueRestrictions.put(systemId, uniqueInfo);
        }

        return uniqueInfo;
    }


}
