package com.alertify.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class AlertifyException extends RuntimeException {

    protected final HttpStatus status;

    public AlertifyException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
