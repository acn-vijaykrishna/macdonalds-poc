package com.mcd;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

import java.util.Properties;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.producer.properties.sasl.jaas.config}")
    private String jaasConfig;

    @Value("${spring.kafka.producer.properties.security.protocol}")
    private String protocol;

    @Value("${spring.kafka.producer.properties.sasl.mechanism}")
    private String mechanism;

    @Bean
    public Properties producerFactory() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        props.put(ProducerConfig.RETRIES_CONFIG, 10);
//        number of milliseconds a producer is willing to wait before sending a batch out.
//        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        //TODO: Enable this when Schema registry is available
//        props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY_URL);
        props.put("security.protocol", protocol);
        props.put("sasl.mechanism", mechanism);
        props.put("sasl.jaas.config", jaasConfig);
        return props;
    }

    @Bean
    public KafkaProducer<String, Object> kafkaProducer() {
        return new KafkaProducer<>(producerFactory());
    }


}
