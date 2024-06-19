package com.mcd;

import com.mcd.model.Event;
import com.mcd.service.XmlProcessingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = KafkaConsumerApplication.class) // Specify the main application class
public class XmlProcessingServiceTest {

    @Autowired
    private XmlProcessingService xmlProcessingService;

    @Test
    public void testProcessEvent() {
        try {
            // Get the file from the classpath
            URL resource = getClass().getClassLoader().getResource("event.xml");
            if (resource == null) {
                throw new IllegalArgumentException("file not found!");
            } else {
                Path path = Paths.get(resource.toURI());
                String xml = Files.readString(path);
                Event event = xmlProcessingService.parseXmlEvent(xml);
                assertNotNull(event);
                String averoFormat = xmlProcessingService.processEvent(event);
                assertNotNull(averoFormat);

                System.out.println("averoFormat ==>" + averoFormat);
            }
        } catch (Exception ex) {
            System.out.println("Exception" + ex);
            fail("Exception occurred: " + ex.getMessage());
        }
    }
}
