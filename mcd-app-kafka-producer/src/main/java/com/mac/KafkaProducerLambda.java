package com.mac;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.w3c.dom.Document;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;

/**
 * KafkaProducerLambda is a class that handles S3 bucket events and sends the data to a Kafka topic.
 * <p>
 * This class contains methods to process an S3 event, read an object from an S3 bucket, and read a properties file.
 * The processS3Event method reads an S3EventModel object and a Lambda Context object as input, reads the S3 bucket and object information from the S3EventModel, reads the object from the S3 bucket, parses the XML file, extracts the relevant data, and sends the extracted data to a Kafka topic.
 * The readS3Object method takes the name of an S3 bucket and an object key as input, uses the AWS SDK to create an S3 client and sends a GetObjectRequest to the S3 bucket, unzips the file and parses the XML content into a Document object.
 * The readConfig method takes the name of a properties file as input, uses the ClassLoader's getResourceAsStream method to read the file, loads the properties from the file into a Properties object, and adds two Kafka producer specific properties to the Properties object.
 *
 * @author Vijay Krishna
 */
public class KafkaProducerLambda implements RequestHandler<Object, String> {

    private static final String TOPIC_NAME = "raw_STLD_restaurant_transaction";


    private KafkaProducer<String, String> kafkaProducer;

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
//        number of milliseconds a producer is willing to wait before sending a batch out.
//        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        //TODO: Enable this when Schema registry is available
//        props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY_URL);
        props.put("security.protocol", protocol);
        props.put("sasl.mechanism", mechanism);
        props.put("sasl.jaas.config", jaasConfig);
        kafkaProducer = new KafkaProducer<>(props);
    }

    /**
     * Processes an S3 event and sends the data to a Kafka topic.
     * <p>
     * This method takes a Document object representing an XML file and a Lambda Context object as input.
     * It reads the XML file, extracts the relevant data, and sends the extracted data to a Kafka topic.
     * The method uses a KafkaProducer to send the data to the Kafka topic.
     * If an error occurs during the process, the method logs the error message and breaks the loop of sending messages.
     *
     * @param stldDoc The Document object representing the XML file.
     * @param context The Lambda context object containing runtime information.
     * @return A string message indicating the result of the operation.
     */
    public String processDocument(Document stldDoc, Context context) {
        long startTime = System.currentTimeMillis();

        context.getLogger().log("ENTRY - Method: processDocument, Timestamp: " + startTime);

        String rawMessageKey = XMLReader.readRawMessageKey(stldDoc, context);
        List<String> rawMessageList = XMLReader.readRawMessageList(stldDoc, context);

        for (String loyalty : rawMessageList) {
            ProducerRecord<String, String> kafkaRecord = new ProducerRecord<>(TOPIC_NAME, rawMessageKey, loyalty);

            try {
                Future<RecordMetadata> future = kafkaProducer.send(kafkaRecord, (metadata, exception) -> {
                    if (exception == null) {
                        context.getLogger().log("Message sent successfully: " + metadata.toString());
                    } else {
                        context.getLogger().log("Error sending message: " + exception.getMessage());
                    }
                });
                RecordMetadata metadata = future.get();
                context.getLogger().log("Message sent to topic: " + metadata.topic() + " partition: " + metadata.partition() + " offset: " + metadata.offset());
            } catch (Exception e) {
                context.getLogger().log("Error producing messages: " + e.getMessage());
                break;
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        context.getLogger().log("EXIT - Method: processDocument, Timestamp: " + endTime + ", Duration: " + duration + "ms");

        return "Processed S3 event and sent to Kafka topic: " + TOPIC_NAME;
    }


    @Override
    public String handleRequest(Object input, Context context) {
        long startTime = System.currentTimeMillis();
        context.getLogger().log("ENTRY - Method: handleRequest, Timestamp: " + startTime);
        try {
            S3EventProcessor s3EventProcessor = new S3EventProcessor();
            return s3EventProcessor.handleRequest(input, context);
        } catch (Exception e) {
            context.getLogger().log("Error occurred while processing S3 input: " + e.getMessage());
        } finally {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            context.getLogger().log("EXIT - Method: handleRequest, Timestamp: " + endTime + ", Duration: " + duration + "ms");
        }
        return null;
    }
}
