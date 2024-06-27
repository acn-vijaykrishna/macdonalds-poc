package com.mcd.service;

import com.mcd.model.Event;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import java.io.StringReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;


@Service
public class XmlProcessingService {

    private static final Logger logger = LogManager.getLogger(XmlProcessingService.class);

    public Event parseStringXmlEvent(String xmlString) {
        logger.info("XML Processing parseStringXmlEvent: String = {}",xmlString);

        try {
            // Clean the XML string
            xmlString = xmlString.trim().replaceFirst("^([\\W]+)<", "<");
            // Create JAXB context and unmarshaller
            JAXBContext context = JAXBContext.newInstance(Event.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            // Unmarshal XML string into Event object
            StringReader reader = new StringReader(xmlString);
            Event event = (Event) unmarshaller.unmarshal(reader);
            return event;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Event parseXmlEvent(String xml) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Event.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (Event) unmarshaller.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String processEvent(Event event) {
        logger.info("XML Processing Event: Custom = {}, Info = {}", event.getEvCustom(), event.getEvCustom().getInfo());
        if (event != null && event.getEvCustom() != null && event.getEvCustom().getInfo() != null) {
            String info = event.getEvCustom().getInfo().getData();
            String decodedInfo = URLDecoder.decode(info, StandardCharsets.UTF_8);
            return convertToAveroFormat(decodedInfo);
        }
        return null;
    }

    private String convertToAveroFormat(String decodedInfo) {
        // Implement the conversion logic to AVERO format
        // This is a placeholder implementation
        return decodedInfo;
    }
}
