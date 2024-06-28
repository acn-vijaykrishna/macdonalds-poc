package com.mac;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



/**
 * Author: Vijay Krishna
 * Kafka Producer as Lambda Function Build to support s3 event and lambda test function events
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
        logger.info("ENTRY - Method: handleRequest, Timestamp: {}", startTime);
        try {
            logger.info("Input Event:" + input.getClass());
            if (input instanceof Map && ((Map<?, ?>) input).containsKey(RECORDS_OBJECT)) {
                logger.info("Received S3 event:" + input);
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

                logger.debug("s3EventModel Generated: "+s3EventModel);
                logger.info("Successfully s3 data set: bucketName = {}, objectKey = {}", bucketName, objectKey);
                return handleS3Event(s3EventModel, context);
            } else {
                logger.warn("Non S3 Event message: {}", input);
                return handleNormalEvent(input, context);
            }
        } catch (Exception ex) {
            logger.error("Error occurred while processing S3 input:",ex);
        } finally {
            long endTime = System.currentTimeMillis();
            logger.info("EXIT - Method: handleRequest, Timestamp: {}, Duration: {} ms", endTime, endTime - startTime);
        }
        return null;
    }

    private String handleS3Event(S3EventModel s3EventModel, Context context) {
        KafkaProducerLambda s3Processor = new KafkaProducerLambda();
        return s3Processor.processS3Event(s3EventModel, context);
    }


    private String handleNormalEvent(Object input, Context context) {
        OtherProcessor otherProcessor = new OtherProcessor();
        return otherProcessor.handleNormalEvent(parseMessage(input), context);
    }

    private String parseMessage(Object input) {
        if (input instanceof Map) {
            try {
                logger.info("Method: parseMessage input :{}", input);
                return OBJECT_MAPPER.writeValueAsString(input);
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

}