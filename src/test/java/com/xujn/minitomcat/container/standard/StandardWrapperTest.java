package com.xujn.minitomcat.container.standard;

import com.xujn.minitomcat.connector.HttpRequest;
import com.xujn.minitomcat.connector.HttpResponse;
import com.xujn.minitomcat.deploy.ServletDefinition;
import com.xujn.minitomcat.servlet.GenericServlet;
import com.xujn.minitomcat.servlet.ServletException;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandardWrapperTest {

    @Test
    void initRunsOnlyOnceAndDestroyOnlyAfterInitialization() {
        TrackingServlet.reset();
        StandardWrapper wrapper = new StandardWrapper(
                new ServletDefinition(TrackingServlet.class.getSimpleName(), TrackingServlet.class.getName(), Map.of(), List.of("/demo")),
                new StandardServletContext("/app")
        );

        wrapper.allocate();
        wrapper.allocate();
        wrapper.destroy();

        assertEquals(1, TrackingServlet.initCount);
        assertEquals(1, TrackingServlet.destroyCount);
    }

    @Test
    void wrapperTurnsUnhandledServletFailureInto500WhenUncommitted() {
        StandardWrapper wrapper = new StandardWrapper(
                new ServletDefinition(FailingServlet.class.getSimpleName(), FailingServlet.class.getName(), Map.of(), List.of("/error")),
                new StandardServletContext("/app")
        );
        HttpRequest request = new HttpRequest("GET", "/app/error", "HTTP/1.1", "localhost", Map.of(), Map.of(), new byte[0]);
        request.setContextPath("/app");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        HttpResponse response = new HttpResponse(output);

        wrapper.invoke(request, response);

        assertTrue(response.isCommitted());
        assertEquals(500, response.getStatus());
    }

    @Test
    void committedResponseIsPreservedAfterServletFailure() {
        StandardWrapper wrapper = new StandardWrapper(
                new ServletDefinition(CommittedThenFailServlet.class.getSimpleName(), CommittedThenFailServlet.class.getName(), Map.of(), List.of("/partial")),
                new StandardServletContext("/app")
        );
        HttpRequest request = new HttpRequest("GET", "/app/partial", "HTTP/1.1", "localhost", Map.of(), Map.of(), new byte[0]);
        request.setContextPath("/app");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        HttpResponse response = new HttpResponse(output);

        wrapper.invoke(request, response);

        assertTrue(response.isCommitted());
        String payload = output.toString(StandardCharsets.UTF_8);
        assertTrue(payload.contains("partial body"));
        assertFalse(payload.contains("Servlet invocation failed"));
    }

    public static class TrackingServlet extends GenericServlet {
        static int initCount;
        static int destroyCount;

        static void reset() {
            initCount = 0;
            destroyCount = 0;
        }

        @Override
        public void init() {
            initCount++;
        }

        @Override
        public void service(HttpRequest request, HttpResponse response) {
        }

        @Override
        public void destroy() {
            destroyCount++;
        }
    }

    public static class FailingServlet extends GenericServlet {
        @Override
        public void service(HttpRequest request, HttpResponse response) throws ServletException {
            throw new ServletException("boom");
        }
    }

    public static class CommittedThenFailServlet extends GenericServlet {
        @Override
        public void service(HttpRequest request, HttpResponse response) throws ServletException {
            response.write("partial body".getBytes(StandardCharsets.UTF_8));
            response.flushBuffer();
            throw new ServletException("boom after commit");
        }
    }
}
