package com.alertify.common.exception;

import org.springframework.http.HttpStatus;

public class BadCredentialsException extends AlertifyException {
    public BadCredentialsException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
