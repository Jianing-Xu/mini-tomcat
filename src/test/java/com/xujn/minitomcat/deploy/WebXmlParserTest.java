package com.xujn.minitomcat.deploy;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WebXmlParserTest {

    private final WebXmlParser parser = new WebXmlParser();

    @Test
    void parsesServletDefinitions() throws Exception {
        Path webXml = Files.createTempFile("web", ".xml");
        Files.writeString(webXml, """
                <web-app>
                    <servlet>
                        <servlet-name>demo</servlet-name>
                        <servlet-class>examples.phase1basic.DemoServlet</servlet-class>
                    </servlet>
                    <servlet-mapping>
                        <servlet-name>demo</servlet-name>
                        <url-pattern>/demo</url-pattern>
                    </servlet-mapping>
                </web-app>
                """);
        WebAppDefinition definition = parser.parse(webXml);
        assertEquals(1, definition.getServletDefinitions().size());
        assertEquals("/demo", definition.getServletDefinitions().get(0).urlPatterns().get(0));
    }

    @Test
    void rejectsUnknownServletMapping() throws Exception {
        Path webXml = Files.createTempFile("web", ".xml");
        Files.writeString(webXml, """
                <web-app>
                    <servlet-mapping>
                        <servlet-name>missing</servlet-name>
                        <url-pattern>/demo</url-pattern>
                    </servlet-mapping>
                </web-app>
                """);
        assertThrows(DeploymentException.class, () -> parser.parse(webXml));
    }

    @Test
    void rejectsBlankPattern() throws Exception {
        Path webXml = Files.createTempFile("web", ".xml");
        Files.writeString(webXml, """
                <web-app>
                    <servlet>
                        <servlet-name>demo</servlet-name>
                        <servlet-class>examples.phase1basic.DemoServlet</servlet-class>
                    </servlet>
                    <servlet-mapping>
                        <servlet-name>demo</servlet-name>
                        <url-pattern> </url-pattern>
                    </servlet-mapping>
                </web-app>
                """);
        assertThrows(DeploymentException.class, () -> parser.parse(webXml));
    }
}
