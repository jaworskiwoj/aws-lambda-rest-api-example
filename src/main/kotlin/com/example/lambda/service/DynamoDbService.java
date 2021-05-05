package com.example.lambda.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;

import java.util.*;

public class DynamoDbService {

  private final DynamoDbClient client = DynamoDbClient.builder().region(Region.EU_CENTRAL_1).build();

  public String getEntity(String tableName, String layerId) throws JsonProcessingException {
    Map<String, AttributeValue> keyToGet = new HashMap<>();
    keyToGet.put("key", AttributeValue.builder().s(layerId).build());

    GetItemRequest request = GetItemRequest.builder()
        .key(keyToGet)
        .tableName(tableName)
        .build();

    Map<String, AttributeValue> entity = client.getItem(request).item();

    if (entity != null && !entity.isEmpty()) {
      return convertToJson(entity);
    } else {
      return null;
    }
  }

  private String convertToJson(Map<String, AttributeValue> entity) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, String> attributeValues = new HashMap<>();
    for (String key : entity.keySet()) {
      attributeValues.put(key, attributeValues.get(key));
    }
    return objectMapper.writeValueAsString(attributeValues);
  }


  public String saveEntity(String tableName, JsonNode entity) throws JsonProcessingException {
    Map<String, AttributeValue> attributeValues = new HashMap<>();
    Iterator<String> fieldNames = entity.fieldNames();
    while (fieldNames.hasNext()) {
      String fieldName = fieldNames.next();
      attributeValues.put(fieldName, AttributeValue.builder().s(entity.findValue(fieldName).asText()).build());
    }


    PutItemRequest request = PutItemRequest.builder()
        .item(attributeValues)
        .tableName(tableName)
        .build();

    PutItemResponse response = client.putItem(request);

    if (response != null && !response.attributes().isEmpty()) {
      return convertToJson(response.attributes());
    } else {
      return null;
    }
  }

}

