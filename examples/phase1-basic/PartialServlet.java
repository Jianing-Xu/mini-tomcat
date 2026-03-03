package examples.phase1basic;

import com.xujn.minitomcat.connector.HttpRequest;
import com.xujn.minitomcat.connector.HttpResponse;
import com.xujn.minitomcat.servlet.HttpServlet;
import com.xujn.minitomcat.servlet.ServletException;
import java.nio.charset.StandardCharsets;

/**
 * Commits a response and then throws to verify committed responses are preserved.
 */
public class PartialServlet extends HttpServlet {

    @Override
    protected void doGet(HttpRequest request, HttpResponse response) throws ServletException {
        response.write("partial body".getBytes(StandardCharsets.UTF_8));
        response.flushBuffer();
        throw new ServletException("boom after commit");
    }
}
