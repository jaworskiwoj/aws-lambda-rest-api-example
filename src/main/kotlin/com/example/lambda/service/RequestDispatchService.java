package com.example.lambda.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;

public class RequestDispatchService {

    public String getResponse(JsonNode request) throws JsonProcessingException, JSONException {
        String requestURL = getRequestUrl(request);
        String requestMethod = getRequestMethod(request);
        String entityName = requestURL.substring(1).split("/")[0];

        ResponseTask responseTask = new ResponseTask();
        DynamoDbService dynamoDbService = new DynamoDbService();

        String responseBody;
        if ("GET".equals(requestMethod)) {
            String[] requestParts = requestURL.split("/");
            responseBody = dynamoDbService.getEntity(entityName, requestParts[requestParts.length - 1]);
        } else if ("POST".equals(requestMethod)) {
            responseBody = dynamoDbService.saveEntity(entityName, getRequestBody(request));
        } else {
            throw new RuntimeException("Unknown method exception");
        }

        return responseTask.prepareResponse(responseBody).toString();

    }

    private String getRequestUrl(JsonNode request) {
        return request.get("path").textValue();
    }

    private String getRequestMethod(JsonNode request) {
        return request.get("httpMethod").textValue();
    }

    private JsonNode getRequestBody(JsonNode request) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(request.get("body").textValue());
    }

}
