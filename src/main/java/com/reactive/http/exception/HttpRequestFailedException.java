package com.reactive.http.exception;

public class HttpRequestFailedException extends RuntimeException {

    public HttpRequestFailedException(String message) {
        super(message);
    }
}
