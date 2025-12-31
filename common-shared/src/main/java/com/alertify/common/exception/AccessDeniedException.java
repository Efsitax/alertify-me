package com.alertify.common.exception;

import org.springframework.http.HttpStatus;

public class AccessDeniedException extends AlertifyException {
    public AccessDeniedException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
