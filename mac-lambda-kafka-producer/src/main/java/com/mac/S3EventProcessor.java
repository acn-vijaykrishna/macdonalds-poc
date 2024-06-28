package com.mac;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
public class S3EventProcessor implements RequestHandler<Object, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String  RECORDS_OBJECT = "Records";
    private static final String S3_OBJECT = "s3";
    private static final String BUCKET_OBJECT = "bucket";
    private static final String OBJECT = "object";
    private static final String NAME = "name";
    private static final String KEY = "key";

    private static final Logger logger = LogManager.getLogger(S3EventProcessor.class);

    //Method to take input of event data as a Map and generate the s3Event
    @Override
    public String handleRequest(Object input, Context context) {
        long startTime = System.currentTimeMillis();
        context.getLogger().log("ENTRY - Method: handleRequest, Timestamp: "+ startTime);
        try {
            context.getLogger().log("Input Event:" + input.getClass());
            if (input instanceof Map && ((Map<?, ?>) input).containsKey(RECORDS_OBJECT)) {
                context.getLogger().log("Received S3 event:" + input);
                //Extract the bucketName and objectKey from the input
                Map<String, Object> inputMap = (Map<String, Object>) input;
                List<Map<String, Object>> records = (List<Map<String, Object>>) inputMap.get(RECORDS_OBJECT);
                Map<String, Object> innerMap = records.get(0);
                Map<String, Object> s3 = (Map<String, Object>) innerMap.get(S3_OBJECT);
                Map<String, Object> bucket = (Map<String, Object>) s3.get(BUCKET_OBJECT);
                String bucketName = (String) bucket.get(NAME);

                Map<String, Object> object = (Map<String, Object>) s3.get(OBJECT);
                String objectKey = (String) object.get(KEY);

                //Generate S3EventModel
                S3EventModel s3EventModel=new S3EventModel();
                s3EventModel.setBucketName(bucketName);
                s3EventModel.setObjectKey(objectKey);

                context.getLogger().log("Successfully s3 data set: bucketName = "+bucketName+", objectKey = "+ objectKey);
                return handleS3Event(s3EventModel, context);
            } else {
                context.getLogger().log("Non S3 Event message: "+ input);
                return handleNormalEvent(input, context);
            }
        } catch (Exception ex) {
            context.getLogger().log("Error occurred while processing S3 input: "+ ex.getMessage());
        } finally {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            context.getLogger().log("EXIT - Method: handleRequest, Timestamp: "+endTime+", Duration: "+duration+"ms");
        }
        return null;
    }

    private String handleS3Event(S3EventModel s3EventModel, Context context) {
        context.getLogger().log("S3Event : "+ s3EventModel);
        String bucketName = s3EventModel.getBucketName();
        String objectKey = s3EventModel.getObjectKey();
        KafkaProducerLambda s3Processor = new KafkaProducerLambda();
        return s3Processor.processDocument(readS3Object(bucketName, objectKey, context), context);
    }


    private String handleNormalEvent(Object input, Context context) {
        OtherProcessor otherProcessor = new OtherProcessor();
        return otherProcessor.handleNormalEvent(parseMessage(input, context), context);
    }

    private String parseMessage(Object input, Context context) {
        if (input instanceof Map) {
            try {
                context.getLogger().log("Method: parseMessage input :"+ input);
                return OBJECT_MAPPER.writeValueAsString(input);
            } catch (IOException e) {
                return null;
            }
        }
        return null;
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




}