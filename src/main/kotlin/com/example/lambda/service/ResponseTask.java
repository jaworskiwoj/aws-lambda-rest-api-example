package com.example.lambda.service;

import org.json.JSONException;
import org.json.JSONObject;

public class ResponseTask {

    public JSONObject prepareResponse(String responseBody) throws JSONException {
        JSONObject responseJson = new JSONObject();

        JSONObject headersJson = new JSONObject();

        headersJson.put("Access-Control-Allow-Headers", "Content-Type, Accept");
        headersJson.put("Access-Control-Allow-Origin", "*");
        headersJson.put("Access-Control-Allow-Methods", "OPTIONS,POST,GET");


        responseJson.put("headers", headersJson);

        if (responseBody != null) {
            responseJson.put("statusCode", 200);

            headersJson.put("Content-Type", "application/json");

            responseJson.put("body", responseBody);
        } else {
            responseJson.put("statusCode", 204);
        }

        return responseJson;
    }

}
