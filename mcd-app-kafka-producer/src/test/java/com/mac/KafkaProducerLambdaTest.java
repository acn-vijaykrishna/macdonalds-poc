package com.mac;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class KafkaProducerLambdaTest {

  private KafkaProducerLambda kafkaProducerLambda;
  private Context mockContext;
  @Mock
  private LambdaLogger mockLogger;

  @BeforeEach
  public void setUp() {
    kafkaProducerLambda = new KafkaProducerLambda();
    mockContext = Mockito.mock(Context.class);
    mockLogger = Mockito.mock(LambdaLogger.class);
  }

  String getMockString() {
    return "test";
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
  public void processDocumentTest() {
    Mockito.when(mockContext.getLogger()).thenReturn(mockLogger);
      String result = kafkaProducerLambda.processDocument(getDoc(), mockContext);
      assertNotNull(result, "The returned String should not be null");
  }
  Document getDoc() {
    try {
      // Get the ClassLoader
      ClassLoader classLoader = XMLReader.class.getClassLoader();

      // Load the file as an InputStream
      InputStream inputStream = classLoader.getResourceAsStream("test.xml");

      // Check if the file was found
      if (inputStream == null) {
        throw new IllegalArgumentException("file not found! " + "example.xml");
      }

      // Parse the XML file
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(inputStream);
      return doc;

    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}