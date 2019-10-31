package org.xdef.impl.util.conv.xd2schemas.xsd.model;

import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.xdef.impl.XNode;
import org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLogger;

import java.util.*;

import static org.xdef.impl.util.conv.xd2schemas.xsd.util.AlgPhase.PREPROCESSING;
import static org.xdef.impl.util.conv.xd2schemas.xsd.util.AlgPhase.TRANSFORMATION;
import static org.xdef.impl.util.conv.xd2schemas.xsd.util.XsdLoggerDefs.*;

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
    private Map<String, XmlSchemaImportLocation> schemaLocationsCtx = null;

    /**
     * Schemas locations which are created in post-processing
     * Key:     schema namespace URI
     * Value:   schema location
     */
    private Map<String, XmlSchemaImportLocation> extraSchemaLocationsCtx = null;

    /**
     * Collection of created XSD schemas
     */
    private XmlSchemaCollection xmlSchemaCollection = null;

    /**
     *
     * Key:     schema name
     * Value:   node path, schema node
     */
    private Map<String, Map<String, SchemaRefNode>> nodeRefs = null;

    /**
     * Nodes which will be created in post-procession
     * Key:     namespace URI
     * Value:   node name, x-definition node
     */
    private Map<String, Map<String, XNode>> nodesToBePostProcessed;

    public void init() {
        schemaNames = new HashSet<String>();
        schemaLocationsCtx = new HashMap<String, XmlSchemaImportLocation>();
        extraSchemaLocationsCtx = new HashMap<String, XmlSchemaImportLocation>();
        xmlSchemaCollection = new XmlSchemaCollection();
        nodeRefs = new HashMap<String, Map<String, SchemaRefNode>>();
        nodesToBePostProcessed = new HashMap<String, Map<String, XNode>>();
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
    public void addSchemaLocation(final String nsUri, final XmlSchemaImportLocation importLocation) {
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

    public final Set<String> getSchemaNames() {
        return schemaNames;
    }

    public final Map<String, XmlSchemaImportLocation> getSchemaLocationsCtx() {
        return schemaLocationsCtx;
    }

    public final Map<String, XmlSchemaImportLocation> getExtraSchemaLocationsCtx() {
        return extraSchemaLocationsCtx;
    }

    public final XmlSchemaCollection getXmlSchemaCollection() {
        return xmlSchemaCollection;
    }

    public final Map<String, Map<String, SchemaRefNode>> getNodeRefs() {
        return nodeRefs;
    }

    public Map<String, Map<String, XNode>> getNodesToBePostProcessed() {
        return nodesToBePostProcessed;
    }
}
