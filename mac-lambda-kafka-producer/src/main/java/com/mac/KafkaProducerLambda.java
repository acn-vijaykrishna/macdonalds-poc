package com.mac;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;



/**
 * Author: Vijay Krishna
 * Kafka Producer as Lambda Function Build to support s3 event and lambda test function events
 */
public class KafkaProducerLambda implements RequestHandler<Object, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String handleRequest(Object input, Context context) {
        try {
            context.getLogger().log("Input Event:" +     input);
            context.getLogger().log("Input Event:" + input.getClass());
            if (input instanceof Map && ((Map<?, ?>) input).containsKey("records")) {
                context.getLogger().log("S3 event detected.");

                Map<String, Object> inputMap = (Map<String, Object>) input;
                List<Map<String, Object>> records = (List<Map<String, Object>>) inputMap.get("records");
                Map<String, Object> innerMap = records.get(0);
                Map<String, Object> s3 = (Map<String, Object>) innerMap.get("s3");
                Map<String, Object> bucket = (Map<String, Object>) s3.get("bucket");
                String bucketName = (String) bucket.get("name");

                Map<String, Object> object = (Map<String, Object>) s3.get("object");
                String objectKey = (String) object.get("key");


                S3EventModel s3EventModel=new S3EventModel();
                s3EventModel.setBucketName(bucketName);
                s3EventModel.setObjectKey(objectKey);

                /*S3Event s3Event =  EventLoader.loadS3Event("/Users/a1567309/IdeaProjects/macdonalds-poc/mac-lambda-kafka-producer/src/test/java/com/mac/s3-event1.json");
                //PojoSerializer<S3Event> serializer = LambdaEventSerializers.serializerFor(S3Event.class, ClassLoader.getSystemClassLoader());
                //S3Event s3Event = serializer.fromJson(jsonString);

                s3Event = objectMapper.readValue(jsonString,S3Event.class);
                final PojoSerializer<S3EventNotification> s3EventSerializer =
                        LambdaEventSerializers.serializerFor(S3EventNotification.class, ClassLoader.getSystemClassLoader()); */


                context.getLogger().log("s3EventModel Generated:"+s3EventModel);
               // Log the S3Event object for debugging
               context.getLogger().log("S3Event to string: " + s3EventModel.toString());

                return handleS3Event(s3EventModel, context);
            } else {
                context.getLogger().log("Non-S3 event detected.");
                return handleNormalEvent(input, context);
            }
        } catch (Exception ex) {
            context.getLogger().log("Exception in handleRequest :" + ex);
        }
        return null;
    }

    private String handleS3Event(S3EventModel s3EventModel, Context context) {
        S3Processor s3Processor = new S3Processor();
        return s3Processor.processS3Event(s3EventModel, context);
    }


    private String handleNormalEvent(Object input, Context context) {
        OtherProcessor otherProcessor = new OtherProcessor();
        return otherProcessor.handleNormalEvent(parseMessage(input), context);
    }

    private String parseMessage(Object input) {
        if (input instanceof Map) {
            try {
                return OBJECT_MAPPER.writeValueAsString(input);
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

}
