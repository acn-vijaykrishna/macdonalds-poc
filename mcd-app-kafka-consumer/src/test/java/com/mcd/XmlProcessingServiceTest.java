package com.mcd;

import com.mcd.KafkaConsumerApplication;
import com.mcd.model.Event;
import com.mcd.service.XmlProcessingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = KafkaConsumerApplication.class) // Specify the main application class
public class XmlProcessingServiceTest {

    @Autowired
    private XmlProcessingService xmlProcessingService;

    @Test
    public void testProcessEvent() {
        try {
            String xml = Files.readString(Path.of("event.xml"));

            Event event = xmlProcessingService.parseXmlEvent(xml);
            assertNotNull(event);

            String averoFormat = xmlProcessingService.processEvent(event);
            assertNotNull(averoFormat);

            System.out.println(averoFormat);
        } catch (Exception ex) {
            System.out.println("Exception" + ex);
        }
    }
}
