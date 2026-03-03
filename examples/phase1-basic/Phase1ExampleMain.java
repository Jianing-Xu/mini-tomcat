package examples.phase1basic;

import com.xujn.minitomcat.bootstrap.Bootstrap;
import com.xujn.minitomcat.bootstrap.MiniTomcat;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

/**
 * Starts the Phase 1 example server used by manual acceptance checks.
 */
public final class Phase1ExampleMain {

    private Phase1ExampleMain() {
    }

    public static void main(String[] args) throws InterruptedException {
        Path serverProperties = Path.of("conf/phase1-basic/server.properties");
        MiniTomcat miniTomcat = Bootstrap.start(serverProperties);
        Runtime.getRuntime().addShutdownHook(new Thread(miniTomcat::stop, "phase1-example-shutdown"));
        System.out.println("Phase 1 example started on http://localhost:" + miniTomcat.getServerConfig().port()
                + miniTomcat.getServerConfig().contextPath());
        new CountDownLatch(1).await();
    }
}
