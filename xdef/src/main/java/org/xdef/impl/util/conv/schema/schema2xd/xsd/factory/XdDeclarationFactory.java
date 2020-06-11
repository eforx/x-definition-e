package org.xdef.impl.util.conv.schema.schema2xd.xsd.factory;

import org.apache.ws.commons.schema.*;
import org.w3c.dom.Element;
import org.xdef.impl.util.conv.schema.util.SchemaLogger;
import org.xdef.impl.util.conv.schema.schema2xd.xsd.factory.declaration.*;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Set;

import static org.xdef.impl.util.conv.schema.util.SchemaLoggerDefs.*;
import static org.xdef.impl.util.conv.schema.xd2schema.xsd.definition.AlgPhase.TRANSFORMATION;

/**
 * Creates x-definition declarations
 */
public class XdDeclarationFactory {

    /**
     * Input schema used for transformation
     */
    private final XmlSchema schema;

    /**
     * X-definition XML node factory
     */
    final private XdNodeFactory xdFactory;

    /**
     * Set of names of already processed top level declarations
     */
    final Set<String> processedTopDeclarations = new HashSet<String>();

    public XdDeclarationFactory(XmlSchema schema, XdNodeFactory xdFactory) {
        this.schema = schema;
        this.xdFactory = xdFactory;
    }

    /**
     * Creates x-definition declaration based on given builder.
     * Append created declaration to builder x-definition parent node
     * @param builder   x-definition declaration builder
     */
    public void createDeclaration(final XdDeclarationBuilder builder) {
        SchemaLogger.printP(LOG_INFO, TRANSFORMATION, builder.simpleType, "Creating declaration ...");
        if (builder.parentNode == null) {
            SchemaLogger.printP(LOG_WARN, TRANSFORMATION, builder.simpleType, "Parent node is not set. Created declaration will be lost!");
            return;
        }

        final Element xdDeclaration = xdFactory.createEmptyDeclaration();
        xdDeclaration.setTextContent(builder.build());
        builder.parentNode.appendChild(xdDeclaration);
    }

    /**
     * Creates x-definition declaration content based on given builder.
     * @param builder   x-definition declaration builder
     * @return x-definition declaration content
     */
    public String createDeclarationContent(final XdDeclarationBuilder builder) {
        SchemaLogger.printP(LOG_INFO, TRANSFORMATION, builder.simpleType, "Creating declaration content ...");
        return builder.build();
    }

    /**
     * Creates x-definition declaration content without any restrictions
     * @param baseType      declaration qualified name
     * @return x-definition declaration content
     */
    public String createSimpleTextDeclaration(final QName baseType) {
        final DefaultTypeFactory defaultTypeFactory = new DefaultTypeFactory(baseType.getLocalPart());
        defaultTypeFactory.setType(IDeclarationTypeFactory.Type.TEXT_DECL);
        return defaultTypeFactory.build("");
    }

    /**
     * Creates default initialized x-definition declaration builder
     * @return
     */
    public XdDeclarationBuilder createBuilder() {
        return new XdDeclarationBuilder().init(schema, this);
    }

    /**
     * Check if top level x-definition declaration can be created
     * @param name  declaration name
     * @return true, if x-definition declaration with given name has not been created yet
     */
    boolean canBeProcessed(final String name) {
        return processedTopDeclarations.add(name);
    }
}
