package com.xujn.minitomcat.connector.bio;

import com.xujn.minitomcat.bootstrap.LifecycleBase;
import com.xujn.minitomcat.connector.HttpRequest;
import com.xujn.minitomcat.connector.HttpResponse;
import com.xujn.minitomcat.connector.ProtocolHandler;
import com.xujn.minitomcat.connector.RequestLine;
import com.xujn.minitomcat.support.http.HttpHeaders;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses the Phase 1 HTTP/1.1 subset for the BIO connector.
 *
 * <p>Key constraint: supports single request per connection with Content-Length bodies only.
 * Thread safety assumption: each socket is parsed by one worker thread.</p>
 */
public class BioProtocolHandler extends LifecycleBase implements ProtocolHandler {

    @Override
    public HttpRequest parseRequest(Socket socket) throws IOException {
        InputStream inputStream = socket.getInputStream();
        String requestLineText = readAsciiLine(inputStream);
        while (requestLineText != null && requestLineText.isBlank()) {
            requestLineText = readAsciiLine(inputStream);
        }
        if (requestLineText == null || requestLineText.isBlank()) {
            return null;
        }

        RequestLine requestLine = parseRequestLine(requestLineText);
        Map<String, String> headers = parseHeaders(inputStream);
        byte[] body = parseBody(inputStream, headers);
        String rawTarget = requestLine.requestTarget();
        int queryIndex = rawTarget.indexOf('?');
        String requestUri = queryIndex >= 0 ? rawTarget.substring(0, queryIndex) : rawTarget;
        Map<String, List<String>> parameters = queryIndex >= 0
                ? parseQueryString(rawTarget.substring(queryIndex + 1))
                : Map.of();
        String host = headers.get(HttpHeaders.HOST);
        return new HttpRequest(
                requestLine.method(),
                requestUri,
                requestLine.protocol(),
                host,
                headers,
                parameters,
                body
        );
    }

    @Override
    public HttpResponse createResponse(Socket socket) throws IOException {
        return new HttpResponse(socket.getOutputStream());
    }

    private RequestLine parseRequestLine(String line) {
        String[] parts = line.split(" ");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Malformed request line: " + line);
        }
        return new RequestLine(parts[0], parts[1], parts[2]);
    }

    private Map<String, String> parseHeaders(InputStream inputStream) throws IOException {
        Map<String, String> headers = new LinkedHashMap<>();
        while (true) {
            String line = readAsciiLine(inputStream);
            if (line == null || line.isEmpty()) {
                return headers;
            }
            int separator = line.indexOf(':');
            if (separator <= 0) {
                throw new IllegalArgumentException("Malformed header line: " + line);
            }
            String name = line.substring(0, separator).trim();
            String value = line.substring(separator + 1).trim();
            headers.put(name, value);
        }
    }

    private byte[] parseBody(InputStream inputStream, Map<String, String> headers) throws IOException {
        String contentLengthValue = headers.get("Content-Length");
        if (contentLengthValue == null) {
            return new byte[0];
        }
        int contentLength = Integer.parseInt(contentLengthValue);
        byte[] body = inputStream.readNBytes(contentLength);
        if (body.length != contentLength) {
            throw new IllegalArgumentException("Expected Content-Length=" + contentLength + " but got " + body.length);
        }
        return body;
    }

    private Map<String, List<String>> parseQueryString(String queryString) {
        Map<String, List<String>> parameters = new LinkedHashMap<>();
        if (queryString.isBlank()) {
            return parameters;
        }
        for (String pair : queryString.split("&")) {
            String[] keyValue = pair.split("=", 2);
            String key = decode(keyValue[0]);
            String value = keyValue.length == 2 ? decode(keyValue[1]) : "";
            parameters.computeIfAbsent(key, ignored -> new ArrayList<>()).add(value);
        }
        return parameters;
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private String readAsciiLine(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        while (true) {
            int current = inputStream.read();
            if (current == -1) {
                return buffer.size() == 0 ? null : buffer.toString(StandardCharsets.UTF_8);
            }
            if (current == '\r') {
                int next = inputStream.read();
                if (next == '\n') {
                    return buffer.toString(StandardCharsets.UTF_8);
                }
                if (next != -1) {
                    buffer.write(next);
                }
                return buffer.toString(StandardCharsets.UTF_8);
            }
            if (current == '\n') {
                return buffer.toString(StandardCharsets.UTF_8);
            }
            buffer.write(current);
        }
    }
}
