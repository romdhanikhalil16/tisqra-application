package com.tisqra.common.exception;

public class NotFoundException extends ResourceNotFoundException {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(resourceName, fieldName, fieldValue);
    }
}

