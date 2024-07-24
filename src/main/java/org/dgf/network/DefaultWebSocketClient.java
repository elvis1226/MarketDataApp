package org.dgf.network;

import com.google.common.base.Preconditions;
import org.dgf.core.Message;
import org.eclipse.jetty.client.HttpClient;

import org.eclipse.jetty.client.HttpProxy;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.jetty.websocket.api.Session;

import java.net.URI;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;

public class DefaultWebSocketClient implements WSSClient {
    private static final Logger logger = LoggerFactory.getLogger(DefaultWebSocketClient.class);
    private WebSocketClient webSocketClient;
    private Session currentSession;
    private final String wssHost;
    private List<String> currencyPairs;
    private final BlockingQueue queue;

    public DefaultWebSocketClient(String wssHost, List<String> currencyPairs, BlockingQueue<Message> queue) {
        Preconditions.checkArgument(!wssHost.isEmpty(), "Empty web socket host url");
        Preconditions.checkArgument(currencyPairs.size() > 0, "empty currency pairs");
        this.wssHost = wssHost;
        this.currencyPairs = currencyPairs;
        this.queue = queue;
    }

    public void connect()
    {
        logger.info("Connecting...");
        try {
            // Use a standard, HTTP/1.1, HttpClient.
            HttpClient httpClient = new HttpClient();

            // Create and start WebSocketClient.
            this.webSocketClient = new WebSocketClient(httpClient);
            this.webSocketClient.start();

            // The client-side WebSocket EndPoint that
            // receives WebSocket messages from the server.
            ClientEndPoint clientEndPoint = new ClientEndPoint(this.currencyPairs, this.queue);
            // The server URI to connect to.
            URI serverURI = URI.create(this.wssHost);

            // Connect the client EndPoint to the server.
            CompletableFuture<Session> clientSessionPromise = webSocketClient.connect(clientEndPoint, serverURI);

            this.currentSession = clientSessionPromise.get();
        }
        catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        logger.info("Connected...");
    }

    public void close() {
        logger.info("Closing...");
        if (this.currentSession != null) {
            this.currentSession.close();
        }

        if (this.webSocketClient != null) {
            try {
                this.webSocketClient.stop();
            } catch (Exception e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }
        }
        logger.info("Closed...");
    }
}
