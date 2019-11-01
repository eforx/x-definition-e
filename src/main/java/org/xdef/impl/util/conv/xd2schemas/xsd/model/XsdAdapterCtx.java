package org.xdef.impl.util.conv.xd2schemas.xsd.model;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.utils.XmlSchemaNamed;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.xd2schemas.xsd.definition.AlgPhase;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdNameUtils;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdNamespaceUtils;

import java.util.*;

import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.AlgPhase.PREPROCESSING;
import static org.xdef.impl.util.conv.xd2schemas.xsd.definition.AlgPhase.TRANSFORMATION;
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

    public void init() {
        schemaNames = new HashSet<String>();
        schemaLocationsCtx = new HashMap<String, XsdSchemaImportLocation>();
        extraSchemaLocationsCtx = new HashMap<String, XsdSchemaImportLocation>();
        xmlSchemaCollection = new XmlSchemaCollection();
        nodes = new HashMap<String, Map<String, SchemaNode>>();
        nodesToBePostProcessed = new HashMap<String, Map<String, XNode>>();
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

    public boolean existsSchemaLocation(final String nsUri) {
        return schemaLocationsCtx.containsKey(nsUri);
    }

    public boolean isPostProcessingNamespace(final String nsUri) {
        return extraSchemaLocationsCtx.containsKey(nsUri);
    }

    public void addNodeToPostProcessing(final String nsUri, final XNode xNode) {
        XsdLogger.printP(LOG_INFO, TRANSFORMATION, xNode, "Add attribute to post-processing.");

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
        final String nodePath = XsdNameUtils.getReferenceNodePath(xPos);
        return addOrUpdateNode(systemId, nodePath, node);
    }

    public SchemaNode addOrUpdateNode(final String systemId, String nodePath, SchemaNode node) {
        Map<String, SchemaNode> xsdSystemRefs = getSystemRefs(systemId);

        final SchemaNode refOrig = xsdSystemRefs.get(nodePath);
        if (refOrig != null && refOrig.getXsdNode() != null) {
            XsdLogger.printG(LOG_DEBUG, XSD_REFERENCE, "Node with this name is already defined. System=" + systemId + ", Path=" + nodePath + ", Node=" + node.getXdPosition());
            return node;
        }

        final String msg = node.hasReference() ? " (with reference)" : "";
        if (refOrig != null) {
            refOrig.copy(node);
            XsdLogger.printG(LOG_INFO, XSD_REFERENCE, "Updating node" + msg + ". System=" + systemId + ", Path=" + nodePath + ", Node=" + node.getXdPosition());
            return refOrig;
        } else {
            xsdSystemRefs.put(nodePath, node);
            XsdLogger.printG(LOG_INFO, XSD_REFERENCE, "Creating node" + msg + ". System=" + systemId + ", Path=" + nodePath + ", Node=" + node.getXdPosition());
            return node;
        }
    }

    public void updateNode(final XNode xNode, final XmlSchemaNamed newXsdNode) {
        final String systemId = XsdNamespaceUtils.getReferenceSystemId(xNode.getXDPosition());
        final String nodePath = XsdNameUtils.getReferenceNodePath(xNode.getXDPosition());
        updateNode(systemId, nodePath, newXsdNode);
    }

    public void updateNode(final String systemId, String nodePath, final XmlSchemaNamed newXsdNode) {
        XsdLogger.printG(LOG_INFO, XSD_REFERENCE, "Updating xsd content of node. System=" + systemId + ", Path=" + nodePath);

        Map<String, SchemaNode> xsdSystemRefs = getSystemRefs(systemId);

        final SchemaNode refOrig = xsdSystemRefs.get(nodePath);
        if (refOrig == null) {
            XsdLogger.printG(LOG_WARN, XSD_REFERENCE, "Node does not exist in system! System=" + systemId + ", Path=" + nodePath);
            return;
        }

        refOrig.setXsdNode(newXsdNode);
    }

    public Map<String, SchemaNode> getSystemRefs(final String systemId) {
        Map<String, SchemaNode> xsdSystemRefs = nodes.get(systemId);
        if (xsdSystemRefs == null) {
            xsdSystemRefs = new HashMap<String, SchemaNode>();
            nodes.put(systemId, xsdSystemRefs);
        }

        return xsdSystemRefs;
    }

}
