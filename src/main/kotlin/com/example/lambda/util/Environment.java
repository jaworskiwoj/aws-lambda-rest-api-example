package com.example.lambda.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Scanner;

public class Environment {

    public final boolean isDev() {
        String environment = getEnvironment();
        return !("dev".equals(environment) || "prod".equals(environment));
    }

    public String getProperty(String propertyName) {
        try {
            Properties properties = getProperties();
            return properties.getProperty(propertyName);
        } catch (IOException ignored) {
        }
        return null;
    }

    private Properties getProperties() throws IOException {
        String resources = getResourceString();

        Properties properties = new Properties();
        properties.load(new ByteArrayInputStream(resources.getBytes(StandardCharsets.UTF_8)));
        return properties;
    }

    private String getResourceString() {
        String resourceName = getResourcePath();
        return new Scanner(Environment.class.getResourceAsStream(resourceName), "UTF-8").useDelimiter("\\A").next();
    }

    private String getResourcePath() {
        String environment = System.getenv("environment");
        if ("dev".equals(environment)) {
            return "/application-dev.properties";
        } else if ("prod".equals(environment)) {
            return "/application-prod.properties";
        } else {
            return "/application-dev.properties";
        }
    }

    public final String getEnvironment() {
        return System.getenv("environment");
    }

}
