package com.mac;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.InputStream;
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
        Properties props = null;
        try {
            props = readConfig("client.properties", context);
        } catch (IOException e) {
            context.getLogger().log("Failed to load configuration: " + e.getMessage());
        }

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        String rawMessageKey = XMLReader.readRawMessageKey(stldDoc, context);
        List<String> rawMessageList = XMLReader.readRawMessageList(stldDoc, context);

        for (String loyalty : rawMessageList) {
            ProducerRecord<String, String> kafkaRecord = new ProducerRecord<>(TOPIC_NAME, rawMessageKey, loyalty);

            try {
                Future<RecordMetadata> future = producer.send(kafkaRecord, (metadata, exception) -> {
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

    /**
     * Reads a properties file and returns it as a Properties object.
     * <p>
     * This method takes the name of a properties file as input.
     * It uses the ClassLoader's getResourceAsStream method to read the file.
     * The properties file is expected to be in the classpath.
     * The method loads the properties from the file into a Properties object.
     * It also adds two Kafka producer specific properties to the Properties object.
     * If the file is not found or an error occurs during the process, the method throws an IOException.
     *
     * @param configFile The name of the properties file.
     * @return A Properties object containing the properties from the file.
     * @throws IOException If the properties file is not found or an error occurs during reading.
     */
    public Properties readConfig(String configFile, Context context) throws IOException {
        long startTime = System.currentTimeMillis();
        context.getLogger().log("ENTRY - Method: readConfig, Timestamp: " + startTime);
        Properties props = new Properties();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(configFile)) {
            if (input == null) {
                throw new IOException(configFile + " not found.");
            }
            props.load(input);
        }

        // Add Kafka producer specific properties
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        context.getLogger().log("EXIT - Method: readConfig, Timestamp: " + endTime + ", Duration: " + duration + "ms");
        return props;
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
