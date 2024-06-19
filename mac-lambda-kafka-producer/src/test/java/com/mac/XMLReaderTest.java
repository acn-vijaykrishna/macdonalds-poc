package com.mac;

import com.mac.XMLReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    String result = xmlReader.readLoyaltyKey();
    // Assert
    assertNotNull(result);
  }
  @Test
  public void shouldReturnListOfLoyaltyWhenReadLoyaltyListIsCalled() {
    //Act
    List<String> result = xmlReader.readLoyaltyList();

    assertNotNull(result);
    assertEquals(104, result.size());
  }


}