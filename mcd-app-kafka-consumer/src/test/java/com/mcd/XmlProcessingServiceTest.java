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
        String xmlString = "<Event RegId=\"1044\" Time=\"20240601121056\" Type=\"Ev_Custom\" storeId=\"25001000\">\n" +
                "    <Ev_Custom>\n" +
                "        <Info code=\"3605\" data=\"Jmx0Oz94bWwgdmVyc2lvbj0mcXVvdDsxLjAmcXVvdDsgZW5jb2Rpbmc9JnF1b3Q7VVRGLTgmcXVvdDs/Jmd0OyZsdDtpbmZvIG9yZGVyS2V5PSZxdW90O1BPUzAwNDU6ODA3ODA2MDkxJnF1b3Q7IHRhYmxlWm9uZU51bWJlcj0mcXVvdDsxODcmcXVvdDsgLyZndDs=\"/>\n" +
                "    </Ev_Custom>\n" +
                "</Event>";

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
