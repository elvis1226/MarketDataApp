package org.dgf.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.dgf.core.DataProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class KafkaProducerClient {
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerClient.class);

    private Producer<String,String> producer;
    private static final String TOPIC_NAME = "market";

    public KafkaProducerClient() {
        this.producer = new KafkaProducer<String,String>(this.getSetting());
    }

    public Properties getSetting() {
        Properties props = new Properties();
        //Assign localhost id
        props.put("bootstrap.servers", "localhost:9092");
        //Set acknowledgements for producer requests.
        props.put("acks", "all");
        //If the request fails, the producer can automatically retry,
        props.put("retries", 0);

        //Specify buffer size in config
        props.put("batch.size", 16384);

        //Reduce the no of requests less than 0
        props.put("linger.ms", 1);

        //The buffer.memory controls the total amount of memory available to the producer for buffering.
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return props;
    }

    public void send(String key, String value) {
        if (key.isEmpty() || value.isEmpty()) {
            return;
        }
        try {
            ProducerRecord record = new ProducerRecord<String, String>(TOPIC_NAME, key, value);
            this.producer.send(record);
        }
        catch (Exception e) {
            logger.warn("Failed to send to kafka {} : {}, error {}", key, value, e.getMessage());
            e.printStackTrace();
        }

    }

    public void close() {
        if (this.producer != null) {
            this.producer.close();
        }
    }

    public static void main(String[] args) throws Exception{

        KafkaProducerClient client = new KafkaProducerClient();

        for(int i = 0; i < 10; i++)
            client.send( Integer.toString(i), Integer.toString(i));
        System.out.println("Message sent successfully");
        client.close();
    }
}
