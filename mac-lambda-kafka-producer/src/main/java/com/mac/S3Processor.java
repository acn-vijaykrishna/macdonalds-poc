package com.mac;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Author: Vijay Krishna
 * Handle S3 bucket events
 */
public class S3Processor {

    private static final String TOPIC_NAME = "raw_STLD_restaurant_transaction";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public String processS3Event(S3EventModel s3EventModel, Context context) {
        context.getLogger().log("S3Event : " + s3EventModel);
        context.getLogger().log("Context : " + context);
        Properties props = null;
        try {
            props = readConfig("client.properties");
        } catch (IOException e) {
            context.getLogger().log("Failed to load configuration: " + e.getMessage());
        }

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        context.getLogger().log("producer : " + producer);

        String bucketName = s3EventModel.getBucketName();
        String objectKey = s3EventModel.getObjectKey();

        context.getLogger().log("BucketName and ObjectKey" + bucketName + objectKey);
        // Read file content from S3
        Document stldDoc = readS3Object(bucketName, objectKey, context);
        String rawMessageKey = XMLReader.readRawMessageKey(stldDoc);
        List<String> rawMessageList = XMLReader.readRawMessageList(stldDoc);

        for(String loyalty : rawMessageList){
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
            }
        }

        return "Processed S3 event and sent to Kafka topic: " + TOPIC_NAME;
    }

    public Document readS3Object(String bucketName, String objectKey, Context context) {
        context.getLogger().log("###### Now Reading from S3 #######");
        Document stldDoc = null;
        try {
            S3Client s3Client = S3Client.builder()
                .region(Region.US_EAST_1)// Adjust region as needed
                .build();
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
            ZipInputStream zipInputStream = new ZipInputStream(s3Object);
            context.getLogger().log(null != zipInputStream ? "###### Object found #######" : "###### Null Object #######");

            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                context.getLogger().log("###### Entry #######" + entry.getName());
                if (!entry.isDirectory() && entry.getName().contains("STLD")) {
                    Scanner scanner = new Scanner(zipInputStream);
                    String xmlContent = scanner.useDelimiter("\\A").next();
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    stldDoc = builder.parse(new InputSource(new StringReader(xmlContent)));
                    zipInputStream.closeEntry();
                    break;
                }
                zipInputStream.closeEntry();
            }
        } catch (Exception e) {
            context.getLogger().log("###### Exception #######" + e.getMessage());
        }
        //return new Scanner(inputStream).useDelimiter("\\A").next();
        return stldDoc;
    }

    public Properties readConfig(String configFile) throws IOException {
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

        return props;
    }

    private String parseMessage(Object input) {
        if (input instanceof Map) {
            try {
                Map<String, Object> inputMap = (Map<String, Object>) input;
                inputMap.put("key", "S3");
                return OBJECT_MAPPER.writeValueAsString(input);
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }
}
