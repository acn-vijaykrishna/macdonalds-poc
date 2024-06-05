package com.mac;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class DynamoDb {

    private static final String DYNAMODB_TABLE_NAME = "MacDEvents";


    public void writeToDynamoDB(JsonObject input) {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(input, JsonObject.class);

        Map<String, AttributeValue> item = convertJsonToAttributeValueMap(jsonObject);
        // Ensure event_id exists
        if (!item.containsKey("event_id")) {
            System.err.println("Error: event_id key is missing");
            return;
        }

        // Prepare the PutItemRequest
        PutItemRequest request = PutItemRequest.builder()
                .tableName(DYNAMODB_TABLE_NAME)
                .item(item)
                .build();


        // Write to DynamoDB
        try {
            dbClient().putItem(request);
            System.out.println("Successfully inserted item into DynamoDB: " + jsonObject.get("event_id").getAsString());
        } catch (DynamoDbException e) {
            System.err.println("Error inserting item into DynamoDB: " + e.getMessage());
        }

    }


    public DynamoDbClient dbClient() {
        // Create a DynamoDB  client
        return DynamoDbClient.builder().region(Region.US_EAST_1).build();
    }

    private Map<String, AttributeValue> convertJsonToAttributeValueMap(JsonObject jsonObject) {
        Map<String, AttributeValue> item = new HashMap<>();
        Set<Map.Entry<String, JsonElement>> entries = jsonObject.entrySet();

        for (Map.Entry<String, JsonElement> entry : entries) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            if (value.isJsonObject()) {
                item.put(key, AttributeValue.builder().m(convertJsonToAttributeValueMap(value.getAsJsonObject())).build());
            } else if (value.isJsonPrimitive()) {
                item.put(key, convertJsonPrimitiveToAttributeValue(value.getAsJsonPrimitive()));
            } else if (value.isJsonNull()) {
                item.put(key, AttributeValue.builder().nul(true).build());
            }
        }

        return item;
    }

    private AttributeValue convertJsonPrimitiveToAttributeValue(JsonPrimitive jsonPrimitive) {
        if (jsonPrimitive.isBoolean()) {
            return AttributeValue.builder().bool(jsonPrimitive.getAsBoolean()).build();
        } else if (jsonPrimitive.isNumber()) {
            return AttributeValue.builder().n(jsonPrimitive.getAsString()).build();
        } else if (jsonPrimitive.isString()) {
            return AttributeValue.builder().s(jsonPrimitive.getAsString()).build();
        } else {
            throw new IllegalArgumentException("Unsupported JSON primitive type: " + jsonPrimitive);
        }
    }
}
