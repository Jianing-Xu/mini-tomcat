package examples.phase1basic;

import com.xujn.minitomcat.connector.HttpRequest;
import com.xujn.minitomcat.connector.HttpResponse;
import com.xujn.minitomcat.servlet.HttpServlet;
import com.xujn.minitomcat.servlet.ServletException;
import java.nio.charset.StandardCharsets;

/**
 * Returns a stable 200 response for the basic acceptance path.
 */
public class DemoServlet extends HttpServlet {

    @Override
    public void init() {
        System.out.println("DemoServlet init");
    }

    @Override
    protected void doGet(HttpRequest request, HttpResponse response) throws ServletException {
        System.out.println("DemoServlet service " + request.getRequestUri());
        response.write("mini-tomcat demo ok".getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void destroy() {
        System.out.println("DemoServlet destroy");
    }
}
