package com.mcd;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@Disabled("This test is disabled ")
public class OtherProcessorTest {
    private static final String TOPIC_NAME = "mac_pos";
    private OtherProcessor otherProcessor;
    private ObjectMapper objectMapper;

    @Mock
    private Context context;
    @Mock
    private S3Client s3Client;

    @Mock
    private LambdaLogger lambdaLogger;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        otherProcessor = new OtherProcessor();
        when(context.getLogger()).thenReturn(lambdaLogger);
        objectMapper = new ObjectMapper();
        // Configure the ObjectMapper to handle unknown properties gracefully
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    }

    @Test
    public void testProcessS3Event() throws IOException {


        // Create a Gson instance
        Gson gson = new Gson();
        LinkedHashMap<String, Object> data = null;
        // Path to the JSON file
        String filePath = "src/test/resources/other-event.json";
        try (FileReader reader = new FileReader(filePath)) {
            // Define the type for LinkedHashMap
            Type type = new TypeToken<LinkedHashMap<String, Object>>() {
            }.getType();

            // Convert JSON file to LinkedHashMap
            data = gson.fromJson(reader, type);

            // Print the result
            System.out.println(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        String result = otherProcessor.handleNormalEvent(data, context);
//        assertEquals("Message sent to topic: mac_pos partition: 2 offset: 1", result);
    }

    @Test
    public void testReadConfig() throws IOException {
        Properties props = otherProcessor.readConfig("client.properties");

        assertEquals("SASL_SSL", props.getProperty("security.protocol"));
    }
}
