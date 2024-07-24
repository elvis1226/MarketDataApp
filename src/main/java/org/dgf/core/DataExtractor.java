package org.dgf.core;
import org.dgf.network.WSSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class DataExtractor implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(DataExtractor.class);

    private WSSClient client;
    private volatile boolean isDone = false;
    private final BlockingQueue<Message> queue;

    public DataExtractor(String host, List<String> pairs, BlockingQueue<Message> queue) {
        this.client = WSSClient.custom(host, pairs, queue);
        this.queue = queue;
    }

    @Override
    public void run() {

        this.client.connect();

        while (!isDone) {
            try {
                Thread.sleep(500);
            }
            catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void complete() {
        this.isDone = true;
    }
}
