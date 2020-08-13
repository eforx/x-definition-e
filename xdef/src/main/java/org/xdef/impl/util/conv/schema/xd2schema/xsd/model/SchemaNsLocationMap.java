package org.xdef.impl.util.conv.schema.xd2schema.xsd.model;

import org.xdef.impl.util.conv.schema.util.SchemaLogger;
import org.xdef.impl.util.conv.schema.xd2schema.xsd.util.XsdNamespaceUtils;
import org.xdef.msg.XSD;
import org.xdef.sys.ReportWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.xdef.impl.util.conv.schema.util.SchemaLoggerDefs.*;
import static org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.AlgPhase.PREPROCESSING;
import static org.xdef.impl.util.conv.schema.xd2schema.xsd.util.Xd2XsdLoggerDefs.XSD_ADAPTER_CTX;

/**
 * Schema namespace location map
 * Key:     schema namespace URI
 * Value:   schema name location Map
 */
public class SchemaNsLocationMap extends HashMap<String, SchemaNameLocationMap> {

    /**
     * Output report writer
     */
    final private ReportWriter reportWriter;
    final private String name;

    public SchemaNsLocationMap(ReportWriter reportWriter, String name) {
        this.reportWriter = reportWriter;
        this.name = name;
    }

    /**
     * Add XSD document location into map
     * @param nsUri             XSD document namespace URI
     * @param importLocation    XSD document location definition
     */
    public XsdSchemaImportLocation addSchemaLocation(final String nsUri, final XsdSchemaImportLocation importLocation) {
        SchemaNameLocationMap importLocations = get(nsUri);
        if (importLocations == null) {
            importLocations = new SchemaNameLocationMap();
            put(nsUri, importLocations);
        }

        final XsdSchemaImportLocation currImportLocation = importLocations.get(importLocation.getFileName());
        if (currImportLocation != null) {
            if (!currImportLocation.equals(importLocation)) {
                reportWriter.warning(XSD.XSD036, nsUri);
                SchemaLogger.printG(LOG_WARN, XSD_ADAPTER_CTX, "Schema location already exists for given namespace URI and file name. " +
                        "mapName=" + name + ", namespaceURI=" + nsUri + ", schema=" + importLocation);
            }

            return currImportLocation;
        }

        SchemaLogger.printP(LOG_INFO, PREPROCESSING, "Add schema location." +
                " mapName=" + name + ", schemaName=" + importLocation.getFileName() + ", namespaceURI=" + nsUri + ", Path=" + importLocation.buildLocation(null));
        importLocations.put(importLocation.getFileName(), importLocation);
        return importLocation;
    }

    /**
     * Add XSD document into map. Document location is created internally.
     * @param nsPrefix          XSD document namespace prefix
     * @param nsUri             XSD document namespace URI
     */
    public XsdSchemaImportLocation addSchemaLocation(final String nsPrefix, final String nsUri) {
        final String schemaName = XsdNamespaceUtils.createExtraSchemaNameFromNsPrefix(nsPrefix);
        return addSchemaLocation(nsUri, new XsdSchemaImportLocation(nsUri, schemaName));
//        SchemaNameLocationMap importLocations = get(nsUri);
//        if (importLocations == null) {
//            importLocations = new SchemaNameLocationMap();
//            put(nsUri, importLocations);
//        }
//
//        final String schemaName = XsdNamespaceUtils.createExtraSchemaNameFromNsPrefix(nsPrefix);
//        final XsdSchemaImportLocation importLocation = new XsdSchemaImportLocation(nsUri, schemaName);
//        final XsdSchemaImportLocation currImportLocation = importLocations.get(schemaName);
//        if (currImportLocation != null) {
//            reportWriter.warning(XSD.XSD036, nsUri);
//            SchemaLogger.printG(LOG_WARN, XSD_ADAPTER_CTX, "Schema location already exists for given namespace URI and file name. " +
//                    "mapName=" + name + ", schemaName=" + schemaName + ", namespaceURI=" + nsUri);
//            return currImportLocation;
//        }
//
//        importLocation = new XsdSchemaImportLocation(nsUri, schemaName);
//        SchemaLogger.printP(LOG_INFO, PREPROCESSING, "Add schema location." +
//                " mapName=" + name + ", schemaName=" + schemaName + ", namespaceURI=" + nsUri + ", Path=" + importLocation.buildLocation(null));
//        importLocations.put(schemaName, new XsdSchemaImportLocation(nsUri, schemaName));
//        return importLocation;
    }

    /**
     * Finds XSD document location if exists by given namespace URI
     * @param nsUri     XSD document namespace URI
     * @return XSD document location if exists, otherwise null
     */
    public XsdSchemaImportLocation findSchemaImport(final String nsUri, final String schemaName) {
        SchemaLogger.printG(LOG_TRACE, XSD_ADAPTER_CTX, "Finding schema by namespace URI and name." +
                " mapName=" + name + ", schemaName=" + schemaName + ", namespaceURI=" + nsUri);

        final Map<String, XsdSchemaImportLocation> importLocations = get(nsUri);
        if (importLocations == null || importLocations.isEmpty()) {
            return null;
        }

        return importLocations.get(schemaName);
    }

    public List<XsdSchemaImportLocation> findSchemaImports(final String nsUri) {
        SchemaLogger.printG(LOG_TRACE, XSD_ADAPTER_CTX, "Finding schema by namespace URI. mapName=" + name + ", namespaceURI=" + nsUri);
        final Map<String, XsdSchemaImportLocation> importLocations = get(nsUri);
        if (importLocations == null) {
            return new ArrayList<XsdSchemaImportLocation>();
        }

        return new ArrayList<XsdSchemaImportLocation>(importLocations.values());
    }

}
