package com.example.udd.exceptionHandling.exception;

public class MalformedQueryException extends RuntimeException {

    public MalformedQueryException(String message) {
        super(message);
    }
}
