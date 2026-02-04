package com.alertify.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceAlreadyExistsException extends AlertifyException {

    public ResourceAlreadyExistsException(String resourceName, String fieldName, Object fieldValue) {
        super(HttpStatus.CONFLICT, String.format("%s already exists with %s : %s", resourceName, fieldName, fieldValue));
    }
}
