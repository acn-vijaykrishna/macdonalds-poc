package com.mcd;

import com.mcd.model.Event;
import com.mcd.service.XmlProcessingService;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;

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
                System.out.println("event ==>"+event.toString());
                assertNotNull(event);
                GenericRecord averoFormat = xmlProcessingService.processEvent(event);
                assertNotNull(averoFormat);

            }
        } catch (Exception ex) {
            System.out.println("Exception" + ex);
            fail("Exception occurred: " + ex.getMessage());
        }
    }

    @Test
    public void testProcessStringEvent() {
        String xmlString = "<Event RegId=\"496\" Time=\"20240601122321\" Type=\"Ev_Custom\" storeId=\"25001000\">\n          \n    <Ev_Custom>\n                \n        <Info code=\"0000\" data=\"Created%7C%7B%22businessDate%22%3A%2220240601%22%2C%22externalCustomerRef%22%3A%22%22%2C%22externalOrderRef%22%3A%221m2g0hyhdy%22%2C%22identificationType%22%3A%22SCANN%22%2C%22orderDate%22%3A%222024-06-01T12%3A22%3A55.000%2B0000%22%2C%22terminalId%22%3A%22KIOSK%22%2C%22burns%22%3A%5B%7B%22rewardRef%22%3A%2283874599%22%2C%22rewardType%22%3A%22POINT%22%2C%22pmix%22%3A%2213464%22%2C%22usedPoints%22%3A%2275%22%7D%5D%7D\"/>\n              \n    </Ev_Custom>\n        \n</Event>\n";

        try {
            // Create JAXB context and unmarshaller
            JAXBContext context = JAXBContext.newInstance(Event.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            // Unmarshal XML string into Event object
            StringReader reader = new StringReader(xmlString);
            Event event = (Event) unmarshaller.unmarshal(reader);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
