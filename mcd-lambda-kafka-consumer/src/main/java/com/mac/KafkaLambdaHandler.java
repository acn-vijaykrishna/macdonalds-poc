package com.mac;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;


import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;


public class KafkaLambdaHandler implements RequestHandler<Object, String> {

    private static final String TOPIC_NAME = "curated_loyalty_transaction";
    private static final String GROUP_ID = "mcd-curated-loyalty";

    @Override
    public String handleRequest(Object input, Context context) {
        // Set up Kafka consumer properties
        Properties props;

        try {
            props = readConfig("client.properties");
        } catch (IOException e) {
            context.getLogger().log("Failed to load configuration: " + e.getMessage());
            return "Error loading configuration";
        }


        // Create Kafka consumer
        KafkaConsumer<String, String> consumer = createKafkaConsumer(props);
        consumer.subscribe(Collections.singletonList(TOPIC_NAME));

        // Poll for records
        StringBuilder messages = new StringBuilder();
        try {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
            for (ConsumerRecord<String, String> record : records) {
                context.getLogger().log("Processing Records : " + record.value());
                // TODO: Call SessionM API
                messages.append("Received message: ").append(record.value()).append("\n");
            }
        } catch (Exception e) {
            context.getLogger().log("Error consuming messages: " + e.getMessage());
        } finally {
            consumer.close();
        }
        // Return the collected messages
        return messages.toString();
    }

    // Protected method to create KafkaConsumer for easy mocking
    protected KafkaConsumer<String, String> createKafkaConsumer(Properties props) {
        return new KafkaConsumer<>(props);
    }

    private Properties readConfig(String configFile) throws IOException {
        Properties props = new Properties();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(configFile)) {
            if (input == null) {
                throw new IOException(configFile + " not found.");
            }
            props.load(input);
        }

        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return props;
    }


}