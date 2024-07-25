package org.dgf.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Properties;

public class KafkaConsumerClient implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerClient.class);
    private static final String TOPIC_NAME = "market";
    private KafkaConsumer<String, String> consumer;
    private volatile boolean isDone = false;

    public KafkaConsumerClient () {
        this.consumer = new KafkaConsumer<String, String>(getSetting());
    }

    @Override
    public void run() {
        try {
            this.subscribe();
        }
        catch (Exception e) {
            logger.warn("{}", e.getMessage());
        }
        finally {
            this.close();
        }
    }

    public Properties getSetting() {
        Properties props = new Properties();

        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "test");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        return props;
    }

    public void subscribe() {
        try {
            this.consumer.subscribe(Arrays.asList(TOPIC_NAME));

            while (!isDone) {
                ConsumerRecords<String, String> records = consumer.poll(100);
                for (ConsumerRecord<String, String> record : records) {
                    // print the offset,key and value for the consumer records.
                    logger.info("Kafka message offset = {}, key = {}, value = {}", record.offset(), record.key(), record.value());
                }
                Thread.sleep(100);
            }
        }
        catch (Exception e) {
            logger.warn ("Failed on pooling kafka message with error {}", e.getMessage());
            e.printStackTrace();
        }
    }

    public void close() {
        this.isDone = true;
        if (this.consumer != null) {
            this.consumer.close();
        }
    }

    public static void main(String[] args) throws Exception {
        KafkaConsumerClient consumer = new KafkaConsumerClient();
        consumer.subscribe();
        Thread.sleep(1000);
        consumer.close();
    }

}
