package examples.phase1basic;

import com.xujn.minitomcat.bootstrap.Bootstrap;
import com.xujn.minitomcat.deploy.DeploymentException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Verifies that conflicting servlet mappings fail during startup.
 */
public final class Phase1ConflictCheckMain {

    private Phase1ConflictCheckMain() {
    }

    public static void main(String[] args) throws IOException {
        Path baseConfig = Path.of("conf/phase1-basic/server.properties").toAbsolutePath().normalize();
        Path conflictWebXml = Path.of("conf/phase1-basic/conflict-web.xml").toAbsolutePath().normalize();
        Path tempProperties = Files.createTempFile("mini-tomcat-conflict-", ".properties");
        List<String> lines = Files.readAllLines(baseConfig);
        lines = lines.stream()
                .map(line -> line.startsWith("server.webXml=") ? "server.webXml=" + conflictWebXml : line)
                .toList();
        Files.write(tempProperties, lines);
        try {
            Bootstrap.start(tempProperties);
            throw new IllegalStateException("Expected deployment conflict but startup succeeded");
        } catch (DeploymentException ex) {
            System.out.println(ex.getMessage());
        } finally {
            Files.deleteIfExists(tempProperties);
        }
    }
}
