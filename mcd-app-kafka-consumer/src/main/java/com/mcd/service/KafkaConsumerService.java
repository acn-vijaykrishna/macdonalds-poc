package com.mcd.service;


import com.mcd.model.Event;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @Autowired
    private XmlProcessingService xmlProcessingService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "${spring.kafka.topic.input}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(ConsumerRecord<String, String> record) {
        String storeId = record.key();
        String message = record.value();
        Event event = xmlProcessingService.parseXmlEvent(message);
        if (event != null) {
            String regId = event.getRegId();
            String averoFormat = xmlProcessingService.processEvent(event);
            String key = storeId + "-" + regId;
            kafkaTemplate.send("${spring.kafka.topic.output}", key, averoFormat);
        }
    }

}
