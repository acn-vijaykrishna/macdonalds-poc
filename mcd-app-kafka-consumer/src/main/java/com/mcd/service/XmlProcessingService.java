package com.mcd.service;

import com.mcd.model.Event;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.apache.avro.Schema;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.StringReader;


@Service
public class XmlProcessingService {

    private static final Logger logger = LogManager.getLogger(XmlProcessingService.class);

    public Event parseStringXmlEvent(String xmlString) {
        logger.info("XML Processing parseStringXmlEvent: String XML = {}", xmlString);
        xmlString = "<com.mcd.model.Event RegId=\"1044\" Time=\"20240601121056\" Type=\"com.mcd.model.Ev_Custom\" storeId=\"25001000\">\n" +
                "    <com.mcd.model.Ev_Custom>\n" +
                "        <com.mcd.model.Info code=\"3605\" data=\"Jmx0Oz94bWwgdmVyc2lvbj0mcXVvdDsxLjAmcXVvdDsgZW5jb2Rpbmc9JnF1b3Q7VVRGLTgmcXVvdDs/Jmd0OyZsdDtpbmZvIG9yZGVyS2V5PSZxdW90O1BPUzAwNDU6ODA3ODA2MDkxJnF1b3Q7IHRhYmxlWm9uZU51bWJlcj0mcXVvdDsxODcmcXVvdDsgLyZndDs=\"/>\n" +
                "    </com.mcd.model.Ev_Custom>\n" +
                "</com.mcd.model.Event>";
        try {

            // Clean the XML string
            xmlString = xmlString.trim().replaceFirst("^([\\W]+)<", "<");

            // Create JAXB context and unmarshaller
            JAXBContext context = JAXBContext.newInstance(Event.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            // Unmarshal XML string into com.mcd.model.Event object
            StringReader reader = new StringReader(xmlString);
            Event event = (Event) unmarshaller.unmarshal(reader);
            return event;
        } catch (JAXBException e) {
            logger.error("JAXBException while parsing XML:{} ", xmlString, e);
        } catch (Exception e) {
            logger.error("Exception while parsing XML:{} ", xmlString, e);
        }
        return null;
    }

    public Event parseXmlEvent(String xml) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Event.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (Event) unmarshaller.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            logger.error("Error Parsing XML = {} ", xml, e);
            return null;
        }
    }

    public byte[] processEvent(Event event) {
        try {
            logger.info("XML Processing com.mcd.model.Event: Custom = {}, com.mcd.model.Info = {}", event.toString());
            if (event != null && event.getEvCustom() != null && event.getEvCustom().getInfo() != null) {
                return convertToAveroFormat(event);
            }
        } catch (Exception e) {
            logger.error("Error processing com.mcd.model.Event: com.mcd.model.Event ID = {} ", event.getRegId(), e);
        }
        return null;
    }

    private byte[] convertToAveroFormat(Event event) {
        logger.info("Convert To Avero Format");
        try {
            Schema schema = new Schema.Parser().parse(AvroConverter.class.getResourceAsStream("/event.avsc"));
            logger.info("schema ==>" + schema);
            AvroConverter converter = new AvroConverter(schema);
            logger.info("converter ==>" + converter);
            byte[] avroData = converter.toAvro(event);
            logger.info("avroData ==>" + avroData);
            return avroData;
        } catch (Exception e) {
            logger.error("Error processing com.mcd.model.Event: com.mcd.model.Event ID = {} ", event.getRegId(), e);
        }
        return null;
    }


}
