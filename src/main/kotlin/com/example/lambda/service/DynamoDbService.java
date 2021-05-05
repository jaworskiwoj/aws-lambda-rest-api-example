package com.example.lambda.service;

import com.example.lambda.util.Environment;
import com.fasterxml.jackson.databind.JsonNode;
import com.example.lambda.util.JsonAttributeValueUtil;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;

import java.util.*;

public class DynamoDbService {

    private final JsonAttributeValueUtil jsonAttributeValueUtil = new JsonAttributeValueUtil();
    private final DynamoDbClient client = DynamoDbClient.builder().region(Region.of(new Environment().getProperty("aws.region"))).build();


    public String getEntity(String tableName, String layerId) {
        Map<String, AttributeValue> keyToGet = new HashMap<>();
        keyToGet.put("key", AttributeValue.builder().s(layerId).build());

        GetItemRequest request = GetItemRequest.builder()
                .key(keyToGet)
                .tableName(tableName)
                .build();

        Map<String, AttributeValue> entity = client.getItem(request).item();

        if (entity != null && !entity.isEmpty()) {
            return jsonAttributeValueUtil.fromAttributeValue(entity).toString();
        } else {
            return null;
        }
    }


    public String saveEntity(String tableName, JsonNode entity) {
        PutItemRequest request = PutItemRequest.builder()
                .item(jsonAttributeValueUtil.toAttributeValues(entity))
                .tableName(tableName)
                .build();

        PutItemResponse response = client.putItem(request);

        if (response != null && !response.attributes().isEmpty()) {
            return jsonAttributeValueUtil.fromAttributeValue(response.attributes()).toString();
        } else {
            return null;
        }
    }

}
