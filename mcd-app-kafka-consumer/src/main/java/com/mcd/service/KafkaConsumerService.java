package com.mcd.service;

import com.mcd.model.Event;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;

@Service
public class KafkaConsumerService {

    private static final Logger logger = LogManager.getLogger(KafkaConsumerService.class);

    @Autowired
    private XmlProcessingService xmlProcessingService;
    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;


    @KafkaListener(topics = "${spring.kafka.topic.input}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(ConsumerRecord<String, String> record) {
        long startTime = System.currentTimeMillis();
        logger.info("ENTRY - Method: listen, Timestamp: {}", startTime);
        String storeId = record.key();
        String message = record.value();
        logger.info("Received message: Key = {}, Value = {}", storeId, message);
        try {
            Event event = xmlProcessingService.parseStringXmlEvent(message);
            //Only Process Loyalty Customers
            if (event != null && event.getEvCustom() != null) {
                String regId = event.getRegId();
                String key = storeId + "-" + regId;
                GenericRecord avroFormat = xmlProcessingService.processEvent(event);
                kafkaProducerService.sendEvent(key, avroFormat);

            } else {
                logger.warn("com.mcd.model.Event parsing returned null for message: {}", message);
            }
        } catch (Exception e) {
            logger.error("Error processing message: Key = {}, Value = {}", storeId, message, e);
        } finally {
            long endTime = System.currentTimeMillis();
            logger.info("EXIT - Method: listen, Timestamp: {}, Duration: {} ms", endTime, endTime - startTime);
        }
    }

}
