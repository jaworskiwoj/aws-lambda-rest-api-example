package com.example.lambda.util;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class Logger {

    private final LambdaLogger logger;

    public Logger(LambdaLogger logger) {
        this.logger = logger;
    }

    public final void info(String log) {
        if (this.logger != null) {
            this.logger.log(log);
        } else {
            System.out.println(log);
        }

    }

}