package com.example.lambda.controller;

import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.example.lambda.util.Environment;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.lambda.service.RequestDispatchService;
import com.example.lambda.util.Logger;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class LambdaRequestHandler implements RequestStreamHandler {


    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        String input = IOUtils.toString(inputStream, "UTF-8");
        Logger log = new Logger(context.getLogger());
        log.info("Profile: " + new Environment().getEnvironment());
        log.info("Input: " + input);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode request = objectMapper.readTree(input);

        String responseJson;
        try {
            responseJson = new RequestDispatchService().getResponse(request);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        writer.write(responseJson);
        writer.close();
    }

    public static void main(String[] args) throws IOException {
        String input = "{ \"path\": \"/objectName/1\", \"httpMethod\": \"GET\" }";
        new LambdaRequestHandler().handleRequest(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), System.out, new MockLambdaContext());
    }
}
