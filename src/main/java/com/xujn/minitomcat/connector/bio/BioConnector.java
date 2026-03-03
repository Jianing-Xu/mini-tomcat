package com.xujn.minitomcat.connector.bio;

import com.xujn.minitomcat.bootstrap.LifecycleBase;
import com.xujn.minitomcat.connector.Connector;
import com.xujn.minitomcat.connector.HttpRequest;
import com.xujn.minitomcat.connector.HttpResponse;
import com.xujn.minitomcat.connector.ProtocolHandler;
import com.xujn.minitomcat.container.Container;
import com.xujn.minitomcat.support.thread.NamedThreadFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SIMPLE_BIO connector backed by ServerSocket and a fixed worker pool.
 *
 * <p>Key constraint: one connection carries one request and is closed afterward.
 * Thread safety assumption: the container reference is fixed before start; request handling uses worker threads.</p>
 */
public class BioConnector extends LifecycleBase implements Connector {

    private final BioConnectorConfig config;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ProtocolHandler protocolHandler = new BioProtocolHandler();
    private volatile Container container;
    private volatile ServerSocket serverSocket;
    private volatile ExecutorService workerPool;
    private volatile Thread acceptorThread;

    public BioConnector(BioConnectorConfig config) {
        this.config = config;
    }

    @Override
    public void setContainer(Container container) {
        if (this.container != null && this.container != container) {
            throw new IllegalStateException("Connector container is already set");
        }
        this.container = container;
    }

    @Override
    public Container getContainer() {
        return container;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) {
        if (container == null) {
            throw new IllegalStateException("Connector has no container");
        }
        container.invoke(request, response);
    }

    @Override
    protected void startInternal() {
        if (container == null) {
            throw new IllegalStateException("Connector requires a container before start");
        }
        try {
            protocolHandler.start();
            serverSocket = new ServerSocket(config.port(), config.backlog());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to bind SIMPLE_BIO connector on port=" + config.port(), ex);
        }
        workerPool = Executors.newFixedThreadPool(config.workerThreads(), new NamedThreadFactory("mini-tomcat-worker"));
        running.set(true);
        acceptorThread = new Thread(this::acceptLoop, "mini-tomcat-acceptor");
        acceptorThread.setDaemon(true);
        acceptorThread.start();
    }

    @Override
    protected void stopInternal() {
        running.set(false);
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {
                // close failure during shutdown does not change stop semantics
            }
        }
        if (workerPool != null) {
            workerPool.shutdownNow();
        }
        protocolHandler.stop();
    }

    private void acceptLoop() {
        while (running.get()) {
            try {
                Socket socket = serverSocket.accept();
                workerPool.submit(new SocketProcessor(this, protocolHandler, socket));
            } catch (SocketException ex) {
                if (running.get()) {
                    throw new IllegalStateException("BIO accept loop failed on port=" + config.port(), ex);
                }
                return;
            } catch (IOException ex) {
                throw new IllegalStateException("BIO accept loop failed on port=" + config.port(), ex);
            }
        }
    }
}
