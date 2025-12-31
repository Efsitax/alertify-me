package com.alertify.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends AlertifyException {
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(HttpStatus.NOT_FOUND, resourceName + " not found with " + fieldName + " : " + fieldValue);
    }
}
