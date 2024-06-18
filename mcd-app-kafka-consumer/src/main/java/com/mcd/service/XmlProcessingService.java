package com.mcd.service;

import com.mcd.model.Event;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Service
public class XmlProcessingService {

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
        if (event != null && event.getEvCustom() != null && event.getEvCustom().getInfo() != null) {
            String info = event.getEvCustom().getInfo();
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
