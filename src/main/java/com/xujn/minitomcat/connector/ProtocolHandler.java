package com.xujn.minitomcat.connector;

import com.xujn.minitomcat.bootstrap.Lifecycle;
import java.io.IOException;
import java.net.Socket;

/**
 * Parses a socket into request and response abstractions.
 */
public interface ProtocolHandler extends Lifecycle {

    HttpRequest parseRequest(Socket socket) throws IOException;

    HttpResponse createResponse(Socket socket) throws IOException;
}
