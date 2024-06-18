package com.mcd.service;


import com.mcd.model.Event;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import java.io.StringReader;

@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "<your-topic-name>", groupId = "<your-consumer-group>")
    public void listen(String message) {
        System.out.println("Received message: " + message);
        Event event = parseXmlEvent(message);
        if (event != null) {
            processEvent(event);
        }
    }

    private Event parseXmlEvent(String xml) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Event.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (Event) unmarshaller.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void processEvent(Event event) {
        // Process the event as needed
        System.out.println("Processed Event: RegId=" + event.getRegId() +
                ", Type=" + event.getType() + ", Time=" + event.getTime());
    }
}
