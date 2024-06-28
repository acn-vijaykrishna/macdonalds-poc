package com.mac;

import com.amazonaws.services.lambda.runtime.Context;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Author: Vijay Krishna
 * Handle S3 bucket events
 */
public class KafkaProducerLambda {

    private static final String TOPIC_NAME = "raw_STLD_restaurant_transaction";
    private static final Logger logger = LogManager.getLogger(KafkaProducerLambda.class);

    /**
     * Processes an S3 event and sends the data to a Kafka topic.
     *
     * This method reads an S3EventModel object and a Lambda Context object as input.
     * It reads the S3 bucket and object information from the S3EventModel.
     * It then reads the object from the S3 bucket, which is expected to be a zipped XML file.
     * The XML file is parsed and the relevant data is extracted.
     * The extracted data is then sent to a Kafka topic.
     *
     * @param s3EventModel The S3 event model containing the bucket and object information.
     * @param context The Lambda context object containing runtime information.
     * @return A string message indicating the result of the operation.
     */
    public String processS3Event(S3EventModel s3EventModel, Context context) {
        long startTime = System.currentTimeMillis();

        context.getLogger().log("ENTRY - Method: processS3Event, Timestamp: " + startTime);
        context.getLogger().log("S3Event : "+ s3EventModel);
        Properties props = null;
        try {
            props = readConfig("client.properties");
        } catch (IOException e) {
            context.getLogger().log("Failed to load configuration: "+ e.getMessage());
        }

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        String bucketName = s3EventModel.getBucketName();
        String objectKey = s3EventModel.getObjectKey();

        // Read file content from S3
        Document stldDoc = readS3Object(bucketName, objectKey, context);
        String rawMessageKey = XMLReader.readRawMessageKey(stldDoc);
        List<String> rawMessageList = XMLReader.readRawMessageList(stldDoc);

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
        context.getLogger().log("EXIT - Method: processS3Event, Timestamp: "+ endTime +", Duration: "+ duration +"ms");

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
