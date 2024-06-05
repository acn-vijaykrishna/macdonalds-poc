package com.mac;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Disabled("This test is disabled ")
public class KafkaLambdaHandlerTest {

    @Mock
    private KafkaConsumer<String, String> consumer;

    @Mock
    private Context context;

    @Mock
    private DynamoDbClient dynamoDbClient;

    private KafkaLambdaHandler handler;

    private static final String TOPIC_NAME = "mac_pos";

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        handler = new KafkaLambdaHandler();
    }

    @Test
    public void testHandleRequest() {
        // Mock the ConsumerRecords to return a predefined record
        ConsumerRecord<String, String> record = new ConsumerRecord<>(TOPIC_NAME, 0, 0L, "key", "value");
        ConsumerRecords<String, String> records = new ConsumerRecords<>(Collections.singletonMap(
                new TopicPartition(TOPIC_NAME, 0),
                Collections.singletonList(record)
        ));

        // Configure the mocked consumer to return the mocked records
        when(consumer.poll(any(Duration.class))).thenReturn(records);

        // Mock the context logger
        LambdaLogger logger = mock(LambdaLogger.class);
        when(context.getLogger()).thenReturn(logger);

        // Replace the real consumer with the mocked one
        handler = new KafkaLambdaHandler() {
            @Override
            protected KafkaConsumer<String, String> createKafkaConsumer(Properties props) {
                return consumer;
            }
        };

        // Call the handleRequest method
        String result = handler.handleRequest(null, context);

        // Verify the consumer consumed the records
        verify(consumer, times(1)).poll(any(Duration.class));

        // Assert the result
        assert (result.contains("Received message: value"));
    }
}
