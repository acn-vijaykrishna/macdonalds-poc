package com.mac;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class KafkaLambdaHandler implements RequestHandler<Object, String> {

    private static final String TOPIC_NAME = "curated_loyalty_transaction";
    private static final String GROUP_ID = "mcd-curated-loyalty";
    private static final String API_URL = "https://8xcpa37nf4.execute-api.us-east-1.amazonaws.com/mcd-dev/loyalty";

    private static final String PROPS_FILE = "client.properties";

    @Override
    public String handleRequest(Object input, Context context) {
        long startTime = System.currentTimeMillis();

        context.getLogger().log("ENTRY - Method: Curated Topic Consumer, Timestamp: " + startTime);
        // Set up Kafka consumer properties
        Properties props;

        try {
            props = readConfig(PROPS_FILE);
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
                context.getLogger().log("Processing Record: " + record.value());

                // Call SessionM API
                String response = callSessionMApi();
                context.getLogger().log("API Response: " + response);

                messages.append("Received message: ").append(record.value()).append("\n");
            }
        } catch (Exception e) {
            context.getLogger().log("Error consuming messages: " + e.getMessage());
        } finally {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            context.getLogger().log("EXIT - Method: Method: Curated Topic Consumer, Timestamp: "+ endTime +", Duration: "+ duration +"ms");
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

    private String callSessionMApi() throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(API_URL);
        httpPost.setEntity(new StringEntity("{}", StandardCharsets.UTF_8));
        httpPost.setHeader("Content-Type", "application/json");

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        }
    }
}
