package com.mac;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.w3c.dom.Document;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.*;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaProducerLambdaTest {

  private KafkaProducerLambda kafkaProducerLambda;
  private S3Event mockS3Event;
  private Context mockContext;
  private S3Client mockS3Client;
  @Mock
  private S3Client s3ClientMock;
  @Mock private S3ClientBuilder s3ClienBuildertMock;
  @Mock
  private LambdaLogger mockLogger;

  @BeforeEach
  public void setUp() {
    kafkaProducerLambda = new KafkaProducerLambda();
    mockS3Event = Mockito.mock(S3Event.class);
    mockContext = Mockito.mock(Context.class);
    mockS3Client = Mockito.mock(S3Client.class);
    mockLogger = Mockito.mock(LambdaLogger.class);
  }

  String getMockString() {
    return "test";
  }

  @Test
  public void testReadS3Object() throws IOException {

    String bucketName = "testBucket";
    String objectKey = "testObject";
    Context mockContext = Mockito.mock(Context.class);

    try (MockedStatic mocked = Mockito.mockStatic(S3Client.class)) {
      mocked.when(S3Client::builder).thenReturn(s3ClienBuildertMock);
      when(s3ClienBuildertMock.region(Region.US_EAST_1)).thenReturn(s3ClienBuildertMock);
      when(s3ClienBuildertMock.build()).thenReturn(s3ClientMock);
      when(s3ClientMock.getObject(any(GetObjectRequest.class))).thenReturn(getMockObjectResponseFromS3());
      Mockito.when(mockContext.getLogger()).thenReturn(mockLogger);
      Document result = kafkaProducerLambda.readS3Object(bucketName, objectKey, mockContext);
      assertNotNull(result, "The returned Document should not be null");
    }
  }

   @Test
   public void readConfigTest() {
    try {
      Mockito.when(mockContext.getLogger()).thenReturn(mockLogger);
      Properties props = kafkaProducerLambda.readConfig("client.properties",mockContext);
      assertNotNull(props, "The returned Properties object should not be null");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  @Disabled
  public void processDocumentTest() throws FileNotFoundException {
    Mockito.when(mockContext.getLogger()).thenReturn(mockLogger);

    try (MockedStatic mocked = Mockito.mockStatic(S3Client.class)) {
      mocked.when(S3Client::builder).thenReturn(s3ClienBuildertMock);
      when(s3ClienBuildertMock.region(Region.US_EAST_1)).thenReturn(s3ClienBuildertMock);
      when(s3ClienBuildertMock.build()).thenReturn(s3ClientMock);
      when(s3ClientMock.getObject(any(GetObjectRequest.class))).thenReturn(getMockObjectResponseFromS3());
      Document doc = kafkaProducerLambda.readS3Object("testBucket", "testObject", mockContext);
      String result = kafkaProducerLambda.processDocument(doc, mockContext);
      assertNotNull(result, "The returned String should not be null");
    }
  }

  ResponseInputStream<GetObjectResponse> getMockObjectResponseFromS3() throws FileNotFoundException {
    InputStream testZipStream = new FileInputStream(new File("src/test/resources/POS.zip"));
    return new ResponseInputStream<>(GetObjectResponse.builder().build(), testZipStream);
  }

}