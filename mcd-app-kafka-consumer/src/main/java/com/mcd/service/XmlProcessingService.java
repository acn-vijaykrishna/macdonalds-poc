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
    private static final String SCHEMA = "/event.avsc";

    public Event parseStringXmlEvent(String xmlString) {
        logger.info("XML Processing parseStringXmlEvent: String XML = {}", xmlString);
        xmlString = "<Event RegId=\"1044\" Time=\"20240601121056\" Type=\"Ev_Custom\" storeId=\"25001000\">\n" +
                "    <Ev_Custom>\n" +
                "        <Info code=\"3605\" data=\"Jmx0Oz94bWwgdmVyc2lvbj0mcXVvdDsxLjAmcXVvdDsgZW5jb2Rpbmc9JnF1b3Q7VVRGLTgmcXVvdDs/Jmd0OyZsdDtpbmZvIG9yZGVyS2V5PSZxdW90O1BPUzAwNDU6ODA3ODA2MDkxJnF1b3Q7IHRhYmxlWm9uZU51bWJlcj0mcXVvdDsxODcmcXVvdDsgLyZndDs=\"/>\n" +
                "    </Ev_Custom>\n" +
                "</Event>";
        try {
            // Clean the XML string
            xmlString = xmlString.trim().replaceFirst("^([\\W]+)<", "<");
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
        Schema schema = new Schema.Parser().parse(AvroConverter.class.getResourceAsStream(SCHEMA));
        AvroConverter converter = new AvroConverter(schema);

        byte[] avroData = converter.toAvro(event);
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
