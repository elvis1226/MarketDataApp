package org.dgf.network;

import com.google.common.collect.ImmutableList;
import org.dgf.core.Message;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ClientEndPoint implements Session.Listener
{
    private static final Logger logger = LoggerFactory.getLogger(ClientEndPoint.class);

    private Session session;
    private ClientStatus status;
    private List<String> currencyPairs;
    private final BlockingQueue<Message> queue;

    public ClientEndPoint(List<String> pairs, BlockingQueue<Message> queue)
    {
        this.currencyPairs = ImmutableList.copyOf(pairs);
        this.queue = queue;
    }

    @Override
    public void onWebSocketOpen(Session session)
    {
        this.session = session;
        logger.info("Connected with server");
        this.status = ClientStatus.CONNECTED;
        session.demand();
    }

    @Override
    public void onWebSocketText(String message)
    {
        String nextMessage = "";

        switch(this.status) {
            case CONNECTED:
                //send subscribe message
                this.status = ClientStatus.START_SUBSCRIBE;
                nextMessage = getSubscribeMessage();
                break;

            case START_SUBSCRIBE:
                this.status = ClientStatus.SUBSCRIBED;
                break;

            case SUBSCRIBED:
                this.status = ClientStatus.IN_PROCESS;
                break;
        }

        this.process(message);

        if (nextMessage.isEmpty()) {
            session.demand();
        }
        else {
            String finalNextMessage = nextMessage;
            session.sendText(nextMessage, Callback.from(session::demand,
                    failure ->
                    {
                        logger.warn("Failed on sendText : {}", finalNextMessage);
                        session.close(StatusCode.SERVER_ERROR, "failure", Callback.NOOP);
                    }));
        }
    }

    @Override
    public void onWebSocketError(Throwable cause)
    {
        // The WebSocket endpoint failed.
        // You may log the error.
        cause.printStackTrace();

        // You may dispose resources.
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason)
    {
        // The WebSocket endpoint has been closed.
        // You may dispose resources.
    }

    private boolean process(String message) {
        final String snapshotPrefix = "\"channel\":\"book\",\"type\":\"snapshot\"";
        final String updatePrefix = "\"channel\":\"book\",\"type\":\"update\",";

        if (message.isEmpty()) {
            logger.warn("Empty message");
            return false;
        }
        int retryTimes = 0;
        String messageType = "";

        if (message.indexOf(snapshotPrefix) != -1) {
            messageType = "snapshot";
        }
        else if (message.indexOf(updatePrefix) != -1) {
            messageType = "update";
        }

        if (!messageType.isEmpty()) {
            logger.debug("enqueue message ...");
            while (true && retryTimes < 3){
                try {
                    if (this.queue.offer(new Message(messageType, message), 2, TimeUnit.SECONDS)) {
                        int size = this.queue.size();
                        if (size > 0 &&  (size % 1000) == 0) {
                            logger.debug("{} message enqueued ...", this.queue.size());
                        }
                        break;
                    }
                    Thread.sleep(1000);
                    retryTimes++;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return true;
    }

    private String getSubscribeMessage() {
        String template = "{   \"method\": \"subscribe\"," +
                               "\"params\": " +
                                   "{" +
                                     "\"channel\": \"book\","+
                                     "\"snapshot\": true," +
                                     "\"symbol\": [ %s ]" +
                                    "}," +
                             "\"req_id\": 1234567890 }";

        String ccys = this.currencyPairs.stream().map(c-> "\"" + c + "\"").collect(Collectors.joining(","));
        String result = String.format(template, ccys);

        logger.info(result);

        return result;
    }

    private String getUnSubscribeMessage() {
        String template = "{" +
                "  \"method\": \"unsubscribe\"," +
                "  \"params\": {" +
                "    \"channel\": \"book\"," +
                "    \"symbol\": [ %s ]" +
                "  }," +
                "  \"req_id\": 1234567890" +
                "}";

        String ccys = this.currencyPairs.stream().map(c-> "\"" + c + "\"").collect(Collectors.joining(","));
        String result = String.format(template, ccys);

        logger.info(result);

        return result;
    }

    public enum ClientStatus {
        CONNECTED,
        START_SUBSCRIBE,
        SUBSCRIBED,
        IN_PROCESS,
        CLOSE
    }
}
