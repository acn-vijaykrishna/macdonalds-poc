package com.mac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

public class XMLReaderTest {

  private XMLReader xmlReader;
  private Document document;

  @BeforeEach
  public void setup() {
    xmlReader = new XMLReader();
    document = mock(Document.class);
  }

  @Test
  public void shouldReturnStoreIdWhenReadLoyaltyKeyIsCalled() {
    // Act
    String result = xmlReader.readRawMessageKey(getDoc());
    // Assert
    assertNotNull(result);
  }
  @Test
  public void shouldReturnListOfLoyaltyWhenReadLoyaltyListIsCalled() {
    //Act
    List<String> result = xmlReader.readRawMessageList(getDoc());

    assertNotNull(result);
    assertEquals(104, result.size());
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

      //doc.getDocumentElement().normalize();

      return doc;

    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }


}