package com.mac;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.serialization.PojoSerializer;
import com.amazonaws.services.lambda.runtime.serialization.events.LambdaEventSerializers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.LinkedHashMap;
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
                ObjectMapper objectMapper = new ObjectMapper();

                Map<String, Object> inputMap = (Map<String, Object>) input;
                String jsonString = objectMapper.writeValueAsString(inputMap);
                context.getLogger().log("jsonString:"+jsonString);

               // Convert JSON string to S3Event
                S3Event s3Event = new S3Event();

                PojoSerializer<S3Event> serializer = LambdaEventSerializers.serializerFor(S3Event.class, ClassLoader.getSystemClassLoader());
               s3Event = serializer.fromJson(jsonString);

                context.getLogger().log("s3Event Generated:"+s3Event);
               // Log the S3Event object for debugging
               context.getLogger().log("S3Event to string: " + s3Event.toString());

                return handleS3Event(s3Event, context);
            } else {
                context.getLogger().log("Non-S3 event detected.");
                return handleNormalEvent(input, context);
            }
        } catch (Exception ex) {
            context.getLogger().log("Exception in handleRequest :" + ex);
        }
        return null;
    }

    private String handleS3Event(S3Event s3Event, Context context) {
        S3Processor s3Processor = new S3Processor();
        return s3Processor.processS3Event(s3Event, context);
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
