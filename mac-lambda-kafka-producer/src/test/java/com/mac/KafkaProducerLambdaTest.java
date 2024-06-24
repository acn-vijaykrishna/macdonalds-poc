package com.mac;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import java.util.*;
import static org.mockito.Mockito.when;

public class KafkaProducerLambdaTest {

    private KafkaProducerLambda kafkaProducerLambda;

    private ObjectMapper objectMapper;

    @Mock
    private Context context;

    @Mock
    private LambdaLogger lambdaLogger;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        kafkaProducerLambda = new KafkaProducerLambda();
        context=Mockito.mock(Context.class);
        when(context.getLogger()).thenReturn(lambdaLogger);
        objectMapper = new ObjectMapper();
        // Configure the ObjectMapper to handle unknown properties gracefully
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    }

    @Test
    public void testHandleRequest() {

        // Create a LinkedHashMap object
        Object input =getMockInputForS3();

        // Call the handleRequest method
        String result = kafkaProducerLambda.handleRequest(input, context);
    }

    private Map<String, Object> getMockInputForS3() {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> innerMap = new HashMap<>();
        Map<String, Object> userIdentity = new HashMap<>();
        Map<String, Object> requestParameters = new HashMap<>();
        Map<String, Object> responseElements = new HashMap<>();
        Map<String, Object> s3 = new HashMap<>();
        Map<String, Object> bucket = new HashMap<>();
        Map<String, Object> object = new HashMap<>();
        Map<String, Object> ownerIdentity = new HashMap<>();

        ownerIdentity.put("principalId", "A1OMQ2X1L9K4RX");

        bucket.put("name", "macdposstore");
        bucket.put("ownerIdentity", ownerIdentity);
        bucket.put("arn", "arn:aws:s3:::macdposstore");

        object.put("key", "POS_STLD_FR_1000_20240601_17.20240601122542.xml");
        object.put("size", 838318);
        object.put("eTag", "2a529a7612da18e1ca9858b0b4c31607");
        object.put("sequencer", "0066730AC724661745");
        object.put("versionId", "1.2");

        s3.put("s3SchemaVersion", "1.0");
        s3.put("configurationId", "macd_s3_lambda_event");
        s3.put("bucket", bucket);
        s3.put("object", object);

        responseElements.put("xAmzRequestId", "J4Q884VHDSKKTZHY");
        responseElements.put("xAmzId2", "w814KpAYX0uHMqk6HpjwVNQvSglfvlD4EYQ8FDTAF2yPvUD2Y/73J8UwcdxVaNTh9uJfNWKmFL44XsG2KXQGq4oofNbON1K2");

        requestParameters.put("sourceIPAddress", "168.94.238.44");

        userIdentity.put("principalId", "AWS:XXXXXXXXXXXXXX:jon.doe@email.com");

        innerMap.put("eventVersion", "2.1");
        innerMap.put("eventSource", "aws:s3");
        innerMap.put("awsRegion", "us-east-1");
        innerMap.put("eventTime", "2024-06-19T16:43:51.312Z");
        innerMap.put("eventName", "ObjectCreated:Put");
        innerMap.put("userIdentity", userIdentity);
        innerMap.put("requestParameters", requestParameters);
        innerMap.put("responseElements", responseElements);
        innerMap.put("s3", s3);

        List<Map<String, Object>> records = new ArrayList<>();
        records.add(innerMap);

        map.put("records", records);

        System.out.println(map);

        return map;
    }
}
