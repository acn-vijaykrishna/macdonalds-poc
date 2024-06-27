package com.mcd.service;

import com.mcd.model.Event;
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
    private KafkaTemplate<String, Object> kafkaTemplate;


    @PostConstruct
    public void init() {
        logger.info("Kafka Username: {}", System.getenv("KAFKA_USERNAME"));
        logger.info("Kafka Password: {}", System.getenv("KAFKA_PASSWORD"));
    }

    @KafkaListener(topics = "${spring.kafka.topic.input}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(ConsumerRecord<String, String> record) {
        long startTime = System.currentTimeMillis();
        logger.info("ENTRY - Method: listen, Timestamp: {}", startTime);
        String storeId = record.key();
        String message = record.value();
        logger.info("Received message: Key = {}, Value = {}", storeId, message);
        try {
            Event event = xmlProcessingService.parseStringXmlEvent(message);
            if (event != null) {
                String regId = event.getRegId();
                String averoFormat = xmlProcessingService.processEvent(event);
                String key = storeId + "-" + regId;
                kafkaTemplate.send("${spring.kafka.topic.output}", key, averoFormat);
                logger.info("Successfully sent message to topic: Key = {}, Value = {}", key, averoFormat);
            } else {
                logger.warn("Event parsing returned null for message: {}", message);
            }
        } catch (Exception e) {
            logger.error("Error processing message: Key = {}, Value = {}", storeId, message, e);
        } finally {
            long endTime = System.currentTimeMillis();
            logger.info("EXIT - Method: listen, Timestamp: {}, Duration: {} ms", endTime, endTime - startTime);
        }
    }

}
