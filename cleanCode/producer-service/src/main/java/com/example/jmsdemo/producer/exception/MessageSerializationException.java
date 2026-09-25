package com.example.jmsdemo.producer.exception;

public class MessageSerializationException extends RuntimeException {

    public MessageSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
