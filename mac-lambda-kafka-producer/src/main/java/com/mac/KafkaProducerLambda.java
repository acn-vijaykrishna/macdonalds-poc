package com.mac;

import com.amazonaws.services.lambda.runtime.Context;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
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
import java.util.Properties;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * KafkaProducerLambda is a class that handles S3 bucket events and sends the data to a Kafka topic.
 *
 * This class contains methods to process an S3 event, read an object from an S3 bucket, and read a properties file.
 * The processS3Event method reads an S3EventModel object and a Lambda Context object as input, reads the S3 bucket and object information from the S3EventModel, reads the object from the S3 bucket, parses the XML file, extracts the relevant data, and sends the extracted data to a Kafka topic.
 * The readS3Object method takes the name of an S3 bucket and an object key as input, uses the AWS SDK to create an S3 client and sends a GetObjectRequest to the S3 bucket, unzips the file and parses the XML content into a Document object.
 * The readConfig method takes the name of a properties file as input, uses the ClassLoader's getResourceAsStream method to read the file, loads the properties from the file into a Properties object, and adds two Kafka producer specific properties to the Properties object.
 *
 * @author Vijay Krishna
 */
public class KafkaProducerLambda {

    private static final String TOPIC_NAME = "raw_STLD_restaurant_transaction";

    /**
     * Processes an S3 event and sends the data to a Kafka topic.
     *
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
            props = readConfig("client.properties");
        } catch (IOException e) {
            context.getLogger().log("Failed to load configuration: "+ e.getMessage());
        }

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        String rawMessageKey = XMLReader.readRawMessageKey(stldDoc, context);
        List<String> rawMessageList = XMLReader.readRawMessageList(stldDoc, context);

        for(String loyalty : rawMessageList){
            ProducerRecord<String, String> kafkaRecord = new ProducerRecord<>(TOPIC_NAME, rawMessageKey, loyalty);

            try {
                producer.send(kafkaRecord, (metadata, exception) -> {
                    if (exception == null) {
                        context.getLogger().log("Message sent to topic: "
                            +metadata.topic()+" partition: "+metadata.partition()+" offset: "
                            + metadata.offset());
                    } else {
                        context.getLogger().log("Error sending message: "+ exception.getMessage());
                    }
                });
            } catch (Exception e) {
                context.getLogger().log("Error producing messages: "+ e.getMessage());
                break;
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        context.getLogger().log("EXIT - Method: processDocument, Timestamp: "+ endTime +", Duration: "+ duration +"ms");

        return "Processed S3 event and sent to Kafka topic: " + TOPIC_NAME;
    }

    /**
     * Reads an object from an S3 bucket and returns it as a Document.
     *
     * This method takes the name of an S3 bucket and an object key as input.
     * It uses the AWS SDK to create an S3 client and sends a GetObjectRequest to the S3 bucket.
     * The object is expected to be a zipped XML file.
     * The method unzips the file and parses the XML content into a Document object.
     * If the object is not found or an error occurs during the process, the method returns null.
     *
     * @param bucketName The name of the S3 bucket.
     * @param objectKey The key of the object in the S3 bucket.
     * @param context The Lambda context object containing runtime information.
     * @return A Document object representing the XML content of the S3 object, or null if the object is not found or an error occurs.
     */
    public Document readS3Object(String bucketName, String objectKey, Context context) {
        context.getLogger().log("###### Now Reading from S3 Bucket: "+bucketName+", Object: "+objectKey+"#######");
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
                context.getLogger().log("Zip File Entry: {}" + entry.getName());
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
            context.getLogger().log("Exception occured while reading object "+ e.getMessage());
        }
        return stldDoc;
    }

    /**
     * Reads a properties file and returns it as a Properties object.
     *
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
}
