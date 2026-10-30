package com.rumi.notification.application;

/**
 * A use case referenced a resource that does not exist.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
