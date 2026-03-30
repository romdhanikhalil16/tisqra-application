package com.tisqra.common.exception;

/**
 * Thrown when a requested resource doesn't exist.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(resourceName + " not found with " + fieldName + "=" + fieldValue);
    }
}

