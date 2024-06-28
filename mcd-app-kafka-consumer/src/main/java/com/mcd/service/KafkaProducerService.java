package com.mcd.service;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Properties;
import java.util.concurrent.Future;

@Service
public class KafkaProducerService {

    private static final Logger logger = LogManager.getLogger(KafkaProducerService.class);

    private final String SCHEMA_REGISTRY_URL = "http://localhost:8081";

    private final String schemaPath = "src/main/avro/event.asvc";

    private KafkaProducer<String, Object> kafkaProducer;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.topic.output}")
    private String outputTopic;


    @Value("${spring.kafka.producer.properties.sasl.jaas.config}")
    private String jaasConfig;

    @Value("${spring.kafka.producer.properties.security.protocol}")
    private String protocol;

    @Value("${spring.kafka.producer.properties.sasl.mechanism}")
    private String mechanism;

    @PostConstruct
    public void init() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
//        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        props.put(ProducerConfig.RETRIES_CONFIG, 10);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
//        props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY_URL);
        props.put("security.protocol", protocol);
        props.put("sasl.mechanism", mechanism);
        props.put("sasl.jaas.config", jaasConfig);
        kafkaProducer = new KafkaProducer<>(props);
    }

    public void sendEvent(String key, Object value) {
        try {
            logger.info("value.toString(): {}", value.toString());
            ProducerRecord<String, Object> record = new ProducerRecord<>(outputTopic, key, value.toString());
            Future<RecordMetadata> future = kafkaProducer.send(record, (metadata, exception) -> {
                if (exception == null) {
                    logger.info("Message sent successfully: {}", metadata.toString());
                } else {
                    logger.error("Error sending message: {}", exception.getMessage());
                }
            });
            RecordMetadata metadata = future.get();
            logger.info("Successfully sent message to topic: Topic ={} Key = {}, Value = {} Partition = {}, Offset = {}",
                    outputTopic, key, value, metadata.partition(), metadata.offset());

        } catch (Exception e) {
            logger.error("Error sending message to topic: Topic ={} Key = {}, Value = {}", outputTopic, key, value, e);
        }
    }

    @PreDestroy
    public void close() {
        if (kafkaProducer != null) {
            kafkaProducer.close();
        }
    }
}
