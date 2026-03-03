package com.xujn.minitomcat.connector.bio;

import com.xujn.minitomcat.connector.Connector;
import com.xujn.minitomcat.connector.HttpRequest;
import com.xujn.minitomcat.connector.HttpResponse;
import com.xujn.minitomcat.connector.ProtocolHandler;
import com.xujn.minitomcat.support.ExceptionUtils;
import com.xujn.minitomcat.support.http.HttpStatus;
import java.io.IOException;
import java.net.Socket;

/**
 * Processes one accepted BIO socket from parse to container dispatch to response flush.
 *
 * <p>Thread safety assumption: one processor exclusively owns its socket and request state.</p>
 */
public class SocketProcessor implements Runnable {

    private final Connector connector;
    private final ProtocolHandler protocolHandler;
    private final Socket socket;

    public SocketProcessor(Connector connector, ProtocolHandler protocolHandler, Socket socket) {
        this.connector = connector;
        this.protocolHandler = protocolHandler;
        this.socket = socket;
    }

    @Override
    public void run() {
        HttpRequest request = null;
        HttpResponse response = null;
        try (socket) {
            request = protocolHandler.parseRequest(socket);
            if (request == null) {
                return;
            }
            response = protocolHandler.createResponse(socket);
            connector.handle(request, response);
            if (!response.isCommitted()) {
                response.flushBuffer();
            }
        } catch (Exception ex) {
            handleFailure(request, response, ex);
        }
    }

    private void handleFailure(HttpRequest request, HttpResponse response, Exception ex) {
        try {
            if (response == null && !socket.isClosed()) {
                response = protocolHandler.createResponse(socket);
            }
            if (response != null && !response.isCommitted()) {
                String requestUri = request == null ? "<unparsed>" : request.getRequestUri();
                String host = request == null ? "<unknown>" : request.getHost();
                response.sendError(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Request handling failed for host=" + host
                                + " requestUri=" + requestUri
                                + " cause=" + ExceptionUtils.rootCauseMessage(ex));
                return;
            }
        } catch (IOException ignored) {
            // fall through to stderr logging when the socket is already unusable
        }
        String requestUri = request == null ? "<unparsed>" : request.getRequestUri();
        String host = request == null ? "<unknown>" : request.getHost();
        System.err.println("Committed response preserved for host=" + host
                + " requestUri=" + requestUri
                + " cause=" + ExceptionUtils.rootCauseMessage(ex));
    }
}
