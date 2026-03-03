package com.xujn.minitomcat.connector;

import com.xujn.minitomcat.support.exception.ResponseCommittedException;
import com.xujn.minitomcat.support.http.HttpHeaders;
import com.xujn.minitomcat.support.http.HttpStatus;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Minimal HTTP response abstraction that buffers output until flush or sendError commits it.
 *
 * <p>Key constraint: once committed, status and body can no longer be overwritten.
 * Thread safety assumption: a response instance is confined to one worker thread.</p>
 */
public class HttpResponse {

    private final OutputStream outputStream;
    private final ByteArrayOutputStream bodyBuffer = new ByteArrayOutputStream();
    private final Map<String, String> headers = new LinkedHashMap<>();
    private int status = HttpStatus.OK;
    private boolean committed;

    public HttpResponse(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        assertNotCommitted("Cannot change status after response commit");
        this.status = status;
    }

    public void setHeader(String name, String value) {
        assertNotCommitted("Cannot change headers after response commit");
        headers.put(name, value);
    }

    public Map<String, String> getHeaders() {
        return Map.copyOf(headers);
    }

    public void write(byte[] body) {
        assertNotCommitted("Cannot write after response commit");
        try {
            bodyBuffer.write(body);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to buffer response body", ex);
        }
    }

    public byte[] getBodyBytes() {
        return bodyBuffer.toByteArray();
    }

    public boolean isCommitted() {
        return committed;
    }

    public void flushBuffer() {
        if (committed) {
            return;
        }
        try {
            byte[] body = bodyBuffer.toByteArray();
            if (!headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
                headers.put(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8");
            }
            headers.put(HttpHeaders.CONTENT_LENGTH, Integer.toString(body.length));
            headers.put(HttpHeaders.CONNECTION, "close");
            outputStream.write(buildStatusLine().getBytes(StandardCharsets.UTF_8));
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                outputStream.write((entry.getKey() + ": " + entry.getValue() + "\r\n").getBytes(StandardCharsets.UTF_8));
            }
            outputStream.write("\r\n".getBytes(StandardCharsets.UTF_8));
            outputStream.write(body);
            outputStream.flush();
            committed = true;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to flush HTTP response", ex);
        }
    }

    public void sendError(int status, String message) {
        assertNotCommitted("Cannot send error after response commit");
        this.status = status;
        bodyBuffer.reset();
        byte[] body = message.getBytes(StandardCharsets.UTF_8);
        write(body);
        flushBuffer();
    }

    private String buildStatusLine() {
        return "HTTP/1.1 " + status + " " + HttpStatus.reasonPhrase(status) + "\r\n";
    }

    private void assertNotCommitted(String message) {
        if (committed) {
            throw new ResponseCommittedException(message);
        }
    }
}
