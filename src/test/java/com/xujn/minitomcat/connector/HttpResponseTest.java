package com.xujn.minitomcat.connector;

import com.xujn.minitomcat.support.exception.ResponseCommittedException;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpResponseTest {

    @Test
    void flushMarksResponseCommitted() {
        HttpResponse response = new HttpResponse(new ByteArrayOutputStream());
        response.write("ok".getBytes(StandardCharsets.UTF_8));
        response.flushBuffer();
        assertTrue(response.isCommitted());
    }

    @Test
    void sendErrorBuildsCommittedErrorResponse() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        HttpResponse response = new HttpResponse(output);
        response.sendError(500, "boom");
        assertEquals(500, response.getStatus());
        assertTrue(response.isCommitted());
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("boom"));
    }

    @Test
    void sendErrorAfterCommitFails() {
        HttpResponse response = new HttpResponse(new ByteArrayOutputStream());
        response.write("ok".getBytes(StandardCharsets.UTF_8));
        response.flushBuffer();
        assertThrows(ResponseCommittedException.class, () -> response.sendError(500, "boom"));
    }
}
