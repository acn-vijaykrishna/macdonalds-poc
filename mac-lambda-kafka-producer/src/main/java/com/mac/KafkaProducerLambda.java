package com.mac;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
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
            if (input instanceof Map && ((Map<?, ?>) input).containsKey("Records")) {
                context.getLogger().log("S3 event detected.");
//                ObjectMapper objectMapper = new ObjectMapper();
//
//                String jsonString = objectMapper.writeValueAsString(input);
//                context.getLogger().log("jsonString:"+jsonString);
//
//                // Register custom deserializer
//                SimpleModule module = new SimpleModule();
//                module.addDeserializer(S3Event.class, new S3EventDeserializer());
//                objectMapper.registerModule(module);
//
//                // Convert JSON string to S3Event
//                S3Event s3Event = objectMapper.readValue(jsonString, S3Event.class);

                // Convert the input map to a JSON string
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonString = objectMapper.writeValueAsString(input);

                // Deserialize the JSON string to S3Event
//                S3Event s3Event = objectMapper.readValue(jsonString, S3Event.class);
                S3Event s3Event = new S3Event();

                // Process the S3Event
//                context.getLogger().log("s3Event: " + s3Event);
//                LinkedHashMap<String, Object> inputMap = (LinkedHashMap<String, Object>) input;
//                Gson gson = new GsonBuilder().create();
//                // Convert inputMap to JSON string
//                String jsonString = gson.toJson(inputMap);
//                context.getLogger().log("jsonString:"+jsonString);
//
//                // Deserialize JSON string to S3Event object
//                S3Event s3Event = gson.fromJson(jsonString, S3Event.class);
//                context.getLogger().log("s3Event Generated:"+s3Event);
//                // Log the S3Event object for debugging
//                context.getLogger().log("S3Event to string: " + s3Event.toString());

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
