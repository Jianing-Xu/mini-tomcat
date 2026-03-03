package examples.phase1basic;

import com.xujn.minitomcat.connector.HttpRequest;
import com.xujn.minitomcat.connector.HttpResponse;
import com.xujn.minitomcat.servlet.HttpServlet;
import java.nio.charset.StandardCharsets;

/**
 * Emits lifecycle logs so shutdown behavior can be checked without extra tooling.
 */
public class InitProbeServlet extends HttpServlet {

    @Override
    public void init() {
        System.out.println("InitProbeServlet init");
    }

    @Override
    protected void doGet(HttpRequest request, HttpResponse response) {
        System.out.println("InitProbeServlet service " + request.getRequestUri());
        response.write("init-probe".getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void destroy() {
        System.out.println("InitProbeServlet destroy");
    }
}
