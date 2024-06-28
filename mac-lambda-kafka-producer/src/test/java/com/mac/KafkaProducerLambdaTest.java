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
class KafkaProducerLambdaTest {

  private KafkaProducerLambda kafkaProducerLambda;
  private S3Event mockS3Event;
  private Context mockContext;
  private S3Client mockS3Client;
  @Mock
  private S3Client s3ClientMock;
  @Mock private S3ClientBuilder s3ClienBuildertMock;
  @Mock
  private LambdaLogger lambdaLogger;

  @BeforeEach
  public void setUp() {
    kafkaProducerLambda = new KafkaProducerLambda();
    mockS3Event = Mockito.mock(S3Event.class);
    mockContext = Mockito.mock(Context.class);
    mockS3Client = Mockito.mock(S3Client.class);
  }

  String getMockString() {
    return "test";
  }
}