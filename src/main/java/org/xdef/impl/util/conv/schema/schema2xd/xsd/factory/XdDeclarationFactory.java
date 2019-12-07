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

public class XdDeclarationFactory {

    /**
     * Input schema used for transformation
     */
    private final XmlSchema schema;

    /**
     * X-definition XML element factory
     */
    final private XdNodeFactory xdFactory;

    final Set<String> processedTopDeclarations = new HashSet<String>();

    public XdDeclarationFactory(XmlSchema schema, XdNodeFactory xdFactory) {
        this.schema = schema;
        this.xdFactory = xdFactory;
    }

    public void createDeclaration(final XdDeclarationBuilder builder) {
        SchemaLogger.printP(LOG_INFO, TRANSFORMATION, builder.simpleType, "Creating declaration ...");
        final Element xdDeclaration = xdFactory.createEmptyDeclaration();
        xdDeclaration.setTextContent(builder.build());
        builder.parentNode.appendChild(xdDeclaration);
    }

    public String createDeclarationContent(final XdDeclarationBuilder builder) {
        SchemaLogger.printP(LOG_INFO, TRANSFORMATION, builder.simpleType, "Creating declaration content ...");
        return builder.build();
    }

    public String createSimpleTextDeclaration(final QName baseType) {
        final EmptyTypeFactory emptyTypeFactory = new EmptyTypeFactory(baseType.getLocalPart());
        emptyTypeFactory.setMode(IDeclarationTypeFactory.Mode.TEXT_DECL);
        return emptyTypeFactory.build("");
    }

    public XdDeclarationBuilder createBuilder() {
        return new XdDeclarationBuilder().init(schema, xdFactory, this);
    }

    boolean canBeProcessed(final String name) {
        return processedTopDeclarations.add(name);
    }
}
