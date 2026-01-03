package com.alertify.common.exception;

import org.springframework.http.HttpStatus;

public class ScrapeFailedException extends AlertifyException{

    public ScrapeFailedException(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message);
    }
}
