package com.mcd;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class KafkaProducerTest {
    private static final String TOPIC_NAME = "raw_STLD_restaurant_transaction";
    @Test
    public void testProduceMessage() {
        // Define the Kafka producer properties
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "pkc-921jm.us-east-2.aws.confluent.cloud:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put("security.protocol", "SASL_SSL");
        props.put("sasl.mechanism", "PLAIN");
        props.put("sasl.jaas.config",
                "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"HIR4OONLSRQIWE3Z\" password=\"1vP9TPvXTKrDWmGBgUl6BRCI2exm5+4K99RPs/ElmTebgzuHJ4WNbsDeOzsukZPu\";");
        props.put(ProducerConfig.ACKS_CONFIG, "all"); // Ensure all brokers acknowledge the message
        props.put(ProducerConfig.RETRIES_CONFIG, 3); // Retry on failure
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1); // Reduce latency
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 6000); // Increase timeout for debugging

        // Create the Kafka producer
        Producer<String, String> producer = new KafkaProducer<>(props);

        // Create the Order object
        Order order = new Order();
        order.ordertime = 1497014222380L;
        order.orderid = 4;
        order.itemid = "Item_184";
        Address address = new Address();
        address.city = "Mountain View";
        address.state = "CA";
        address.zipcode = 94041;
        order.address = address;

        // Convert the Order object to a JSON string
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = "";
        try {
            jsonString = objectMapper.writeValueAsString(order);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        // Send the JSON string to Kafka and wait for acknowledgement
        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, "key-1", jsonString);
        try {
            Future<RecordMetadata> future = producer.send(record);
            RecordMetadata metadata = future.get(); // Wait for the message to be sent and acknowledged
            assertNotNull(metadata);
            System.out.println("Message sent to topic " + metadata.topic() + " partition " + metadata.partition() + " offset " + metadata.offset());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            // Close the producer
            producer.close();
        }
    }

    // Order class definition
    public static class Order {
        public long ordertime;
        public int orderid;
        public String itemid;
        public Address address;
    }

    // Address class definition
    public static class Address {
        public String city;
        public String state;
        public int zipcode;
    }
}