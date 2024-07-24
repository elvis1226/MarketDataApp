package org.dgf.network;

import org.dgf.core.Message;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public interface WSSClient {
    void connect();
    void close();

    static WSSClient custom(String host, List<String> pairs, BlockingQueue<Message> queue) {
        return new DefaultWebSocketClient(host, pairs, queue);
    }
}
