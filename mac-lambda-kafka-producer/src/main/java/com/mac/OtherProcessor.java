package com.mac;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.Future;

/**
 * Author: Vijay Krishna
 * Handle lambda test function events
 */
public class OtherProcessor {

    private static final String TOPIC_NAME = "mac_pos";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public String handleNormalEvent(String input, Context context) {
        context.getLogger().log("Input : " + input);
        context.getLogger().log("Input : " + input.getClass());
        context.getLogger().log("Context : " + context);
        Properties props;
        try {
            props = readConfig("client.properties");
        } catch (IOException e) {
            context.getLogger().log("Failed to load configuration: " + e.getMessage());
            return "Error loading configuration";
        }

        context.getLogger().log("Kafka Properties : " + props);
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        // Parse the input message
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Message is null or empty");
        }

        try {
            context.getLogger().log("Processing Message : " + input);
            ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, input);
            Future<RecordMetadata> future = producer.send(record);
            RecordMetadata metadata = future.get();
            return "Message sent to topic: " + metadata.topic() + " partition: " + metadata.partition() + " offset: " + metadata.offset();
        } catch (Exception e) {
            context.getLogger().log("Error sending message to Kafka: " + e.getMessage());
            return "Error sending message to Kafka: " + e.getMessage();
        }finally {
            producer.close();
        }

    }


    public Properties readConfig(String configFile) throws IOException {
        Properties props = new Properties();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(configFile)) {
            if (input == null) {
                throw new IOException(configFile + " not found.");
            }
            props.load(input);
        }

        // Add Kafka producer specific properties
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        return props;
    }

}
