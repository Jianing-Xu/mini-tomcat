package examples.phase1basic;

import com.xujn.minitomcat.connector.HttpRequest;
import com.xujn.minitomcat.connector.HttpResponse;
import com.xujn.minitomcat.servlet.HttpServlet;
import com.xujn.minitomcat.servlet.ServletException;

/**
 * Forces the container down the 500 error path before the response is committed.
 */
public class ErrorServlet extends HttpServlet {

    @Override
    protected void doGet(HttpRequest request, HttpResponse response) throws ServletException {
        throw new ServletException("boom");
    }
}
