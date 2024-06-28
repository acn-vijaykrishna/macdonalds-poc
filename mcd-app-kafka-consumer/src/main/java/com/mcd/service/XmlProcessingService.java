package com.mcd.service;

import com.mcd.model.Event;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.SeekableByteArrayInput;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.io.DatumReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.apache.avro.generic.GenericRecord;

import java.io.IOException;

import java.io.StringReader;


@Service
public class XmlProcessingService {

    private static final Logger logger = LogManager.getLogger(XmlProcessingService.class);

    public Event parseStringXmlEvent(String xmlString) {
        logger.info("XML Processing parseStringXmlEvent: String XML = {}", xmlString);
        try {
            // Clean the XML string
            xmlString = xmlString.trim().replaceFirst("^([\\W]+)<", "<");

            // Log the cleaned XML string
            logger.info("Cleaned XML String: {}", xmlString);

            // Create JAXB context and unmarshaller
            JAXBContext context = JAXBContext.newInstance(Event.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            StringReader reader = new StringReader(xmlString);
            Event event = (Event) unmarshaller.unmarshal(reader);
            return event;
        } catch (JAXBException e) {
            logger.error("JAXBException while parsing XML: {} ", xmlString, e);
        } catch (Exception e) {
            logger.error("Exception while parsing XML: {} ", xmlString, e);
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


    public GenericRecord processEvent(Event event) {
        try {
            logger.info("XML Processing Event: Custom = {}, Info = {}", event.toString());
            if (event != null && event.getEvCustom() != null && event.getEvCustom().getInfo() != null) {
                return convertToAvroFormat(event);
            }
        } catch (Exception e) {
            logger.error("Error processing Event: Event ID = {} ", event.getRegId(), e);
        }
        return null;
    }

    private GenericRecord convertToAvroFormat(Event event) throws IOException {
        logger.info("Convert To Avro Format");
        Schema schema = new Schema.Parser().parse(AvroConverter.class.getResourceAsStream("/event.avsc"));
        logger.info("schema ==>" + schema);
        AvroConverter converter = new AvroConverter(schema);
        logger.info("converter ==>" + converter);

        byte[] avroData = converter.toAvro(event);
        logger.info("avroData ==>" + avroData);

        // Convert byte[] back to GenericRecord
        try (SeekableByteArrayInput input = new SeekableByteArrayInput(avroData)) {
            DatumReader<GenericRecord> datumReader = new GenericDatumReader<>(schema);
            try (DataFileReader<GenericRecord> dataFileReader = new DataFileReader<>(input, datumReader)) {
                if (dataFileReader.hasNext()) {
                    return dataFileReader.next();
                } else {
                    throw new IOException("No data found in Avro data");
                }
            }
        }
    }


}
