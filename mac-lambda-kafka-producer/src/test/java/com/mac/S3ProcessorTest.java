package com.mac;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3ProcessorTest {

  private S3Processor s3Processor;
  private S3Event mockS3Event;
  private Context mockContext;
  private S3Client mockS3Client;
  @Mock
  private S3Client s3ClientMock;
  @Mock private S3ClientBuilder s3ClienBuildertMock;

  private LambdaLogger logger = new LambdaLogger() {
    @Override
    public void log(String message) {
      System.out.println(message);
    }

    @Override
    public void log(byte[] bytes) {
      System.out.println(bytes);
    }
  };
  @BeforeEach
  public void setUp() {
    s3Processor = new S3Processor();
    mockS3Event = Mockito.mock(S3Event.class);
    mockContext = Mockito.mock(Context.class);
    mockS3Client = Mockito.mock(S3Client.class);
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
      //when(s3ClientMock.getObject(any(GetObjectRequest.class))).thenReturn(getMockString());
      // Act
      Document result = s3Processor.readS3Object(bucketName, objectKey, mockContext);

      // Assert
      // Verify the result as per your expectation
      // For example, if you expect the zip file to contain 2 XML files, you can assert that the result size is 2
      assertNotNull(result, "The returned Document should not be null");
    }

  }

  ResponseInputStream<GetObjectResponse> getMockObjectResponseFromS3() throws FileNotFoundException {
    InputStream testZipStream = new FileInputStream(new File("src/test/resources/POS.zip"));
    return new ResponseInputStream<>(GetObjectResponse.builder().build(), testZipStream);
  }

  String getMockString() {
    return "test";
  }
}