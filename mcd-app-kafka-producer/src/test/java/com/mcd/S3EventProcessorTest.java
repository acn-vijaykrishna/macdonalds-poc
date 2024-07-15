package com.mcd;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Document;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class S3EventProcessorTest {

    private S3EventProcessor s3EventProcessor;

    private ObjectMapper objectMapper;
    private S3Event mockS3Event;
    private Context mockContext;
    private S3Client mockS3Client;
    @Mock
    private S3Client s3ClientMock;
    @Mock private S3ClientBuilder s3ClienBuildertMock;
    @Mock
    private Context context;
    @Mock
    private LambdaLogger lambdaLogger;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        s3EventProcessor = new S3EventProcessor();
        context=Mockito.mock(Context.class);
        when(context.getLogger()).thenReturn(lambdaLogger);
        objectMapper = new ObjectMapper();
        // Configure the ObjectMapper to handle unknown properties gracefully
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mockS3Event = Mockito.mock(S3Event.class);
        mockContext = Mockito.mock(Context.class);
        mockS3Client = Mockito.mock(S3Client.class);

    }

    @Test
    public void testHandleRequest() {

        // Create a LinkedHashMap object
        Object input =getMockInputForS3();

        // Call the handleRequest method
        String result = s3EventProcessor.handleRequest(input, context);
    }

    @Test
    public void testReadS3Object() throws IOException {

        // Arrange
        String bucketName = "testBucket";
        String objectKey = "testObject";
        Context mockContext = Mockito.mock(Context.class);

        try (MockedStatic mocked = Mockito.mockStatic(S3Client.class)) {
            mocked.when(S3Client::builder).thenReturn(s3ClienBuildertMock);
            when(s3ClienBuildertMock.region(Region.US_EAST_1)).thenReturn(s3ClienBuildertMock);
            when(s3ClienBuildertMock.build()).thenReturn(s3ClientMock);
            //when s3ClientMock.getObject is called with any GetObjectRequest, then return s3Object
            when(s3ClientMock.getObject(any(GetObjectRequest.class))).thenReturn(getMockObjectResponseFromS3());
            when(mockContext.getLogger()).thenReturn(lambdaLogger);
            // Act
            Document result = s3EventProcessor.readS3Object(bucketName, objectKey, mockContext);

            // Assert
            // Verify the result as per your expectation
            // For example, if you expect the zip file to contain 2 XML files, you can assert that the result size is 2
            assertNotNull(result, "The returned Document should not be null");
        }

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

        map.put("Records", records);

        System.out.println(map);

        return map;
    }

    ResponseInputStream<GetObjectResponse> getMockObjectResponseFromS3() throws FileNotFoundException {
        InputStream testZipStream = new FileInputStream(new File("src/test/resources/POS.zip"));
        return new ResponseInputStream<>(GetObjectResponse.builder().build(), testZipStream);
    }
}
