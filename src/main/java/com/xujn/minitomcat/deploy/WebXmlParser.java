package com.xujn.minitomcat.deploy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Parses the Phase 1 subset of web.xml into immutable deployment metadata.
 *
 * <p>Key constraint: only servlet declarations, init params, and servlet mappings are supported.
 * Thread safety assumption: used during startup before the container becomes concurrent.</p>
 */
public class WebXmlParser {

    public WebAppDefinition parse(Path webXmlPath) {
        if (!Files.exists(webXmlPath)) {
            throw new DeploymentException("web.xml not found at " + webXmlPath);
        }

        Document document = parseDocument(webXmlPath);
        Map<String, MutableServletDefinition> definitions = parseServletDefinitions(document);
        parseServletMappings(document, definitions);

        List<ServletDefinition> servletDefinitions = new ArrayList<>();
        for (MutableServletDefinition definition : definitions.values()) {
            if (definition.urlPatterns.isEmpty()) {
                throw new DeploymentException("Servlet " + definition.servletName + " has no servlet-mapping");
            }
            servletDefinitions.add(definition.toImmutable());
        }
        return new WebAppDefinition(List.copyOf(servletDefinitions));
    }

    private Document parseDocument(Path webXmlPath) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setExpandEntityReferences(false);
            return factory.newDocumentBuilder().parse(Files.newInputStream(webXmlPath));
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new DeploymentException("Failed to parse web.xml at " + webXmlPath + ": " + ex.getMessage(), ex);
        }
    }

    private Map<String, MutableServletDefinition> parseServletDefinitions(Document document) {
        Map<String, MutableServletDefinition> definitions = new LinkedHashMap<>();
        NodeList servletNodes = document.getElementsByTagName("servlet");
        for (int i = 0; i < servletNodes.getLength(); i++) {
            Element servletElement = asElement(servletNodes.item(i));
            String servletName = requiredText(servletElement, "servlet-name");
            String servletClass = requiredText(servletElement, "servlet-class");
            if (definitions.containsKey(servletName)) {
                throw new DeploymentException("Duplicate servlet definition detected for servlet=" + servletName);
            }

            Map<String, String> initParameters = new LinkedHashMap<>();
            NodeList initParamNodes = servletElement.getElementsByTagName("init-param");
            for (int j = 0; j < initParamNodes.getLength(); j++) {
                Element initParamElement = asElement(initParamNodes.item(j));
                initParameters.put(
                        requiredText(initParamElement, "param-name"),
                        requiredText(initParamElement, "param-value")
                );
            }
            definitions.put(servletName, new MutableServletDefinition(servletName, servletClass, initParameters));
        }
        return definitions;
    }

    private void parseServletMappings(Document document, Map<String, MutableServletDefinition> definitions) {
        NodeList mappingNodes = document.getElementsByTagName("servlet-mapping");
        for (int i = 0; i < mappingNodes.getLength(); i++) {
            Element mappingElement = asElement(mappingNodes.item(i));
            String servletName = requiredText(mappingElement, "servlet-name");
            MutableServletDefinition definition = definitions.get(servletName);
            if (definition == null) {
                throw new DeploymentException("Servlet mapping references unknown servlet=" + servletName);
            }
            String urlPattern = requiredText(mappingElement, "url-pattern");
            if (urlPattern.isBlank()) {
                throw new DeploymentException("Servlet mapping for servlet=" + servletName + " has empty url-pattern");
            }
            definition.urlPatterns.add(urlPattern.strip());
        }
    }

    private Element asElement(Node node) {
        return (Element) node;
    }

    private String requiredText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            throw new DeploymentException("Missing required element <" + tagName + "> in web.xml");
        }
        String value = nodes.item(0).getTextContent();
        if (value == null || value.isBlank()) {
            throw new DeploymentException("Element <" + tagName + "> must not be blank in web.xml");
        }
        return value.strip();
    }

    private static final class MutableServletDefinition {
        private final String servletName;
        private final String servletClass;
        private final Map<String, String> initParameters;
        private final List<String> urlPatterns = new ArrayList<>();

        private MutableServletDefinition(String servletName, String servletClass, Map<String, String> initParameters) {
            this.servletName = servletName;
            this.servletClass = servletClass;
            this.initParameters = new LinkedHashMap<>(initParameters);
        }

        private ServletDefinition toImmutable() {
            return new ServletDefinition(
                    servletName,
                    servletClass,
                    Map.copyOf(initParameters),
                    List.copyOf(urlPatterns)
            );
        }
    }
}
